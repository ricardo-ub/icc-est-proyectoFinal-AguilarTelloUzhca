package views;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

import models.MapPoint;
import models.VisualizationMode;
import structures.graphs.Graph;
import structures.graphs.PathResult;

public class MapPanel extends JPanel {

    // Tamaño de referencia sobre el que se ubican los nodos (coordenadas "de
    // diseño")
    private static int ANCHO_DISENO = 700;
    private static int ALTO_DISENO = 500;

    private static Color COLOR_FONDO = new Color(24, 26, 30);
    private static Color COLOR_ARISTA = new Color(120, 126, 138);
    private static Color COLOR_NODO = new Color(235, 236, 240);
    private static Color COLOR_NODO_BORDE = new Color(90, 96, 106);
    private static Color COLOR_ACENTO = new Color(96, 165, 250);
    private static Color COLOR_VISITADO = new Color(51, 84, 138);
    private static Color COLOR_INICIO = new Color(74, 222, 128);
    private static Color COLOR_DESTINO = new Color(248, 113, 113);
    private static Color COLOR_TEXTO = new Color(240, 241, 244);
    private static Color COLOR_TEXTO_SOMBRA = new Color(0, 0, 0, 170);

    // Velocidad con la que se pinta la ruta encontrada
    private static double INCREMENTO_POR_TICK = 0.045;
    private static int INTERVALO_TIMER_MS = 30;

    // Velocidad con la que se revelan los nodos visitados (modo exploración)
    private static double INCREMENTO_VISITADOS_POR_TICK = 0.5;
    private static int INTERVALO_VISITADOS_MS = 110;

    private Graph<MapPoint> grafo;
    private Image imagenFondo;

    private Set<MapPoint> visitados;
    private List<MapPoint> visitadosOrdenados;
    private List<MapPoint> path;
    private MapPoint nodoInicio;
    private MapPoint nodoDestino;
    private VisualizationMode modo;

    private double progreso;
    private double progresoVisitados;
    private boolean mostrarVisitadosProgresivo;

    private Timer timerVisitados;
    private Timer timerPath;

    public MapPanel() {
        setBackground(COLOR_FONDO);
    }

    public void setGrafo(Graph<MapPoint> grafo) {
        this.grafo = grafo;
        repaint();
    }

    public void cargarImagenFondo(String ruta) {
        try {
            imagenFondo = ImageIO.read(new File(ruta));
        } catch (Exception e) {
            System.out.println("No se pudo cargar la imagen: " + e.getMessage());
            imagenFondo = null;
        }
        repaint();
    }

    // ---------- Conversión entre coordenadas de diseño y coordenadas de pantalla
    // ----------

    private double getEscalaX() {
        return Math.max(1, getWidth()) / (double) ANCHO_DISENO;
    }

    private double getEscalaY() {
        return Math.max(1, getHeight()) / (double) ALTO_DISENO;
    }

    private int[] aPantalla(MapPoint punto) {
        int x = (int) (punto.getX() * getEscalaX());
        int y = (int) (punto.getY() * getEscalaY());
        return new int[] { x, y };
    }

    // Usado por MainFrame para convertir un click del mouse a coordenadas de
    // diseño,
    // antes de crear un nuevo MapPoint.
    public int[] convertirClickACoordenadas(int xPantalla, int yPantalla) {
        int xDiseno = (int) (xPantalla / getEscalaX());
        int yDiseno = (int) (yPantalla / getEscalaY());
        return new int[] { xDiseno, yDiseno };
    }

    public void mostrarResultado(PathResult<MapPoint> resultado, MapPoint inicio, MapPoint destino,
            VisualizationMode modo) {

        detenerAnimaciones();

        if (resultado == null) {
            limpiarResultado();
            return;
        }

        this.visitados = resultado.getVisitados();
        this.visitadosOrdenados = new ArrayList<>(resultado.getVisitados());
        this.path = new ArrayList<>(resultado.getPath());
        this.nodoInicio = inicio;
        this.nodoDestino = destino;
        this.modo = modo;

        this.progreso = 0;
        this.progresoVisitados = 0;
        this.mostrarVisitadosProgresivo = (modo == VisualizationMode.EXPLORATION);

        if (mostrarVisitadosProgresivo && !visitadosOrdenados.isEmpty()) {
            iniciarAnimacionVisitados();
        } else {
            iniciarAnimacionPath();
        }

        repaint();
    }

    public void limpiarResultado() {
        detenerAnimaciones();
        this.visitados = null;
        this.visitadosOrdenados = null;
        this.path = null;
        this.nodoInicio = null;
        this.nodoDestino = null;
        this.progreso = 0;
        this.progresoVisitados = 0;
        this.mostrarVisitadosProgresivo = false;
        repaint();
    }

    private void iniciarAnimacionVisitados() {
        double maximo = visitadosOrdenados.size();

        timerVisitados = new Timer(INTERVALO_VISITADOS_MS, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                progresoVisitados += INCREMENTO_VISITADOS_POR_TICK;
                if (progresoVisitados >= maximo) {
                    progresoVisitados = maximo;
                    timerVisitados.stop();
                    iniciarAnimacionPath();
                }
                repaint();
            }
        });
        timerVisitados.start();
    }

    private void iniciarAnimacionPath() {
        if (path == null || path.size() < 2) {
            repaint();
            return;
        }

        double maximo = path.size() - 1;

        timerPath = new Timer(INTERVALO_TIMER_MS, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                progreso += INCREMENTO_POR_TICK;
                if (progreso >= maximo) {
                    progreso = maximo;
                    timerPath.stop();
                }
                repaint();
            }
        });
        timerPath.start();
    }

    private void detenerAnimaciones() {
        if (timerVisitados != null) {
            timerVisitados.stop();
            timerVisitados = null;
        }
        if (timerPath != null) {
            timerPath.stop();
            timerPath = null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        if (imagenFondo != null) {
            g2.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
        }

        if (grafo == null) {
            return;
        }

        dibujarAristas(g2);
        dibujarPath(g2);
        dibujarNodos(g2);
    }

    private void dibujarAristas(Graphics2D g2) {
        g2.setStroke(new BasicStroke(1.6f));
        Map<MapPoint, Set<MapPoint>> mapa = grafo.getGraph();

        for (Map.Entry<MapPoint, Set<MapPoint>> entry : mapa.entrySet()) {
            MapPoint origen = entry.getKey();
            int[] pOrigen = aPantalla(origen);

            for (MapPoint destino : entry.getValue()) {
                boolean bidireccional = mapa.containsKey(destino) && mapa.get(destino).contains(origen);
                int[] pDestino = aPantalla(destino);

                g2.setColor(COLOR_ARISTA);
                g2.drawLine(pOrigen[0], pOrigen[1], pDestino[0], pDestino[1]);

                if (!bidireccional) {
                    dibujarFlecha(g2, pOrigen, pDestino);
                }
            }
        }
    }

    private void dibujarFlecha(Graphics2D g2, int[] origen, int[] destino) {
        double dx = destino[0] - origen[0];
        double dy = destino[1] - origen[1];
        double distancia = Math.sqrt(dx * dx + dy * dy);
        if (distancia == 0) {
            return;
        }

        double ux = dx / distancia;
        double uy = dy / distancia;
        int puntaX = destino[0] - (int) (ux * 14);
        int puntaY = destino[1] - (int) (uy * 14);

        double angulo = Math.atan2(dy, dx);
        int tam = 7;
        int x1 = puntaX - (int) (tam * Math.cos(angulo - Math.PI / 6));
        int y1 = puntaY - (int) (tam * Math.sin(angulo - Math.PI / 6));
        int x2 = puntaX - (int) (tam * Math.cos(angulo + Math.PI / 6));
        int y2 = puntaY - (int) (tam * Math.sin(angulo + Math.PI / 6));

        int[] xs = { puntaX, x1, x2 };
        int[] ys = { puntaY, y1, y2 };

        g2.setColor(COLOR_ARISTA);
        g2.fillPolygon(xs, ys, 3);
    }

    private void dibujarPath(Graphics2D g2) {
        if (path == null || path.size() < 2 || progreso <= 0) {
            return;
        }

        g2.setColor(COLOR_ACENTO);
        g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int segmentoActual = (int) Math.floor(progreso);
        double fraccion = progreso - segmentoActual;

        for (int i = 0; i < segmentoActual && i < path.size() - 1; i++) {
            int[] a = aPantalla(path.get(i));
            int[] b = aPantalla(path.get(i + 1));
            g2.drawLine(a[0], a[1], b[0], b[1]);
        }

        if (segmentoActual < path.size() - 1 && fraccion > 0) {
            int[] a = aPantalla(path.get(segmentoActual));
            int[] b = aPantalla(path.get(segmentoActual + 1));
            int xIntermedio = a[0] + (int) ((b[0] - a[0]) * fraccion);
            int yIntermedio = a[1] + (int) ((b[1] - a[1]) * fraccion);
            g2.drawLine(a[0], a[1], xIntermedio, yIntermedio);
        }
    }

    private void dibujarNodos(Graphics2D g2) {
        int radio = 14;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));

        for (MapPoint punto : grafo.getNodes()) {
            Color borde = COLOR_NODO_BORDE;
            Color relleno = COLOR_NODO;
            float grosor = 2f;

            boolean marcarVisitado = false;
            if (mostrarVisitadosProgresivo && visitadosOrdenados != null) {
                int indice = visitadosOrdenados.indexOf(punto);
                marcarVisitado = indice >= 0 && indice < (int) progresoVisitados + 1;
            }

            if (marcarVisitado) {
                relleno = COLOR_VISITADO;
                borde = COLOR_ACENTO;
            }
            if (punto.equals(nodoInicio)) {
                borde = COLOR_INICIO;
                grosor = 3f;
            }
            if (punto.equals(nodoDestino)) {
                borde = COLOR_DESTINO;
                grosor = 3f;
            }

            int[] p = aPantalla(punto);
            int x = p[0] - radio / 2;
            int y = p[1] - radio / 2;

            g2.setColor(new Color(0, 0, 0, 60));
            g2.fillOval(x - 1, y + 2, radio + 2, radio + 2);

            g2.setColor(relleno);
            g2.fillOval(x, y, radio, radio);
            g2.setColor(borde);
            g2.setStroke(new BasicStroke(grosor));
            g2.drawOval(x, y, radio, radio);

            String texto = punto.getId();
            int tx = p[0] + radio;
            int ty = p[1] + 4;
            g2.setColor(COLOR_TEXTO_SOMBRA);
            g2.drawString(texto, tx + 1, ty + 1);
            g2.setColor(COLOR_TEXTO);
            g2.drawString(texto, tx, ty);
        }
    }
}