#ifndef DIJKSTRA_H
#define DIJKSTRA_H

#include <string>
#include <vector>
#include "Graph.h"

// 最短路径结果
struct PathResult {
    std::vector<std::string> path;  // 路径（地点序列）
    double totalWeight;              // 总权重
    bool found;                      // 是否找到路径
    
    PathResult() : totalWeight(0.0), found(false) {}
};

// Dijkstra算法实现
class Dijkstra {
public:
    // 使用Dijkstra算法计算最短路径
    static PathResult findShortestPath(const Graph& graph, 
                                       const std::string& start, 
                                       const std::string& end);
    
    // 将路径转换为输出格式（用-->连接）
    static std::string pathToString(const std::vector<std::string>& path);
};

#endif // DIJKSTRA_H

