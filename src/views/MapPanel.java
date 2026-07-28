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
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

import models.MapPoint;
import structures.graphs.Graph;
import structures.graphs.PathResult;

public class MapPanel extends JPanel {

    private static final Color COLOR_FONDO = new Color(248, 249, 251);
    private static final Color COLOR_ARISTA = new Color(210, 214, 220);
    private static final Color COLOR_NODO = Color.WHITE;
    private static final Color COLOR_NODO_BORDE = new Color(150, 158, 168);
    private static final Color COLOR_ACENTO = new Color(37, 99, 235);
    private static final Color COLOR_VISITADO = new Color(191, 210, 250);
    private static final Color COLOR_INICIO = new Color(22, 163, 74);
    private static final Color COLOR_DESTINO = new Color(220, 38, 38);
    private static final Color COLOR_TEXTO = new Color(55, 60, 68);

    private static final double INCREMENTO_POR_TICK = 0.025;
    private static final int INTERVALO_TIMER_MS = 45;

    private Graph<MapPoint> grafo;
    private Image imagenFondo;

    private Set<MapPoint> visitados;
    private List<MapPoint> path;
    private MapPoint nodoInicio;
    private MapPoint nodoDestino;

    private double progreso;
    private Timer timerAnimacion;

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

    public void mostrarResultado(PathResult<MapPoint> resultado, MapPoint inicio, MapPoint destino) {
        detenerAnimacion();

        if (resultado == null) {
            limpiarResultado();
            return;
        }

        this.visitados = resultado.getVisitados();
        this.path = new ArrayList<>(resultado.getPath());
        this.nodoInicio = inicio;
        this.nodoDestino = destino;
        this.progreso = 0;

        iniciarAnimacion();
    }

    public void limpiarResultado() {
        detenerAnimacion();
        this.visitados = null;
        this.path = null;
        this.nodoInicio = null;
        this.nodoDestino = null;
        this.progreso = 0;
        repaint();
    }

    private void iniciarAnimacion() {
        if (path == null || path.size() < 2) {
            repaint();
            return;
        }

        double maximo = path.size() - 1;

        timerAnimacion = new Timer(INTERVALO_TIMER_MS, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                progreso += INCREMENTO_POR_TICK;
                if (progreso >= maximo) {
                    progreso = maximo;
                    detenerAnimacion();
                }
                repaint();
            }
        });
        timerAnimacion.start();
    }

    private void detenerAnimacion() {
        if (timerAnimacion != null) {
            timerAnimacion.stop();
            timerAnimacion = null;
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
        g2.setColor(COLOR_ARISTA);
        g2.setStroke(new BasicStroke(1.5f));
        java.util.Map<MapPoint, Set<MapPoint>> mapa = grafo.getGraph();
        for (java.util.Map.Entry<MapPoint, Set<MapPoint>> entry : mapa.entrySet()) {
            MapPoint origen = entry.getKey();
            for (MapPoint destino : entry.getValue()) {
                g2.drawLine(origen.getX(), origen.getY(), destino.getX(), destino.getY());
            }
        }
    }

    private void dibujarPath(Graphics2D g2) {
        if (path == null || path.size() < 2 || progreso <= 0) {
            return;
        }

        g2.setColor(COLOR_ACENTO);
        g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int segmentoActual = (int) Math.floor(progreso);
        double fraccion = progreso - segmentoActual;

        for (int i = 0; i < segmentoActual && i < path.size() - 1; i++) {
            MapPoint a = path.get(i);
            MapPoint b = path.get(i + 1);
            g2.drawLine(a.getX(), a.getY(), b.getX(), b.getY());
        }

        if (segmentoActual < path.size() - 1 && fraccion > 0) {
            MapPoint a = path.get(segmentoActual);
            MapPoint b = path.get(segmentoActual + 1);
            int xIntermedio = a.getX() + (int) ((b.getX() - a.getX()) * fraccion);
            int yIntermedio = a.getY() + (int) ((b.getY() - a.getY()) * fraccion);
            g2.drawLine(a.getX(), a.getY(), xIntermedio, yIntermedio);
        }
    }

    private void dibujarNodos(Graphics2D g2) {
        int radio = 14;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));

        for (MapPoint punto : grafo.getNodes()) {
            Color borde = COLOR_NODO_BORDE;
            Color relleno = COLOR_NODO;
            float grosor = 2f;

            if (visitados != null && visitados.contains(punto)) {
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

            int x = punto.getX() - radio / 2;
            int y = punto.getY() - radio / 2;

            g2.setColor(relleno);
            g2.fillOval(x, y, radio, radio);
            g2.setColor(borde);
            g2.setStroke(new BasicStroke(grosor));
            g2.drawOval(x, y, radio, radio);

            g2.setColor(COLOR_TEXTO);
            g2.drawString(punto.getId(), punto.getX() + radio, punto.getY() + 4);
        }
    }
}