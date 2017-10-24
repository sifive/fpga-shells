#---------------Physical Constraints-----------------

set_property BOARD_PIN {clk_p} [get_ports sys_diff_clock_clk_p]
set_property BOARD_PIN {clk_n} [get_ports sys_diff_clock_clk_n]
set_property BOARD_PIN {reset} [get_ports reset]

create_clock -name sys_diff_clk -period 5.0 [get_ports sys_diff_clock_clk_p]
set_input_jitter [get_clocks -of_objects [get_ports sys_diff_clock_clk_p]] 0.5

set_property BOARD_PIN {leds_8bits_tri_o_0} [get_ports led_0]
set_property BOARD_PIN {leds_8bits_tri_o_1} [get_ports led_1]
set_property BOARD_PIN {leds_8bits_tri_o_2} [get_ports led_2]
set_property BOARD_PIN {leds_8bits_tri_o_3} [get_ports led_3]
set_property BOARD_PIN {leds_8bits_tri_o_4} [get_ports led_4]
set_property BOARD_PIN {leds_8bits_tri_o_5} [get_ports led_5]
set_property BOARD_PIN {leds_8bits_tri_o_6} [get_ports led_6]
set_property BOARD_PIN {leds_8bits_tri_o_7} [get_ports led_7]

set_property PACKAGE_PIN AU33 [get_ports uart_rx]
set_property IOSTANDARD LVCMOS18 [get_ports uart_rx]
set_property IOB TRUE [get_ports uart_rx]
set_property PACKAGE_PIN AT32 [get_ports uart_ctsn]
set_property IOSTANDARD LVCMOS18 [get_ports uart_ctsn]
set_property IOB TRUE [get_ports uart_ctsn]
set_property PACKAGE_PIN AU36 [get_ports uart_tx]
set_property IOSTANDARD LVCMOS18 [get_ports uart_tx]
set_property IOB TRUE [get_ports uart_tx]
set_property PACKAGE_PIN AR34 [get_ports uart_rtsn]
set_property IOSTANDARD LVCMOS18 [get_ports uart_rtsn]
set_property IOB TRUE [get_ports uart_rtsn]

# Platform specific constraints
set_property IOB TRUE [get_cells "U500VC707System/uarts_0/txm/out_reg"]
set_property IOB TRUE [get_cells "uart_rxd_sync/sync_1"]

# PCI Express
#FMC 1 refclk
set_property PACKAGE_PIN A10 [get_ports {pcie_REFCLK_rxp}]
set_property PACKAGE_PIN A9 [get_ports {pcie_REFCLK_rxn}]
create_clock -name pcie_ref_clk -period 10 [get_ports pcie_REFCLK_rxp]
set_input_jitter [get_clocks -of_objects [get_ports pcie_REFCLK_rxp]] 0.5

set_property PACKAGE_PIN H4 [get_ports {pcie_pci_exp_txp}]
set_property PACKAGE_PIN H3 [get_ports {pcie_pci_exp_txn}]

set_property PACKAGE_PIN G6 [get_ports {pcie_pci_exp_rxp}]
set_property PACKAGE_PIN G5 [get_ports {pcie_pci_exp_rxn}]

# JTAG
set_property CLOCK_DEDICATED_ROUTE FALSE [get_nets jtag_TCK_IBUF]
set_property -dict { PACKAGE_PIN R32  IOSTANDARD LVCMOS18  PULLUP TRUE } [get_ports {jtag_TCK}]
set_property -dict { PACKAGE_PIN W36  IOSTANDARD LVCMOS18  PULLUP TRUE } [get_ports {jtag_TMS}]
set_property -dict { PACKAGE_PIN W37  IOSTANDARD LVCMOS18  PULLUP TRUE } [get_ports {jtag_TDI}]
set_property -dict { PACKAGE_PIN V40  IOSTANDARD LVCMOS18  PULLUP TRUE } [get_ports {jtag_TDO}]

# SDIO
set_property -dict { PACKAGE_PIN AN30  IOSTANDARD LVCMOS18  IOB TRUE } [get_ports {sdio_clk}]
set_property -dict { PACKAGE_PIN AP30  IOSTANDARD LVCMOS18  IOB TRUE  PULLUP TRUE } [get_ports {sdio_cmd}]
set_property -dict { PACKAGE_PIN AR30  IOSTANDARD LVCMOS18  IOB TRUE  PULLUP TRUE } [get_ports {sdio_dat[0]}]
set_property -dict { PACKAGE_PIN AU31  IOSTANDARD LVCMOS18  IOB TRUE  PULLUP TRUE } [get_ports {sdio_dat[1]}]
set_property -dict { PACKAGE_PIN AV31  IOSTANDARD LVCMOS18  IOB TRUE  PULLUP TRUE } [get_ports {sdio_dat[2]}]
set_property -dict { PACKAGE_PIN AT30  IOSTANDARD LVCMOS18  IOB TRUE  PULLUP TRUE } [get_ports {sdio_dat[3]}]

set_clock_groups -asynchronous \
  -group { clk_pll_i } \
  -group { \
	clk_out1_vc707clk_wiz_sync \
	clk_out2_vc707clk_wiz_sync \
	clk_out3_vc707clk_wiz_sync \
	clk_out4_vc707clk_wiz_sync \
	clk_out5_vc707clk_wiz_sync \
	clk_out6_vc707clk_wiz_sync \
	clk_out7_vc707clk_wiz_sync } \
  -group [list [get_clocks -include_generated_clocks -of_objects [get_pins -hier -filter {name =~ *pcie*TXOUTCLK}]]]
