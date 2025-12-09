#ifndef GRAPH_H
#define GRAPH_H

#include <string>
#include <vector>
#include <map>
#include <unordered_map>
#include <limits>

// 边的结构体
struct Edge {
    std::string from;      // 起始地点
    std::string to;        // 目标地点
    double weight;         // 权重（堵车系数）
    std::string roadId;    // 道路ID
    int length;            // 道路长度（米）
    int speedLimit;        // 道路限速（km/h）
    int lanes;             // 车道数
    int vehicles;          // 现有车辆数
    
    Edge(const std::string& f, const std::string& t, double w, 
         const std::string& id = "", int len = 0, int speed = 0, 
         int l = 0, int v = 0)
        : from(f), to(t), weight(w), roadId(id), length(len), 
          speedLimit(speed), lanes(l), vehicles(v) {}
};

// 图类
class Graph {
private:
    // 使用邻接表表示图
    // key: 地点名称, value: 从该地点出发的所有边
    std::unordered_map<std::string, std::vector<Edge>> adjacencyList;
    
    // 所有地点的集合
    std::vector<std::string> vertices;
    
    // 地点名称到索引的映射（用于快速查找）
    std::map<std::string, int> vertexIndex;
    
public:
    Graph();
    ~Graph();
    
    // 基础接口
    void addEdge(const std::string& from, const std::string& to, 
                 double weight, const std::string& roadId = "",
                 int length = 0, int speedLimit = 0, 
                 int lanes = 0, int vehicles = 0);
    
    void addVertex(const std::string& vertex);
    
    // 获取从某个地点出发的所有边
    std::vector<Edge> getEdges(const std::string& from) const;
    
    // 获取所有顶点
    std::vector<std::string> getVertices() const;
    
    // 检查顶点是否存在
    bool hasVertex(const std::string& vertex) const;
    
    // 检查边是否存在
    bool hasEdge(const std::string& from, const std::string& to) const;
    
    // 获取边的数量
    size_t getEdgeCount() const;
    
    // 获取顶点的数量
    size_t getVertexCount() const;
    
    // 清空图
    void clear();
    
    // 更新边的权重（用于动态更新）
    void updateEdgeWeight(const std::string& from, const std::string& to, 
                          double newWeight);
    
    // 更新边的车辆数（用于动态更新）
    void updateEdgeVehicles(const std::string& from, const std::string& to, 
                            int newVehicles);
    
    // 打印图的信息（用于调试）
    void printGraph() const;
};

#endif // GRAPH_H

