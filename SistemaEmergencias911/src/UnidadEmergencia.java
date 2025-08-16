public class UnidadEmergencia implements Runnable {
    public final int id;
    public final Llamada.Tipo tipo;
    public volatile boolean ocupado = false;
    public volatile Llamada casoActual = null;

    private final SistemaEmergencias s;

    public UnidadEmergencia(int id, Llamada.Tipo tipo, SistemaEmergencias s){
        this.id = id; this.tipo = tipo; this.s = s;
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()){
            try {
                Llamada l = s.tomarAsignacion(this); // bloquea hasta tener caso
                ocupado = true; casoActual = l;

                long ms = switch (l.prioridad){
                    case CRITICA -> 3000;
                    case ALTA    -> 4500;
                    case MEDIA   -> 6000;
                    case BAJA    -> 7500;
                };
                Thread.sleep(ms);

                l.estado = Llamada.Estado.RESUELTA;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                ocupado = false; casoActual = null;
            }
        }
    }
}
