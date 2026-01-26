// CPU测试平台
`timescale 1ns/1ps

module CPU_tb;

    // 时钟和复位信号
    reg clk;
    reg rst;
    
    // 实例化CPU
    CPU cpu(
        .clk(clk),
        .rst(rst)
    );
    
    // 时钟生成：周期为20ns（50MHz）
    initial begin
        clk = 0;
        forever #10 clk = ~clk;
    end
    
    // 测试激励
    initial begin
        // 初始化波形文件
        $dumpfile("cpu_wave.vcd");
        $dumpvars(0, CPU_tb);
        
        // 显示所有寄存器的值
        $dumpvars(0, cpu.RF.registers[0]);
        $dumpvars(0, cpu.RF.registers[1]);
        $dumpvars(0, cpu.RF.registers[2]);
        $dumpvars(0, cpu.RF.registers[3]);
        $dumpvars(0, cpu.RF.registers[4]);
        $dumpvars(0, cpu.RF.registers[5]);
        $dumpvars(0, cpu.RF.registers[6]);
        $dumpvars(0, cpu.RF.registers[7]);
        $dumpvars(0, cpu.RF.registers[8]);
        
        // 显示数据存储器的部分内容
        $dumpvars(0, cpu.DM.memory[0]);
        $dumpvars(0, cpu.DM.memory[4]);
        $dumpvars(0, cpu.DM.memory[8]);
        $dumpvars(0, cpu.DM.memory[12]);
        
        // 复位CPU
        rst = 1;
        #25;
        rst = 0;
        
        // 打印测试信息
        $display("========================================");
        $display("RISC-V Single Cycle CPU Simulation Start");
        $display("========================================");
        $display("Time\tPC\tInstruction\tOperation");
        $display("----------------------------------------");
        
        // 运行足够的时钟周期来执行所有指令
        // 每条指令需要1个时钟周期，共10条指令（包括beq的循环）
        repeat(15) begin
            @(posedge clk);
            #1;  // 等待信号稳定
            display_instruction_info();
        end
        
        // 显示最终寄存器状态
        #20;
        $display("\n========================================");
        $display("Final Register Values:");
        $display("========================================");
        $display("X0  = 0x%08h (should be 0)", cpu.RF.registers[0]);
        $display("X1  = 0x%08h (should be 0x00000008)", cpu.RF.registers[1]);
        $display("X2  = 0x%08h (should be 0x00000004)", cpu.RF.registers[2]);
        $display("X3  = 0x%08h (should be 0x0000000C)", cpu.RF.registers[3]);
        $display("X4  = 0x%08h (should be 0x00000004)", cpu.RF.registers[4]);
        $display("X5  = 0x%08h (should be 0x0000000C)", cpu.RF.registers[5]);
        $display("X6  = 0x%08h (should be 0x0000000D)", cpu.RF.registers[6]);
        $display("X7  = 0x%08h (should be 0x00000000)", cpu.RF.registers[7]);
        $display("X8  = 0x%08h (should be 0x00000001)", cpu.RF.registers[8]);
        
        $display("\n========================================");
        $display("Data Memory Values:");
        $display("========================================");
        $display("DM[0x04] = 0x%08h (should be 0x0000000D)", 
                 {cpu.DM.memory[7], cpu.DM.memory[6], cpu.DM.memory[5], cpu.DM.memory[4]});
        $display("DM[0x0C] = 0x%08h (should be 0x00000004)", 
                 {cpu.DM.memory[15], cpu.DM.memory[14], cpu.DM.memory[13], cpu.DM.memory[12]});
        
        $display("\n========================================");
        $display("Simulation Complete!");
        $display("========================================");
        
        $finish;
    end
    
    // 显示每条指令的执行信息
    task display_instruction_info;
        reg [31:0] inst;
        reg [6:0] opcode;
        begin
            inst = cpu.Instruction;
            opcode = inst[6:0];
            
            $write("%0t\t%h\t%h\t", $time, cpu.PC, inst);
            
            case(opcode)
                7'b0110011: begin  // R-type
                    case(inst[14:12])
                        3'b000: begin
                            if (inst[31:25] == 7'b0000000)
                                $display("ADD X%0d, X%0d, X%0d", inst[11:7], inst[19:15], inst[24:20]);
                            else
                                $display("SUB X%0d, X%0d, X%0d", inst[11:7], inst[19:15], inst[24:20]);
                        end
                        3'b110: $display("OR  X%0d, X%0d, X%0d", inst[11:7], inst[19:15], inst[24:20]);
                        3'b010: $display("SLT X%0d, X%0d, X%0d", inst[11:7], inst[19:15], inst[24:20]);
                        default: $display("Unknown R-type");
                    endcase
                end
                7'b0010011: begin  // I-type arithmetic
                    case(inst[14:12])
                        3'b000: $display("ADDI X%0d, X%0d, %0d", inst[11:7], inst[19:15], $signed(inst[31:20]));
                        3'b110: $display("ORI  X%0d, X%0d, %0d", inst[11:7], inst[19:15], $signed(inst[31:20]));
                        3'b010: $display("SLTI X%0d, X%0d, %0d", inst[11:7], inst[19:15], $signed(inst[31:20]));
                        default: $display("Unknown I-type");
                    endcase
                end
                7'b0000011: $display("LW   X%0d, %0d(X%0d)", inst[11:7], $signed(inst[31:20]), inst[19:15]);
                7'b0100011: $display("SW   X%0d, %0d(X%0d)", inst[24:20], $signed({inst[31:25], inst[11:7]}), inst[19:15]);
                7'b1100011: $display("BEQ  X%0d, X%0d, %0d", inst[19:15], inst[24:20], $signed({inst[31], inst[7], inst[30:25], inst[11:8], 1'b0}));
                default: $display("NOP or Unknown");
            endcase
        end
    endtask
    
    // 监控关键信号变化
    initial begin
        $monitor("Time=%0t | PC=%h | Inst=%h | X1=%h | X2=%h | X3=%h | ALU_Result=%h", 
                 $time, cpu.PC, cpu.Instruction, 
                 cpu.RF.registers[1], cpu.RF.registers[2], cpu.RF.registers[3],
                 cpu.ALU_Result);
    end

endmodule

