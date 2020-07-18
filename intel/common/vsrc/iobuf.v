// Joseph Tarango
module iobuf(
		output wire [0:0] dataout, //   dout.export
		input  wire [0:0] datain,  //    din.export
		input  wire [0:0] oe,      //     oe.export
		inout  wire [0:0] padio    // pad_io.export
);

IOBUF iobufElement(
		.dataout (dataout), //  output,  width = 1,   dout.export
		.datain  (datain),  //   input,  width = 1,    din.export
		.oe      (oe),      //   input,  width = 1,     oe.export
		.padio   (padio)    //   inout,  width = 1, pad_io.export
);

endmodule


module ibuf(
  input wire [0:0] datain,
  output wire [0:0] dataout
);

IBUF ibufElement(
  .datain(datain),
  .dataout(dataout)
);

endmodule

module obuf(
  input wire [0:0] datain,
  output wire [0:0] dataout
);

OBUF obufElement(
  .datain(datain),
  .dataout(dataout)
);

endmodule
