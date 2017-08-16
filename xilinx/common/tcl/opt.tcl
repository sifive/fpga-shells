# See LICENSE for license details.

# Optimize the netlist
opt_design -directive Explore

# Checkpoint the current design
write_checkpoint -force [file join $wrkdir post_opt]
