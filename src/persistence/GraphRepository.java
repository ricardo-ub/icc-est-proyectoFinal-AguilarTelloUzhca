package persistence;

import java.io.IOException;

import models.MapPoint;
import structures.graphs.Graph;

public interface GraphRepository {
    void guardar(Graph<MapPoint> grafo, String rutaArchivo) throws IOException;
    Graph<MapPoint> cargar(String rutaArchivo) throws IOException;
}
