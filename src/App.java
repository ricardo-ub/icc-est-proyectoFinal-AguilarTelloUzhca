import javax.swing.SwingUtilities;

import controllers.MapController;
import models.MapPoint;
import structures.graphs.Graph;
import views.MainFrame;

public class App {
    public static void main(String[] args) throws Exception {

        Graph<MapPoint> grafo = new Graph<>();

        MapPoint a = new MapPoint("A", 100, 120);
        MapPoint b = new MapPoint("B", 300, 150);
        MapPoint c = new MapPoint("C", 300, 350);
        MapPoint d = new MapPoint("D", 500, 300);

        grafo.add(a);
        grafo.add(b);
        grafo.add(c);
        grafo.add(d);

        grafo.addEdge(a, b);
        grafo.addEdge(b, c);
        grafo.addEdge(c, d);
        grafo.addEdgeUni(a, d);

        MapController controller = new MapController(grafo, null);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame frame = new MainFrame(controller);
                frame.setVisible(true);
            }
        });
    }
}