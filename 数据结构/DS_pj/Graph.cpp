#include "Graph.h"
#include <iostream>
#include <algorithm>

Graph::Graph() {
}

Graph::~Graph() {
}

void Graph::addVertex(const std::string& vertex) {
    if (adjacencyList.find(vertex) == adjacencyList.end()) {
        adjacencyList[vertex] = std::vector<Edge>();
        vertices.push_back(vertex);
        vertexIndex[vertex] = vertices.size() - 1;
    }
}

void Graph::addEdge(const std::string& from, const std::string& to, 
                    double weight, const std::string& roadId,
                    int length, int speedLimit, int lanes, int vehicles) {
    // 确保两个顶点都存在
    addVertex(from);
    addVertex(to);
    
    // 添加边
    Edge edge(from, to, weight, roadId, length, speedLimit, lanes, vehicles);
    adjacencyList[from].push_back(edge);
}

std::vector<Edge> Graph::getEdges(const std::string& from) const {
    auto it = adjacencyList.find(from);
    if (it != adjacencyList.end()) {
        return it->second;
    }
    return std::vector<Edge>();
}

std::vector<std::string> Graph::getVertices() const {
    return vertices;
}

bool Graph::hasVertex(const std::string& vertex) const {
    return adjacencyList.find(vertex) != adjacencyList.end();
}

bool Graph::hasEdge(const std::string& from, const std::string& to) const {
    auto it = adjacencyList.find(from);
    if (it != adjacencyList.end()) {
        for (const auto& edge : it->second) {
            if (edge.to == to) {
                return true;
            }
        }
    }
    return false;
}

size_t Graph::getEdgeCount() const {
    size_t count = 0;
    for (const auto& pair : adjacencyList) {
        count += pair.second.size();
    }
    return count;
}

size_t Graph::getVertexCount() const {
    return vertices.size();
}

void Graph::clear() {
    adjacencyList.clear();
    vertices.clear();
    vertexIndex.clear();
}

void Graph::updateEdgeWeight(const std::string& from, const std::string& to, 
                             double newWeight) {
    auto it = adjacencyList.find(from);
    if (it != adjacencyList.end()) {
        for (auto& edge : it->second) {
            if (edge.to == to) {
                edge.weight = newWeight;
                return;
            }
        }
    }
}

void Graph::updateEdgeVehicles(const std::string& from, const std::string& to, 
                                int newVehicles) {
    auto it = adjacencyList.find(from);
    if (it != adjacencyList.end()) {
        for (auto& edge : it->second) {
            if (edge.to == to) {
                edge.vehicles = newVehicles;
                return;
            }
        }
    }
}

void Graph::printGraph() const {
    std::cout << "图信息：" << std::endl;
    std::cout << "顶点数: " << getVertexCount() << std::endl;
    std::cout << "边数: " << getEdgeCount() << std::endl;
    std::cout << "\n邻接表：" << std::endl;
    for (const auto& pair : adjacencyList) {
        std::cout << pair.first << " -> ";
        for (const auto& edge : pair.second) {
            std::cout << edge.to << " (权重: " << edge.weight << ") ";
        }
        std::cout << std::endl;
    }
}

