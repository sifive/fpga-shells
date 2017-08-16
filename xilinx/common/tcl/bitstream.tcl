# See LICENSE for license details.

# Write a bitstream for the current design
write_bitstream -force [file join $wrkdir "${top}.bit"]

# Save the timing delays for cells in the design in SDF format
write_sdf -force [file join $wrkdir "${top}.sdf"]

# Export the current netlist in verilog format
write_verilog -mode timesim -force [file join ${wrkdir} "${top}.v"]
