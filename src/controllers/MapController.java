package controllers;

import java.io.IOException;

import models.MapPoint;
import persistence.GraphRepository;
import structures.graphs.Graph;
import structures.graphs.PathFinder;
import structures.graphs.PathResult;
import structures.graphs.implementations.BFSPathFinder;
import structures.graphs.implementations.DFSPathFinder;

public class MapController {

    private Graph<MapPoint> graph;
    private GraphRepository repository;

    public MapController(Graph<MapPoint> graph, GraphRepository repository) {
        this.graph = graph;
        this.repository = repository;
    }

    public PathResult<MapPoint> ejecutarBFS(MapPoint inicio, MapPoint destino) {
        PathFinder<MapPoint> bfs = new BFSPathFinder<>();
        return bfs.find(graph, inicio, destino);
    }

    public PathResult<MapPoint> ejecutarDFS(MapPoint inicio, MapPoint destino) {
        PathFinder<MapPoint> dfs = new DFSPathFinder<>();
        return dfs.find(graph, inicio, destino);
    }

    public PathResult<MapPoint> ejecutar(String algoritmo, MapPoint inicio, MapPoint destino) {
        if (algoritmo == null) {
            return null;
        }

        if (algoritmo.equalsIgnoreCase("BFS")) {
            return ejecutarBFS(inicio, destino);
        }

        if (algoritmo.equalsIgnoreCase("DFS")) {
            return ejecutarDFS(inicio, destino);
        }

        return null;
    }

    public boolean agregarPunto(MapPoint punto) {
        if (punto == null) {
            return false;
        }

        return graph.add(punto);
    }

    public void eliminarPunto(MapPoint punto) {
        if (punto != null) {
            graph.removeNode(punto);
        }
    }

    public boolean agregarConexion(MapPoint inicio, MapPoint destino, boolean bidireccional) {
        if (inicio == null || destino == null) {
            return false;
        }

        if (bidireccional) {
            return graph.addEdge(inicio, destino);
        }

        return graph.addEdgeUni(inicio, destino);
    }

    public void eliminarConexion(MapPoint inicio, MapPoint destino, boolean bidireccional) {
        if (inicio == null || destino == null) {
            return;
        }

        if (bidireccional) {
            graph.removeEdge(inicio, destino);
        } else {
            graph.removeEdgeUni(inicio, destino);
        }
    }

    public void guardar(String rutaArchivo) throws IOException {
        repository.guardar(graph, rutaArchivo);
    }

    public void cargar(String rutaArchivo) throws IOException {
        graph = repository.cargar(rutaArchivo);
    }

    public Graph<MapPoint> getGraph() {
        return graph;
    }
}