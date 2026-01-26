// 立即数生成模块
// 根据不同指令类型生成符号扩展的立即数
module ImmGen(
    input [31:0] Instruction,   // 32位指令
    output reg [31:0] Immediate // 32位立即数
);

    wire [6:0] opcode;
    assign opcode = Instruction[6:0];
    
    // RISC-V指令opcode定义
    parameter OP_I_TYPE = 7'b0010011;  // I型算术指令
    parameter OP_LOAD   = 7'b0000011;  // Load指令
    parameter OP_STORE  = 7'b0100011;  // Store指令
    parameter OP_BRANCH = 7'b1100011;  // Branch指令

    always @(*) begin
        case(opcode)
            OP_I_TYPE, OP_LOAD: begin
                // I型指令：立即数在[31:20]
                Immediate = {{20{Instruction[31]}}, Instruction[31:20]};
            end
            
            OP_STORE: begin
                // S型指令：立即数在[31:25]和[11:7]
                Immediate = {{20{Instruction[31]}}, Instruction[31:25], Instruction[11:7]};
            end
            
            OP_BRANCH: begin
                // B型指令：立即数在[31]、[7]、[30:25]、[11:8]
                Immediate = {{19{Instruction[31]}}, Instruction[31], 
                            Instruction[7], Instruction[30:25], Instruction[11:8], 1'b0};
            end
            
            default: begin
                Immediate = 32'd0;
            end
        endcase
    end

endmodule

