// Joseph Tarango
module  s10_fpga_golden_top
(
    //Clock, Reset, GPIO(LED, DIP, PB...)
    input           clk_fpga_50m,                   //1.8V - 50MHz
    input           clk_fpga_b3l_p,                 //LVDS - 100MHz (Programmable Si5338)
    input           clk_enet_p,                     //LVDS - 125MHz (Ethernet)
    input           clk_hilo_p,                     //LVDS - 133MHz (EMIF)
    input           cpu_resetn,                     //1.8V - CPU Reset Pushbutton
    input   [ 3:0]  user_dipsw,                     //1.8V - User DIP Switches
    input   [ 2:0]  user_pb,                        //1.8V - User Pushbuttons
    output  [ 3:0]  user_led_g,                     //1.8V - User LEDs
    output  [ 3:0]  user_led_r,                     //1.8V - User LEDs
    inout           lt_io_scl,                      //1.8V - SCL
    inout           lt_io_sda,                      //1.8V - SDA
    output          sma_clkout_p,                   //LVDS - SMA Clock Output
    output          sma_clkout_n,                   //LVDS - SMA Clock Output
    inout           max5_clk,                       //1.8V
    inout   [ 3:0]  max5_ben,                       //1.8V 
    inout           max5_oen,                       //1.8V
    inout           max5_csn,                       //1.8v
    inout           max5_wen,                       //1.8V

    //USB
    inout   [ 7:0]  usb_data,                       //1.8V from Max10
    inout   [ 1:0]  usb_addr,                       //1.8V from Max10
    inout           usb_fpga_clk,                   //1.8V - Need level translator from 3.3V from Cypress USB
    output          usb_empty,                      //1.8V from Max10
    output          usb_full,                       //1.8V from Max10
    input           usb_oen,                        //1.8V from Max10
    input           usb_rdn,                        //1.8V from Max10
    input           usb_resetn,                     //1.8V from Max10
    inout           usb_scl,                        //1.8V from Max10
    inout           usb_sda,                        //1.8V from Max10
    input           usb_wrn,                        //1.8V from Max10 
                                                    
    //Ethernet                                      
    input           enet_rx_p,                      //LVDS SGMII RX Data
    output          enet_tx_p,                      //LVDS SGMII TX Data
    output          enet_intn,                      //1.8V, need level translator
    output          enet_resetn,                    //1.8V, need level translator
    output          enet_mdc,                       //1.8V, need level translator
    inout           enet_mdio,                      //1.8V, need level translator  

    //EMIF                                          //Variable voltage to support DDR3, DDR4, RLD3, QDR4
    output  [31:0]  mem_addr_cmd,                   //addr_cmd
    output          mem_clk_p,                      //clk_p
    output          mem_clk_n,                      //clk_n
    inout   [ 8:0]  mem_dq_addr_cmd,                //dq_addr_cmd
    inout           mem_dqs_addr_cmd_p,             //dqs_addr_cmd_p
    inout           mem_dqs_addr_cmd_n,             //dqs_addr_cmd_n
    inout   [ 3:0]  mem_dma,                        //dma
    inout   [33:0]  mem_dqa,                        //dqa
    inout   [ 3:0]  mem_dqsa_p,                     //dqsa_p
    inout   [ 3:0]  mem_dqsa_n,                     //dqsa_n
    inout   [ 1:0]  mem_qka_p,                      //qka_p        
    inout   [ 3:0]  mem_dmb,                        //dmb
    inout   [33:0]  mem_dqb,                        //dqb
    inout   [ 3:0]  mem_dqsb_p,                     //dqsb_p
    inout   [ 3:0]  mem_dqsb_n,                     //dqsb_n
    inout   [ 1:0]  mem_qkb_p,                      //qkb_p
    input           rzq_b2m,                        //rzq

    //Flash
    output  [26:1]  flash_addr,                     //1.8V - flash addr
    inout   [15:0]  flash_data,                     //1.8V - flash data
    output          flash_advn,                     //1.8V - flash advn
    output  [ 1:0]  flash_cen,                      //1.8V - flash cen
    output          flash_clk,                      //1.8V - flash clk
    output          flash_oen,                      //1.8V - flash oen
    input   [ 1:0]  flash_rdybsyn,                  //1.8V - flash rdybsyn
    output          flash_resetn,                   //1.8V - flash resetn
    output          flash_wen,                      //1.8V - flash wen
  
    //Transceiver Reference Clock
    input           refclk_qsfp1_p,                 //LVDS - 644.53125MHz
    input           refclk_sdi_p,                   //LVDS - 148.5MHz
    input           sdi_refclk_sma_p,               //LVDS - From SMA
    input   [ 1:0]  fmca_gbtclk_m2c_p,              //LVDS - From FMC
    input           refclk_fmca_p,                  //LVDS - 625MHz
    input           refclk4_p,                      //LVDS - 156.25MHz 
    input           pcie_ob_refclk_p,               //LVDS - 100MHz
    input           pcie_edge_refclk_p,             //LVDS - From PCIe
    input           refclk1_p,                      //LVDS - 155.52MHz
    input           refclk_dp_p,                    //LVDS - 135MHz
  
    //QSFP
    input   [ 3:0]  qsfp1_rx_p,                     //QSFP XCVR RX Data
    output  [ 3:0]  qsfp1_tx_p,                     //QSFP XCVR TX Data
    input           qsfp1_interruptn,               //1.8V, need level translator from 3.3V
    output          qsfp1_lp_mode,                  //1.8V, need level translator from 3.3V
    input           qsfp1_mod_prsn,                 //1.8V, need level translator from 3.3V
    output          qsfp1_mod_seln,                 //1.8V, need level translator from 3.3V
    output          qsfp1_rstn,                     //1.8V, need level translator from 3.3V
    output          qsfp1_scl,                      //1.8V, need level translator from 3.3V
    inout           qsfp1_sda,                      //1.8V, need level translator from 3.3V  
  
    //SDI
    input           sdi_rx_p,                       //SDI XCVR RX Data
    output          sdi_tx_p,                       //SDI XCVR TX Data
    output          sdi_mf0_bypass,                 //1.8V, need level translator from 3.3V
    output          sdi_mf1_auto_sleep,             //1.8V, need level translator from 3.3V
    output          sdi_mf2_mute,                   //1.8V, need level translator from 3.3V
    output          sdi_tx_sd_hdn,                  //1.8V, need level translator from 3.3V  
    output          sdi_clk148_up,                  //Voltage Control for SDI VCXO
    output          sdi_clk148_down,                //Voltage Control for SDI VCXO  
  
    //FMC 
    input   [15:0]  fmca_dp_m2c_p,                  //Transceiver Data FPGA RX
    output  [15:0]  fmca_dp_c2m_p,                  //Transceiver Data FPGA TX       
    input   [ 1:0]  fmca_clk_m2c_p,                 //LVDS - Dedicated Clock Input
    input   [ 1:0]  fmca_la_rx_clk_p,               //LVDS - Clock Input
                                                    
    input   [14:0]  fmca_la_rx_p,                   //LVDS
    input   [14:0]  fmca_la_rx_n,                   //LVDS  
    output  [16:0]  fmca_la_tx_p,                   //LVDS
    output  [16:0]  fmca_la_tx_n,                   //LVDS         
    inout   [ 1:0]  fmca_ga,                        //1.8V
    input           fmca_prsntn,                    //1.8V
    inout           fmca_scl,                       //1.8V
    inout           fmca_sda,                       //1.8V
    output          fmca_rx_led,                    //1.8V
    output          fmca_tx_led,                    //1.8V  

    //PCIe 
    input   [15:0]  pcie_rx_p,                      //PCIe Receive Data-req's OCT
    output  [15:0]  pcie_tx_p,                      //PCIe Transmit Data            
    input           pcie_perstn,                    //1.8V - PCIe Reset 
    inout           pcie_smbclk,                    //2.5V - SMBus Clock
    inout           pcie_smbdat,                    //2.5V - SMBus Data
    output          pcie_waken,                     //2.5V - PCIe Wake-Up                
    output          pcie_led_g3,                    //1.8V - User LED - Labeled Gen3
    output          pcie_led_g2,                    //1.8V - User LED - Labeled Gen2
    output          pcie_led_x1,                    //1.8V - User LED - Labeled x1
    output          pcie_led_x4,                    //1.8V - User LED - Labeled x4
    output          pcie_led_x8,                    //1.8V - User LED - Labeled x8

    //Display Port 
    output  [ 3:0]  dp_ml_lane_p,                   //XCVR data out.
    input           dp_aux_ch_p,                    //Input LVDS, Output Diff SSTL-1.8V (BLVDS)
    input           dp_aux_ch_n,                    //Input LVDS, Output Diff SSTL-1.8V (BLVDS)
    input           dp_hot_plug,                    //1.8V, need level translator
    inout           dp_config1,                     //1.8V, need level translator
    inout           dp_config2,                     //1.8V, need level translator
    output          dp_aux_tx_drv_out,              //1.8V, need level translator
    input           dp_aux_tx_drv_in,               //1.8V, need level translator
    output          dp_aux_tx_drv_oe                //1.8V, need level translator
);                                            

endmodule
