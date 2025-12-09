@echo off
chcp 65001 >nul 2>&1
REM Windows 编译脚本

echo 正在编译路径规划系统...

REM 检查是否有g++
where g++ >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo 错误: 未找到g++编译器
    echo 请安装MinGW或使用Visual Studio
    pause
    exit /b 1
)

REM 编译
g++ -std=c++17 -O2 -o path_planner.exe Graph.cpp Dijkstra.cpp FileParser.cpp TrafficCalculator.cpp main.cpp

if %ERRORLEVEL% EQU 0 (
    echo 编译成功！可执行文件: path_planner.exe
    echo.
    echo 使用方法:
    echo   path_planner.exe ^<测试用例目录^>
    echo.
    echo 示例:
    echo   path_planner.exe Test_Cases\eazy_test_cases\shanghai_test_cases\case1_simple
) else (
    echo 编译失败！
    pause
    exit /b 1
)

pause

