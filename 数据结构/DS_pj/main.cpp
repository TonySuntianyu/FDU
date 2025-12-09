#include <iostream>
#include <vector>
#include <string>
#include <algorithm>
#include <sstream>
#include <fstream>
#ifdef _WIN32
#include <windows.h>
#include <io.h>
#include <fcntl.h>
#else
#include <dirent.h>
#endif
#include "Graph.h"
#include "FileParser.h"
#include "Dijkstra.h"

// 获取目录中所有地图文件（按时间排序）
std::vector<std::string> getMapFiles(const std::string& directory) {
    std::vector<std::string> mapFiles;
    
#ifdef _WIN32
    // Windows实现
    std::string searchPath = directory + "\\*";
    WIN32_FIND_DATAA findData;
    HANDLE hFind = FindFirstFileA(searchPath.c_str(), &findData);
    
    if (hFind != INVALID_HANDLE_VALUE) {
        do {
            if (!(findData.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)) {
                std::string filename = findData.cFileName;
                if (filename.find("map_") == 0 && filename.find(".csv") != std::string::npos) {
                    mapFiles.push_back(directory + "\\" + filename);
                }
            }
        } while (FindNextFileA(hFind, &findData));
        FindClose(hFind);
    }
#else
    // Linux/macOS实现
    DIR* dir = opendir(directory.c_str());
    if (dir != nullptr) {
        struct dirent* entry;
        while ((entry = readdir(dir)) != nullptr) {
            std::string filename = entry->d_name;
            if (filename.find("map_") == 0 && filename.find(".csv") != std::string::npos) {
                mapFiles.push_back(directory + "/" + filename);
            }
        }
        closedir(dir);
    }
#endif
    
    // 按文件名排序（时间顺序）
    std::sort(mapFiles.begin(), mapFiles.end());
    
    return mapFiles;
}

int main(int argc, char* argv[]) {
#ifdef _WIN32
    // 设置控制台编码为UTF-8，解决中文乱码问题
    SetConsoleOutputCP(65001);
    SetConsoleCP(65001);
#endif
    
    // 检查命令行参数
    if (argc < 2) {
        std::cerr << "使用方法: " << argv[0] << " <测试用例目录>" << std::endl;
        std::cerr << "例如: " << argv[0] << " Test_Cases/eazy_test_cases/shanghai_test_cases/case1_simple" << std::endl;
        return 1;
    }
    
    std::string testCaseDir = argv[1];
    
    // 解析需求文件
    std::string demandFile = testCaseDir + "/demand.txt";
    Demand demand = FileParser::parseDemand(demandFile);
    
    if (demand.start.empty() || demand.end.empty()) {
        std::cerr << "无法解析需求文件或起点/终点为空" << std::endl;
        return 1;
    }
    
    std::cout << "起点: " << demand.start << std::endl;
    std::cout << "终点: " << demand.end << std::endl;
    std::cout << std::endl;
    
    // 获取所有地图文件
    std::vector<std::string> mapFiles = getMapFiles(testCaseDir);
    
    if (mapFiles.empty()) {
        std::cerr << "未找到地图文件" << std::endl;
        return 1;
    }
    
    // 处理每个时间点的地图
    for (const auto& mapFile : mapFiles) {
        // 创建新图
        Graph graph;
        
        // 解析地图文件
        FileParser::parseMapFile(mapFile, graph);
        
        // 使用Dijkstra算法计算最短路径
        PathResult result = Dijkstra::findShortestPath(graph, demand.start, demand.end);
        
        // 输出结果
        if (result.found) {
            std::string pathStr = Dijkstra::pathToString(result.path);
            std::cout << pathStr << std::endl;
        } else {
            std::cerr << "无法找到从 " << demand.start << " 到 " << demand.end << " 的路径" << std::endl;
        }
    }
    
    return 0;
}

