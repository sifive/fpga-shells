# * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
# * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
# * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
# * THE CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
# * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
# * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
# * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
# * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
# * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
# * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
# *
# * @file Makefile
# * @author Skynet
# * @brief Core Generator.
# * @benchmark: N/A
# * @researcher: Joseph David Tarango
# * @date: JULY 18, 2020
###########################################################################
# To generate the everything, type "make all".
# To run other commands type "make <command>". I.E. "make turnin".
# To remove the executable and any extraneous files type "make clean" at
# the command prompt.
###########################################################################
# More information
#  Board information 
#   https://fpgacloud.intel.com/devstore/board/
#  Stratix 10 Board 
#   https://fpgacloud.intel.com/devstore/platform/?board=98
#   I.E. https://fpgacloud.intel.com/devstore/platform/19.3.0/Pro/intel-stratix-10-h-tile-cvp-example-design-for-initialization-mode/
# https://rocketboards.org/foswiki/Projects/Stratix10PCIeRootPortWithMSI
# Getting started information
# Installing Quartus 
#  https://www.intel.com/content/dam/www/programmable/us/en/pdfs/literature/manual/quartus_install.pdf
# Introduction 
#  https://youtu.be/bwoyQ_RnaiA
#  https://www.intel.com/content/dam/www/programmable/us/en/pdfs/literature/hb/qts/qts_qii5v1.pdf
#  https://www.intel.com/content/dam/www/programmable/us/en/pdfs/literature/hb/qts/qsys_intro.pdf
#  https://www.intel.com/content/www/us/en/programmable/documentation/sbc1513987577203.html
# Intel Altera to Xilinx Map
#  https://www.intel.com/content/www/us/en/programmable/documentation/mtr1422491996806.html 
#  https://rocketboards.org/foswiki/Documentation/S10SoCNewUsers
#  https://www.intel.com/content/www/us/en/programmable/education/webcasts/all/wc-2009-xilinx-ise-to-quartus-software.html
#  https://www.xilinx.com/support/documentation/sw_manuals/ug1192-xilinx-design-for-intel.pdf
# Command line Scripting
#  https://youtu.be/01oz-hJ0mDU
# TCL Scripting 
#  https://www.intel.com/content/dam/www/programmable/us/en/pdfs/literature/manual/tclscriptrefmnl.pdf
#  https://www.intel.com/content/dam/www/programmable/us/en/pdfs/literature/hb/qts/qts_qii52003.pdf
#  https://www.intel.com/content/altera-www/global/en_us/index/support/support-resources/design-examples/design-software/tcl.html
#  generate_tcl_file <name> [-c | compile] [-s | simulate] [-b | build] [-w | overwrite]
# Make Example
#  https://github.com/mfischer/Altera-Makefile
# Timing Reference
#  https://www.intel.cn/content/dam/altera-www/global/zh_CN/pdfs/literature/wp/wp_timinganalysis.pdf
###########################################################################
# Tool information
###########################################################################

ifndef ALTERAPATH
	$(info Intel quartus compile flow tested on 20.2) 
	$(info Please ensure you have the tools installed.)
    $(info Intel FPGA setup) 	
	$(info ALTERAD_LICENSE_FILE=123456@PresidentSkroob.PlanetSpaceball.com)
	$(info export ALTERAPATH=/opt/intel/FPGA_pro/20.2)
	$(info export INTELFPGAOCLSDKROOT=${ALTERAPATH}/hld)
	$(info export ALTERAOCLSDKROOT=${ALTERAPATH}/hld)
	$(info export QSYS_ROOTDIR="${ALTERAPATH}/qsys/bin")
	$(info export QUARTUS_ROOTDIR=${ALTERAPATH}/quartus)
	$(info export QUARTUS_ROOTDIR_OVERRIDE=${QUARTUS_ROOTDIR})
	$(info export PATH=${PATH}:${ALTERAPATH}/quartus/bin)
	$(info export PATH=${PATH}:${ALTERAPATH}/nios2eds/bin)
	$(info export PATH=${PATH}:${ALTERAPATH}/modelsim_ase/bin)
	$(info export PATH=${PATH}:${ALTERAPATH}/quartus/sopc_builder/bin)
	$(info export PATH=${PATH}:${QSYS_ROOTDIR})
	$(info Altera GUI Command: ${ALTERAPATH}/qpro --64bit)
    $(info Optional - Intel Compiler Setup)
	$(info INTEL_LICENSE_FILE=1@DarkHelmet.PlanetSpaceball.com)
	$(info ISS_ROOT=/opt/intel/system_studio_2020)
	$(info INTEL_LIB_PATH=${ISS_ROOT}/lib/intel64_lin)
	$(info CPATH=${ISS_ROOT}/compilers_and_libraries/linux/include)
	$(info CPATH_MORE=${ISS_ROOT}/compilers_and_libraries/linux/daal/include)
	$(info DAALROOT=${ISS_ROOT}/compilers_and_libraries/linux/daal)
	$(info TBBROOT=${ISS_ROOT}/compilers_and_libraries/linux/tbb)
	$(info IE_COMPILER=${ISS_ROOT}/compilers_and_libraries/linux/bin/intel64/icc)
	$(info source ${ISS_ROOT}/bin/compilervars.sh -arch intel64 -platform linux)
	$(info source ${ISS_ROOT}/iss_env.sh)
	$(info Optional - RISC-V Tools)
	$(info export JAVA_HOME=/opt/jdk1.8.0_192)
	$(info export PATH=${PATH}:${JAVA_HOME}/bin)
	$(info export RISCV=${RISCV_GCCHOME})
	$(info export RISCV_GCCHOME=/opt/RISC-V/tools/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14)
	$(info export RISCV_OCDHOME=/opt/RISC-V/riscv-openocd-0.10.0-2019.02.0-x86_64-linux-ubuntu14)
	$(info export RISCV_OPENOCD=${RISCV_OCDHOME})
endif

###############################################################################
# Path Setup
###############################################################################
path := $(patsubst %/,%,$(dir $(abspath $(lastword $(MAKEFILE_LIST)))))
cwd  := $(shell pwd)
CURRENT_PATH := $(subst $(lastword $(notdir $(MAKEFILE_LIST))),,$(subst $(space),\$(space),$(shell realpath '$(strip $(MAKEFILE_LIST))')))

###############################################################################
# Exe files
###############################################################################
CP := /usr/bin/cp
MD := /usr/bin/mkdir
RM := /usr/bin/rm
TR := /usr/bin/tar

###############################################################################
# Clean folders and files
###############################################################################
QTMP_DIRS=qpb output_files synth_dumps tmp-clearbox

CLEAN_FILES=plusarg_reader.v \
plusarg_reader.v \
AsyncResetReg.v \
iobuf_wire.v \
bootrom.v \
$(PRJ_TOP_NAME).v

###############################################################################
# FPGA files and ip
###############################################################################
FPGA_BASEDIR=../../fpga-shells/intel/sgx-dev/basefiles/
DEST_DIR=$(CURRENT_PATH)

# Intel IP generated files.
IP_FILES=$(FPGA_BASEDIR)vJTAG.ip \
$(FPGA_BASEDIR)rom.ip

# Intel IP generated directory.
IP_DIRS=$(FPGA_BASEDIR)vJTAG \
$(FPGA_BASEDIR)rom

# Intel top project name.
PRJ_TOP_NAME=top
QTCL_FILE=$(PRJ_TOP_NAME).tcl

# Verilog files needed
VRC_FILES=../../rocket-chip/src/main/resources/vsrc/plusarg_reader.v \
../../rocket-chip/src/main/resources/vsrc/plusarg_reader.v \
../../rocket-chip/src/main/resources/vsrc/AsyncResetReg.v \
../../fpga-shells/intel/common/vsrc/iobuf_wire.v \
../../fpga-shells/intel/common/vsrc/bootrom.v

###############################################################################
# Target
.DEFAULT_GOAL := all
###############################################################################
all: clean copybase copyfiles
	$(info DEBUG INFO)
	quartus_sh  --flow compile $(PRJ_TOP_NAME).qpf
	quartus_sh  -t $(QTCL_FILE) --flow compile $(PRJ_TOP_NAME)
	quartus_syn -t $(QTCL_FILE) $(PRJ_TOP_NAME)
	quartus_fit -t $(QTCL_FILE) $(PRJ_TOP_NAME)
	quartus_asm -t $(QTCL_FILE) $(PRJ_TOP_NAME)
	quartus_sta -t $(QTCL_FILE) $(PRJ_TOP_NAME)
	quartus_eda -t $(QTCL_FILE) $(PRJ_TOP_NAME) --simulation --tool=modelsim --format=verilog
.PHONY: all

# Copy base files
copybase:
	$(CP) -Rfav $(FPGA_BASEDIR)/. $(DEST_DIR)
.PHONY: copybase

# Copy HDL files.
copyfiles: copybase
	$(CP) -fv $(VRC_FILES) $(BUILD_DIR)
.PHONY: copyfiles

# Clean up in this folder
clean:
	$(RM) -rf  $(QTMP_DIRS)
.PHONY: clean

# Create a tar ball for project
turnin:
	$(TR) -czvf $(PRJ_TOP_NAME) *
.PHONY: turnin
