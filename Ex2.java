import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;

public class Ex2 {

    private static Map<String, List<String>> graph = new HashMap<>();
    private static Map<String, Integer> inDegree = new HashMap<>();

    public static void main(String[] args) throws IOException, FileFormatException, CycleFound {
        // Choose a file in the folder Graphs in the current directory
        JFileChooser jf = new JFileChooser("Graphs");
        int result = jf.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jf.getSelectedFile();
            readGraph(selectedFile); // Read nodes and edges from the selected file
            topSort(); // Perform topological sorting and print the result
        }
    }

    // Read in a graph from a file and populate the graph and inDegree
    public static void readGraph(File selectedFile) throws IOException, FileFormatException {
        BufferedReader r = new BufferedReader(new FileReader(selectedFile));
        String line = null;

        try {
            // Skip over comment lines in the beginning of the file
            while (!(line = r.readLine()).equalsIgnoreCase("[Vertex]"));
            System.out.println(); System.out.println("Nodes:");

            // Read all vertex definitions
            while (!(line = r.readLine()).equalsIgnoreCase("[Edges]")) {
                if (line.trim().length() > 0) {  // Skip empty lines
                    try {
                        String[] nodeNames = line.split(",");
                        for (String n : nodeNames) {
                            n = n.trim();
                            System.out.println(n); // Print the node name
                            graph.putIfAbsent(n, new ArrayList<>());
                            inDegree.putIfAbsent(n, 0); // Initialize inDegree
                        }
                    } catch (Exception e) { // Something wrong in the graph file
                        r.close();
                        throw new FileFormatException("Error in vertex definitions");
                    }
                }
            }
        } catch (NullPointerException e1) { // The input file has the wrong format
            throw new FileFormatException("No [Vertex] or [Edges] section found in the file " + selectedFile.getName());
        }

        System.out.println(); System.out.println("Edges:");
        // Read all edge definitions
        while ((line = r.readLine()) != null) {
            if (line.trim().length() > 0) {  // Skip empty lines
                try {
                    String[] edges = line.split(","); // Edges are comma-separated pairs e1:e2
                    for (String e : edges) {
                        String[] edgePair = e.trim().split(":"); // Split edge components v1:v2
                        String from = edgePair[0].trim();
                        String to = edgePair[1].trim();
                        System.out.println(from + " -> " + to);

                        // Populate graph and update inDegree
                        graph.get(from).add(to);
                        inDegree.put(to, inDegree.getOrDefault(to, 0) + 1);
                        inDegree.putIfAbsent(from, 0); // Ensure from-node exists in inDegree
                    }
                } catch (Exception e) { // Something is wrong, Edges should be in format v1:v2
                    r.close();
                    throw new FileFormatException("Error in edge definition");
                }
            }
        }
        r.close(); // Close the reader
    }

    // Topological sorting using Kahn's algorithm
    public static void topSort() throws CycleFound {
        Queue<String> queue = new LinkedList<>();
        List<String> sorted = new ArrayList<>();

        // Add all nodes with inDegree 0 to the queue
        for (String node : inDegree.keySet()) {
            if (inDegree.get(node) == 0) {
                queue.add(node);
            }
        }

        while (!queue.isEmpty()) {
            String current = queue.poll();
            sorted.add(current);

            // Reduce inDegree of neighbors and add to queue if inDegree becomes 0
            for (String neighbor : graph.getOrDefault(current, new ArrayList<>())) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.add(neighbor);
                }
            }
        }

        // If sorted list does not contain all vertices, there's a cycle
        if (sorted.size() != graph.size()) {
            throw new CycleFound("The graph contains a cycle and cannot be topologically sorted.");
        }

        System.out.println("\nTopological Sort:");
        for (String node : sorted) {
            System.out.print(node + " ");
        }
    }
}

@SuppressWarnings("serial")
class FileFormatException extends Exception { // Input file has the wrong format
    public FileFormatException(String message) {
        super(message);
    }
}

@SuppressWarnings("serial")
class CycleFound extends Exception { // The graph is cyclic
    public CycleFound(String message) {
        super(message);
    }
}
