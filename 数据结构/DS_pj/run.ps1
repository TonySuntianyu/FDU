# PowerShell 运行脚本

param(
    [Parameter(Mandatory=$false)]
    [string]$TestCase = ""
)

if ($TestCase -eq "") {
    Write-Host "使用方法: .\run.ps1 -TestCase <测试用例目录>" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "示例:" -ForegroundColor Cyan
    Write-Host "  .\run.ps1 -TestCase 'Test_Cases\eazy_test_cases\shanghai_test_cases\case1_simple'"
    Write-Host "  .\run.ps1 -TestCase 'Test_Cases\eazy_test_cases\shanghai_test_cases\case2_medium'"
    Write-Host "  .\run.ps1 -TestCase 'Test_Cases\large_scale_cases\large_scale_case_example'"
    exit 1
}

Write-Host "正在运行路径规划系统..." -ForegroundColor Green
Write-Host "测试用例: $TestCase" -ForegroundColor Cyan
Write-Host ""

& .\path_planner.exe $TestCase

Write-Host ""
Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

