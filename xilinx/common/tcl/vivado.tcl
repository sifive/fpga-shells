# See LICENSE for license details.

# Synthesize the design
source [file join $scriptdir "synth.tcl"]

# Pre-implementation debug
if {[info exists ::env(PRE_IMPL_DEBUG_TCL)]} {
  source [file join $scriptdir $::env(PRE_IMPL_DEBUG_TCL)]
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
if {[info exists ::env(POST_IMPL_DEBUG_TCL)]} {
  source [file join $scriptdir $::env(POST_IMPL_DEBUG_TCL)]
}

# Create reports for the current implementation
source [file join $scriptdir "report.tcl"]
