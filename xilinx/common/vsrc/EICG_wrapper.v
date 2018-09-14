module EICG_wrapper(
  output out,
  input en,
  input in
);

  BUFGCE #(
    .CE_TYPE("SYNC"),
    .IS_CE_INVERTED(1'b0),
    .IS_I_INVERTED(1'b0)
  ) BUFGCE_inst (
    .O(out),
    .CE(en),
    .I(in)
  );

endmodule
