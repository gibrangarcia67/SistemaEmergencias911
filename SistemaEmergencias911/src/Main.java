import javax.swing.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Núcleo
        SistemaEmergencias s = new SistemaEmergencias();

        // Unidades (mínimas necesarias: ambulancia, policía, bomberos)
        s.iniciarUnidades(List.of(
                new UnidadEmergencia(101, Llamada.Tipo.AMBULANCIA, s),
                new UnidadEmergencia(102, Llamada.Tipo.AMBULANCIA, s),
                new UnidadEmergencia(201, Llamada.Tipo.POLICIA, s),
                new UnidadEmergencia(202, Llamada.Tipo.POLICIA, s),
                new UnidadEmergencia(301, Llamada.Tipo.BOMBEROS, s)
        ));

        // Operadores
        s.iniciarOperadores(2);

        // Despachador + generador de llamadas
        s.iniciarDespachador();
        s.iniciarGenerador();

        // GUI
        SwingUtilities.invokeLater(() -> new EmergenciasGUI(s).setVisible(true));
    }
}
