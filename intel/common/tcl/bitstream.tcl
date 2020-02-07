# Joseph Tarango
# See LICENSE for license details.
# Write a bitstream for the current design
# Xilinx Command
#  write_bitstream -force [file join $wrkdir "${top}.bit"]
# Description
# The Vivado® software provides the write_bitstream executable to generate FPGA programming files. The Intel® Quartus® Prime software provides the quartus_asm executable to generate programming files for FPGA configuration.
# For command line help, type quartus_asm --help at the command prompt.
# The following example creates the filtref.sof programming file for the filtref project:
quartus_asm "${top}" [file join $wrkdir "${top}.sof"]

# Save the timing delays for cells in the design in SDF format
# Xilinx Command 
#   write_sdfwrite_sdf -force [file join $wrkdir "${top}.sdf"]
# Export the current netlist in verilog format
# Xilinx Command 
#   write_verilog -mode timesim -force [file join ${wrkdir} "${top}.v"]
# Description
# In Vivado® , the write_sdf executable reads data from design files, and writes timing delays in .sdf files. The write_verilog executable uses this output and generates the netlists for third-party tools. Similarly, the Intel® Quartus® Prime Pro Edition software provides the quartus_eda executable to generate netlists and other output files for use with third-party EDA tools.
# For command line help, type quartus_eda --help at the command prompt.
# The following example creates the filtref.vo simulation Verilog HDL netlist file, that you can use to simulate the filtref project with ModelSim® :
quartus_eda "${top}" --simulation=on --format=verilog --tool=modelsim [file join $wrkdir "${top}.v" "${top}.svo" ]