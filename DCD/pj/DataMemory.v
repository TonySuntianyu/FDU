// 数据存储器模块（DM）
// 256字节，按字节寻址（0-255）
module DataMemory(
    input clk,                  // 时钟信号
    input MemWrite,             // 写使能
    input MemRead,              // 读使能
    input [7:0] Address,        // 地址（字节地址）
    input [31:0] WriteData,     // 写入数据
    output [31:0] ReadData      // 读出数据
);

    // 256字节存储空间
    reg [7:0] memory [0:255];
    
    // 循环变量
    integer i;
    
    // 初始化数据存储器
    initial begin
        for (i = 0; i < 16; i = i + 1) begin
            memory[i] = i[7:0];
        end
        for (i = 16; i < 256; i = i + 1) begin
            memory[i] = 8'h00;
        end
    end
    
    // 写操作（同步，小端模式）
    always @(posedge clk) begin
        if (MemWrite) begin
            memory[Address]   <= WriteData[7:0];
            memory[Address+1] <= WriteData[15:8];
            memory[Address+2] <= WriteData[23:16];
            memory[Address+3] <= WriteData[31:24];
        end
    end
    
    // 读操作（组合逻辑，小端模式）
    assign ReadData = MemRead ? {memory[Address+3], memory[Address+2],
                                 memory[Address+1], memory[Address]} : 32'd0;

endmodule

