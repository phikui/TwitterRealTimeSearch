package features;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by phil on 05.07.15.
 */
public class RetweetGraphNode {
    private static int idCounter = 0;
    private int id;
    private String name;
    private List<RetweetGraphNode> neighbors;


    public RetweetGraphNode(String name) {
        this.name = name;
        id = idCounter;
        idCounter++;
        neighbors = new ArrayList<>();
    }

    public List<RetweetGraphNode> getNeighbors() {
        return neighbors;
    }

    public void addNeighbor(RetweetGraphNode x) {
        neighbors.add(x);
    }

    public void removeNeighbor(RetweetGraphNode x) {
        neighbors.remove(x);
    }

    public int getDegree() {
        return neighbors.size();
    }

    public String getName() {
        return name;
    }

    public boolean hasNeighbor(RetweetGraphNode x) {
        return neighbors.contains(x);
    }
}
