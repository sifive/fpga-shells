# Creating a Design Checkpoint (DCP) from RTL

Generate verilog using `make verilog`
(Make sure `<TopVerilogModule>Wrapper.sv` is included in verilog sources --- this is needed to interface with the Amazon shell's [0:0] packed vectors for some SDRAM control signals)

Clone aws-fpga repo (or navigate to existing aws-fpga repo)

```bash
(on AMI)
$ git clone https://github.com/aws/aws-fpga.git
$ cd aws-fpga
```

Set up CL directory and environment variables

```bash
(on AMI)
$ source hdk_setup.sh
$ cd $HDK_DIR/cl/developer_designs
$ mkdir <configname>
$ cd !$
$ export CL_DIR=$(pwd)
$ source $HDK_DIR/cl/developer_designs/prepare_new_cl.sh
```
\***note**: Every login must source `hdk_setup.sh` and set `CL_DIR`

Copy verilog files to `design` directory (assuming `aws-fpga` is cloned into user's home directory)

```bash
(on build machine)
$ mkdir vsources
$ cp <configname>/verilog/<FullConfigName>/*.v vsources
$ cp <configname>/memgen/<FullConfigName>.rams.v vsources
$ cp <configname>/romgen/<FullConfigName>.roms.v vsources
$ scp vsources/*.v user@ami:aws-fpga/hdk/cl/developer_designs/<configname>/design
```

Enter `build/scripts` directory and set up scripts

```bash
(on AMI)
$ cd $CL_DIR/build/scripts
$ mv synth_hello_world.tcl synth_<TopVerilogModule>.tcl
$ ln -sf $HDK_DIR/common/shell_stable/build/scripts/aws_build_dcp_from_cl.sh
```

Modify `encrypt.tcl` to source RTL. Replace the lines that copy source files with the following snippet

```tcl
set VSOURCES [glob $CL_DIR/design/*.{v,sv,vh}]
foreach VSOURCE $VSOURCES {
  file copy -force $VSOURCE $TARGET_DIR
}
```

Replace glob pattern for encryption command to include RTL sources and remove encrypt command for .vhd sources

```tcl
glob -nocomplain -- $TARGET_DIR/*.{v,vh,sv}
```

\***note**: if synthesis and/or place and route is failing, it may be helpful to comment out the line to encrypt the user RTL.
Encryption is only needed for AFI generation

Modify `create_dcp_from_cl.tcl`, assigning the toplevel design name (`F1VU9PShell`) to `CL_Module`:

```bash
set CL_Module <TopVerilogModule>
```

Also add the following line to the command-line arguments section:

```tcl
set VDEFINES [lindex $argv 13]
```

Modify `synth_<TopVerilogModule>.tcl` to source verilog files
Replace the glob pattern for the user RTL files with

```tcl
read_verilog -sv [glob $ENC_SRC_DIR/*.{v,sv}]
```

If using the `sh_ddr` module, a significant amount of rework needs to be done on the IP/verilog includes in the AWS shell/IP sourcing section.
Replace everything from `# ----- End of section replaced by user ------` to `puts "AWS FPGA: Reading AWS constraints";` with the following `read` commands:

```tcl
#Read IP for virtual jtag / ILA/VIO
read_ip [ list \
  $HDK_SHELL_DESIGN_DIR/ip/ila_0/ila_0.xci\
  $HDK_SHELL_DESIGN_DIR/ip/cl_debug_bridge/cl_debug_bridge.xci \
  $HDK_SHELL_DESIGN_DIR/ip/ila_vio_counter/ila_vio_counter.xci \
  $HDK_SHELL_DESIGN_DIR/ip/vio_0/vio_0.xci
]

#Read DDR IP
read_ip [ list \
  $HDK_SHELL_DESIGN_DIR/ip/ddr4_core/ddr4_core.xci
]

#Read AWS Design files
read_verilog -sv [ list \
  $HDK_SHELL_DESIGN_DIR/lib/lib_pipe.sv \
  $HDK_SHELL_DESIGN_DIR/lib/bram_2rw.sv \
  $HDK_SHELL_DESIGN_DIR/lib/flop_fifo.sv \
  $HDK_SHELL_DESIGN_DIR/sh_ddr/synth/sync.v \
  $HDK_SHELL_DESIGN_DIR/sh_ddr/synth/flop_ccf.sv \
  $HDK_SHELL_DESIGN_DIR/sh_ddr/synth/ccf_ctl.v \
  $HDK_SHELL_DESIGN_DIR/sh_ddr/synth/mgt_acc_axl.sv \
  $HDK_SHELL_DESIGN_DIR/sh_ddr/synth/mgt_gen_axl.sv \
  $HDK_SHELL_DESIGN_DIR/sh_ddr/synth/sh_ddr.sv \
  $HDK_SHELL_DESIGN_DIR/interfaces/cl_ports.vh
]

#Read IP for axi register slices
read_ip [ list \
  $HDK_SHELL_DESIGN_DIR/ip/src_register_slice/src_register_slice.xci \
  $HDK_SHELL_DESIGN_DIR/ip/dest_register_slice/dest_register_slice.xci \
  $HDK_SHELL_DESIGN_DIR/ip/axi_clock_converter_0/axi_clock_converter_0.xci \
  $HDK_SHELL_DESIGN_DIR/ip/axi_register_slice/axi_register_slice.xci \
  $HDK_SHELL_DESIGN_DIR/ip/axi_register_slice_light/axi_register_slice_light.xci
]

# Additional IP's that might be needed if using the DDR
read_bd [ list \
  $HDK_SHELL_DESIGN_DIR/ip/cl_axi_interconnect/cl_axi_interconnect.bd
]
```

Finally, in `$CL_DIR/design`

```bash
$ mv cl_template_defines.vh <TopVerilogModule>_defines.vh
$ rm cl_template.sv
```

Modify `<TopVerilogModule>_defines.vh`, replacing the tick-define for `CL_NAME`:

```verilog
`define CL_NAME <TopVerilogModule>
```
