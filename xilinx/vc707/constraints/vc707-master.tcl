if { [llength [get_ports -quiet chiplink_b2c_clk]] > 0 } {
  create_clock -name chiplink_b2c_clock -period 10 [get_ports chiplink_b2c_clk]
}

set group_mem [get_clocks -quiet {clk_pll_i}]
set group_sys [get_clocks -quiet {sys_diff_clk                    \
                                  clk_out*_vc707_sys_clock_mmcm1  \
                                  clk_out*_vc707_sys_clock_mmcm2}]
set group_cl  [get_clocks -quiet {chiplink_b2c_clock              \
                                  clk_out*_vc707_sys_clock_mmcm3}]
set group_pci [get_clocks -quiet -include_generated_clocks -of_objects [get_pins -hier -filter {name =~ *pcie*TXOUTCLK}]]

set group_jtag [get_clocks -quiet {jtag_TCK}]

puts "group_mem: $group_mem"
puts "group_sys: $group_sys"
puts "group_pci: $group_pci"
puts "group_cl:  $group_cl"
puts "group_jtag: $group_jtag"

set groups [list]
if { [llength $group_mem]    > 0 } { lappend groups -group $group_mem }
if { [llength $group_sys]    > 0 } { lappend groups -group $group_sys }
if { [llength $group_pci]    > 0 } { lappend groups -group $group_pci }
if { [llength $group_cl]     > 0 } { lappend groups -group $group_cl }
if { [llength $group_cjtag]  > 0 } { lappend groups -group $group_jtag }

puts "set_clock_groups -asynchronous $groups"
set_clock_groups -asynchronous {*}$groups
