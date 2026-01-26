# 文本编辑器启动脚本
# 使用方法: .\start.ps1

Write-Host "正在启动文本编辑器..." -ForegroundColor Green

# 切换到脚本所在目录
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

# 运行程序
mvn exec:java

