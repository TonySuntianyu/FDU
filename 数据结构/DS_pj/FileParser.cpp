#include "FileParser.h"
#include <fstream>
#include <sstream>
#include <iostream>
#include <algorithm>
#include "TrafficCalculator.h"

Demand FileParser::parseDemand(const std::string& filename) {
    Demand demand;
    std::ifstream file(filename);
    
    if (!file.is_open()) {
        std::cerr << "无法打开需求文件: " << filename << std::endl;
        return demand;
    }
    
    std::string line;
    while (std::getline(file, line)) {
        // 查找起点
        size_t startPos = line.find("起点");
        if (startPos != std::string::npos) {
            size_t colonPos = line.find("：", startPos);
            if (colonPos != std::string::npos) {
                // 跳过"："（UTF-8编码占3个字节）
                demand.start = trim(line.substr(colonPos + 3));
            }
        }
        // 查找终点
        size_t endPos = line.find("终点");
        if (endPos != std::string::npos) {
            size_t colonPos = line.find("：", endPos);
            if (colonPos != std::string::npos) {
                demand.end = trim(line.substr(colonPos + 3));
            }
        }
    }
    
    file.close();
    return demand;
}

void FileParser::parseMapFile(const std::string& filename, Graph& graph) {
    std::ifstream file(filename);
    
    if (!file.is_open()) {
        std::cerr << "无法打开地图文件: " << filename << std::endl;
        return;
    }
    
    std::string line;
    bool isFirstLine = true;
    
    while (std::getline(file, line)) {
        // 跳过表头
        if (isFirstLine) {
            isFirstLine = false;
            continue;
        }
        
        // 跳过空行
        if (line.empty()) {
            continue;
        }
        
        std::stringstream ss(line);
        std::string token;
        std::vector<std::string> tokens;
        
        // 解析CSV行（处理可能的引号）
        bool inQuotes = false;
        std::string currentToken;
        
        for (char c : line) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.push_back(trim(currentToken));
                currentToken.clear();
            } else {
                currentToken += c;
            }
        }
        tokens.push_back(trim(currentToken)); // 最后一个token
        
        // 根据CSV格式解析
        // 格式1: 道路ID,起始地点,目标地点,道路方向,道路长度(米),道路限速(km/h),车道数,现有车辆数
        // 格式2: 道路ID,起始地点,目标地点,道路类型,道路方向,道路长度(米),道路限速(km/h),车道数,现有车辆数
        
        if (tokens.size() >= 8) {
            std::string roadId = tokens[0];
            std::string from = tokens[1];
            std::string to = tokens[2];
            
            // 判断是否有道路类型字段
            int offset = 0;
            if (tokens.size() == 9) {
                offset = 1; // 有道路类型字段
            }
            
            std::string direction = tokens[3 + offset];
            int length = 0, speedLimit = 0, lanes = 0, vehicles = 0;
            
            try {
                length = std::stoi(tokens[4 + offset]);
                speedLimit = std::stoi(tokens[5 + offset]);
                lanes = std::stoi(tokens[6 + offset]);
                vehicles = std::stoi(tokens[7 + offset]);
            } catch (const std::exception& e) {
                std::cerr << "解析数值错误: " << line << std::endl;
                continue;
            }
            
            // 计算权重
            double weight = TrafficCalculator::calculateWeight(vehicles, lanes, length, speedLimit);
            
            // 根据道路方向添加边
            if (direction == "双向") {
                // 双向道路：添加两条边
                graph.addEdge(from, to, weight, roadId, length, speedLimit, lanes, vehicles);
                graph.addEdge(to, from, weight, roadId, length, speedLimit, lanes, vehicles);
            } else if (direction == "单向") {
                // 单向道路：只添加一条边
                graph.addEdge(from, to, weight, roadId, length, speedLimit, lanes, vehicles);
            }
        }
    }
    
    file.close();
}

std::string FileParser::trim(const std::string& str) {
    size_t first = str.find_first_not_of(" \t\n\r");
    if (first == std::string::npos) {
        return "";
    }
    size_t last = str.find_last_not_of(" \t\n\r");
    return str.substr(first, (last - first + 1));
}

