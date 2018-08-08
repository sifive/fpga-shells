# See LICENSE for license details.

# Process command line arguments
# http://wiki.tcl.tk/1730
set ip_vivado_tcls {}

while {[llength $argv]} {
  set argv [lassign $argv[set argv {}] flag]
  switch -glob $flag {
    -top-module {
      set argv [lassign $argv[set argv {}] top]
    }
    -F {
      # This should be a simple file format with one filepath per line
      set argv [lassign $argv[set argv {}] vsrc_manifest]
    }
    -board {
      set argv [lassign $argv[set argv {}] board]
    }
    -ip-vivado-tcls {
      set argv [lassign $argv[set argv {}] ip_vivado_tcls]
    }
    -pre-impl-debug-tcl {
      set argv [lassign $argv[set argv {}] pre_impl_debug_tcl]
    }
    -post-impl-debug-tcl {
      set argv [lassign $argv[set argv {}] post_impl_debug_tcl]
    }
    default {
      return -code error [list {unknown option} $flag]
    }
  }
}

if {![info exists top]} {
  return -code error [list {--top-module option is required}]
}

if {![info exists vsrc_manifest]} {
  return -code error [list {-F option is required}]
}

if {![info exists board]} {
  return -code error [list {--board option is required}]
}

# Set the variable for all the common files
set commondir [file dirname $scriptdir]

# Set the variable that points to board specific files
set boarddir [file join [file dirname $commondir] $board]
source [file join $boarddir tcl board.tcl]

# Set the variable that points to board constraint files
set constraintsdir [file join $boarddir constraints]

# Set the variable that points to common verilog sources
set srcdir [file join $commondir vsrc]

# Creates a work directory
set wrkdir [file join [pwd] obj]

# Create the directory for IPs
set ipdir [file join $wrkdir ip]

# Create an in-memory project
create_project -part $part_fpga -force $top

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

# Add verilog files from manifest
proc load_vsrc_manifest {obj vsrc_manifest} {
  set fp [open $vsrc_manifest r]
  set files [lsearch -not -exact -all -inline [split [read $fp] "\n"] {}]
  set relative_files {}
  foreach path $files {
    if {[string match {/*} $path]} {
      lappend relative_files $path
    } elseif {![string match {#*} $path]} {
      lappend relative_files [file join [file dirname $vsrc_manifest] $path]
    }
  }
  add_files -norecurse -fileset $obj {*}$relative_files
  close $fp
}

load_vsrc_manifest $obj $vsrc_manifest

# Add IP Vivado TCL
if {$ip_vivado_tcls ne {}} {
  # Split string into words even with multiple consecutive spaces
  # http://wiki.tcl.tk/989
  set ip_vivado_tcls [regexp -inline -all -- {\S+} $ip_vivado_tcls]
}

if {[get_filesets -quiet sim_1] eq ""} {
	create_fileset -simset sim_1
}
set obj [current_fileset -simset]

if {[get_filesets -quiet constrs_1] eq ""} {
	create_fileset -constrset constrs_1
}

set obj [current_fileset -constrset]
add_files -quiet -norecurse -fileset $obj [lsort [glob -directory $constraintsdir -nocomplain {*.xdc}]]
add_files -quiet -norecurse -fileset $obj [lsort [glob -directory $constraintsdir -nocomplain {*.tcl}]]
