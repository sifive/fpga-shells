# Upload an MCS-format memory configuration file to the board.

if {$argc < 3 || $argc > 3} {
	puts $argc
	puts {Error: Invalid number of arguments}
	puts {Usage: upload_mcs.tcl board mcsfile prmfile}
	exit 1
}
lassign $argv board mcsfile prmfile

open_hw
connect_hw_server
open_hw_target

current_hw_device [get_hw_devices $board]
refresh_hw_device -update_hw_probes false [lindex [get_hw_devices $board] 0]
refresh_hw_device [lindex [get_hw_devices $board] 0]

create_hw_cfgmem -hw_device [lindex [get_hw_devices $board] 0] [lindex [get_cfgmem_parts {mt28gu01gaax1e-bpi-x16}] 0]
set_property PROGRAM.BLANK_CHECK  0 [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
set_property PROGRAM.ERASE  1 [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
set_property PROGRAM.CFG_PROGRAM  1 [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
set_property PROGRAM.VERIFY  1 [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
set_property PROGRAM.CHECKSUM  0 [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
refresh_hw_device [lindex [get_hw_devices $board] 0]

set_property PROGRAM.ADDRESS_RANGE  {use_file} [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
set_property PROGRAM.FILES [list $mcsfile ] [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
set_property PROGRAM.BPI_RS_PINS {none} [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
set_property PROGRAM.UNUSED_PIN_TERMINATION {pull-none} [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
set_property PROGRAM.BLANK_CHECK  0 [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
set_property PROGRAM.ERASE  1 [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
set_property PROGRAM.CFG_PROGRAM  1 [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
set_property PROGRAM.VERIFY  1 [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
set_property PROGRAM.CHECKSUM  0 [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
set_property PROGRAM.PRM_FILES [list $prmfile ] [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
set_property PROGRAM.ADDRESS_RANGE  {use_file} [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
set_property PROGRAM.FILES [list $mcsfile ] [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
set_property PROGRAM.PRM_FILE { $prmfile } [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
set_property PROGRAM.BPI_RS_PINS {none} [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
set_property PROGRAM.UNUSED_PIN_TERMINATION {pull-none} [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
set_property PROGRAM.BLANK_CHECK  0 [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
set_property PROGRAM.ERASE  1 [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
set_property PROGRAM.CFG_PROGRAM  1 [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
set_property PROGRAM.VERIFY  1 [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
set_property PROGRAM.CHECKSUM  0 [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]

startgroup 

if {![string equal [get_property PROGRAM.HW_CFGMEM_TYPE  [lindex [get_hw_devices $board] 0]] [get_property MEM_TYPE [get_property CFGMEM_PART [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]]]] }  {
	create_hw_bitstream -hw_device [lindex [get_hw_devices $board] 0] [get_property PROGRAM.HW_CFGMEM_BITFILE [ lindex [get_hw_devices $board] 0]];
	program_hw_devices [lindex [get_hw_devices $board] 0];
}; 
program_hw_cfgmem -hw_cfgmem [ get_property PROGRAM.HW_CFGMEM [lindex [get_hw_devices $board] 0]]
