#include "Dijkstra.h"
#include <queue>
#include <unordered_map>
#include <limits>
#include <algorithm>

// 用于优先队列的节点
struct Node {
    std::string vertex;
    double distance;
    
    Node(const std::string& v, double d) : vertex(v), distance(d) {}
    
    // 用于优先队列（最小堆）
    bool operator>(const Node& other) const {
        return distance > other.distance;
    }
};

PathResult Dijkstra::findShortestPath(const Graph& graph, 
                                      const std::string& start, 
                                      const std::string& end) {
    PathResult result;
    
    // 检查起点和终点是否存在
    if (!graph.hasVertex(start) || !graph.hasVertex(end)) {
        result.found = false;
        return result;
    }
    
    // 如果起点和终点相同
    if (start == end) {
        result.path.push_back(start);
        result.totalWeight = 0.0;
        result.found = true;
        return result;
    }
    
    // 距离表：从起点到各顶点的最短距离
    std::unordered_map<std::string, double> dist;
    
    // 前驱表：用于回溯路径
    std::unordered_map<std::string, std::string> prev;
    
    // 已访问集合
    std::unordered_map<std::string, bool> visited;
    
    // 初始化所有顶点的距离为无穷大
    auto vertices = graph.getVertices();
    for (const auto& v : vertices) {
        dist[v] = std::numeric_limits<double>::max();
        visited[v] = false;
    }
    
    // 起点距离为0
    dist[start] = 0.0;
    
    // 优先队列（最小堆）
    std::priority_queue<Node, std::vector<Node>, std::greater<Node>> pq;
    pq.push(Node(start, 0.0));
    
    // Dijkstra主循环
    while (!pq.empty()) {
        // 取出距离最小的顶点
        Node current = pq.top();
        pq.pop();
        
        std::string u = current.vertex;
        
        // 如果已经访问过，跳过
        if (visited[u]) {
            continue;
        }
        
        // 标记为已访问
        visited[u] = true;
        
        // 如果到达终点，可以提前结束（可选优化）
        if (u == end) {
            break;
        }
        
        // 遍历所有邻接边
        auto edges = graph.getEdges(u);
        for (const auto& edge : edges) {
            std::string v = edge.to;
            double weight = edge.weight;
            
            // 如果找到更短的路径
            if (!visited[v] && dist[u] + weight < dist[v]) {
                dist[v] = dist[u] + weight;
                prev[v] = u;
                pq.push(Node(v, dist[v]));
            }
        }
    }
    
    // 如果无法到达终点
    if (dist[end] == std::numeric_limits<double>::max()) {
        result.found = false;
        return result;
    }
    
    // 回溯路径
    std::vector<std::string> path;
    std::string current = end;
    
    while (current != start) {
        path.push_back(current);
        if (prev.find(current) != prev.end()) {
            current = prev[current];
        } else {
            // 无法回溯，说明路径不存在
            result.found = false;
            return result;
        }
    }
    path.push_back(start);
    
    // 反转路径（从起点到终点）
    std::reverse(path.begin(), path.end());
    
    result.path = path;
    result.totalWeight = dist[end];
    result.found = true;
    
    return result;
}

std::string Dijkstra::pathToString(const std::vector<std::string>& path) {
    if (path.empty()) {
        return "";
    }
    
    std::string result = path[0];
    for (size_t i = 1; i < path.size(); i++) {
        result += "-->";
        result += path[i];
    }
    
    return result;
}

