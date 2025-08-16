import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Núcleo del sistema (dispatcher):
 * - Cola con prioridad de llamadas entrantes
 * - Operadores toman llamadas y las pasan a despacho
 * - Despacho asigna unidades libres según tipo
 * - Generador de llamadas aleatorias (hilo)
 */
public class SistemaEmergencias {

    // Colas
    private final PriorityBlockingQueue<Llamada> entrantes = new PriorityBlockingQueue<>();
    private final BlockingQueue<Llamada> aDespacho = new LinkedBlockingQueue<>();

    // Asignación 1-1: unidad -> caso
    private final Map<UnidadEmergencia, SynchronousQueue<Llamada>> asignaciones = new ConcurrentHashMap<>();

    // Estado global (para la GUI)
    public final List<Llamada> todas = Collections.synchronizedList(new ArrayList<>());
    public final List<UnidadEmergencia> unidades = Collections.synchronizedList(new ArrayList<>());

    // Hilos vivos para poder detener si hiciera falta
    private final List<Thread> hilos = new ArrayList<>();

    // ==== API ====
    public void registrarUnidad(UnidadEmergencia u){
        asignaciones.put(u, new SynchronousQueue<>());
        unidades.add(u);
    }

    public void enviarLlamada(Llamada l){
        todas.add(l);
        entrantes.offer(l);
    }

    public Llamada tomarEntrante() throws InterruptedException {
        return entrantes.take();
    }

    public void mandarADespacho(Llamada l){
        l.estado = Llamada.Estado.DESPACHADA;
        aDespacho.offer(l);
    }

    public Llamada tomarAsignacion(UnidadEmergencia u) throws InterruptedException {
        return asignaciones.get(u).take();
    }

    // ==== Hilos del sistema ====
    public void iniciarDespachador(){
        Thread t = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()){
                try {
                    Llamada l = aDespacho.take();
                    UnidadEmergencia libre = esperarUnidadLibre(l.tipo);
                    if (libre != null) {
                        l.unidadId = libre.id;
                        asignaciones.get(libre).put(l);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "DISPATCHER");
        t.setDaemon(true);
        t.start();
        hilos.add(t);
    }

    private UnidadEmergencia esperarUnidadLibre(Llamada.Tipo tipo) throws InterruptedException {
        while (true){
            synchronized (unidades){
                for (UnidadEmergencia u : unidades){
                    if (u.tipo == tipo && !u.ocupado) return u;
                }
            }
            Thread.sleep(120);
        }
    }

    // Generador de llamadas
    public void iniciarGenerador(){
        Thread t = new Thread(new GeneradorLlamadas(this), "CALL-GEN");
        t.setDaemon(true);
        t.start();
        hilos.add(t);
    }

    // Operadores
    public void iniciarOperadores(int cuantos){
        for (int i = 1; i <= cuantos; i++){
            Thread t = new Thread(new Operador(i, this), "OP-"+i);
            t.setDaemon(true);
            t.start();
            hilos.add(t);
        }
    }

    // Unidades (cada una corre su hilo)
    public void iniciarUnidades(List<UnidadEmergencia> lista){
        for (UnidadEmergencia u : lista){
            registrarUnidad(u);
            Thread t = new Thread(u, "UNIT-"+u.id);
            t.setDaemon(true);
            t.start();
            hilos.add(t);
        }
    }

    // ==== util interno ====
    private static final class GeneradorLlamadas implements Runnable {
        private final SistemaEmergencias s;
        private final AtomicInteger seq = new AtomicInteger(1);
        private final Random rnd = new Random();

        private GeneradorLlamadas(SistemaEmergencias s){ this.s = s; }

        @Override public void run() {
            while(!Thread.currentThread().isInterrupted()){
                try {
                    Thread.sleep(700 + rnd.nextInt(1200));
                    int id = seq.getAndIncrement();
                    Llamada.Tipo tipo = Llamada.Tipo.values()[rnd.nextInt(3)];
                    Llamada.Prioridad pr = pickPriority();
                    Point loc = new Point(rnd.nextInt(100), rnd.nextInt(100));
                    s.enviarLlamada(new Llamada(id, tipo, pr, loc));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        private Llamada.Prioridad pickPriority(){
            int x = new Random().nextInt(100);
            if (x < 20) return Llamada.Prioridad.CRITICA;
            if (x < 45) return Llamada.Prioridad.ALTA;
            if (x < 80) return Llamada.Prioridad.MEDIA;
            return Llamada.Prioridad.BAJA;
        }
    }
}
