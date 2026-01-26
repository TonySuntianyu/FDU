## 自动售货机约束文件 - Nexys4 DDR
## 基于 lab3.v 中的 vending_machine 模块

## 时钟信号 - 100MHz
set_property -dict { PACKAGE_PIN E3    IOSTANDARD LVCMOS33 } [get_ports { clk }];
create_clock -add -name sys_clk_pin -period 10.00 -waveform {0 5} [get_ports { clk }];

## 商品选择开关 (SW[2:0])
set_property -dict { PACKAGE_PIN J15   IOSTANDARD LVCMOS33 } [get_ports { product_selected[0] }];
set_property -dict { PACKAGE_PIN L16   IOSTANDARD LVCMOS33 } [get_ports { product_selected[1] }];
set_property -dict { PACKAGE_PIN M13   IOSTANDARD LVCMOS33 } [get_ports { product_selected[2] }];

## 按钮输入
## button_5:  投币5元按钮 (BTNC - 中间按钮)
## button_2:  投币2元按钮 (BTNU - 上按钮)
## button_1:  投币1元按钮 (BTNL - 左按钮)
## button_05: 投币0.5元按钮 (BTNR - 右按钮)
set_property -dict { PACKAGE_PIN N17   IOSTANDARD LVCMOS33 } [get_ports { button_5 }];
set_property -dict { PACKAGE_PIN M18   IOSTANDARD LVCMOS33 } [get_ports { button_2 }];
set_property -dict { PACKAGE_PIN P17   IOSTANDARD LVCMOS33 } [get_ports { button_1 }];
set_property -dict { PACKAGE_PIN M17   IOSTANDARD LVCMOS33 } [get_ports { button_05 }];

## 7段数码管段选信号 (A2G[6:0])
## A2G[0] = CA (段A)
## A2G[1] = CB (段B)
## A2G[2] = CC (段C)
## A2G[3] = CD (段D)
## A2G[4] = CE (段E)
## A2G[5] = CF (段F)
## A2G[6] = CG (段G)
set_property -dict { PACKAGE_PIN T10   IOSTANDARD LVCMOS33 } [get_ports { A2G[0] }];
set_property -dict { PACKAGE_PIN R10   IOSTANDARD LVCMOS33 } [get_ports { A2G[1] }];
set_property -dict { PACKAGE_PIN K16   IOSTANDARD LVCMOS33 } [get_ports { A2G[2] }];
set_property -dict { PACKAGE_PIN K13   IOSTANDARD LVCMOS33 } [get_ports { A2G[3] }];
set_property -dict { PACKAGE_PIN P15   IOSTANDARD LVCMOS33 } [get_ports { A2G[4] }];
set_property -dict { PACKAGE_PIN T11   IOSTANDARD LVCMOS33 } [get_ports { A2G[5] }];
set_property -dict { PACKAGE_PIN L18   IOSTANDARD LVCMOS33 } [get_ports { A2G[6] }];

## 数码管位选信号 (AN[7:0]) - 低电平有效
set_property -dict { PACKAGE_PIN J17   IOSTANDARD LVCMOS33 } [get_ports { AN[0] }];
set_property -dict { PACKAGE_PIN J18   IOSTANDARD LVCMOS33 } [get_ports { AN[1] }];
set_property -dict { PACKAGE_PIN T9    IOSTANDARD LVCMOS33 } [get_ports { AN[2] }];
set_property -dict { PACKAGE_PIN J14   IOSTANDARD LVCMOS33 } [get_ports { AN[3] }];
set_property -dict { PACKAGE_PIN P14   IOSTANDARD LVCMOS33 } [get_ports { AN[4] }];
set_property -dict { PACKAGE_PIN T14   IOSTANDARD LVCMOS33 } [get_ports { AN[5] }];
set_property -dict { PACKAGE_PIN K2    IOSTANDARD LVCMOS33 } [get_ports { AN[6] }];
set_property -dict { PACKAGE_PIN U13   IOSTANDARD LVCMOS33 } [get_ports { AN[7] }];

## 小数点信号 (DP) - 低电平点亮
## 注意: Nexys4 DDR 只有一个小数点引脚(DP),连接到所有数码管
## 代码已修改为使用单个 DP 信号，根据扫描位置动态控制小数点显示
set_property -dict { PACKAGE_PIN H15   IOSTANDARD LVCMOS33 } [get_ports { DP }];

## 购买成功指示LED (LED[0])
set_property -dict { PACKAGE_PIN H17   IOSTANDARD LVCMOS33 } [get_ports { success_led }];
