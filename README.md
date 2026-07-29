# Proyecto final de Estructura de Datos

## Aplicación de los algoritmos BFS y DFS en un mapa

### Integrantes

- Aguilar
- Tello
- Uzhca

## Introducción

El proyecto consiste en una aplicación desarrollada en Java que representa diferentes lugares de un mapa mediante un grafo. Cada lugar corresponde a un nodo y las conexiones entre lugares se representan mediante aristas.

El usuario puede seleccionar un punto de inicio y un punto de destino y ejecutar una búsqueda utilizando BFS o DFS. El programa muestra los nodos que fueron visitados, la ruta encontrada, la cantidad de conexiones recorridas y el tiempo utilizado por el algoritmo.

También se pueden agregar o eliminar puntos, crear conexiones unidireccionales o bidireccionales y guardar la configuración del mapa en un archivo de texto.

Para desarrollar el proyecto se aplicaron temas vistos durante el ciclo, como grafos, clases genéricas, colas, conjuntos, mapas, recursividad, lectura de archivos, programación orientada a objetos e interfaces gráficas con Swing.

## Objetivo general

Desarrollar una aplicación en Java que permita representar un mapa mediante un grafo y buscar rutas entre dos puntos utilizando los algoritmos BFS y DFS.

## Objetivos específicos

- Representar lugares del mapa mediante nodos.
- Crear conexiones unidireccionales y bidireccionales.
- Implementar los algoritmos BFS y DFS.
- Mostrar los nodos visitados durante cada recorrido.
- Obtener la ruta encontrada desde el inicio hasta el destino.
- Medir el tiempo de ejecución de cada algoritmo.
- Guardar y cargar la información del grafo.
- Mostrar los resultados en una interfaz gráfica.

## Descripción general del programa

La ejecución empieza en la clase `App`. En esta clase se intenta cargar el grafo desde el archivo de configuración.

## Clase MapPoint

La clase `MapPoint` representa un lugar del mapa. Guarda un identificador y dos coordenadas.

```java
private String id;
private int x;
private int y;
```

El constructor valida que el identificador no esté vacío y que las coordenadas no sean negativas.

```java
public MapPoint(String id, int x, int y) {
    if (id == null || id.isBlank()) {
        throw new IllegalArgumentException("El id de un MapPoint no puede estar vacío.");
    }
    this.id = id;
    setCoordenadas(x, y);
}
```

Si alguno de estos datos es incorrecto, se lanza una excepción en lugar de crear un punto con información inválida.

Los métodos `equals` y `hashCode` se generaron usando únicamente el identificador.

```java
if (id == null) {
    if (other.id != null)
        return false;
} else if (!id.equals(other.id))
    return false;
```

Esto significa que dos `MapPoint` con el mismo id se consideran el mismo punto, sin importar si cambian sus coordenadas. Esta decisión es importante porque `Graph<T>` usa `equals`/`hashCode` para saber si un nodo ya existe.

## Clase Graph

La clase `Graph<T>` almacena los puntos y sus conexiones utilizando un mapa.

```java
Map<Node<T>, Set<Node<T>>> nodes;
```

Se utilizó `LinkedHashMap` y `LinkedHashSet` en vez de `HashMap`/`HashSet` porque el proyecto necesita conservar el orden en que se agregan los nodos y las conexiones. Este orden se usa después para representar la exploración de BFS y DFS en la interfaz.

Agregar un nodo devuelve `true` o `false` según si ya existía.

```java
public boolean add(T value) {
    Node<T> nodo = new Node<>(value);
    if (nodes.containsKey(nodo)) {
        return false;
    }
    nodes.put(nodo, new LinkedHashSet<>());
    return true;
}
```

Las conexiones se crean con `addEdge` (bidireccional) y `addEdgeUni` (dirigida). Ninguna de las dos crea nodos automáticamente: si alguno de los dos puntos no existe en el grafo, la operación simplemente devuelve `false`.

```java
public boolean addEdge(T v1, T v2) {
    Node<T> nV1 = new Node<>(v1);
    Node<T> nV2 = new Node<>(v2);
    if (!nodes.containsKey(nV1) || !nodes.containsKey(nV2)) return false;
    boolean a = nodes.get(nV1).add(nV2);
    boolean b = nodes.get(nV2).add(nV1);
    return a || b;
}
```

Para eliminar existen `removeEdge`, `removeEdgeUni` y `removeNode`, que primero comprueban que los nodos existan antes de modificar la estructura.

```java
public void removeNode(T value) {
    Node<T> nodo = new Node<>(value);
    if (nodes.containsKey(nodo)) {
        nodes.remove(nodo);
        for (Set<Node<T>> set : nodes.values()) {
            set.remove(nodo);
        }
    }
}
```

`removeNode` no solo quita el nodo del mapa, también recorre el resto de conexiones para eliminar las referencias que otros nodos tenían hacia él.

El método `getVecinos` no devuelve la estructura interna del grafo, sino una copia. Esto evita que otra clase modifique el grafo directamente desde afuera.

```java
public Set<T> getVecinos(T current) {
    Node<T> nodo = new Node<>(current);
    Set<Node<T>> internos = nodes.getOrDefault(nodo, new LinkedHashSet<>());
    Set<T> vecinos = new LinkedHashSet<>();
    for (Node<T> n : internos) {
        vecinos.add(n.getValue());
    }
    return Collections.unmodifiableSet(vecinos);
}
```

También se agregaron `getNodes()`, que entrega la lista de puntos en orden de inserción, y `getGraph()`, que entrega una copia completa del grafo (nodo y sus vecinos). Estos dos métodos los usa la vista para dibujar los puntos y las conexiones sobre el mapa, y `FileGraphRepository` para recorrer el grafo al momento de guardarlo.

Por último, `getDirections()` y `getConnections()` cuentan cuántas conexiones dirigidas y bidireccionales tiene el grafo, recorriendo la estructura real en el momento en que se llaman, en vez de mantener un contador aparte que se pudiera desactualizar.

## Persistencia: GraphRepository y FileGraphRepository

`GraphRepository` es la interfaz que define el contrato de guardado y carga.

```java
public interface GraphRepository {
    void guardar(Graph<MapPoint> grafo, String rutaArchivo) throws IOException;
    Graph<MapPoint> cargar(String rutaArchivo) throws IOException;
}
```

`FileGraphRepository` es la implementación que guarda el grafo en un archivo de texto plano, sin usar librerías externas. El archivo se organiza en dos secciones: `NODOS` y `ARISTAS`.

```text
NODOS
A,100,120
B,250,130
ARISTAS
A,B,true
```

Cada nodo se guarda como `id,x,y` y cada conexión como `desde,hasta,esBidireccional`.

```java
List<MapPoint> nodos = grafo.getNodes();
for (MapPoint punto : nodos) {
    writer.write(punto.getId() + "," + punto.getX() + "," + punto.getY());
    writer.newLine();
}
```

Para saber si una conexión es bidireccional, se comprueba si el destino también tiene al origen como vecino.

```java
boolean bidireccional = grafo.getVecinos(destino).contains(origen);
```

Al cargar, se lee el archivo línea por línea. Una variable `seccion` indica si las líneas que siguen corresponden a nodos o a aristas.

```java
if (linea.equals("NODOS") || linea.equals("ARISTAS")) {
    seccion = linea;
    continue;
}
```

Cada línea se separa con `split(",")` para reconstruir el `MapPoint` o la conexión correspondiente. Los puntos se guardan primero en un mapa temporal (`puntosPorId`) para poder relacionar las aristas con el objeto `MapPoint` correcto antes de agregarlas al grafo.

```java
MapPoint desde = puntosPorId.get(partes[0]);
MapPoint hasta = puntosPorId.get(partes[1]);
```

## Parte de BFS, DFS, Controller, VisualizationMode y Recorrido final

La Persona 2 estuvo encargada principalmente del funcionamiento de los recorridos BFS y DFS, de la información generada por cada búsqueda y del controlador utilizado para conectar los algoritmos con la interfaz.

Los archivos trabajados fueron:

- PathFinder.java
- PathResult.java
- BFSPathFinder.java
- DFSPathFinder.java
- MapController.java
- VisualizationMode.java

También se realizaron pruebas utilizando objetos "MapPoint" para verificar que los algoritmos funcionaran con el mismo tipo de dato utilizado por el programa.

## Interfaz PathFinder

Se creó la interfaz `PathFinder<T>` para definir la estructura común que deben cumplir los algoritmos de búsqueda.

```java
public interface PathFinder<T> {

    PathResult<T> find(Graph<T> graph, T start, T end);
}
```

El método "find" recibe tres datos:

- El grafo donde se realizará la búsqueda.
- El nodo de inicio.
- El nodo de destino.

Como resultado devuelve un objeto `PathResult<T>`.

BFS y DFS implementan esta interfaz, por lo que los dos algoritmos pueden utilizarse de una forma parecida.

```java
PathFinder<MapPoint> bfs = new BFSPathFinder<>();
PathFinder<MapPoint> dfs = new DFSPathFinder<>();
```


## Clase PathResult

La clase `PathResult<T>` almacena la información generada después de ejecutar BFS o DFS.

Sus atributos principales son:

```java
private final Set<T> visitados;
private final Set<T> path;
private final long tiempoEjecucion;
private final boolean encontrado;
```

El conjunto "visitados" contiene todos los nodos que el algoritmo revisó durante la búsqueda.

El conjunto "path" contiene solamente los nodos que forman parte de la ruta final.


El atributo "tiempoEjecucion" guarda el tiempo utilizado por el algoritmo. Para medirlo se registra el tiempo antes de comenzar:

```java
long inicioTiempo = System.nanoTime();
```

Después se resta el tiempo inicial al tiempo final:

```java
System.nanoTime() - inicioTiempo
```

El resultado se guarda en nanosegundos.

El atributo "encontrado" indica si existe o no una ruta entre los dos puntos seleccionados.

```java
private final boolean encontrado;
```

Cuando se encuentra el destino, su valor es `true`. Cuando los puntos no están conectados o alguno de ellos no existe, su valor es `false`.

También se implementó un método para conocer la cantidad de nodos visitados:

```java
public int getCantidadVisitados() {
    return visitados.size();
}
```

La cantidad de aristas se calcula utilizando la cantidad de nodos que forman la ruta.

```java
public int getCantidadAristas() {
    if (!encontrado || path.isEmpty()) {
        return 0;
    }

    return path.size() - 1;
}
```

Por ejemplo, la ruta formada por los nodos `A`, `B` y `C` contiene dos aristas: una conexión entre A y B y otra entre B y C.

## Implementación de BFS

BFS significa búsqueda en anchura. Este algoritmo primero revisa los vecinos directos del nodo inicial y después continúa con los vecinos de esos nodos.

La clase se declaró de la siguiente forma:

```java
public class BFSPathFinder<T> implements PathFinder<T>
```

Al implementar `PathFinder<T>`, la clase debe contener el método find.

```java
public PathResult<T> find(Graph<T> graph, T start, T end)
```

Al comenzar el método se registra el tiempo inicial:

```java
long inicioTiempo = System.nanoTime();
```

Después se crean las estructuras necesarias para realizar la búsqueda.

```java
Queue<T> queue = new LinkedList<>();
Set<T> visitados = new LinkedHashSet<>();
Map<T, T> parent = new LinkedHashMap<>();
```

La cola "queue" se utiliza para controlar el orden de los nodos pendientes. BFS trabaja con el principio de que el primer elemento que entra es el primero que sale.

El conjunto "visitados" evita que un nodo sea revisado más de una vez. Esto también evita recorridos infinitos cuando el grafo contiene ciclos.

Se utilizó "LinkedHashSet" porque, además de evitar elementos repetidos, mantiene el orden en que los nodos fueron agregados. Este orden se necesita después para mostrar la exploración en la interfaz.

El mapa "parent" guarda el nodo desde el cual se llegó a cada vecino.

Por ejemplo, si se recorre A, después B y luego C, se puede almacenar lo siguiente:

```text
A tiene como padre null
B tiene como padre A
C tiene como padre B
```

Antes de empezar el recorrido se validan los datos recibidos.

```java
if (graph == null || start == null || end == null || !graph.contains(start) || !graph.contains(end)) {
    return new PathResult<>(visitados, new LinkedHashSet<>(), System.nanoTime() - inicioTiempo, false);
}
```

Esta validación comprueba que el grafo no sea nulo, que el inicio y el destino sean válidos y que los dos puntos existan dentro del grafo.

Si alguno de estos datos es incorrecto, se devuelve un resultado vacío sin ejecutar el recorrido.

Luego se agrega el nodo inicial a la cola, al conjunto de visitados y al mapa de padres.

```java
queue.add(start);
visitados.add(start);
parent.put(start, null);
```

El recorrido se mantiene mientras existan elementos dentro de la cola.

```java
while (!queue.isEmpty()) {
    T current = queue.poll();
}
```

"queue.poll()" obtiene y elimina el primer elemento de la cola.

Después se compara el nodo actual con el destino.

```java
if (current.equals(end)) {
    return new PathResult<>(visitados, buildPath(parent, end), System.nanoTime() - inicioTiempo, true);
}
```

Si el nodo actual es el destino, se reconstruye la ruta y se devuelve el resultado.

Cuando todavía no se llega al destino, se obtienen los vecinos del nodo actual.

```java
for (T vecino : graph.getVecinos(current)) {
    if (!visitados.contains(vecino)) {
        visitados.add(vecino);
        parent.put(vecino, current);
        queue.add(vecino);
    }
}
```

Cada vecino que todavía no ha sido visitado se registra, se relaciona con su nodo padre y se agrega al final de la cola.

Si la cola queda vacía y nunca se encuentra el destino, el método devuelve un resultado con "encontrado" en "false".

## Reconstrucción de la ruta en BFS

BFS no construye directamente la ruta mientras realiza la exploración. Para obtenerla utiliza el mapa "parent".

La reconstrucción empieza desde el destino y retrocede hasta llegar al nodo inicial.

```java
for (T at = end; at != null; at = parent.get(at)) {
    pathInvertido.add(at);
}
```

Si la ruta correcta es:

```text
A, E, D
```

el ciclo la obtiene inicialmente de la siguiente forma:

```text
D, E, A
```

Por esta razón se debe invertir la lista.

```java
Collections.reverse(pathInvertido);
```

Finalmente se convierte en un "LinkedHashSet" para conservar el orden.

```java
return new LinkedHashSet<>(pathInvertido);
```

Una característica importante de BFS es que encuentra la ruta con menor cantidad de aristas cuando el grafo no utiliza pesos.

## Implementación de DFS

DFS significa búsqueda en profundidad. A diferencia de BFS, DFS sigue una rama del grafo hasta donde sea posible antes de regresar y probar otra.

La clase también implementa la interfaz `PathFinder<T>`.

```java
public class DFSPathFinder<T> implements PathFinder<T>
```

Al iniciar se crean dos conjuntos.

```java
Set<T> visitados = new LinkedHashSet<>();
Set<T> path = new LinkedHashSet<>();
```

"visitados" guarda todos los nodos revisados por el algoritmo.

"path" guarda el camino que se está formando durante la búsqueda.

Antes de ejecutar la recursividad se comprueba que el grafo, el inicio y el destino sean válidos.

```java
if (graph == null || start == null || end == null || !graph.contains(start) || !graph.contains(end)) {
    return new PathResult<>(visitados, path, System.nanoTime() - inicioTiempo, false);
}
```

Después se llama al método recursivo.

```java
boolean encontrado = dfs(graph, start, end, visitados, path);
```

El método "dfs" recibe el nodo actual, el destino, los visitados y el camino.

```java
private boolean dfs(Graph<T> graph, T current, T end, Set<T> visitados, Set<T> path)
```

Primero se registra el nodo actual.

```java
visitados.add(current);
path.add(current);
```

Luego se comprueba si el nodo actual es el destino.

```java
if (current.equals(end)) {
    return true;
}
```

Si todavía no se llegó al destino, se recorren los vecinos del nodo actual.

```java
for (T vecino : graph.getVecinos(current)) {
    if (!visitados.contains(vecino)) {
        if (dfs(graph, vecino, end, visitados, path)) {
            return true;
        }
    }
}
```

Por cada vecino no visitado se realiza una nueva llamada al mismo método. Esto permite que DFS avance cada vez más dentro de una rama.

Si una rama no lleva al destino, se elimina el nodo actual del camino.

```java
path.remove(current);
return false;
```

Esta parte corresponde al retroceso o backtracking.

El nodo se elimina de "path" porque no forma parte de la ruta final, pero permanece dentro de "visitados" porque ya fue revisado.

Por ejemplo, si el algoritmo intenta recorrer A, B y C, pero desde C no puede llegar al destino, C se elimina de la ruta y la ejecución regresa a B para revisar otro vecino.

DFS permite encontrar una ruta si existe, pero no garantiza que sea la más corta.

## Diferencia entre BFS y DFS

Para comprobar los algoritmos se utilizó un grafo formado por los puntos A, B, C, D y E.

Las conexiones permitían llegar desde A hasta D de dos formas:

```text
A, B, C, D
```

o:

```text
A, E, D
```

BFS encontró:

```text
A, E, D
```

Esto ocurre porque BFS revisa primero los puntos que se encuentran a menor cantidad de conexiones desde el inicio.

DFS encontró:

```text
A, B, C, D
```

Esto ocurre porque DFS sigue primero una rama completa según el orden en que aparecen los vecinos.

Los dos algoritmos encontraron una ruta correcta, pero BFS encontró la que tenía menos conexiones.

## MapController

La clase `MapController` se encarga de comunicar la interfaz con el grafo, los algoritmos y la persistencia.

Sus atributos son:

```java
private Graph<MapPoint> graph;
private GraphRepository repository;
```

El controlador recibe estos objetos en su constructor.

```java
public MapController(Graph<MapPoint> graph, GraphRepository repository) {
    this.graph = graph;
    this.repository = repository;
}
```

Para ejecutar BFS se creó el siguiente método:

```java
public PathResult<MapPoint> ejecutarBFS(MapPoint inicio, MapPoint destino) {
    PathFinder<MapPoint> bfs = new BFSPathFinder<>();
    return bfs.find(graph, inicio, destino);
}
```

Este método crea un objeto "BFSPathFinder" y ejecuta "find" utilizando el grafo actual.

Para ejecutar DFS se utiliza el mismo procedimiento.

```java
public PathResult<MapPoint> ejecutarDFS(MapPoint inicio, MapPoint destino) {
    PathFinder<MapPoint> dfs = new DFSPathFinder<>();
    return dfs.find(graph, inicio, destino);
}
```

También se creó un método general que recibe el nombre del algoritmo.

```java
public PathResult<MapPoint> ejecutar(String algoritmo, MapPoint inicio, MapPoint destino)
```

Dentro del método se comprueba la opción seleccionada.

```java
if (algoritmo.equalsIgnoreCase("BFS")) {
    return ejecutarBFS(inicio, destino);
}

if (algoritmo.equalsIgnoreCase("DFS")) {
    return ejecutarDFS(inicio, destino);
}
```

La interfaz solamente debe enviar el nombre del algoritmo, el inicio y el destino. El controlador se encarga de decidir qué clase debe utilizar.

Además de ejecutar los algoritmos, el controlador contiene operaciones para agregar y eliminar puntos.

```java
public boolean agregarPunto(MapPoint punto) {
    if (punto == null) {
        return false;
    }

    return graph.add(punto);
}
```

También administra las conexiones.

```java
public boolean agregarConexion(MapPoint inicio, MapPoint destino, boolean bidireccional) {
    if (inicio == null || destino == null) {
        return false;
    }

    if (bidireccional) {
        return graph.addEdge(inicio, destino);
    }

    return graph.addEdgeUni(inicio, destino);
}
```

Cuando la conexión es bidireccional se utiliza "addEdge". Cuando es unidireccional se utiliza "addEdgeUni".

El controlador también permite eliminar conexiones y solicitar el guardado o la carga de información.

```java
public void guardar(String rutaArchivo) throws IOException {
    repository.guardar(graph, rutaArchivo);
}
```

```java
public void cargar(String rutaArchivo) throws IOException {
    graph = repository.cargar(rutaArchivo);
}
```

Con esta organización, la interfaz no necesita acceder directamente a las operaciones internas del grafo ni conocer cómo se guarda el archivo.

## VisualizationMode

Se creó la enumeración `VisualizationMode` para indicar cómo debe mostrarse el resultado.

```java
public enum VisualizationMode {
    EXPLORATION,
    FINAL_PATH
}
```

En el modo `EXPLORATION` se muestran los nodos según el orden en que fueron visitados.

En el modo `FINAL_PATH` se muestra únicamente el camino final desde el inicio hasta el destino.

Esta clase no ejecuta BFS ni DFS. Su función es informar a la interfaz qué parte del `PathResult` debe representar.



## Conclusión

El proyecto permitió aplicar los grafos en una situación visual, utilizando puntos de un mapa y conexiones entre ellos.

La parte desarrollada por la Persona 2 permitió integrar los algoritmos BFS y DFS con el resto de la aplicación. Se corrigió la búsqueda en anchura para reconstruir correctamente la ruta y se utilizó recursividad con retroceso para mantener la ruta de DFS.

También se implementó `PathResult` para separar los nodos visitados de la ruta final y guardar información adicional como el tiempo de ejecución, la cantidad de visitados y la cantidad de aristas.

Finalmente, `MapController` permitió conectar los algoritmos con la interfaz sin que la vista tuviera que modificar directamente el grafo. Esto facilitó la organización del código y permitió que cada parte del programa mantuviera una responsabilidad específica.