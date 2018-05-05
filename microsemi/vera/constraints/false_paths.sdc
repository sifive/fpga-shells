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

# Old DLL clock:
#	                      iofpga/chiplink_rx_dll/chiplink_rx_dll_0/dll_inst_0/CLK_0 } ] \

# RX side: want to latch almost anywhere except on the rising edge of the clock
# The data signals coming from Aloe have: clock - 1.2 <= transition <= clock + 0.8
# Let's add 0.6ns of safety for trace jitter+skew on both sides:
#   min = period - 1.8
#   max = period + 1.4
set_input_delay -min 6.2 -clock {chiplink_b2c_clk} [ get_ports { chiplink_b2c_data* chiplink_b2c_rst chiplink_b2c_send } ]
set_input_delay -max 9.4 -clock {chiplink_b2c_clk} [ get_ports { chiplink_b2c_data* chiplink_b2c_rst chiplink_b2c_send } ]
# phase=180 -> 3.8ns setup, -3.5ns hold .... 3.65ns => -164.25 degree
# phase=12  ->

# TX side: want to transition almost anywhere except on the rising edge of the clock
# The data signals going to Aloe must have: clock - 1.85 <= NO transition <= clock + 0.65
# Let's add 1ns of safey for trace jitter+skew on both sides:
#   min =          2.85
#   max = period - 1.65
set_output_delay -min 2.85 -clock {chiplink_c2b_clk} [ get_ports { chiplink_c2b_data* chiplink_c2b_rst chiplink_c2b_send } ]
set_output_delay -max 6.35 -clock {chiplink_c2b_clk} [ get_ports { chiplink_c2b_data* chiplink_c2b_rst chiplink_c2b_send } ]
# phase 180   -> -0.30ns setup, 1.10ns hold ... 0.7ns => +31.5 degrees
# phase 211.5 ->  0.45ns setup, 0.42ns hold
