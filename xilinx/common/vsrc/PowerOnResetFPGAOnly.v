// See LICENSE file for license details.
(* keep_hierarchy = "yes" *) module PowerOnResetFPGAOnly(
  input clock,
  (* dont_touch = "true" *) output power_on_reset
);
  reg reset;
  assign power_on_reset = reset;

  initial begin
     reset <= 1'b1;
  end

  always @(posedge clock) begin
     reset <= 1'b0;
  end
endmodule
