package structures.graphs.implementations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import structures.graphs.Graph;
import structures.graphs.PathFinder;
import structures.graphs.PathResult;

public class BFSPathFinder<T> implements PathFinder<T> {

    @Override
    public PathResult<T> find(Graph<T> graph, T start, T end) {

        long inicioTiempo = System.nanoTime();

        Queue<T> queue = new LinkedList<>();
        Set<T> visitados = new LinkedHashSet<>();
        Map<T, T> parent = new LinkedHashMap<>();

        if (graph == null|| start == null|| end == null|| !graph.contains(start)|| !graph.contains(end)) {

            return new PathResult<>(visitados, new LinkedHashSet<>(),System.nanoTime() - inicioTiempo,false);
        }

        queue.add(start);
        visitados.add(start);
        parent.put(start, null);

        while (!queue.isEmpty()) {

            T current = queue.poll();

            if (current.equals(end)) {
                return new PathResult<>(visitados,buildPath(parent, end),System.nanoTime() - inicioTiempo,true);
            }

            for (T vecino : graph.getVecinos(current)) {

                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    parent.put(vecino, current);
                    queue.add(vecino);
                }
            }
        }

        return new PathResult<>(visitados,new LinkedHashSet<>(),System.nanoTime() - inicioTiempo,false);
    }

    private Set<T> buildPath(Map<T, T> parent, T end) {

        List<T> pathInvertido = new ArrayList<>();

        for (T at = end; at != null; at = parent.get(at)) {
            pathInvertido.add(at);
        }

        Collections.reverse(pathInvertido);

        return new LinkedHashSet<>(pathInvertido);
    }
}