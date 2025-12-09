#ifndef FILE_PARSER_H
#define FILE_PARSER_H

#include <string>
#include "Graph.h"

// 需求信息结构
struct Demand {
    std::string start;  // 起点
    std::string end;    // 终点
};

// 文件解析器
class FileParser {
public:
    // 解析需求文件
    static Demand parseDemand(const std::string& filename);
    
    // 解析地图文件并构建图
    static void parseMapFile(const std::string& filename, Graph& graph);
    
    // 从字符串中提取地点名称（去除可能的空格）
    static std::string trim(const std::string& str);
};

#endif // FILE_PARSER_H

