CXXSRCS := ~/hwacha-template/rocket-chip/csrc/f1-fesvr.cc
CXXFLAGS := $(CXXFLAGS) -std=c++11 -I$(RISCV)/include -I$(base_dir)/csrc -I/home/centos/hwacha-template/fpga-shells/amazon/common/aws-fpga/sdk/userspace/include
LDFLAGS := $(LDFLAGS) -L$(RISCV)/lib -Wl,-rpath,$(RISCV)/lib -lfesvr -lpthread -lfpga_mgmt -lrt -lpthread

f1_driver: $(CXXSRCS)
	g++ $(CXXFLAGS) $(LDFLAGS) $(CXXSRCS) -o $@
