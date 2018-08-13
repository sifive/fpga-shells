# See LICENSE for license details.

create_ip -vendor xilinx.com -library ip -name clk_wiz -module_name mmcm -dir $ipdir -force
set_property -dict [list \
	CONFIG.PRIMITIVE {MMCM} \
	CONFIG.RESET_TYPE {ACTIVE_LOW} \
	CONFIG.CLKOUT1_USED {true} \
        CONFIG.CLKOUT2_USED {true} \
        CONFIG.CLKOUT3_USED {true} \
	CONFIG.CLKOUT1_REQUESTED_OUT_FREQ {8.388} \
        CONFIG.CLKOUT2_REQUESTED_OUT_FREQ {65.000} \
        CONFIG.CLKOUT3_REQUESTED_OUT_FREQ {32.500} \
	] [get_ips mmcm]

create_ip -vendor xilinx.com -library ip -name proc_sys_reset -module_name reset_sys -dir $ipdir -force
set_property -dict [list \
	CONFIG.C_EXT_RESET_HIGH {false} \
	CONFIG.C_AUX_RESET_HIGH {false} \
	CONFIG.C_NUM_BUS_RST {1} \
	CONFIG.C_NUM_PERP_RST {1} \
	CONFIG.C_NUM_INTERCONNECT_ARESETN {1} \
	CONFIG.C_NUM_PERP_ARESETN {1} \
	] [get_ips reset_sys]

create_ip -vendor xilinx.com -library ip -name ila -module_name ila -dir $ipdir -force
set_property -dict [list \
	CONFIG.C_NUM_OF_PROBES {1} \
	CONFIG.C_TRIGOUT_EN {false} \
	CONFIG.C_TRIGIN_EN {false} \
	CONFIG.C_MONITOR_TYPE {Native} \
	CONFIG.C_ENABLE_ILA_AXI_MON {false} \
	CONFIG.C_PROBE0_WIDTH {4} \
	CONFIG.C_PROBE10_TYPE {1} \
	CONFIG.C_PROBE10_WIDTH {32} \
	CONFIG.C_PROBE11_TYPE {1} \
	CONFIG.C_PROBE11_WIDTH {32} \
	CONFIG.C_PROBE12_TYPE {1} \
	CONFIG.C_PROBE12_WIDTH {64} \
	CONFIG.C_PROBE13_TYPE {1} \
	CONFIG.C_PROBE13_WIDTH {64} \
	CONFIG.C_PROBE14_TYPE {1} \
	CONFIG.C_PROBE14_WIDTH {97} \
	] [get_ips ila]

