package views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import controllers.MapController;
import models.MapPoint;
import structures.graphs.PathResult;

public class MainFrame extends JFrame {

    private static final Color COLOR_BARRA = Color.WHITE;
    private static final Color COLOR_BORDE = new Color(224, 227, 231);
    private static final Color COLOR_TEXTO_TITULO = new Color(30, 33, 38);
    private static final Color COLOR_TEXTO_SECUNDARIO = new Color(120, 126, 135);
    private static final Color COLOR_ACENTO = new Color(37, 99, 235);
    private static final Color COLOR_BOTON_SECUNDARIO = new Color(241, 243, 245);

    private MapController controller;
    private MapPanel mapPanel;

    private JComboBox<MapPoint> comboInicio;
    private JComboBox<MapPoint> comboDestino;
    private JRadioButton radioBFS;
    private JRadioButton radioDFS;
    private JButton botonEjecutar;
    private JButton botonLimpiar;
    private JLabel labelEstado;

    public MainFrame(MapController controller) {
        this.controller = controller;
        setTitle("Biblioteca Aguilar-Tello-Uzhca - Mapa de Rutas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 700);
        setMinimumSize(new Dimension(750, 550));
        setLocationRelativeTo(null);

        mapPanel = new MapPanel();
        mapPanel.setGrafo(controller.getGraph());
        mapPanel.cargarImagenFondo("src/resources/maps/mapa.png");

        add(crearBarraSuperior(), BorderLayout.NORTH);
        add(mapPanel, BorderLayout.CENTER);
        add(crearPanelControles(), BorderLayout.SOUTH);

        cargarSelectores();
    }

    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(COLOR_BARRA);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDE),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)));

        JLabel titulo = new JLabel("Mapa de Rutas");
        titulo.setForeground(COLOR_TEXTO_TITULO);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 17));
        barra.add(titulo, BorderLayout.WEST);

        labelEstado = new JLabel("Selecciona inicio y destino");
        labelEstado.setForeground(COLOR_TEXTO_SECUNDARIO);
        labelEstado.setFont(new Font("SansSerif", Font.PLAIN, 12));
        labelEstado.setHorizontalAlignment(SwingConstants.RIGHT);
        barra.add(labelEstado, BorderLayout.EAST);

        return barra;
    }

    private JPanel crearPanelControles() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 14));
        panel.setBackground(COLOR_BARRA);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDE));

        comboInicio = crearCombo();
        comboDestino = crearCombo();

        radioBFS = new JRadioButton("BFS", true);
        radioDFS = new JRadioButton("DFS");
        radioBFS.setBackground(COLOR_BARRA);
        radioDFS.setBackground(COLOR_BARRA);
        radioBFS.setFont(new Font("SansSerif", Font.PLAIN, 12));
        radioDFS.setFont(new Font("SansSerif", Font.PLAIN, 12));

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(radioBFS);
        grupo.add(radioDFS);

        botonEjecutar = crearBoton("Ejecutar", COLOR_ACENTO, Color.WHITE);
        botonLimpiar = crearBoton("Limpiar", COLOR_BOTON_SECUNDARIO, COLOR_TEXTO_TITULO);

        botonEjecutar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ejecutarBusqueda();
            }
        });

        botonLimpiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mapPanel.limpiarResultado();
                labelEstado.setText("Selecciona inicio y destino");
            }
        });

        panel.add(crearEtiqueta("Inicio"));
        panel.add(comboInicio);
        panel.add(crearEtiqueta("Destino"));
        panel.add(comboDestino);
        panel.add(separadorVertical());
        panel.add(radioBFS);
        panel.add(radioDFS);
        panel.add(separadorVertical());
        panel.add(botonEjecutar);
        panel.add(botonLimpiar);

        return panel;
    }

    private JComboBox<MapPoint> crearCombo() {
        JComboBox<MapPoint> combo = new JComboBox<>();
        combo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        combo.setBackground(Color.WHITE);
        return combo;
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel label = new JLabel(texto);
        label.setForeground(COLOR_TEXTO_SECUNDARIO);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return label;
    }

    private JPanel separadorVertical() {
        JPanel separador = new JPanel();
        separador.setPreferredSize(new Dimension(1, 24));
        separador.setBackground(COLOR_BORDE);
        return separador;
    }

    private JButton crearBoton(String texto, Color fondo, Color texto2) {
        JButton boton = new JButton(texto);
        boton.setBackground(fondo);
        boton.setForeground(texto2);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
        return boton;
    }

    private void cargarSelectores() {
        comboInicio.removeAllItems();
        comboDestino.removeAllItems();
        for (MapPoint punto : controller.getGraph().getNodes()) {
            comboInicio.addItem(punto);
            comboDestino.addItem(punto);
        }
    }

    private void ejecutarBusqueda() {
        MapPoint inicio = (MapPoint) comboInicio.getSelectedItem();
        MapPoint destino = (MapPoint) comboDestino.getSelectedItem();

        if (inicio == null || destino == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un nodo de inicio y uno de destino.");
            return;
        }

        String algoritmo = radioBFS.isSelected() ? "BFS" : "DFS";
        PathResult<MapPoint> resultado = controller.ejecutar(algoritmo, inicio, destino);

        if (resultado == null || !resultado.isEncontrado()) {
            JOptionPane.showMessageDialog(this, "No se encontró una ruta entre esos puntos.");
            mapPanel.mostrarResultado(resultado, inicio, destino);
            labelEstado.setText("Sin ruta entre " + inicio.getId() + " y " + destino.getId());
            return;
        }

        mapPanel.mostrarResultado(resultado, inicio, destino);
        labelEstado.setText(algoritmo + "  ·  Visitados: " + resultado.getCantidadVisitados()
                + "  ·  Aristas: " + resultado.getCantidadAristas()
                + "  ·  " + resultado.getTiempoEjecucion() + " ms");
    }
}