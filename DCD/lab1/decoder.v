module decoder_3to8(
    input wire [2:0] swt,      // 3位输入（对应开发板的3个开关）
    output reg [7:0] led       // 8位输出（对应开发板的8个LED）
);

always @(*) begin
    case(swt)                  // 用swt替代原来的A
        3'b000: led = 8'b00000001;  // 用led替代原来的Y
        3'b001: led = 8'b00000010;
        3'b010: led = 8'b00000100;
        3'b011: led = 8'b00001000;
        3'b100: led = 8'b00010000;
        3'b101: led = 8'b00100000;
        3'b110: led = 8'b01000000;
        3'b111: led = 8'b10000000;
        default: led = 8'b00000000;
    endcase
end

endmodule
