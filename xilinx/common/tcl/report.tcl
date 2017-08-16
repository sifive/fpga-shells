# See LICENSE for license details.

# Create a report directory
set rptdir [file join $wrkdir report]
file mkdir $rptdir

# Create a datasheet for the current design
report_datasheet -file [file join $rptdir datasheet.txt]

# Report utilization of the current device
set rptutil [file join $rptdir utilization.txt]
report_utilization -hierarchical -file $rptutil

# Report information about clock nets in the design
report_clock_utilization -file $rptutil -append

# Report the RAM resources utilized in the implemented design
report_ram_utilization -file $rptutil -append -detail

# Report timing summary for a max of 10 paths per group
report_timing_summary -file [file join $rptdir timing.txt] -max_paths 10

# Report the highest fanout of nets in the implemented design
report_high_fanout_nets -file [file join $rptdir fanout.txt] -timing -load_types -max_nets 25

# Run DRC
report_drc -file [file join $rptdir drc.txt]

# Report details of the IO banks in the design
report_io -file [file join $rptdir io.txt]

# Report a table of all clocks in the design
report_clocks -file [file join $rptdir clocks.txt]

# Fail loudly if timing not met
#
# We would ideally elevate critical warning Route 35-39 to an error, but it is
# currently not being emitted with our flow for some reason.
# https://forums.xilinx.com/t5/Implementation/Making-timing-violations-fatal-to-the-Vivado-build/m-p/716957#M15979
set timing_slack [get_property SLACK [get_timing_paths]]
if {$timing_slack < 0} {
  puts "Failed to meet timing by $timing_slack, see [file join $rptdir timing.txt]"
  exit 1
}
