package structures.graphs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import structures.node.Node;

public class Graph<T> {

    Map<Node<T>, Set<Node<T>>> nodes;

    public Graph() {
        this.nodes = new LinkedHashMap<>();
    }

    public boolean add(T value) {
        Node<T> nodo = new Node<>(value);
        if (nodes.containsKey(nodo)) {
            return false;
        }
        nodes.put(nodo, new LinkedHashSet<>());
        return true;
    }

    public boolean contains(T value) {
        return nodes.containsKey(new Node<>(value));
    }

    // Ya NO auto-crea nodos: si v1 o v2 no existen, devuelve false.
    public boolean addEdge(T v1, T v2) {
        Node<T> nV1 = new Node<>(v1);
        Node<T> nV2 = new Node<>(v2);
        if (!nodes.containsKey(nV1) || !nodes.containsKey(nV2)) return false;
        boolean a = nodes.get(nV1).add(nV2);
        boolean b = nodes.get(nV2).add(nV1);
        return a || b;
    }

    public boolean addEdgeUni(T v1, T v2) {
        Node<T> nV1 = new Node<>(v1);
        Node<T> nV2 = new Node<>(v2);
        if (!nodes.containsKey(nV1) || !nodes.containsKey(nV2)) return false;
        return nodes.get(nV1).add(nV2);
    }

    public void removeEdge(T v1, T v2) {
        Node<T> nV1 = new Node<>(v1);
        Node<T> nV2 = new Node<>(v2);
        if (nodes.containsKey(nV1) && nodes.containsKey(nV2)) {
            nodes.get(nV1).remove(nV2);
            nodes.get(nV2).remove(nV1);
        }
    }

    public void removeEdgeUni(T v1, T v2) {
        Node<T> nV1 = new Node<>(v1);
        Node<T> nV2 = new Node<>(v2);
        if (nodes.containsKey(nV1) && nodes.containsKey(nV2)) {
            nodes.get(nV1).remove(nV2);
        }
    }

    public void removeNode(T value) {
        Node<T> nodo = new Node<>(value);
        if (nodes.containsKey(nodo)) {
            nodes.remove(nodo);
            for (Set<Node<T>> set : nodes.values()) {
                set.remove(nodo);
            }
        }
    }

    public int getDirections() {
        int total = 0;
        for (Set<Node<T>> conexiones : nodes.values()) {
            total += conexiones.size();
        }
        return total;
    }

    public int getConnections() {
        int direcciones = getDirections();
        int bidireccionales = 0;
        for (Map.Entry<Node<T>, Set<Node<T>>> entry : nodes.entrySet()) {
            Node<T> origen = entry.getKey();
            for (Node<T> destino : entry.getValue()) {
                if (nodes.containsKey(destino) && nodes.get(destino).contains(origen)) {
                    bidireccionales++;
                }
            }
        }
        return direcciones - (bidireccionales / 2);
    }

    public void print() {
        for (Map.Entry<Node<T>, Set<Node<T>>> entry : nodes.entrySet()) {
            System.out.print(entry.getKey() + " -> ");
            for (Node<T> nodo : entry.getValue()) {
                System.out.print(nodo);
            }
            System.out.println();
        }
    }

    // Copia inmutable: ya no se puede modificar el grafo desde afuera
    public Set<T> getVecinos(T current) {
        Node<T> nodo = new Node<>(current);
        Set<Node<T>> internos = nodes.getOrDefault(nodo, new LinkedHashSet<>());
        Set<T> vecinos = new LinkedHashSet<>();
        for (Node<T> n : internos) {
            vecinos.add(n.getValue());
        }
        return Collections.unmodifiableSet(vecinos);
    }

    public List<T> getNodes() {
        List<T> lista = new ArrayList<>();
        for (Node<T> nodo : nodes.keySet()) {
            lista.add(nodo.getValue());
        }
        return Collections.unmodifiableList(lista);
    }

    public Map<T, Set<T>> getGraph() {
        Map<T, Set<T>> copia = new LinkedHashMap<>();
        for (Map.Entry<Node<T>, Set<Node<T>>> entry : nodes.entrySet()) {
            Set<T> vecinos = new LinkedHashSet<>();
            for (Node<T> n : entry.getValue()) vecinos.add(n.getValue());
            copia.put(entry.getKey().getValue(), Collections.unmodifiableSet(vecinos));
        }
        return Collections.unmodifiableMap(copia);
    }

    public void clear() {
        nodes.clear();
    }
}