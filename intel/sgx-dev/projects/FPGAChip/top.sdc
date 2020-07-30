# (C) 2001-2019 Intel Corporation. All rights reserved.
# Your use of Intel Corporation's design tools, logic functions and other 
# software and tools, and its AMPP partner logic functions, and any output 
# files from any of the foregoing (including device programming or simulation 
# files), and any associated documentation or information are expressly subject 
# to the terms and conditions of the Intel Program License Subscription 
# Agreement, Intel FPGA IP License Agreement, or other applicable 
# license agreement, including, without limitation, that your use is for the 
# sole purpose of programming logic devices manufactured by Intel and sold by 
# Intel or its authorized distributors.  Please refer to the applicable 
# agreement for further details.
#
# Revision    Date         Quartus version      Comment 
# ========    ====         ===============      =======
# 1.0         15-Dec-17    17.1                 Initial release
# 2.0         04-Oct-19    19.3                 Add set_clock_group constraints for REF_CLK_PLL, REF_CLK and altera_reserved_tck.
#                                               Add derive_clock_uncertainty & set_false_path constraints.

set_time_format -unit ns -decimal_places 3

#**************************************************************
# Create Clock
#**************************************************************
set REF_CLK_PLL [get_clocks {pll_0|iopll_0_refclk}]
set REF_CLK [get_clocks {qsys_top_0|triple_speed_ethernet_0|qsys_top_eth_tse_0|i_lvdsio_0|core|arch_inst|pll_inst|internal_pll_refclk}]

create_clock -name {altera_reserved_tck} [get_ports { altera_reserved_tck }] -period 24MHz

#**************************************************************
# Set Clock Uncertainty
#**************************************************************
derive_clock_uncertainty

#**************************************************************
# Set Clock Groups
#**************************************************************
set_clock_groups -asynchronous \
-group $REF_CLK \
-group $REF_CLK_PLL

# From Timequest cookbook
set_clock_groups -exclusive -group [get_clocks altera_reserved_tck]

#**************************************************************
# Set False Path
#**************************************************************
set_false_path -to [get_registers {pll_0|iopll_0|stratix10_altera_iopll_i|s10_iopll.fourteennm_pll~pll_e_reg__nff}]