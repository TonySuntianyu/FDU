// ALU模块 - 算术逻辑单元
// 支持的操作：ADD, SUB, OR, SLT
module ALU(
    input [31:0] A,          // 操作数A
    input [31:0] B,          // 操作数B
    input [3:0] ALUOp,       // ALU操作码
    output reg [31:0] Result, // 运算结果
    output Zero              // 零标志位（用于beq指令）
);

    // ALU操作码定义
    parameter ALU_ADD = 4'b0000;  // 加法
    parameter ALU_SUB = 4'b0001;  // 减法
    parameter ALU_OR  = 4'b0010;  // 或运算
    parameter ALU_SLT = 4'b0011;  // 小于则置位

    always @(*) begin
        case(ALUOp)
            ALU_ADD: Result = A + B;
            ALU_SUB: Result = A - B;
            ALU_OR:  Result = A | B;
            ALU_SLT: Result = ($signed(A) < $signed(B)) ? 32'd1 : 32'd0;
            default: Result = 32'd0;
        endcase
    end

    // 零标志位，用于条件跳转
    assign Zero = (Result == 32'd0);

endmodule

