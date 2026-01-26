// CPU测试平台 - 简化版（最大兼容性）
`timescale 1ns/1ps

module CPU_tb_simple;

    // 时钟和复位信号
    reg clk;
    reg rst;
    
    // 实例化CPU
    CPU cpu(
        .clk(clk),
        .rst(rst)
    );
    
    // 时钟生成：周期为20ns（50MHz）
    always #10 clk = ~clk;
    
    // 测试激励
    initial begin
        // 初始化
        clk = 0;
        rst = 1;
        
        // 等待25ns后解除复位
        #25;
        rst = 0;
        
        // 运行300ns（足够执行所有指令）
        #300;
        
        // 显示最终结果
        $display("========================================");
        $display("Final Register Values:");
        $display("========================================");
        $display("X1 = 0x%h (expected: 0x00000008)", cpu.RF.registers[1]);
        $display("X2 = 0x%h (expected: 0x00000004)", cpu.RF.registers[2]);
        $display("X3 = 0x%h (expected: 0x0000000C)", cpu.RF.registers[3]);
        $display("X4 = 0x%h (expected: 0x00000004)", cpu.RF.registers[4]);
        $display("X5 = 0x%h (expected: 0x0000000C)", cpu.RF.registers[5]);
        $display("X6 = 0x%h (expected: 0x0000000D)", cpu.RF.registers[6]);
        $display("X7 = 0x%h (expected: 0x00000000)", cpu.RF.registers[7]);
        $display("X8 = 0x%h (expected: 0x00000001)", cpu.RF.registers[8]);
        $display("========================================");
        
        // 检查结果
        if (cpu.RF.registers[1] == 32'h00000008 &&
            cpu.RF.registers[2] == 32'h00000004 &&
            cpu.RF.registers[3] == 32'h0000000C &&
            cpu.RF.registers[4] == 32'h00000004 &&
            cpu.RF.registers[5] == 32'h0000000C &&
            cpu.RF.registers[6] == 32'h0000000D &&
            cpu.RF.registers[7] == 32'h00000000 &&
            cpu.RF.registers[8] == 32'h00000001) begin
            $display("*** TEST PASSED ***");
        end else begin
            $display("*** TEST FAILED ***");
        end
        
        $finish;
    end

endmodule

