if {$argc < 1 || $argc > 1} {
	puts $argc
	puts {Error: Invalid number of arguments}
	puts {Usage: boot.tcl board}
	exit 1
}
lassign $argv board

open_hw
connect_hw_server
open_hw_target
current_hw_device [get_hw_devices $board]
boot_hw_device  [lindex [get_hw_devices $board] 0]