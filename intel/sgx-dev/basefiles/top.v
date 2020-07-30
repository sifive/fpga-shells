// (C) 2001-2020 Intel Corporation. All rights reserved.
// Your use of Intel Corporation's design tools, logic functions and other 
// software and tools, and its AMPP partner logic functions, and any output 
// files from any of the foregoing (including device programming or simulation 
// files), and any associated documentation or information are expressly subject 
// to the terms and conditions of the Intel Program License Subscription 
// Agreement, Intel FPGA IP License Agreement, or other applicable 
// license agreement, including, without limitation, that your use is for the 
// sole purpose of programming logic devices manufactured by Intel and sold by 
// Intel or its authorized distributors.  Please refer to the applicable 
// agreement for further details.
//
// Description: 
// ===========
// Stratix 10 GX L-tile Triple-Speed Ethernet reference design top level 
//
// Revision  Date            Description
// ========  ====            ===========
// 1.0       Jul-2020        Initial release 

// Warning for Reset Release IP: 
// =============================
// Do not instantiate this IP multiple time. Move this IP to your 
// top level if you are integrating this reference design into your design. 
// @note https://verilogguide.readthedocs.io/en/latest/verilog/firstproject.html
module top (
   //Clock and Reset
   input wire          REF_CLK_PLL,      // 50MHz refclk internal FIFO, packet generator, packet monitor & AV-ST interface

	// RISC-V Processor
   input wire          key1,
   input wire          key2,
   input wire          key3,
   output wire         led_0,
   output wire         led_1,
   output wire         led_2,
   output wire         led_3,
   output wire         led_4
);

// -------------------------------------------------------------------------
// JTAG Magic
// -------------------------------------------------------------------------
localparam jtagStateSize = 23; // JTAG State signal size.
integer i; // For loop

// Altera State of JTAG
wire   [jtagStateSize:0]     jtagState;
wire        ledSet;
reg         errorSignal;
wire        jtag_tdi;
wire        jtag_tdo;
wire        jtag_tck;
wire        jtag_tms;
wire        uart_rx;
wire        uart_tx;

vJTAG RISCvJTAG(
	.tdi                (jtag_tdi),     //  output,  width = 1,     jtag.tdi
	.tdo                (jtag_tdo),     //   input,  width = 1,     .tdo
	.ir_in              (uart_rx),      //  output,  width = 1,     .ir_in
	.ir_out             (uart_tx),      //   input,  width = 1,     .ir_out
	.virtual_state_cdr  (jtagState[0]), //  output,  width = 1,     .virtual_state_cdr
	.virtual_state_sdr  (jtagState[1]), //  output,  width = 1,     .virtual_state_sdr
	.virtual_state_e1dr (jtagState[2]), //  output,  width = 1,     .virtual_state_e1dr
	.virtual_state_pdr  (jtagState[3]), //  output,  width = 1,     .virtual_state_pdr
	.virtual_state_e2dr (jtagState[4]), //  output,  width = 1,     .virtual_state_e2dr
	.virtual_state_udr  (jtagState[5]), //  output,  width = 1,     .virtual_state_udr
	.virtual_state_cir  (jtagState[6]), //  output,  width = 1,     .virtual_state_cir
	.virtual_state_uir  (jtagState[7]), //  output,  width = 1,     .virtual_state_uir
	.tms                (jtag_tms),     //  output,  width = 1,     .tms
	.jtag_state_tlr     (jtagState[8]), //  output,  width = 1,     .jtag_state_tlr
	.jtag_state_rti     (jtagState[9]), //  output,  width = 1,     .jtag_state_rti
	.jtag_state_sdrs    (jtagState[10]),//  output,  width = 1,     .jtag_state_sdrs
	.jtag_state_cdr     (jtagState[11]),//  output,  width = 1,     .jtag_state_cdr
	.jtag_state_sdr     (jtagState[12]),//  output,  width = 1,     .jtag_state_sdr
	.jtag_state_e1dr    (jtagState[13]),//  output,  width = 1,     .jtag_state_e1dr
	.jtag_state_pdr     (jtagState[14]),//  output,  width = 1,     .jtag_state_pdr
	.jtag_state_e2dr    (jtagState[15]),//  output,  width = 1,     .jtag_state_e2dr
	.jtag_state_udr     (jtagState[16]),//  output,  width = 1,     .jtag_state_udr
	.jtag_state_sirs    (jtagState[17]),//  output,  width = 1,     .jtag_state_sirs
	.jtag_state_cir     (jtagState[18]),//  output,  width = 1,     .jtag_state_cir
	.jtag_state_sir     (jtagState[19]),//  output,  width = 1,     .jtag_state_sir
	.jtag_state_e1ir    (jtagState[20]),//  output,  width = 1,     .jtag_state_e1ir
	.jtag_state_pir     (jtagState[21]),//  output,  width = 1,     .jtag_state_pir
	.jtag_state_e2ir    (jtagState[22]),//  output,  width = 1,     .jtag_state_e2ir
	.jtag_state_uir     (jtagState[23]),//  output,  width = 1,     .jtag_state_uir
	.tck                (jtag_tck)      //  output,  width = 1,      tck.clk
);

FPGAChip uFPGACHIP(
   .clk25   (REF_CLK_PLL), // input
   .key1    (key1),        // input
   .key2    (key2),        // input
   .key3    (key3),        // input
   .led_0   (led_0),       // output
   .led_1   (led_1),       // output
   .led_2   (led_2),       // output
   .led_3   (led_3),       // output
   .jtag_tdi(jtag_tdi),    // input
   .jtag_tdo(jtag_tdo),    // input
   .jtag_tck(jtag_tck),    // input
   .jtag_tms(jtag_tms),    // input
   .uart_rx (uart_rx),     // input
   .uart_tx (uart_tx)      // output
);

// Merge all virtual states into led
always @ (posedge REF_CLK_PLL)
	begin
		for (i = 0; i < 23; i = i + 1) begin
			errorSignal <= (errorSignal | jtagState[i]);
		end
	end

// -------------------------------------------------------------------------
// Status ports assignments
// -------------------------------------------------------------------------
assign led_4 = (errorSignal);

endmodule 