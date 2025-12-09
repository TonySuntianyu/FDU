#!/bin/bash
# Linux/macOS 编译脚本

echo "正在编译路径规划系统..."

# 检查编译器
if command -v g++ &> /dev/null; then
    COMPILER="g++"
elif command -v clang++ &> /dev/null; then
    COMPILER="clang++"
else
    echo "错误: 未找到C++编译器 (g++ 或 clang++)"
    exit 1
fi

# 编译
$COMPILER -std=c++17 -O2 -o path_planner Graph.cpp Dijkstra.cpp FileParser.cpp TrafficCalculator.cpp main.cpp

if [ $? -eq 0 ]; then
    echo "编译成功！可执行文件: ./path_planner"
    echo ""
    echo "使用方法:"
    echo "  ./path_planner <测试用例目录>"
    echo ""
    echo "示例:"
    echo "  ./path_planner Test_Cases/eazy_test_cases/shanghai_test_cases/case1_simple"
else
    echo "编译失败！"
    exit 1
fi

