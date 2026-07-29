package persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.MapPoint;
import structures.graphs.Graph;

public class FileGraphRepository implements GraphRepository {

    @Override
    public void guardar(Graph<MapPoint> grafo, String rutaArchivo) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivo));

        writer.write("NODOS");
        writer.newLine();

        List<MapPoint> nodos = grafo.getNodes();
        for (MapPoint punto : nodos) {
            writer.write(punto.getId() + "," + punto.getX() + "," + punto.getY());
            writer.newLine();
        }

        writer.write("ARISTAS");
        writer.newLine();

        for (MapPoint origen : nodos) {
            Set<MapPoint> vecinos = grafo.getVecinos(origen);
            for (MapPoint destino : vecinos) {
                boolean bidireccional = grafo.getVecinos(destino).contains(origen);
                writer.write(origen.getId() + "," + destino.getId() + "," + bidireccional);
                writer.newLine();
            }
        }

        writer.close();
    }

    @Override
    public Graph<MapPoint> cargar(String rutaArchivo) throws IOException {
        Graph<MapPoint> grafo = new Graph<>();
        Map<String, MapPoint> puntosPorId = new HashMap<>();

        BufferedReader reader = new BufferedReader(new FileReader(rutaArchivo));
        String linea;
        String seccion = "";
        int numeroLinea = 0;

        try {
            while ((linea = reader.readLine()) != null) {
                numeroLinea++;
                linea = linea.trim();

                if (linea.isEmpty()) {
                    continue;
                }

                if (linea.equals("NODOS") || linea.equals("ARISTAS")) {
                    seccion = linea;
                    continue;
                }

                String[] partes = linea.split(",");

                if (seccion.equals("NODOS")) {
                    if (partes.length < 3) {
                        throw new IOException("Línea " + numeroLinea + " incompleta, se esperaba id,x,y: " + linea);
                    }

                    String id = partes[0];

                    if (puntosPorId.containsKey(id)) {
                        throw new IOException("Línea " + numeroLinea + " repite el identificador '" + id + "'.");
                    }

                    int x;
                    int y;
                    try {
                        x = Integer.parseInt(partes[1]);
                        y = Integer.parseInt(partes[2]);
                    } catch (NumberFormatException ex) {
                        throw new IOException("Línea " + numeroLinea + " tiene coordenadas inválidas: " + linea);
                    }

                    MapPoint punto = new MapPoint(id, x, y);
                    puntosPorId.put(id, punto);
                    grafo.add(punto);
                }

                if (seccion.equals("ARISTAS")) {
                    if (partes.length < 3) {
                        throw new IOException(
                                "Línea " + numeroLinea + " incompleta, se esperaba desde,hasta,bidireccional: "
                                        + linea);
                    }

                    MapPoint desde = puntosPorId.get(partes[0]);
                    MapPoint hasta = puntosPorId.get(partes[1]);

                    if (desde == null || hasta == null) {
                        throw new IOException("Línea " + numeroLinea + " referencia un nodo inexistente: " + linea);
                    }

                    boolean bidireccional = Boolean.parseBoolean(partes[2]);

                    if (bidireccional) {
                        grafo.addEdge(desde, hasta);
                    } else {
                        grafo.addEdgeUni(desde, hasta);
                    }
                }
            }
        } catch (IllegalArgumentException ex) {
            throw new IOException("Línea " + numeroLinea + " tiene datos inválidos: " + ex.getMessage());
        } finally {
            reader.close();
        }

        return grafo;
    }
}