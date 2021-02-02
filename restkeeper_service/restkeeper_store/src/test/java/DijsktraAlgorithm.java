import java.util.Arrays;

public class DijsktraAlgorithm {

    private static final int UNREACHABLE = 6666666;

    public static void main(String[] args) {
        int[][] adjacencyMatrix = {
                {0, 5, 1, UNREACHABLE, UNREACHABLE},
                {5, 0 ,2, UNREACHABLE, 1},
                {1, 2, 0, 1, UNREACHABLE},
                {UNREACHABLE, UNREACHABLE, 1, 0, 3},
                {UNREACHABLE, 1, UNREACHABLE, 3, 0}
        };
        int[] operator = new DijsktraAlgorithm().operator(0, adjacencyMatrix);
        System.out.println("The shortest distances from node 'A' to 'BCDE' node are as follows: " + Arrays.toString(operator));
    }

    private int[] operator(int startingPoint, int[][] adjacencyMatrix) {
        int[] shortestPathway = new int[adjacencyMatrix.length];
        shortestPathway[0] = 0;// The distance from the starting point to itself is 0
        String[] nodeName = new String[adjacencyMatrix.length];
        for (int i = 0; i < adjacencyMatrix.length; i++) {
            nodeName[i] = startingPoint + "==>" + i;
        }
        int accessedVertex[] = new int[adjacencyMatrix.length];
        accessedVertex[0] = 1;
        for (int i1 = 1; i1 < adjacencyMatrix.length; i1++) {
            int dm = Integer.MAX_VALUE;
            int x = -1;
            for (int i = 0; i < adjacencyMatrix.length; i++) {
                if (adjacencyMatrix[startingPoint][i] < dm && accessedVertex[i] == 0) {
                    dm = adjacencyMatrix[startingPoint][i];
                         x = i;
                }
            }
            accessedVertex[x] = 1;
            // Select an unmarked vertex closest to the starting point, mark the newly selected vertex as the shortest path and the shortest path to the starting point is dm
            shortestPathway[x] = dm;
            // Using x as the middle point, modify the distance from start to the unvisited points
            for (int i = 0; i < adjacencyMatrix.length; i++) {
                if (adjacencyMatrix[startingPoint][i] > adjacencyMatrix[startingPoint][x] + adjacencyMatrix[x][i] && accessedVertex[i] == 0) {
                    adjacencyMatrix[startingPoint][i] = adjacencyMatrix[startingPoint][x] + adjacencyMatrix[x][i];
                    nodeName[i] = Arrays.toString(nodeName) + "==>" + i;
                }
            }
        }
        String node = "";
        String start = "";
        for (int i = 0; i < adjacencyMatrix.length; i++) {
            if (i == 0) {
                node = "A";
            }
            if (i == 1) {
                node = "B";
            }
            if (i == 2) {
                node = "C";
            }
            if (i == 3) {
                node = "D";
            }
            if (i == 4) {
                node = "E";
            }
            if (startingPoint == 0) {
                start = "A";
            }
            System.out.println("From " + start + " set out to " + node + " shortest path is: ") ;
            System.out.println(nodeName[i] + " = " + shortestPathway[i]);
        }
        return shortestPathway;
    }

}
