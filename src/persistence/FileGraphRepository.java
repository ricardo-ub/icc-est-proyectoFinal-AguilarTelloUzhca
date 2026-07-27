package persistence;

import java.io.IOException;

import models.MapPoint;
import structures.graphs.Graph;

public class FileGraphRepository implements GraphRepository {

    @Override
    public void guardar(Graph<MapPoint> grafo, String rutaArchivo) throws IOException {
        
    }

    @Override
    public Graph<MapPoint> cargar(String rutaArchivo) throws IOException {
        return null;
    }
}