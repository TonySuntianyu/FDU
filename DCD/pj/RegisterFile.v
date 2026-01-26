// 寄存器堆模块
// 32个32位通用寄存器，X0恒为0
module RegisterFile(
    input clk,                  // 时钟信号
    input rst,                  // 复位信号
    input RegWrite,             // 寄存器写使能
    input [4:0] ReadReg1,       // 读寄存器1地址
    input [4:0] ReadReg2,       // 读寄存器2地址
    input [4:0] WriteReg,       // 写寄存器地址
    input [31:0] WriteData,     // 写入数据
    output [31:0] ReadData1,    // 读出数据1
    output [31:0] ReadData2     // 读出数据2
);

    // 32个32位寄存器
    reg [31:0] registers [0:31];
    
    // 循环变量
    integer i;
    
    // 初始化和写操作
    always @(posedge clk or posedge rst) begin
        if (rst) begin
            // 复位时清零所有寄存器
            for (i = 0; i < 32; i = i + 1) begin
                registers[i] <= 32'd0;
            end
        end
        else if (RegWrite && WriteReg != 5'd0) begin
            // X0寄存器不可写，保持为0
            registers[WriteReg] <= WriteData;
        end
    end

    // 读操作（组合逻辑）
    assign ReadData1 = (ReadReg1 == 5'd0) ? 32'd0 : registers[ReadReg1];
    assign ReadData2 = (ReadReg2 == 5'd0) ? 32'd0 : registers[ReadReg2];

endmodule

