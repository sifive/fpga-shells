	rom u0 (
		.q       (_connected_to_q_),       //  output,  width = 32,       q.dataout
		.address (_connected_to_address_), //   input,  width = 11, address.address
		.clock   (_connected_to_clock_),   //   input,   width = 1,   clock.clk
		.rden    (_connected_to_rden_)     //   input,   width = 1,    rden.rden
	);

