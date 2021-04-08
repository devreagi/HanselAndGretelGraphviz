package co.edu.utadeo.avanzada.grafos;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static guru.nidi.graphviz.attribute.Rank.RankDir.TOP_TO_BOTTOM;
import static guru.nidi.graphviz.model.Factory.*;

public class HanselAndGretel {

    private final int n;
    private final Comparator<Node> comparator = (node1, node2) -> {
        if (Math.abs(node1.value - node2.value) < 0)
            return 0;
        return (node1.value - node2.value) > 0 ? +1 : -1;
    };
    private Integer[] prev;
    private List<List<Edge>> graph;

    public HanselAndGretel(int n) {
        this.n = n;
        createEmptyGraph();
    }

    private static void dibujarGrafo(MutableGraph r, HanselAndGretel graph, List<Integer> path) throws IOException {
        r.use((gr, ctx) -> {
            // recorre todos los nodos
            for (int from = 0; from < graph.getGraph().size(); from++) {
                //se define el estilo de cada nodo
                if (path.contains(from)) {
                    mutNode(String.valueOf(from)).add(Color.BLUEVIOLET.fill(), Style.lineWidth(5), Style.FILLED);
                } else {
                    mutNode(String.valueOf(from)).add(Color.WHITE.fill(), Style.lineWidth(2), Style.FILLED);
                }
                // recorre todas las aristas de cada nodo
                for (int j = 0; j < graph.getGraph().get(from).size(); j++) {
                    int to = graph.getGraph().get(from).get(j).to;
                    long cost = graph.getGraph().get(from).get(j).cost;
                    //se define el estilo de cada arista
                    if ((path.indexOf(from) + 1) == path.indexOf(to)) {
                        linkAttrs().add(Label.lines(String.valueOf(cost)), Style.BOLD, Color.YELLOW);
                    } else {
                        linkAttrs().add(Label.html(String.valueOf(cost)), Color.BLACK);
                    }
                    //dibuja la arista de acuerdo al estilo
                    mutNode(String.valueOf(from)).addLink(mutNode(String.valueOf(to)));
                }
            }
        });
        //exportar grafo a imagen
        Graphviz.fromGraph(r).height(1080).width(2048).render(Format.PNG).toFile(new File("resultado/hansel_y_gretel.png"));
    }

    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(new BufferedReader(new InputStreamReader(System.in)));
        int size = in.nextInt();
        int edges = in.nextInt();

        HanselAndGretel graph = new HanselAndGretel(size);
        for (int i = 0; i < edges; i++) {
            int from = in.nextInt();
            int to = in.nextInt();
            int cost = in.nextInt();
            graph.addEdge(from, to, cost);
        }
        int origenConsulta = in.nextInt();
        int destinoConsulta = in.nextInt();
        long distancia = (long) graph.dijkstra(origenConsulta, destinoConsulta);
        List<Integer> path = graph.reconstructPath(origenConsulta, destinoConsulta);

        //Instancia un objeto de tipo MutableGraph
        MutableGraph r = mutGraph("hansel_y_gretel");
        // TRUE: Arista en forma de flecha.
        // FALSE: una linea normal
        r.setDirected(true);

        if (distancia == Long.MAX_VALUE) {
            System.out.println("Hansel y Gretel se fueron de paseo al inframundo.");
            r.add(mutNode("F por Hansel y gretel").add(Color.WHITE.fill(), Style.FILLED));
            //Estilo del grafo
            r.graphAttrs().add(Rank.dir(TOP_TO_BOTTOM), Color.RED.gradient(Color.rgb("888888")).background().angle(90));
        } else {
            System.out.println("La distancia es de " + distancia + " para llegar a la casa.");
            System.out.println("El camino es: " + path.toString());
            //Estilo del grafo
            r.graphAttrs().add(Rank.dir(TOP_TO_BOTTOM), Color.WHITE.gradient(Color.rgb("888888")).background().angle(90));
        }
        dibujarGrafo(r, graph, path);
        in.close();
    }

    public void addEdge(int from, int to, int cost) {
        graph.get(from).add(new Edge(from, to, cost));
    }

    public List<List<Edge>> getGraph() {
        return graph;
    }

    public List<Integer> reconstructPath(int start, int end) {
        if (end < 0 || end >= n)
            throw new IllegalArgumentException("Invalid node index");
        if (start < 0 || start >= n)
            throw new IllegalArgumentException("Invalid node index");
        double dist = dijkstra(start, end);
        List<Integer> path = new ArrayList<>();
        if (dist == Long.MAX_VALUE)
            return path;
        for (Integer at = end; at != null; at = prev[at])
            path.add(at);
        Collections.reverse(path);
        return path;
    }

    public double dijkstra(int start, int end) {
        double[] dist = new double[n];
        Arrays.fill(dist, Long.MAX_VALUE);
        dist[start] = 0;

        PriorityQueue<Node> pq = new PriorityQueue<>(2 * n, comparator);
        pq.offer(new Node(start, 0));

        boolean[] visited = new boolean[n];
        prev = new Integer[n];

        while (!pq.isEmpty()) {
            Node node = pq.poll();
            visited[node.id] = true;

            if (dist[node.id] < node.value)
                continue;

            List<Edge> edges = graph.get(node.id);
            for (Edge edge : edges) {
                if (visited[edge.to])
                    continue;

                double newDist = dist[edge.from] + edge.cost;
                if (newDist < dist[edge.to]) {
                    prev[edge.to] = edge.from;
                    dist[edge.to] = newDist;
                    pq.offer(new Node(edge.to, dist[edge.to]));
                }
            }
            if (node.id == end)
                return dist[end];
        }
        return Long.MAX_VALUE;
    }

    private void createEmptyGraph() {
        graph = new ArrayList<>(n);
        for (int i = 0; i < n; i++)
            graph.add(new ArrayList<>());
    }

    public static class Edge {
        long cost;
        int from, to;

        public Edge(int from, int to, long cost) {
            this.from = from;
            this.to = to;
            this.cost = cost;
        }
    }

    public static class Node {
        int id;
        double value;

        public Node(int id, double value) {
            this.id = id;
            this.value = value;
        }
    }
}