module rom (
		output wire [31:0] q,       //       q.dataout
		input  wire [10:0] address, // address.address
		input  wire        clock,   //   clock.clk
		input  wire        rden     //    rden.rden
	);
endmodule

