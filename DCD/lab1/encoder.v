module encoder_4to2(
    input  wire [3:0] swt,  // 4个输入开关（swt[3]为最高位）
    output reg  [1:0] led   // 2个输出LED（led[1]为高位）
);

// 组合逻辑：按高位优先原则编码
always @(*) begin
    if (swt[3]) begin       // 最高位输入有效
        led = 2'b11;
    end
    else if (swt[2]) begin  // 次高位输入有效
        led = 2'b10;
    end
    else if (swt[1]) begin  // 次低位输入有效
        led = 2'b01;
    end
    else if (swt[0]) begin  // 最低位输入有效
        led = 2'b00;
    end
    else begin              // 无输入时默认输出00
        led = 2'b00;
    end
end

endmodule
