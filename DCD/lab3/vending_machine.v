`timescale 1ns / 1ps

// 自动售货机控制器 - 最终可综合版（适配原XDC约束）
// 修复所有关键警告，可顺利生成比特流
module vending_machine(  // 恢复原模块名，匹配XDC
    input wire clk,                      // 恢复原端口名，匹配XDC
    input wire [2:0] product_selected,   // 恢复原端口名，匹配XDC
    input wire button_5,                 // 恢复原端口名，匹配XDC
    input wire button_2,                 // 恢复原端口名，匹配XDC
    input wire button_1,                 // 恢复原端口名，匹配XDC
    input wire button_05,                // 恢复原端口名，匹配XDC
    output wire [6:0] A2G,               // 恢复原端口名，匹配XDC
    output reg [7:0] AN,                 // 恢复原端口名，匹配XDC
    output reg DP,                       // 恢复原端口名，匹配XDC
    output reg success_led               // 恢复原端口名，匹配XDC
);

// ====================== 1. 价格表（可综合版）======================
reg [7:0] prod_price;         
always @(*) begin
    case(product_selected)
        3'd0: prod_price = 8'd0;    // 无效商品
        3'd1: prod_price = 8'd5;    // 1号: 0.5元 (5*0.1)
        3'd2: prod_price = 8'd10;   // 2号: 1元 (10*0.1)
        3'd3: prod_price = 8'd15;   // 3号: 1.5元 (15*0.1)
        3'd4: prod_price = 8'd20;   // 4号: 2元 (20*0.1)
        3'd5: prod_price = 8'd65;   // 5号: 6.5元 (65*0.1)
        3'd6: prod_price = 8'd130;  // 6号: 13元 (130*0.1)
        3'd7: prod_price = 8'd0;    // 无效商品
    endcase
end

// ====================== 2. 核心状态变量 =======================
reg [7:0] current_price = 8'd0;     // 显式初始化
reg [7:0] total_paid = 8'd0;        // 显式初始化
reg [2:0] last_switch = 3'b000;
reg [3:0] display_value = 4'd0;     // 显式初始化

// ====================== 3. 消抖时钟生成（50MHz→3Hz）======================
reg [23:0] count = 24'd0;
reg clk2 = 1'b0;

always @(posedge clk) begin
    count <= count + 1'b1;
    if (count[23] == 1'b1) begin
        clk2 <= ~clk2;
        count <= 24'd0;
    end
end

// ====================== 4. 按钮消抖+边沿检测 =======================
reg button_5_reg1=0, button_5_reg2=0; // 显式初始化
reg button_2_reg1=0, button_2_reg2=0;
reg button_1_reg1=0, button_1_reg2=0;
reg button_05_reg1=0, button_05_reg2=0;

wire button_5_edge;
wire button_2_edge;
wire button_1_edge;
wire button_05_edge;

// 双寄存器同步消抖
always @(posedge clk2) begin
    button_5_reg1 <= button_5;
    button_2_reg1 <= button_2;
    button_1_reg1 <= button_1;
    button_05_reg1 <= button_05;
    
    button_5_reg2 <= button_5_reg1;
    button_2_reg2 <= button_2_reg1;
    button_1_reg2 <= button_1_reg1;
    button_05_reg2 <= button_05_reg1;
end

// 上升沿检测（避免组合逻辑冒险）
assign button_5_edge = button_5_reg1 & ~button_5_reg2;
assign button_2_edge = button_2_reg1 & ~button_2_reg2;
assign button_1_edge = button_1_reg1 & ~button_1_reg2;
assign button_05_edge = button_05_reg1 & ~button_05_reg2;

// ====================== 5. 商品选择+投币逻辑 =======================
always @(posedge clk2) begin
    if (product_selected != last_switch) begin
        last_switch <= product_selected;
        current_price <= prod_price;
        total_paid <= 8'd0;
        success_led <= 1'b0;
    end else begin
        // 投币累加（避免多条件同时触发）
        if (button_5_edge) begin
            total_paid <= total_paid + 8'd50;  // 5元=50*0.1
        end else if (button_2_edge) begin
            total_paid <= total_paid + 8'd20;  // 2元=20*0.1
        end else if (button_1_edge) begin
            total_paid <= total_paid + 8'd10;  // 1元=10*0.1
        end else if (button_05_edge) begin
            total_paid <= total_paid + 8'd5;   // 0.5元=5*0.1
        end

        // 交易成功判断（避免逻辑歧义）
        success_led <= (total_paid >= current_price) && (current_price > 8'd0);
    end
end

// ====================== 6. 数码管扫描驱动 =======================
reg [19:0] scan_counter = 20'd0;    // 显式初始化
wire [1:0] scan_pos;

// 扫描时钟分频（100Hz）
always @(posedge clk) begin
    scan_counter <= scan_counter + 1'b1;
    if (scan_counter == 20'd999_999) begin
        scan_counter <= 20'd0;
    end
end

assign scan_pos = scan_counter[18:17];

// 数码管译码逻辑（共阳极，匹配硬件）
reg [6:0] seg_output = 7'b1111111;  // 显式初始化
always @(*) begin
    case (display_value)
        4'd0: seg_output = 7'b1000000;
        4'd1: seg_output = 7'b1111001;
        4'd2: seg_output = 7'b0100100;
        4'd3: seg_output = 7'b0110000;
        4'd4: seg_output = 7'b0011001;
        4'd5: seg_output = 7'b0010010;
        4'd6: seg_output = 7'b0000010;
        4'd7: seg_output = 7'b1111000;
        4'd8: seg_output = 7'b0000000;
        4'd9: seg_output = 7'b0010000;
        default: seg_output = 7'b1111111;
    endcase
end
assign A2G = seg_output;

// 数码管动态扫描（消除悬空信号警告）
always @(*) begin
    AN = 8'b11111111;  // 默认值，消除警告
    DP = 1'b1;         // 默认值，消除警告
    display_value = 4'd0; // 默认值，消除警告
    
    case (scan_pos)
        2'b00: begin
            AN = 8'b11111110;
            DP = 1'b1;
            display_value = product_selected;
        end
        2'b01: begin
            AN = 8'b11111101;
            DP = 1'b1;
            display_value = total_paid % 10;
        end
        2'b10: begin
            AN = 8'b11111011;
            DP = 1'b0;
            display_value = (total_paid / 10) % 10;
        end
        2'b11: begin
            AN = 8'b11110111;
            DP = 1'b1;
            display_value = (total_paid / 100) % 10;
        end
    endcase
end

endmodule