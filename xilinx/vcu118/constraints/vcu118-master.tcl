set group_mem [get_clocks -quiet {pll_clk*}]
set group_sys [get_clocks -quiet {sys_diff_clk                    \
                                  clk_out*_vcu118_sys_clock_mmcm0  \
                                  clk_out*_vcu118_sys_clock_mmcm1}]

set group_jtag [get_clocks -quiet {JTCK}]

puts "group_mem: $group_mem"
puts "group_sys: $group_sys"
puts "group_jtag: $group_jtag"

set groups [list]
if { [llength $group_mem]    > 0 } { lappend groups -group $group_mem }
if { [llength $group_sys]    > 0 } { lappend groups -group $group_sys }
if { [llength $group_jtag]   > 0 } { lappend groups -group $group_jtag }

puts "set_clock_groups -asynchronous $groups"
set_clock_groups -asynchronous {*}$groups
