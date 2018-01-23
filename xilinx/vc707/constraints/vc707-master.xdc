#---------------Physical Constraints-----------------

#get_port_part_pins
#clk_n clk_p dip_switches_tri_i_0 dip_switches_tri_i_1 dip_switches_tri_i_2 dip_switches_tri_i_3 dip_switches_tri_i_4 dip_switches_tri_i_5 dip_switches_tri_i_6 dip_switches_tri_i_7 iic_main_scl_i iic_main_sda_i lcd_7bits_tri_o_0 lcd_7bits_tri_o_1 lcd_7bits_tri_o_2 lcd_7bits_tri_o_3 lcd_7bits_tri_o_4 lcd_7bits_tri_o_5 lcd_7bits_tri_o_6 leds_8bits_tri_o_0 leds_8bits_tri_o_1 leds_8bits_tri_o_2 leds_8bits_tri_o_3 leds_8bits_tri_o_4 leds_8bits_tri_o_5 leds_8bits_tri_o_6 leds_8bits_tri_o_7 linear_flash_addr_1 linear_flash_addr_10 linear_flash_addr_11 linear_flash_addr_12 linear_flash_addr_13 linear_flash_addr_14 linear_flash_addr_15 linear_flash_addr_16 linear_flash_addr_17 linear_flash_addr_18 linear_flash_addr_19 linear_flash_addr_2 linear_flash_addr_20 linear_flash_addr_21 linear_flash_addr_22 linear_flash_addr_23 linear_flash_addr_24 linear_flash_addr_25 linear_flash_addr_26 linear_flash_addr_3 linear_flash_addr_4 linear_flash_addr_5 linear_flash_addr_6 linear_flash_addr_7 linear_flash_addr_8 linear_flash_addr_9 linear_flash_adv_ldn linear_flash_ce_n linear_flash_dq_i_0 linear_flash_dq_i_1 linear_flash_dq_i_10 linear_flash_dq_i_11 linear_flash_dq_i_12 linear_flash_dq_i_13 linear_flash_dq_i_14 linear_flash_dq_i_15 linear_flash_dq_i_2 linear_flash_dq_i_3 linear_flash_dq_i_4 linear_flash_dq_i_5 linear_flash_dq_i_6 linear_flash_dq_i_7 linear_flash_dq_i_8 linear_flash_dq_i_9 linear_flash_oen linear_flash_wen mdc mdio_i phy_rst_out push_buttons_5bits_tri_i_0 push_buttons_5bits_tri_i_1 push_buttons_5bits_tri_i_2 push_buttons_5bits_tri_i_3 push_buttons_5bits_tri_i_4 reset rotary_inca_push_incb_tri_i_0 rotary_inca_push_incb_tri_i_1 rotary_inca_push_incb_tri_i_2 rs232_uart_rxd rs232_uart_txd sfp_rxn sfp_rxp sfp_sgmii_txn sfp_sgmii_txp sgmii_mgt_clkn sgmii_mgt_clkp sgmii_rxn sgmii_rxp sgmii_txn sgmii_txp sma_lvds_rxn sma_lvds_rxp sma_lvds_txn sma_lvds_txp sma_mgt_clkn sma_mgt_clkp sma_sfp_rxn sma_sfp_rxp sma_sfp_txn sma_sfp_txp

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

set_property BOARD_PIN {push_buttons_5bits_tri_i_0}  [get_ports btn_0]
set_property BOARD_PIN {push_buttons_5bits_tri_i_1}  [get_ports btn_1]
set_property BOARD_PIN {push_buttons_5bits_tri_i_2}  [get_ports btn_2]
set_property BOARD_PIN {push_buttons_5bits_tri_i_3}  [get_ports btn_3]
set_property BOARD_PIN {push_buttons_5bits_tri_i_4}  [get_ports btn_5]

set_property BOARD_PIN {dip_switches_tri_i_0} [get_ports sw_0]
set_property BOARD_PIN {dip_switches_tri_i_1} [get_ports sw_1]
set_property BOARD_PIN {dip_switches_tri_i_2} [get_ports sw_2]
set_property BOARD_PIN {dip_switches_tri_i_3} [get_ports sw_3]
set_property BOARD_PIN {dip_switches_tri_i_4} [get_ports sw_4]
set_property BOARD_PIN {dip_switches_tri_i_5} [get_ports sw_5]
set_property BOARD_PIN {dip_switches_tri_i_6} [get_ports sw_6]
set_property BOARD_PIN {dip_switches_tri_i_7} [get_ports sw_7]

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
	clk_out1_vc707_sys_clock_mmcm2 \
	clk_out2_vc707_sys_clock_mmcm2 \
	clk_out3_vc707_sys_clock_mmcm2 \
	clk_out4_vc707_sys_clock_mmcm2 \
	clk_out5_vc707_sys_clock_mmcm2 \
	clk_out6_vc707_sys_clock_mmcm2 \
	clk_out7_vc707_sys_clock_mmcm2 } \
  -group { \
	clk_out1_vc707_sys_clock_mmcm1 \
	clk_out2_vc707_sys_clock_mmcm1 } \
  -group [list [get_clocks -include_generated_clocks -of_objects [get_pins -hier -filter {name =~ *pcie*TXOUTCLK}]]]

