#VCU118 Xilinx 

#board part0 pins defined in
#Vivado/2017.3/data/boards/board_files/vc707/1.3/part0_pins.xml

#BOARD_PIN
#set_property BOARD_PIN {sysclk1_300_p} [get_ports sys_diff_clock_clk_p]
#set_property BOARD_PIN {sysclk1_300_n} [get_ports sys_diff_clock_clk_n]
#set_property BOARD_PIN {CPU_RESET} [get_ports reset]
#No BOARD_PIN
set_property -dict { PACKAGE_PIN G31  IOSTANDARD DIFF_SSTL12 } [get_ports sys_diff_clock_clk_p]
set_property -dict { PACKAGE_PIN F31  IOSTANDARD DIFF_SSTL12 } [get_ports sys_diff_clock_clk_n]
set_property -dict { PACKAGE_PIN L19  IOSTANDARD LVMOS12 } [get_ports reset]

create_clock -name sys_diff_clk -period 3.332 [get_ports sys_diff_clock_clk_p]
set_input_jitter [get_clocks -of_objects [get_ports sys_diff_clock_clk_p]] 0.5

#BOARD_PIN
#set_property BOARD_PIN {GPIO_LED_0_LS} [get_ports led_0]
#set_property BOARD_PIN {GPIO_LED_1_LS} [get_ports led_1]
#set_property BOARD_PIN {GPIO_LED_2_LS} [get_ports led_2]
#set_property BOARD_PIN {GPIO_LED_3_LS} [get_ports led_3]
#set_property BOARD_PIN {GPIO_LED_4_LS} [get_ports led_4]
#set_property BOARD_PIN {GPIO_LED_5_LS} [get_ports led_5]
#set_property BOARD_PIN {GPIO_LED_6_LS} [get_ports led_6]
#set_property BOARD_PIN {GPIO_LED_7_LS} [get_ports led_7]
#No BOARD_PIN
set_property -dict { PACKAGE_PIN AT32  IOSTANDARD LVCMOS12  DRIVE 8 } [get_ports led_0]
set_property -dict { PACKAGE_PIN AV34  IOSTANDARD LVCMOS12  DRIVE 8 } [get_ports led_1]
set_property -dict { PACKAGE_PIN AY30  IOSTANDARD LVCMOS12  DRIVE 8 } [get_ports led_2]
set_property -dict { PACKAGE_PIN BB32  IOSTANDARD LVCMOS12  DRIVE 8 } [get_ports led_3]
set_property -dict { PACKAGE_PIN BF32  IOSTANDARD LVCMOS12  DRIVE 8 } [get_ports led_4]
set_property -dict { PACKAGE_PIN AU37  IOSTANDARD LVCMOS12  DRIVE 8 } [get_ports led_5]
set_property -dict { PACKAGE_PIN AV36  IOSTANDARD LVCMOS12  DRIVE 8 } [get_ports led_6]
set_property -dict { PACKAGE_PIN BA37  IOSTANDARD LVCMOS12  DRIVE 8 } [get_ports led_7]


#BOARD_PIN
#set_property BOARD_PIN {GPIO_SW_C}  [get_ports btn_0]
#set_property BOARD_PIN {GPIO_SW_W}  [get_ports btn_1]
#set_property BOARD_PIN {GPIO_SW_S}  [get_ports btn_2]
#set_property BOARD_PIN {GPIO_SW_E}  [get_ports btn_3]
#set_property BOARD_PIN {GPIO_SW_N}  [get_ports btn_5]
#No BOARD_BIN
set_property -dict { PACKAGE_PIN BD23  IOSTANDARD LVCMOS18} [get_ports btn_0]
set_property -dict { PACKAGE_PIN BF22  IOSTANDARD LVCMOS18} [get_ports btn_1]
set_property -dict { PACKAGE_PIN BE22  IOSTANDARD LVCMOS18} [get_ports btn_2]
set_property -dict { PACKAGE_PIN BE23  IOSTANDARD LVCMOS18} [get_ports btn_3]
set_property -dict { PACKAGE_PIN BB24  IOSTANDARD LVCMOS18} [get_ports btn_4]


#todo was 8 DIP in vc707 now 4
#BOARD_PIN
#set_property BOARD_PIN {GPIO_DIP_SW1} [get_ports sw_0]
#set_property BOARD_PIN {GPIO_DIP_SW2} [get_ports sw_1]
#set_property BOARD_PIN {GPIO_DIP_SW3} [get_ports sw_2]
#set_property BOARD_PIN {GPIO_DIP_SW4} [get_ports sw_3]
#No BOARD_PIN
set_property -dict { PACKAGE_PIN B17  IOSTANDARD LVCMOS12} [get_ports sw_0]
set_property -dict { PACKAGE_PIN G16  IOSTANDARD LVCMOS12} [get_ports sw_1]
set_property -dict { PACKAGE_PIN J16  IOSTANDARD LVCMOS12} [get_ports sw_2]
set_property -dict { PACKAGE_PIN D21  IOSTANDARD LVCMOS12} [get_ports sw_3]

#BOARD_PIN
#set_property BOARD_PIN {USB_UART_RX} [get_ports uart_rx]
#todo was cts_n
#set_property BOARD_PIN {USB_UART_CTS} [get_ports uart_cts] 
#set_property BOARD_PIN {USB_UART_TX} [get_property uart_tx]
#todo was rts_n
#set_property BOARD_PIN {USB_UART_RTS} [get_property uart_rts]
#No BOARD_PIN
set_property -dict { PACKAGE_PIN AW25} [get_ports uart_rx]
#todo was cts_n
set_property -dict { PACKAGE_PIN BB22  IOSTANDARD LVCMOS18} [get_ports uart_cts]
set_property -dict { PACKAGE_PIN BB21} [get_ports uart_tx]
#todo was rts_n
set_property -dict { PACKAGE_PIN AY25  IOSTANDARD LVCMOS18} [get_ports uart_rts]


# Platform specific constraints
set_property IOB TRUE [get_cells "U500VCU118System/uarts_0/txm/out_reg"]
set_property IOB TRUE [get_cells "uart_rxd_sync/sync_1"]


# PCI Express
# J22 FMCP_HSPC (FMC + HSPC)
set_property PACKAGE_PIN P42 [get_ports {pcie_pci_exp_txp[0]}]
set_property PACKAGE_PIN P43 [get_ports {pcie_pci_exp_txn[0]}]
set_property PACKAGE_PIN U45 [get_ports {pcie_pci_exp_rxp[0]}]
set_property PACKAGE_PIN P46 [get_ports {pcie_pci_exp_rxn[0]}]

set_property PACKAGE_PIN M42 [get_ports {pcie_pci_exp_txp[1]}]
set_property PACKAGE_PIN M43 [get_ports {pcie_pci_exp_txn[1]}]
set_property PACKAGE_PIN R45 [get_ports {pcie_pci_exp_rxp[1]}]
set_property PACKAGE_PIN R46 [get_ports {pcie_pci_exp_rxn[1]}]

set_property PACKAGE_PIN T42 [get_ports {pcie_pci_exp_txp[2]}]
set_property PACKAGE_PIN T43 [get_ports {pcie_pci_exp_txn[2]}]
set_property PACKAGE_PIN W45 [get_ports {pcie_pci_exp_rxp[2]}]
set_property PACKAGE_PIN W46 [get_ports {pcie_pci_exp_rxn[2]}]

set_property PACKAGE_PIN K42 [get_ports {pcie_pci_exp_txp[3]}]
set_property PACKAGE_PIN K43 [get_ports {pcie_pci_exp_txn[3]}]
set_property PACKAGE_PIN N45 [get_ports {pcie_pci_exp_rxp[3]}]
set_property PACKAGE_PIN N46 [get_ports {pcie_pci_exp_rxn[3]}]

#refclk
set_property PACKAGE_PIN V38 [get_ports {pcie_REFCLK_rxp}]
set_property PACKAGE_PIN V39 [get_ports {pcie_REFCLK_rxn}]
create_clock -name pcie_ref_clk -period 10 [get_ports pcie_REFCLK_rxp]
set_input_jitter [get_clocks -of_objects [get_ports pcie_REFCLK_rxp]] 0.5

# PMODs 
# not in part_pins.xml

# PMOD0 Female
# PMOD0_0_LS AY14
# PMOD0_1_LS AY15
# PMOD0_2_LS AW15
# PMOD0_3_LS AV15
# PMOD0_4_LS AV16
# PMOD0_5_LS AU16
# PMOD0_6_LS AT15
# PMOD0_7_LS AT16

# PMOD1 Male
# PMOD1_0_LS N28
# PMOD1_1_LS M30
# PMOD1_2_LS N30
# PMOD1_3_LS P30
# PMOD1_4_LS P29
# PMOD1_5_LS L31
# PMOD1_6_LS M31
# PMOD1_7_LS R29

# todo : SDIO via PMOD correct pins, connected to random pins on PMOD0
set_property -dict { PACKAGE_PIN AY14  IOSTANDARD LVCMOS18  IOB TRUE } [get_ports {sdio_clk}]
set_property -dict { PACKAGE_PIN AY15  IOSTANDARD LVCMOS18  IOB TRUE  PULLUP TRUE } [get_ports {sdio_cmd}]
set_property -dict { PACKAGE_PIN AW15  IOSTANDARD LVCMOS18  IOB TRUE  PULLUP TRUE } [get_ports {sdio_dat[0]}]
set_property -dict { PACKAGE_PIN AV15  IOSTANDARD LVCMOS18  IOB TRUE  PULLUP TRUE } [get_ports {sdio_dat[1]}]
set_property -dict { PACKAGE_PIN AV16  IOSTANDARD LVCMOS18  IOB TRUE  PULLUP TRUE } [get_ports {sdio_dat[2]}]
set_property -dict { PACKAGE_PIN AU16  IOSTANDARD LVCMOS18  IOB TRUE  PULLUP TRUE } [get_ports {sdio_dat[3]}]

# JTAG
# todo connect to J2 FMC HPC1, connected to random pins on PMOD1
set_property CLOCK_DEDICATED_ROUTE FALSE [get_nets jtag_TCK_IBUF]
set_property -dict { PACKAGE_PIN N28  IOSTANDARD LVCMOS18  PULLUP TRUE } [get_ports {jtag_TCK}]
set_property -dict { PACKAGE_PIN M30  IOSTANDARD LVCMOS18  PULLUP TRUE } [get_ports {jtag_TMS}]
set_property -dict { PACKAGE_PIN N30  IOSTANDARD LVCMOS18  PULLUP TRUE } [get_ports {jtag_TDI}]
set_property -dict { PACKAGE_PIN P30  IOSTANDARD LVCMOS18  PULLUP TRUE } [get_ports {jtag_TDO}]



set_clock_groups -asynchronous \
  -group { clk_pll_i } \
  -group { \
	clk_out1_vc707_sys_clock_mmcm0 \
	clk_out2_vc707_sys_clock_mmcm0 \
	clk_out3_vc707_sys_clock_mmcm0 \
	clk_out4_vc707_sys_clock_mmcm0 \
	clk_out5_vc707_sys_clock_mmcm0 \
	clk_out6_vc707_sys_clock_mmcm0 \
	clk_out7_vc707_sys_clock_mmcm0 } \
  -group { \
	clk_out1_vc707_sys_clock_mmcm1 \
	clk_out2_vc707_sys_clock_mmcm1 } \
  -group [list [get_clocks -include_generated_clocks -of_objects [get_pins -hier -filter {name =~ *pcie*TXOUTCLK}]]]

