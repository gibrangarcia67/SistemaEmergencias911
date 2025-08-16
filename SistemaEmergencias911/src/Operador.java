public class Operador implements Runnable {
    public final int id;
    private final SistemaEmergencias s;

    public Operador(int id, SistemaEmergencias s){
        this.id = id; this.s = s;
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()){
            try {
                Llamada l = s.tomarEntrante();              // cola con prioridad
                l.estado = Llamada.Estado.EN_ATENCION;
                l.operadorId = id;
                Thread.sleep(800);                           // simula entrevista/registro
                s.mandarADespacho(l);                        // va a despacho
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
