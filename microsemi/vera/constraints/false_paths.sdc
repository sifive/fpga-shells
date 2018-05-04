set_false_path -from [ get_ports { ereset_n }]

set_clock_groups -asynchronous \
	-group [ get_clocks { chiplink_b2c_clk } ] \
	-group [ get_clocks { iofpga/chiplink_rx_dll/chiplink_rx_dll_0/dll_inst_0/CLK_0 } ] \
	-group [ get_clocks { ref_clk0 } ] \
	-group [ get_clocks { hart_clk_ccc/hart_clk_ccc_0/pll_inst_0/OUT0 } ] \
	-group [ get_clocks { hart_clk_ccc/hart_clk_ccc_0/pll_inst_0/OUT1 } ] \
	-group [ get_clocks { osc_rc160mhz } ] \
	-group [ get_clocks { ref_clk_pad_p } ]

# RX: side, want to latch on the rising edge of the clock
set_input_delay -min -1 -clock {chiplink_b2c_clk} [ get_ports { chiplink_b2c_data* chiplink_b2c_rst chiplink_b2c_send } ]
set_input_delay -max 0  -clock {chiplink_b2c_clk} [ get_ports { chiplink_b2c_data* chiplink_b2c_rst chiplink_b2c_send } ]

# TX side: want clock + data to have transition on rising edge
set_output_delay -min 0 -clock {hart_clk_ccc/hart_clk_ccc_0/pll_inst_0/OUT1} [ get_ports { chiplink_c2b_data* chiplink_c2b_rst chiplink_c2b_send } ]
set_output_delay -max 1 -clock {hart_clk_ccc/hart_clk_ccc_0/pll_inst_0/OUT1} [ get_ports { chiplink_c2b_data* chiplink_c2b_rst chiplink_c2b_send } ]
