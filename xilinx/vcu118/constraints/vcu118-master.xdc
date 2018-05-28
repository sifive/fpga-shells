#-------------- MCS Generation ----------------------
set_property BITSTREAM.CONFIG.EXTMASTERCCLK_EN div-1  [current_design]
set_property BITSTREAM.CONFIG.SPI_FALL_EDGE YES       [current_design]
set_property BITSTREAM.CONFIG.SPI_BUSWIDTH 8          [current_design]
set_property BITSTREAM.GENERAL.COMPRESS TRUE          [current_design]
set_property BITSTREAM.CONFIG.UNUSEDPIN Pulldown      [current_design]
set_property CFGBVS GND                               [current_design]
set_property CONFIG_VOLTAGE 1.8                       [current_design]
set_property CONFIG_MODE SPIx8                        [current_design]


#---------------Physical Constraints-----------------

#get_port_part_pins
#clk_n clk_p dip_switches_tri_i_0 dip_switches_tri_i_1 dip_switches_tri_i_2 dip_switches_tri_i_3 dip_switches_tri_i_4 dip_switches_tri_i_5 dip_switches_tri_i_6 dip_switches_tri_i_7 iic_main_scl_i iic_main_sda_i lcd_7bits_tri_o_0 lcd_7bits_tri_o_1 lcd_7bits_tri_o_2 lcd_7bits_tri_o_3 lcd_7bits_tri_o_4 lcd_7bits_tri_o_5 lcd_7bits_tri_o_6 leds_8bits_tri_o_0 leds_8bits_tri_o_1 leds_8bits_tri_o_2 leds_8bits_tri_o_3 leds_8bits_tri_o_4 leds_8bits_tri_o_5 leds_8bits_tri_o_6 leds_8bits_tri_o_7 linear_flash_addr_1 linear_flash_addr_10 linear_flash_addr_11 linear_flash_addr_12 linear_flash_addr_13 linear_flash_addr_14 linear_flash_addr_15 linear_flash_addr_16 linear_flash_addr_17 linear_flash_addr_18 linear_flash_addr_19 linear_flash_addr_2 linear_flash_addr_20 linear_flash_addr_21 linear_flash_addr_22 linear_flash_addr_23 linear_flash_addr_24 linear_flash_addr_25 linear_flash_addr_26 linear_flash_addr_3 linear_flash_addr_4 linear_flash_addr_5 linear_flash_addr_6 linear_flash_addr_7 linear_flash_addr_8 linear_flash_addr_9 linear_flash_adv_ldn linear_flash_ce_n linear_flash_dq_i_0 linear_flash_dq_i_1 linear_flash_dq_i_10 linear_flash_dq_i_11 linear_flash_dq_i_12 linear_flash_dq_i_13 linear_flash_dq_i_14 linear_flash_dq_i_15 linear_flash_dq_i_2 linear_flash_dq_i_3 linear_flash_dq_i_4 linear_flash_dq_i_5 linear_flash_dq_i_6 linear_flash_dq_i_7 linear_flash_dq_i_8 linear_flash_dq_i_9 linear_flash_oen linear_flash_wen mdc mdio_i phy_rst_out push_buttons_5bits_tri_i_0 push_buttons_5bits_tri_i_1 push_buttons_5bits_tri_i_2 push_buttons_5bits_tri_i_3 push_buttons_5bits_tri_i_4 reset rotary_inca_push_incb_tri_i_0 rotary_inca_push_incb_tri_i_1 rotary_inca_push_incb_tri_i_2 rs232_uart_rxd rs232_uart_txd sfp_rxn sfp_rxp sfp_sgmii_txn sfp_sgmii_txp sgmii_mgt_clkn sgmii_mgt_clkp sgmii_rxn sgmii_rxp sgmii_txn sgmii_txp sma_lvds_rxn sma_lvds_rxp sma_lvds_txn sma_lvds_txp sma_mgt_clkn sma_mgt_clkp sma_sfp_rxn sma_sfp_rxp sma_sfp_txn sma_sfp_txp

set_property PACKAGE_PIN E12 [get_ports sys_diff_clock_clk_p]
set_property IOSTANDARD DIFF_SSTL12 [get_ports sys_diff_clock_clk_p]

set_property PACKAGE_PIN D12 [get_ports sys_diff_clock_clk_n]
set_property IOSTANDARD DIFF_SSTL12 [get_ports sys_diff_clock_clk_n]

set_property PACKAGE_PIN L19 [get_ports reset]
set_property IOSTANDARD LVCMOS12 [get_ports reset]

create_clock -name sys_diff_clk -period 4.0 [get_ports sys_diff_clock_clk_p]
set_input_jitter [get_clocks -of_objects [get_ports sys_diff_clock_clk_p]] 0.05

# DDR4 C0
set_property PACKAGE_PIN E13      [get_ports "ddr_c0_ddr4_act_n"]
set_property PACKAGE_PIN D14      [get_ports "ddr_c0_ddr4_adr[0]"]
set_property PACKAGE_PIN C12      [get_ports "ddr_c0_ddr4_adr[10]"]
set_property PACKAGE_PIN B13      [get_ports "ddr_c0_ddr4_adr[11]"]
set_property PACKAGE_PIN C13      [get_ports "ddr_c0_ddr4_adr[12]"]
set_property PACKAGE_PIN D15      [get_ports "ddr_c0_ddr4_adr[13]"]
set_property PACKAGE_PIN H14      [get_ports "ddr_c0_ddr4_adr[14]"]
set_property PACKAGE_PIN H15      [get_ports "ddr_c0_ddr4_adr[15]"]
set_property PACKAGE_PIN F15      [get_ports "ddr_c0_ddr4_adr[16]"]
set_property PACKAGE_PIN B15      [get_ports "ddr_c0_ddr4_adr[1]"]
set_property PACKAGE_PIN B16      [get_ports "ddr_c0_ddr4_adr[2]"]
set_property PACKAGE_PIN C14      [get_ports "ddr_c0_ddr4_adr[3]"]
set_property PACKAGE_PIN C15      [get_ports "ddr_c0_ddr4_adr[4]"]
set_property PACKAGE_PIN A13      [get_ports "ddr_c0_ddr4_adr[5]"]
set_property PACKAGE_PIN A14      [get_ports "ddr_c0_ddr4_adr[6]"]
set_property PACKAGE_PIN A15      [get_ports "ddr_c0_ddr4_adr[7]"]
set_property PACKAGE_PIN A16      [get_ports "ddr_c0_ddr4_adr[8]"]
set_property PACKAGE_PIN B12      [get_ports "ddr_c0_ddr4_adr[9]"]
set_property PACKAGE_PIN G15      [get_ports "ddr_c0_ddr4_ba[0]"]
set_property PACKAGE_PIN G13      [get_ports "ddr_c0_ddr4_ba[1]"]
set_property PACKAGE_PIN H13      [get_ports "ddr_c0_ddr4_bg"]
set_property PACKAGE_PIN E14      [get_ports "ddr_c0_ddr4_ck_c"]
set_property PACKAGE_PIN F14      [get_ports "ddr_c0_ddr4_ck_t"]
set_property PACKAGE_PIN A10      [get_ports "ddr_c0_ddr4_cke"]
set_property PACKAGE_PIN F13      [get_ports "ddr_c0_ddr4_cs_n"]
set_property PACKAGE_PIN G11      [get_ports "ddr_c0_ddr4_dm_dbi_n[0]"]
set_property PACKAGE_PIN R18      [get_ports "ddr_c0_ddr4_dm_dbi_n[1]"]
set_property PACKAGE_PIN K17      [get_ports "ddr_c0_ddr4_dm_dbi_n[2]"]
set_property PACKAGE_PIN G18      [get_ports "ddr_c0_ddr4_dm_dbi_n[3]"]
set_property PACKAGE_PIN B18      [get_ports "ddr_c0_ddr4_dm_dbi_n[4]"]
set_property PACKAGE_PIN P20      [get_ports "ddr_c0_ddr4_dm_dbi_n[5]"]
set_property PACKAGE_PIN L23      [get_ports "ddr_c0_ddr4_dm_dbi_n[6]"]
set_property PACKAGE_PIN G22      [get_ports "ddr_c0_ddr4_dm_dbi_n[7]"]
set_property PACKAGE_PIN F11      [get_ports "ddr_c0_ddr4_dq[0]"]
set_property PACKAGE_PIN M18      [get_ports "ddr_c0_ddr4_dq[10]"]
set_property PACKAGE_PIN M17      [get_ports "ddr_c0_ddr4_dq[11]"]
set_property PACKAGE_PIN N19      [get_ports "ddr_c0_ddr4_dq[12]"]
set_property PACKAGE_PIN N18      [get_ports "ddr_c0_ddr4_dq[13]"]
set_property PACKAGE_PIN N17      [get_ports "ddr_c0_ddr4_dq[14]"]
set_property PACKAGE_PIN M16      [get_ports "ddr_c0_ddr4_dq[15]"]
set_property PACKAGE_PIN L16      [get_ports "ddr_c0_ddr4_dq[16]"]
set_property PACKAGE_PIN K16      [get_ports "ddr_c0_ddr4_dq[17]"]
set_property PACKAGE_PIN L18      [get_ports "ddr_c0_ddr4_dq[18]"]
set_property PACKAGE_PIN K18      [get_ports "ddr_c0_ddr4_dq[19]"]
set_property PACKAGE_PIN E11      [get_ports "ddr_c0_ddr4_dq[1]"]
set_property PACKAGE_PIN J17      [get_ports "ddr_c0_ddr4_dq[20]"]
set_property PACKAGE_PIN H17      [get_ports "ddr_c0_ddr4_dq[21]"]
set_property PACKAGE_PIN H19      [get_ports "ddr_c0_ddr4_dq[22]"]
set_property PACKAGE_PIN H18      [get_ports "ddr_c0_ddr4_dq[23]"]
set_property PACKAGE_PIN F19      [get_ports "ddr_c0_ddr4_dq[24]"]
set_property PACKAGE_PIN F18      [get_ports "ddr_c0_ddr4_dq[25]"]
set_property PACKAGE_PIN E19      [get_ports "ddr_c0_ddr4_dq[26]"]
set_property PACKAGE_PIN E18      [get_ports "ddr_c0_ddr4_dq[27]"]
set_property PACKAGE_PIN G20      [get_ports "ddr_c0_ddr4_dq[28]"]
set_property PACKAGE_PIN F20      [get_ports "ddr_c0_ddr4_dq[29]"]
set_property PACKAGE_PIN F10      [get_ports "ddr_c0_ddr4_dq[2]"]
set_property PACKAGE_PIN E17      [get_ports "ddr_c0_ddr4_dq[30]"]
set_property PACKAGE_PIN D16      [get_ports "ddr_c0_ddr4_dq[31]"]
set_property PACKAGE_PIN D17      [get_ports "ddr_c0_ddr4_dq[32]"]
set_property PACKAGE_PIN C17      [get_ports "ddr_c0_ddr4_dq[33]"]
set_property PACKAGE_PIN C19      [get_ports "ddr_c0_ddr4_dq[34]"]
set_property PACKAGE_PIN C18      [get_ports "ddr_c0_ddr4_dq[35]"]
set_property PACKAGE_PIN D20      [get_ports "ddr_c0_ddr4_dq[36]"]
set_property PACKAGE_PIN D19      [get_ports "ddr_c0_ddr4_dq[37]"]
set_property PACKAGE_PIN C20      [get_ports "ddr_c0_ddr4_dq[38]"]
set_property PACKAGE_PIN B20      [get_ports "ddr_c0_ddr4_dq[39]"]
set_property PACKAGE_PIN F9       [get_ports "ddr_c0_ddr4_dq[3]"]
set_property PACKAGE_PIN N23      [get_ports "ddr_c0_ddr4_dq[40]"]
set_property PACKAGE_PIN M23      [get_ports "ddr_c0_ddr4_dq[41]"]
set_property PACKAGE_PIN R21      [get_ports "ddr_c0_ddr4_dq[42]"]
set_property PACKAGE_PIN P21      [get_ports "ddr_c0_ddr4_dq[43]"]
set_property PACKAGE_PIN R22      [get_ports "ddr_c0_ddr4_dq[44]"]
set_property PACKAGE_PIN P22      [get_ports "ddr_c0_ddr4_dq[45]"]
set_property PACKAGE_PIN T23      [get_ports "ddr_c0_ddr4_dq[46]"]
set_property PACKAGE_PIN R23      [get_ports "ddr_c0_ddr4_dq[47]"]
set_property PACKAGE_PIN K24      [get_ports "ddr_c0_ddr4_dq[48]"]
set_property PACKAGE_PIN J24      [get_ports "ddr_c0_ddr4_dq[49]"]
set_property PACKAGE_PIN H12      [get_ports "ddr_c0_ddr4_dq[4]"]
set_property PACKAGE_PIN M21      [get_ports "ddr_c0_ddr4_dq[50]"]
set_property PACKAGE_PIN L21      [get_ports "ddr_c0_ddr4_dq[51]"]
set_property PACKAGE_PIN K21      [get_ports "ddr_c0_ddr4_dq[52]"]
set_property PACKAGE_PIN J21      [get_ports "ddr_c0_ddr4_dq[53]"]
set_property PACKAGE_PIN K22      [get_ports "ddr_c0_ddr4_dq[54]"]
set_property PACKAGE_PIN J22      [get_ports "ddr_c0_ddr4_dq[55]"]
set_property PACKAGE_PIN H23      [get_ports "ddr_c0_ddr4_dq[56]"]
set_property PACKAGE_PIN H22      [get_ports "ddr_c0_ddr4_dq[57]"]
set_property PACKAGE_PIN E23      [get_ports "ddr_c0_ddr4_dq[58]"]
set_property PACKAGE_PIN E22      [get_ports "ddr_c0_ddr4_dq[59]"]
set_property PACKAGE_PIN G12      [get_ports "ddr_c0_ddr4_dq[5]"]
set_property PACKAGE_PIN F21      [get_ports "ddr_c0_ddr4_dq[60]"]
set_property PACKAGE_PIN E21      [get_ports "ddr_c0_ddr4_dq[61]"]
set_property PACKAGE_PIN F24      [get_ports "ddr_c0_ddr4_dq[62]"]
set_property PACKAGE_PIN F23      [get_ports "ddr_c0_ddr4_dq[63]"]
set_property PACKAGE_PIN E9       [get_ports "ddr_c0_ddr4_dq[6]"]
set_property PACKAGE_PIN D9       [get_ports "ddr_c0_ddr4_dq[7]"]
set_property PACKAGE_PIN R19      [get_ports "ddr_c0_ddr4_dq[8]"]
set_property PACKAGE_PIN P19      [get_ports "ddr_c0_ddr4_dq[9]"]
set_property PACKAGE_PIN D10      [get_ports "ddr_c0_ddr4_dqs_c[0]"]
set_property PACKAGE_PIN P16      [get_ports "ddr_c0_ddr4_dqs_c[1]"]
set_property PACKAGE_PIN J19      [get_ports "ddr_c0_ddr4_dqs_c[2]"]
set_property PACKAGE_PIN E16      [get_ports "ddr_c0_ddr4_dqs_c[3]"]
set_property PACKAGE_PIN A18      [get_ports "ddr_c0_ddr4_dqs_c[4]"]
set_property PACKAGE_PIN M22      [get_ports "ddr_c0_ddr4_dqs_c[5]"]
set_property PACKAGE_PIN L20      [get_ports "ddr_c0_ddr4_dqs_c[6]"]
set_property PACKAGE_PIN G23      [get_ports "ddr_c0_ddr4_dqs_c[7]"]
set_property PACKAGE_PIN D11      [get_ports "ddr_c0_ddr4_dqs_t[0]"]
set_property PACKAGE_PIN P17      [get_ports "ddr_c0_ddr4_dqs_t[1]"]
set_property PACKAGE_PIN K19      [get_ports "ddr_c0_ddr4_dqs_t[2]"]
set_property PACKAGE_PIN F16      [get_ports "ddr_c0_ddr4_dqs_t[3]"]
set_property PACKAGE_PIN A19      [get_ports "ddr_c0_ddr4_dqs_t[4]"]
set_property PACKAGE_PIN N22      [get_ports "ddr_c0_ddr4_dqs_t[5]"]
set_property PACKAGE_PIN M20      [get_ports "ddr_c0_ddr4_dqs_t[6]"]
set_property PACKAGE_PIN H24      [get_ports "ddr_c0_ddr4_dqs_t[7]"]
set_property PACKAGE_PIN C8       [get_ports "ddr_c0_ddr4_odt"]
set_property PACKAGE_PIN N20      [get_ports "ddr_c0_ddr4_reset_n"]

#set_property BOARD_PIN {leds_8bits_tri_o_0} [get_ports led_0]
#set_property BOARD_PIN {leds_8bits_tri_o_1} [get_ports led_1]
#set_property BOARD_PIN {leds_8bits_tri_o_2} [get_ports led_2]
#set_property BOARD_PIN {leds_8bits_tri_o_3} [get_ports led_3]
#set_property BOARD_PIN {leds_8bits_tri_o_4} [get_ports led_4]
#set_property BOARD_PIN {leds_8bits_tri_o_5} [get_ports led_5]
#set_property BOARD_PIN {leds_8bits_tri_o_6} [get_ports led_6]
#set_property BOARD_PIN {leds_8bits_tri_o_7} [get_ports led_7]

#set_property BOARD_PIN {push_buttons_5bits_tri_i_0}  [get_ports btn_0]
#set_property BOARD_PIN {push_buttons_5bits_tri_i_1}  [get_ports btn_1]
#set_property BOARD_PIN {push_buttons_5bits_tri_i_2}  [get_ports btn_2]
#set_property BOARD_PIN {push_buttons_5bits_tri_i_3}  [get_ports btn_3]
#set_property BOARD_PIN {push_buttons_5bits_tri_i_4}  [get_ports btn_5]

#set_property BOARD_PIN {dip_switches_tri_i_0} [get_ports sw_0]
#set_property BOARD_PIN {dip_switches_tri_i_1} [get_ports sw_1]
#set_property BOARD_PIN {dip_switches_tri_i_2} [get_ports sw_2]
#set_property BOARD_PIN {dip_switches_tri_i_3} [get_ports sw_3]
#set_property BOARD_PIN {dip_switches_tri_i_4} [get_ports sw_4]
#set_property BOARD_PIN {dip_switches_tri_i_5} [get_ports sw_5]
#set_property BOARD_PIN {dip_switches_tri_i_6} [get_ports sw_6]
#set_property BOARD_PIN {dip_switches_tri_i_7} [get_ports sw_7]

set_property PACKAGE_PIN AW25 [get_ports uart_rx]
set_property IOSTANDARD LVCMOS18 [get_ports uart_rx]
set_property IOB TRUE [get_ports uart_rx]
set_property PACKAGE_PIN AY25 [get_ports uart_ctsn]
set_property IOSTANDARD LVCMOS18 [get_ports uart_ctsn]
set_property IOB TRUE [get_ports uart_ctsn]
set_property PACKAGE_PIN BB21 [get_ports uart_tx]
set_property IOSTANDARD LVCMOS18 [get_ports uart_tx]
set_property IOB TRUE [get_ports uart_tx]
set_property PACKAGE_PIN BB22 [get_ports uart_rtsn]
set_property IOSTANDARD LVCMOS18 [get_ports uart_rtsn]
set_property IOB TRUE [get_ports uart_rtsn]

# Platform specific constraints
set_property IOB TRUE [get_cells "U500VCU118System/uarts_0/txm/out_reg"]
set_property IOB TRUE [get_cells "uart_rxd_sync/sync_1"]

# JTAG
set_property CLOCK_DEDICATED_ROUTE FALSE [get_nets jtag_TCK_IBUF]
set_property -dict { PACKAGE_PIN BB12  IOSTANDARD LVCMOS18  PULLUP TRUE } [get_ports {jtag_TCK}]
set_property -dict { PACKAGE_PIN BB13  IOSTANDARD LVCMOS18  PULLUP TRUE } [get_ports {jtag_TMS}]
set_property -dict { PACKAGE_PIN BB14  IOSTANDARD LVCMOS18  PULLUP TRUE } [get_ports {jtag_TDI}]
set_property -dict { PACKAGE_PIN BA14  IOSTANDARD LVCMOS18  PULLUP TRUE } [get_ports {jtag_TDO}]

# SDIO
set_property -dict { PACKAGE_PIN AV15  IOSTANDARD LVCMOS18  IOB TRUE } [get_ports {sdio_clk}]
set_property -dict { PACKAGE_PIN AY15  IOSTANDARD LVCMOS18  IOB TRUE  PULLUP TRUE } [get_ports {sdio_cmd}]
set_property -dict { PACKAGE_PIN AW15  IOSTANDARD LVCMOS18  IOB TRUE  PULLUP TRUE } [get_ports {sdio_dat[0]}]
set_property -dict { PACKAGE_PIN AV16  IOSTANDARD LVCMOS18  IOB TRUE  PULLUP TRUE } [get_ports {sdio_dat[1]}]
set_property -dict { PACKAGE_PIN AU16  IOSTANDARD LVCMOS18  IOB TRUE  PULLUP TRUE } [get_ports {sdio_dat[2]}]
set_property -dict { PACKAGE_PIN AY14  IOSTANDARD LVCMOS18  IOB TRUE  PULLUP TRUE } [get_ports {sdio_dat[3]}]

set_clock_groups -asynchronous \
  -group { pll_clk* } \
  -group { \
	clk_out1_vcu118_sys_clock_mmcm0 \
	clk_out2_vcu118_sys_clock_mmcm0 \
	clk_out3_vcu118_sys_clock_mmcm0 \
	clk_out4_vcu118_sys_clock_mmcm0 \
	clk_out5_vcu118_sys_clock_mmcm0 \
	clk_out6_vcu118_sys_clock_mmcm0 \
	clk_out7_vcu118_sys_clock_mmcm0 } \
  -group { \
	clk_out1_vcu118_sys_clock_mmcm1 \
	clk_out2_vcu118_sys_clock_mmcm1 }

