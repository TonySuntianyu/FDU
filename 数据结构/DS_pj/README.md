# 路径规划系统

## 项目简介

本项目实现了一个基于Dijkstra算法的路径规划系统，支持动态地图更新和多时间点的路径规划。系统能够根据道路的实时交通状况（车辆数、车道数、道路长度、限速等）计算最优路径。

## 开发环境

- **编程语言**: C++17
- **编译器**: g++ (支持C++17标准)
- **开发平台**: Windows/Linux/macOS
- **依赖**: 标准C++库（无外部依赖）

## 项目结构

```
.
├── Graph.h              # 图类头文件
├── Graph.cpp            # 图类实现
├── Dijkstra.h           # Dijkstra算法头文件
├── Dijkstra.cpp         # Dijkstra算法实现
├── FileParser.h         # 文件解析器头文件
├── FileParser.cpp       # 文件解析器实现
├── TrafficCalculator.h  # 堵车系数计算器头文件
├── TrafficCalculator.cpp # 堵车系数计算器实现
├── main.cpp             # 主程序
└── README.md            # 说明文档
```

## 编译方法

### 方法一：使用提供的编译脚本（推荐）

**Linux/macOS:**
```bash
chmod +x compile.sh
./compile.sh
```

**Windows:**
```cmd
compile.bat
```

### 方法二：使用Makefile

```bash
make
```

### 方法三：手动编译

**Linux/macOS:**
```bash
g++ -std=c++17 -O2 -o path_planner Graph.cpp Dijkstra.cpp FileParser.cpp TrafficCalculator.cpp main.cpp
```

**Windows (MinGW):**
```cmd
g++ -std=c++17 -O2 -o path_planner.exe Graph.cpp Dijkstra.cpp FileParser.cpp TrafficCalculator.cpp main.cpp
```

**Windows (Visual Studio):**
```cmd
cl /EHsc /std:c++17 Graph.cpp Dijkstra.cpp FileParser.cpp TrafficCalculator.cpp main.cpp /Fe:path_planner.exe
```

**注意：**
- 需要支持C++17标准的编译器
- Windows系统会自动使用Windows API进行目录遍历
- Linux/macOS系统使用dirent.h进行目录遍历

## 运行方法

```bash
./path_planner <测试用例目录>
```

例如：
```bash
./path_planner Test_Cases/eazy_test_cases/shanghai_test_cases/case1_simple
```

## 核心功能

1. **图数据结构**: 使用邻接表实现的有向图，支持动态添加顶点和边
2. **堵车系数计算**: 根据车辆数、车道数、道路长度、限速计算道路权重
3. **最短路径算法**: 使用Dijkstra算法计算最优路径
4. **动态更新**: 支持多时间点的地图文件，自动处理路况变化
5. **文件解析**: 自动解析CSV格式的地图文件和TXT格式的需求文件

## 堵车系数计算公式

系统使用以下公式计算道路权重：

```
基础时间 = 道路长度(千米) / 限速(km/h)
车辆密度 = 现有车辆数 / 车道数
归一化密度 = 车辆密度 / 道路长度(千米)
权重 = 基础时间 × (1 + 归一化密度 × 调节因子)
```

这个公式考虑了：
- 道路长度和限速（基础通行时间）
- 车辆密度（拥堵程度）
- 道路容量（车道数）

## 输出格式

每当地图信息更新，输出一行最优路径，格式如下：

```
起点-->中间点1-->中间点2-->...-->终点
```

例如：
```
复旦大学-->幸福小镇-->绿地足球场-->红星广场-->外滩-->海上购物城-->东方明珠-->陆家嘴
```

## 注意事项

1. 确保测试用例目录中包含`demand.txt`文件和至少一个`map_*.csv`文件
2. 地图文件会按文件名（时间）顺序处理
3. 如果起点和终点之间不存在路径，程序会输出错误信息

