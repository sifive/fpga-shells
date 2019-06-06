# See LICENSE for license details.

# Set the variable for the directory that includes all scripts
set scriptdir [file dirname [info script]]

# Set up variables and Vivado objects
source [file join $scriptdir "preset.tcl"]

# Initialize Vivado project files
source [file join $scriptdir "init.tcl"]

puts "INFO: Recreating block diagram from $boarddir/zybo_bd.tcl"
source [file join $boarddir tcl zybo_bd.tcl]

