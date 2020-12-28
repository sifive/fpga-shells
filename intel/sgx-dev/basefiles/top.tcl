# Copyright (C) 2020  Intel Corporation. All rights reserved.
# Your use of Intel Corporation's design tools, logic functions 
# and other software and tools, and any partner logic 
# functions, and any output files from any of the foregoing 
# (including device programming or simulation files), and any 
# associated documentation or information are expressly subject 
# to the terms and conditions of the Intel Program License 
# Subscription Agreement, the Intel Quartus Prime License Agreement,
# the Intel FPGA IP License Agreement, or other applicable license
# agreement, including, without limitation, that your use is for
# the sole purpose of programming logic devices manufactured by
# Intel and sold by Intel or its authorized distributors.  Please
# refer to the applicable agreement for further details, at
# https://fpgasoftware.intel.com/eula.

# Quartus Prime: Generate Tcl File for Project
# File: top.tcl
# Generated on: Thu Jul 30 12:00:00 2020

# Load Quartus Prime Tcl Project package
package require ::quartus::project

set need_to_close_project 0
set make_assignments 1

# Check that the right project is open
if {[is_project_open]} {
	if {[string compare $quartus(project) "top"]} {
		puts "Project top is not open"
		set make_assignments 0
	}
} else {
	# Only open if not already open
	if {[project_exists top]} {
		project_open -revision top top
	} else {
		project_new -revision top top
	}
	set need_to_close_project 1
}

# Make assignments
if {$make_assignments} {
	set_global_assignment -name FAMILY "Stratix 10"
	set_global_assignment -name DEVICE 1SG280LU2F50E2VG
	set_global_assignment -name ORIGINAL_QUARTUS_VERSION 17.1.0
	set_global_assignment -name PROJECT_CREATION_TIME_DATE "20:00:00  JULY 30, 2020"
	set_global_assignment -name LAST_QUARTUS_VERSION "20.2.0 Pro Edition"
	set_global_assignment -name PROJECT_OUTPUT_DIRECTORY output_files
	set_global_assignment -name MIN_CORE_JUNCTION_TEMP 0
	set_global_assignment -name MAX_CORE_JUNCTION_TEMP 100
	set_global_assignment -name ERROR_CHECK_FREQUENCY_DIVISOR 1
	set_global_assignment -name EDA_SIMULATION_TOOL "ModelSim-Altera (Verilog)"
	set_global_assignment -name EDA_TIME_SCALE "1 ps" -section_id eda_simulation
	set_global_assignment -name EDA_OUTPUT_DATA_FORMAT "VERILOG HDL" -section_id eda_simulation
	set_global_assignment -name VERILOG_MACRO "SYNTHESIS=1"
	set_global_assignment -name VERILOG_FILE "top.v"
	set_global_assignment -name VERILOG_FILE "iobuf_wire.v"
	set_global_assignment -name VERILOG_FILE "bootrom.v"
	set_global_assignment -name VERILOG_FILE "plusarg_reader.v"
	set_global_assignment -name VERILOG_FILE "syncResetReg.v"
	set_global_assignment -name VERILOG_FILE "sifive.freedom.sgx.min.DefaultSGXConfig.v"
	set_global_assignment -name IP_FILE "rom.ip"
	set_global_assignment -name IP_FILE "vJTAG.ip"
	set_global_assignment -name OPTIMIZATION_MODE "OPTIMIZE NETLIST FOR ROUTABILITY"
	set_global_assignment -name FAST_PRESERVE OFF -entity top
	set_instance_assignment -name IO_STANDARD "1.8 V" -to REF_CLK_PLL -entity top
	set_location_assignment PIN_BH33 -to REF_CLK_PLL
	set_location_assignment PIN_H18 -to key1
	set_instance_assignment -name IO_STANDARD "1.8 V" -to key1 -entity top
	set_location_assignment PIN_G18 -to key2
	set_instance_assignment -name IO_STANDARD "1.8 V" -to key2 -entity top
	set_location_assignment PIN_H20 -to key3
	set_instance_assignment -name IO_STANDARD "1.8 V" -to key3 -entity top
	set_location_assignment PIN_B19 -to led_0
	set_instance_assignment -name IO_STANDARD "1.8 V" -to led_0 -entity top
	set_location_assignment PIN_D18 -to led_1
	set_instance_assignment -name IO_STANDARD "1.8 V" -to led_1 -entity top
	set_location_assignment PIN_D19 -to led_2
	set_instance_assignment -name IO_STANDARD "1.8 V" -to led_2 -entity top
	set_location_assignment PIN_E17 -to led_3
	set_instance_assignment -name IO_STANDARD "1.8 V" -to led_3 -entity top
	set_location_assignment PIN_F17 -to led_4
	set_instance_assignment -name IO_STANDARD "1.8 V" -to led_4 -entity top

	# Including default assignments
	set_global_assignment -name FLOW_ENABLE_DESIGN_ASSISTANT ON -family "Stratix 10"
	set_global_assignment -name TIMING_ANALYZER_MULTICORNER_ANALYSIS ON -family "Stratix 10"
	set_global_assignment -name TDC_CCPP_TRADEOFF_TOLERANCE 0 -family "Stratix 10"
	set_global_assignment -name TIMING_ANALYZER_DO_CCPP_REMOVAL ON -family "Stratix 10"
	set_global_assignment -name PHYSICAL_SHIFT_REGISTER_INFERENCE ON -family "Stratix 10"
	set_global_assignment -name SYNTH_TIMING_DRIVEN_SYNTHESIS ON -family "Stratix 10"
	set_global_assignment -name SYNCHRONIZATION_REGISTER_CHAIN_LENGTH 3 -family "Stratix 10"
	set_global_assignment -name SYNTH_RESOURCE_AWARE_INFERENCE_FOR_BLOCK_RAM ON -family "Stratix 10"
	set_global_assignment -name USE_ADVANCED_DETAILED_LAB_LEGALITY ON -family "Stratix 10"
	set_global_assignment -name ADVANCED_PHYSICAL_SYNTHESIS_REGISTER_PACKING ON -family "Stratix 10"
	set_global_assignment -name PHYSICAL_SYNTHESIS ON -family "Stratix 10"
	set_global_assignment -name POST_ROUTE_PHYSICAL_SYNTHESIS OFF -family "Stratix 10"
	set_global_assignment -name STRATIXV_CONFIGURATION_SCHEME "ACTIVE SERIAL X4" -family "Stratix 10"
	set_global_assignment -name OPTIMIZE_HOLD_TIMING "ALL PATHS" -family "Stratix 10"
	set_global_assignment -name OPTIMIZE_MULTI_CORNER_TIMING ON -family "Stratix 10"
	set_global_assignment -name ENABLE_PHYSICAL_DSP_MERGING ON -family "Stratix 10"
	set_global_assignment -name FITTER_PACK_AGGRESSIVE_ROUTABILITY OFF -family "Stratix 10"
	set_global_assignment -name AUTO_DELAY_CHAINS ON -family "Stratix 10"
	set_global_assignment -name ENABLE_ED_CRC_CHECK ON -family "Stratix 10"
	set_global_assignment -name ALLOW_SEU_FAULT_INJECTION OFF -family "Stratix 10"
	set_global_assignment -name FITTER_RESYNTHESIS ON -family "Stratix 10"
	set_global_assignment -name FITTER_EARLY_RETIMING ON -family "Stratix 10"
	set_global_assignment -name HYPER_EARLY_RETIMER OFF -family "Stratix 10"
	set_global_assignment -name FLOW_ENABLE_HYPER_RETIMER_FAST_FORWARD OFF -family "Stratix 10"
	set_global_assignment -name HYPER_RETIMER_FAST_FORWARD_ON_HIERARCHY ON -family "Stratix 10"
	set_global_assignment -name GENERATE_PR_RBF_FILE ON -family "Stratix 10"
	set_global_assignment -name POWER_USE_DEVICE_CHARACTERISTICS TYPICAL -family "Stratix 10"
	set_global_assignment -name ACTIVE_SERIAL_CLOCK AS_FREQ_100MHZ -family "Stratix 10"

	# Commit assignments
	export_assignments

	# Close project
	if {$need_to_close_project} {
		project_close
	}
}
