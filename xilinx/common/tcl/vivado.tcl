# See LICENSE for license details.

# Set the variable for the directory that includes all scripts
set scriptdir [file dirname [info script]]

# Set up variables and Vivado objects
source [file join $scriptdir "prologue.tcl"]

# Initialize Vivado project files
source [file join $scriptdir "init.tcl"]

# Synthesize the design
source [file join $scriptdir "synth.tcl"]

# Pre-implementation debug
if {[info exists pre_impl_debug_tcl]} {
  source [file join $scriptdir $pre_impl_debug_tcl]
}

# Post synthesis optimization
source [file join $scriptdir "opt.tcl"]

# Place the design
source [file join $scriptdir "place.tcl"]

# Route the design
source [file join $scriptdir "route.tcl"]

# Generate bitstream and save verilog netlist
source [file join $scriptdir "bitstream.tcl"]

# Post-implementation debug
if {[info exists post_impl_debug_tcl)]} {
  source [file join $scriptdir $post_impl_debug_tcl]
}

# Create reports for the current implementation
source [file join $scriptdir "report.tcl"]
