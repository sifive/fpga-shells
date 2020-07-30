// Joseph Tarango
module iobuf(
		output wire [0:0] dataout, //   dout.export
		input  wire [0:0] datain,  //    din.export
		input  wire [0:0] oe,      //     oe.export
		inout  wire [0:0] padio    // pad_io.export
);

assign dataout = datain;

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

assign dataout = datain;

IOBUF ibufElement(
  .datain(datain),
  .dataout(dataout)
);

endmodule

module obuf(
  input wire [0:0] datain,
  output wire [0:0] dataout
);

assign dataout = datain;
IOBUF obufElement(
  .datain(datain),
  .dataout(dataout)
);

endmodule
