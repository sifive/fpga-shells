set_false_path -from [ get_ports { ereset_n }]

# The c2b_clk comes from a phase-shifted output of the PLL
create_generated_clock -name {chiplink_c2b_clk} \
	-divide_by 1 -phase 0 \
	-source [ get_pins { hart_clk_ccc/hart_clk_ccc_0/pll_inst_0/OUT2 } ] \
	[ get_ports { chiplink_c2b_clk } ]

set_clock_groups -asynchronous \
	-group [ get_clocks { chiplink_b2c_clk \
	                      iofpga/chiplink_rx_pll/chiplink_rx_pll_0/pll_inst_0/OUT1 } ] \
	-group [ get_clocks { hart_clk_ccc/hart_clk_ccc_0/pll_inst_0/OUT0 } ] \
	-group [ get_clocks { ref_clk0 \
	                      hart_clk_ccc/hart_clk_ccc_0/pll_inst_0/OUT1 \
	                      hart_clk_ccc/hart_clk_ccc_0/pll_inst_0/OUT2 \
	                      chiplink_c2b_clk } ] \
	-group [ get_clocks { osc_rc160mhz } ] \
	-group [ get_clocks { ref_clk_pad_p } ]

# RX side: want to latch almost anywhere except on the rising edge of the clock
# The data signals coming from Aloe have: clock - 1.2 <= transition <= clock + 0.8
# Let's add 0.6ns of safety for trace jitter+skew on both sides:
#   min = hold           = -1.2 - 0.6
#   max = period - setup =  0.8 + 0.6
# We add a full period because RX clock insertion adds more than a full  period of delay
set_input_delay -min 6.2 -clock {chiplink_b2c_clk} [ get_ports { chiplink_b2c_data* chiplink_b2c_rst chiplink_b2c_send } ]
set_input_delay -max 9.4 -clock {chiplink_b2c_clk} [ get_ports { chiplink_b2c_data* chiplink_b2c_rst chiplink_b2c_send } ]
# phase = 12.0 -> 0.62ns setup slack, 1.5ns hold slack

# TX side: want to transition almost anywhere except on the rising edge of the clock
# The data signals going to Aloe must have: clock - 1.85 <= NO transition <= clock + 0.65
# Let's add 0.6ns of safey for trace jitter+skew on both sides:
#   min = -hold = -0.65 - 0.6
#   max = setup =  1.85 + 0.6
set_output_delay -min -1.25 -clock {chiplink_c2b_clk} [ get_ports { chiplink_c2b_data* chiplink_c2b_rst chiplink_c2b_send } ]
set_output_delay -max  2.45 -clock {chiplink_c2b_clk} [ get_ports { chiplink_c2b_data* chiplink_c2b_rst chiplink_c2b_send } ]
# phase = 31.5 -> 0.55ns setup slack, 0.45ns hold slack
