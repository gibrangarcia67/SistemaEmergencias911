import java.awt.*;
import java.time.Instant;

public class Llamada implements Comparable<Llamada> {
    public enum Tipo { AMBULANCIA, POLICIA, BOMBEROS }
    public enum Prioridad {
        CRITICA(0), ALTA(1), MEDIA(2), BAJA(3);
        public final int peso;
        Prioridad(int p){ this.peso = p; }
    }
    public enum Estado { EN_COLA, EN_ATENCION, DESPACHADA, RESUELTA }

    public final int id;
    public final Tipo tipo;
    public final Prioridad prioridad;
    public final Point ubicacion;        // para el mapa
    public final Instant creadaEn;

    public volatile Estado estado = Estado.EN_COLA;
    public volatile Integer operadorId = null;
    public volatile Integer unidadId = null;

    public Llamada(int id, Tipo tipo, Prioridad prioridad, Point ubicacion){
        this.id = id;
        this.tipo = tipo;
        this.prioridad = prioridad;
        this.ubicacion = ubicacion;
        this.creadaEn = Instant.now();
    }

    @Override
    public int compareTo(Llamada o) {
        int c = Integer.compare(this.prioridad.peso, o.prioridad.peso);
        if (c != 0) return c;
        return this.creadaEn.compareTo(o.creadaEn);
    }
}
