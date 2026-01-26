// 单周期CPU顶层模块
module CPU(
    input clk,              // 时钟信号
    input rst               // 复位信号
);

    // 程序计数器PC
    reg [31:0] PC;
    
    // 指令和立即数
    wire [31:0] Instruction;
    wire [31:0] Immediate;
    
    // 控制信号
    wire RegWrite, MemWrite, MemRead, MemtoReg, ALUSrc, Branch;
    wire [3:0] ALUOp;
    
    // 寄存器堆信号
    wire [4:0] rs1, rs2, rd;
    wire [31:0] ReadData1, ReadData2, WriteData;
    
    // ALU信号
    wire [31:0] ALU_A, ALU_B, ALU_Result;
    wire Zero;
    
    // 存储器信号
    wire [31:0] MemReadData;
    
    // PC控制信号
    wire PCSrc;
    wire [31:0] PC_Next, PC_Branch;
    
    // 提取指令字段
    assign rs1 = Instruction[19:15];
    assign rs2 = Instruction[24:20];
    assign rd  = Instruction[11:7];
    
    // PC更新逻辑
    always @(posedge clk or posedge rst) begin
        if (rst)
            PC <= 32'd0;
        else
            PC <= PC_Next;
    end
    
    // PC跳转控制
    assign PCSrc = Branch & Zero;
    assign PC_Branch = PC + Immediate;
    assign PC_Next = PCSrc ? PC_Branch : (PC + 4);
    
    // 程序存储器实例化
    ProgramMemory PM(
        .Address(PC[7:0]),
        .Instruction(Instruction)
    );
    
    // 控制单元实例化
    ControlUnit CU(
        .opcode(Instruction[6:0]),
        .funct3(Instruction[14:12]),
        .funct7(Instruction[31:25]),
        .RegWrite(RegWrite),
        .MemWrite(MemWrite),
        .MemRead(MemRead),
        .MemtoReg(MemtoReg),
        .ALUSrc(ALUSrc),
        .Branch(Branch),
        .ALUOp(ALUOp)
    );
    
    // 立即数生成模块实例化
    ImmGen IG(
        .Instruction(Instruction),
        .Immediate(Immediate)
    );
    
    // 寄存器堆实例化
    RegisterFile RF(
        .clk(clk),
        .rst(rst),
        .RegWrite(RegWrite),
        .ReadReg1(rs1),
        .ReadReg2(rs2),
        .WriteReg(rd),
        .WriteData(WriteData),
        .ReadData1(ReadData1),
        .ReadData2(ReadData2)
    );
    
    // ALU输入选择
    assign ALU_A = ReadData1;
    assign ALU_B = ALUSrc ? Immediate : ReadData2;
    
    // ALU实例化
    ALU alu(
        .A(ALU_A),
        .B(ALU_B),
        .ALUOp(ALUOp),
        .Result(ALU_Result),
        .Zero(Zero)
    );
    
    // 数据存储器实例化
    DataMemory DM(
        .clk(clk),
        .MemWrite(MemWrite),
        .MemRead(MemRead),
        .Address(ALU_Result[7:0]),
        .WriteData(ReadData2),
        .ReadData(MemReadData)
    );
    
    // 写回数据选择
    assign WriteData = MemtoReg ? MemReadData : ALU_Result;

endmodule

