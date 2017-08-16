# See LICENSE for license details.

# Route the current design
route_design -directive Explore

# Optimize the current design post routing
phys_opt_design -directive Explore

# Checkpoint the current design
write_checkpoint -force [file join $wrkdir post_route]
