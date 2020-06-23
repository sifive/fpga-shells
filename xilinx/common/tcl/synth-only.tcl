# See LICENSE for license details.

# Set the variable for the directory that includes all scripts
set scriptdir [file dirname [info script]]

# Set up variables and Vivado objects
source [file join $scriptdir "prologue.tcl"]

# Initialize Vivado project files
source [file join $scriptdir "init.tcl"]

# Synthesize the design
source [file join $scriptdir "synth.tcl"]
