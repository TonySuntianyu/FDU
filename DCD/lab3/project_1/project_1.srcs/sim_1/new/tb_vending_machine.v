// 修复版自动售货机Testbench
`timescale 1ns / 1ps

module tb_vending_machine;

reg clk;                  
reg [2:0] product_selected; 
reg button_5;             
reg button_2;             
reg button_1;             
reg button_05;            

wire [6:0] A2G;           
wire [7:0] AN;            
wire DP;                  
wire success_led;         

vending_machine uut (
    .clk(clk),
    .product_selected(product_selected),
    .button_5(button_5),
    .button_2(button_2),
    .button_1(button_1),
    .button_05(button_05),
    .A2G(A2G),
    .AN(AN),
    .DP(DP),
    .success_led(success_led)
);

// 100MHz时钟
initial begin
    clk = 0;
    forever #5 clk = ~clk; 
end

// 测试激励（适配3Hz消抖时钟）
initial begin
    product_selected = 3'b000;
    button_5 = 0;
    button_2 = 0;
    button_1 = 0;
    button_05 = 0;
    
    #1000;
    
    // 测试1号商品（0.5元）
    $display("=== 测试场景1：选择1号商品（0.5元） ===");
    product_selected = 3'b001; 
    #35000000; // 等待35ms（超过3Hz消抖周期）
    
    // 模拟按键抖动后按下
    button_05 = 1;
    #10000;    // 模拟10us抖动
    button_05 = 0;
    #10000;
    button_05 = 1;
    #40000000; // 稳定按下40ms
    button_05 = 0;
    #35000000; 
    
    if (success_led == 1) begin
        $display("场景1测试通过：成功LED点亮");
    end else begin
        $display("场景1测试失败：成功LED未点亮");
    end
    
    // 测试5号商品（6.5元）
    $display("\n=== 测试场景2：选择5号商品（6.5元） ===");
    product_selected = 3'b101; 
    #35000000; 
    
    button_5 = 1;
    #40000000;
    button_5 = 0;
    #35000000;
    
    button_1 = 1;
    #40000000;
    button_1 = 0;
    #35000000;
    
    button_05 = 1;
    #40000000;
    button_05 = 0;
    #35000000;
    
    if (success_led == 1) begin
        $display("场景2测试通过：成功LED点亮");
    end else begin
        $display("场景2测试失败：成功LED未点亮");
    end
    
    #100000000;
    $display("\n=== 所有测试场景执行完毕 ===");
    $finish;
end

initial begin
    $dumpfile("vending_machine_fixed.vcd");
    $dumpvars(0, tb_vending_machine);
end

endmodule