@echo off
echo 正在启动多种棋类游戏...

REM 编译项目
mvn clean compile

REM 运行项目 (直接使用Maven的classpath)
mvn exec:java

echo 游戏已退出
pause 