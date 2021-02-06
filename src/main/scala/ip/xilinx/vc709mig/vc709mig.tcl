  # // ElaborationArtefacts.add(
  # //   "vc709mig4gb.vivado.tcl",
  # //   """ 
  # //     create_bd_cell -type ip -vlvn xilinx.com:ip:mig_7series:3.0 vc709mig4gb -dir $ipdir -force
  # //     set_property -dict [list \
  # //     CONFIG.AXI4_INTERFACE                       {true} \
  # //     CONFIG.TARGET_FPGA                          {xc7vx690t-ffg1761/-2} \
  # //     CONFIG.C0.ControllerType                    {DDR3_SDRAM} \      
  # //     CONFIG.C0.DDR3_TimePeriod                   {1250} \
  # //     CONFIG.C0.DDR3_MemoryType                   {SODIMMs} \
  # //     CONFIG.C0.DDR3_MemoryPart                   {MT8KTF51264HZ-1G9} \
  # //     CONFIG.C0.DDR3_MemoryVoltage                {1.5V} \
  # //     CONFIG.C0.DDR3_BankMachineCnt               {4} \
  # //     CONFIG.C0.DDR3_Ordering                     {Normal} \
  # //     CONFIG.C0_S_AXI_DATA_WIDTH                  {64} \
  # //     CONFIG.C0_C_RD_WR_ARB_ALGORITHM             {RD_PRI_REG} \
  # //     CONFIG.C0_S_AXI_SUPPORTS_NARROW_BURST       {0} \
  # //     CONFIG.C0_S_AXI_ID_WIDTH                    {4} \    
  # //     CONFIG.InputClkFreq                         {200} \ 
  # //     CONFIG.C0.DDR3_BurstType                    {Sequential} \
  # //     CONFIG.C0.DDR3_OutputDriverImpedenceControl {RZQ/7} \
  # //     CONFIG.C0.DDR3_ControllerChipSelectPin      {enable}
  # //     CONFIG.C0.DDR3_OnDieTermination             {RZQ/6} \
  # //     CONFIG.UserMemoryAddressMap                 {BANK_ROW_COLUMN} \
  # //     CONFIG.System_Clock                         {No_Buffer} \
  # //     CONFIG.Reference_Clock                      {Use System Clock} \
  # //     CONFIG.System_Reset_Polarity                {ACTIVE HIGH}
  # //     CONFIG.Debug_Signal                         {Disable} \
  # //     CONFIG.IOPowerReduction                     {ON} \
  # //     CONFIG.DCI_Cascade                          {false} \
  # //     CONFIG.BankSelectionFlag                    {false} \ 
  # //     CONFIG.RESET_BOARD_INTERFACE                {Custom} \
  # //     ] [get_ips vc709mig4gb]"""
  # // )