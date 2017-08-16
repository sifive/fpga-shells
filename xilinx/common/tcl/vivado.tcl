# See LICENSE for license details.

# Synthesize the design
source [file join $scriptdir "synth.tcl"]

# Pre-implementation debug
source [file join $scriptdir "pre-impl-debug.tcl"]

# Post synthesis optimization
source [file join $scriptdir "opt.tcl"]

# Place the design
source [file join $scriptdir "place.tcl"]

# Route the design
source [file join $scriptdir "route.tcl"]

# Generate bitstream and save verilog netlist
source [file join $scriptdir "bitstream.tcl"]

# Post-implementation debug
source [file join $scriptdir "post-impl-debug.tcl"]

# Create reports for the current implementation
source [file join $scriptdir "report.tcl"]
