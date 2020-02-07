# Joseph Tarango
# See LICENSE for license details.

# Optimize the netlist
# Xilinx cmd
#  opt_design -directive Explore
# In the Intel® Quartus® Prime Pro Edition command-line flow, the quartus_syn executable performs both synthesis (synth_design) and mapping of design elements to device resources (opt_design).
# The following command runs logic synthesis and technology mapping of a design named filtref:
quartus_syn "${top}"
# Note: For command line help, type quartus_syn --help at the command prompt. In addition, you can recompile11 only the partitions that you change, instead of compiling the whole design.

# Checkpoint the current design
# In Vivado® , the write_checkpoint command allows you to save a project at any point in the design process. In the Intel® Quartus® Prime Pro Edition software, you can export the results of a compilation at various stages of compilation flow using the quartus_cdb executable.
# In addition, the quartus_cdb executable allows you to import and export version-compatible databases. This ability simplifies design migration between versions of the Intel® Quartus® Prime Pro Edition software; you can import a database from a version of the Intel® Quartus® Prime Pro Edition into another version of the software, without the need of full compilation. After import, you only need to rerun timing analysis or simulation with the updated timing models.
# Xilinx cmd
#   write_checkpoint -force [file join $wrkdir post_opt]
quartus_cdb --quartus_metadata all [file join $wrkdir post_opt]
