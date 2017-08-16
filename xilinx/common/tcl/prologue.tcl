# See LICENSE for license details.

# Set the variable for the directory that includes all scripts
set scriptdir [file dirname [info script]]

# Set the variable for all the common files
set commondir [file dirname $scriptdir]

# Set the variable that points to board specific files
set boarddir [file join [file dirname $commondir] $name]

# Set the variable that points to board constraint files
set constraintsdir [file join $boarddir constraints]

# Set the variable that points to common verilog sources
set srcdir [file join $commondir vsrc]

# Creates a work directory
set wrkdir [file join [pwd] obj]

# Create the directory for IPs
set ipdir [file join $wrkdir ip]

# Set the top for the design based on an environment variable
set top $::env(FPGA_TOP_SYSTEM)

# Create an in-memory project
create_project -part $part_fpga -in_memory

# Set the board part, target language, default library, and IP directory
# paths for the current project
set_property -dict [list \
	BOARD_PART $part_board \
	TARGET_LANGUAGE {Verilog} \
	DEFAULT_LIB {xil_defaultlib} \
	IP_REPO_PATHS $ipdir \
	] [current_project]

if {[get_filesets -quiet sources_1] eq ""} {
	create_fileset -srcset sources_1
}
set obj [current_fileset]

# Add verilog files from VSRCS environment variable
if {[info exists ::env(VSRCS)]} {
  # Split string into words even with multiple consecutive spaces
  # http://wiki.tcl.tk/989
  set vsrcs [regexp -inline -all -- {\S+} $::env(VSRCS)]
  foreach vsrc $vsrcs {
    add_files -norecurse -fileset $obj $vsrc
  }
}

if {[get_filesets -quiet sim_1] eq ""} {
	create_fileset -simset sim_1
}
set obj [current_fileset -simset]

if {[get_filesets -quiet constrs_1] eq ""} {
	create_fileset -constrset constrs_1
}

set obj [current_fileset -constrset]
add_files -norecurse -fileset $obj [glob -directory $constraintsdir {*.xdc}]
