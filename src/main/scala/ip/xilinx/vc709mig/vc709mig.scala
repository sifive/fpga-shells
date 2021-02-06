// See LICENSE for license details.
package sifive.fpgashells.ip.xilinx.vc709mig

import Chisel._
import chisel3.experimental.{Analog,attach}
import freechips.rocketchip.util.{ElaborationArtefacts}
import freechips.rocketchip.util.GenericParameterizedBundle
import freechips.rocketchip.config._

// IP VLNV: xilinx.com:customize_ip:vc709mig:4.1
// Black Box

class VC709MIGIODDR(depth : BigInt) extends GenericParameterizedBundle(depth) {
  require((depth<=0x100000000L),"VC709MIGIODDR supports upto 4GB depth configuraton")
  val ddr3_addr             = Bits(OUTPUT,16)
  val ddr3_ba               = Bits(OUTPUT,3)
  val ddr3_ras_n            = Bool(OUTPUT)
  val ddr3_cas_n            = Bool(OUTPUT)
  val ddr3_we_n             = Bool(OUTPUT)
  val ddr3_reset_n          = Bool(OUTPUT)
  val ddr3_ck_p             = Bits(OUTPUT,1)
  val ddr3_ck_n             = Bits(OUTPUT,1)
  val ddr3_cke              = Bits(OUTPUT,1)
  val ddr3_cs_n             = Bits(OUTPUT,1)
  val ddr3_odt              = Bits(OUTPUT,1)
  val ddr3_dm               = Bits(OUTPUT,8)

  val ddr3_dq               = Analog(64.W)
  val ddr3_dqs_n            = Analog(8.W)
  val ddr3_dqs_p            = Analog(8.W)
}

//reused directly in io bundle for sifive.blocks.devices.xilinxvc709mig
trait VC709MIGIOClocksReset extends Bundle {
  //inputs
  //"NO_BUFFER" clock source (must be connected to IBUF outside of IP)
  val sys_clk_i             = Bool(INPUT)
  //user interface signals
  val ui_clk                = Clock(OUTPUT)
  val ui_clk_sync_rst       = Bool(OUTPUT)
  val mmcm_locked           = Bool(OUTPUT)
  val aresetn               = Bool(INPUT)
  //misc
  val init_calib_complete   = Bool(OUTPUT)
  val sys_rst               = Bool(INPUT)
}

object vc709mig
{
  var vc709migNo = 0
  def alloc = {
    vc709migNo += 1
    vc709migNo
  }
  def last = {
    vc709mig.vc709migNo - 1
  }
}

//scalastyle:off
//turn off linter: blackbox name must match verilog module
class vc709mig(depth : BigInt)(implicit val p:Parameters) extends BlackBox
{
  require((depth<=0x100000000L), "vc709mig supports upto 4GB depth configuraton")
  require((vc709mig.alloc <= 2), "vc709mig supports upto two memory controllers")

  val index = vc709mig.last
  override def desiredName = Seq("vc709mig_a", "vc709mig_b")(index)

    val io = new VC709MIGIODDR(depth) with VC709MIGIOClocksReset {
        // User interface signals
        val app_sr_req            = Bool(INPUT)
        val app_ref_req           = Bool(INPUT)
        val app_zq_req            = Bool(INPUT)
        val app_sr_active         = Bool(OUTPUT)
        val app_ref_ack           = Bool(OUTPUT)
        val app_zq_ack            = Bool(OUTPUT)
        //axi_s
        //slave interface write address ports
        val s_axi_awid            = Bits(INPUT,4)
        val s_axi_awaddr          = Bits(INPUT,32)
        val s_axi_awlen           = Bits(INPUT,8)
        val s_axi_awsize          = Bits(INPUT,3)
        val s_axi_awburst         = Bits(INPUT,2)
        val s_axi_awlock          = Bits(INPUT,1)
        val s_axi_awcache         = Bits(INPUT,4)
        val s_axi_awprot          = Bits(INPUT,3)
        val s_axi_awqos           = Bits(INPUT,4)
        val s_axi_awvalid         = Bool(INPUT)
        val s_axi_awready         = Bool(OUTPUT)
        //slave interface write data ports
        val s_axi_wdata           = Bits(INPUT,64)
        val s_axi_wstrb           = Bits(INPUT,8)
        val s_axi_wlast           = Bool(INPUT)
        val s_axi_wvalid          = Bool(INPUT)
        val s_axi_wready          = Bool(OUTPUT)
        //slave interface write response ports
        val s_axi_bready          = Bool(INPUT)
        val s_axi_bid             = Bits(OUTPUT,4)
        val s_axi_bresp           = Bits(OUTPUT,2)
        val s_axi_bvalid          = Bool(OUTPUT)
        //slave interface read address ports
        val s_axi_arid            = Bits(INPUT,4)
        val s_axi_araddr          = Bits(INPUT,32)
        val s_axi_arlen           = Bits(INPUT,8)
        val s_axi_arsize          = Bits(INPUT,3)
        val s_axi_arburst         = Bits(INPUT,2)
        val s_axi_arlock          = Bits(INPUT,1)
        val s_axi_arcache         = Bits(INPUT,4)
        val s_axi_arprot          = Bits(INPUT,3)
        val s_axi_arqos           = Bits(INPUT,4)
        val s_axi_arvalid         = Bool(INPUT)
        val s_axi_arready         = Bool(OUTPUT)
        //slave interface read data ports
        val s_axi_rready          = Bool(INPUT)
        val s_axi_rid             = Bits(OUTPUT,4)
        val s_axi_rdata           = Bits(OUTPUT,64)
        val s_axi_rresp           = Bits(OUTPUT,2)
        val s_axi_rlast           = Bool(OUTPUT)
        val s_axi_rvalid          = Bool(OUTPUT)
        //misc
	      val device_temp_i         = Bits(INPUT,12)
        // val device_temp           = Bits(OUTPUT,12)
    }

   val vc709mig_a = """ {<?xml version='1.0' encoding='UTF-8'?>
<!-- IMPORTANT: This is an internal file that has been generated by the MIG software. Any direct editing or changes made to this file may result in unpredictable behavior or data corruption. It is strongly advised that users do not edit the contents of this file. Re-run the MIG GUI with the required settings if any of the options provided below need to be altered. -->
<Project NoOfControllers="1" >
    <ModuleName>vc709mig_a</ModuleName>
    <dci_inouts_inputs>1</dci_inouts_inputs>
    <dci_inputs>1</dci_inputs>
    <Debug_En>OFF</Debug_En>
    <DataDepth_En>1024</DataDepth_En>
    <LowPower_En>ON</LowPower_En>
    <XADC_En>Disabled</XADC_En>
    <TargetFPGA>xc7vx690t-ffg1761/-2</TargetFPGA>
    <Version>4.1</Version>
    <SystemClock>No Buffer</SystemClock>
    <ReferenceClock>Use System Clock</ReferenceClock>
    <SysResetPolarity>ACTIVE HIGH</SysResetPolarity>
    <BankSelectionFlag>FALSE</BankSelectionFlag>
    <InternalVref>0</InternalVref>
    <dci_hr_inouts_inputs>50 Ohms</dci_hr_inouts_inputs>
    <dci_cascade>0</dci_cascade>
    <Controller number="0" >
        <MemoryDevice>DDR3_SDRAM/SODIMMs/MT8KTF51264HZ-1G9</MemoryDevice>
        <TimePeriod>1250</TimePeriod>
        <VccAuxIO>2.0V</VccAuxIO>
        <PHYRatio>4:1</PHYRatio>
        <InputClkFreq>200</InputClkFreq>
        <UIExtraClocks>0</UIExtraClocks>
        <MMCM_VCO>800</MMCM_VCO>
        <MMCMClkOut0> 1.000</MMCMClkOut0>
        <MMCMClkOut1>1</MMCMClkOut1>
        <MMCMClkOut2>1</MMCMClkOut2>
        <MMCMClkOut3>1</MMCMClkOut3>
        <MMCMClkOut4>1</MMCMClkOut4>
        <DataWidth>64</DataWidth>
        <DeepMemory>1</DeepMemory>
        <DataMask>1</DataMask>
        <ECC>Disabled</ECC>
        <Ordering>Normal</Ordering>
        <BankMachineCnt>4</BankMachineCnt>
        <CustomPart>FALSE</CustomPart>
        <NewPartName></NewPartName>
        <RowAddress>16</RowAddress>
        <ColAddress>10</ColAddress>
        <BankAddress>3</BankAddress>
        <MemoryVoltage>1.5V</MemoryVoltage>
        <UserMemoryAddressMap>BANK_ROW_COLUMN</UserMemoryAddressMap>
        <PinSelection>
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="A20" SLEW="" name="ddr3_addr[0]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="B21" SLEW="" name="ddr3_addr[10]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="B17" SLEW="" name="ddr3_addr[11]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="A15" SLEW="" name="ddr3_addr[12]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="A21" SLEW="" name="ddr3_addr[13]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="F17" SLEW="" name="ddr3_addr[14]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="E17" SLEW="" name="ddr3_addr[15]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="B19" SLEW="" name="ddr3_addr[1]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="C20" SLEW="" name="ddr3_addr[2]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="A19" SLEW="" name="ddr3_addr[3]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="A17" SLEW="" name="ddr3_addr[4]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="A16" SLEW="" name="ddr3_addr[5]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="D20" SLEW="" name="ddr3_addr[6]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="C18" SLEW="" name="ddr3_addr[7]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="D17" SLEW="" name="ddr3_addr[8]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="C19" SLEW="" name="ddr3_addr[9]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="D21" SLEW="" name="ddr3_ba[0]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="C21" SLEW="" name="ddr3_ba[1]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="D18" SLEW="" name="ddr3_ba[2]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="K17" SLEW="" name="ddr3_cas_n" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15" PADName="E18" SLEW="" name="ddr3_ck_n[0]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15" PADName="E19" SLEW="" name="ddr3_ck_p[0]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="K19" SLEW="" name="ddr3_cke[0]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="J17" SLEW="" name="ddr3_cs_n[0]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="M13" SLEW="" name="ddr3_dm[0]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="K15" SLEW="" name="ddr3_dm[1]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="F12" SLEW="" name="ddr3_dm[2]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="A14" SLEW="" name="ddr3_dm[3]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="C23" SLEW="" name="ddr3_dm[4]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="D25" SLEW="" name="ddr3_dm[5]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="C31" SLEW="" name="ddr3_dm[6]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="F31" SLEW="" name="ddr3_dm[7]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="N14" SLEW="" name="ddr3_dq[0]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="H13" SLEW="" name="ddr3_dq[10]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="J13" SLEW="" name="ddr3_dq[11]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="L16" SLEW="" name="ddr3_dq[12]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="L15" SLEW="" name="ddr3_dq[13]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="H14" SLEW="" name="ddr3_dq[14]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="J15" SLEW="" name="ddr3_dq[15]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="E15" SLEW="" name="ddr3_dq[16]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="E13" SLEW="" name="ddr3_dq[17]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="F15" SLEW="" name="ddr3_dq[18]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="E14" SLEW="" name="ddr3_dq[19]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="N13" SLEW="" name="ddr3_dq[1]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="G13" SLEW="" name="ddr3_dq[20]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="G12" SLEW="" name="ddr3_dq[21]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="F14" SLEW="" name="ddr3_dq[22]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="G14" SLEW="" name="ddr3_dq[23]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="B14" SLEW="" name="ddr3_dq[24]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="C13" SLEW="" name="ddr3_dq[25]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="B16" SLEW="" name="ddr3_dq[26]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="D15" SLEW="" name="ddr3_dq[27]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="D13" SLEW="" name="ddr3_dq[28]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="E12" SLEW="" name="ddr3_dq[29]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="L14" SLEW="" name="ddr3_dq[2]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="C16" SLEW="" name="ddr3_dq[30]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="D16" SLEW="" name="ddr3_dq[31]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="A24" SLEW="" name="ddr3_dq[32]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="B23" SLEW="" name="ddr3_dq[33]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="B27" SLEW="" name="ddr3_dq[34]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="B26" SLEW="" name="ddr3_dq[35]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="A22" SLEW="" name="ddr3_dq[36]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="B22" SLEW="" name="ddr3_dq[37]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="A25" SLEW="" name="ddr3_dq[38]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="C24" SLEW="" name="ddr3_dq[39]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="M14" SLEW="" name="ddr3_dq[3]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="E24" SLEW="" name="ddr3_dq[40]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="D23" SLEW="" name="ddr3_dq[41]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="D26" SLEW="" name="ddr3_dq[42]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="C25" SLEW="" name="ddr3_dq[43]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="E23" SLEW="" name="ddr3_dq[44]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="D22" SLEW="" name="ddr3_dq[45]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="F22" SLEW="" name="ddr3_dq[46]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="E22" SLEW="" name="ddr3_dq[47]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="A30" SLEW="" name="ddr3_dq[48]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="D27" SLEW="" name="ddr3_dq[49]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="M12" SLEW="" name="ddr3_dq[4]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="A29" SLEW="" name="ddr3_dq[50]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="C28" SLEW="" name="ddr3_dq[51]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="D28" SLEW="" name="ddr3_dq[52]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="B31" SLEW="" name="ddr3_dq[53]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="A31" SLEW="" name="ddr3_dq[54]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="A32" SLEW="" name="ddr3_dq[55]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="E30" SLEW="" name="ddr3_dq[56]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="F29" SLEW="" name="ddr3_dq[57]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="F30" SLEW="" name="ddr3_dq[58]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="F27" SLEW="" name="ddr3_dq[59]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="N15" SLEW="" name="ddr3_dq[5]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="C30" SLEW="" name="ddr3_dq[60]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="E29" SLEW="" name="ddr3_dq[61]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="F26" SLEW="" name="ddr3_dq[62]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="D30" SLEW="" name="ddr3_dq[63]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="M11" SLEW="" name="ddr3_dq[6]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="L12" SLEW="" name="ddr3_dq[7]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="K14" SLEW="" name="ddr3_dq[8]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="K13" SLEW="" name="ddr3_dq[9]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="M16" SLEW="" name="ddr3_dqs_n[0]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="J12" SLEW="" name="ddr3_dqs_n[1]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="G16" SLEW="" name="ddr3_dqs_n[2]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="C14" SLEW="" name="ddr3_dqs_n[3]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="A27" SLEW="" name="ddr3_dqs_n[4]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="E25" SLEW="" name="ddr3_dqs_n[5]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="B29" SLEW="" name="ddr3_dqs_n[6]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="E28" SLEW="" name="ddr3_dqs_n[7]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="N16" SLEW="" name="ddr3_dqs_p[0]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="K12" SLEW="" name="ddr3_dqs_p[1]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="H16" SLEW="" name="ddr3_dqs_p[2]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="C15" SLEW="" name="ddr3_dqs_p[3]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="A26" SLEW="" name="ddr3_dqs_p[4]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="F25" SLEW="" name="ddr3_dqs_p[5]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="B28" SLEW="" name="ddr3_dqs_p[6]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="E27" SLEW="" name="ddr3_dqs_p[7]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="H20" SLEW="" name="ddr3_odt[0]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="E20" SLEW="" name="ddr3_ras_n" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="LVCMOS15" PADName="P18" SLEW="" name="ddr3_reset_n" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="F20" SLEW="" name="ddr3_we_n" IN_TERM="" />
        </PinSelection>
        <System_Control>
            <Pin PADName="AV40(MRCC_P)" Bank="15" name="sys_rst" />
            <Pin PADName="No connect" Bank="Select Bank" name="init_calib_complete" />
            <Pin PADName="No connect" Bank="Select Bank" name="tg_compare_error" />
        </System_Control>
        <TimingParameters>
            <Parameters twtr="7.5" trrd="5" trefi="7.8" tfaw="27" trtp="7.5" tcke="5" trfc="260" trp="13.91" tras="34" trcd="13.91" />
        </TimingParameters>
        <mrBurstLength name="Burst Length" >8 - Fixed</mrBurstLength>
        <mrBurstType name="Read Burst Type and Length" >Sequential</mrBurstType>
        <mrCasLatency name="CAS Latency" >11</mrCasLatency>
        <mrMode name="Mode" >Normal</mrMode>
        <mrDllReset name="DLL Reset" >No</mrDllReset>
        <mrPdMode name="DLL control for precharge PD" >Slow Exit</mrPdMode>
        <emrDllEnable name="DLL Enable" >Enable</emrDllEnable>
        <emrOutputDriveStrength name="Output Driver Impedance Control" >RZQ/7</emrOutputDriveStrength>
        <emrMirrorSelection name="Address Mirroring" >Disable</emrMirrorSelection>
        <emrCSSelection name="Controller Chip Select Pin" >Enable</emrCSSelection>
        <emrRTT name="RTT (nominal) - On Die Termination (ODT)" >RZQ/6</emrRTT>
        <emrPosted name="Additive Latency (AL)" >0</emrPosted>
        <emrOCD name="Write Leveling Enable" >Disabled</emrOCD>
        <emrDQS name="TDQS enable" >Enabled</emrDQS>
        <emrRDQS name="Qoff" >Output Buffer Enabled</emrRDQS>
        <mr2PartialArraySelfRefresh name="Partial-Array Self Refresh" >Full Array</mr2PartialArraySelfRefresh>
        <mr2CasWriteLatency name="CAS write latency" >8</mr2CasWriteLatency>
        <mr2AutoSelfRefresh name="Auto Self Refresh" >Enabled</mr2AutoSelfRefresh>
        <mr2SelfRefreshTempRange name="High Temparature Self Refresh Rate" >Normal</mr2SelfRefreshTempRange>
        <mr2RTTWR name="RTT_WR - Dynamic On Die Termination (ODT)" >Dynamic ODT off</mr2RTTWR>
        <PortInterface>AXI</PortInterface>
        <AXIParameters>
            <C0_C_RD_WR_ARB_ALGORITHM>RD_PRI_REG</C0_C_RD_WR_ARB_ALGORITHM>
            <C0_S_AXI_ADDR_WIDTH>32</C0_S_AXI_ADDR_WIDTH>
            <C0_S_AXI_DATA_WIDTH>64</C0_S_AXI_DATA_WIDTH>
            <C0_S_AXI_ID_WIDTH>4</C0_S_AXI_ID_WIDTH>
            <C0_S_AXI_SUPPORTS_NARROW_BURST>0</C0_S_AXI_SUPPORTS_NARROW_BURST>
        </AXIParameters>
    </Controller>

</Project>
}"""

  val vc709mig_b = """ {<?xml version='1.0' encoding='UTF-8'?>
<!-- IMPORTANT: This is an internal file that has been generated by the MIG software. Any direct editing or changes made to this file may result in unpredictable behavior or data corruption. It is strongly advised that users do not edit the contents of this file. Re-run the MIG GUI with the required settings if any of the options provided below need to be altered. -->
<Project NoOfControllers="1" >
    <ModuleName>vc709mig_b</ModuleName>
    <dci_inouts_inputs>1</dci_inouts_inputs>
    <dci_inputs>1</dci_inputs>
    <Debug_En>OFF</Debug_En>
    <DataDepth_En>1024</DataDepth_En>
    <LowPower_En>ON</LowPower_En>
    <XADC_En>Disabled</XADC_En>
    <TargetFPGA>xc7vx690t-ffg1761/-2</TargetFPGA>
    <Version>4.1</Version>
    <SystemClock>No Buffer</SystemClock>
    <ReferenceClock>Use System Clock</ReferenceClock>
    <SysResetPolarity>ACTIVE HIGH</SysResetPolarity>
    <BankSelectionFlag>FALSE</BankSelectionFlag>
    <InternalVref>0</InternalVref>
    <dci_hr_inouts_inputs>50 Ohms</dci_hr_inouts_inputs>
    <dci_cascade>0</dci_cascade>
    <Controller number="0" >
        <MemoryDevice>DDR3_SDRAM/SODIMMs/MT8KTF51264HZ-1G9</MemoryDevice>
        <TimePeriod>1250</TimePeriod>
        <VccAuxIO>2.0V</VccAuxIO>
        <PHYRatio>4:1</PHYRatio>
        <InputClkFreq>200</InputClkFreq>
        <UIExtraClocks>0</UIExtraClocks>
        <MMCM_VCO>800</MMCM_VCO>
        <MMCMClkOut0> 1.000</MMCMClkOut0>
        <MMCMClkOut1>1</MMCMClkOut1>
        <MMCMClkOut2>1</MMCMClkOut2>
        <MMCMClkOut3>1</MMCMClkOut3>
        <MMCMClkOut4>1</MMCMClkOut4>
        <DataWidth>64</DataWidth>
        <DeepMemory>1</DeepMemory>
        <DataMask>1</DataMask>
        <ECC>Disabled</ECC>
        <Ordering>Normal</Ordering>
        <BankMachineCnt>4</BankMachineCnt>
        <CustomPart>FALSE</CustomPart>
        <NewPartName></NewPartName>
        <RowAddress>16</RowAddress>
        <ColAddress>10</ColAddress>
        <BankAddress>3</BankAddress>
        <MemoryVoltage>1.5V</MemoryVoltage>
        <UserMemoryAddressMap>BANK_ROW_COLUMN</UserMemoryAddressMap>
        <PinSelection>
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AN19" SLEW="FAST" name="ddr3_addr[0]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AM17" SLEW="FAST" name="ddr3_addr[10]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AM18" SLEW="FAST" name="ddr3_addr[11]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AL17" SLEW="FAST" name="ddr3_addr[12]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AK17" SLEW="FAST" name="ddr3_addr[13]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AM19" SLEW="FAST" name="ddr3_addr[14]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AL19" SLEW="FAST" name="ddr3_addr[15]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AR19" SLEW="FAST" name="ddr3_addr[1]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AP20" SLEW="FAST" name="ddr3_addr[2]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AP17" SLEW="FAST" name="ddr3_addr[3]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AP18" SLEW="FAST" name="ddr3_addr[4]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AJ18" SLEW="FAST" name="ddr3_addr[5]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AN16" SLEW="FAST" name="ddr3_addr[6]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AM16" SLEW="FAST" name="ddr3_addr[7]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AK18" SLEW="FAST" name="ddr3_addr[8]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AK19" SLEW="FAST" name="ddr3_addr[9]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AR17" SLEW="FAST" name="ddr3_ba[0]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AR18" SLEW="FAST" name="ddr3_ba[1]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AN18" SLEW="FAST" name="ddr3_ba[2]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AT20" SLEW="FAST" name="ddr3_cas_n" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15" PADName="AU17" SLEW="FAST" name="ddr3_ck_n[0]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15" PADName="AT17" SLEW="FAST" name="ddr3_ck_p[0]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AW17" SLEW="FAST" name="ddr3_cke[0]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AV16" SLEW="FAST" name="ddr3_cs_n[0]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AT22" SLEW="FAST" name="ddr3_dm[0]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AL22" SLEW="FAST" name="ddr3_dm[1]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AU24" SLEW="FAST" name="ddr3_dm[2]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="BB23" SLEW="FAST" name="ddr3_dm[3]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="BB12" SLEW="FAST" name="ddr3_dm[4]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AV15" SLEW="FAST" name="ddr3_dm[5]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AK12" SLEW="FAST" name="ddr3_dm[6]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AP13" SLEW="FAST" name="ddr3_dm[7]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AN24" SLEW="FAST" name="ddr3_dq[0]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AL21" SLEW="FAST" name="ddr3_dq[10]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AM21" SLEW="FAST" name="ddr3_dq[11]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AJ21" SLEW="FAST" name="ddr3_dq[12]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AJ20" SLEW="FAST" name="ddr3_dq[13]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AK20" SLEW="FAST" name="ddr3_dq[14]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AL20" SLEW="FAST" name="ddr3_dq[15]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AW22" SLEW="FAST" name="ddr3_dq[16]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AW23" SLEW="FAST" name="ddr3_dq[17]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AW21" SLEW="FAST" name="ddr3_dq[18]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AV21" SLEW="FAST" name="ddr3_dq[19]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AM24" SLEW="FAST" name="ddr3_dq[1]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AU23" SLEW="FAST" name="ddr3_dq[20]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AV23" SLEW="FAST" name="ddr3_dq[21]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AR24" SLEW="FAST" name="ddr3_dq[22]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AT24" SLEW="FAST" name="ddr3_dq[23]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="BB24" SLEW="FAST" name="ddr3_dq[24]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="BA24" SLEW="FAST" name="ddr3_dq[25]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AY23" SLEW="FAST" name="ddr3_dq[26]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AY24" SLEW="FAST" name="ddr3_dq[27]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AY25" SLEW="FAST" name="ddr3_dq[28]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="BA25" SLEW="FAST" name="ddr3_dq[29]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AR22" SLEW="FAST" name="ddr3_dq[2]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="BB21" SLEW="FAST" name="ddr3_dq[30]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="BA21" SLEW="FAST" name="ddr3_dq[31]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AY14" SLEW="FAST" name="ddr3_dq[32]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AW15" SLEW="FAST" name="ddr3_dq[33]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="BB14" SLEW="FAST" name="ddr3_dq[34]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="BB13" SLEW="FAST" name="ddr3_dq[35]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AW12" SLEW="FAST" name="ddr3_dq[36]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AY13" SLEW="FAST" name="ddr3_dq[37]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AY12" SLEW="FAST" name="ddr3_dq[38]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="BA12" SLEW="FAST" name="ddr3_dq[39]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AR23" SLEW="FAST" name="ddr3_dq[3]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AU12" SLEW="FAST" name="ddr3_dq[40]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AU13" SLEW="FAST" name="ddr3_dq[41]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AT12" SLEW="FAST" name="ddr3_dq[42]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AU14" SLEW="FAST" name="ddr3_dq[43]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AV13" SLEW="FAST" name="ddr3_dq[44]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AW13" SLEW="FAST" name="ddr3_dq[45]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AT15" SLEW="FAST" name="ddr3_dq[46]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AR15" SLEW="FAST" name="ddr3_dq[47]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AL15" SLEW="FAST" name="ddr3_dq[48]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AJ15" SLEW="FAST" name="ddr3_dq[49]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AN23" SLEW="FAST" name="ddr3_dq[4]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AK14" SLEW="FAST" name="ddr3_dq[50]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AJ12" SLEW="FAST" name="ddr3_dq[51]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AJ16" SLEW="FAST" name="ddr3_dq[52]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AL16" SLEW="FAST" name="ddr3_dq[53]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AJ13" SLEW="FAST" name="ddr3_dq[54]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AK13" SLEW="FAST" name="ddr3_dq[55]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AR14" SLEW="FAST" name="ddr3_dq[56]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AT14" SLEW="FAST" name="ddr3_dq[57]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AM12" SLEW="FAST" name="ddr3_dq[58]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AP11" SLEW="FAST" name="ddr3_dq[59]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AM23" SLEW="FAST" name="ddr3_dq[5]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AM13" SLEW="FAST" name="ddr3_dq[60]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AN13" SLEW="FAST" name="ddr3_dq[61]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AM11" SLEW="FAST" name="ddr3_dq[62]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AN11" SLEW="FAST" name="ddr3_dq[63]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AN21" SLEW="FAST" name="ddr3_dq[6]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AP21" SLEW="FAST" name="ddr3_dq[7]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AK23" SLEW="FAST" name="ddr3_dq[8]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15_T_DCI" PADName="AJ23" SLEW="FAST" name="ddr3_dq[9]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="AP22" SLEW="FAST" name="ddr3_dqs_n[0]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="AK22" SLEW="FAST" name="ddr3_dqs_n[1]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="AU21" SLEW="FAST" name="ddr3_dqs_n[2]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="BB22" SLEW="FAST" name="ddr3_dqs_n[3]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="BA14" SLEW="FAST" name="ddr3_dqs_n[4]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="AR12" SLEW="FAST" name="ddr3_dqs_n[5]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="AL14" SLEW="FAST" name="ddr3_dqs_n[6]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="AN14" SLEW="FAST" name="ddr3_dqs_n[7]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="AP23" SLEW="FAST" name="ddr3_dqs_p[0]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="AJ22" SLEW="FAST" name="ddr3_dqs_p[1]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="AT21" SLEW="FAST" name="ddr3_dqs_p[2]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="BA22" SLEW="FAST" name="ddr3_dqs_p[3]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="BA15" SLEW="FAST" name="ddr3_dqs_p[4]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="AP12" SLEW="FAST" name="ddr3_dqs_p[5]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="AK15" SLEW="FAST" name="ddr3_dqs_p[6]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="DIFF_SSTL15_T_DCI" PADName="AN15" SLEW="FAST" name="ddr3_dqs_p[7]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AT16" SLEW="FAST" name="ddr3_odt[0]" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AV19" SLEW="FAST" name="ddr3_ras_n" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="LVCMOS15" PADName="BB19" SLEW="FAST" name="ddr3_reset_n" IN_TERM="" />
            <Pin VCCAUX_IO="HIGH" IOSTANDARD="SSTL15" PADName="AU19" SLEW="FAST" name="ddr3_we_n" IN_TERM="" />
        </PinSelection>
        <System_Control>
            <Pin PADName="AV40(MRCC_P)" Bank="15" name="sys_rst" />
            <Pin PADName="No connect" Bank="Select Bank" name="init_calib_complete" />
            <Pin PADName="No connect" Bank="Select Bank" name="tg_compare_error" />
        </System_Control>
        <TimingParameters>
            <Parameters twtr="7.5" trrd="5" trefi="7.8" tfaw="27" trtp="7.5" tcke="5" trfc="260" trp="13.91" tras="34" trcd="13.91" />
        </TimingParameters>
        <mrBurstLength name="Burst Length" >8 - Fixed</mrBurstLength>
        <mrBurstType name="Read Burst Type and Length" >Sequential</mrBurstType>
        <mrCasLatency name="CAS Latency" >11</mrCasLatency>
        <mrMode name="Mode" >Normal</mrMode>
        <mrDllReset name="DLL Reset" >No</mrDllReset>
        <mrPdMode name="DLL control for precharge PD" >Slow Exit</mrPdMode>
        <emrDllEnable name="DLL Enable" >Enable</emrDllEnable>
        <emrOutputDriveStrength name="Output Driver Impedance Control" >RZQ/7</emrOutputDriveStrength>
        <emrMirrorSelection name="Address Mirroring" >Disable</emrMirrorSelection>
        <emrCSSelection name="Controller Chip Select Pin" >Enable</emrCSSelection>
        <emrRTT name="RTT (nominal) - On Die Termination (ODT)" >RZQ/6</emrRTT>
        <emrPosted name="Additive Latency (AL)" >0</emrPosted>
        <emrOCD name="Write Leveling Enable" >Disabled</emrOCD>
        <emrDQS name="TDQS enable" >Enabled</emrDQS>
        <emrRDQS name="Qoff" >Output Buffer Enabled</emrRDQS>
        <mr2PartialArraySelfRefresh name="Partial-Array Self Refresh" >Full Array</mr2PartialArraySelfRefresh>
        <mr2CasWriteLatency name="CAS write latency" >8</mr2CasWriteLatency>
        <mr2AutoSelfRefresh name="Auto Self Refresh" >Enabled</mr2AutoSelfRefresh>
        <mr2SelfRefreshTempRange name="High Temparature Self Refresh Rate" >Normal</mr2SelfRefreshTempRange>
        <mr2RTTWR name="RTT_WR - Dynamic On Die Termination (ODT)" >Dynamic ODT off</mr2RTTWR>
        <PortInterface>AXI</PortInterface>
        <AXIParameters>
            <C0_C_RD_WR_ARB_ALGORITHM>RD_PRI_REG</C0_C_RD_WR_ARB_ALGORITHM>
            <C0_S_AXI_ADDR_WIDTH>32</C0_S_AXI_ADDR_WIDTH>
            <C0_S_AXI_DATA_WIDTH>64</C0_S_AXI_DATA_WIDTH>
            <C0_S_AXI_ID_WIDTH>4</C0_S_AXI_ID_WIDTH>
            <C0_S_AXI_SUPPORTS_NARROW_BURST>0</C0_S_AXI_SUPPORTS_NARROW_BURST>
        </AXIParameters>
    </Controller>

</Project>
}"""

  val migprj = Seq(vc709mig_a, vc709mig_b)(index)
  val migprjname =  Seq("{/vc709mig_a.prj}", "{/vc709mig_b.prj}")(index)
  val modulename =  Seq("vc709mig_a", "vc709mig_b")(index)

  ElaborationArtefacts.add(
  modulename++".vivado.tcl",
   """set migprj """ ++ migprj ++ """
   set migprjfile """ ++ migprjname ++ """
   set migprjfilepath $ipdir$migprjfile
   set fp [open $migprjfilepath w+]
   puts $fp $migprj
   close $fp
   create_ip -vendor xilinx.com -library ip -name mig_7series -module_name """ ++ modulename ++ """ -dir $ipdir -force
   set_property CONFIG.XML_INPUT_FILE $migprjfilepath [get_ips """ ++ modulename ++ """] """
  )
}
//scalastyle:on
