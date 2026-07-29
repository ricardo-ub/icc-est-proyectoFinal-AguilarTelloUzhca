package views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import controllers.MapController;
import models.MapPoint;
import models.VisualizationMode;
import structures.graphs.PathResult;

public class MainFrame extends JFrame {

    private static String RUTA_CONFIGURACION = "src/resources/configuration/graph.txt";

    private static Color COLOR_FONDO_APP = new Color(20, 21, 24);
    private static Color COLOR_BARRA = new Color(28, 30, 34);
    private static Color COLOR_TARJETA = new Color(33, 35, 40);
    private static Color COLOR_BORDE = new Color(52, 55, 61);
    private static Color COLOR_TEXTO_TITULO = new Color(235, 236, 240);
    private static Color COLOR_TEXTO_SECUNDARIO = new Color(150, 155, 163);
    private static Color COLOR_ACENTO = new Color(96, 165, 250);
    private static Color COLOR_BOTON_SECUNDARIO = new Color(45, 48, 54);
    private static Color COLOR_PELIGRO = new Color(220, 90, 90);

    private MapController controller;
    private MapPanel mapPanel;

    private JComboBox<MapPoint> comboInicio;
    private JComboBox<MapPoint> comboDestino;
    private JRadioButton radioBFS;
    private JRadioButton radioDFS;
    private JRadioButton radioExploracion;
    private JRadioButton radioRutaFinal;
    private JButton botonEjecutar;
    private JButton botonLimpiar;

    private JButton botonModoCrearNodo;
    private JComboBox<MapPoint> comboEliminarNodo;
    private JButton botonEliminarNodo;
    private JComboBox<MapPoint> comboOrigenConexion;
    private JComboBox<MapPoint> comboDestinoConexion;
    private JRadioButton radioUni;
    private JRadioButton radioBi;
    private JButton botonAgregarConexion;
    private JButton botonEliminarConexion;

    private JButton botonGuardar;
    private JButton botonCargar;

    private JLabel labelEstado;
    private JTextArea areaLog;

    private boolean modoCrearNodo;

    public MainFrame(MapController controller) {
        this.controller = controller;
        setTitle("Biblioteca Aguilar-Tello-Uzhca - Mapa de Rutas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO_APP);

        mapPanel = new MapPanel();
        mapPanel.setGrafo(controller.getGraph());
        mapPanel.cargarImagenFondo("resources/maps/MAPA.png");
        mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (modoCrearNodo) {
                    crearNodoEnClick(e.getX(), e.getY());
                }
            }
        });

        add(crearBarraSuperior(), BorderLayout.NORTH);
        add(mapPanel, BorderLayout.CENTER);
        add(crearBarraLateral(), BorderLayout.EAST);
        add(crearPanelLog(), BorderLayout.SOUTH);

        actualizarSelectores();
    }

    // ---------- Barra superior ----------

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
        labelEstado.setForeground(COLOR_ACENTO);
        labelEstado.setFont(new Font("SansSerif", Font.PLAIN, 12));
        labelEstado.setHorizontalAlignment(SwingConstants.RIGHT);
        barra.add(labelEstado, BorderLayout.EAST);

        return barra;
    }

    // ---------- Panel lateral (todas las tarjetas) ----------

    private JScrollPane crearBarraLateral() {
        JPanel columna = new JPanel();
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
        columna.setBackground(COLOR_FONDO_APP);
        columna.setBorder(BorderFactory.createEmptyBorder(14, 12, 14, 14));

        columna.add(crearTarjetaRuta());
        columna.add(Box.createVerticalStrut(12));
        columna.add(crearTarjetaEdicionNodos());
        columna.add(Box.createVerticalStrut(12));
        columna.add(crearTarjetaEdicionConexiones());
        columna.add(Box.createVerticalStrut(12));
        columna.add(crearTarjetaPersistencia());
        columna.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(columna);
        scroll.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, COLOR_BORDE));
        scroll.setPreferredSize(new Dimension(300, 0));
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.getViewport().setBackground(COLOR_FONDO_APP);
        return scroll;
    }

    private JPanel crearTarjeta(String titulo) {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(COLOR_TARJETA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE, 1, true),
                BorderFactory.createEmptyBorder(12, 14, 14, 14)));
        tarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, tarjeta.getMaximumSize().height));

        JLabel label = new JLabel(titulo);
        label.setForeground(COLOR_TEXTO_TITULO);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        tarjeta.add(label);
        tarjeta.add(Box.createVerticalStrut(10));

        return tarjeta;
    }

    // ---------- Tarjeta: Ruta ----------

    private JPanel crearTarjetaRuta() {
        JPanel tarjeta = crearTarjeta("Ruta");

        comboInicio = crearComboMapPoint();
        comboDestino = crearComboMapPoint();

        tarjeta.add(crearEtiquetaCampo("Inicio"));
        tarjeta.add(comboInicio);
        tarjeta.add(Box.createVerticalStrut(8));
        tarjeta.add(crearEtiquetaCampo("Destino"));
        tarjeta.add(comboDestino);
        tarjeta.add(Box.createVerticalStrut(10));

        radioBFS = crearRadio("BFS", true);
        radioDFS = crearRadio("DFS", false);
        ButtonGroup grupoAlgoritmo = new ButtonGroup();
        grupoAlgoritmo.add(radioBFS);
        grupoAlgoritmo.add(radioDFS);

        tarjeta.add(crearEtiquetaCampo("Algoritmo"));
        tarjeta.add(crearFilaRadios(radioBFS, radioDFS));
        tarjeta.add(Box.createVerticalStrut(10));

        radioExploracion = crearRadio("Exploración", true);
        radioRutaFinal = crearRadio("Ruta final", false);
        ButtonGroup grupoModo = new ButtonGroup();
        grupoModo.add(radioExploracion);
        grupoModo.add(radioRutaFinal);

        tarjeta.add(crearEtiquetaCampo("Modo de visualización"));
        tarjeta.add(crearFilaRadios(radioExploracion, radioRutaFinal));
        tarjeta.add(Box.createVerticalStrut(12));

        botonEjecutar = crearBoton("Ejecutar", COLOR_ACENTO, Color.WHITE);
        botonLimpiar = crearBoton("Limpiar recorrido", COLOR_BOTON_SECUNDARIO, COLOR_TEXTO_TITULO);
        botonEjecutar.setAlignmentX(Component.LEFT_ALIGNMENT);
        botonLimpiar.setAlignmentX(Component.LEFT_ALIGNMENT);

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
                agregarLog("Recorrido limpiado.");
            }
        });

        tarjeta.add(botonEjecutar);
        tarjeta.add(Box.createVerticalStrut(6));
        tarjeta.add(botonLimpiar);

        return tarjeta;
    }

    // ---------- Tarjeta: crear / eliminar nodos ----------

    private JPanel crearTarjetaEdicionNodos() {
        JPanel tarjeta = crearTarjeta("Nodos");

        botonModoCrearNodo = crearBoton("Crear nodo", COLOR_BOTON_SECUNDARIO, COLOR_TEXTO_TITULO);
        botonModoCrearNodo.setAlignmentX(Component.LEFT_ALIGNMENT);
        botonModoCrearNodo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                alternarModoCrearNodo();
            }
        });

        JLabel ayuda = new JLabel("Actívalo y haz click sobre el mapa");
        ayuda.setForeground(COLOR_TEXTO_SECUNDARIO);
        ayuda.setFont(new Font("SansSerif", Font.ITALIC, 11));
        ayuda.setAlignmentX(Component.LEFT_ALIGNMENT);

        tarjeta.add(botonModoCrearNodo);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(ayuda);
        tarjeta.add(Box.createVerticalStrut(12));

        comboEliminarNodo = crearComboMapPoint();
        botonEliminarNodo = crearBoton("Eliminar nodo", COLOR_PELIGRO, Color.WHITE);
        botonEliminarNodo.setAlignmentX(Component.LEFT_ALIGNMENT);
        botonEliminarNodo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eliminarNodoSeleccionado();
            }
        });

        tarjeta.add(crearEtiquetaCampo("Eliminar"));
        tarjeta.add(comboEliminarNodo);
        tarjeta.add(Box.createVerticalStrut(8));
        tarjeta.add(botonEliminarNodo);

        return tarjeta;
    }

    // ---------- Tarjeta: conexiones ----------

    private JPanel crearTarjetaEdicionConexiones() {
        JPanel tarjeta = crearTarjeta("Conexiones");

        comboOrigenConexion = crearComboMapPoint();
        comboDestinoConexion = crearComboMapPoint();

        tarjeta.add(crearEtiquetaCampo("Desde"));
        tarjeta.add(comboOrigenConexion);
        tarjeta.add(Box.createVerticalStrut(8));
        tarjeta.add(crearEtiquetaCampo("Hasta"));
        tarjeta.add(comboDestinoConexion);
        tarjeta.add(Box.createVerticalStrut(10));

        radioUni = crearRadio("Unidireccional", false);
        radioBi = crearRadio("Bidireccional", true);
        ButtonGroup grupoTipo = new ButtonGroup();
        grupoTipo.add(radioUni);
        grupoTipo.add(radioBi);

        tarjeta.add(crearEtiquetaCampo("Tipo de conexión"));
        tarjeta.add(crearFilaRadios(radioUni, radioBi));
        tarjeta.add(Box.createVerticalStrut(12));

        botonAgregarConexion = crearBoton("Agregar conexión", COLOR_ACENTO, Color.WHITE);
        botonEliminarConexion = crearBoton("Eliminar conexión", COLOR_PELIGRO, Color.WHITE);
        botonAgregarConexion.setAlignmentX(Component.LEFT_ALIGNMENT);
        botonEliminarConexion.setAlignmentX(Component.LEFT_ALIGNMENT);

        botonAgregarConexion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agregarConexionSeleccionada();
            }
        });

        botonEliminarConexion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eliminarConexionSeleccionada();
            }
        });

        tarjeta.add(botonAgregarConexion);
        tarjeta.add(Box.createVerticalStrut(6));
        tarjeta.add(botonEliminarConexion);

        return tarjeta;
    }

    // ---------- Tarjeta: persistencia ----------

    private JPanel crearTarjetaPersistencia() {
        JPanel tarjeta = crearTarjeta("Configuración");

        JLabel ruta = new JLabel(RUTA_CONFIGURACION);
        ruta.setForeground(COLOR_TEXTO_SECUNDARIO);
        ruta.setFont(new Font("SansSerif", Font.PLAIN, 11));
        ruta.setAlignmentX(Component.LEFT_ALIGNMENT);
        tarjeta.add(ruta);
        tarjeta.add(Box.createVerticalStrut(10));

        botonGuardar = crearBoton("Guardar", COLOR_BOTON_SECUNDARIO, COLOR_TEXTO_TITULO);
        botonCargar = crearBoton("Cargar", COLOR_BOTON_SECUNDARIO, COLOR_TEXTO_TITULO);
        botonGuardar.setAlignmentX(Component.LEFT_ALIGNMENT);
        botonCargar.setAlignmentX(Component.LEFT_ALIGNMENT);

        botonGuardar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarConfiguracion();
            }
        });

        botonCargar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cargarConfiguracion();
            }
        });

        tarjeta.add(botonGuardar);
        tarjeta.add(Box.createVerticalStrut(6));
        tarjeta.add(botonCargar);

        return tarjeta;
    }

    // ---------- Panel de log (la "pantallita de información") ----------

    private JPanel crearPanelLog() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BARRA);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDE));
        panel.setPreferredSize(new Dimension(0, 130));

        JLabel titulo = new JLabel("Actividad");
        titulo.setForeground(COLOR_TEXTO_TITULO);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 12));
        titulo.setBorder(BorderFactory.createEmptyBorder(8, 14, 6, 0));
        panel.add(titulo, BorderLayout.NORTH);

        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setBackground(COLOR_BARRA);
        areaLog.setForeground(COLOR_TEXTO_SECUNDARIO);
        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        areaLog.setBorder(BorderFactory.createEmptyBorder(0, 14, 8, 14));

        JScrollPane scroll = new JScrollPane(areaLog);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(COLOR_BARRA);
        panel.add(scroll, BorderLayout.CENTER);

        agregarLog("Aplicación iniciada.");

        return panel;
    }

    // ---------- Helpers de estilo ----------

    private JComboBox<MapPoint> crearComboMapPoint() {
        JComboBox<MapPoint> combo = new JComboBox<>();
        combo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        combo.setBackground(COLOR_TARJETA);
        combo.setForeground(COLOR_TEXTO_TITULO);
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE, 1, true),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)));

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                if (value instanceof MapPoint) {
                    MapPoint punto = (MapPoint) value;
                    label.setText(punto.getId() + "   (" + punto.getX() + ", " + punto.getY() + ")");
                }
                label.setBackground(isSelected ? COLOR_ACENTO : COLOR_TARJETA);
                label.setForeground(isSelected ? Color.WHITE : COLOR_TEXTO_TITULO);
                return label;
            }
        });

        return combo;
    }

    private JLabel crearEtiquetaCampo(String texto) {
        JLabel label = new JLabel(texto);
        label.setForeground(COLOR_TEXTO_SECUNDARIO);
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JRadioButton crearRadio(String texto, boolean seleccionado) {
        JRadioButton radio = new JRadioButton(texto, seleccionado);
        radio.setBackground(COLOR_TARJETA);
        radio.setForeground(COLOR_TEXTO_TITULO);
        radio.setFont(new Font("SansSerif", Font.PLAIN, 12));
        radio.setFocusPainted(false);
        return radio;
    }

    private JPanel crearFilaRadios(JRadioButton a, JRadioButton b) {
        JPanel fila = new JPanel();
        fila.setLayout(new BoxLayout(fila, BoxLayout.X_AXIS));
        fila.setBackground(COLOR_TARJETA);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.add(a);
        fila.add(Box.createHorizontalStrut(6));
        fila.add(b);
        return fila;
    }

    private JButton crearBoton(String texto, Color fondo, Color colorTexto) {
        JButton boton = new JButton(texto);
        boton.setBackground(fondo);
        boton.setForeground(colorTexto);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setOpaque(true);
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        return boton;
    }

    private void agregarLog(String mensaje) {
        areaLog.append(mensaje + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }

    // ---------- Selectores ----------

    private void actualizarSelectores() {
        comboInicio.removeAllItems();
        comboDestino.removeAllItems();
        comboEliminarNodo.removeAllItems();
        comboOrigenConexion.removeAllItems();
        comboDestinoConexion.removeAllItems();

        for (MapPoint punto : controller.getGraph().getNodes()) {
            comboInicio.addItem(punto);
            comboDestino.addItem(punto);
            comboEliminarNodo.addItem(punto);
            comboOrigenConexion.addItem(punto);
            comboDestinoConexion.addItem(punto);
        }
    }

    // ---------- Acciones: ruta ----------

    private void ejecutarBusqueda() {
        MapPoint inicio = (MapPoint) comboInicio.getSelectedItem();
        MapPoint destino = (MapPoint) comboDestino.getSelectedItem();

        if (inicio == null || destino == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un nodo de inicio y uno de destino.");
            return;
        }

        String algoritmo = radioBFS.isSelected() ? "BFS" : "DFS";
        VisualizationMode modo = radioExploracion.isSelected()
                ? VisualizationMode.EXPLORATION
                : VisualizationMode.FINAL_PATH;

        PathResult<MapPoint> resultado = controller.ejecutar(algoritmo, inicio, destino);

        if (resultado == null || !resultado.isEncontrado()) {
            mapPanel.mostrarResultado(resultado, inicio, destino, modo);
            String mensaje = "Sin ruta entre " + inicio.getId() + " y " + destino.getId();
            labelEstado.setText(mensaje);
            agregarLog(algoritmo + ": " + mensaje);
            return;
        }

        mapPanel.mostrarResultado(resultado, inicio, destino, modo);

        String resumen = algoritmo + "  ·  Visitados: " + resultado.getCantidadVisitados()
                + "  ·  Aristas: " + resultado.getCantidadAristas()
                + "  ·  " + resultado.getTiempoEjecucion() + " ms";
        labelEstado.setText(resumen);

        String modoTexto = modo == VisualizationMode.EXPLORATION ? "exploración" : "ruta final";
        agregarLog(resumen + "  ·  " + inicio.getId() + " -> " + destino.getId() + "  ·  modo " + modoTexto);
    }

    // ---------- Acciones: nodos ----------

    private void alternarModoCrearNodo() {
        modoCrearNodo = !modoCrearNodo;

        if (modoCrearNodo) {
            botonModoCrearNodo.setText("Click en el mapa...");
            botonModoCrearNodo.setBackground(COLOR_ACENTO);
            botonModoCrearNodo.setForeground(Color.WHITE);
            mapPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            agregarLog("Modo crear nodo activado.");
        } else {
            botonModoCrearNodo.setText("Crear nodo");
            botonModoCrearNodo.setBackground(COLOR_BOTON_SECUNDARIO);
            botonModoCrearNodo.setForeground(COLOR_TEXTO_TITULO);
            mapPanel.setCursor(Cursor.getDefaultCursor());
            agregarLog("Modo crear nodo desactivado.");
        }
    }

    private void crearNodoEnClick(int x, int y) {
        int[] coordenadas = mapPanel.convertirClickACoordenadas(x, y);

        String id = JOptionPane.showInputDialog(this, "Identificador del nuevo nodo:", "Crear nodo",
                JOptionPane.PLAIN_MESSAGE);

        if (id == null || id.isBlank()) {
            return;
        }

        try {
            MapPoint nuevo = new MapPoint(id, coordenadas[0], coordenadas[1]);
            boolean agregado = controller.agregarPunto(nuevo);

            if (!agregado) {
                JOptionPane.showMessageDialog(this, "Ya existe un nodo con ese identificador.");
                return;
            }

            actualizarSelectores();
            mapPanel.repaint();
            agregarLog("Nodo '" + id + "' creado en (" + coordenadas[0] + ", " + coordenadas[1] + ").");
            labelEstado.setText("Nodo '" + id + "' creado");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void eliminarNodoSeleccionado() {
        MapPoint punto = (MapPoint) comboEliminarNodo.getSelectedItem();

        if (punto == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un nodo para eliminar.");
            return;
        }

        controller.eliminarPunto(punto);
        actualizarSelectores();
        mapPanel.limpiarResultado();
        agregarLog("Nodo '" + punto.getId() + "' eliminado junto con sus conexiones.");
        labelEstado.setText("Nodo '" + punto.getId() + "' eliminado");
    }

    // ---------- Acciones: conexiones ----------

    private void agregarConexionSeleccionada() {
        MapPoint origen = (MapPoint) comboOrigenConexion.getSelectedItem();
        MapPoint destino = (MapPoint) comboDestinoConexion.getSelectedItem();

        if (origen == null || destino == null || origen.equals(destino)) {
            JOptionPane.showMessageDialog(this, "Selecciona dos nodos distintos.");
            return;
        }

        boolean bidireccional = radioBi.isSelected();
        boolean agregada = controller.agregarConexion(origen, destino, bidireccional);

        if (!agregada) {
            JOptionPane.showMessageDialog(this, "No se pudo crear la conexión.");
            return;
        }

        mapPanel.repaint();
        String tipo = bidireccional ? "bidireccional" : "unidireccional";
        agregarLog("Conexión " + tipo + ": " + origen.getId() + " -> " + destino.getId());
        labelEstado.setText("Conexión " + origen.getId() + " -> " + destino.getId() + " creada");
    }

    private void eliminarConexionSeleccionada() {
        MapPoint origen = (MapPoint) comboOrigenConexion.getSelectedItem();
        MapPoint destino = (MapPoint) comboDestinoConexion.getSelectedItem();

        if (origen == null || destino == null) {
            JOptionPane.showMessageDialog(this, "Selecciona los nodos de la conexión.");
            return;
        }

        boolean bidireccional = radioBi.isSelected();
        controller.eliminarConexion(origen, destino, bidireccional);
        mapPanel.repaint();
        agregarLog("Conexión eliminada: " + origen.getId() + " -> " + destino.getId());
        labelEstado.setText("Conexión eliminada");
    }

    // ---------- Acciones: persistencia ----------

    private void guardarConfiguracion() {
        try {
            File archivo = new File(RUTA_CONFIGURACION);
            if (archivo.getParentFile() != null) {
                archivo.getParentFile().mkdirs();
            }
            controller.guardar(RUTA_CONFIGURACION);
            agregarLog("Configuración guardada en " + RUTA_CONFIGURACION);
            labelEstado.setText("Configuración guardada");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage());
            agregarLog("Error al guardar la configuración.");
        }
    }

    private void cargarConfiguracion() {
        File archivo = new File(RUTA_CONFIGURACION);
        if (!archivo.exists()) {
            JOptionPane.showMessageDialog(this, "Todavía no existe un archivo de configuración guardado.");
            agregarLog("No se encontró el archivo de configuración.");
            return;
        }

        try {
            controller.cargar(RUTA_CONFIGURACION);
            mapPanel.setGrafo(controller.getGraph());
            mapPanel.limpiarResultado();
            actualizarSelectores();
            agregarLog("Configuración cargada desde " + RUTA_CONFIGURACION);
            labelEstado.setText("Configuración cargada");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar: " + ex.getMessage());
            agregarLog("Error al cargar la configuración.");
        }
    }
}