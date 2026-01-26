# GTKWave 自动加载信号的TCL脚本
# 使用方法: gtkwave cpu_wave.vcd -S GTKWave使用脚本.tcl

# 设置时间单位
gtkwave::setZoomRangeTimes 0 400ns

# 添加时钟和复位信号
set top CPU_tb.cpu
gtkwave::addSignalsFromList "$top.clk"
gtkwave::addSignalsFromList "$top.rst"

# 添加PC和指令
gtkwave::addSignalsFromList "$top.PC"
gtkwave::addSignalsFromList "$top.Instruction"

# 添加ALU相关信号
gtkwave::addSignalsFromList "$top.ALU_A"
gtkwave::addSignalsFromList "$top.ALU_B"
gtkwave::addSignalsFromList "$top.ALU_Result"
gtkwave::addSignalsFromList "$top.Zero"

# 添加控制信号
gtkwave::addSignalsFromList "$top.RegWrite"
gtkwave::addSignalsFromList "$top.MemWrite"
gtkwave::addSignalsFromList "$top.MemRead"
gtkwave::addSignalsFromList "$top.MemtoReg"
gtkwave::addSignalsFromList "$top.ALUSrc"
gtkwave::addSignalsFromList "$top.Branch"

# 添加寄存器（X1-X8）
for {set i 1} {$i <= 8} {incr i} {
    gtkwave::addSignalsFromList "$top.RF.registers\[$i\]"
}

# 添加立即数
gtkwave::addSignalsFromList "$top.Immediate"

# 设置显示格式为十六进制
gtkwave::highlightSignalsFromList "$top.PC"
gtkwave::setCurrentTranslateProc hex
gtkwave::highlightSignalsFromList "$top.Instruction"
gtkwave::setCurrentTranslateProc hex
gtkwave::highlightSignalsFromList "$top.ALU_Result"
gtkwave::setCurrentTranslateProc hex

# 缩放到合适的范围
gtkwave::setZoomRangeTimes 20ns 250ns

# 添加时间标记
gtkwave::setMarker 25ns
gtkwave::setMarker 45ns
gtkwave::setMarker 65ns
gtkwave::setMarker 85ns
gtkwave::setMarker 105ns
gtkwave::setMarker 125ns
gtkwave::setMarker 145ns
gtkwave::setMarker 165ns
gtkwave::setMarker 185ns
gtkwave::setMarker 205ns

puts "GTKWave configuration loaded successfully!"
puts "All signals have been added to the waveform viewer."
puts "Use Ctrl+Mouse Wheel to zoom in/out."

