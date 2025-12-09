import java.util.*;

public class Main {
    // 边的内部类，用于存储目标顶点和权重
    static class Edge {
        int to;
        int weight;
        
        Edge(int to, int weight) {
            this.to = to;
            this.weight = weight;
        }
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // 读取输入
        int n = scanner.nextInt(); // 事件数（顶点数）
        int m = scanner.nextInt(); // 活动数（边数）
        
        // 构建图：邻接表存储边
        List<List<Edge>> graph = new ArrayList<>();
        // 反向图：用于计算TE（最早发生时间）
        List<List<Edge>> reverseGraph = new ArrayList<>();
        // 入度数组
        int[] inDegree = new int[n];
        
        // 初始化图
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
            reverseGraph.add(new ArrayList<>());
        }
        
        // 读取边并构建图
        for (int i = 0; i < m; i++) {
            int u = scanner.nextInt();
            int v = scanner.nextInt();
            int t = scanner.nextInt();
            
            graph.get(u).add(new Edge(v, t));
            reverseGraph.get(v).add(new Edge(u, t));
            inDegree[v]++;
        }
        
        // 1. 拓扑排序
        List<Integer> topoOrder = new ArrayList<>();
        Queue<Integer> queue = new LinkedList<>();
        
        // 找到所有入度为0的顶点（起点）
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                queue.offer(i);
            }
        }
        
        // 执行拓扑排序
        while (!queue.isEmpty()) {
            int u = queue.poll();
            topoOrder.add(u);
            
            for (Edge edge : graph.get(u)) {
                int v = edge.to;
                inDegree[v]--;
                if (inDegree[v] == 0) {
                    queue.offer(v);
                }
            }
        }
        
        // 2. 计算TE（最早发生时间）
        int[] TE = new int[n];
        // 起点事件的最早发生时间为0
        TE[0] = 0;
        
        // 按照拓扑顺序计算每个事件的最早发生时间
        for (int u : topoOrder) {
            // 遍历所有指向u的边（在反向图中）
            for (Edge edge : reverseGraph.get(u)) {
                int prev = edge.to; // 前驱事件
                int weight = edge.weight; // 活动持续时间
                // TE[u] = max(所有前驱的TE + 对应边的权重)
                TE[u] = Math.max(TE[u], TE[prev] + weight);
            }
        }
        
        // 3. 计算TL（最迟发生时间）
        int[] TL = new int[n];
        // 终点事件的最迟发生时间等于最早发生时间（项目总工期）
        int totalDuration = TE[n - 1];
        Arrays.fill(TL, totalDuration);
        TL[n - 1] = totalDuration;
        
        // 按照逆拓扑顺序计算每个事件的最迟发生时间
        for (int i = topoOrder.size() - 1; i >= 0; i--) {
            int u = topoOrder.get(i);
            // 遍历所有从u出发的边
            for (Edge edge : graph.get(u)) {
                int v = edge.to; // 后继事件
                int weight = edge.weight; // 活动持续时间
                // TL[u] = min(所有后继的TL - 对应边的权重)
                TL[u] = Math.min(TL[u], TL[v] - weight);
            }
        }
        
        // 4. 找出关键路径
        List<Integer> criticalPath = new ArrayList<>();
        // 关键路径上的事件满足：TE[i] == TL[i]
        for (int u : topoOrder) {
            if (TE[u] == TL[u]) {
                criticalPath.add(u);
            }
        }
        
        // 5. 输出结果
        // 输出TE
        for (int i = 0; i < n; i++) {
            System.out.print(TE[i]);
            if (i < n - 1) {
                System.out.print(" ");
            }
        }
        System.out.println();
        
        // 输出TL
        for (int i = 0; i < n; i++) {
            System.out.print(TL[i]);
            if (i < n - 1) {
                System.out.print(" ");
            }
        }
        System.out.println();
        
        // 输出关键路径
        for (int i = 0; i < criticalPath.size(); i++) {
            System.out.print(criticalPath.get(i));
            if (i < criticalPath.size() - 1) {
                System.out.print(" ");
            }
        }
        System.out.println();
        
        // 输出项目最短总工期
        System.out.println(totalDuration);
        
        scanner.close();
    }
}

