# See LICENSE for license details.

namespace eval ::program::boards {}

set ::program::boards::spec [dict create \
	arty            [dict create  iface spix4   size 16   bitaddr 0x0        memdev {n25q128-3.3v-spi-x1_x2_x4}] \
	arty_a7_100     [dict create  iface spix4   size 16   bitaddr 0x0        memdev {s25fl128sxxxxxx0-spi-x1_x2_x4}] \
	vc707           [dict create  iface bpix16  size 128  bitaddr 0x3000000  ] \
	vcu118          [dict create  iface spix8   size 256  bitaddr 0x0        memdev {mt25qu01g-spi-x1_x2_x4_x8}]]
