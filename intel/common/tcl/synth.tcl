# Joseph Tarango
# See LICENSE for license details.

# Read the specified list of IP files
# Xilinx cmd 
#  read_ip [glob -directory $ipdir [file join * {*.xci}]]
quartus_ipgenerate --quartus-project="${top}".qpf --clear-output-directory "${top}".qsys --upgrade-ip-cores [glob -directory $ipdir [file join * {*.xci}]]

# Synthesize the design
# Xilinx cmd Switch from Xilinx ISE Software to Quartus II Software
#  synth_design -top $top -flatten_hierarchy rebuilt
quartus_syn "${top}" --recompile --analysis_and_elaboration

# Checkpoint the current design
# Xilinx cmd 
#  write_checkpoint -force [file join $wrkdir post_synth]
quartus_cdb "${top}" --write_settings_files=on --post_map=on --create_companion --quartus_metadata all [file join $wrkdir post_synth]
