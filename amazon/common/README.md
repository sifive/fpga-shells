## Amazon Centos FPGA image

# centos host dependencies
```
sudo yum install -y autoconf automake curl dtc libmpc-devel mpfr-devel gmp-devel gawk build-essential bison flex texinfo gperf libtool patchutils bc zlib-devel swig python-devel
```

# device tree compiler
```
git clone https://git.kernel.org/pub/scm/utils/dtc/dtc.git
cd dtc 
make -j`nproc` 
sudo make install PREFIX=/usr/local
```

# RISCV tools environment
```
sudo chown -R centos /opt/riscv/tools-09a1ffe5
export RISCV=/opt/riscv/tools-09a1ffe5
sudo mkdir -p /opt/riscv/tools-09a1ffe5
export PATH=$PATH:$RISCV/bin
```

# rocket-chip commit id 09a1ffe5afb57673e0641a86cdb94347056c5c06
```
git clone --recursive https://github.com/freechipsproject/rocket-chip.git
cd rocket-chip/riscv-tools
export MAKEFLAGS=-j`nproc`
./build.sh
```

# fpga-shells
```
git clone https://github.com/sifive/fpga-shells.git
git checkout ace47afa325b88487ba3005c0a72c163e9007ff2
cd amazon/common
git clone https://github.com/aws/aws-fpga.git
```
