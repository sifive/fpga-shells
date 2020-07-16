module BootROM(
  input wire [10:0] address,
  input wire clock,
  input wire me,
  input wire oe,
  output wire [31:0] q
);

wire [31:0] q_r;

rom r(
  .address(address),
  .clock(clock),
  .rden(me),
  .q(q_r)
);

assign q[31:24] = q_r[7:0];
assign q[23:16] = q_r[15:8];
assign q[15:8] = q_r[23:16];
assign q[7:0] = q_r[31:24];

endmodule
