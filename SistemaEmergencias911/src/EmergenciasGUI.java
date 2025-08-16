import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

public class EmergenciasGUI extends JFrame {

    private final SistemaEmergencias s;

    public EmergenciasGUI(SistemaEmergencias s) {
        super("Sistema de Emergencias 911 — Simulador");
        this.s = s;

        // Configuración de la ventana principal
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // Configuración del JTabbedPane
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 16));

        // Panel 1: Llamadas
        CallsModel callsModel = new CallsModel(s);
        JTable callsTable = new JTable(callsModel);
        customizeTable(callsTable);
        JPanel p1 = new JPanel(new BorderLayout());
        p1.add(new JScrollPane(callsTable), BorderLayout.CENTER);
        p1.add(barraSuperior(), BorderLayout.NORTH);
        tabs.add("Panel de Llamadas", p1);

        // Panel 2: Despacho
        UnitsModel unitsModel = new UnitsModel(s);
        JTable unitsTable = new JTable(unitsModel);
        customizeTable(unitsTable);
        JPanel p2 = new JPanel(new BorderLayout());
        p2.add(new JScrollPane(unitsTable), BorderLayout.CENTER);
        tabs.add("Panel de Despacho", p2);

        // Panel 3: Mapa
        MapPanel mapa = new MapPanel(s);
        JPanel p3 = new JPanel(new BorderLayout());
        p3.add(mapa, BorderLayout.CENTER);
        tabs.add("Mapa Simulado", p3);

        // Estilo de la ventana y disposición
        add(tabs);
        setBackground(new Color(245, 245, 245));

        // Refrescar UI cada 400 ms
        Timer t = new Timer(400, e -> {
            callsModel.fireTableDataChanged();
            unitsModel.fireTableDataChanged();
            mapa.repaint();
        });
        t.start();
    }

    private void customizeTable(JTable table) {
        // Diseño personalizado para la tabla
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.setGridColor(new Color(220, 220, 220));
        table.setSelectionBackground(new Color(0, 120, 255));
        table.setSelectionForeground(Color.WHITE);
        table.setBackground(new Color(255, 255, 255));
    }

    private JComponent barraSuperior() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setBackground(new Color(0, 120, 255));
        bar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel info = new JLabel("Simulando llamadas… Prioridad: CRITICA > ALTA > MEDIA > BAJA");
        info.setForeground(Color.WHITE);

        // Botón para crear llamada manual (ahora verde)
        JButton crear = new JButton("Crear llamada manual");
        crear.setFont(new Font("Segoe UI", Font.BOLD, 14));
        crear.setBackground(new Color(0, 180, 100));  // Verde en lugar de azul
        crear.setForeground(Color.WHITE);  // Texto blanco
        crear.setFocusPainted(false);
        crear.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 85), 2)); // Verde oscuro para el borde
        crear.addActionListener(e -> UtilUI.crearLlamadaManual(s));

        bar.add(info);
        bar.add(crear);
        return bar;
    }

    // ======= MODELOS DE TABLA =======
    private static final class CallsModel extends AbstractTableModel {
        private final SistemaEmergencias s;
        private final String[] cols = {"ID", "Tipo", "Prioridad", "Estado", "Operador", "Unidad"};
        private CallsModel(SistemaEmergencias s) {
            this.s = s;
        }

        @Override
        public int getRowCount() {
            return s.todas.size();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(int c) {
            return cols[c];
        }

        @Override
        public Object getValueAt(int r, int c) {
            Llamada x = s.todas.get(r);
            return switch (c) {
                case 0 -> x.id;
                case 1 -> x.tipo;
                case 2 -> x.prioridad;
                case 3 -> x.estado;
                case 4 -> x.operadorId == null ? "-" : x.operadorId;
                case 5 -> x.unidadId == null ? "-" : x.unidadId;
                default -> "";
            };
        }
    }

    private static final class UnitsModel extends AbstractTableModel {
        private final SistemaEmergencias s;
        private final String[] cols = {"Unidad", "Tipo", "Ocupado", "Caso actual"};
        private UnitsModel(SistemaEmergencias s) {
            this.s = s;
        }

        @Override
        public int getRowCount() {
            return s.unidades.size();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(int c) {
            return cols[c];
        }

        @Override
        public Object getValueAt(int r, int c) {
            UnidadEmergencia u = s.unidades.get(r);
            return switch (c) {
                case 0 -> u.id;
                case 1 -> u.tipo;
                case 2 -> u.ocupado ? "Sí" : "No";
                case 3 -> (u.casoActual == null ? "-" : ("Llamada #" + u.casoActual.id));
                default -> "";
            };
        }
    }

    // ======= MAPA =======
    private static final class MapPanel extends JPanel {
        private final SistemaEmergencias s;

        private MapPanel(SistemaEmergencias s) {
            this.s = s;
            setPreferredSize(new Dimension(480, 360));
            setBackground(new Color(245, 245, 245));  // Fondo gris claro
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Grilla simple
            g2.setColor(new Color(230, 230, 230));
            for (int i = 0; i < 10; i++) {
                int x = i * getWidth() / 10;
                int y = i * getHeight() / 10;
                g2.drawLine(x, 0, x, getHeight());
                g2.drawLine(0, y, getWidth(), y);
            }

            // Llamadas por estado
            synchronized (s.todas) {
                for (Llamada c : s.todas) {
                    int x = (int) (c.ubicacion.x / 100.0 * getWidth());
                    int y = (int) (c.ubicacion.y / 100.0 * getHeight());
                    switch (c.estado) {
                        case EN_COLA -> g2.setColor(new Color(180, 180, 180));
                        case EN_ATENCION -> g2.setColor(new Color(255, 165, 0));
                        case DESPACHADA -> g2.setColor(new Color(0, 120, 255));
                        case RESUELTA -> g2.setColor(new Color(0, 170, 80));
                    }
                    int r = 10;
                    g2.fillOval(x - r / 2, y - r / 2, r, r);
                }
            }

            // Unidades (cuadrados)
            synchronized (s.unidades) {
                g2.setStroke(new BasicStroke(2f));
                for (UnidadEmergencia u : s.unidades) {
                    int x = (u.id * 37) % getWidth();
                    int y = (u.id * 53) % getHeight();
                    g2.setColor(u.tipo == Llamada.Tipo.AMBULANCIA ? new Color(200, 0, 0)
                            : u.tipo == Llamada.Tipo.POLICIA ? new Color(0, 0, 160)
                            : new Color(255, 100, 0));
                    g2.drawRect(x, y, 14, 14);
                    if (u.ocupado) g2.fillRect(x + 3, y + 3, 8, 8);
                }
            }
        }
    }

    // ======= Util =======
    private static final class UtilUI {
        static void crearLlamadaManual(SistemaEmergencias s) {
            Llamada.Tipo tipo = (Llamada.Tipo) JOptionPane.showInputDialog(
                    null, "Tipo de emergencia:", "Nueva llamada",
                    JOptionPane.QUESTION_MESSAGE, null,
                    Llamada.Tipo.values(), Llamada.Tipo.AMBULANCIA);

            Llamada.Prioridad pr = (Llamada.Prioridad) JOptionPane.showInputDialog(
                    null, "Prioridad:", "Nueva llamada",
                    JOptionPane.QUESTION_MESSAGE, null,
                    Llamada.Prioridad.values(), Llamada.Prioridad.MEDIA);

            if (tipo == null || pr == null) return;

            int id = (int) (System.currentTimeMillis() % 100000);
            java.util.Random r = new java.util.Random();
            Point loc = new Point(r.nextInt(100), r.nextInt(100));
            s.enviarLlamada(new Llamada(id, tipo, pr, loc));
        }
    }
}
