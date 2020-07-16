module iobuf(
  input wire [0:0] datain,
  output wire [0:0] dataout,  
  input wire oe,
  input wire padio
);

IOBUF iobuffElement(
  .datain(datain),
  .dataout(dataout),
  .oe(oe),
  .padio(padio)
);

endmodule

module ibuf(
  input wire [0:0] datain,
  output wire [0:0] dataout,  
  input wire oe
);

IOBUF iobuffElement(
  .datain(datain),
  .dataout(dataout),
  .oe(oe)
);

endmodule
