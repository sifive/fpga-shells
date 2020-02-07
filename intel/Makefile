################################################################ 
# Project Configuration: 
# 
# Specify the name of the design (project) and compiler setting
# (csf) and the list of source files used.
# Joseph Tarango
################################################################

QUARTUS ?= quartus
QUARTUSFLAGS := \
	-nojournal -mode batch \
	-source $(fpga_board_script_dir)/board.tcl \
	-source $(fpga_common_script_dir)/prologue.tcl

# Path to a program in raw binary format to be flashed into the address that the
# bootrom jumps to.
# FIXME: This variable should probably be communicated by a higher-level Makefile
FLASHED_PROGRAM ?=

# Init project
init = $(FPGA_BUILD_DIR)/.init
$(init): $(fpga_common_script_dir)/init.tcl
	mkdir -p $(FPGA_BUILD_DIR) && \
	cd $(FPGA_BUILD_DIR) && \
	VSRCS="$(VSRCS)" IPQUARTUSTCLS="$(IPQUARTUSTCLS)" $(QUARTUS) $(QUARTUSFLAGS) -source $<

.PHONY: init
init: $(init)

# Generate bitstream
bit := $(FPGA_BUILD_DIR)/obj/$(FPGA_TOP_SYSTEM).bit
$(bit): $(fpga_common_script_dir)/quartus.tcl $(init)
	cd $(FPGA_BUILD_DIR) && \
	VSRCS="$(VSRCS)" $(QUARTUS) $(QUARTUSFLAGS) -source $<

.PHONY: bit
bit: $(bit)

# Generate mcs
mcs := $(FPGA_BUILD_DIR)/obj/system.mcs
$(mcs): $(bit)
	cd $(FPGA_BUILD_DIR) && \
	$(QUARTUS) $(QUARTUSFLAGS) $(fpga_common_script_dir)/write_cfgmem.tcl -tclargs $(BOARD) $@ $^ $(FLASHED_PROGRAM)

.PHONY: mcs
mcs: $(mcs)

.PHONY: clean
clean::
	rm -rf $(FPGA_BUILD_DIR)
