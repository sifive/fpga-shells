// IOBUF_altera_gpio_1930_alkbnhy.v

// This file was auto-generated from altera_gpio_hw.tcl.  If you edit it your changes
// will probably be lost.
// 
// Generated using ACDS version 20.2 50

`timescale 1 ps / 1 ps
module IOBUF_altera_gpio_1930_alkbnhy (
		output wire [0:0] dataout, //   dout.export
		input  wire [0:0] datain,  //    din.export
		input  wire [0:0] oe,      //     oe.export
		inout  wire [0:0] padio    // pad_io.export
	);

	altera_gpio #(
		.SIZE                (1),
		.PIN_TYPE            ("bidir"),
		.REGISTER_MODE       ("none"),
		.HALF_RATE           ("false"),
		.SEPARATE_I_O_CLOCKS ("false"),
		.BUFFER_TYPE         ("single-ended"),
		.PSEUDO_DIFF         ("false"),
		.ARESET_MODE         ("none"),
		.SRESET_MODE         ("none"),
		.OPEN_DRAIN          ("false"),
		.BUS_HOLD            ("false"),
		.ENABLE_OE           ("false"),
		.ENABLE_CKE          ("false"),
		.ENABLE_TERM         ("false")
	) core (
		.dout                       (dataout),              //  output,  width = 1,   dout.export
		.din                        (datain),               //   input,  width = 1,    din.export
		.oe                         (oe),                   //   input,  width = 1,     oe.export
		.pad_io                     (padio),                //   inout,  width = 1, pad_io.export
		.cke                        (1'b1),                 // (terminated),                    
		.ck_fr_in                   (1'b0),                 // (terminated),                    
		.ck_fr_out                  (1'b0),                 // (terminated),                    
		.ck_in                      (1'b0),                 // (terminated),                    
		.ck_out                     (1'b0),                 // (terminated),                    
		.ck_fr                      (1'b0),                 // (terminated),                    
		.ck                         (1'b0),                 // (terminated),                    
		.ck_hr_in                   (1'b0),                 // (terminated),                    
		.ck_hr_out                  (1'b0),                 // (terminated),                    
		.ck_hr                      (1'b0),                 // (terminated),                    
		.pad_io_b                   (),                     // (terminated),                    
		.pad_in                     (1'b0),                 // (terminated),                    
		.pad_in_b                   (1'b0),                 // (terminated),                    
		.pad_out                    (),                     // (terminated),                    
		.pad_out_b                  (),                     // (terminated),                    
		.seriesterminationcontrol   (16'b0000000000000000), // (terminated),                    
		.parallelterminationcontrol (16'b0000000000000000), // (terminated),                    
		.aclr                       (1'b0),                 // (terminated),                    
		.aset                       (1'b0),                 // (terminated),                    
		.sclr                       (1'b0),                 // (terminated),                    
		.sset                       (1'b0)                  // (terminated),                    
	);

endmodule
