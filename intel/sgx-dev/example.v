module FPGAChip( // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230797.2]
  output [1:0]  mem_odt, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230798.4]
  output [1:0]  mem_cs_n, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230799.4]
  output [1:0]  mem_cke, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230800.4]
  output [13:0] mem_addr, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230801.4]
  output [1:0]  mem_ba, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230802.4]
  output        mem_ras_n, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230803.4]
  output        mem_cas_n, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230804.4]
  output        mem_we_n, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230805.4]
  output [7:0]  mem_dm, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230806.4]
  inout  [1:0]  mem_clk, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230807.4]
  inout  [1:0]  mem_clk_n, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230808.4]
  inout  [63:0] mem_dq, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230809.4]
  inout  [7:0]  mem_dqs, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230810.4]
  input         clk25, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230811.4]
  input         clk27, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230812.4]
  input         clk48, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230813.4]
  input         key1, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230814.4]
  input         key2, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230815.4]
  input         key3, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230816.4]
  output        led_0, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230817.4]
  output        led_1, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230818.4]
  output        led_2, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230819.4]
  output        led_3, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230820.4]
  input         jtag_tdi, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230821.4]
  inout         jtag_tdo, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230822.4]
  input         jtag_tck, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230823.4]
  input         jtag_tms, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230824.4]
  input         uart_rx, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230825.4]
  output        uart_tx, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230826.4]
  output        sd_cs, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230827.4]
  output        sd_sck, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230828.4]
  output        sd_mosi, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230829.4]
  input         sd_miso // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230830.4]
  
  
set_global_assignment -name FAMILY "Stratix 10"
set_global_assignment -name DEVICE 1SG280HU2F50E2VG
set_global_assignment -name TOP_LEVEL_ENTITY FPGAChip
set_global_assignment -name PROJECT_CREATION_TIME_DATE "16:16:00  JULY 4, 2020"
set_global_assignment -name LAST_QUARTUS_VERSION "20.2 Pro Edition"
set_global_assignment -name PROJECT_OUTPUT_DIRECTORY output_files
set_global_assignment -name ERROR_CHECK_FREQUENCY_DIVISOR 4
set_global_assignment -name EDA_SIMULATION_TOOL "ModelSim-Altera (Verilog)"
set_global_assignment -name EDA_OUTPUT_DATA_FORMAT "VERILOG HDL" -section_id eda_simulation
set_global_assignment -name POWER_PRESET_COOLING_SOLUTION "23 MM HEAT SINK WITH 200 LFPM AIRFLOW"
set_global_assignment -name POWER_BOARD_THERMAL_MODEL "NONE (CONSERVATIVE)"
set_global_assignment -name UNIPHY_SEQUENCER_DQS_CONFIG_ENABLE ON
set_global_assignment -name OPTIMIZE_MULTI_CORNER_TIMING ON
set_global_assignment -name ECO_REGENERATE_REPORT ON
set_global_assignment -name STRATIX_DEVICE_IO_STANDARD "1.8 V"

module FPGAChip( // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230797.2]
  output [1:0]  mem_odt, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230798.4]
  output [1:0]  mem_cs_n, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230799.4]
  output [1:0]  mem_cke, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230800.4]
  output [13:0] mem_addr, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230801.4]
  output [1:0]  mem_ba, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230802.4]
  output        mem_ras_n, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230803.4]
  output        mem_cas_n, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230804.4]
  output        mem_we_n, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230805.4]
  output [7:0]  mem_dm, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230806.4]
  inout  [1:0]  mem_clk, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230807.4]
  inout  [1:0]  mem_clk_n, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230808.4]
  inout  [63:0] mem_dq, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230809.4]
  inout  [7:0]  mem_dqs, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230810.4]
set_location_assignment PIN_BH33 -to clk25
set_location_assignment PIN_J20 -to clk27
set_location_assignment PIN_J19 -to clk48
set_location_assignment PIN_H18 -to key1
set_location_assignment PIN_G18 -to key2
set_location_assignment PIN_H20 -to key3
set_location_assignment PIN_B19 -to led_0
set_location_assignment PIN_E17 -to led_1
set_location_assignment PIN_D18 -to led_2
set_location_assignment PIN_D19 -to led_3
  input         jtag_tdi, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230821.4]
  inout         jtag_tdo, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230822.4]
  input         jtag_tck, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230823.4]
  input         jtag_tms, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230824.4]
  input         uart_rx, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230825.4]
  output        uart_tx, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230826.4]
  output        sd_cs, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230827.4]
  output        sd_sck, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230828.4]
  output        sd_mosi, // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230829.4]
  input         sd_miso // @[:sifive.freedom.sgx.dev.DefaultSGXConfig.fir@230830.4]
