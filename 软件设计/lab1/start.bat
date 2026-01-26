@echo off
chcp 65001 >nul
echo 正在启动文本编辑器...
cd /d "%~dp0"
mvn exec:java
pause

