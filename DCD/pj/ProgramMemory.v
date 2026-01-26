// 程序存储器模块（PM）
// 256字节，按字节寻址（0-255）
module ProgramMemory(
    input [7:0] Address,        // 地址（字节地址）
    output [31:0] Instruction   // 32位指令输出
);

    // 256字节存储空间
    reg [7:0] memory [0:255];
    
    // 循环变量
    integer i;
    
    // 初始化程序存储器
    initial begin
        // 初始化所有内存为0
        for (i = 0; i < 256; i = i + 1) begin
            memory[i] = 8'h00;
        end
        
        // 根据测试指令文档初始化程序存储器（小端模式）
        // 指令1: addi X1, X0, 0x8 (0x00800093)
        memory[0] = 8'h93;
        memory[1] = 8'h00;
        memory[2] = 8'h80;
        memory[3] = 8'h00;
        
        // 指令2: lw X2, 4(X1) (0x0040a103)
        memory[4] = 8'h03;
        memory[5] = 8'ha1;
        memory[6] = 8'h40;
        memory[7] = 8'h00;
        
        // 指令3: add X3, X1, X2 (0x002081b3)
        memory[8] = 8'hb3;
        memory[9] = 8'h81;
        memory[10] = 8'h20;
        memory[11] = 8'h00;
        
        // 指令4: sub X4, X3, X1 (0x40118233)
        memory[12] = 8'h33;
        memory[13] = 8'h82;
        memory[14] = 8'h11;
        memory[15] = 8'h40;
        
        // 指令5: or X5, X1, X4 (0x0040e2b3)
        memory[16] = 8'hb3;
        memory[17] = 8'he2;
        memory[18] = 8'h40;
        memory[19] = 8'h00;
        
        // 指令6: ori X6, X5, 1 (0x0012e313)
        memory[20] = 8'h13;
        memory[21] = 8'he3;
        memory[22] = 8'h12;
        memory[23] = 8'h00;
        
        // 指令7: sw X6, 0(X2) (0x00612023)
        memory[24] = 8'h23;
        memory[25] = 8'h20;
        memory[26] = 8'h61;
        memory[27] = 8'h00;
        
        // 指令8: slt X7, X2, X4 (0x004123b3)
        memory[28] = 8'hb3;
        memory[29] = 8'h23;
        memory[30] = 8'h41;
        memory[31] = 8'h00;
        
        // 指令9: slti X8, X2, 8 (0x00812413)
        memory[32] = 8'h13;
        memory[33] = 8'h24;
        memory[34] = 8'h81;
        memory[35] = 8'h00;
        
        // 指令10（可选）: beq X3, X5, -12 (0xfe518ae3)
        memory[36] = 8'he3;
        memory[37] = 8'h8a;
        memory[38] = 8'h51;
        memory[39] = 8'hfe;
    end
    
    // 读取指令（小端模式，组合逻辑）
    assign Instruction = {memory[Address+3], memory[Address+2], 
                         memory[Address+1], memory[Address]};

endmodule

