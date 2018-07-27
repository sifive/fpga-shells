if { [llength [get_ports -quiet chiplink_b2c_clk]] > 0 } {
  create_clock -name chiplink_b2c_clock -period 10 [get_ports chiplink_b2c_clk]
  create_generated_clock -name {chiplink_c2b_clock} \
          -divide_by 1 \
          -source [ get_pins { vc707_sys_clock_mmcm0/inst/mmcm_adv_inst/CLKOUT6 } ] \
          [ get_ports { chiplink_c2b_clk } ]

  # RX side: want to latch almost anywhere except on the rising edge of the clock
  # The data signals coming from Aloe have: clock - 1.2 <= transition <= clock + 0.8
  # Let's add 0.6ns of safety for trace jitter+skew on both sides:
  #   min = hold           = - 1.2 - 0.6
  #   max = period - setup =   0.8 + 0.6
  set_input_delay -min -1.8 -clock {chiplink_b2c_clock} [ get_ports { chiplink_b2c_data* chiplink_b2c_rst chiplink_b2c_send } ]
  set_input_delay -max  1.4 -clock {chiplink_b2c_clock} [ get_ports { chiplink_b2c_data* chiplink_b2c_rst chiplink_b2c_send } ]

  # TX side: want to transition almost anywhere except on the rising edge of the clock
  # The data signals going to Aloe must have: clock - 1.85 <= NO transition <= clock + 0.65
  # Let's add 1ns of safey for trace jitter+skew on both sides:
  #   min = -hold = -0.65 - 0.6
  #   max = setup =  1.85 + 0.6
  set_output_delay -min -1.25 -clock {chiplink_c2b_clock} [ get_ports { chiplink_c2b_data* chiplink_c2b_rst chiplink_c2b_send } ]
  set_output_delay -max  2.45 -clock {chiplink_c2b_clock} [ get_ports { chiplink_c2b_data* chiplink_c2b_rst chiplink_c2b_send } ]
}

set group_mem [get_clocks -quiet {clk_pll_i}]
set group_sys [get_clocks -quiet {sys_diff_clk                    \
                                  clk_out*_vc707_sys_clock_mmcm1  \
                                  clk_out*_vc707_sys_clock_mmcm2  \
                                  chiplink_c2b_clock}]
set group_cl  [get_clocks -quiet {chiplink_b2c_clock              \
                                  clk_out*_vc707_sys_clock_mmcm3}]
set group_pci [get_clocks -quiet {userclk1 txoutclk}]

set group_jtag [get_clocks -quiet {JTCK}]

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
if { [llength $group_jtag]   > 0 } { lappend groups -group $group_jtag }

puts "set_clock_groups -asynchronous $groups"
set_clock_groups -asynchronous {*}$groups
