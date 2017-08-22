VIVADO ?= vivado
VIVADOFLAGS := \
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
	VSRCS="$(VSRCS)" IPVIVADOTCLS="$(IPVIVADOTCLS)" $(VIVADO) $(VIVADOFLAGS) -source $<

.PHONY: init
init: $(init)

# Generate bitstream
bit := $(FPGA_BUILD_DIR)/obj/$(FPGA_TOP_SYSTEM).bit
$(bit): $(fpga_common_script_dir)/vivado.tcl $(init)
	cd $(FPGA_BUILD_DIR) && \
	VSRCS="$(VSRCS)" $(VIVADO) $(VIVADOFLAGS) -source $<

.PHONY: bit
bit: $(bit)

# Generate mcs
mcs := $(FPGA_BUILD_DIR)/obj/system.mcs
$(mcs): $(bit)
	cd $(FPGA_BUILD_DIR) && \
	$(VIVADO) $(VIVADOFLAGS) $(fpga_common_script_dir)/write_cfgmem.tcl -tclargs $(BOARD) $@ $^ $(FLASHED_PROGRAM)

.PHONY: mcs
mcs: $(mcs)

.PHONY: clean
clean::
	rm -rf $(FPGA_BUILD_DIR)
