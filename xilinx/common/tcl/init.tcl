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

# AR 58526 <http://www.xilinx.com/support/answers/58526.html>
set xci_files [get_files -all {*.xci}]
foreach xci_file $xci_files {
  set_property GENERATE_SYNTH_CHECKPOINT {false} -quiet $xci_file
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
