@echo off
chcp 65001 >nul 2>&1
REM 测试所有测试用例

echo ========================================
echo 路径规划系统 - 完整测试
echo ========================================
echo.

echo [测试1] case1_simple (1个时间点)
echo ----------------------------------------
path_planner.exe "Test_Cases\eazy_test_cases\shanghai_test_cases\case1_simple"
echo.
echo.

echo [测试2] case2_medium (2个时间点)
echo ----------------------------------------
path_planner.exe "Test_Cases\eazy_test_cases\shanghai_test_cases\case2_medium"
echo.
echo.

echo [测试3] case3_complex (3个时间点)
echo ----------------------------------------
path_planner.exe "Test_Cases\eazy_test_cases\shanghai_test_cases\case3_complex"
echo.
echo.

echo [测试4] large_scale_case_example (6个时间点)
echo ----------------------------------------
path_planner.exe "Test_Cases\large_scale_cases\large_scale_case_example"
echo.
echo.

echo ========================================
echo 所有测试完成！
echo ========================================
pause

