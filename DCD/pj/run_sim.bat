@echo off
REM Windows批处理脚本 - 使用iverilog运行仿真

echo ========================================
echo RISC-V Single Cycle CPU Simulation
echo ========================================

REM 检查iverilog是否安装
where iverilog >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Error: iverilog not found!
    echo Please install iverilog from: http://bleyer.org/icarus/
    pause
    exit /b 1
)

echo.
echo [1/3] Compiling Verilog files...
iverilog -o cpu_sim.vvp ALU.v RegisterFile.v ProgramMemory.v DataMemory.v ControlUnit.v ImmGen.v CPU.v CPU_tb.v

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Error: Compilation failed!
    pause
    exit /b 1
)

echo [2/3] Running simulation...
vvp cpu_sim.vvp

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Error: Simulation failed!
    pause
    exit /b 1
)

echo.
echo [3/3] Opening waveform viewer...
if exist gtkwave.exe (
    gtkwave cpu_wave.vcd
) else (
    echo GTKWave not found. You can manually open cpu_wave.vcd
    echo Download GTKWave from: http://gtkwave.sourceforge.net/
)

echo.
echo ========================================
echo Simulation Complete!
echo Waveform saved to: cpu_wave.vcd
echo ========================================
pause

