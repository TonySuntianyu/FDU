#include <iostream>
#include <vector>
#include <queue>
#include <algorithm>
using namespace std;

// 边的结构体，用于存储目标顶点和权重
struct Edge {
    int to;
    int weight;
    
    Edge(int to, int weight) : to(to), weight(weight) {}
};

int main() {
    // 读取输入
    int n, m;
    cin >> n; // 事件数（顶点数）
    cin >> m; // 活动数（边数）
    
    // 构建图：邻接表存储边
    vector<vector<Edge>> graph(n);
    // 反向图：用于计算TE（最早发生时间）
    vector<vector<Edge>> reverseGraph(n);
    // 入度数组
    vector<int> inDegree(n, 0);
    
    // 读取边并构建图
    for (int i = 0; i < m; i++) {
        int u, v, t;
        cin >> u >> v >> t;
        
        graph[u].push_back(Edge(v, t));
        reverseGraph[v].push_back(Edge(u, t));
        inDegree[v]++;
    }
    
    // 1. 拓扑排序
    vector<int> topoOrder;
    queue<int> q;
    
    // 找到所有入度为0的顶点（起点）
    for (int i = 0; i < n; i++) {
        if (inDegree[i] == 0) {
            q.push(i);
        }
    }
    
    // 执行拓扑排序
    while (!q.empty()) {
        int u = q.front();
        q.pop();
        topoOrder.push_back(u);
        
        for (const Edge& edge : graph[u]) {
            int v = edge.to;
            inDegree[v]--;
            if (inDegree[v] == 0) {
                q.push(v);
            }
        }
    }
    
    // 2. 计算TE（最早发生时间）
    vector<int> TE(n, 0);
    // 起点事件的最早发生时间为0
    TE[0] = 0;
    
    // 按照拓扑顺序计算每个事件的最早发生时间
    for (int u : topoOrder) {
        // 遍历所有指向u的边（在反向图中）
        for (const Edge& edge : reverseGraph[u]) {
            int prev = edge.to; // 前驱事件
            int weight = edge.weight; // 活动持续时间
            // TE[u] = max(所有前驱的TE + 对应边的权重)
            TE[u] = max(TE[u], TE[prev] + weight);
        }
    }
    
    // 3. 计算TL（最迟发生时间）
    vector<int> TL(n);
    // 终点事件的最迟发生时间等于最早发生时间（项目总工期）
    int totalDuration = TE[n - 1];
    fill(TL.begin(), TL.end(), totalDuration);
    TL[n - 1] = totalDuration;
    
    // 按照逆拓扑顺序计算每个事件的最迟发生时间
    for (int i = topoOrder.size() - 1; i >= 0; i--) {
        int u = topoOrder[i];
        // 遍历所有从u出发的边
        for (const Edge& edge : graph[u]) {
            int v = edge.to; // 后继事件
            int weight = edge.weight; // 活动持续时间
            // TL[u] = min(所有后继的TL - 对应边的权重)
            TL[u] = min(TL[u], TL[v] - weight);
        }
    }
    
    // 4. 找出关键路径
    vector<int> criticalPath;
    // 关键路径上的事件满足：TE[i] == TL[i]
    for (int u : topoOrder) {
        if (TE[u] == TL[u]) {
            criticalPath.push_back(u);
        }
    }
    
    // 5. 输出结果
    // 输出TE
    for (int i = 0; i < n; i++) {
        cout << TE[i];
        if (i < n - 1) {
            cout << " ";
        }
    }
    cout << endl;
    
    // 输出TL
    for (int i = 0; i < n; i++) {
        cout << TL[i];
        if (i < n - 1) {
            cout << " ";
        }
    }
    cout << endl;
    
    // 输出关键路径
    for (int i = 0; i < criticalPath.size(); i++) {
        cout << criticalPath[i];
        if (i < criticalPath.size() - 1) {
            cout << " ";
        }
    }
    cout << endl;
    
    // 输出项目最短总工期
    cout << totalDuration << endl;
    
    return 0;
}

