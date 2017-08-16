# See LICENSE for license details.
#
# Create an MCS-format memory configuration file from a bitstream and an
# optional data file.

set script_program_dir [file dirname [info script]]
source [file join $script_program_dir {boards.tcl}]

if {$argc < 3 || $argc > 4} {
	puts $argc
	puts {Error: Invalid number of arguments}
	puts {Usage: write_cfgmem.tcl board mcsfile bitfile [datafile]}
	exit 1
}
lassign $argv board mcsfile bitfile datafile

if {![dict exists $::program::boards::spec $board]} {
	puts {Unsupported board}
	exit 1
}
set board [dict get $::program::boards::spec $board]

write_cfgmem -format mcs -interface [dict get $board iface] -size [dict get $board size] \
	-loadbit "up [dict get $board bitaddr] $bitfile" \
	-loaddata [expr {$datafile ne "" ? "up 0x400000 $datafile" : ""}] \
	-file $mcsfile -force
