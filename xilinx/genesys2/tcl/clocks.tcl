set group_sys [get_clocks -quiet {sys_clock}]

create_clock -add -name JTCK -period 100 -waveform {0 50} [get_ports {jtag_TCK}]
set group_jtag [get_clocks -quiet {JTCK}]

puts "group_sys: $group_sys"
puts "group_jtag: $group_jtag"

set groups [list]
if { [llength $group_sys]  > 0 } { lappend groups -group $group_sys  }
if { [llength $group_jtag] > 0 } { lappend groups -group $group_jtag }

puts "set_clock_groups -asynchronous $groups"
set_clock_groups -asynchronous {*}$groups
