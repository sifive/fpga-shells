#-------------- MCS Generation ----------------------
set_property CFGBVS GND                               [current_design]
set_property CONFIG_VOLTAGE 1.8                       [current_design]

set_property EXTRACT_ENABLE YES                       [get_cells dut_/spi_0_1/mac/phy/txd_reg*]
set_property EXTRACT_ENABLE YES                       [get_cells dut_/spi_0_1/mac/phy/sck_reg]
