`timescale 1ns / 1ps

//------------------------------------------------------------------------------
// 1. ALU Core Module (Dataflow Style)
//------------------------------------------------------------------------------
// This version uses continuous assignments to model the combinational logic,
// which contrasts with the behavioral `always @(*)` block approach.
module alu_core_dataflow (
    input      [3:0] A,
    input      [3:0] B,
    input      [1:0] op,
    output     [7:0] F,
    output           Cout
);

    // -- Intermediate wires for each operation's result --
    wire [7:0] f_add, f_sub, f_not, f_mul;
    wire       c_add, c_sub, c_not, c_mul;
    
    // Operation 00: Addition
    wire [4:0] add_res = A + B;
    assign c_add = add_res[4];
    assign f_add = {{3'b0, c_add}, add_res[3:0]};

    // Operation 01: Subtraction (A - B = A + ~B + 1)
    wire [4:0] sub_res = A + (~B) + 1;
    assign c_sub = ~sub_res[4]; // Borrow is the inverse of the carry-out
    assign f_sub = {{3'b0, c_sub}, sub_res[3:0]};
    
    // Operation 10: Bitwise NOT
    assign c_not = 1'b0;
    assign f_not = {4'b0, ~A};
    
    // Operation 11: Multiplication
    assign c_mul = 1'b0;
    assign f_mul = A * B;
    
    // -- Final output selection using conditional operators (Mux) --
    // This creates a 4-to-1 multiplexer controlled by 'op'.
    assign F = (op == 2'b00) ? f_add :
               (op == 2'b01) ? f_sub :
               (op == 2'b10) ? f_not :
                               f_mul;
                               
    assign Cout = (op == 2'b00) ? c_add :
                  (op == 2'b01) ? c_sub :
                  (op == 2'b10) ? c_not :
                                  c_mul;
                                  
endmodule


//------------------------------------------------------------------------------
// 2. Top Module (Refactored with FSM and Function)
//------------------------------------------------------------------------------
module lab2_alu_top_refactored (
    input        CLK100MHZ,
    input  [3:0] A,
    input  [3:0] B,
    input  [1:0] op,
    output [7:0] F,
    output       Cout,
    output [1:0] AN,
    output [6:0] seg
);

    // -- Instantiate the dataflow-style ALU core --
    alu_core_dataflow u_alu_core (
        .A(A),
        .B(B),
        .op(op),
        .F(F),      // F is directly passed through to the LEDs
        .Cout(Cout) // Cout is directly passed through to its LED
    );

    //========================================================
    // 2.1 Display Driver FSM
    //========================================================
    // This FSM controls which digit is active and what data is displayed.
    // It's a more formal structure than a simple toggling signal.
    
    // FSM state definition
    localparam S_SHOW_LOW  = 1'b0; // State to display F[3:0] on AN0
    localparam S_SHOW_HIGH = 1'b1; // State to display F[7:4] on AN1
    
    reg state; // FSM state register
    
    // A counter to control the refresh rate of the display (~500 Hz)
    // 100MHz / 200,000 = 500 Hz
    reg [17:0] refresh_counter;
    localparam REFRESH_PERIOD = 18'd100000;

    // FSM state transition logic (sequential)
    always @(posedge CLK100MHZ) begin
        if (refresh_counter < REFRESH_PERIOD - 1) begin
            refresh_counter <= refresh_counter + 1;
        end else begin
            refresh_counter <= 0;
            state <= ~state; // Transition to the other state
        end
    end

    //========================================================
    // 2.2 Decoder Function (Common Anode)
    //========================================================
    // Encapsulating the decoder in a function cleans up the main logic.
    // This decoder is for COMMON ANODE displays (0 = segment on).
    function [6:0] hex_to_seg (input [3:0] data);
        begin
            case(data)
                4'h0: hex_to_seg = 7'b1000000; // 0
                4'h1: hex_to_seg = 7'b1111001; // 1
                4'h2: hex_to_seg = 7'b0100100; // 2
                4'h3: hex_to_seg = 7'b0110000; // 3
                4'h4: hex_to_seg = 7'b0011001; // 4
                4'h5: hex_to_seg = 7'b0010010; // 5
                4'h6: hex_to_seg = 7'b0000010; // 6
                4'h7: hex_to_seg = 7'b1111000; // 7
                4'h8: hex_to_seg = 7'b0000000; // 8
                4'h9: hex_to_seg = 7'b0010000; // 9
                4'hA: hex_to_seg = 7'b0001000; // A
                4'hB: hex_to_seg = 7'b0000011; // b
                4'hC: hex_to_seg = 7'b1000110; // C
                4'hD: hex_to_seg = 7'b0100001; // d
                4'hE: hex_to_seg = 7'b0000110; // E
                4'hF: hex_to_seg = 7'b0001110; // F
                default: hex_to_seg = 7'b1111111; // Off
            endcase
        end
    endfunction
    
    //========================================================
    // 2.3 FSM Output Logic (Combinational)
    //========================================================
    // This logic determines the outputs based on the current FSM state.
    reg [3:0] data_to_display;
    reg [1:0] anode_select;
    
    always @(*) begin
        case(state)
            S_SHOW_LOW: begin
                data_to_display = F[3:0];
                anode_select    = 2'b10; // Activate AN0 (right digit)
            end
            S_SHOW_HIGH: begin
                data_to_display = F[7:4];
                anode_select    = 2'b01; // Activate AN1 (left digit)
            end
            default: begin
                data_to_display = 4'hF;
                anode_select    = 2'b11; // All off
            end
        endcase
    end
    
    // Final assignment to output ports
    assign AN  = anode_select;
    assign seg = hex_to_seg(data_to_display);

endmodule