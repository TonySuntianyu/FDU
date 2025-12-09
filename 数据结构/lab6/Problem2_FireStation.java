import java.util.Scanner;

/**
 * 题目2：消防局选址问题
 * 
 * 算法思路：
 * 这是一个图论中的"图的中心"问题。需要找到图中eccentricity（偏心率）最小的节点。
 * eccentricity定义为：从该节点到其他所有节点的最短路径的最大值。
 * 
 * 解决步骤：
 * 1. 使用Floyd-Warshall算法计算所有点对之间的最短路径
 * 2. 对于每个节点，计算其到其他所有节点的最短路径的最大值（eccentricity）
 * 3. 选择eccentricity最小的节点，如果有多个，选择编号最小的
 * 
 * 时间复杂度：O(n^3)，其中n是节点数量（Floyd-Warshall算法）
 * 空间复杂度：O(n^2)，用于存储距离矩阵
 */
public class Problem2_FireStation {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            // 读取街区数量
            int n = scanner.nextInt();
            
            // 读取邻接矩阵
            int[][] graph = new int[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    graph[i][j] = scanner.nextInt();
                }
            }
            
            // 计算最优消防局选址
            int optimalLocation = findOptimalFireStation(graph, n);
            System.out.println(optimalLocation);
        }
    }
    
    /**
     * 找到最优的消防局选址
     * 
     * @param graph 邻接矩阵，graph[i][j]表示街区i到街区j的直接道路长度
     * @param n 街区数量
     * @return 最优消防局选址的街区编号
     */
    private static int findOptimalFireStation(int[][] graph, int n) {
        // 使用Floyd-Warshall算法计算所有点对之间的最短路径
        int[][] dist = floydWarshall(graph, n);
        
        // 对于每个节点，计算其eccentricity（到其他所有节点的最短路径的最大值）
        int minEccentricity = Integer.MAX_VALUE;
        int optimalNode = -1;
        
        for (int i = 0; i < n; i++) {
            int eccentricity = 0;
            boolean reachable = true;
            
            // 找到从节点i到其他所有节点的最短路径的最大值
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    continue;
                }
                if (dist[i][j] == Integer.MAX_VALUE) {
                    reachable = false;
                    break;
                }
                if (dist[i][j] > eccentricity) {
                    eccentricity = dist[i][j];
                }
            }
            
            if (!reachable) {
                continue;
            }
            
            // 如果找到更小的eccentricity，或者eccentricity相同但编号更小
            if (eccentricity < minEccentricity) {
                minEccentricity = eccentricity;
                optimalNode = i;
            }
        }
        
        return optimalNode;
    }
    
    /**
     * Floyd-Warshall算法：计算所有点对之间的最短路径
     * 
     * @param graph 邻接矩阵
     * @param n 节点数量
     * @return 最短路径距离矩阵
     */
    private static int[][] floydWarshall(int[][] graph, int n) {
        // 初始化距离矩阵
        int[][] dist = new int[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    // 节点到自身的距离为0
                    dist[i][j] = 0;
                } else if (graph[i][j] != 0) {
                    // 如果存在直接道路，距离为道路长度
                    dist[i][j] = graph[i][j];
                } else {
                    // 如果不存在直接道路，初始化为无穷大
                    dist[i][j] = Integer.MAX_VALUE;
                }
            }
        }
        
        // Floyd-Warshall算法的核心：三重循环
        // 对于每个中间节点k，尝试通过k来缩短i到j的距离
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    // 如果通过节点k可以缩短i到j的距离
                    if (dist[i][k] != Integer.MAX_VALUE && 
                        dist[k][j] != Integer.MAX_VALUE &&
                        dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                    }
                }
            }
        }
        
        return dist;
    }
}

