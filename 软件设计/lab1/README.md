# 文本编辑器

基于命令行的文本编辑器，支持多文件编辑、撤销/重做、日志记录等功能。

## 功能特性

- ✅ 多文件同时编辑
- ✅ 基本文本编辑操作（追加、插入、删除、替换）
- ✅ 撤销/重做功能
- ✅ 工作区状态持久化
- ✅ 命令执行日志记录
- ✅ 自动日志启用（检测 # log 标记）

## 快速开始

### 编译和运行

**在项目根目录（包含 pom.xml 的目录）执行：**

```powershell
# 方式1：直接运行（推荐，已在 pom.xml 中配置主类）
mvn exec:java

# 方式2：使用启动脚本（Windows）
.\start.bat

# 方式3：先编译后直接运行
mvn compile
java -cp target/classes editor.Main

# 方式4：打包后运行
mvn package
java -jar target/text-editor-1.0.0.jar
```

**注意**：在 PowerShell 中，如果直接使用 `-Dexec.mainClass` 参数可能遇到解析问题，建议使用方式1（直接运行）或方式3（先编译）。

### 运行测试

```bash
mvn test
```

## 命令列表

### 工作区命令
- `load <file>` - 加载文件
- `save [file|all]` - 保存文件
- `init <file> [with-log]` - 创建新缓冲区
- `close [file]` - 关闭文件
- `edit <file>` - 切换活动文件
- `editor-list` - 显示文件列表
- `dir-tree [path]` - 显示目录树
- `undo` - 撤销
- `redo` - 重做
- `exit` - 退出程序

### 文本编辑命令
- `append "text"` - 追加文本
- `insert line:col "text"` - 插入文本
- `delete line:col <len>` - 删除字符
- `replace line:col <len> "text"` - 替换文本
- `show [start:end]` - 显示内容

### 日志命令
- `log-on [file]` - 启用日志
- `log-off [file]` - 关闭日志
- `log-show [file]` - 显示日志

## 项目结构

```
lab1/
├── src/
│   ├── main/java/editor/
│   │   ├── Main.java                 # 主程序
│   │   ├── command/                  # 命令解析和执行
│   │   ├── editor/                   # 编辑器模块
│   │   ├── workspace/                # 工作区模块
│   │   ├── logging/                  # 日志模块
│   │   └── event/                    # 事件模块
│   └── test/java/editor/             # 测试代码
├── docs/
│   └── 架构设计文档.md                # 架构设计文档
├── pom.xml                           # Maven配置
└── README.md                         # 本文件
```

## 设计模式

- **命令模式**: 实现撤销/重做功能
- **观察者模式**: 实现命令执行事件通知
- **备忘录模式**: 实现工作区状态持久化

## 技术栈

- Java 11+
- Maven 3.6+
- JUnit 5

## 许可证


本项目为课程作业，仅供学习使用。
