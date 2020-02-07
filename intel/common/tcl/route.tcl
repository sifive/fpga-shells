# Joseph Tarango
# See LICENSE for license details.

# Route the current design
# Xilinx cmd
#  route_design -directive Explore
quartus_fit --route

# Not needed for altera
# Optimize the current design post routing
# Xilinx cmd
#  phys_opt_design -directive Explore

# Checkpoint the current design
# Xilinx cmd
#  write_checkpoint -force [file join $wrkdir post_route]
quartus_cdb "${top}" --write_settings_files=on --post_map=on --create_companion --quartus_metadata all [file join $wrkdir post_place]
