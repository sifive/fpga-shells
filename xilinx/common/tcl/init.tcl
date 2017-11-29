# See LICENSE for license details.

# Include helper functions
source [file join $scriptdir "util.tcl"]

# Create the diretory for IPs
file mkdir $ipdir

# Update the IP catalog
update_ip_catalog -rebuild

# Generate IP implementations. Vivado TCL emitted from Chisel Blackboxes
foreach ip_vivado_tcl $ip_vivado_tcls {
  source $ip_vivado_tcl
}
# Optional board-specific ip script
set boardiptcl [file join $boarddir tcl ip.tcl]
if {[file exists $boardiptcl]} {
  source $boardiptcl
}

#report_ip_status

# Turn off OOC (out-of-context synthesis) 
# AR 58526 <http://www.xilinx.com/support/answers/58526.html>
#set_property GENERATE_SYNTH_CHECKPOINT {false} [get_files -all {*.xci}]
# Does not work for new "subsystem" IPs wihch contain multiple XCI files
# Need to apply properyt to "top level" XCIs only
set ip_component_names [get_property CONFIG.Component_Name [get_ips]]
foreach ip_component_name $ip_component_names {
  set xci_extension {.xci}
  set ip_xci $ip_component_name$xci_extension
  set_property GENERATE_SYNTH_CHECKPOINT {false} [get_files $ip_xci]
}

# Get a list of IPs in the current design
set obj [get_ips]

# Generate target data for the inlcuded IPs in the design
generate_target all $obj

# Export the IP user files
export_ip_user_files -of_objects $obj -no_script -force

# Get the list of active source and constraint files
set obj [current_fileset]

#Xilinx bug workaround
#scrape IP tree for directories containing .vh files
#[get_property include_dirs] misses all IP core subdirectory includes if user has specified -dir flag in create_ip
set property_include_dirs [get_property include_dirs $obj]

# Include generated files for the IPs in the design
set ip_include_dirs [concat $property_include_dirs [findincludedir $ipdir "*.vh"]]
set ip_include_dirs [concat $ip_include_dirs [findincludedir $srcdir "*.h"]]
set ip_include_dirs [concat $ip_include_dirs [findincludedir $srcdir "*.vh"]]
