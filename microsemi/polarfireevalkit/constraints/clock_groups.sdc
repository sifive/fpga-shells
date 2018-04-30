#set_clock_groups -name {Coreplex} -logically_exclusive -group [ get_clocks { hart_clk_ccc/hart_clk_ccc_0/pll_inst_0/OUT0 } ]
set_clock_groups -name {PCIe_AXI} -logically_exclusive -group [ get_clocks { hart_clk_ccc/hart_clk_ccc_0/pll_inst_0/OUT1 } ]
set_clock_groups -name {DDR_subsystem} -logically_exclusive -group [ get_clocks { iofpga/polarfireddr/island/blackbox/CCC_0/pll_inst_0/OUT1 } ]


