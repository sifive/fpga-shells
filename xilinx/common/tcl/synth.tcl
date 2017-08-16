# See LICENSE for license details.

# Read the specified list of IP files
read_ip [glob -directory $ipdir [file join * {*.xci}]]

# Synthesize the design
synth_design -top $top -flatten_hierarchy rebuilt

# Checkpoint the current design
write_checkpoint -force [file join $wrkdir post_synth]
