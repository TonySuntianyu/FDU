// 控制单元模块
// 根据指令的opcode、funct3和funct7生成控制信号
module ControlUnit(
    input [6:0] opcode,         // 指令操作码
    input [2:0] funct3,         // 功能码3
    input [6:0] funct7,         // 功能码7
    output reg RegWrite,        // 寄存器写使能
    output reg MemWrite,        // 存储器写使能
    output reg MemRead,         // 存储器读使能
    output reg MemtoReg,        // 存储器数据写回寄存器
    output reg ALUSrc,          // ALU第二操作数来源（0:寄存器, 1:立即数）
    output reg Branch,          // 分支指令标志
    output reg [3:0] ALUOp      // ALU操作码
);

    // RISC-V指令opcode定义
    parameter OP_R_TYPE = 7'b0110011;  // R型指令
    parameter OP_I_TYPE = 7'b0010011;  // I型算术指令
    parameter OP_LOAD   = 7'b0000011;  // Load指令
    parameter OP_STORE  = 7'b0100011;  // Store指令
    parameter OP_BRANCH = 7'b1100011;  // Branch指令

    // ALU操作码定义
    parameter ALU_ADD = 4'b0000;
    parameter ALU_SUB = 4'b0001;
    parameter ALU_OR  = 4'b0010;
    parameter ALU_SLT = 4'b0011;

    always @(*) begin
        // 默认值
        RegWrite = 0;
        MemWrite = 0;
        MemRead = 0;
        MemtoReg = 0;
        ALUSrc = 0;
        Branch = 0;
        ALUOp = ALU_ADD;
        
        case(opcode)
            OP_R_TYPE: begin  // R型指令：add, sub, or, slt
                RegWrite = 1;
                ALUSrc = 0;
                case({funct7, funct3})
                    10'b0000000_000: ALUOp = ALU_ADD;  // add
                    10'b0100000_000: ALUOp = ALU_SUB;  // sub
                    10'b0000000_110: ALUOp = ALU_OR;   // or
                    10'b0000000_010: ALUOp = ALU_SLT;  // slt
                    default: ALUOp = ALU_ADD;
                endcase
            end
            
            OP_I_TYPE: begin  // I型算术指令：addi, ori, slti
                RegWrite = 1;
                ALUSrc = 1;
                case(funct3)
                    3'b000: ALUOp = ALU_ADD;  // addi
                    3'b110: ALUOp = ALU_OR;   // ori
                    3'b010: ALUOp = ALU_SLT;  // slti
                    default: ALUOp = ALU_ADD;
                endcase
            end
            
            OP_LOAD: begin  // lw指令
                RegWrite = 1;
                MemRead = 1;
                MemtoReg = 1;
                ALUSrc = 1;
                ALUOp = ALU_ADD;
            end
            
            OP_STORE: begin  // sw指令
                MemWrite = 1;
                ALUSrc = 1;
                ALUOp = ALU_ADD;
            end
            
            OP_BRANCH: begin  // beq指令
                Branch = 1;
                ALUSrc = 0;
                ALUOp = ALU_SUB;
            end
            
            default: begin
                // 默认值已设置
            end
        endcase
    end

endmodule

