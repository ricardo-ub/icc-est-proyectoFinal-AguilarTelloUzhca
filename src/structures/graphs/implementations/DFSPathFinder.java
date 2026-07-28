package structures.graphs.implementations;

import java.util.LinkedHashSet;
import java.util.Set;

import structures.graphs.Graph;
import structures.graphs.PathFinder;
import structures.graphs.PathResult;

public class DFSPathFinder<T> implements PathFinder<T> {

    @Override
    public PathResult<T> find(Graph<T> graph, T start, T end) {

        long inicioTiempo = System.nanoTime();

        Set<T> visitados = new LinkedHashSet<>();
        Set<T> path = new LinkedHashSet<>();

        if (graph == null || start == null || end == null || !graph.contains(start) || !graph.contains(end)) {
            return new PathResult<>(visitados, path, System.nanoTime() - inicioTiempo, false);
        }

        boolean encontrado = dfs(graph, start, end, visitados, path);

        return new PathResult<>(visitados, path, System.nanoTime() - inicioTiempo, encontrado);
    }

    private boolean dfs(Graph<T> graph, T current, T end, Set<T> visitados, Set<T> path) {

        visitados.add(current);
        path.add(current);

        if (current.equals(end)) {
            return true;
        }

        for (T vecino : graph.getVecinos(current)) {
            if (!visitados.contains(vecino)) {
                if (dfs(graph, vecino, end, visitados, path)) {
                    return true;
                }
            }
        }

        path.remove(current);
        return false;
    }
}