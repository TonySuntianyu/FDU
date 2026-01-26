#!/bin/bash
# Linux/Mac脚本 - 使用iverilog运行仿真

echo "========================================"
echo "RISC-V Single Cycle CPU Simulation"
echo "========================================"

# 检查iverilog是否安装
if ! command -v iverilog &> /dev/null; then
    echo "Error: iverilog not found!"
    echo "Please install with: sudo apt-get install iverilog (Ubuntu/Debian)"
    echo "                 or: brew install icarus-verilog (macOS)"
    exit 1
fi

echo ""
echo "[1/3] Compiling Verilog files..."
iverilog -o cpu_sim.vvp ALU.v RegisterFile.v ProgramMemory.v DataMemory.v ControlUnit.v ImmGen.v CPU.v CPU_tb.v

if [ $? -ne 0 ]; then
    echo ""
    echo "Error: Compilation failed!"
    exit 1
fi

echo "[2/3] Running simulation..."
vvp cpu_sim.vvp

if [ $? -ne 0 ]; then
    echo ""
    echo "Error: Simulation failed!"
    exit 1
fi

echo ""
echo "[3/3] Opening waveform viewer..."
if command -v gtkwave &> /dev/null; then
    gtkwave cpu_wave.vcd &
else
    echo "GTKWave not found. You can manually open cpu_wave.vcd"
    echo "Install with: sudo apt-get install gtkwave (Ubuntu/Debian)"
    echo "          or: brew install --cask gtkwave (macOS)"
fi

echo ""
echo "========================================"
echo "Simulation Complete!"
echo "Waveform saved to: cpu_wave.vcd"
echo "========================================"

