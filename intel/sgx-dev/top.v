module  top
(
    //Clock, Reset, GPIO(LED, DIP, PB...)
    input           clk_fpga_50m,                   //1.8V - 50MHz
    input   [ 3:0]  user_dipsw,                     //1.8V - User DIP Switches
    output  [ 3:0]  user_led_g,                     //1.8V - User LEDs                                      
    //Ethernet                                      
    input           enet_rx_p,                      //LVDS SGMII RX Data
    output          enet_tx_p,                      //LVDS SGMII TX Data
    output          enet_intn,                      //1.8V, need level translator
    output          enet_resetn,                    //1.8V, need level translator
    output          enet_mdc,                       //1.8V, need level translator
    inout           enet_mdio                       //1.8V, need level translator  
);

    // JTAG
    //wire jtag_tdi; // input
    //wire jtag_tdo; // input
    //wire jtag_tck; // input
    //wire jtag_tms; // input

	 //Ethernet                                      
    //assign          enet_intn=1'b0;
    //assign          enet_resetn = 1'b0;
    //assign          enet_mdc = 1'b0;
    //assign          enet_mdio = 1'bz;

OBUF oBufa(
.datain(1'b0),
.dataout(enet_intn)
);
OBUF oBufb(
.datain(1'b0),
.dataout(enet_resetn)
);
OBUF oBufc(
.datain(1'b0),
.dataout(enet_mdc)
);
OBUF oBufd(
.datain(1'b0),
.dataout(enet_mdio)
);


//SCJTAG uJTAG( //altera_soft_core_jtag_io(
  .tdi(jtag_tdi),
  .tdo(jtag_tdo),
  .tck(jtag_tck),
  .tms(jtag_tms)
);


FPGAChip uFPGACHIP(
.clk25(clk_fpga_50m), // input
.clk27(clk_fpga_50m), // input
.clk48(clk_fpga_50m), // input
.key1(user_dipsw[0]), // input
.key2(user_dipsw[1]), // input
.key3(user_dipsw[2]), // input
.led_0(user_led_g[0]),// output
.led_1(user_led_g[1]),// output
.led_2(user_led_g[2]),// output
.led_3(user_led_g[3]),// output
.jtag_tdi(jtag_tdi),  // input
.jtag_tdo(jtag_tdo),  // input
.jtag_tck(jtag_tck),  // input
.jtag_tms(jtag_tms),  // input
.uart_rx(enet_tx_p),  // input
.uart_tx(enet_rx_n)   // output
);

endmodule
