// See LICENSE for license details.

module AnalogToUInt (a, b);

   parameter WIDTH = 1;

   inout [WIDTH-1:0]  a;
   output [WIDTH-1:0] b;

   assign b = a;

endmodule
