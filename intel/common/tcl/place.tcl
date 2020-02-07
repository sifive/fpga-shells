# Joseph Tarango
# See LICENSE for license details.

# Optimize the current placed netlist
# Place the current design
# place_design -directive Explore
# Depending on the use mode, the Vivado® software provides different commands to place and route device resources into the FPGA device. In Project Mode, the launch_runs impl 1 executable performs place and route, and the equivalent Intel® Quartus® Prime Pro Edition executable is quartus_fit. In Non-Project Mode, the Vivado® software provides the place_design and route_design executables. The Intel® Quartus® Prime Pro Edition software allows you perform place and route stages separately in the quartus_fit executable through arguments.
# The Intel® Quartus® Prime Pro Edition Fitter includes the following stages:
# Plan—places all periphery elements (such as I/Os and PLLs) and determines a legal clock plan, without core placement or routing.
# Early Place—places all core elements in an approximate location to facilitate design planning. Finalizes clock planning for Intel® Stratix® 10 designs.
# Place—places all core elements in a legal location.
# Route—creates all routing between the elements in the design.
# Retime 12 —performs register retiming and moves existing registers into Hyper-Registers to increase performance by removing retiming restrictions and eliminating critical paths.
# Finalize—for Intel® Arria® 10 and Intel® Cyclone® 10 GX devices, converts unnecessary tiles to High-Speed or Low-Power. For Intel® Stratix® 10 devices, performs post-route.
# Fast Forward12—generates detailed reports that estimate performance gains achievable by making specific RTL modifications.
# You can run each Fitter stage standalone by providing the appropriate argument to the quartus_fit executable. For more information, run quartus_fit --help.
# The following example performs place-and-route by fitting the logic of the Intel® Quartus® Prime Pro Edition filtref project:
# quartus_fit filtref
# For command line help, type quartus_fit --help at the command prompt
# Xilinx cmd
#  phys_opt_design -directive Explore
quartus_fit "${top}"

# Not needed for altera
# Optimize dynamic power using intelligent clock gating
# Xilinx cmd
#  power_opt_design

# Checkpoint the current design
# Xilinx cmd
#  write_checkpoint -force [file join $wrkdir post_place]
quartus_cdb "${top}" --write_settings_files=on --post_map=on --create_companion --quartus_metadata all [file join $wrkdir post_place]