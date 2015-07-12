package features;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by phil on 05.07.15.
 */
public class RetweetGraph {
    private List<RetweetGraphNode> nodes;


    public RetweetGraph() {
        nodes = new ArrayList<RetweetGraphNode>();
    }


    public void addNode(RetweetGraphNode x) {
        nodes.add(x);
    }

    public void removeNode(RetweetGraphNode x) {
        nodes.remove(x);
    }

    public void addDirectedEdge(RetweetGraphNode from, RetweetGraphNode to) {
        //make sure nodes are in the graph
        if (!nodes.contains(from)) {
            nodes.add(from);
        }
        if (!nodes.contains(to)) {
            nodes.add(to);
        }
        from.addNeighbor(to);

    }

    public void addUndirectedEdge(RetweetGraphNode x1, RetweetGraphNode x2) {
        //make sure nodes are in the graph
        if (!nodes.contains(x1)) {
            nodes.add(x1);
        }
        if (!nodes.contains(x2)) {
            nodes.add(x2);
        }
        x1.addNeighbor(x2);
        x2.addNeighbor(x1);
    }


    public double getAverageDegree() {
        int sum = 0;
        for (RetweetGraphNode x : nodes) {
            sum += x.getDegree();
        }

        return sum / ((float) nodes.size()); //force float division
    }


    public int getNumberOfNodes() {
        return nodes.size();
    }

    public int getNumberOfEdges() {
        int sum = 0;
        for (RetweetGraphNode x : nodes) {
            sum += x.getDegree();
        }
        return sum;
    }


    public RetweetGraphNode getNodeFromName(String name) {
        for (RetweetGraphNode x : nodes) {
            if (x.getName() == name) {
                return x;
            }
        }
        RetweetGraphNode newNode = new RetweetGraphNode(name);
        nodes.add(newNode);
        return newNode;
    }

    public RetweetGraphNode getNodeFromId(int id) {
        return nodes.get(id);
    }


    public int getDiameter() {
        //calculated using floyd warshall
        int[][] distanceMatrix = new int[nodes.size()][nodes.size()];

        //initial values
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                RetweetGraphNode iNode = getNodeFromId(i);
                RetweetGraphNode jNode = getNodeFromId(j);
                if (iNode.hasNeighbor(jNode)) {
                    distanceMatrix[i][j] = 1;
                } else {
                    distanceMatrix[i][j] = Integer.MAX_VALUE; //set to inf
                }
            }
        }

        //floyd warshall dynamic programming using matrix
        for (int k = 0; k < distanceMatrix.length; k++) {
            for (int i = 0; i < distanceMatrix.length; i++) {
                for (int j = 0; j < distanceMatrix.length; j++) {
                    distanceMatrix[i][j] = Math.min(distanceMatrix[i][j], distanceMatrix[i][k] + distanceMatrix[k][j]);

                    //handle integer overflow (max value + x)
                    if (distanceMatrix[i][j] < 0) {
                        distanceMatrix[i][j] = Integer.MAX_VALUE; //set to inf
                    }
                }
            }
        }


        //find maximum and ignore inf
        int max = -1;
        //initial values
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                //System.out.println(distanceMatrix[i][j]);
                if (distanceMatrix[i][j] > max && distanceMatrix[i][j] < Integer.MAX_VALUE) {
                    max = distanceMatrix[i][j];
                }
            }
        }

        return max;
    }


}
