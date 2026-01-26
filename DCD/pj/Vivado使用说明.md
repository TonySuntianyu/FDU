# Vivado仿真使用说明

## ⚠️ 编译错误已修复

已修复的问题：
1. ✅ 将 `integer` 变量声明移到 `initial` 块外部（ProgramMemory.v）
2. ✅ 创建了Vivado兼容的测试平台（CPU_tb_vivado.v）

---

## 📋 在Vivado中使用本项目的步骤

### 方法1：使用Vivado GUI（推荐）

#### 第1步：创建新项目
1. 打开Vivado
2. File → Project → New
3. 选择RTL Project
4. 不勾选"Do not specify sources at this time"
5. 点击Next

#### 第2步：添加设计源文件
1. 点击 "Add Files"
2. 添加以下文件（作为Design Sources）：
   ```
   ✓ ALU.v
   ✓ RegisterFile.v
   ✓ ProgramMemory.v
   ✓ DataMemory.v
   ✓ ControlUnit.v
   ✓ ImmGen.v
   ✓ CPU.v
   ```
3. 点击Next

#### 第3步：添加仿真源文件
1. 在添加源文件界面，切换到 "Add or create simulation sources"
2. 点击 "Add Files"
3. 添加：
   ```
   ✓ CPU_tb_vivado.v  （Vivado专用版本）
   ```
   **注意**：使用 `CPU_tb_vivado.v` 而不是 `CPU_tb.v`
4. 点击Next

#### 第4步：选择FPGA芯片（可选）
- 如果只是仿真，可以选择任意芯片
- 推荐：Artix-7系列或Zynq-7000系列
- 点击Next，然后Finish

#### 第5步：运行仿真
1. 在Flow Navigator中，找到 "SIMULATION"
2. 点击 "Run Behavioral Simulation"
3. 等待编译和仿真启动

#### 第6步：查看波形
1. 仿真启动后，会自动打开波形窗口
2. 在Scope窗口选择 `CPU_tb` → `cpu`
3. 从Objects窗口添加信号到波形：

**必须添加的信号：**
```
✓ clk
✓ rst  
✓ PC
✓ Instruction
✓ ALU_Result
✓ RegWrite
✓ MemWrite
✓ MemRead
```

**展开RF添加寄存器：**
```
✓ RF → registers[1] 到 registers[8]
```

**展开DM添加存储器：**
```
✓ DM → memory[4]
✓ DM → memory[12]
```

4. 右键信号 → Radix → Hexadecimal（设置为16进制）
5. 点击工具栏的 ▶ 运行仿真到结束

---

### 方法2：使用TCL命令（高级）

在Vivado的Tcl Console中执行：

```tcl
# 创建项目
create_project cpu_project ./vivado_project -part xc7a35tcpg236-1

# 添加设计源文件
add_files {ALU.v RegisterFile.v ProgramMemory.v DataMemory.v ControlUnit.v ImmGen.v CPU.v}

# 添加仿真文件
add_files -fileset sim_1 {CPU_tb_vivado.v}

# 更新编译顺序
update_compile_order -fileset sources_1
update_compile_order -fileset sim_1

# 运行仿真
launch_simulation
```

---

## 🔍 查看仿真结果

### 在Tcl Console中查看输出

运行仿真后，在Tcl Console窗口可以看到类似如下输出：

```
========================================
RISC-V Single Cycle CPU Simulation Start
========================================
Time    PC      Instruction     Operation
----------------------------------------
25      00      00800093        ADDI X1, X0, 8
45      04      0040a103        LW   X2, 4(X1)
65      08      002081b3        ADD X3, X1, X2
85      0c      40118233        SUB X4, X3, X1
...

========================================
Final Register Values:
========================================
X0  = 0x00000000 (should be 0)
X1  = 0x00000008 (should be 0x00000008)
X2  = 0x00000004 (should be 0x00000004)
...
```

### 波形图观察要点

#### 定位到特定指令
使用时间标尺，每条指令20ns：

| 指令 | PC | 时间范围 |
|------|-----|---------|
| addi | 0x00 | 25-45ns |
| lw   | 0x04 | 45-65ns |
| add  | 0x08 | 65-85ns |
| sub  | 0x0C | 85-105ns |
| or   | 0x10 | 105-125ns |
| ori  | 0x14 | 125-145ns |
| sw   | 0x18 | 145-165ns |
| slt  | 0x1C | 165-185ns |
| slti | 0x20 | 185-205ns |
| beq  | 0x24 | 205-225ns |

---

## 📸 导出波形图

### 方法1：导出为图片
1. File → Export → Export Waveform
2. 选择导出范围和格式（PNG推荐）
3. 设置分辨率（建议1920x1080）

### 方法2：截图工具
1. 缩放到合适的时间范围
2. 使用Windows截图工具（Win+Shift+S）
3. 或使用Snipaste等第三方工具

### 波形图设置技巧

**调整显示格式：**
- 右键信号 → Radix → Hexadecimal（十六进制）
- 右键信号 → Radix → Unsigned Decimal（无符号十进制）

**添加标记：**
- 右键时间轴 → Add Marker
- 在关键时间点添加标记便于截图

**调整波形颜色：**
- 右键信号 → Wave Color
- 不同类型信号用不同颜色区分

**分组显示：**
- 选中多个信号 → 右键 → New Group
- 创建"寄存器"、"控制信号"等分组

---

## ⚠️ 常见问题解决

### Q1: 编译错误 - integer变量
**问题**：`[USF-XSim-62] 'compile' step failed`

**解决**：确保使用了修复后的代码，integer变量已移到initial块外部

### Q2: 找不到CPU_tb_vivado模块
**问题**：Simulation sources中没有testbench

**解决**：
1. Sources窗口 → Simulation Sources
2. 右键 → Add or Create Simulation Sources
3. 添加 `CPU_tb_vivado.v`

### Q3: 波形显示X或Z态
**问题**：信号未初始化

**解决**：
1. 检查复位信号是否正常工作
2. 确保rst在开始时为高电平
3. 检查所有存储器是否正确初始化

### Q4: 仿真运行很慢
**问题**：仿真时间过长

**解决**：
1. 减少仿真时间：修改testbench中的repeat次数
2. 使用增量编译
3. 关闭不必要的调试功能

### Q5: 看不到Tcl Console输出
**问题**：$display输出不可见

**解决**：
1. Window → Tcl Console（确保窗口打开）
2. 检查仿真设置：Simulation → Simulation Settings
3. 确保"Enable Tcl output"选项已勾选

---

## 🎯 仿真检查清单

完成仿真后，请检查：

- [ ] 所有文件编译无错误
- [ ] 仿真成功启动
- [ ] 波形窗口正常显示
- [ ] PC值从0x00开始顺序递增
- [ ] X1-X8寄存器值符合预期
- [ ] DM[4]和DM[12]数据正确
- [ ] beq指令正确跳转（PC: 0x24→0x1C）
- [ ] Tcl Console有完整输出
- [ ] 已导出所有需要的波形图

---

## 📊 预期仿真结果

### 寄存器最终值
```
X0 = 0x00000000  ✓
X1 = 0x00000008  ✓
X2 = 0x00000004  ✓
X3 = 0x0000000C  ✓
X4 = 0x00000004  ✓
X5 = 0x0000000C  ✓
X6 = 0x0000000D  ✓
X7 = 0x00000000  ✓
X8 = 0x00000001  ✓
```

### 存储器关键位置
```
DM[0x04] = 0x0000000D  ✓ (sw指令写入)
DM[0x0C] = 0x00000004  ✓ (初始数据)
```

---

## 💡 优化建议

### 1. 添加更多观察信号
在波形中添加：
- `ALU_A`, `ALU_B` - 查看ALU输入
- `Immediate` - 查看立即数生成
- `ALUOp` - 查看ALU操作类型
- `PCSrc` - 查看PC跳转控制

### 2. 使用层次化显示
创建信号分组：
```
📁 控制信号
  ├─ RegWrite
  ├─ MemWrite
  ├─ MemRead
  └─ ALUSrc

📁 寄存器
  ├─ X1 (registers[1])
  ├─ X2 (registers[2])
  └─ ...

📁 存储器
  ├─ DM[4]
  └─ DM[12]
```

### 3. 保存波形配置
1. File → Simulation Waveform → Save Configuration As
2. 保存为 `.wcfg` 文件
3. 下次仿真自动加载配置

---

## 📞 技术支持

如果遇到问题：
1. 查看 `README.md` 了解项目整体架构
2. 查看 `波形图获取指南.md` 了解波形分析
3. 检查Vivado日志文件中的详细错误信息

---

**祝您仿真顺利！** 🎉

