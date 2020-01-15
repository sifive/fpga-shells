// See LICENSE for license details.

module UIntToAnalog (a, b, b_en);

   parameter WIDTH = 1;

   inout [WIDTH-1:0] a;
   input [WIDTH-1:0] b;
   input             b_en;

   wire [31:0]       z32;
   
   assign z32 = 32'hZZZZZZZZ;

   assign a = b_en ? b : z32[WIDTH-1:0];

endmodule
