@echo off
chcp 65001 >nul 2>&1
REM 运行路径规划系统

if "%1"=="" (
    echo 使用方法: run.bat ^<测试用例目录^>
    echo.
    echo 示例:
    echo   run.bat Test_Cases\eazy_test_cases\shanghai_test_cases\case1_simple
    echo   run.bat Test_Cases\eazy_test_cases\shanghai_test_cases\case2_medium
    echo   run.bat Test_Cases\large_scale_cases\large_scale_case_example
    exit /b 1
)

echo 正在运行路径规划系统...
echo 测试用例: %1
echo.
path_planner.exe %1
echo.
pause

