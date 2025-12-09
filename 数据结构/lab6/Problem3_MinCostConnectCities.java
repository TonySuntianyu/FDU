import java.util.*;

/**
 * 题目3：连接所有城市的最低成本
 * 
 * 算法思路：
 * 这是一个最小生成树（MST）问题。需要使用Kruskal算法或Prim算法来求解。
 * 本实现使用Kruskal算法，结合并查集（Union-Find）来判断是否形成环。
 * 
 * Kruskal算法步骤：
 * 1. 将所有边按权重从小到大排序
 * 2. 依次选择权重最小的边，如果该边的两个端点不在同一个连通分量中，则加入MST
 * 3. 使用并查集来高效判断两个节点是否在同一个连通分量中
 * 4. 如果最终MST包含n-1条边（n个节点），则所有城市连通，输出总成本；否则输出-1
 * 
 * 时间复杂度：O(m*log m)，其中m是边的数量（主要是排序的时间复杂度）
 * 空间复杂度：O(n)，用于并查集
 */
public class Problem3_MinCostConnectCities {
    // 边类，用于存储边的信息
    static class Edge {
        int from;
        int to;
        int cost;
        
        Edge(int from, int to, int cost) {
            this.from = from;
            this.to = to;
            this.cost = cost;
        }
    }
    
    // 并查集类
    static class UnionFind {
        private int[] parent;
        private int[] rank;
        
        /**
         * 初始化并查集
         * @param n 节点数量
         */
        UnionFind(int n) {
            parent = new int[n];
            rank = new int[n];
            // 初始化：每个节点的父节点是自己，秩为0
            for (int i = 0; i < n; i++) {
                parent[i] = i;
                rank[i] = 0;
            }
        }
        
        /**
         * 查找节点x的根节点（带路径压缩优化）
         * @param x 节点编号
         * @return 根节点编号
         */
        int find(int x) {
            if (parent[x] != x) {
                // 路径压缩：将x的父节点直接指向根节点
                parent[x] = find(parent[x]);
            }
            return parent[x];
        }
        
        /**
         * 合并两个节点所在的集合（按秩合并优化）
         * @param x 节点1
         * @param y 节点2
         * @return 如果两个节点已经在同一个集合中，返回false；否则返回true
         */
        boolean union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);
            
            // 如果两个节点已经在同一个集合中，返回false
            if (rootX == rootY) {
                return false;
            }
            
            // 按秩合并：将秩小的树合并到秩大的树下
            if (rank[rootX] < rank[rootY]) {
                parent[rootX] = rootY;
            } else if (rank[rootX] > rank[rootY]) {
                parent[rootY] = rootX;
            } else {
                // 秩相等时，任意选择一个作为根，并增加其秩
                parent[rootY] = rootX;
                rank[rootX]++;
            }
            
            return true;
        }
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // 读取城市数量和公路数量
        int n = scanner.nextInt();
        int m = scanner.nextInt();
        
        // 读取所有边的信息
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            int a = scanner.nextInt();
            int b = scanner.nextInt();
            int c = scanner.nextInt();
            edges.add(new Edge(a, b, c));
        }
        
        // 计算最小生成树的总成本
        int minCost = kruskalMST(edges, n);
        System.out.println(minCost);
        
        scanner.close();
    }
    
    /**
     * 使用Kruskal算法计算最小生成树的总成本
     * 
     * @param edges 所有边的列表
     * @param n 节点数量
     * @return 最小生成树的总成本，如果图不连通则返回-1
     */
    private static int kruskalMST(List<Edge> edges, int n) {
        // 按边的权重从小到大排序
        edges.sort((e1, e2) -> Integer.compare(e1.cost, e2.cost));
        
        // 初始化并查集
        UnionFind uf = new UnionFind(n);
        
        int totalCost = 0;
        int edgeCount = 0; // 已加入MST的边数
        
        // 遍历所有边（已按权重排序）
        for (Edge edge : edges) {
            // 如果该边的两个端点不在同一个连通分量中
            if (uf.union(edge.from, edge.to)) {
                // 将该边加入MST
                totalCost += edge.cost;
                edgeCount++;
                
                // 如果已经加入了n-1条边，说明所有节点都已连通
                if (edgeCount == n - 1) {
                    break;
                }
            }
        }
        
        // 如果最终MST的边数少于n-1，说明图不连通
        if (edgeCount < n - 1) {
            return -1;
        }
        
        return totalCost;
    }
}

