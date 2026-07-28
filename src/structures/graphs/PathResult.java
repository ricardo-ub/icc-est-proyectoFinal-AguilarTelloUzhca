package structures.graphs;

import java.util.Set;

public class PathResult<T> {

    private final Set<T> visitados;
    private final Set<T> path;
    private final long tiempoEjecucion;
    private final boolean encontrado;

    public PathResult(Set<T> visitados,Set<T> path,long tiempoEjecucion,boolean encontrado) {

        this.visitados = visitados;
        this.path = path;
        this.tiempoEjecucion = tiempoEjecucion;
        this.encontrado = encontrado;
    }

    public Set<T> getVisitados() {
        return visitados;
    }

    public Set<T> getPath() {
        return path;
    }

    public long getTiempoEjecucion() {
        return tiempoEjecucion;
    }

    public boolean isEncontrado() {
        return encontrado;
    }

    public int getCantidadVisitados() {
        return visitados.size();
    }

    public int getCantidadAristas() {
        if (!encontrado || path.isEmpty()) {
            return 0;
        }

        return path.size() - 1;
    }

    @Override
    public String toString() {
        return "Visitados: " + visitados+ (path.isEmpty()? "\nNo se encontró un camino": "\nCamino: " + path);
    }
}