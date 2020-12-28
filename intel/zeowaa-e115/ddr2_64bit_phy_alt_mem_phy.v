//

`default_nettype none

`ifdef ALT_MEM_PHY_DEFINES
`else
`include "alt_mem_phy_defines.v"
`endif

//
(* altera_attribute = "-name MESSAGE_DISABLE 14130" *) module ddr2_64bit_phy_alt_mem_phy (
                        //Clock and reset inputs:
                        pll_ref_clk,
                        global_reset_n,
                        soft_reset_n,

                        // Used to indicate PLL loss of lock for system reset management:
                        reset_request_n,

                        // Clock and reset for the controller interface:
                        ctl_clk,
                        ctl_reset_n,

                        // Write data interface:
                        ctl_dqs_burst,
                        ctl_wdata_valid,
                        ctl_wdata,
                        ctl_dm,
                        ctl_wlat,

                        // Address and command interface:
                        ctl_addr,
                        ctl_ba,
                        ctl_cas_n,
                        ctl_cke,
                        ctl_cs_n,
                        ctl_odt,
                        ctl_ras_n,
                        ctl_we_n,
                        ctl_rst_n,
                        ctl_mem_clk_disable,

                        // Read data interface:
                        ctl_doing_rd,
                        ctl_rdata,
                        ctl_rdata_valid,
                        ctl_rlat,

                        //re-calibration request & configuration:
                        ctl_cal_req,
                        ctl_cal_byte_lane_sel_n,

                        //Calibration status interface:
                        ctl_cal_success,
                        ctl_cal_fail,
                        ctl_cal_warning,

                        //ports to memory device(s):
                        mem_addr,
                        mem_ba,
                        mem_cas_n,
                        mem_cke,
                        mem_cs_n,
                        mem_dm,
                        mem_odt,
                        mem_ras_n,
                        mem_we_n,
                        mem_clk,
                        mem_clk_n,
                        mem_reset_n,

                        // Bidirectional Memory interface signals:
                        mem_dq,
                        mem_dqs,
                        mem_dqs_n,

                        // Auxiliary clocks. Some systems may need these for debugging
                        // purposes, or for full-rate to half-rate bridge interfaces
                        aux_half_rate_clk,
                        aux_full_rate_clk,

                        // Debug interface:- ALTERA USE ONLY
                        dbg_clk,
                        dbg_reset_n,
                        dbg_addr,
                        dbg_wr,
                        dbg_rd,
                        dbg_cs,
                        dbg_wr_data,
                        dbg_rd_data,
                        dbg_waitrequest

                      );


// Default parameter values :

parameter FAMILY                          =     "CYCLONEIII";
parameter MEM_IF_MEMTYPE                  =           "DDR2";
parameter LEVELLING                       =                0; // RFU
parameter SPEED_GRADE                     =             "C6";
parameter DLL_DELAY_BUFFER_MODE           =           "HIGH";
parameter DLL_DELAY_CHAIN_LENGTH          =                8;
parameter DQS_DELAY_CTL_WIDTH             =                6;
parameter DQS_OUT_MODE                    =   "DELAY_CHAIN2";
parameter DQS_PHASE                       =             9000;
parameter DQS_PHASE_SETTING               =                2;
parameter DWIDTH_RATIO                    =                4;
parameter MEM_IF_DWIDTH                   =               64;
parameter MEM_IF_ADDR_WIDTH               =               13;
parameter MEM_IF_BANKADDR_WIDTH           =                3;
parameter MEM_IF_CS_WIDTH                 =                2;
parameter MEM_IF_DM_WIDTH                 =                8;
parameter MEM_IF_DM_PINS_EN               =                1;
parameter MEM_IF_DQ_PER_DQS               =                8;
parameter MEM_IF_DQS_WIDTH                =                8;
parameter MEM_IF_OCT_EN                   =                0;
parameter MEM_IF_CLK_PAIR_COUNT           =                3;
parameter MEM_IF_CLK_PS                   =             4000;
parameter MEM_IF_CLK_PS_STR               =        "4000 ps";
parameter MEM_IF_MR_0                     =                0;
parameter MEM_IF_MR_1                     =                0;
parameter MEM_IF_MR_2                     =                0;
parameter MEM_IF_MR_3                     =                0;
parameter MEM_IF_PRESET_RLAT              =                0;
parameter PLL_STEPS_PER_CYCLE             =               24;
parameter SCAN_CLK_DIVIDE_BY              =                4;
parameter REDUCE_SIM_TIME                 =                0;
parameter CAPABILITIES                    =                0;
parameter TINIT_TCK                       =            40000;
parameter TINIT_RST                       =           100000;
parameter DBG_A_WIDTH                     =               13;
parameter SEQ_STRING_ID                   =       "seq_name";
parameter MEM_IF_CS_PER_RANK              =                1;    // duplicates CS, CKE, ODT, sequencer still controls 1 rank, but it is subdivided from controller perspective.
parameter MEM_IF_RANKS_PER_SLOT           =                1;    // how ranks are arranged into slot - needed for odt setting in the sequencer
parameter MEM_IF_RDV_PER_CHIP             =                0;   // multiple chips, and which gives valid data
parameter GENERATE_ADDITIONAL_DBG_RTL     =                0;   // DDR2 sequencer specific
parameter CAPTURE_PHASE_OFFSET            =                0;
parameter MEM_IF_ADDR_CMD_PHASE           =                0;
parameter DLL_EXPORT_IMPORT               =           "NONE";
parameter MEM_IF_DQSN_EN                  =                1;

parameter RANK_HAS_ADDR_SWAP              =                0;

//
localparam phy_report_prefix              = "ddr2_64bit_phy_alt_mem_phy (top level) : ";

// function to set the USE_MEM_CLK_FOR_ADDR_CMD_CLK localparam based on MEM_IF_ADDR_CMD_PHASE
function integer set_mem_clk_for_ac_clk (input reg [23:0] addr_cmd_phase);
    integer return_value;
    begin
        return_value = 0;
        case (addr_cmd_phase)
            0, 180       : return_value = 1;
            90, 270      : return_value = 0;
            default      : begin
                           //synthesis translate_off
                               $display(phy_report_prefix, "Illegal value set on MEM_IF_ADDR_CMD_PHASE parameter: ", addr_cmd_phase);
                               $stop;
                           //synthesis translate_on
                           end
        endcase
        set_mem_clk_for_ac_clk = return_value;
    end
endfunction

// function to set the ADDR_CMD_NEGEDGE_EN localparam based on MEM_IF_ADDR_CMD_PHASE
function integer set_ac_negedge_en(input reg [23:0] addr_cmd_phase);
    integer return_value;
    begin
        return_value = 0;
        case (addr_cmd_phase)
            90, 180      : return_value = 1;
            0, 270       : return_value = 0;
            default      : begin
                           //synthesis translate_off
                               $display(phy_report_prefix, "Illegal value set on MEM_IF_ADDR_CMD_PHASE parameter: ", addr_cmd_phase);
                               $stop;
                           //synthesis translate_on
                           end
        endcase
        set_ac_negedge_en = return_value;
    end
endfunction


localparam USE_MEM_CLK_FOR_ADDR_CMD_CLK   =           set_mem_clk_for_ac_clk(MEM_IF_ADDR_CMD_PHASE);
localparam ADDR_CMD_NEGEDGE_EN            =           set_ac_negedge_en(MEM_IF_ADDR_CMD_PHASE);

localparam LOCAL_IF_DWIDTH                =           MEM_IF_DWIDTH*DWIDTH_RATIO;
localparam LOCAL_IF_CLK_PS                =           MEM_IF_CLK_PS/(DWIDTH_RATIO/2);
localparam PLL_REF_CLK_PS                 =           LOCAL_IF_CLK_PS;

localparam MEM_IF_DQS_CAPTURE_EN          =                0;
localparam ADDR_COUNT_WIDTH               =                4;
localparam RDP_RESYNC_LAT_CTL_EN          =                0;
localparam DEDICATED_MEMORY_CLK_EN        =                0;

localparam ADV_LAT_WIDTH                  =                5;
localparam CAPTURE_MIMIC_PATH             =                0;
localparam DDR_MIMIC_PATH_EN              =                1;
localparam MIMIC_DEBUG_EN                 =                0;
localparam NUM_MIMIC_SAMPLE_CYCLES        =                6;
localparam NUM_DEBUG_SAMPLES_TO_STORE     =             4096;
localparam ASYNCHRONOUS_AVALON_CLOCK      =                1;
localparam RDV_INITIAL_LAT                =               23;
localparam RDP_INITIAL_LAT                = (DWIDTH_RATIO == 2 ? 5:6);
localparam RESYNC_PIPELINE_DEPTH          =                0;
localparam CLOCK_INDEX_WIDTH              =                3;
localparam OCT_LAT_WIDTH                  =    ADV_LAT_WIDTH;


// I/O Signal definitions :

// Clock and reset I/O :
input  wire                                                    pll_ref_clk;
input  wire                                                    global_reset_n;
input  wire                                                    soft_reset_n;

// This is the PLL locked signal :
output wire                                                    reset_request_n;

// The controller must use this phy_clk to interface to the PHY.  It is
// optional as to whether the remainder of the system uses it :
output wire                                                    ctl_clk;
output wire                                                    ctl_reset_n;

// new AFI I/Os -  write data i/f:
input  wire [MEM_IF_DQS_WIDTH * DWIDTH_RATIO/2 -1 : 0]         ctl_dqs_burst;
input  wire [MEM_IF_DQS_WIDTH * DWIDTH_RATIO/2 -1 : 0]         ctl_wdata_valid;
input  wire [MEM_IF_DWIDTH * DWIDTH_RATIO      -1 : 0]         ctl_wdata;
input  wire [MEM_IF_DM_WIDTH * DWIDTH_RATIO    -1 : 0]         ctl_dm;
output wire [4 : 0]                                            ctl_wlat;

// new AFI I/Os - addr/cmd i/f:
input  wire [MEM_IF_ADDR_WIDTH  * DWIDTH_RATIO/2 -1 : 0]       ctl_addr;
input  wire [MEM_IF_BANKADDR_WIDTH * DWIDTH_RATIO/2 -1 : 0]    ctl_ba;
input  wire [1 * DWIDTH_RATIO/2 -1 : 0]                        ctl_cas_n;
input  wire [MEM_IF_CS_WIDTH * DWIDTH_RATIO/2 - 1:0]           ctl_cke;
input  wire [MEM_IF_CS_WIDTH * DWIDTH_RATIO/2 - 1:0]           ctl_cs_n;
input  wire [MEM_IF_CS_WIDTH * DWIDTH_RATIO/2 - 1:0]           ctl_odt;
input  wire [1 * DWIDTH_RATIO/2 -1 : 0]                        ctl_ras_n;
input  wire [1 * DWIDTH_RATIO/2 -1 : 0]                        ctl_we_n;
input  wire [DWIDTH_RATIO/2 - 1 : 0]                           ctl_rst_n;
input  wire [MEM_IF_CLK_PAIR_COUNT - 1 : 0]                    ctl_mem_clk_disable;

// new AFI I/Os - read data i/f:
input  wire [MEM_IF_DQS_WIDTH * DWIDTH_RATIO / 2 -1 : 0]       ctl_doing_rd;
output wire [MEM_IF_DWIDTH * DWIDTH_RATIO      -1 : 0]         ctl_rdata;
output wire [DWIDTH_RATIO / 2 -1 : 0]                          ctl_rdata_valid;
output wire [4 : 0]                                            ctl_rlat;

// re-calibration request and configuration:
input  wire                                                    ctl_cal_req;
input  wire [MEM_IF_DQS_WIDTH * MEM_IF_CS_WIDTH - 1 : 0]       ctl_cal_byte_lane_sel_n;

// new AFI I/Os - status interface:
output wire                                                    ctl_cal_success;
output wire                                                    ctl_cal_fail;
output wire                                                    ctl_cal_warning;


//Outputs to DIMM :
output wire [MEM_IF_ADDR_WIDTH - 1 : 0]                        mem_addr;
output wire [MEM_IF_BANKADDR_WIDTH - 1 : 0]                    mem_ba;
output wire                                                    mem_cas_n;
output wire [MEM_IF_CS_WIDTH - 1 : 0]                          mem_cke;
output wire [MEM_IF_CS_WIDTH - 1 : 0]                          mem_cs_n;
wire        [MEM_IF_DWIDTH - 1 : 0]                            mem_d;
output wire [MEM_IF_DM_WIDTH - 1 : 0]                          mem_dm;
output wire [MEM_IF_CS_WIDTH - 1 : 0]                          mem_odt;
output wire                                                    mem_ras_n;
output wire                                                    mem_we_n;
output wire                                                    mem_reset_n;

//The mem_clks are outputs, but one is sometimes used for the mimic_path, so
//is looped back in.  Therefore defining as an inout ensures no errors in Quartus :
inout  wire [MEM_IF_CLK_PAIR_COUNT - 1 : 0]                    mem_clk;
inout  wire [MEM_IF_CLK_PAIR_COUNT - 1 : 0]                    mem_clk_n;

//Bidirectional:
inout  tri  [MEM_IF_DWIDTH - 1 : 0]                            mem_dq;
inout  tri  [MEM_IF_DWIDTH / MEM_IF_DQ_PER_DQS - 1 : 0]        mem_dqs;
inout  tri  [MEM_IF_DWIDTH / MEM_IF_DQ_PER_DQS - 1 : 0]        mem_dqs_n;


// AVALON MM Slave   -- debug IF
input  wire                                                    dbg_clk;
input  wire                                                    dbg_reset_n;
input  wire [DBG_A_WIDTH -1 : 0]                               dbg_addr;
input  wire                                                    dbg_wr;
input  wire                                                    dbg_rd;
input  wire                                                    dbg_cs;
input  wire [31 : 0]                                           dbg_wr_data;
output wire [31 : 0]                                           dbg_rd_data;
output wire                                                    dbg_waitrequest;

// Auxillary clocks. These do not have to be connected if the system
// doesn't require them :
output wire                                                    aux_half_rate_clk;
output wire                                                    aux_full_rate_clk;

// Internal signal declarations :

// Clocks :

// full-rate memory clock
wire                                            mem_clk_2x;

// half-rate memory clock
wire                                            mem_clk_1x;

// write_clk_2x is a full-rate write clock.  It is -90 degress aligned to the
// system clock :
wire                                            write_clk_2x;

wire                                            phy_clk_1x_src;
wire                                            phy_clk_1x;
wire                                            ac_clk_2x;
wire                                            cs_n_clk_2x;
wire                                            resync_clk_2x;
wire                                            measure_clk_1x;
wire                                            measure_clk_2x;

wire                                            half_rate_clk;

wire [DQS_DELAY_CTL_WIDTH - 1 : 0 ]             dedicated_dll_delay_ctrl;

// resets, async assert, de-assert is sync'd to each clock domain
wire                                            reset_mem_clk_2x_n;
wire                                            reset_rdp_phy_clk_1x_n;
wire                                            reset_phy_clk_1x_n;
wire                                            reset_ac_clk_2x_n;
wire                                            reset_cs_n_clk_2x_n;
wire                                            reset_mimic_2x_n;
wire                                            reset_resync_clk_2x_n;
wire                                            reset_seq_n;
wire                                            reset_measure_clk_1x_n;
wire                                            reset_measure_clk_2x_n;
wire                                            reset_write_clk_2x_n;


// Misc signals :
wire                                            phs_shft_busy;
wire                                            pll_seq_reconfig_busy;


// Sequencer signals
wire                                                           seq_mmc_start;
wire                                                           seq_pll_inc_dec_n;
wire                                                           seq_pll_start_reconfig;
wire [CLOCK_INDEX_WIDTH - 1 : 0]                               seq_pll_select;
wire [MEM_IF_DQS_WIDTH -1 : 0]                                 seq_rdp_dec_read_lat_1x;
wire [MEM_IF_DQS_WIDTH -1 : 0]                                 seq_rdp_inc_read_lat_1x;

wire                                                           seq_rdp_reset_req_n;

wire                                                           seq_ac_sel;

wire [MEM_IF_ADDR_WIDTH * DWIDTH_RATIO/2 - 1 : 0]              seq_ac_addr;
wire [MEM_IF_BANKADDR_WIDTH * DWIDTH_RATIO/2 - 1 : 0]          seq_ac_ba;
wire [DWIDTH_RATIO/2 -1 : 0]                                   seq_ac_cas_n;
wire [DWIDTH_RATIO/2 -1 : 0]                                   seq_ac_ras_n;
wire [DWIDTH_RATIO/2 -1 : 0]                                   seq_ac_we_n;
wire [MEM_IF_CS_WIDTH * DWIDTH_RATIO/2 - 1 : 0]                seq_ac_cke;
wire [MEM_IF_CS_WIDTH * DWIDTH_RATIO/2 - 1 : 0]                seq_ac_cs_n;
wire [MEM_IF_CS_WIDTH * DWIDTH_RATIO/2 - 1 : 0]                seq_ac_odt;

wire [DWIDTH_RATIO * MEM_IF_DM_WIDTH - 1 : 0 ]                 seq_wdp_dm;
wire [MEM_IF_DQS_WIDTH * (DWIDTH_RATIO/2) - 1 : 0]             seq_wdp_dqs_burst;
wire [MEM_IF_DWIDTH * DWIDTH_RATIO - 1 : 0 ]                   seq_wdp_wdata;
wire [MEM_IF_DQS_WIDTH * (DWIDTH_RATIO/2) - 1 : 0]             seq_wdp_wdata_valid;
wire [DWIDTH_RATIO - 1 :0]                                     seq_wdp_dqs;
wire                                                           seq_wdp_ovride;

wire [MEM_IF_DQS_WIDTH * (DWIDTH_RATIO/2) - 1 : 0]             oct_rsst_sel;

wire [MEM_IF_DQS_WIDTH * DWIDTH_RATIO/2 - 1 : 0]               seq_doing_rd;
wire                                                           seq_rdata_valid_lat_inc;
wire                                                           seq_rdata_valid_lat_dec;

wire  [DWIDTH_RATIO/2 - 1 : 0]                                 seq_rdata_valid;


reg  [DQS_DELAY_CTL_WIDTH - 1 : 0 ]                            dedicated_dll_delay_ctrl_r;

// set pll clock index of resync and mimic clocks
wire [CLOCK_INDEX_WIDTH                        - 1 : 0]        pll_resync_clk_index;
wire [CLOCK_INDEX_WIDTH                        - 1 : 0]        pll_measure_clk_index;


// Mimic signals :
wire                                               mmc_seq_done;
wire                                               mmc_seq_value;
wire                                               mimic_data;

wire                                               mux_seq_controller_ready;
wire                                               mux_seq_wdata_req;


// Read datapath signals :

// Connections from the IOE to the read datapath :
wire [MEM_IF_DWIDTH - 1 : 0]                       dio_rdata_h_2x;
wire [MEM_IF_DWIDTH - 1 : 0]                       dio_rdata_l_2x;


// Write datapath signals :



// wires from the wdp to the dpio :
wire [MEM_IF_DWIDTH - 1 : 0]                 wdp_wdata3_1x;
wire [MEM_IF_DWIDTH - 1 : 0]                 wdp_wdata2_1x;
wire [MEM_IF_DWIDTH - 1 : 0]                 wdp_wdata1_1x;
wire [MEM_IF_DWIDTH - 1 : 0]                 wdp_wdata0_1x;


wire [MEM_IF_DWIDTH - 1 : 0]                 wdp_wdata_h_2x;
wire [MEM_IF_DWIDTH - 1 : 0]                 wdp_wdata_l_2x;
wire [MEM_IF_DWIDTH - 1 : 0]                 wdp_wdata_oe_2x;
wire  [(LOCAL_IF_DWIDTH/8) - 1 : 0]          ctl_mem_be;

wire [MEM_IF_DQS_WIDTH - 1 : 0]              wdp_wdata_oe_h_1x;
wire [MEM_IF_DQS_WIDTH - 1 : 0]              wdp_wdata_oe_l_1x;

wire [MEM_IF_DQS_WIDTH - 1 : 0]              wdp_dqs3_1x;
wire [MEM_IF_DQS_WIDTH - 1 : 0]              wdp_dqs2_1x;
wire [MEM_IF_DQS_WIDTH - 1 : 0]              wdp_dqs1_1x;
wire [MEM_IF_DQS_WIDTH - 1 : 0]              wdp_dqs0_1x;

wire [(MEM_IF_DQS_WIDTH) - 1 : 0]            wdp_wdqs_2x;

wire [MEM_IF_DQS_WIDTH - 1 : 0]              wdp_dqs_oe_h_1x;
wire [MEM_IF_DQS_WIDTH - 1 : 0]              wdp_dqs_oe_l_1x;

wire [(MEM_IF_DQS_WIDTH) - 1 : 0]            wdp_wdqs_oe_2x;

wire [MEM_IF_DM_WIDTH -1 : 0]                wdp_dm3_1x;
wire [MEM_IF_DM_WIDTH -1 : 0]                wdp_dm2_1x;
wire [MEM_IF_DM_WIDTH -1 : 0]                wdp_dm1_1x;
wire [MEM_IF_DM_WIDTH -1 : 0]                wdp_dm0_1x;

wire [MEM_IF_DM_WIDTH -1 : 0]                wdp_dm_h_2x;
wire [MEM_IF_DM_WIDTH -1 : 0]                wdp_dm_l_2x;

wire [MEM_IF_DQS_WIDTH -1 : 0]               wdp_oct_h_1x;
wire [MEM_IF_DQS_WIDTH -1 : 0]               wdp_oct_l_1x;

wire [MEM_IF_DQS_WIDTH -1 : 0]               seq_dqs_add_2t_delay;

wire                                         ctl_add_1t_ac_lat_internal;
wire                                         ctl_add_1t_odt_lat_internal;
wire                                         ctl_add_intermediate_regs_internal;
wire                                         ctl_negedge_en_internal;

wire                                         ctl_mem_dqs_burst;
wire [MEM_IF_DWIDTH*DWIDTH_RATIO - 1 : 0]    ctl_mem_wdata;
wire                                         ctl_mem_wdata_valid;

// These ports are tied off for DDR,DDR2,DDR3.  Registers are used to reduce Quartus warnings :
(* preserve *) reg [3 : 0]                   ctl_mem_dqs = 4'b1100;

wire [MEM_IF_CS_WIDTH - 1 : 0]               int_rank_has_addr_swap;

//SIII declarations :

//Outputs from the dp_io block to the read_dp block :
wire [MEM_IF_DWIDTH - 1 : 0]                     dio_rdata3_1x;
wire [MEM_IF_DWIDTH - 1 : 0]                     dio_rdata2_1x;
wire [MEM_IF_DWIDTH - 1 : 0]                     dio_rdata1_1x;
wire [MEM_IF_DWIDTH - 1 : 0]                     dio_rdata0_1x;

reg [DWIDTH_RATIO/2 - 1 : 0]                     rdv_pipe_ip;
reg [MEM_IF_DQS_WIDTH * DWIDTH_RATIO/2 - 1 : 0]  merged_doing_rd;


wire [OCT_LAT_WIDTH - 1 : 0]                     seq_oct_oct_delay; // oct_lat
wire [OCT_LAT_WIDTH - 1 : 0]                     seq_oct_oct_extend; //oct_extend_duration
wire seq_oct_val;

wire seq_mem_clk_disable;


wire [DWIDTH_RATIO/2 - 1 : 0]                    seq_ac_rst_n;

wire                                             dqs_delay_update_en;
wire [DQS_DELAY_CTL_WIDTH - 1 : 0 ]              dlloffset_offsetctrl_out;

// Generate auxillary clocks:
generate

    // Half-rate mode :
    if (DWIDTH_RATIO == 4)
    begin
        assign aux_half_rate_clk = phy_clk_1x;
        assign aux_full_rate_clk = mem_clk_2x;
    end

    // Full-rate mode :
    else
    begin
        assign aux_half_rate_clk = half_rate_clk;
        assign aux_full_rate_clk = phy_clk_1x;
    end

endgenerate

// The top level I/O should not have the "Nx" clock domain suffices, so this is
// assigned here.  Also note that to avoid delta delay issues both the external and
// internal phy_clks are assigned to a common 'src' clock :
assign ctl_clk         = phy_clk_1x_src;
assign phy_clk_1x      = phy_clk_1x_src;

assign ctl_reset_n     = reset_phy_clk_1x_n;

// Instance I/O modules :


//
ddr2_64bit_phy_alt_mem_phy_dp_io #(
    .MEM_IF_CLK_PS              (MEM_IF_CLK_PS),
    .MEM_IF_BANKADDR_WIDTH      (MEM_IF_BANKADDR_WIDTH),
    .MEM_IF_CS_WIDTH            (MEM_IF_CS_WIDTH),
    .MEM_IF_DWIDTH              (MEM_IF_DWIDTH),
    .MEM_IF_DM_WIDTH            (MEM_IF_DM_WIDTH),
    .MEM_IF_DM_PINS_EN          (MEM_IF_DM_PINS_EN),
    .MEM_IF_DQ_PER_DQS          (MEM_IF_DQ_PER_DQS),
    .MEM_IF_DQS_CAPTURE_EN      (MEM_IF_DQS_CAPTURE_EN),
    .MEM_IF_DQS_WIDTH           (MEM_IF_DQS_WIDTH),
    .MEM_IF_ROWADDR_WIDTH       (MEM_IF_ADDR_WIDTH),
    .DLL_DELAY_BUFFER_MODE      (DLL_DELAY_BUFFER_MODE),
    .DQS_OUT_MODE               (DQS_OUT_MODE),
    .DQS_PHASE                  (DQS_PHASE)
) dpio (
    .reset_resync_clk_2x_n      (reset_resync_clk_2x_n),
    .resync_clk_2x              (resync_clk_2x),
    .mem_clk_2x                 (mem_clk_2x),
    .write_clk_2x               (write_clk_2x),
    .mem_dm                     (mem_dm),
    .mem_dq                     (mem_dq),
    .mem_dqs                    (mem_dqs),
    .dio_rdata_h_2x             (dio_rdata_h_2x),
    .dio_rdata_l_2x             (dio_rdata_l_2x),
    .wdp_dm_h_2x                (wdp_dm_h_2x),
    .wdp_dm_l_2x                (wdp_dm_l_2x),
    .wdp_wdata_h_2x             (wdp_wdata_h_2x),
    .wdp_wdata_l_2x             (wdp_wdata_l_2x),
    .wdp_wdata_oe_2x            (wdp_wdata_oe_2x),
    .wdp_wdqs_2x                (wdp_wdqs_2x),
    .wdp_wdqs_oe_2x             (wdp_wdqs_oe_2x)
);

// Instance the read datapath :

//
ddr2_64bit_phy_alt_mem_phy_read_dp #(
    .ADDR_COUNT_WIDTH          (ADDR_COUNT_WIDTH),
    .BIDIR_DPINS               (1),
    .DWIDTH_RATIO              (DWIDTH_RATIO),
    .MEM_IF_CLK_PS             (MEM_IF_CLK_PS),
    .FAMILY                    (FAMILY),
    .LOCAL_IF_DWIDTH           (LOCAL_IF_DWIDTH),
    .MEM_IF_DQ_PER_DQS         (MEM_IF_DQ_PER_DQS),
    .MEM_IF_DQS_WIDTH          (MEM_IF_DQS_WIDTH),
    .MEM_IF_DWIDTH             (MEM_IF_DWIDTH),
    .RDP_INITIAL_LAT           (RDP_INITIAL_LAT),
    .RDP_RESYNC_LAT_CTL_EN     (RDP_RESYNC_LAT_CTL_EN),
    .RESYNC_PIPELINE_DEPTH     (RESYNC_PIPELINE_DEPTH)
) rdp (
    .phy_clk_1x                (phy_clk_1x),
    .resync_clk_2x             (resync_clk_2x),
    .reset_phy_clk_1x_n        (reset_rdp_phy_clk_1x_n),
    .reset_resync_clk_2x_n     (reset_resync_clk_2x_n),
    .seq_rdp_dec_read_lat_1x   (seq_rdp_dec_read_lat_1x[0]),
    .seq_rdp_dmx_swap          (1'b0),
    .seq_rdp_inc_read_lat_1x   (seq_rdp_inc_read_lat_1x[0]),
    .dio_rdata_h_2x            (dio_rdata_h_2x),
    .dio_rdata_l_2x            (dio_rdata_l_2x),
    .ctl_mem_rdata             (ctl_rdata)
);
//          enhancements a different delay per dqs group may be implemented using the
//          full vector


// Instance the write datapath :

generate

    // Half-rate Write datapath :
    if (DWIDTH_RATIO == 4)
    begin : half_rate_wdp_gen

        //
        ddr2_64bit_phy_alt_mem_phy_write_dp #(
                    .BIDIR_DPINS           (1),
            .LOCAL_IF_DRATE        ("HALF"),
            .LOCAL_IF_DWIDTH       (LOCAL_IF_DWIDTH),
            .MEM_IF_DM_WIDTH       (MEM_IF_DM_WIDTH),
            .MEM_IF_DQ_PER_DQS     (MEM_IF_DQ_PER_DQS),
            .MEM_IF_DQS_WIDTH      (MEM_IF_DQS_WIDTH),
            .GENERATE_WRITE_DQS    (1),
            .MEM_IF_DWIDTH         (MEM_IF_DWIDTH),
            .DWIDTH_RATIO          (DWIDTH_RATIO)
        ) wdp (
            .phy_clk_1x            (phy_clk_1x),
            .mem_clk_2x            (mem_clk_2x),
            .write_clk_2x          (write_clk_2x),
            .reset_phy_clk_1x_n    (reset_phy_clk_1x_n),
            .reset_mem_clk_2x_n    (reset_mem_clk_2x_n),
            .reset_write_clk_2x_n  (reset_write_clk_2x_n),
            .ctl_mem_be            (ctl_dm),
            .ctl_mem_dqs_burst     (ctl_dqs_burst),
            .ctl_mem_wdata         (ctl_wdata),
            .ctl_mem_wdata_valid   (ctl_wdata_valid),
            .seq_be                (seq_wdp_dm),
            .seq_dqs_burst         (seq_wdp_dqs_burst),
            .seq_wdata             (seq_wdp_wdata),
            .seq_wdata_valid       (seq_wdp_wdata_valid),
            .seq_ctl_sel           (seq_wdp_ovride),
            .wdp_wdata_h_2x        (wdp_wdata_h_2x),
            .wdp_wdata_l_2x        (wdp_wdata_l_2x),
            .wdp_wdata_oe_2x       (wdp_wdata_oe_2x),
            .wdp_wdqs_2x           (wdp_wdqs_2x),
            .wdp_wdqs_oe_2x        (wdp_wdqs_oe_2x),
            .wdp_dm_h_2x           (wdp_dm_h_2x),
            .wdp_dm_l_2x           (wdp_dm_l_2x)
        );

    end

    // Full-rate :
    else
    begin : full_rate_wdp_gen

        //
        ddr2_64bit_phy_alt_mem_phy_write_dp_fr #(
                    .BIDIR_DPINS           (1),
            .LOCAL_IF_DRATE        ("FULL"),
            .LOCAL_IF_DWIDTH       (LOCAL_IF_DWIDTH),
            .MEM_IF_DM_WIDTH       (MEM_IF_DM_WIDTH),
            .MEM_IF_DQ_PER_DQS     (MEM_IF_DQ_PER_DQS),
            .MEM_IF_DQS_WIDTH      (MEM_IF_DQS_WIDTH),
            .GENERATE_WRITE_DQS    (1),
            .MEM_IF_DWIDTH         (MEM_IF_DWIDTH),
            .DWIDTH_RATIO          (DWIDTH_RATIO)
        ) wdp (
            .phy_clk_1x            (phy_clk_1x),
            .mem_clk_2x            (mem_clk_2x),
            .write_clk_2x          (write_clk_2x),
            .reset_phy_clk_1x_n    (reset_phy_clk_1x_n),
            .reset_mem_clk_2x_n    (reset_mem_clk_2x_n),
            .reset_write_clk_2x_n  (reset_write_clk_2x_n),
            .ctl_mem_be            (ctl_dm),
            .ctl_mem_dqs_burst     (ctl_dqs_burst),
            .ctl_mem_wdata         (ctl_wdata),
            .ctl_mem_wdata_valid   (ctl_wdata_valid),
            .seq_be                (seq_wdp_dm),
            .seq_dqs_burst         (seq_wdp_dqs_burst),
            .seq_wdata             (seq_wdp_wdata),
            .seq_wdata_valid       (seq_wdp_wdata_valid),
            .seq_ctl_sel            (seq_wdp_ovride),
            .wdp_wdata_h_2x        (wdp_wdata_h_2x),
            .wdp_wdata_l_2x        (wdp_wdata_l_2x),
            .wdp_wdata_oe_2x       (wdp_wdata_oe_2x),
            .wdp_wdqs_2x           (wdp_wdqs_2x),
            .wdp_wdqs_oe_2x        (wdp_wdqs_oe_2x),
            .wdp_dm_h_2x           (wdp_dm_h_2x),
            .wdp_dm_l_2x           (wdp_dm_l_2x)
        );

    end

endgenerate


// Instance the address and command :

generate

    // Half-rate address and command :
    if (DWIDTH_RATIO == 4)
    begin : half_rate_adc_gen

        //
        ddr2_64bit_phy_alt_mem_phy_addr_cmd #(
                     .DWIDTH_RATIO                 (DWIDTH_RATIO),
             .MEM_ADDR_CMD_BUS_COUNT       (1),
             .MEM_IF_BANKADDR_WIDTH        (MEM_IF_BANKADDR_WIDTH),
             .MEM_IF_CS_WIDTH              (MEM_IF_CS_WIDTH),
             .MEM_IF_MEMTYPE               (MEM_IF_MEMTYPE),
             .MEM_IF_ROWADDR_WIDTH         (MEM_IF_ADDR_WIDTH)
        ) adc (
             .ac_clk_2x                    (ac_clk_2x),
             .cs_n_clk_2x                  (cs_n_clk_2x),
             .phy_clk_1x                   (phy_clk_1x),
             .reset_ac_clk_2x_n            (reset_ac_clk_2x_n),
             .reset_cs_n_clk_2x_n          (reset_cs_n_clk_2x_n),
             .ctl_add_1t_ac_lat            (ctl_add_1t_ac_lat_internal),
             .ctl_add_1t_odt_lat           (ctl_add_1t_odt_lat_internal),
             .ctl_add_intermediate_regs    (ctl_add_intermediate_regs_internal),
        //     .ctl_negedge_en               (ctl_negedge_en_internal),
             .ctl_negedge_en               (ADDR_CMD_NEGEDGE_EN[0 : 0]),
             .ctl_mem_addr_h               (ctl_addr[MEM_IF_ADDR_WIDTH -1 : 0]),
             .ctl_mem_addr_l               (ctl_addr[(MEM_IF_ADDR_WIDTH  * DWIDTH_RATIO/2) -1 : MEM_IF_ADDR_WIDTH]),
             .ctl_mem_ba_h                 (ctl_ba[MEM_IF_BANKADDR_WIDTH -1 : 0]),
             .ctl_mem_ba_l                 (ctl_ba[MEM_IF_BANKADDR_WIDTH * DWIDTH_RATIO/2 -1 : MEM_IF_BANKADDR_WIDTH]),
             .ctl_mem_cas_n_h              (ctl_cas_n[0]),
             .ctl_mem_cas_n_l              (ctl_cas_n[1]),
             .ctl_mem_cke_h                (ctl_cke[MEM_IF_CS_WIDTH - 1 : 0]),
             .ctl_mem_cke_l                (ctl_cke[MEM_IF_CS_WIDTH * DWIDTH_RATIO/2 - 1 : MEM_IF_CS_WIDTH]),
             .ctl_mem_cs_n_h               (ctl_cs_n[MEM_IF_CS_WIDTH - 1 : 0]),
             .ctl_mem_cs_n_l               (ctl_cs_n[MEM_IF_CS_WIDTH * DWIDTH_RATIO/2 - 1 : MEM_IF_CS_WIDTH]),
             .ctl_mem_odt_h                (ctl_odt[MEM_IF_CS_WIDTH - 1 : 0]),
             .ctl_mem_odt_l                (ctl_odt[MEM_IF_CS_WIDTH * DWIDTH_RATIO/2 - 1 : MEM_IF_CS_WIDTH]),
             .ctl_mem_ras_n_h              (ctl_ras_n[0]),
             .ctl_mem_ras_n_l              (ctl_ras_n[1]),
             .ctl_mem_we_n_h               (ctl_we_n[0]),
             .ctl_mem_we_n_l               (ctl_we_n[1]),
             .seq_addr_h                   (seq_ac_addr[MEM_IF_ADDR_WIDTH -1 : 0]),
             .seq_addr_l                   (seq_ac_addr[MEM_IF_ADDR_WIDTH  * DWIDTH_RATIO/2 -1 : MEM_IF_ADDR_WIDTH]),
             .seq_ba_h                     (seq_ac_ba[MEM_IF_BANKADDR_WIDTH -1 : 0]),
             .seq_ba_l                     (seq_ac_ba[MEM_IF_BANKADDR_WIDTH * DWIDTH_RATIO/2 -1 : MEM_IF_BANKADDR_WIDTH]),
             .seq_cas_n_h                  (seq_ac_cas_n[0]),
             .seq_cas_n_l                  (seq_ac_cas_n[1]),
             .seq_cke_h                    (seq_ac_cke[MEM_IF_CS_WIDTH - 1 : 0]),
             .seq_cke_l                    (seq_ac_cke[MEM_IF_CS_WIDTH * DWIDTH_RATIO/2 - 1 : MEM_IF_CS_WIDTH]),
             .seq_cs_n_h                   (seq_ac_cs_n[MEM_IF_CS_WIDTH - 1 : 0]),
             .seq_cs_n_l                   (seq_ac_cs_n[MEM_IF_CS_WIDTH * DWIDTH_RATIO/2 - 1 : MEM_IF_CS_WIDTH]),
             .seq_odt_h                    (seq_ac_odt[MEM_IF_CS_WIDTH - 1 : 0]),
             .seq_odt_l                    (seq_ac_odt[MEM_IF_CS_WIDTH * DWIDTH_RATIO/2 - 1 : MEM_IF_CS_WIDTH]),
             .seq_ras_n_h                  (seq_ac_ras_n[0]),
             .seq_ras_n_l                  (seq_ac_ras_n[1]),
             .seq_we_n_h                   (seq_ac_we_n[0]),
             .seq_we_n_l                   (seq_ac_we_n[1]),
             .seq_ac_sel                   (seq_ac_sel),
             .mem_addr                     (mem_addr),
             .mem_ba                       (mem_ba),
             .mem_cas_n                    (mem_cas_n),
             .mem_cke                      (mem_cke),
             .mem_cs_n                     (mem_cs_n),
             .mem_odt                      (mem_odt),
             .mem_ras_n                    (mem_ras_n),
             .mem_we_n                     (mem_we_n)
        );
    end

    // Full-rate :
    else
    begin : full_rate_adc_gen

        //
        ddr2_64bit_phy_alt_mem_phy_addr_cmd #(
                     .DWIDTH_RATIO                 (DWIDTH_RATIO),
             .MEM_ADDR_CMD_BUS_COUNT       (1),
             .MEM_IF_BANKADDR_WIDTH        (MEM_IF_BANKADDR_WIDTH),
             .MEM_IF_CS_WIDTH              (MEM_IF_CS_WIDTH),
             .MEM_IF_MEMTYPE               (MEM_IF_MEMTYPE),
             .MEM_IF_ROWADDR_WIDTH         (MEM_IF_ADDR_WIDTH)
        ) adc (
             .ac_clk_2x                    (ac_clk_2x),
             .cs_n_clk_2x                  (cs_n_clk_2x),
             .phy_clk_1x                   (phy_clk_1x),
             .reset_ac_clk_2x_n            (reset_ac_clk_2x_n),
             .reset_cs_n_clk_2x_n          (reset_cs_n_clk_2x_n),
             .ctl_add_1t_ac_lat            (ctl_add_1t_ac_lat_internal),
             .ctl_add_1t_odt_lat           (ctl_add_1t_odt_lat_internal),
             .ctl_add_intermediate_regs    (ctl_add_intermediate_regs_internal),
        //     .ctl_negedge_en               (ctl_negedge_en_internal),
             .ctl_negedge_en               (ADDR_CMD_NEGEDGE_EN[0 : 0]),
             .ctl_mem_addr_h               (),
             .ctl_mem_addr_l               (ctl_addr[MEM_IF_ADDR_WIDTH  -1 : 0]),
             .ctl_mem_ba_h                 (),
             .ctl_mem_ba_l                 (ctl_ba[MEM_IF_BANKADDR_WIDTH -1 : 0]),
             .ctl_mem_cas_n_h              (),
             .ctl_mem_cas_n_l              (ctl_cas_n[0]),
             .ctl_mem_cke_h                (),
             .ctl_mem_cke_l                (ctl_cke[MEM_IF_CS_WIDTH - 1 : 0]),
             .ctl_mem_cs_n_h               (),
             .ctl_mem_cs_n_l               (ctl_cs_n[MEM_IF_CS_WIDTH - 1 : 0]),
             .ctl_mem_odt_h                (),
             .ctl_mem_odt_l                (ctl_odt[MEM_IF_CS_WIDTH - 1 : 0]),
             .ctl_mem_ras_n_h              (),
             .ctl_mem_ras_n_l              (ctl_ras_n[0]),
             .ctl_mem_we_n_h               (),
             .ctl_mem_we_n_l               (ctl_we_n[0]),
             .seq_addr_h                   (),
             .seq_addr_l                   (seq_ac_addr[MEM_IF_ADDR_WIDTH -1 : 0]),
             .seq_ba_h                     (),
             .seq_ba_l                     (seq_ac_ba[MEM_IF_BANKADDR_WIDTH -1 : 0]),
             .seq_cas_n_h                  (),
             .seq_cas_n_l                  (seq_ac_cas_n[0]),
             .seq_cke_h                    (),
             .seq_cke_l                    (seq_ac_cke[MEM_IF_CS_WIDTH - 1 : 0]),
             .seq_cs_n_h                   (),
             .seq_cs_n_l                   (seq_ac_cs_n[MEM_IF_CS_WIDTH - 1 : 0]),
             .seq_odt_h                    (),
             .seq_odt_l                    (seq_ac_odt[MEM_IF_CS_WIDTH - 1 : 0]),
             .seq_ras_n_h                  (),
             .seq_ras_n_l                  (seq_ac_ras_n[0]),
             .seq_we_n_h                   (),
             .seq_we_n_l                   (seq_ac_we_n[0]),
             .seq_ac_sel                   (seq_ac_sel),
             .mem_addr                     (mem_addr),
             .mem_ba                       (mem_ba),
             .mem_cas_n                    (mem_cas_n),
             .mem_cke                      (mem_cke),
             .mem_cs_n                     (mem_cs_n),
             .mem_odt                      (mem_odt),
             .mem_ras_n                    (mem_ras_n),
             .mem_we_n                     (mem_we_n)
        );

    end
endgenerate

 assign int_rank_has_addr_swap = RANK_HAS_ADDR_SWAP[MEM_IF_CS_WIDTH - 1 : 0];
 assign pll_resync_clk_index   = 5;
 assign pll_measure_clk_index  = 4;

   //
  ddr2_64bit_phy_alt_mem_phy_seq_wrapper
      //
    seq_wrapper (
        .phy_clk_1x                         (phy_clk_1x),
        .reset_phy_clk_1x_n                 (reset_phy_clk_1x_n),
        .ctl_cal_success                    (ctl_cal_success),
        .ctl_cal_fail                       (ctl_cal_fail),
        .ctl_cal_warning                    (ctl_cal_warning),
        .ctl_cal_req                        (ctl_cal_req),
        .int_RANK_HAS_ADDR_SWAP             (int_rank_has_addr_swap),
        .ctl_cal_byte_lane_sel_n            (ctl_cal_byte_lane_sel_n),
        .seq_pll_inc_dec_n                  (seq_pll_inc_dec_n),
        .seq_pll_start_reconfig             (seq_pll_start_reconfig),
        .seq_pll_select                     (seq_pll_select),
        .phs_shft_busy                      (phs_shft_busy),
        .pll_resync_clk_index               (pll_resync_clk_index),
        .pll_measure_clk_index              (pll_measure_clk_index),
        .sc_clk_dp                          (),
        .scan_enable_dqs_config             (),
        .scan_update                        (),
        .scan_din                           (),
        .scan_enable_ck                     (),
        .scan_enable_dqs                    (),
        .scan_enable_dqsn                   (),
        .scan_enable_dq                     (),
        .scan_enable_dm                     (),
        .hr_rsc_clk                         (1'b0), // Halfrate resync clock not required for non-SIII style families.
        .seq_ac_addr                        (seq_ac_addr),
        .seq_ac_ba                          (seq_ac_ba),
        .seq_ac_cas_n                       (seq_ac_cas_n),
        .seq_ac_ras_n                       (seq_ac_ras_n),
        .seq_ac_we_n                        (seq_ac_we_n),
        .seq_ac_cke                         (seq_ac_cke),
        .seq_ac_cs_n                        (seq_ac_cs_n),
        .seq_ac_odt                         (seq_ac_odt),
        .seq_ac_rst_n                       (seq_ac_rst_n),
        .seq_ac_sel                         (seq_ac_sel),
        .seq_mem_clk_disable                (seq_mem_clk_disable),
        .ctl_add_1t_ac_lat_internal         (ctl_add_1t_ac_lat_internal),
        .ctl_add_1t_odt_lat_internal        (ctl_add_1t_odt_lat_internal),
        .ctl_add_intermediate_regs_internal (ctl_add_intermediate_regs_internal),
        .seq_rdv_doing_rd                   (seq_doing_rd),
        .seq_rdp_reset_req_n                (seq_rdp_reset_req_n),
        .seq_rdp_inc_read_lat_1x            (seq_rdp_inc_read_lat_1x),
        .seq_rdp_dec_read_lat_1x            (seq_rdp_dec_read_lat_1x),
        .ctl_rdata                          (ctl_rdata),
        .int_rdata_valid_1t                 (seq_rdata_valid),
        .seq_rdata_valid_lat_inc            (seq_rdata_valid_lat_inc),
        .seq_rdata_valid_lat_dec            (seq_rdata_valid_lat_dec),
        .ctl_rlat                           (ctl_rlat),
        .seq_poa_lat_dec_1x                 (),
        .seq_poa_lat_inc_1x                 (),
        .seq_poa_protection_override_1x     (),
        .seq_oct_oct_delay                  (seq_oct_oct_delay),
        .seq_oct_oct_extend                 (seq_oct_oct_extend),
        .seq_oct_val                        (seq_oct_val),
        .seq_wdp_dqs_burst                  (seq_wdp_dqs_burst),
        .seq_wdp_wdata_valid                (seq_wdp_wdata_valid),
        .seq_wdp_wdata                      (seq_wdp_wdata),
        .seq_wdp_dm                         (seq_wdp_dm),
        .seq_wdp_dqs                        (seq_wdp_dqs),
        .seq_wdp_ovride                     (seq_wdp_ovride),
        .seq_dqs_add_2t_delay               (seq_dqs_add_2t_delay),
        .ctl_wlat                           (ctl_wlat),
        .seq_mmc_start                      (seq_mmc_start),
        .mmc_seq_done                       (mmc_seq_done),
        .mmc_seq_value                      (mmc_seq_value),
        .mem_err_out_n                      (1'b1),
        .parity_error_n                     (),
        .dbg_clk                            (dbg_clk),
        .dbg_reset_n                        (dbg_reset_n),
        .dbg_addr                           (dbg_addr),
        .dbg_wr                             (dbg_wr),
        .dbg_rd                             (dbg_rd),
        .dbg_cs                             (dbg_cs),
        .dbg_wr_data                        (dbg_wr_data),
        .dbg_rd_data                        (dbg_rd_data),
        .dbg_waitrequest                    (dbg_waitrequest)
    );

// Generate rdata_valid for sequencer and control blocks
//
ddr2_64bit_phy_alt_mem_phy_rdata_valid #(
     .FAMILY                    (FAMILY),
     .MEM_IF_DQS_WIDTH          (MEM_IF_DQS_WIDTH),
     .RDATA_VALID_AWIDTH        (5),
     .RDATA_VALID_INITIAL_LAT   (RDV_INITIAL_LAT),
     .DWIDTH_RATIO              (DWIDTH_RATIO)
) rdv_pipe (
     .phy_clk_1x                (phy_clk_1x),
     .reset_phy_clk_1x_n        (reset_rdp_phy_clk_1x_n),
     .seq_rdata_valid_lat_dec   (seq_rdata_valid_lat_dec),
     .seq_rdata_valid_lat_inc   (seq_rdata_valid_lat_inc),
     .seq_doing_rd              (seq_doing_rd),
     .ctl_doing_rd              (ctl_doing_rd),
     .ctl_cal_success           (ctl_cal_success),
     .ctl_rdata_valid           (ctl_rdata_valid),
     .seq_rdata_valid           (seq_rdata_valid)
);

// Instance the CIII clock and reset :

//
ddr2_64bit_phy_alt_mem_phy_clk_reset #(
    .AC_PHASE                             (MEM_IF_ADDR_CMD_PHASE),
    .CLOCK_INDEX_WIDTH                    (CLOCK_INDEX_WIDTH),
    .CAPTURE_MIMIC_PATH                   (CAPTURE_MIMIC_PATH),
    .DDR_MIMIC_PATH_EN                    (DDR_MIMIC_PATH_EN),
    .DEDICATED_MEMORY_CLK_EN              (DEDICATED_MEMORY_CLK_EN),
    .DLL_EXPORT_IMPORT                    (DLL_EXPORT_IMPORT),
    .DWIDTH_RATIO                         (DWIDTH_RATIO),
    .LOCAL_IF_CLK_PS                      (LOCAL_IF_CLK_PS),
    .MEM_IF_CLK_PAIR_COUNT                (MEM_IF_CLK_PAIR_COUNT),
    .MEM_IF_CLK_PS                        (MEM_IF_CLK_PS),
    .MEM_IF_CS_WIDTH                      (MEM_IF_CS_WIDTH),
    .MEM_IF_DQ_PER_DQS                    (MEM_IF_DQ_PER_DQS),
    .MEM_IF_DQS_WIDTH                     (MEM_IF_DQS_WIDTH),
    .MEM_IF_DWIDTH                        (MEM_IF_DWIDTH),
    .MIF_FILENAME                         ("PLL.MIF"),
    .PLL_EXPORT_IMPORT                    ("NONE"),
    .PLL_REF_CLK_PS                       (PLL_REF_CLK_PS),
    .PLL_TYPE                             ("ENHANCED"),
    .SPEED_GRADE                          ("C6"),
    .DLL_DELAY_BUFFER_MODE                (DLL_DELAY_BUFFER_MODE),
    .DLL_DELAY_CHAIN_LENGTH               (DLL_DELAY_CHAIN_LENGTH),
    .DQS_OUT_MODE                         (DQS_OUT_MODE),
    .DQS_PHASE                            (DQS_PHASE),
    .SCAN_CLK_DIVIDE_BY                   (SCAN_CLK_DIVIDE_BY),
    .USE_MEM_CLK_FOR_ADDR_CMD_CLK         (USE_MEM_CLK_FOR_ADDR_CMD_CLK)
) clk (
    .pll_ref_clk                          (pll_ref_clk),
    .global_reset_n                       (global_reset_n),
    .soft_reset_n                         (soft_reset_n),
    .seq_rdp_reset_req_n                  (seq_rdp_reset_req_n),

    .ac_clk_2x                            (ac_clk_2x),
    .measure_clk_2x                       (measure_clk_2x),
    .mem_clk_2x                           (mem_clk_2x),
    .mem_clk                              (mem_clk),
    .mem_clk_n                            (mem_clk_n),
    .phy_clk_1x                           (phy_clk_1x_src),
    .resync_clk_2x                        (resync_clk_2x),
    .cs_n_clk_2x                          (cs_n_clk_2x),
    .write_clk_2x                         (write_clk_2x),
    .half_rate_clk                        (half_rate_clk),

    .reset_ac_clk_2x_n                    (reset_ac_clk_2x_n),
    .reset_measure_clk_2x_n               (reset_measure_clk_2x_n),
    .reset_mem_clk_2x_n                   (reset_mem_clk_2x_n),
    .reset_phy_clk_1x_n                   (reset_phy_clk_1x_n),
    .reset_resync_clk_2x_n                (reset_resync_clk_2x_n),
    .reset_write_clk_2x_n                 (reset_write_clk_2x_n),
    .reset_cs_n_clk_2x_n                  (reset_cs_n_clk_2x_n),
    .reset_rdp_phy_clk_1x_n               (reset_rdp_phy_clk_1x_n),
    .mem_reset_n                          (mem_reset_n),

    .reset_request_n                      (reset_request_n),

    .phs_shft_busy                        (phs_shft_busy),
    .seq_pll_inc_dec_n                    (seq_pll_inc_dec_n),
    .seq_pll_select                       (seq_pll_select),
    .seq_pll_start_reconfig               (seq_pll_start_reconfig),
    .mimic_data_2x                        (mimic_data),
    .seq_clk_disable                      (seq_mem_clk_disable),
    .ctrl_clk_disable                     (ctl_mem_clk_disable)
);

// Instance the mimic block :

//
ddr2_64bit_phy_alt_mem_phy_mimic #(
    .NUM_MIMIC_SAMPLE_CYCLES (NUM_MIMIC_SAMPLE_CYCLES)
) mmc (
    .measure_clk             (measure_clk_2x),
    .reset_measure_clk_n     (reset_measure_clk_2x_n),
    .mimic_data_in           (mimic_data),
    .seq_mmc_start           (seq_mmc_start),
    .mmc_seq_done            (mmc_seq_done),
    .mmc_seq_value           (mmc_seq_value)
);

// If required, instance the Mimic debug block.  If the debug block is used, a top level input
// for mimic_recapture_debug_data should be created.
generate

    if (MIMIC_DEBUG_EN == 1)
    begin : create_mimic_debug_ram

        //
        ddr2_64bit_phy_alt_mem_phy_mimic_debug #(
                    .NUM_DEBUG_SAMPLES_TO_STORE (NUM_DEBUG_SAMPLES_TO_STORE),
            .PLL_STEPS_PER_CYCLE        (PLL_STEPS_PER_CYCLE)
        ) mmc_debug (
            .measure_clk                (measure_clk_1x),
            .reset_measure_clk_n        (reset_measure_clk_1x_n),
            .mmc_seq_done               (mmc_seq_done),
            .mmc_seq_value              (mmc_seq_value),
            .mimic_recapture_debug_data (1'b0)
        );

    end

endgenerate

endmodule

`default_nettype wire


//

`ifdef ALT_MEM_PHY_DEFINES
`else
`include "alt_mem_phy_defines.v"
`endif

//
module ddr2_64bit_phy_alt_mem_phy_clk_reset (
                               pll_ref_clk,
                               global_reset_n,
                               soft_reset_n,
                               seq_rdp_reset_req_n,

                               ac_clk_2x,
                               measure_clk_2x,
                               mem_clk_2x,
                               mem_clk,
                               mem_clk_n,
                               phy_clk_1x,
                               resync_clk_2x,
                               cs_n_clk_2x,
                               write_clk_2x,
                               half_rate_clk,

                               reset_ac_clk_2x_n,
                               reset_measure_clk_2x_n,
                               reset_mem_clk_2x_n,
                               reset_phy_clk_1x_n,
                               reset_resync_clk_2x_n,
                               reset_write_clk_2x_n,
                               reset_cs_n_clk_2x_n,
                               reset_rdp_phy_clk_1x_n,
                               mem_reset_n,

                               reset_request_n, // new output

                               phs_shft_busy,

                               seq_pll_inc_dec_n,
                               seq_pll_select,
                               seq_pll_start_reconfig,

                               mimic_data_2x,

                               seq_clk_disable,
                               ctrl_clk_disable

                              ) /* synthesis altera_attribute="SUPPRESS_DA_RULE_INTERNAL=\"R101,C104\"" */;

// Note the peculiar ranging below is necessary to use a generated CASE statement
// later in the code :
parameter       AC_PHASE                     =      "MEM_CLK";
parameter       CLOCK_INDEX_WIDTH            =              3;
parameter [0:0] CAPTURE_MIMIC_PATH           =              0;
parameter [0:0] DDR_MIMIC_PATH_EN            =              1;
parameter [0:0] DEDICATED_MEMORY_CLK_EN      =              0;
parameter       DLL_EXPORT_IMPORT            =         "NONE";
parameter       DWIDTH_RATIO                 =              4;
parameter       LOCAL_IF_CLK_PS              =           4000;
parameter       MEM_IF_CLK_PAIR_COUNT        =              3;
parameter       MEM_IF_CLK_PS                =           4000;
parameter       MEM_IF_CS_WIDTH              =              2;
parameter       MEM_IF_DQ_PER_DQS            =              8;
parameter       MEM_IF_DQS_WIDTH             =              8;
parameter       MEM_IF_DWIDTH                =             64;
parameter       MIF_FILENAME                 =      "PLL.MIF";
parameter       PLL_EXPORT_IMPORT            =         "NONE";
parameter       PLL_REF_CLK_PS               =           4000;
parameter       PLL_TYPE                     =     "ENHANCED";
parameter       SPEED_GRADE                  =           "C3";
parameter       DLL_DELAY_BUFFER_MODE        =         "HIGH";
parameter       DLL_DELAY_CHAIN_LENGTH       =             10;
parameter       DQS_OUT_MODE                 = "DELAY_CHAIN2";
parameter       DQS_PHASE                    =             72;
parameter       SCAN_CLK_DIVIDE_BY           =              2;
parameter       USE_MEM_CLK_FOR_ADDR_CMD_CLK =              1;

// Clock/reset inputs :
input  wire                                                               global_reset_n;
input  wire                                                               pll_ref_clk;
input  wire                                                               soft_reset_n;
input  wire                                                               seq_rdp_reset_req_n;

// Clock/reset outputs :
(* altera_attribute = "-name global_signal   global_clock" *) output wire ac_clk_2x;
(* altera_attribute = "-name global_signal regional_clock" *) output wire measure_clk_2x;
(* altera_attribute = "-name global_signal   global_clock" *) output wire mem_clk_2x;
inout wire [MEM_IF_CLK_PAIR_COUNT - 1 : 0]                                mem_clk;
inout wire [MEM_IF_CLK_PAIR_COUNT - 1 : 0]                                mem_clk_n;
(* altera_attribute = "-name global_signal   global_clock" *) output wire phy_clk_1x;
(* altera_attribute = "-name global_signal regional_clock" *) output wire resync_clk_2x;
(* altera_attribute = "-name global_signal   global_clock" *) output wire cs_n_clk_2x;
(* altera_attribute = "-name global_signal   global_clock" *) output wire write_clk_2x;
output wire                                                               half_rate_clk;

output wire                                                               reset_ac_clk_2x_n;
output wire                                                               reset_measure_clk_2x_n;
output wire                                                               reset_mem_clk_2x_n;
output  reg                                                               reset_phy_clk_1x_n;
output wire                                                               reset_resync_clk_2x_n;
output wire                                                               reset_write_clk_2x_n;
output wire                                                               reset_cs_n_clk_2x_n;
output wire                                                               reset_rdp_phy_clk_1x_n;
output wire                                                               mem_reset_n;

// This is the PLL locked signal :
output wire                                                               reset_request_n;

// Misc I/O :
output reg                                                                phs_shft_busy;

input  wire                                                               seq_pll_inc_dec_n;
input  wire [CLOCK_INDEX_WIDTH - 1 : 0 ]                                  seq_pll_select;
input  wire                                                               seq_pll_start_reconfig;

output wire                                                               mimic_data_2x;

input wire                                                                seq_clk_disable;
input wire [MEM_IF_CLK_PAIR_COUNT - 1 : 0]                                ctrl_clk_disable;



(*preserve*) reg                                                          seq_pll_start_reconfig_ams;
(*preserve*) reg                                                          seq_pll_start_reconfig_r;
(*preserve*) reg                                                          seq_pll_start_reconfig_2r;
(*preserve*) reg                                                          seq_pll_start_reconfig_3r;

wire                                                                      pll_scanclk;
wire                                                                      pll_scanread;
wire                                                                      pll_scanwrite;
wire                                                                      pll_scandata;
wire                                                                      pll_scandone;
wire                                                                      pll_scandataout;

reg                                                                       pll_new_dir;
reg [2:0]                                                                 pll_new_phase;
wire                                                                      pll_phase_auto_calibrate_pulse;

reg [`CLK_PLL_RECONFIG_FSM_WIDTH-1:0]                                     pll_reconfig_state;

reg                                                                       pll_reconfig_initialised;
reg [CLOCK_INDEX_WIDTH - 1 : 0 ]                                          pll_current_phase;
reg                                                                       pll_current_dir;

reg [`CLK_PLL_RECONFIG_FSM_WIDTH-1:0]                                     next_pll_reconfig_state;

reg                                                                       next_pll_reconfig_initialised;
reg [CLOCK_INDEX_WIDTH - 1 : 0 ]                                          next_pll_current_phase;
reg                                                                       next_pll_current_dir;

(*preserve*) reg                                                          pll_reprogram_request_pulse;   // 1 scan clk cycle long
(*preserve*) reg                                                          pll_reprogram_request_pulse_r;
(*preserve*) reg                                                          pll_reprogram_request_pulse_2r;
wire                                                                      pll_reprogram_request_long_pulse; // 3 scan clk cycles long
(*preserve*) reg                                                          pll_reprogram_request;

wire                                                                      pll_reconfig_busy;
reg                                                                       pll_reconfig;
reg                                                                       pll_reconfig_write_param;
reg [3:0]                                                                 pll_reconfig_counter_type;
reg [8:0]                                                                 pll_reconfig_data_in;

wire                                                                      pll_locked;


wire                                                                      phy_internal_reset_n;
wire                                                                      pll_reset;

(*preserve*) reg                                                          global_pre_clear;
wire                                                                      global_or_soft_reset_n;

(*preserve*) reg                                                          clk_div_reset_ams_n   = 1'b0;
(*preserve*) reg                                                          clk_div_reset_ams_n_r = 1'b0;

(*preserve*) reg                                                          pll_reconfig_reset_ams_n   = 1'b0;
(*preserve*) reg                                                          pll_reconfig_reset_ams_n_r = 1'b0;

(*preserve*) reg                                                          reset_master_ams;

wire [MEM_IF_CLK_PAIR_COUNT - 1 : 0]                                      mimic_data_2x_internal;

// Create the scan clock.  This is a divided-down version of the PLL reference clock.
// The scan chain will have an Fmax of around 100MHz, and so initially the scan clock is
// created by a divide-by 4 circuit to allow plenty of margin with the expected reference
// clock frequency of 100MHz.  This may be changed via the parameter.

reg [2:0]                                                                 divider  = 3'h0;
(*preserve*) reg                                                          scan_clk = 1'b0;


(*preserve*) reg                                                          global_reset_ams_n   = 1'b0;
(*preserve*) reg                                                          global_reset_ams_n_r = 1'b0;


wire                                                                      pll_phase_done;

(*preserve*) reg [2:0]                                                    seq_pll_start_reconfig_ccd_pipe;

(*preserve*) reg                                                          seq_pll_inc_dec_ccd;
(*preserve*) reg [CLOCK_INDEX_WIDTH - 1 : 0]                              seq_pll_select_ccd ;

wire                                                                      pll_reconfig_reset_n;

wire                                                                      clk_divider_reset_n;


// Output the PLL locked signal to be used as a reset_request_n - IE. reset when the PLL loses
// lock :
assign reset_request_n = pll_locked;



// Reset the scanclk clock divider if we either have a global_reset or the PLL loses lock
assign pll_reconfig_reset_n = global_reset_n && pll_locked;


// Clock divider circuit reset generation.
always @(posedge phy_clk_1x or negedge pll_reconfig_reset_n)
begin

    if (pll_reconfig_reset_n == 1'b0)
    begin
        clk_div_reset_ams_n   <= 1'b0;
        clk_div_reset_ams_n_r <= 1'b0;
    end

    else
    begin
        clk_div_reset_ams_n   <= 1'b1;
        clk_div_reset_ams_n_r <= clk_div_reset_ams_n;
    end

end

// PLL reconfig and synchronisation circuit reset generation.
always @(posedge scan_clk or negedge pll_reconfig_reset_n)
begin

    if (pll_reconfig_reset_n == 1'b0)
    begin
        pll_reconfig_reset_ams_n   <= 1'b0;
        pll_reconfig_reset_ams_n_r <= 1'b0;
    end

    else
    begin
        pll_reconfig_reset_ams_n   <= 1'b1;
        pll_reconfig_reset_ams_n_r <= pll_reconfig_reset_ams_n;
    end

end

// Create the scan clock.  Used for PLL reconfiguring in this block.

// Clock divider reset is the direct output of the AMS flops :
assign clk_divider_reset_n = clk_div_reset_ams_n_r;

generate

    if (SCAN_CLK_DIVIDE_BY == 1)
    begin : no_scan_clk_divider

        always @(phy_clk_1x)
        begin
            scan_clk = phy_clk_1x;
        end

    end

    else
    begin : gen_scan_clk

        always @(posedge phy_clk_1x or negedge clk_divider_reset_n)
        begin

            if (clk_divider_reset_n == 1'b0)
            begin
                scan_clk  <= 1'b0;
                divider   <= 3'h0;
            end

            else
            begin

                // This method of clock division does not require "divider" to be used
                // as an intermediate clock:
                if (divider == (SCAN_CLK_DIVIDE_BY / 2 - 1))
                begin
                    scan_clk <= ~scan_clk; // Toggle
                    divider  <= 3'h0;
                end

                else
                begin
                    scan_clk <= scan_clk; // Do not toggle
                    divider  <= divider + 3'h1;
                end

            end

        end

    end

endgenerate




// NB. This lookup table shall be different for CIII/SIII
// The PLL phasecounterselect is 3 bits wide, therefore hardcode the output to 3 bits :
function [2:0] lookup;

input [CLOCK_INDEX_WIDTH-1:0] seq_num;
begin
    casez (seq_num)
    3'b000  : lookup = 3'b010; // legal code
    3'b001  : lookup = 3'b011; // legal code
    3'b010  : lookup = 3'b111; // illegal - return code 3'b111
    3'b011  : lookup = 3'b100; // legal code
    3'b100  : lookup = 3'b110; // legal code
    3'b101  : lookup = 3'b101; // legal code
    3'b110  : lookup = 3'b111; // illegal - return code 3'b111
    3'b111  : lookup = 3'b111; // illegal - return code 3'b111
    default : lookup = 3'bxxx; // X propagation
    endcase
end

endfunction



// Metastable-harden seq_pll_start_reconfig signal (from the phy clock domain). NB:
// by double-clocking this signal, it is not necessary to double-clock the
// seq_pll_inc_dec_n and seq_pll_select signals - since they will only get
// used on a positive edge of the metastable-hardened seq_pll_start_reconfig signal (so
// should be stable by that point).

always @(posedge phy_clk_1x or negedge reset_phy_clk_1x_n)
begin

    if (reset_phy_clk_1x_n == 1'b0)
    begin
        seq_pll_inc_dec_ccd             <= 1'b0;
        seq_pll_select_ccd              <= {CLOCK_INDEX_WIDTH{1'b0}};
        seq_pll_start_reconfig_ccd_pipe <= 3'b000;
    end

    // Generate 'ccd' Cross Clock Domain signals :
    else
    begin

        seq_pll_start_reconfig_ccd_pipe <= {seq_pll_start_reconfig_ccd_pipe[1:0], seq_pll_start_reconfig};

        if (seq_pll_start_reconfig == 1'b1 && seq_pll_start_reconfig_ccd_pipe[0] == 1'b0)
        begin
            seq_pll_inc_dec_ccd <= seq_pll_inc_dec_n;
            seq_pll_select_ccd  <= seq_pll_select;
        end

    end

end


always @(posedge scan_clk or negedge pll_reconfig_reset_ams_n_r)
begin

    if (pll_reconfig_reset_ams_n_r == 1'b0)
    begin
        seq_pll_start_reconfig_ams     <= 1'b0;
        seq_pll_start_reconfig_r       <= 1'b0;
        seq_pll_start_reconfig_2r      <= 1'b0;
        seq_pll_start_reconfig_3r      <= 1'b0;

        pll_reprogram_request_pulse    <= 1'b0;
        pll_reprogram_request_pulse_r  <= 1'b0;
        pll_reprogram_request_pulse_2r <= 1'b0;
        pll_reprogram_request          <= 1'b0;
    end

    else
    begin
        seq_pll_start_reconfig_ams     <= seq_pll_start_reconfig_ccd_pipe[2];
        seq_pll_start_reconfig_r       <= seq_pll_start_reconfig_ams;
        seq_pll_start_reconfig_2r      <= seq_pll_start_reconfig_r;
        seq_pll_start_reconfig_3r      <= seq_pll_start_reconfig_2r;

        pll_reprogram_request_pulse    <= pll_phase_auto_calibrate_pulse;
        pll_reprogram_request_pulse_r  <= pll_reprogram_request_pulse;
        pll_reprogram_request_pulse_2r <= pll_reprogram_request_pulse_r;
        pll_reprogram_request          <= pll_reprogram_request_long_pulse;

    end

end



// Rising-edge detect to generate a single phase shift step
assign pll_phase_auto_calibrate_pulse = ~seq_pll_start_reconfig_3r && seq_pll_start_reconfig_2r;

// extend the phase shift request pulse to be 3 scan clk cycles long.
assign pll_reprogram_request_long_pulse = pll_reprogram_request_pulse || pll_reprogram_request_pulse_r || pll_reprogram_request_pulse_2r;



// Register the Phase step settings
always @(posedge scan_clk or negedge pll_reconfig_reset_ams_n_r)
begin
    if (pll_reconfig_reset_ams_n_r == 1'b0)
    begin
        pll_new_dir   <= 1'b0;
        pll_new_phase <= 3'h0; // CIII PLL has 3bit phase select
    end

    else
    begin

        if (pll_phase_auto_calibrate_pulse)
        begin
            pll_new_dir   <= seq_pll_inc_dec_ccd;
            pll_new_phase <= lookup(seq_pll_select_ccd);
        end

    end
end



// generate the busy signal - just the inverse of the done o/p from the pll OR'd with the reprogram request (in case the done o/p is missed).
always @(posedge scan_clk or negedge pll_reconfig_reset_ams_n_r)
begin

    if (pll_reconfig_reset_ams_n_r == 1'b0)
        phs_shft_busy <= 1'b0;

    else
        phs_shft_busy <= pll_reprogram_request || ~pll_phase_done;

end




// Gate the soft reset input (from SOPC builder for example) with the PLL
// locked signal :
assign global_or_soft_reset_n  = soft_reset_n && global_reset_n;

// Create the PHY internal reset signal :
assign phy_internal_reset_n = pll_locked && global_or_soft_reset_n;

// The PLL resets only on a global reset :
assign pll_reset = !global_reset_n;


generate

    // Half-rate mode :
    if (DWIDTH_RATIO == 4)
    begin


        //
        ddr2_64bit_phy_alt_mem_phy_pll pll(
                    .areset(pll_reset),
            .inclk0(pll_ref_clk),
            .phasecounterselect(pll_new_phase),
            .phasestep(pll_reprogram_request),
            .phaseupdown(pll_new_dir),
            .scanclk(scan_clk),
            .c0(phy_clk_1x),
            .c1(mem_clk_2x),
            .c2(write_clk_2x),
            .c3(resync_clk_2x),
            .c4(measure_clk_2x),
            .locked(pll_locked),
            .phasedone(pll_phase_done)
        );

        assign half_rate_clk = phy_clk_1x;

    end

    // Full-rate mode :
    else
    begin


        //
        ddr2_64bit_phy_alt_mem_phy_pll pll(
                    .areset(pll_reset),
            .inclk0(pll_ref_clk),
            .phasecounterselect(pll_new_phase),
            .phasestep(pll_reprogram_request),
            .phaseupdown(pll_new_dir),
            .scanclk(scan_clk),
            .c0(half_rate_clk),
            .c1(mem_clk_2x),
            .c2(write_clk_2x),
            .c3(resync_clk_2x),
            .c4(measure_clk_2x),
            .locked(pll_locked),
            .phasedone(pll_phase_done)
        );

        // NB. phy_clk_1x is now full-rate, despite the "1x" naming convention :
        assign phy_clk_1x = mem_clk_2x;

    end

endgenerate



generate

    if (USE_MEM_CLK_FOR_ADDR_CMD_CLK == 1)
    begin

        assign ac_clk_2x   = mem_clk_2x;
        assign cs_n_clk_2x = mem_clk_2x;

    end

    else
    begin

        assign ac_clk_2x   = write_clk_2x;
        assign cs_n_clk_2x = write_clk_2x;

    end

endgenerate



//NB. cannot use altddio_out instantiations, as need the combout and regout outputs for mimic path.
generate
genvar clk_pair;

    case ({DDR_MIMIC_PATH_EN, CAPTURE_MIMIC_PATH, DEDICATED_MEMORY_CLK_EN})

    // Mimic path option 1 - this is the default configuration
    default :
    begin

    for (clk_pair = 0 ; clk_pair < MEM_IF_CLK_PAIR_COUNT; clk_pair = clk_pair + 1)
    begin : DDR_CLK_OUT

        // Instance "mem_clk" output pad, with appropriate mimic path connections :
        altddio_bidir   #(
            .extend_oe_disable ("UNUSED"),
            .implement_input_in_lcell ("UNUSED"),
            .intended_device_family ("Cyclone III"),
            .invert_output ("OFF"),
            .lpm_type ("altddio_bidir"),
            .oe_reg ("UNUSED"),
            .power_up_high ("OFF"),
            .width (1)

        )ddr_clk_out_p (
            .padio (mem_clk[clk_pair]),
            .outclock (~mem_clk_2x),
            .inclock (measure_clk_2x),
            .oe (1'b1),
            .datain_h (1'b0),
            .datain_l (1'b1),
            .dataout_h (mimic_data_2x_internal[clk_pair]),
            .dataout_l (),
            .aclr (seq_clk_disable || ctrl_clk_disable[clk_pair]),
            .aset (),
            .combout (),
            .dqsundelayedout (),
            .inclocken (),
            .oe_out (),
            .outclocken (1'b1),
            .sclr (1'b0),
            .sset (1'b0)
        );

        // Instance "mem_clk_n" output pad, no mimic connections made as these are on the
        // 'mem_clk' :
        altddio_bidir   #(
            .extend_oe_disable ("UNUSED"),
            .implement_input_in_lcell ("UNUSED"),
            .intended_device_family ("Cyclone III"),
            .invert_output ("OFF"),
            .lpm_type ("altddio_bidir"),
            .oe_reg ("UNUSED"),
            .power_up_high ("OFF"),
            .width (1)
        )ddr_clk_out_n (
            .padio (mem_clk_n[clk_pair]),
            .outclock (mem_clk_2x),
            .inclock (),
            .oe (1'b1),
            .datain_h (1'b0),
            .datain_l (1'b1),
            .dataout_h (),
            .dataout_l (),
            .aclr (seq_clk_disable || ctrl_clk_disable[clk_pair]),
            .aset (),
            .combout (),
            .dqsundelayedout (),
            .inclocken (),
            .oe_out (),
            .outclocken (1'b1),
            .sclr (1'b0),
            .sset (1'b0)
        );


    end //for

    // Pick off the mimic data from the first internal mimic_data signal :
    assign mimic_data_2x = mimic_data_2x_internal[0];

    end // caseitem

    endcase

endgenerate





// Master reset generation :
always @(posedge phy_clk_1x or negedge phy_internal_reset_n)
begin

    if (phy_internal_reset_n == 1'b0)
    begin
        reset_master_ams   <= 1'b0;
        global_pre_clear    <= 1'b0;
    end

    else
    begin
        reset_master_ams   <= 1'b1;
        global_pre_clear    <= reset_master_ams;
    end

end


// phy_clk reset generation :
always @(posedge phy_clk_1x or negedge global_pre_clear)
begin

    if (global_pre_clear == 1'b0)
    begin
        reset_phy_clk_1x_n <= 1'b0;
    end

    else
    begin
        reset_phy_clk_1x_n <= global_pre_clear;
    end

end




// NB. phy_clk reset is generated above.


// phy_clk reset generation for read datapaths :
//
ddr2_64bit_phy_alt_mem_phy_reset_pipe # (.PIPE_DEPTH (2) ) reset_rdp_phy_clk_pipe(
     .clock     (phy_clk_1x),
     .pre_clear (seq_rdp_reset_req_n && global_pre_clear),
     .reset_out (reset_rdp_phy_clk_1x_n)
);


// mem_clk reset generation :

//
ddr2_64bit_phy_alt_mem_phy_reset_pipe # (.PIPE_DEPTH (2) ) mem_pipe (
    .clock     (mem_clk_2x),
    .pre_clear (global_pre_clear),
    .reset_out (mem_reset_n)
);

// ac_clk_2x reset generation :
//
ddr2_64bit_phy_alt_mem_phy_reset_pipe # (.PIPE_DEPTH (2) ) ac_clk_pipe_2x (
    .clock     (ac_clk_2x),
    .pre_clear (global_pre_clear),
    .reset_out (reset_ac_clk_2x_n)
);

// measure_clk_2x reset generation :
//
ddr2_64bit_phy_alt_mem_phy_reset_pipe # (.PIPE_DEPTH (2) ) measure_clk_pipe (
    .clock     (measure_clk_2x),
    .pre_clear (global_pre_clear),
    .reset_out (reset_measure_clk_2x_n)
);

// mem_clk_2x reset generation :
//
ddr2_64bit_phy_alt_mem_phy_reset_pipe # (.PIPE_DEPTH (4) ) mem_clk_pipe(
    .clock     (mem_clk_2x),
    .pre_clear (global_pre_clear),
    .reset_out (reset_mem_clk_2x_n)
);



// resync_clk_2x reset generation :
//
ddr2_64bit_phy_alt_mem_phy_reset_pipe # (.PIPE_DEPTH (2) ) resync_clk_pipe(
    .clock     (resync_clk_2x),
    .pre_clear (seq_rdp_reset_req_n && global_pre_clear),
    .reset_out (reset_resync_clk_2x_n)
);

// write_clk_2x reset generation :
//
ddr2_64bit_phy_alt_mem_phy_reset_pipe # (.PIPE_DEPTH (4) ) write_clk_pipe(
    .clock     (write_clk_2x),
    .pre_clear (global_pre_clear),
    .reset_out (reset_write_clk_2x_n)
);

// cs_clk_2x reset generation :
//
ddr2_64bit_phy_alt_mem_phy_reset_pipe # (.PIPE_DEPTH (4) ) cs_n_clk_pipe_2x(
    .clock     (cs_n_clk_2x),
    .pre_clear (global_pre_clear),
    .reset_out (reset_cs_n_clk_2x_n)
);


endmodule

//

`default_nettype none

`ifdef ALT_MEM_PHY_DEFINES
`else
`include "alt_mem_phy_defines.v"
`endif

//
module ddr2_64bit_phy_alt_mem_phy_ac (
                            clk_2x,
                            reset_2x_n,
                            phy_clk_1x,
                            ctl_add_1t_ac_lat,
                            ctl_negedge_en,
                            ctl_add_intermediate_regs,
                            period_sel,
                            seq_ac_sel,
                            ctl_ac_h,
                            ctl_ac_l,
                            seq_ac_h,
                            seq_ac_l,
                            mem_ac );
parameter POWER_UP_HIGH = 1;
parameter DWIDTH_RATIO  = 4;

// NB. clk_2x could be either ac_clk_2x or cs_n_clk_2x :
input wire   clk_2x;
input wire   reset_2x_n;
input wire   phy_clk_1x;

input wire   ctl_add_1t_ac_lat;
input wire   ctl_negedge_en;
input wire   ctl_add_intermediate_regs;
input wire   period_sel;
input wire   seq_ac_sel;
             
input wire   ctl_ac_h;
input wire   ctl_ac_l;
             
input wire   seq_ac_h;
input wire   seq_ac_l;

output wire  mem_ac;

(* preserve *) reg  ac_h_r     = POWER_UP_HIGH[0];
(* preserve *) reg  ac_l_r     = POWER_UP_HIGH[0];
(* preserve *) reg  ac_h_2r    = POWER_UP_HIGH[0];
(* preserve *) reg  ac_l_2r    = POWER_UP_HIGH[0];
(* preserve *) reg  ac_1t      = POWER_UP_HIGH[0];
(* preserve *) reg  ac_2x      = POWER_UP_HIGH[0];
(* preserve *) reg  ac_2x_r    = POWER_UP_HIGH[0];
(* preserve *) reg  ac_2x_2r   = POWER_UP_HIGH[0];
(* preserve *) reg  ac_2x_mux  = POWER_UP_HIGH[0];

reg ac_2x_retime     = POWER_UP_HIGH[0];
reg ac_2x_retime_r   = POWER_UP_HIGH[0];
reg ac_2x_deg_choice = POWER_UP_HIGH[0];

reg ac_h;
reg ac_l;

wire reset_2x ;
assign reset_2x         = ~reset_2x_n;

generate 
    
    if (DWIDTH_RATIO == 4) //HR
    begin : hr_mux_gen
    
        // Half Rate DDR memory types require an extra cycle of latency :
        always @(posedge phy_clk_1x)
        begin
        
            casez(seq_ac_sel)
            
            1'b0 :
            begin
                ac_l <= ctl_ac_l;
                ac_h <= ctl_ac_h;
            end
            
            1'b1 :
            begin
                ac_l <= seq_ac_l;
                ac_h <= seq_ac_h;
            end
        
            endcase
            
        end //always   
        
    end
    
    else // FR
    begin : fr_passthru_gen
        
        // Note that "_h" is unused in full-rate and no latency
        // is required :
        always @*
        begin
            
            casez(seq_ac_sel)
            
            1'b0 :
            begin
                ac_l <= ctl_ac_l;
            end
            
            1'b1 :
            begin
                ac_l <= seq_ac_l;
            end
            
            endcase
             
        end
    end
    
endgenerate    

generate

    if (DWIDTH_RATIO == 4)
    begin : half_rate

        // Initial registering of inputs :
        always @(posedge phy_clk_1x)
        begin
            ac_h_r  <= ac_h;
            ac_l_r  <= ac_l;
        end


        // Select high and low phases periodically to create the _2x signal :
        always @*
        begin

            casez(period_sel)

            1'b0     : ac_2x = ac_l_2r;
            1'b1     : ac_2x = ac_h_2r;
            default  : ac_2x = 1'bx; // X propagaton

            endcase

        end
        
        always @(posedge clk_2x)
        begin

            // Second stage of registering - on clk_2x
            ac_h_2r <= ac_h_r;
            ac_l_2r <= ac_l_r;

            // 1t registering - used if ctl_add_1t_ac_lat is true
            ac_1t   <= ac_2x;

            // AC_PHASE==270 requires an extra cycle of delay :
            ac_2x_deg_choice <= ac_1t;

            // If not at AC_PHASE==270, ctl_add_intermediate_regs shall be zero :
            if (ctl_add_intermediate_regs == 1'b0)
            begin
            
                if (ctl_add_1t_ac_lat == 1'b1)
                begin
                    ac_2x_r <= ac_1t;
                end
                
                else
                begin
                    ac_2x_r <= ac_2x;
                end
                
            end
            
            // If at AC_PHASE==270, ctl_add_intermediate_regs shall be one
            // and an extra cycle delay is required :
            else
            begin
           
                if (ctl_add_1t_ac_lat == 1'b1)
                begin
                    ac_2x_r <= ac_2x_deg_choice;
                end
                
                else
                begin
                    ac_2x_r <= ac_1t;
                end            
            
            end

            // Register the above output for use when ctl_negedge_en is set :
            ac_2x_2r <= ac_2x_r;

        end


        // Determine whether to select the "_r" or "_2r" variant :
        always @*
        begin

            casez(ctl_negedge_en)

            1'b0     : ac_2x_mux = ac_2x_r;
            1'b1     : ac_2x_mux = ac_2x_2r;
            default  : ac_2x_mux = 1'bx; // X propagaton

            endcase

        end

        if (POWER_UP_HIGH == 1)
        begin

            altddio_out #(
                .extend_oe_disable      ("UNUSED"),
                .intended_device_family ("Cyclone III"),
                .lpm_hint               ("UNUSED"),
                .lpm_type               ("altddio_out"),
                .oe_reg                 ("UNUSED"),
                .power_up_high          ("ON"),
                .width                  (1)
            ) addr_pin (
                .aset                   (reset_2x),
                .datain_h               (ac_2x_mux),
                .datain_l               (ac_2x_r),
                .dataout                (mem_ac),
                .oe                     (1'b1),
                .outclock               (clk_2x),
                .outclocken             (1'b1),
                .aclr                   (),
                .sset                   (),
                .sclr                   (),
                .oe_out                 ()
            );

        end

        else
        begin

            altddio_out #(
                .extend_oe_disable      ("UNUSED"),
                .intended_device_family ("Cyclone III"),
                .lpm_hint               ("UNUSED"),
                .lpm_type               ("altddio_out"),
                .oe_reg                 ("UNUSED"),
                .power_up_high          ("OFF"),
                .width                  (1)
            ) addr_pin (
                .aclr                   (reset_2x),
                .aset                   (),
                .datain_h               (ac_2x_mux),
                .datain_l               (ac_2x_r),
                .dataout                (mem_ac),
                .oe                     (1'b1),
                .outclock               (clk_2x),
                .outclocken             (1'b1),
                .sset                   (),
                .sclr                   (),
                .oe_out                 ()
                
            );

        end

    end // Half-rate

    // full-rate
    else
    begin : full_rate
   
        always @(posedge phy_clk_1x)
        begin
        
            // 1t registering - only used if ctl_add_1t_ac_lat is true
            ac_1t <= ac_l;
        
            // add 1 addr_clock delay if "Add 1T" is set:
           if (ctl_add_1t_ac_lat == 1'b1)
               ac_2x <= ac_1t;
           else
               ac_2x <= ac_l;
        
        end
        
        always @(posedge clk_2x)
        begin
            ac_2x_deg_choice <= ac_2x;
        end   
        
        // Note this is for 270 degree operation to align it to the correct clock phase.
        always @*
        begin
            casez(ctl_add_intermediate_regs)
                1'b0     : ac_2x_r = ac_2x;
                1'b1     : ac_2x_r = ac_2x_deg_choice;
                default  : ac_2x_r = 1'bx; // X propagaton
            endcase
        end
        
        always @(posedge clk_2x)
        begin
            ac_2x_2r <= ac_2x_r;
        end
        
        // Determine whether to select the "_r" or "_2r" variant :
        always @*
        begin
            casez(ctl_negedge_en)
                1'b0     : ac_2x_mux = ac_2x_r;
                1'b1     : ac_2x_mux = ac_2x_2r;
                default  : ac_2x_mux = 1'bx; // X propagaton
            endcase
        end
                       
        if (POWER_UP_HIGH == 1)
        begin
        
            altddio_out #(
                .extend_oe_disable      ("UNUSED"),
                .intended_device_family ("Cyclone III"),
                .lpm_hint               ("UNUSED"),
                .lpm_type               ("altddio_out"),
                .oe_reg                 ("UNUSED"),
                .power_up_high          ("ON"),
                .width                  (1)
            ) addr_pin (
                .aset                   (reset_2x),
                .datain_h               (ac_2x_mux),
                .datain_l               (ac_2x_r),
                .dataout                (mem_ac),
                .oe                     (1'b1),
                .outclock               (clk_2x),
                .outclocken             (1'b1),
                .aclr                   (),
                .sclr                   (),
                .sset                   (),
                .oe_out                 ()
            );
        
        end
        
        else
        begin
        
            altddio_out #(
                .extend_oe_disable      ("UNUSED"),
                .intended_device_family ("Cyclone III"),
                .lpm_hint               ("UNUSED"),
                .lpm_type               ("altddio_out"),
                .oe_reg                 ("UNUSED"),
                .power_up_high          ("OFF"),
                .width                  (1)
            ) addr_pin (
                .aclr                   (reset_2x),
                .datain_h               (ac_2x_mux),
                .datain_l               (ac_2x_r),
                .dataout                (mem_ac),
                .oe                     (1'b1),
                .outclock               (clk_2x),
                .outclocken             (1'b1),
                .aset                   (),
                .sclr                   (),
                .sset                   (),
                .oe_out                 ()
            );
            
        end // else: !if(POWER_UP_HIGH == 1) 

    end // block: full_rate

endgenerate

endmodule

`default_nettype wire

//

`default_nettype none

`ifdef ALT_MEM_PHY_DEFINES
`else
`include "alt_mem_phy_defines.v"
`endif

//
module ddr2_64bit_phy_alt_mem_phy_addr_cmd ( 
                                  ac_clk_2x,
                                  cs_n_clk_2x,
                                  phy_clk_1x,
                                  reset_ac_clk_2x_n,
                                  reset_cs_n_clk_2x_n,
                                  
                                  // Addr/cmd interface from controller
                                  ctl_add_1t_ac_lat,
                                  ctl_add_1t_odt_lat,				   
                                  ctl_add_intermediate_regs,
                                  
                                  ctl_negedge_en,
				                  ctl_mem_addr_h,
                                  ctl_mem_addr_l,
                                  ctl_mem_ba_h,
                                  ctl_mem_ba_l,
                                  ctl_mem_cas_n_h,
                                  ctl_mem_cas_n_l,
                                  ctl_mem_cke_h,
                                  ctl_mem_cke_l,
                                  ctl_mem_cs_n_h,
                                  ctl_mem_cs_n_l,
                                  ctl_mem_odt_h,
                                  ctl_mem_odt_l,
                                  ctl_mem_ras_n_h,
                                  ctl_mem_ras_n_l,
                                  ctl_mem_we_n_h,
                                  ctl_mem_we_n_l,
                                  
                                  // Interface from Sequencer, used for calibration
                                  // as the MRS registers need to be controlled :
                                  seq_addr_h,
                                  seq_addr_l,
                                  seq_ba_h,
                                  seq_ba_l,
                                  seq_cas_n_h,
                                  seq_cas_n_l,
                                  seq_cke_h,
                                  seq_cke_l,
                                  seq_cs_n_h,
                                  seq_cs_n_l,
                                  seq_odt_h,
                                  seq_odt_l,
                                  seq_ras_n_h,
                                  seq_ras_n_l,
                                  seq_we_n_h,
                                  seq_we_n_l,
                                  
                                  seq_ac_sel,
                                  
                                  mem_addr,
                                  mem_ba,
                                  mem_cas_n,
                                  mem_cke,
                                  mem_cs_n,
                                  mem_odt,
                                  mem_ras_n,
                                  mem_we_n );


parameter DWIDTH_RATIO            =           4;
parameter MEM_ADDR_CMD_BUS_COUNT  =           1;
parameter MEM_IF_BANKADDR_WIDTH   =           3;
parameter MEM_IF_CS_WIDTH         =           2;
parameter MEM_IF_MEMTYPE          =       "DDR";
parameter MEM_IF_ROWADDR_WIDTH    =          13;

input wire                                  cs_n_clk_2x;
input wire                                  ac_clk_2x;
input wire                                  phy_clk_1x;

input wire                                  reset_ac_clk_2x_n;
input wire                                  reset_cs_n_clk_2x_n;

input wire [MEM_IF_ROWADDR_WIDTH -1:0]      ctl_mem_addr_h;
input wire [MEM_IF_ROWADDR_WIDTH -1:0]      ctl_mem_addr_l;
input wire                                  ctl_add_1t_ac_lat;
input wire                                  ctl_add_1t_odt_lat;   
input wire                                  ctl_negedge_en;
input wire                                  ctl_add_intermediate_regs;    
input wire [MEM_IF_BANKADDR_WIDTH - 1:0]    ctl_mem_ba_h;
input wire [MEM_IF_BANKADDR_WIDTH - 1:0]    ctl_mem_ba_l;
input wire                                  ctl_mem_cas_n_h;
input wire                                  ctl_mem_cas_n_l;
input wire [MEM_IF_CS_WIDTH - 1:0]          ctl_mem_cke_h;
input wire [MEM_IF_CS_WIDTH - 1:0]          ctl_mem_cke_l;
input wire [MEM_IF_CS_WIDTH - 1:0]          ctl_mem_cs_n_h;
input wire [MEM_IF_CS_WIDTH - 1:0]          ctl_mem_cs_n_l;
input wire [MEM_IF_CS_WIDTH - 1:0]          ctl_mem_odt_h;
input wire [MEM_IF_CS_WIDTH - 1:0]          ctl_mem_odt_l;
input wire                                  ctl_mem_ras_n_h;
input wire                                  ctl_mem_ras_n_l;
input wire                                  ctl_mem_we_n_h;
input wire                                  ctl_mem_we_n_l;

input wire [MEM_IF_ROWADDR_WIDTH -1:0]      seq_addr_h;
input wire [MEM_IF_ROWADDR_WIDTH -1:0]      seq_addr_l;
input wire [MEM_IF_BANKADDR_WIDTH - 1:0]    seq_ba_h;
input wire [MEM_IF_BANKADDR_WIDTH - 1:0]    seq_ba_l;
input wire                                  seq_cas_n_h;
input wire                                  seq_cas_n_l;
input wire [MEM_IF_CS_WIDTH - 1:0]          seq_cke_h;
input wire [MEM_IF_CS_WIDTH - 1:0]          seq_cke_l;
input wire [MEM_IF_CS_WIDTH - 1:0]          seq_cs_n_h;
input wire [MEM_IF_CS_WIDTH - 1:0]          seq_cs_n_l;
input wire [MEM_IF_CS_WIDTH - 1:0]          seq_odt_h;
input wire [MEM_IF_CS_WIDTH - 1:0]          seq_odt_l;
input wire                                  seq_ras_n_h;
input wire                                  seq_ras_n_l;
input wire                                  seq_we_n_h;
input wire                                  seq_we_n_l;

input wire                                  seq_ac_sel;

output wire [MEM_IF_ROWADDR_WIDTH - 1 : 0]  mem_addr;
output wire [MEM_IF_BANKADDR_WIDTH - 1 : 0] mem_ba;
output wire                                 mem_cas_n;
output wire [MEM_IF_CS_WIDTH - 1 : 0]       mem_cke;
output wire [MEM_IF_CS_WIDTH - 1 : 0]       mem_cs_n;
output wire [MEM_IF_CS_WIDTH - 1 : 0]       mem_odt;
output wire                                 mem_ras_n;
output wire                                 mem_we_n;


// Periodical select registers - per group of pins
reg  [`ADC_NUM_PIN_GROUPS-1:0]              count_addr = `ADC_NUM_PIN_GROUPS'b0;
reg  [`ADC_NUM_PIN_GROUPS-1:0]              count_addr_2x = `ADC_NUM_PIN_GROUPS'b0;
reg  [`ADC_NUM_PIN_GROUPS-1:0]              count_addr_2x_r = `ADC_NUM_PIN_GROUPS'b0;
reg  [`ADC_NUM_PIN_GROUPS-1:0]              period_sel_addr = `ADC_NUM_PIN_GROUPS'b0;



generate
genvar ia;

for (ia=0; ia<`ADC_NUM_PIN_GROUPS - 1; ia=ia+1)
begin : SELECTS

    always @(posedge phy_clk_1x)
    begin
        count_addr[ia] <= ~count_addr[ia];
    end

    always @(posedge ac_clk_2x)
    begin
        count_addr_2x[ia]   <= count_addr[ia];
        count_addr_2x_r[ia] <= count_addr_2x[ia];
        period_sel_addr[ia] <= ~(count_addr_2x_r[ia] ^ count_addr_2x[ia]);
    end

end

endgenerate


//now generate cs_n period sel, off the dedicated cs_n clock :
always @(posedge phy_clk_1x)
begin
    count_addr[`ADC_CS_N_PERIOD_SEL] <= ~count_addr[`ADC_CS_N_PERIOD_SEL];
end

always @(posedge cs_n_clk_2x)
begin
    count_addr_2x  [`ADC_CS_N_PERIOD_SEL] <= count_addr   [`ADC_CS_N_PERIOD_SEL];
    count_addr_2x_r[`ADC_CS_N_PERIOD_SEL] <= count_addr_2x[`ADC_CS_N_PERIOD_SEL];
    period_sel_addr[`ADC_CS_N_PERIOD_SEL] <= ~(count_addr_2x_r[`ADC_CS_N_PERIOD_SEL] ^ count_addr_2x[`ADC_CS_N_PERIOD_SEL]);
end


// Create the ADDR I/O structure :
	
generate
genvar ib;

    for (ib=0; ib<MEM_IF_ROWADDR_WIDTH; ib=ib+1)
    begin : addr

        //
        ddr2_64bit_phy_alt_mem_phy_ac # ( 
                    .POWER_UP_HIGH (1),
            .DWIDTH_RATIO (DWIDTH_RATIO)
        ) addr_struct (
            .clk_2x                    (ac_clk_2x),
            .reset_2x_n                (1'b1),
            .phy_clk_1x                (phy_clk_1x),
            .ctl_add_1t_ac_lat         (ctl_add_1t_ac_lat),
            .ctl_negedge_en            (ctl_negedge_en),
            .ctl_add_intermediate_regs (ctl_add_intermediate_regs),
            .period_sel                (period_sel_addr[`ADC_ADDR_PERIOD_SEL]),
            .seq_ac_sel                (seq_ac_sel),
            .ctl_ac_h                  (ctl_mem_addr_h[ib]),
            .ctl_ac_l                  (ctl_mem_addr_l[ib]),
            .seq_ac_h                  (seq_addr_h[ib]),
            .seq_ac_l                  (seq_addr_l[ib]),
            .mem_ac                    (mem_addr[ib])
        );
        
    end

endgenerate

// Create the BANK_ADDR I/O structure :
generate
genvar ic;

    for (ic=0; ic<MEM_IF_BANKADDR_WIDTH; ic=ic+1)
    begin : ba

        //
        ddr2_64bit_phy_alt_mem_phy_ac #(
                    .POWER_UP_HIGH (0),
            .DWIDTH_RATIO (DWIDTH_RATIO)
        ) ba_struct  ( 
            .clk_2x                    (ac_clk_2x),
            .reset_2x_n                (1'b1),
            .phy_clk_1x                (phy_clk_1x),
            .ctl_add_1t_ac_lat         (ctl_add_1t_ac_lat),
            .ctl_negedge_en            (ctl_negedge_en),
            .ctl_add_intermediate_regs (ctl_add_intermediate_regs),
            .period_sel                (period_sel_addr[`ADC_BA_PERIOD_SEL]),
            .seq_ac_sel                (seq_ac_sel),
            .ctl_ac_h                  (ctl_mem_ba_h[ic]),
            .ctl_ac_l                  (ctl_mem_ba_l[ic]),
            .seq_ac_h                  (seq_ba_h[ic]),
            .seq_ac_l                  (seq_ba_l[ic]),
            .mem_ac                    (mem_ba[ic])
        );

    end

endgenerate

// Create the CAS_N I/O structure :
//
ddr2_64bit_phy_alt_mem_phy_ac #( 
    .POWER_UP_HIGH (1),
    .DWIDTH_RATIO (DWIDTH_RATIO)

) cas_n_struct ( 
    .clk_2x                    (ac_clk_2x),
    .reset_2x_n                (1'b1),
    .phy_clk_1x                (phy_clk_1x),
    .ctl_add_1t_ac_lat         (ctl_add_1t_ac_lat),
    .ctl_negedge_en            (ctl_negedge_en),
    .ctl_add_intermediate_regs (ctl_add_intermediate_regs),
    .period_sel                (period_sel_addr[`ADC_CAS_N_PERIOD_SEL]),
    .seq_ac_sel                (seq_ac_sel),
    .ctl_ac_h                  (ctl_mem_cas_n_h),
    .ctl_ac_l                  (ctl_mem_cas_n_l),
    .seq_ac_h                  (seq_cas_n_h),
    .seq_ac_l                  (seq_cas_n_l),
    .mem_ac                    (mem_cas_n)
);


// Create the CKE I/O structure :
generate
genvar id;

    for (id=0; id<MEM_IF_CS_WIDTH; id=id+1)
    begin : cke

        //
        ddr2_64bit_phy_alt_mem_phy_ac # (
                    .POWER_UP_HIGH (0),
            .DWIDTH_RATIO (DWIDTH_RATIO)
        ) cke_struct  ( 
            .clk_2x                    (ac_clk_2x),
            .reset_2x_n                (reset_ac_clk_2x_n),
            .phy_clk_1x                (phy_clk_1x),
            .ctl_add_1t_ac_lat         (ctl_add_1t_ac_lat),
            .ctl_negedge_en            (ctl_negedge_en),
            .ctl_add_intermediate_regs (ctl_add_intermediate_regs),
            .period_sel                (period_sel_addr[`ADC_CKE_PERIOD_SEL]),
            .seq_ac_sel                (seq_ac_sel),
            .ctl_ac_h                  (ctl_mem_cke_h[id]),
            .ctl_ac_l                  (ctl_mem_cke_l[id]),
            .seq_ac_h                  (seq_cke_h[id]),
            .seq_ac_l                  (seq_cke_l[id]),
            .mem_ac                    (mem_cke[id])
        );

    end

endgenerate


// Create the CS_N I/O structure.  Note that the 2x clock is different.
generate
genvar ie;

    for (ie=0; ie<MEM_IF_CS_WIDTH; ie=ie+1)
    begin : cs_n

        //
        ddr2_64bit_phy_alt_mem_phy_ac # ( 
                    .POWER_UP_HIGH (1),
            .DWIDTH_RATIO (DWIDTH_RATIO)
        ) cs_n_struct ( 
            .clk_2x                    (cs_n_clk_2x),
            .reset_2x_n                (reset_ac_clk_2x_n),
            .phy_clk_1x                (phy_clk_1x),
            .ctl_add_1t_ac_lat         (ctl_add_1t_ac_lat),
            .ctl_negedge_en            (ctl_negedge_en),
            .ctl_add_intermediate_regs (ctl_add_intermediate_regs),
            .period_sel                (period_sel_addr[`ADC_CS_N_PERIOD_SEL]),
            .seq_ac_sel                (seq_ac_sel),
            .ctl_ac_h                  (ctl_mem_cs_n_h[ie]),
            .ctl_ac_l                  (ctl_mem_cs_n_l[ie]),
            .seq_ac_h                  (seq_cs_n_h[ie]),
            .seq_ac_l                  (seq_cs_n_l[ie]),
            .mem_ac                    (mem_cs_n[ie])
        );

    end

endgenerate


// Create the ODT I/O structure :

generate
genvar ig;

    if (MEM_IF_MEMTYPE != "DDR")
    begin : gen_odt
    
        for (ig=0; ig<MEM_IF_CS_WIDTH; ig=ig+1)
        begin : odt

            //
            ddr2_64bit_phy_alt_mem_phy_ac #(
                    	.POWER_UP_HIGH (0),
        	.DWIDTH_RATIO (DWIDTH_RATIO)
            ) odt_struct  ( 
        	.clk_2x 		           (ac_clk_2x),
        	.reset_2x_n		           (1'b1),
        	.phy_clk_1x		           (phy_clk_1x),
        	.ctl_add_1t_ac_lat	       (ctl_add_1t_odt_lat),
        	.ctl_negedge_en 	       (ctl_negedge_en),
        	.ctl_add_intermediate_regs (ctl_add_intermediate_regs),
        	.period_sel		           (period_sel_addr[`ADC_ODT_PERIOD_SEL]),
        	.seq_ac_sel 	           (seq_ac_sel),
            .ctl_ac_h		           (ctl_mem_odt_h[ig]),
            .ctl_ac_l		           (ctl_mem_odt_l[ig]),
            .seq_ac_h		           (seq_odt_h[ig]),
            .seq_ac_l		           (seq_odt_l[ig]),
            .mem_ac 		           (mem_odt[ig])
            );

        end
	
    end
    
endgenerate


// Create the RAS_N I/O structure :
//
ddr2_64bit_phy_alt_mem_phy_ac # (
    .POWER_UP_HIGH (1),
    .DWIDTH_RATIO (DWIDTH_RATIO)
) ras_n_struct  ( 
    .clk_2x                    (ac_clk_2x),
    .reset_2x_n                (1'b1),
    .phy_clk_1x                (phy_clk_1x),
    .ctl_add_1t_ac_lat         (ctl_add_1t_ac_lat),
    .ctl_negedge_en            (ctl_negedge_en),
    .ctl_add_intermediate_regs (ctl_add_intermediate_regs),
    .period_sel                (period_sel_addr[`ADC_RAS_N_PERIOD_SEL]),
    .seq_ac_sel                (seq_ac_sel),
    .ctl_ac_h                  (ctl_mem_ras_n_h),
    .ctl_ac_l                  (ctl_mem_ras_n_l),
    .seq_ac_h                  (seq_ras_n_h),
    .seq_ac_l                  (seq_ras_n_l),
    .mem_ac                    (mem_ras_n)
);


// Create the WE_N I/O structure :
//
ddr2_64bit_phy_alt_mem_phy_ac # (
    .POWER_UP_HIGH (1),
    .DWIDTH_RATIO (DWIDTH_RATIO)
) we_n_struct  (
    .clk_2x                    (ac_clk_2x),
    .reset_2x_n                (1'b1),
    .phy_clk_1x                (phy_clk_1x),
    .ctl_add_1t_ac_lat         (ctl_add_1t_ac_lat),
    .ctl_negedge_en            (ctl_negedge_en),
    .ctl_add_intermediate_regs (ctl_add_intermediate_regs),
    .period_sel                (period_sel_addr[`ADC_WE_N_PERIOD_SEL]),
    .seq_ac_sel                (seq_ac_sel),
    .ctl_ac_h                  (ctl_mem_we_n_h),
    .ctl_ac_l                  (ctl_mem_we_n_l),
    .seq_ac_h                  (seq_we_n_h),
    .seq_ac_l                  (seq_we_n_l),
    .mem_ac                    (mem_we_n)
);

endmodule

`default_nettype wire

/* Legal Notice: (C)2006 Altera Corporation. All rights reserved.  Your
   use of Altera Corporation's design tools, logic functions and other
   software and tools, and its AMPP partner logic functions, and any
   output files any of the foregoing (including device programming or
   simulation files), and any associated documentation or information are
   expressly subject to the terms and conditions of the Altera Program
   License Subscription Agreement or other applicable license agreement,
   including, without limitation, that your use is for the sole purpose
   of programming logic devices manufactured by Altera and sold by Altera
   or its authorized distributors.  Please refer to the applicable
   agreement for further details. */


/*////////////////////////////////////////////////////////////////////////////-
  Title           : Datapath IO elements

  File:  $RCSfile : alt_mem_phy_dp_io.v,v $

  Last Modified   : $Date: 2013/08/11 $

  Revision        : $Revision: #1 $

  Abstract        : NewPhy Datapath IO atoms.  This block shall connect to both
                    the read and write datapath blocks, abstracting the I/O
                    elements from the remainder of the circuit.  This file
                    instances atoms specific to Cyclone III.
////////////////////////////////////////////////////////////////////////////-*/

`include "alt_mem_phy_defines.v"

//DQS pin assignments
(* altera_attribute = " -name DQS_FREQUENCY 150.0MHz -to dqs[0].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[0].dq[0].dq_ibuf -from dqs[0].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[0].dq[1].dq_ibuf -from dqs[0].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[0].dq[2].dq_ibuf -from dqs[0].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[0].dq[3].dq_ibuf -from dqs[0].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[0].dq[4].dq_ibuf -from dqs[0].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[0].dq[5].dq_ibuf -from dqs[0].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[0].dq[6].dq_ibuf -from dqs[0].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[0].dq[7].dq_ibuf -from dqs[0].dqs_obuf; -name DQ_GROUP 9 -to dm[0].dm_obuf -from dqs[0].dqs_obuf; -name DQS_FREQUENCY 150.0MHz -to dqs[1].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[1].dq[0].dq_ibuf -from dqs[1].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[1].dq[1].dq_ibuf -from dqs[1].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[1].dq[2].dq_ibuf -from dqs[1].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[1].dq[3].dq_ibuf -from dqs[1].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[1].dq[4].dq_ibuf -from dqs[1].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[1].dq[5].dq_ibuf -from dqs[1].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[1].dq[6].dq_ibuf -from dqs[1].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[1].dq[7].dq_ibuf -from dqs[1].dqs_obuf; -name DQ_GROUP 9 -to dm[1].dm_obuf -from dqs[1].dqs_obuf; -name DQS_FREQUENCY 150.0MHz -to dqs[2].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[2].dq[0].dq_ibuf -from dqs[2].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[2].dq[1].dq_ibuf -from dqs[2].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[2].dq[2].dq_ibuf -from dqs[2].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[2].dq[3].dq_ibuf -from dqs[2].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[2].dq[4].dq_ibuf -from dqs[2].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[2].dq[5].dq_ibuf -from dqs[2].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[2].dq[6].dq_ibuf -from dqs[2].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[2].dq[7].dq_ibuf -from dqs[2].dqs_obuf; -name DQ_GROUP 9 -to dm[2].dm_obuf -from dqs[2].dqs_obuf; -name DQS_FREQUENCY 150.0MHz -to dqs[3].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[3].dq[0].dq_ibuf -from dqs[3].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[3].dq[1].dq_ibuf -from dqs[3].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[3].dq[2].dq_ibuf -from dqs[3].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[3].dq[3].dq_ibuf -from dqs[3].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[3].dq[4].dq_ibuf -from dqs[3].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[3].dq[5].dq_ibuf -from dqs[3].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[3].dq[6].dq_ibuf -from dqs[3].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[3].dq[7].dq_ibuf -from dqs[3].dqs_obuf; -name DQ_GROUP 9 -to dm[3].dm_obuf -from dqs[3].dqs_obuf; -name DQS_FREQUENCY 150.0MHz -to dqs[4].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[4].dq[0].dq_ibuf -from dqs[4].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[4].dq[1].dq_ibuf -from dqs[4].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[4].dq[2].dq_ibuf -from dqs[4].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[4].dq[3].dq_ibuf -from dqs[4].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[4].dq[4].dq_ibuf -from dqs[4].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[4].dq[5].dq_ibuf -from dqs[4].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[4].dq[6].dq_ibuf -from dqs[4].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[4].dq[7].dq_ibuf -from dqs[4].dqs_obuf; -name DQ_GROUP 9 -to dm[4].dm_obuf -from dqs[4].dqs_obuf; -name DQS_FREQUENCY 150.0MHz -to dqs[5].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[5].dq[0].dq_ibuf -from dqs[5].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[5].dq[1].dq_ibuf -from dqs[5].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[5].dq[2].dq_ibuf -from dqs[5].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[5].dq[3].dq_ibuf -from dqs[5].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[5].dq[4].dq_ibuf -from dqs[5].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[5].dq[5].dq_ibuf -from dqs[5].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[5].dq[6].dq_ibuf -from dqs[5].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[5].dq[7].dq_ibuf -from dqs[5].dqs_obuf; -name DQ_GROUP 9 -to dm[5].dm_obuf -from dqs[5].dqs_obuf; -name DQS_FREQUENCY 150.0MHz -to dqs[6].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[6].dq[0].dq_ibuf -from dqs[6].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[6].dq[1].dq_ibuf -from dqs[6].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[6].dq[2].dq_ibuf -from dqs[6].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[6].dq[3].dq_ibuf -from dqs[6].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[6].dq[4].dq_ibuf -from dqs[6].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[6].dq[5].dq_ibuf -from dqs[6].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[6].dq[6].dq_ibuf -from dqs[6].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[6].dq[7].dq_ibuf -from dqs[6].dqs_obuf; -name DQ_GROUP 9 -to dm[6].dm_obuf -from dqs[6].dqs_obuf; -name DQS_FREQUENCY 150.0MHz -to dqs[7].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[7].dq[0].dq_ibuf -from dqs[7].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[7].dq[1].dq_ibuf -from dqs[7].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[7].dq[2].dq_ibuf -from dqs[7].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[7].dq[3].dq_ibuf -from dqs[7].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[7].dq[4].dq_ibuf -from dqs[7].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[7].dq[5].dq_ibuf -from dqs[7].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[7].dq[6].dq_ibuf -from dqs[7].dqs_obuf; -name DQ_GROUP 9 -to dqs_group[7].dq[7].dq_ibuf -from dqs[7].dqs_obuf; -name DQ_GROUP 9 -to dm[7].dm_obuf -from dqs[7].dqs_obuf" *)

//
module ddr2_64bit_phy_alt_mem_phy_dp_io (
                                reset_resync_clk_2x_n,
                                resync_clk_2x,
                                mem_clk_2x,
                                write_clk_2x,
                                mem_dm,
                                mem_dq,
                                mem_dqs,
                                dio_rdata_h_2x,
                                dio_rdata_l_2x,
                                wdp_dm_h_2x,
                                wdp_dm_l_2x,
                                wdp_wdata_h_2x,
                                wdp_wdata_l_2x,
                                wdp_wdata_oe_2x,
                                wdp_wdqs_2x,
                                wdp_wdqs_oe_2x
                              );

parameter MEM_IF_CLK_PS             =           4000;
parameter MEM_IF_BANKADDR_WIDTH     =              3;
parameter MEM_IF_CS_WIDTH           =              2;
parameter MEM_IF_DWIDTH             =             64;
parameter MEM_IF_DM_PINS_EN         =              1;
parameter MEM_IF_DM_WIDTH           =              8;
parameter MEM_IF_DQ_PER_DQS         =              8;
parameter MEM_IF_DQS_CAPTURE_EN     =              0;
parameter MEM_IF_DQS_WIDTH          =              8;
parameter MEM_IF_ROWADDR_WIDTH      =             13;
parameter DLL_DELAY_BUFFER_MODE     =         "HIGH";
parameter DQS_OUT_MODE              = "DELAY_CHAIN2";
parameter DQS_PHASE                 =             72;


input  wire                                             reset_resync_clk_2x_n;
input  wire                                             resync_clk_2x;
input  wire                                             mem_clk_2x;
input  wire                                             write_clk_2x;
output wire [MEM_IF_DM_WIDTH - 1 : 0]                   mem_dm;
inout  wire [MEM_IF_DWIDTH - 1 : 0]                     mem_dq;
inout  wire [MEM_IF_DWIDTH / MEM_IF_DQ_PER_DQS - 1 : 0] mem_dqs;
(* preserve *) output reg [MEM_IF_DWIDTH - 1 : 0]       dio_rdata_h_2x;
(* preserve *) output reg [MEM_IF_DWIDTH - 1 : 0]       dio_rdata_l_2x;
input  wire [MEM_IF_DM_WIDTH -1 : 0]                    wdp_dm_h_2x;
input  wire [MEM_IF_DM_WIDTH -1 : 0]                    wdp_dm_l_2x;
input  wire [MEM_IF_DWIDTH - 1 : 0]                     wdp_wdata_h_2x;
input  wire [MEM_IF_DWIDTH - 1 : 0]                     wdp_wdata_l_2x;
input  wire [MEM_IF_DWIDTH - 1 : 0]                     wdp_wdata_oe_2x;
input  wire [(MEM_IF_DQS_WIDTH) - 1 : 0]                wdp_wdqs_2x;
input  wire [(MEM_IF_DQS_WIDTH) - 1 : 0]                wdp_wdqs_oe_2x;

(* altera_attribute=" -name FAST_OUTPUT_ENABLE_REGISTER ON" *) reg [MEM_IF_DWIDTH - 1 : 0] wdp_wdata_oe_2x_r;

wire [MEM_IF_DWIDTH - 1 : 0]                            rdata_n_captured;
wire [MEM_IF_DWIDTH - 1 : 0]                            rdata_p_captured;
                                                       
(* preserve *) wire [(MEM_IF_DQS_WIDTH) - 1 : 0]        wdp_wdqs_oe_2x_r;
 
(* preserve *) reg  [MEM_IF_DWIDTH - 1 : 0]             rdata_n_ams;
(* preserve *) reg  [MEM_IF_DWIDTH - 1 : 0]             rdata_p_ams;

// Use DQS clock to register DQ read data
wire [(MEM_IF_DQS_WIDTH) - 1 : 0]                       dqs_clk;
wire [(MEM_IF_DQS_WIDTH) - 1 : 0]                       dq_capture_clk;

wire                                                    reset_resync_clk_2x;


wire [MEM_IF_DWIDTH - 1 : 0]                            dq_ddio_dataout;
wire [MEM_IF_DWIDTH - 1 : 0]                            dq_datain;
wire [MEM_IF_DWIDTH - 1 : 0]                            dm_ddio_dataout;
wire [MEM_IF_DWIDTH / MEM_IF_DQ_PER_DQS - 1 : 0]        dqs_ddio_dataout;


genvar i,j;


// Create non-inverted reset for pads :
assign reset_resync_clk_2x = ~reset_resync_clk_2x_n;

// Read datapath functionality :


// synchronise to the resync_clk_2x_domain

always @(posedge resync_clk_2x)
begin

    // Resynchronise :
            
    // The comparison to 'X' is performed to prevent X's being captured in non-DQS mode
    // and propagating into the sequencer's control logic.  This can only occur when a Verilog SIMGEN
    // model of the sequencer is being used :
    if (|rdata_p_captured === 1'bX) 
        rdata_p_ams <= {MEM_IF_DWIDTH{1'b0}};  
    else
        rdata_p_ams <= rdata_p_captured; // 'captured' is from the IOEs
        
     if (|rdata_n_captured === 1'bX) 
        rdata_n_ams <= {MEM_IF_DWIDTH{1'b0}};  
    else
        rdata_n_ams <= rdata_n_captured; // 'captured' is from the IOEs
    
    // Output registers :
    dio_rdata_h_2x <= rdata_p_ams;
    dio_rdata_l_2x <= rdata_n_ams;

end


// Either use the DQS clock to capture, or the resync clock:

generate

    if (MEM_IF_DQS_CAPTURE_EN == 1)
        assign dq_capture_clk = ~dqs_clk;
    else
        assign dq_capture_clk = {MEM_IF_DQS_WIDTH{resync_clk_2x}};

endgenerate


// DQ pins and their logic

generate

// First generate each DQS group :
for (i=0; i<MEM_IF_DQS_WIDTH ; i=i+1)
begin : dqs_group

    // Then generate DQ pins for each group :
    for (j=0; j<MEM_IF_DQ_PER_DQS ; j=j+1)
    begin : dq
            
        // DDIO out :
        cycloneiii_ddio_out # (
            .power_up("low"),
            .async_mode("none"),
            .sync_mode("none"),
            .lpm_type("cycloneiii_ddio_out"),
            .use_new_clocking_model("true")            
        ) dq_ddio_out ( 
            .datainlo(wdp_wdata_l_2x[j+(i*MEM_IF_DQ_PER_DQS)]),
            .datainhi(wdp_wdata_h_2x[j+(i*MEM_IF_DQ_PER_DQS)]),
            .clkhi(write_clk_2x),
            .clklo(write_clk_2x),
            .clk(),
            .muxsel(write_clk_2x),
            .ena(1'b1),
            .areset(),
            .sreset(),
            .dataout(dq_ddio_dataout[j+(i*MEM_IF_DQ_PER_DQS)]),
            .dfflo(),
            .dffhi(),
            .devpor(),
            .devclrn()
        );
     
        // Need to register OE :
        always @(posedge write_clk_2x)
        begin        
            wdp_wdata_oe_2x_r[j+(i*MEM_IF_DQ_PER_DQS)] <= wdp_wdata_oe_2x[j+(i*MEM_IF_DQ_PER_DQS)]; 
        end       
        
        //The buffer itself (output side) :
        cycloneiii_io_obuf dq_obuf (
            .i(dq_ddio_dataout[j+(i*MEM_IF_DQ_PER_DQS)]),
            .oe(wdp_wdata_oe_2x_r[j+(i*MEM_IF_DQ_PER_DQS)]),  
            .seriesterminationcontrol(),
            .devoe(),
            .o(mem_dq[j+(i*MEM_IF_DQ_PER_DQS)]), //pad
            .obar()
        );
            
        //The buffer itself (input side) :
                
        cycloneiii_io_ibuf # (
            .simulate_z_as("gnd")
        ) dq_ibuf (
            .i(mem_dq[j+(i*MEM_IF_DQ_PER_DQS)]),//pad
            .ibar(),
            .o(dq_datain[j+(i*MEM_IF_DQ_PER_DQS)])
        );

        // Read data input DDIO path :
        altddio_in #( .intended_device_family ("Cyclone III"),
            .lpm_hint      ("UNUSED"),
            .lpm_type      ("altddio_in"),
            .power_up_high ("OFF"),
            .width         (1)
        ) dqi (            
            .aclr          (reset_resync_clk_2x),
            .aset          (),
            .sset          (),
            .sclr          (),
            .datain        (dq_datain[j+(i*MEM_IF_DQ_PER_DQS)]),
            .dataout_h     (rdata_n_captured[j+(i*MEM_IF_DQ_PER_DQS)]),
            .dataout_l     (rdata_p_captured[j+(i*MEM_IF_DQ_PER_DQS)]),
            .inclock       (dq_capture_clk[i]),
            .inclocken     (1'b1)
        );
        
    end

end

endgenerate



//////////////////////////////////////////////////////////////////////////////-
// DM Pin and its logic
//////////////////////////////////////////////////////////////////////////////-


generate

if (MEM_IF_DM_PINS_EN)
begin

    for (i=0; i<MEM_IF_DM_WIDTH; i=i+1)
    begin : dm
      
    	cycloneiii_ddio_out # (
    	    .power_up("low"),
    	    .async_mode("none"),
    	    .sync_mode("none"),
    	    .lpm_type("cycloneiii_ddio_out"),
    	    .use_new_clocking_model("true")	     
    	) dm_ddio_out ( 
    	    .datainlo(wdp_dm_l_2x[i]),
    	    .datainhi(wdp_dm_h_2x[i]),
    	    .clkhi(write_clk_2x),
    	    .clklo(write_clk_2x),
    	    .clk(),
    	    .muxsel(write_clk_2x),
    	    .ena(1'b1),
    	    .areset(),
    	    .sreset(),
    	    .dataout(dm_ddio_dataout[i]),
    	    .dfflo(),
    	    .dffhi(),
    	    .devpor(),
    	    .devclrn()
    	);
    		
    	cycloneiii_io_obuf dm_obuf (
    	    .i(dm_ddio_dataout[i]),
    	    .oe(1'b1),  
    	    .seriesterminationcontrol(),
    	    .devoe(),
    	    .o(mem_dm[i]), //pad
    	    .obar()
    	);
    	     
    end

end

endgenerate



// Note that for CIII DQS-capture mode is unsupported, and so DQS is only used
// as an output :
generate

for (i=0; i<(MEM_IF_DQS_WIDTH); i=i+1)
begin : dqs

    cycloneiii_ddio_out # (
         .power_up("low"),
         .async_mode("none"),
         .sync_mode("none"),
         .lpm_type("cycloneiii_ddio_out"),
         .use_new_clocking_model("true")            
     ) dqs_ddio_out ( 
         .datainlo(1'b0),
         .datainhi(wdp_wdqs_2x[i]),
         .clkhi(mem_clk_2x),
         .clklo(mem_clk_2x),
         .clk(),
         .muxsel(mem_clk_2x),
         .ena(1'b1),
         .areset(),
         .sreset(),
         .dataout(dqs_ddio_dataout[i]),
         .dfflo(),
         .dffhi(),
         .devpor(),
         .devclrn()
     );
              
    // NB. Invert the OE input, as Quartus requires us to invert
    // the OE input on the OBUF :
    cycloneiii_ddio_oe # (
         .power_up("low"),
         .async_mode("none"),
         .sync_mode("none"),
         .lpm_type("cycloneiii_ddio_oe")
     ) dqsoe_ddio_oe ( 
         .oe(~wdp_wdqs_oe_2x[i]),
         .clk(mem_clk_2x),
         .ena(1'b1),
         .areset(),
         .sreset(),
         .dataout(wdp_wdqs_oe_2x_r[i]),
         .dfflo(),
         .dffhi(),
         .devpor(),
         .devclrn()
     );    
          
     // Output buffer itself :
     // OE input is active HIGH
     cycloneiii_io_obuf dqs_obuf (
         .i(dqs_ddio_dataout[i]),
         .oe(~wdp_wdqs_oe_2x_r[i]),  
         .seriesterminationcontrol(),
         .devoe(),
         .o(mem_dqs[i]), 
         .obar()
     );         

end

endgenerate


endmodule

//

`ifdef ALT_MEM_PHY_DEFINES
`else
`include "alt_mem_phy_defines.v"
`endif

//
module ddr2_64bit_phy_alt_mem_phy_read_dp ( phy_clk_1x,
                             resync_clk_2x,
                             reset_phy_clk_1x_n,
                             reset_resync_clk_2x_n,
                             seq_rdp_dec_read_lat_1x,
                             seq_rdp_dmx_swap,
                             seq_rdp_inc_read_lat_1x,
                             dio_rdata_h_2x,
                             dio_rdata_l_2x,
                             ctl_mem_rdata
                            );

parameter ADDR_COUNT_WIDTH         =               4;
parameter BIDIR_DPINS              =               1; // 0 for QDR only.
parameter DWIDTH_RATIO             =               4;
parameter MEM_IF_CLK_PS            =            4000;
parameter FAMILY                   =    "Stratix II";
parameter LOCAL_IF_DWIDTH          =             256;
parameter MEM_IF_DQ_PER_DQS        =               8;
parameter MEM_IF_DQS_WIDTH         =               8;
parameter MEM_IF_DWIDTH            =              64;
parameter MEM_IF_PHY_NAME          = "STRATIXII_DQS";
parameter RDP_INITIAL_LAT          =               6;
parameter RDP_RESYNC_LAT_CTL_EN    =               0;
parameter RESYNC_PIPELINE_DEPTH    =               1;

localparam NUM_DQS_PINS            = MEM_IF_DQS_WIDTH;

input  wire                         phy_clk_1x;
input  wire                         resync_clk_2x;
input  wire                         reset_phy_clk_1x_n;
input  wire                         reset_resync_clk_2x_n;
input  wire                         seq_rdp_dec_read_lat_1x;
input  wire                         seq_rdp_dmx_swap;
input  wire                         seq_rdp_inc_read_lat_1x;
input  wire [MEM_IF_DWIDTH-1 : 0]   dio_rdata_h_2x;
input  wire [MEM_IF_DWIDTH-1 : 0]   dio_rdata_l_2x;
output wire [LOCAL_IF_DWIDTH-1 : 0] ctl_mem_rdata;

// concatonated read data :
wire [(2*MEM_IF_DWIDTH)-1 : 0]      dio_rdata_2x;
reg  [ADDR_COUNT_WIDTH - DWIDTH_RATIO/2 : 0]     rd_ram_rd_addr;   
reg  [ADDR_COUNT_WIDTH - 1 : 0]     rd_ram_wr_addr;
wire [(2*MEM_IF_DWIDTH)-1 : 0]      rd_data_piped_2x;



reg                                 inc_read_lat_sync_r;
reg                                 dec_read_lat_sync_r;

// Optional AMS registers :
reg                                 inc_read_lat_ams;  
reg                                 inc_read_lat_sync;  
                                                         
reg                                 dec_read_lat_ams;  
reg                                 dec_read_lat_sync;  
                                                         
reg                                 state;
reg                                 dmx_swap_ams;
reg                                 dmx_swap_sync;
reg                                 dmx_swap_sync_r;

wire                                wr_addr_toggle_detect;
wire                                wr_addr_stall;
wire                                wr_addr_double_inc;

wire                                rd_addr_stall;
wire                                rd_addr_double_inc;

// Read data from ram, prior to mapping/re-ordering :
wire [LOCAL_IF_DWIDTH-1 : 0]        ram_rdata_1x;

////////////////////////////////////////////////////////////////////////////////
//                          Write Address block
////////////////////////////////////////////////////////////////////////////////


// 'toggle detect' logic :
assign wr_addr_toggle_detect = !dmx_swap_sync_r && dmx_swap_sync;

// 'stall' logic :
assign wr_addr_stall      = !(~state && wr_addr_toggle_detect);

// 'double_inc' logic :
assign wr_addr_double_inc = state && wr_addr_toggle_detect;

// Write address generation
// When no demux toggle - increment every clock cycle
// When demux toggle is detected,invert addr_stall
// if addr_stall is 1 then do nothing to address, else double increment this cycle.
always@ (posedge resync_clk_2x or negedge reset_resync_clk_2x_n)
begin

   if (reset_resync_clk_2x_n == 0)
   begin
       rd_ram_wr_addr  <= RDP_INITIAL_LAT[3:0];
       state           <= 1'b0;
       dmx_swap_ams    <= 1'b0;
       dmx_swap_sync   <= 1'b0;
       dmx_swap_sync_r <= 1'b0;
   end

   else
   begin

       // Synchronise dmx_swap :
       dmx_swap_ams    <= seq_rdp_dmx_swap;
       dmx_swap_sync   <= dmx_swap_ams;
       dmx_swap_sync_r <= dmx_swap_sync;

       // RAM write address :
       if (wr_addr_stall == 1'b1)
       begin
           rd_ram_wr_addr <= rd_ram_wr_addr + 1'b1 + wr_addr_double_inc;
       end

       // Maintain single bit state :
       if (wr_addr_toggle_detect == 1'b1)
       begin
           state <= ~state;
       end

   end

end



////////////////////////////////////////////////////////////////////////////////
//                          Pipeline registers
////////////////////////////////////////////////////////////////////////////////


// Concatenate the input read data taking note of ram input datawidths
// This is concatenating rdata_p and rdata_n from a DQS group together so that the rest
// of the pipeline can use a single vector:
generate

genvar dqs_group_num;

    for (dqs_group_num = 0; dqs_group_num < NUM_DQS_PINS ; dqs_group_num  = dqs_group_num + 1)
    begin : ddio_remap

        assign dio_rdata_2x[2*MEM_IF_DQ_PER_DQS*dqs_group_num + 2*MEM_IF_DQ_PER_DQS-1: 2*MEM_IF_DQ_PER_DQS*dqs_group_num]

         = {  dio_rdata_l_2x[MEM_IF_DQ_PER_DQS*dqs_group_num + MEM_IF_DQ_PER_DQS-1: MEM_IF_DQ_PER_DQS*dqs_group_num],
              dio_rdata_h_2x[MEM_IF_DQ_PER_DQS*dqs_group_num + MEM_IF_DQ_PER_DQS-1: MEM_IF_DQ_PER_DQS*dqs_group_num]
            };
    end

endgenerate


// Generate appropriate pipeline depth

generate
genvar i;

    if (RESYNC_PIPELINE_DEPTH > 0) 
    begin : resync_pipeline_gen

    // Declare pipeline registers
    reg  [(2*MEM_IF_DWIDTH)-1 : 0 ] pipeline_delay [0 : RESYNC_PIPELINE_DEPTH - 1] ;

        for (i=0; i< RESYNC_PIPELINE_DEPTH; i = i + 1)
        begin : PIPELINE

            always @(posedge resync_clk_2x)
            begin                

                if (i==0)
                    pipeline_delay[i] <= dio_rdata_2x;
                else
                    pipeline_delay[i] <= pipeline_delay[i-1];               
               
            end //always

        end //for

        assign rd_data_piped_2x = pipeline_delay[RESYNC_PIPELINE_DEPTH-1];

    end

    // If pipeline registers are not configured, pass-thru :
    else
    begin : no_resync_pipe_gen

        assign rd_data_piped_2x = dio_rdata_2x;

    end

endgenerate





////////////////////////////////////////////////////////////////////////////////
//         Instantiate the read_dp dpram
////////////////////////////////////////////////////////////////////////////////

generate

    if (DWIDTH_RATIO == 4)
    begin : half_rate_ram_gen
    
        altsyncram #(
             .address_reg_b             ("CLOCK1"),
             .clock_enable_input_a      ("BYPASS"),
             .clock_enable_input_b      ("BYPASS"),
             .clock_enable_output_b     ("BYPASS"),
             .intended_device_family    (FAMILY),
             .lpm_type                  ("altsyncram"),
             .numwords_a                ((2**ADDR_COUNT_WIDTH )),
             .numwords_b                ((2**ADDR_COUNT_WIDTH )/2),
             .operation_mode            ("DUAL_PORT"),
             .outdata_aclr_b            ("NONE"),
             .outdata_reg_b             ("CLOCK1"),
             .power_up_uninitialized    ("FALSE"),
             .widthad_a                 (ADDR_COUNT_WIDTH),
             .widthad_b                 (ADDR_COUNT_WIDTH - 1),
             .width_a                   (MEM_IF_DWIDTH*2),
             .width_b                   (MEM_IF_DWIDTH*4),
             .width_byteena_a           (1)
        ) altsyncram_component (
             .wren_a            (1'b1),
             .clock0            (resync_clk_2x),
             .clock1            (phy_clk_1x),
             .address_a         (rd_ram_wr_addr),
             .address_b         (rd_ram_rd_addr),
             .data_a            (rd_data_piped_2x),
             .q_b               (ram_rdata_1x),
             .aclr0             (1'b0),
             .aclr1             (1'b0),
             .addressstall_a    (1'b0),
             .addressstall_b    (1'b0),
             .byteena_a         (1'b1),
             .byteena_b         (1'b1),
             .clocken0          (1'b1),
             .clocken1          (1'b1),
             .clocken2          (),
             .clocken3          (),
             .data_b            ({(MEM_IF_DWIDTH*4){1'b1}}),
             .q_a               (),
             .rden_b            (1'b1),
             .rden_a            (),
             .wren_b            (1'b0),
             .eccstatus         ()
        );

        // Read data mapping :
        genvar dqs_group_num_b;

        for (dqs_group_num_b = 0; dqs_group_num_b < NUM_DQS_PINS ; dqs_group_num_b  = dqs_group_num_b + 1)
        begin : remap_logic
            assign ctl_mem_rdata [MEM_IF_DWIDTH * 0 + dqs_group_num_b * MEM_IF_DQ_PER_DQS + MEM_IF_DQ_PER_DQS -1 : MEM_IF_DWIDTH * 0 + dqs_group_num_b * MEM_IF_DQ_PER_DQS ] = ram_rdata_1x [                     dqs_group_num_b * 2 * MEM_IF_DQ_PER_DQS +     MEM_IF_DQ_PER_DQS - 1 :                     dqs_group_num_b * 2 * MEM_IF_DQ_PER_DQS                     ];
            assign ctl_mem_rdata [MEM_IF_DWIDTH * 1 + dqs_group_num_b * MEM_IF_DQ_PER_DQS + MEM_IF_DQ_PER_DQS -1 : MEM_IF_DWIDTH * 1 + dqs_group_num_b * MEM_IF_DQ_PER_DQS ] = ram_rdata_1x [                     dqs_group_num_b * 2 * MEM_IF_DQ_PER_DQS + 2 * MEM_IF_DQ_PER_DQS - 1 :                     dqs_group_num_b * 2 * MEM_IF_DQ_PER_DQS + MEM_IF_DQ_PER_DQS ];
            assign ctl_mem_rdata [MEM_IF_DWIDTH * 2 + dqs_group_num_b * MEM_IF_DQ_PER_DQS + MEM_IF_DQ_PER_DQS -1 : MEM_IF_DWIDTH * 2 + dqs_group_num_b * MEM_IF_DQ_PER_DQS ] = ram_rdata_1x [ MEM_IF_DWIDTH * 2 + dqs_group_num_b * 2 * MEM_IF_DQ_PER_DQS +     MEM_IF_DQ_PER_DQS - 1 : MEM_IF_DWIDTH * 2 + dqs_group_num_b * 2 * MEM_IF_DQ_PER_DQS                     ];
            assign ctl_mem_rdata [MEM_IF_DWIDTH * 3 + dqs_group_num_b * MEM_IF_DQ_PER_DQS + MEM_IF_DQ_PER_DQS -1 : MEM_IF_DWIDTH * 3 + dqs_group_num_b * MEM_IF_DQ_PER_DQS ] = ram_rdata_1x [ MEM_IF_DWIDTH * 2 + dqs_group_num_b * 2 * MEM_IF_DQ_PER_DQS + 2 * MEM_IF_DQ_PER_DQS - 1 : MEM_IF_DWIDTH * 2 + dqs_group_num_b * 2 * MEM_IF_DQ_PER_DQS + MEM_IF_DQ_PER_DQS ];
        end

     end // block: half_rate_dp
   
endgenerate

// full-rate
   
generate

    if (DWIDTH_RATIO == 2)
    begin : full_rate_ram_gen
    
        altsyncram #(
             .address_reg_b             ("CLOCK1"),
             .clock_enable_input_a      ("BYPASS"),
             .clock_enable_input_b      ("BYPASS"),
             .clock_enable_output_b     ("BYPASS"),
             .intended_device_family    (FAMILY),
             .lpm_type                  ("altsyncram"),
             .numwords_a                ((2**ADDR_COUNT_WIDTH )),
             .numwords_b                ((2**ADDR_COUNT_WIDTH )),
             .operation_mode            ("DUAL_PORT"),
             .outdata_aclr_b            ("NONE"),
             .outdata_reg_b             ("CLOCK1"),
             .power_up_uninitialized    ("FALSE"),
             .widthad_a                 (ADDR_COUNT_WIDTH),
             .widthad_b                 (ADDR_COUNT_WIDTH),
             .width_a                   (MEM_IF_DWIDTH*2),
             .width_b                   (MEM_IF_DWIDTH*2),
             .width_byteena_a           (1)
        ) altsyncram_component(
             .wren_a            (1'b1),
             .clock0            (resync_clk_2x),
             .clock1            (phy_clk_1x),
             .address_a         (rd_ram_wr_addr),
             .address_b         (rd_ram_rd_addr),
             .data_a            (rd_data_piped_2x),
             .q_b               (ram_rdata_1x),
             .aclr0             (1'b0),
             .aclr1             (1'b0),
             .addressstall_a    (1'b0),
             .addressstall_b    (1'b0),
             .byteena_a         (1'b1),
             .byteena_b         (1'b1),
             .clocken0          (1'b1),
             .clocken1          (1'b1),
             .clocken2          (),
             .clocken3          (),
             .data_b            ({(MEM_IF_DWIDTH*2){1'b1}}),
             .q_a               (),
             .rden_b            (1'b1),
             .rden_a            (),
             .wren_b            (1'b0),
             .eccstatus         ()             
        );

        // Read data mapping :
        genvar dqs_group_num_b;

        for (dqs_group_num_b = 0; dqs_group_num_b < NUM_DQS_PINS ; dqs_group_num_b  = dqs_group_num_b + 1)
        begin : remap_logic
            assign ctl_mem_rdata [MEM_IF_DWIDTH * 0 + dqs_group_num_b * MEM_IF_DQ_PER_DQS + MEM_IF_DQ_PER_DQS -1 : MEM_IF_DWIDTH * 0 + dqs_group_num_b * MEM_IF_DQ_PER_DQS ] = ram_rdata_1x [                     dqs_group_num_b * 2 * MEM_IF_DQ_PER_DQS +     MEM_IF_DQ_PER_DQS - 1 :                     dqs_group_num_b * 2 * MEM_IF_DQ_PER_DQS                     ];
            assign ctl_mem_rdata [MEM_IF_DWIDTH * 1 + dqs_group_num_b * MEM_IF_DQ_PER_DQS + MEM_IF_DQ_PER_DQS -1 : MEM_IF_DWIDTH * 1 + dqs_group_num_b * MEM_IF_DQ_PER_DQS ] = ram_rdata_1x [                     dqs_group_num_b * 2 * MEM_IF_DQ_PER_DQS + 2 * MEM_IF_DQ_PER_DQS - 1 :                     dqs_group_num_b * 2 * MEM_IF_DQ_PER_DQS + MEM_IF_DQ_PER_DQS ];

        end
              
     end // block: half_rate_dp
   
endgenerate

   

////////////////////////////////////////////////////////////////////////////////
//                          Read Address block
////////////////////////////////////////////////////////////////////////////////


// Optional Anti-metastability flops :
generate

    if (RDP_RESYNC_LAT_CTL_EN == 1)

    always@ (posedge phy_clk_1x or negedge reset_phy_clk_1x_n)
    begin : rd_addr_ams

        if (reset_phy_clk_1x_n == 1'b0)
        begin

            inc_read_lat_ams    <= 1'b0;
            inc_read_lat_sync   <= 1'b0;
            inc_read_lat_sync_r <= 1'b0;

            // Synchronise rd_lat_inc_1x :
            dec_read_lat_ams    <= 1'b0;
            dec_read_lat_sync   <= 1'b0;
            dec_read_lat_sync_r <= 1'b0;

        end

        else
        begin

            // Synchronise rd_lat_inc_1x :
            inc_read_lat_ams    <= seq_rdp_inc_read_lat_1x;
            inc_read_lat_sync   <= inc_read_lat_ams;
            inc_read_lat_sync_r <= inc_read_lat_sync;

            // Synchronise rd_lat_inc_1x :
            dec_read_lat_ams    <= seq_rdp_dec_read_lat_1x;
            dec_read_lat_sync   <= dec_read_lat_ams;
            dec_read_lat_sync_r <= dec_read_lat_sync;

        end

    end // always

    // No anti-metastability protection required :
    else

    always@ (posedge phy_clk_1x or negedge reset_phy_clk_1x_n)
    begin

        if (reset_phy_clk_1x_n == 1'b0)
        begin
            inc_read_lat_sync_r    <= 1'b0;
            dec_read_lat_sync_r   <= 1'b0;
        end

        else
        begin
            // No need to re-synchronise, just register for edge detect :
            inc_read_lat_sync_r <= seq_rdp_inc_read_lat_1x;
            dec_read_lat_sync_r <= seq_rdp_dec_read_lat_1x;
        end

    end

endgenerate




generate

    if (RDP_RESYNC_LAT_CTL_EN == 1)
    begin : lat_ctl_en_gen

        // 'toggle detect' logic :
        //assign rd_addr_double_inc =    !inc_read_lat_sync_r && inc_read_lat_sync;
        assign rd_addr_double_inc =    ( !dec_read_lat_sync_r && dec_read_lat_sync );
        // 'stall' logic :
       // assign rd_addr_stall      = !( !dec_read_lat_sync_r && dec_read_lat_sync );
        assign rd_addr_stall      =  !inc_read_lat_sync_r && inc_read_lat_sync;
    end

    else
    begin : no_lat_ctl_en_gen
        // 'toggle detect' logic :
        //assign rd_addr_double_inc =    !inc_read_lat_sync_r && seq_rdp_inc_read_lat_1x;
        assign rd_addr_double_inc =   ( !dec_read_lat_sync_r && seq_rdp_dec_read_lat_1x );
        // 'stall' logic :
        //assign rd_addr_stall      = !( !dec_read_lat_sync_r && seq_rdp_dec_read_lat_1x );
        assign rd_addr_stall      = !inc_read_lat_sync_r && seq_rdp_inc_read_lat_1x;
    end

endgenerate



always@ (posedge phy_clk_1x or negedge reset_phy_clk_1x_n)
begin

   if (reset_phy_clk_1x_n == 0)
   begin
       rd_ram_rd_addr  <= { ADDR_COUNT_WIDTH - DWIDTH_RATIO/2 {1'b0} };      
   end

   else
   begin

       // RAM read address :
       if (rd_addr_stall == 1'b0)
       begin
           rd_ram_rd_addr <= rd_ram_rd_addr + 1'b1 + rd_addr_double_inc;
       end

   end

end





endmodule

//

// Note, this write datapath logic matches both the spec, and what was done in
// the beta project.

`ifdef ALT_MEM_PHY_DEFINES
`else
`include "alt_mem_phy_defines.v"
`endif

//
module ddr2_64bit_phy_alt_mem_phy_write_dp_fr(
                                // clocks
                                phy_clk_1x,            // full-rate clock
                                mem_clk_2x,            // full-rate clock
                                write_clk_2x,          // full-rate clock

                                // active-low resets, sync'd to clock domain
                                reset_phy_clk_1x_n,
                                reset_mem_clk_2x_n,
                                reset_write_clk_2x_n,

                                // control i/f inputs
                                ctl_mem_be,
                                ctl_mem_dqs_burst,
                                ctl_mem_wdata,
                                ctl_mem_wdata_valid,

                                // seq i/f inputs :
                                seq_be,
                                seq_dqs_burst,
                                seq_wdata,
                                seq_wdata_valid,

                                seq_ctl_sel,

                                // outputs to IOEs
                                wdp_wdata_h_2x,
                                wdp_wdata_l_2x,
                                wdp_wdata_oe_2x,
                                wdp_wdqs_2x,
                                wdp_wdqs_oe_2x,
                                wdp_dm_h_2x,
                                wdp_dm_l_2x
                                );

// parameter declarations
parameter BIDIR_DPINS        = 1;
parameter LOCAL_IF_DRATE     = "FULL";
parameter LOCAL_IF_DWIDTH    = 256;
parameter MEM_IF_DM_WIDTH    = 8;
parameter MEM_IF_DQ_PER_DQS  = 8;
parameter MEM_IF_DQS_WIDTH   = 8;
parameter GENERATE_WRITE_DQS = 1;
parameter MEM_IF_DWIDTH      = 64;
parameter DWIDTH_RATIO       = 2;
parameter MEM_IF_DM_PINS_EN  = 1;


// "internal" parameter, not to get propagated from higher levels...
parameter NUM_DUPLICATE_REGS = 4;             // 1 per nibble to save registers


// clocks
input wire                                                   phy_clk_1x;          // half-rate system clock
input wire                                                   mem_clk_2x;          // full-rate memory clock
input wire                                                   write_clk_2x;        // full-rate write clock

// resets, async assert, de-assert is sync'd to each clock domain
input wire                                                   reset_phy_clk_1x_n;
input wire                                                   reset_mem_clk_2x_n;
input wire                                                   reset_write_clk_2x_n;

// control i/f inputs
input wire [MEM_IF_DM_WIDTH  * DWIDTH_RATIO - 1 : 0]         ctl_mem_be;           // byte enable == ~data mask
input wire [MEM_IF_DQS_WIDTH * DWIDTH_RATIO/2 - 1 : 0]       ctl_mem_dqs_burst;    // dqs burst indication
input wire [MEM_IF_DWIDTH*DWIDTH_RATIO - 1 : 0]              ctl_mem_wdata;        // write data
input wire [MEM_IF_DQS_WIDTH * DWIDTH_RATIO/2 - 1 : 0]       ctl_mem_wdata_valid;  // write data valid indication

// seq i/f inputs
input wire [MEM_IF_DM_WIDTH  * DWIDTH_RATIO   - 1 : 0]       seq_be;
input wire [MEM_IF_DWIDTH    * DWIDTH_RATIO   - 1 : 0]       seq_wdata;
input wire [MEM_IF_DQS_WIDTH * DWIDTH_RATIO/2 - 1 : 0]       seq_dqs_burst;
input wire [MEM_IF_DQS_WIDTH * DWIDTH_RATIO/2 - 1 : 0]       seq_wdata_valid;

input wire                                                   seq_ctl_sel;

(*preserve*) output reg  [MEM_IF_DWIDTH - 1 : 0]             wdp_wdata_h_2x;      // wdata_h to IOE
(*preserve*) output reg  [MEM_IF_DWIDTH - 1 : 0]             wdp_wdata_l_2x;      // wdata_l to IOE
output wire [MEM_IF_DWIDTH - 1 : 0]                          wdp_wdata_oe_2x;     // OE to DQ pin
output reg  [(MEM_IF_DQS_WIDTH) - 1 : 0]                     wdp_wdqs_2x;         // DQS to IOE
output reg  [(MEM_IF_DQS_WIDTH) - 1 : 0]                     wdp_wdqs_oe_2x;      // OE to DQS pin

(*preserve*) output reg [MEM_IF_DM_WIDTH -1 : 0]                 wdp_dm_h_2x;         // dm_h to IOE
(*preserve*) output reg [MEM_IF_DM_WIDTH -1 : 0]                 wdp_dm_l_2x;         // dm_l to IOE

// internal reg declarations

// registers (on a per- DQS group basis) which sync the dqs_burst_1x to phy_clk or mem_clk_2x.
// They are used to generate the DQS signal and its OE.
(*preserve*) reg [MEM_IF_DQS_WIDTH -1 : 0]                   dqs_burst_1x_r;
(*preserve*) reg [MEM_IF_DQS_WIDTH -1 : 0]                   dqs_burst_2x_r1;
(*preserve*) reg [MEM_IF_DQS_WIDTH -1 : 0]                   dqs_burst_2x_r2;
(*preserve*) reg [MEM_IF_DQS_WIDTH -1 : 0]                   dqs_burst_2x_r3;

// registers to generate the dq_oe, on a per nibble basis, to save registers.
(*preserve*) reg [MEM_IF_DWIDTH/NUM_DUPLICATE_REGS -1 : 0 ] wdata_valid_1x_r1;   // wdata_valid_1x, retimed in phy_clk_1x
(*preserve*) reg [MEM_IF_DWIDTH/NUM_DUPLICATE_REGS -1 : 0 ] wdata_valid_1x_r2;   // wdata_valid_1x_r1, retimed in phy_clk_1x

(* preserve, altera_attribute = "-name ADV_NETLIST_OPT_ALLOWED \"NEVER ALLOW\"" *) reg [MEM_IF_DWIDTH/NUM_DUPLICATE_REGS -1 : 0 ] dq_oe_2x;            // 1 per nibble, to save registers

             reg [MEM_IF_DWIDTH - 1 : 0]                    wdp_wdata_oe_2x_int; // intermediate output, gets assigned to o/p signal

             reg [LOCAL_IF_DWIDTH -1 : 0]                   mem_wdata_r1;     // mem_wdata, retimed in phy_clk_1x
             reg [LOCAL_IF_DWIDTH -1 : 0]                   mem_wdata_r2;     // mem_wdata_r1, retimed in phy_clk_1x

// registers used to generate the mux select signal for wdata.
(*preserve*) reg [MEM_IF_DWIDTH/NUM_DUPLICATE_REGS -1 : 0 ] wdata_valid_1x_r;    // 1 per nibble, to save registers
(*preserve*) reg [MEM_IF_DWIDTH/NUM_DUPLICATE_REGS -1 : 0 ] wdata_valid_2x_r1;   // 1 per nibble, to save registers
(*preserve*) reg [MEM_IF_DWIDTH/NUM_DUPLICATE_REGS -1 : 0 ] wdata_valid_2x_r2;   // 1 per nibble, to save registers
(*preserve*) reg [MEM_IF_DWIDTH/NUM_DUPLICATE_REGS -1 : 0 ] wdata_sel;           // 1 per nibble, to save registers

// registers used to generate the dm mux select and dm signals
             reg [MEM_IF_DM_WIDTH*DWIDTH_RATIO - 1 : 0]     mem_dm_r1;
             reg [MEM_IF_DM_WIDTH*DWIDTH_RATIO - 1 : 0]     mem_dm_r2;
(*preserve*) reg                                            wdata_dm_1x_r;       // preserved, to stop merge with wdata_valid_1x_r
(*preserve*) reg                                            wdata_dm_2x_r1;      // preserved, to stop merge with wdata_valid_2x_r1
(*preserve*) reg                                            wdata_dm_2x_r2;      // preserved, to stop merge with wdata_valid_2x_r2
(*preserve*) reg                                            dm_sel;              // preserved, to stop merge with wdata_sel

// MUX outputs....
reg [MEM_IF_DQS_WIDTH * DWIDTH_RATIO/2 - 1 : 0] mem_dqs_burst  ;
reg [MEM_IF_DQS_WIDTH * DWIDTH_RATIO/2 - 1 : 0] mem_wdata_valid;
reg [MEM_IF_DM_WIDTH  * DWIDTH_RATIO   - 1 : 0] mem_be         ;
reg [MEM_IF_DWIDTH    * DWIDTH_RATIO   - 1 : 0] mem_wdata      ;

wire [MEM_IF_DQS_WIDTH - 1 : 0]                             wdp_wdqs_oe_2x_int;


always @*
begin

    // Select controller or sequencer according to the select signal :
    if (seq_ctl_sel)
    begin
        mem_be            = seq_be;
        mem_wdata         = seq_wdata;
        mem_wdata_valid   = seq_wdata_valid;
        mem_dqs_burst     = seq_dqs_burst;
    end


    else
    begin
        mem_be            = ctl_mem_be;
        mem_wdata         = ctl_mem_wdata;
        mem_wdata_valid   = ctl_mem_wdata_valid;
        mem_dqs_burst     = ctl_mem_dqs_burst;
    end

end

genvar  a, b, c, d; // variable for generate statement
integer j;          // variable for loop counters

/////////////////////////////////////////////////////////////////////////
// generate the following write DQS logic on a per DQS group basis.
// wdp_wdqs_2x and wdp_wdqs_oe_2x get output to the IOEs.
////////////////////////////////////////////////////////////////////////

generate
if (GENERATE_WRITE_DQS == 1)
begin

    for (a=0; a<MEM_IF_DQS_WIDTH; a = a+1)
    begin : gen_loop_dqs

        always @(posedge phy_clk_1x or negedge reset_phy_clk_1x_n)
        begin
            if (reset_phy_clk_1x_n == 1'b0)
            begin
                wdp_wdqs_2x[a]     <= 0;
                wdp_wdqs_oe_2x[a]  <= 0;
            end
            else
            begin
                wdp_wdqs_2x[a]     <= wdp_wdqs_oe_2x[a];
                wdp_wdqs_oe_2x[a]  <= mem_dqs_burst;
            end
        end
    end
end
endgenerate

///////////////////////////////////////////////////////////////////
// Generate the write DQ logic.
// These are internal registers which will be used to assign to:
// wdp_wdata_h_2x, wdp_wdata_l_2x, wdp_wdata_oe_2x
// (these get output to the IOEs).
//////////////////////////////////////////////////////////////////

// number of times to replicate mem_wdata_valid bits
parameter DQ_OE_REPS = MEM_IF_DQ_PER_DQS/NUM_DUPLICATE_REGS;

generate
for (a=0; a<MEM_IF_DWIDTH/MEM_IF_DQ_PER_DQS; a=a+1)
begin : gen_dq_oe_2x
    always @(posedge phy_clk_1x or negedge reset_phy_clk_1x_n)
    begin

        if (reset_phy_clk_1x_n == 1'b0)
        begin
            dq_oe_2x[DQ_OE_REPS*(a+1)-1:DQ_OE_REPS*a] <= {(DQ_OE_REPS){1'b0}};
        end
        else

        begin
            dq_oe_2x[DQ_OE_REPS*(a+1)-1:DQ_OE_REPS*a] <= {(DQ_OE_REPS){mem_wdata_valid[a]}};
        end

    end
end
endgenerate

////////////////////////////////////////////////////////////////////////////////
// fanout the dq_oe_2x, which has one register per NUM_DUPLICATE_REGS
// (to save registers), to each bit of wdp_wdata_oe_2x_int and then
// assign to the output wire wdp_wdata_oe_2x( ie one oe for each DQ "pin").
////////////////////////////////////////////////////////////////////////////////

always @(dq_oe_2x)
begin

    for (j=0; j<MEM_IF_DWIDTH; j=j+1)
    begin
        wdp_wdata_oe_2x_int[j] = dq_oe_2x[(j/NUM_DUPLICATE_REGS)];
    end

end

assign wdp_wdata_oe_2x = wdp_wdata_oe_2x_int;


//////////////////////////////////////////////////////////////////////
// Write DQ mapping from mem_wdata to wdata_l_2x, wdata_h_2x
//////////////////////////////////////////////////////////////////////

generate
for (c=0; c<MEM_IF_DWIDTH; c=c+1)
begin : gen_wdata

    always @(posedge phy_clk_1x)
    begin
            wdp_wdata_l_2x[c] <= mem_wdata[c +   MEM_IF_DWIDTH];
            wdp_wdata_h_2x[c] <= mem_wdata[c                  ];

    end
end
endgenerate



///////////////////////////////////////////////////////
// Conditional generation of DM logic, based on generic
///////////////////////////////////////////////////////

generate
if (MEM_IF_DM_PINS_EN == 1'b1)
begin : dm_logic_enabled


    ///////////////////////////////////////////////////////////////////
    // Write DM logic: assignment to wdp_dm_h_2x, wdp_dm_l_2x
    ///////////////////////////////////////////////////////////////////



    // for loop inside a generate statement, but outside a procedural block
    // is treated as a nested generate

    for (d=0; d<MEM_IF_DM_WIDTH; d=d+1)
    begin : gen_dm

        always @(posedge phy_clk_1x)
        begin

            if (mem_wdata_valid[d] == 1'b1) // nb might need to duplicate to meet timing
            begin
                wdp_dm_l_2x[d] <= mem_be[d+MEM_IF_DM_WIDTH];
                wdp_dm_h_2x[d] <= mem_be[d];
            end

            // To support writes of less than the burst length, mask data when invalid :
            else
            begin
                wdp_dm_l_2x[d] <= 1'b1;
                wdp_dm_h_2x[d] <= 1'b1;
            end




        end
    end // block: gen_dm


end // block: dm_logic_enabled


endgenerate


endmodule



//

`ifdef ALT_MEM_PHY_DEFINES
`else
`include "alt_mem_phy_defines.v"
`endif

`default_nettype none

//
module ddr2_64bit_phy_alt_mem_phy_rdata_valid ( // inputs
                               phy_clk_1x,
                               reset_phy_clk_1x_n,
                               seq_rdata_valid_lat_dec,
                               seq_rdata_valid_lat_inc,
                               seq_doing_rd,
                               ctl_doing_rd,
                               ctl_cal_success,
                               
                               // outputs
                               ctl_rdata_valid,
                               seq_rdata_valid
                              );

parameter FAMILY                       = "CYCLONEIII";
parameter MEM_IF_DQS_WIDTH             = 8;                              
parameter RDATA_VALID_AWIDTH           = 5;
parameter RDATA_VALID_INITIAL_LAT      = 16;
parameter DWIDTH_RATIO                 = 2;

localparam MAX_RDATA_VALID_DELAY       = 2 ** RDATA_VALID_AWIDTH;
localparam RDV_DELAY_SHR_LEN           = MAX_RDATA_VALID_DELAY*(DWIDTH_RATIO/2);

// clocks
input  wire                                              phy_clk_1x;

// resets
input  wire                                              reset_phy_clk_1x_n;

// control signals from sequencer
input  wire                                              seq_rdata_valid_lat_dec;
input  wire                                              seq_rdata_valid_lat_inc;
input  wire [MEM_IF_DQS_WIDTH * DWIDTH_RATIO / 2 -1 : 0] seq_doing_rd;
input  wire [MEM_IF_DQS_WIDTH * DWIDTH_RATIO / 2 -1 : 0] ctl_doing_rd;
input  wire                                              ctl_cal_success;

// output to IOE
output reg [DWIDTH_RATIO / 2 -1 : 0]                     ctl_rdata_valid;
output reg [DWIDTH_RATIO / 2 -1 : 0]                     seq_rdata_valid;

// Internal Signals / Variables
reg  [RDATA_VALID_AWIDTH - 1 : 0]                         rd_addr;
reg  [RDATA_VALID_AWIDTH - 1 : 0]                         wr_addr;
reg  [RDATA_VALID_AWIDTH - 1 : 0]                         next_wr_addr;
reg  [DWIDTH_RATIO/2 - 1 : 0]                             wr_data;

wire [DWIDTH_RATIO / 2 -1 : 0]                            int_rdata_valid;
reg  [DWIDTH_RATIO/2 - 1 : 0]                             rdv_pipe_ip;
reg                                                       rdv_pipe_ip_beat2_r;
reg  [MEM_IF_DQS_WIDTH * DWIDTH_RATIO/2 - 1 : 0]          merged_doing_rd;
reg                                                       seq_rdata_valid_lat_dec_1t;
reg                                                       seq_rdata_valid_lat_inc_1t;
reg                                                       bit_order_1x;

// Generate the input to the RDV delay.
// Also determine the data for the OCT control & postamble paths (merged_doing_rd)
generate
    if (DWIDTH_RATIO == 4)
    begin : merging_doing_rd_halfrate
        always @*
        begin
            merged_doing_rd = seq_doing_rd | (ctl_doing_rd & {(2 * MEM_IF_DQS_WIDTH) {ctl_cal_success}});
            rdv_pipe_ip[0]  = | merged_doing_rd[    MEM_IF_DQS_WIDTH - 1 : 0];
            rdv_pipe_ip[1]  = | merged_doing_rd[2 * MEM_IF_DQS_WIDTH - 1 : MEM_IF_DQS_WIDTH];
        end
    end
    else  // DWIDTH_RATIO == 2
    begin : merging_doing_rd_fullrate
        always @*
        begin
            merged_doing_rd = seq_doing_rd | (ctl_doing_rd & { MEM_IF_DQS_WIDTH {ctl_cal_success}});
            rdv_pipe_ip[0]  = | merged_doing_rd[MEM_IF_DQS_WIDTH - 1 : 0];
        end
    end // else: !if(DWIDTH_RATIO == 4)
endgenerate


// Register inc/dec rdata_valid signals and generate bit_order_1x
always @(posedge phy_clk_1x or negedge reset_phy_clk_1x_n)
begin
    if (reset_phy_clk_1x_n == 1'b0)
        begin
            seq_rdata_valid_lat_dec_1t <= 1'b0;
            seq_rdata_valid_lat_inc_1t <= 1'b0;
            bit_order_1x               <= 1'b1;
            
        end
    else
        begin
            rdv_pipe_ip_beat2_r <= rdv_pipe_ip[DWIDTH_RATIO/2 - 1];
            seq_rdata_valid_lat_dec_1t <= seq_rdata_valid_lat_dec;
            seq_rdata_valid_lat_inc_1t <= seq_rdata_valid_lat_inc;
            
            if (DWIDTH_RATIO == 2)
                bit_order_1x <= 1'b0;
            else if (seq_rdata_valid_lat_dec == 1'b1 && seq_rdata_valid_lat_dec_1t == 1'b0)
            begin
                bit_order_1x <=  ~bit_order_1x;
            end
            
            else if (seq_rdata_valid_lat_inc == 1'b1 && seq_rdata_valid_lat_inc_1t == 1'b0)
            begin
                bit_order_1x <= ~bit_order_1x;
            end
        end
end

// write data
generate // based on DWIDTH RATIO
  if (DWIDTH_RATIO == 4) // Half Rate
  begin : halfrate_wdata_gen
      always @* // combinational logic sensitivity
      begin

        if (bit_order_1x == 1'b0)
        begin
            wr_data  = {rdv_pipe_ip[1], rdv_pipe_ip[0]};
        end

        else
        begin
            wr_data  = {rdv_pipe_ip[0], rdv_pipe_ip_beat2_r};
        end
      end
  end
else // Full-rate
 begin : fullrate_wdata_gen

  always @* // combinational logic sensitivity
    begin
        wr_data = rdv_pipe_ip;
    end
  end
endgenerate

// write address
always @*
begin

    next_wr_addr = wr_addr + 1'b1;

    if (seq_rdata_valid_lat_dec == 1'b1 && seq_rdata_valid_lat_dec_1t == 1'b0)
    begin

        if ((bit_order_1x == 1'b0) || (DWIDTH_RATIO == 2))
        begin
            next_wr_addr = wr_addr;
        end

    end

    else if (seq_rdata_valid_lat_inc == 1'b1 && seq_rdata_valid_lat_inc_1t == 1'b0)
    begin

        if ((bit_order_1x == 1'b1) || (DWIDTH_RATIO ==2))
        begin
            next_wr_addr = wr_addr + 2'h2;
        end

    end

end


always @(posedge phy_clk_1x or negedge reset_phy_clk_1x_n)
begin

    if (reset_phy_clk_1x_n == 1'b0)
    begin
        wr_addr <= RDATA_VALID_INITIAL_LAT[RDATA_VALID_AWIDTH - 1 : 0];
    end

    else
    begin
        wr_addr <= next_wr_addr;
    end

end

//     read address generator : just a free running counter.
always @(posedge phy_clk_1x or negedge reset_phy_clk_1x_n)
begin

    if (reset_phy_clk_1x_n == 1'b0)
    begin
        rd_addr <= {RDATA_VALID_AWIDTH{1'b0}};
    end

    else
    begin
        rd_addr <= rd_addr + 1'b1;     //inc address, can wrap
    end

end

// altsyncram instance
altsyncram #(.
    address_aclr_b            ("NONE"),
	.address_reg_b            ("CLOCK0"),
	.clock_enable_input_a     ("BYPASS"),
	.clock_enable_input_b     ("BYPASS"),
	.clock_enable_output_b    ("BYPASS"),
	.intended_device_family   (FAMILY),
	.lpm_type                 ("altsyncram"),
	.numwords_a               (2**RDATA_VALID_AWIDTH),
	.numwords_b               (2**RDATA_VALID_AWIDTH),
	.operation_mode           ("DUAL_PORT"),
	.outdata_aclr_b           ("NONE"),
	.outdata_reg_b            ("CLOCK0"),
	.power_up_uninitialized   ("FALSE"),
	.widthad_a                (RDATA_VALID_AWIDTH),
	.widthad_b                (RDATA_VALID_AWIDTH),
	.width_a                  (DWIDTH_RATIO/2),
	.width_b                  (DWIDTH_RATIO/2),
	.width_byteena_a          (1)
) altsyncram_component (
	.wren_a                   (1'b1),
	.clock0                   (phy_clk_1x),
	.address_a                (wr_addr),
	.address_b                (rd_addr),
	.data_a                   (wr_data),
	.q_b                      (int_rdata_valid),
	.aclr0                    (1'b0),
	.aclr1                    (1'b0),
	.addressstall_a           (1'b0),
	.addressstall_b           (1'b0),
	.byteena_a                (1'b1),
	.byteena_b                (1'b1),
	.clock1                   (1'b1),
	.clocken0                 (1'b1),
	.clocken1                 (1'b1),
	.clocken2                 (1'b1),
	.clocken3                 (1'b1),
	.data_b                   ({(DWIDTH_RATIO/2){1'b1}}),
	.eccstatus                (),
	.q_a                      (),
	.rden_a                   (1'b1),
	.rden_b                   (1'b1),
	.wren_b                   (1'b0)
);

// Generate read data valid enable signals for controller and seqencer
always @(posedge phy_clk_1x or negedge reset_phy_clk_1x_n)
begin
    if (reset_phy_clk_1x_n == 1'b0)
        begin
            ctl_rdata_valid <= {(DWIDTH_RATIO/2){1'b0}};
            seq_rdata_valid <= {(DWIDTH_RATIO/2){1'b0}};
        end
    else
        begin
            // shift the shift register by DWIDTH_RATIO locations
            // rdv_delay_index plus (DWIDTH_RATIO/2)-1 bits counting down
            ctl_rdata_valid <= int_rdata_valid & {(DWIDTH_RATIO/2){ctl_cal_success}};
            seq_rdata_valid <= int_rdata_valid;
        end
end
 

endmodule

`default_nettype wire

//

`ifdef ALT_MEM_PHY_DEFINES
`else
`include "alt_mem_phy_defines.v"
`endif

//
module ddr2_64bit_phy_alt_mem_phy_mux (
                         phy_clk_1x,
                         reset_phy_clk_1x_n,

// MUX Outputs to controller :
                         ctl_address,
                         ctl_read_req,
                         ctl_wdata,
                         ctl_write_req,
                         ctl_size,
                         ctl_be,
                         ctl_refresh_req,
                         ctl_burstbegin,

// Controller inputs to the MUX :
                         ctl_ready,
                         ctl_wdata_req,
                         ctl_rdata,
                         ctl_rdata_valid,
                         ctl_refresh_ack,
                         ctl_init_done,

// MUX Select line :
                         ctl_usr_mode_rdy,

// MUX inputs from local interface :
                         local_address,
                         local_read_req,
                         local_wdata,
                         local_write_req,
                         local_size,
                         local_be,
                         local_refresh_req,
                         local_burstbegin,

// MUX outputs to sequencer :
                         mux_seq_controller_ready,
                         mux_seq_wdata_req,

// MUX inputs from sequencer :
                         seq_mux_address,
                         seq_mux_read_req,
                         seq_mux_wdata,
                         seq_mux_write_req,
                         seq_mux_size,
                         seq_mux_be,
                         seq_mux_refresh_req,
                         seq_mux_burstbegin,

// Changes made to accomodate new ports for self refresh/power-down & Auto precharge in HP Controller (User to PHY)
                         local_autopch_req,
                         local_powerdn_req,
                         local_self_rfsh_req,
                         local_powerdn_ack,
                         local_self_rfsh_ack,
			
// Changes made to accomodate new ports for self refresh/power-down & Auto precharge in HP Controller (PHY to Controller)
                         ctl_autopch_req,
                         ctl_powerdn_req,
                         ctl_self_rfsh_req,
                         ctl_powerdn_ack,
                         ctl_self_rfsh_ack,

// Also MUX some signals from the controller to the local interface :
                         local_ready,
                         local_wdata_req,
                         local_init_done,
                         local_rdata,
                         local_rdata_valid,
                         local_refresh_ack

                       );


parameter LOCAL_IF_AWIDTH       =  26;
parameter LOCAL_IF_DWIDTH       = 256;
parameter LOCAL_BURST_LEN_BITS  =   1;
parameter MEM_IF_DQ_PER_DQS     =   8;
parameter MEM_IF_DWIDTH         =  64;

input wire                                           phy_clk_1x;
input wire                                           reset_phy_clk_1x_n;

// MUX Select line :
input wire                                           ctl_usr_mode_rdy;

// MUX inputs from local interface :
input wire [LOCAL_IF_AWIDTH - 1 : 0]                 local_address;
input wire                                           local_read_req;
input wire [LOCAL_IF_DWIDTH - 1 : 0]                 local_wdata;
input wire                                           local_write_req;
input wire [LOCAL_BURST_LEN_BITS - 1 : 0]            local_size;
input wire [(LOCAL_IF_DWIDTH/8) - 1 : 0]             local_be;
input wire                                           local_refresh_req;
input wire                                           local_burstbegin;

// MUX inputs from sequencer :
input wire [LOCAL_IF_AWIDTH - 1 : 0]                 seq_mux_address;
input wire                                           seq_mux_read_req;
input wire [LOCAL_IF_DWIDTH - 1 : 0]                 seq_mux_wdata;
input wire                                           seq_mux_write_req;
input wire [LOCAL_BURST_LEN_BITS - 1 : 0]            seq_mux_size;
input wire [(LOCAL_IF_DWIDTH/8) - 1:0]               seq_mux_be;
input wire                                           seq_mux_refresh_req;
input wire                                           seq_mux_burstbegin;

// MUX Outputs to controller :
output reg [LOCAL_IF_AWIDTH - 1 : 0]                 ctl_address;
output reg                                           ctl_read_req;
output reg [LOCAL_IF_DWIDTH - 1 : 0]                 ctl_wdata;
output reg                                           ctl_write_req;
output reg [LOCAL_BURST_LEN_BITS - 1 : 0]            ctl_size;
output reg [(LOCAL_IF_DWIDTH/8) - 1:0]               ctl_be;
output reg                                           ctl_refresh_req;
output reg                                           ctl_burstbegin;


// The "ready" input from the controller shall be passed to either the
// local interface if in user mode, or the sequencer :
input wire                                           ctl_ready;
output reg                                           local_ready;
output reg                                           mux_seq_controller_ready;

// The controller's "wdata req" output is similarly passed to either
// the local interface if in user mode, or the sequencer :
input wire                                           ctl_wdata_req;
output reg                                           local_wdata_req;
output reg                                           mux_seq_wdata_req;

input wire                                           ctl_init_done;
output reg                                           local_init_done;

input wire [LOCAL_IF_DWIDTH - 1 : 0]                 ctl_rdata;
output reg  [LOCAL_IF_DWIDTH - 1 : 0]                local_rdata;

input wire                                           ctl_rdata_valid;
output reg                                           local_rdata_valid;

input wire                                           ctl_refresh_ack;
output reg                                           local_refresh_ack;

//-> Changes made to accomodate new ports for self refresh/power-down & Auto precharge in HP Controller (User to PHY)
input  wire                                          local_autopch_req;
input  wire                                          local_powerdn_req;
input  wire                                          local_self_rfsh_req;
output reg                                           local_powerdn_ack;
output reg                                           local_self_rfsh_ack;
			
// --> Changes made to accomodate new ports for self refresh/power-down & Auto precharge in HP Controller (PHY to Controller)
output reg                                           ctl_autopch_req;
output reg                                           ctl_powerdn_req;
output reg                                           ctl_self_rfsh_req;
input  wire                                          ctl_powerdn_ack;
input  wire                                          ctl_self_rfsh_ack;


wire                                                 local_burstbegin_held;
reg                                                  burstbegin_hold;

always @(posedge phy_clk_1x or negedge reset_phy_clk_1x_n)
begin

    if (reset_phy_clk_1x_n == 1'b0) 
        burstbegin_hold <= 1'b0;
        
    else
    begin
    
        if (local_ready == 1'b0 && (local_write_req == 1'b1 || local_read_req == 1'b1) && local_burstbegin == 1'b1)
            burstbegin_hold <= 1'b1;
        else if (local_ready == 1'b1 && (local_write_req == 1'b1 || local_read_req == 1'b1))
            burstbegin_hold <= 1'b0;
            
    end
end

// Gate the local burstbegin signal with the held version :
assign local_burstbegin_held = burstbegin_hold || local_burstbegin;

always @*
begin

    if (ctl_usr_mode_rdy == 1'b1)
    begin

        // Pass local interface signals to the controller if ready :
        ctl_address            = local_address;
        ctl_read_req           = local_read_req;
        ctl_wdata              = local_wdata;
        ctl_write_req          = local_write_req;
        ctl_size               = local_size;
        ctl_be                 = local_be;
        ctl_refresh_req        = local_refresh_req;
        ctl_burstbegin         = local_burstbegin_held;

        // If in user mode,  pass on the controller's ready
        // and wdata request signals to the local interface :
        local_ready         = ctl_ready;
        local_wdata_req     = ctl_wdata_req;
        local_init_done     = ctl_init_done;
        local_rdata         = ctl_rdata;
        local_rdata_valid   = ctl_rdata_valid;
        local_refresh_ack   = ctl_refresh_ack;

        // Whilst indicate to the sequencer that the controller is busy :
        mux_seq_controller_ready = 1'b0;
        mux_seq_wdata_req        = 1'b0;

        // Autopch_req & Local_power_req changes
        ctl_autopch_req     = local_autopch_req;
        ctl_powerdn_req     = local_powerdn_req;
        ctl_self_rfsh_req   = local_self_rfsh_req;
        local_powerdn_ack   = ctl_powerdn_ack;
        local_self_rfsh_ack = ctl_self_rfsh_ack;


    end

    else
    begin

        // Pass local interface signals to the sequencer if not in user mode :

        // NB. The controller will have more address bits than the sequencer, so
        // these are zero padded :
        ctl_address            = seq_mux_address;
        ctl_read_req           = seq_mux_read_req;
        ctl_wdata              = seq_mux_wdata;
        ctl_write_req          = seq_mux_write_req;
        ctl_size               = seq_mux_size;        // NB. Should be tied-off when the mux is instanced
        ctl_be                 = seq_mux_be;          // NB. Should be tied-off when the mux is instanced
        ctl_refresh_req        = local_refresh_req; // NB. Should be tied-off when the mux is instanced
        ctl_burstbegin         = seq_mux_burstbegin; // NB. Should be tied-off when the mux is instanced

        // Indicate to the local IF that the controller is busy :
        local_ready         = 1'b0;
        local_wdata_req     = 1'b0;
        local_init_done     = 1'b0;
        local_rdata         = {LOCAL_IF_DWIDTH{1'b0}};
        local_rdata_valid   = 1'b0;
        local_refresh_ack   = ctl_refresh_ack;

        // If not in user mode,  pass on the controller's ready
        // and wdata request signals to the sequencer :
        mux_seq_controller_ready   = ctl_ready;
        mux_seq_wdata_req   = ctl_wdata_req;

       // Autopch_req & Local_power_req changes
        ctl_autopch_req     = 1'b0;
        ctl_powerdn_req     = 1'b0;
        ctl_self_rfsh_req   = local_self_rfsh_req;
        local_powerdn_ack   = 1'b0;
        local_self_rfsh_ack = ctl_self_rfsh_ack;

    end

end




endmodule

//

`default_nettype none

`ifdef ALT_MEM_PHY_DEFINES
`else
`include "alt_mem_phy_defines.v"
`endif

/* -----------------------------------------------------------------------------
// module description                                                        
---------------------------------------------------------------------------- */
//
module ddr2_64bit_phy_alt_mem_phy_mimic(
                         //Inputs 
        
                         //Clocks 
                         measure_clk,         // full rate clock from PLL
                         mimic_data_in,       // Input against which the VT variations 
                                              // are tracked (e.g. memory clock)        

                         // Active low reset                            
                         reset_measure_clk_n, 
        
                         //Indicates that the mimic calibration sequence can start
                         seq_mmc_start,       // from sequencer

        
                         //Outputs      
                         mmc_seq_done,        // mimic calibration finished for the current PLL phase       
                         mmc_seq_value        // result value of the mimic calibration
        
        );

   input  wire measure_clk;
   input  wire mimic_data_in;
   input  wire reset_measure_clk_n;
   input  wire seq_mmc_start;
   output wire mmc_seq_done;
   output wire mmc_seq_value;
   
   function integer clogb2;
      input [31:0] value;
      for (clogb2=0; value>0; clogb2=clogb2+1)
          value = value >> 1;
   endfunction // clogb2



   
   // Parameters        
   parameter NUM_MIMIC_SAMPLE_CYCLES = 6;     

   parameter SHIFT_REG_COUNTER_WIDTH = clogb2(NUM_MIMIC_SAMPLE_CYCLES); 
   

   reg [`MIMIC_FSM_WIDTH-1:0]           mimic_state;
   
  
   reg [2:0]                            seq_mmc_start_metastable; 
   wire                                 start_edge_detected;      
   
   (* altera_attribute=" -name fast_input_register OFF"*) reg [1:0] mimic_data_in_metastable; 


   wire                                 mimic_data_in_sample;   

   wire                                 shift_reg_data_out_all_ones; 
   reg                                  mimic_done_out;         
   reg                                  mimic_value_captured;      


   reg [SHIFT_REG_COUNTER_WIDTH : 0]    shift_reg_counter;              
   reg                                  shift_reg_enable;               

   wire                                 shift_reg_data_in; 
   reg                                  shift_reg_s_clr;
   wire                                 shift_reg_a_clr;   

   reg [NUM_MIMIC_SAMPLE_CYCLES -1 : 0] shift_reg_data_out;   
    
   // shift register which contains the sampled data
   always @(posedge measure_clk or posedge shift_reg_a_clr)
   begin
      if (shift_reg_a_clr == 1'b1)
      begin
          shift_reg_data_out    <= {NUM_MIMIC_SAMPLE_CYCLES{1'b0}};
      end
     
      else
      begin
         if (shift_reg_s_clr == 1'b1)
         begin
             shift_reg_data_out <= {NUM_MIMIC_SAMPLE_CYCLES{1'b0}};
         end     

         else if (shift_reg_enable == 1'b1)
         begin
             shift_reg_data_out <= {(shift_reg_data_out[NUM_MIMIC_SAMPLE_CYCLES -2 : 0]), shift_reg_data_in};         
         end     
      end 
   end 
          
    
  // Metastable-harden mimic_start : 
  always @(posedge measure_clk or negedge reset_measure_clk_n)
  begin
  
    if (reset_measure_clk_n == 1'b0)
    begin
        seq_mmc_start_metastable    <= 0;    
    end
    else

    begin
        seq_mmc_start_metastable[0] <= seq_mmc_start;
        seq_mmc_start_metastable[1] <= seq_mmc_start_metastable[0]; 
        seq_mmc_start_metastable[2] <= seq_mmc_start_metastable[1];    
    end 
  
  end 

  assign start_edge_detected =  seq_mmc_start_metastable[1] 
                             && !seq_mmc_start_metastable[2];
                                                                
  // Metastable-harden mimic_data_in : 
  always @(posedge measure_clk or negedge reset_measure_clk_n)
  begin

    if (reset_measure_clk_n == 1'b0)
    begin
        mimic_data_in_metastable    <= 0; 
    end
      //some mimic paths configurations have another flop inside the wysiwyg ioe 
    else
      
    begin  
        mimic_data_in_metastable[0] <= mimic_data_in;    
        mimic_data_in_metastable[1] <= mimic_data_in_metastable[0];             
    end 
    
  end 
  
  assign mimic_data_in_sample =  mimic_data_in_metastable[1];
   
  // Main FSM : 
  always @(posedge measure_clk or negedge reset_measure_clk_n )
  begin
  
     if (reset_measure_clk_n == 1'b0)
     begin
  
         mimic_state           <= `MIMIC_IDLE;
      
         mimic_done_out        <= 1'b0;
         mimic_value_captured  <= 1'b0;
      
         shift_reg_counter     <= 0;       
         shift_reg_enable      <= 1'b0;
         shift_reg_s_clr       <= 1'b0;  

     end 
     
     else
     begin
  
         case (mimic_state)
      
         `MIMIC_IDLE : begin
        
                           shift_reg_counter     <= 0;       
                           mimic_done_out        <= 1'b0;
                           shift_reg_s_clr       <= 1'b1;
                           shift_reg_enable      <= 1'b1;         
            
                           if (start_edge_detected == 1'b1)
                           begin
                               mimic_state       <= `MIMIC_SAMPLE; 
                               shift_reg_counter <= shift_reg_counter + 1'b1;     
                               shift_reg_s_clr   <= 1'b0;
                           end             
                           else
                             
                           begin
                               mimic_state <= `MIMIC_IDLE; 
                           end
         end // case: MIMIC_IDLE
           
           `MIMIC_SAMPLE : begin
  
                               shift_reg_counter        <= shift_reg_counter + 1'b1;     
              
                               if (shift_reg_counter == NUM_MIMIC_SAMPLE_CYCLES + 1)

                               begin
                                   mimic_done_out       <= 1'b1; 
                                   mimic_value_captured <= shift_reg_data_out_all_ones; //captured only here
                                   shift_reg_enable     <= 1'b0;        
                                   shift_reg_counter    <= shift_reg_counter;          
                                   mimic_state          <= `MIMIC_SEND;                    
                               end 
           end // case: MIMIC_SAMPLE
           
           `MIMIC_SEND : begin
        
                             mimic_done_out  <= 1'b1; //redundant statement, here just for readibility 
                             mimic_state     <= `MIMIC_SEND1; 

            /* mimic_value_captured will not change during MIMIC_SEND
               it will change next time mimic_done_out is asserted
               mimic_done_out will be reset during MIMIC_IDLE
               the purpose of the current state is to add one clock cycle
               mimic_done_out will be active for 2 measure_clk clock cycles, i.e
               the pulses duration will be just one sequencer clock cycle
               (which is half rate) */
           end // case: MIMIC_SEND

           // MIMIC_SEND1 and MIMIC_SEND2 extend the mimic_done_out signal by another 2 measure_clk_2x cycles
           // so it is a total of 4 measure clocks long (ie 2 half-rate clock cycles long in total)
           `MIMIC_SEND1 : begin
        
                              mimic_done_out  <= 1'b1; //redundant statement, here just for readibility 
                              mimic_state     <= `MIMIC_SEND2; 

           end 

           `MIMIC_SEND2 : begin
        
                              mimic_done_out  <= 1'b1; //redundant statement, here just for readibility 
                              mimic_state     <= `MIMIC_IDLE; 

           end 

           
           default : begin
                         mimic_state <= `MIMIC_IDLE;
           end
           
            
         endcase
      
     end 
  
  end 
  
  assign shift_reg_data_out_all_ones   = (( & shift_reg_data_out) == 1'b1) ? 1'b1 
                                                                           : 1'b0;
   
  // Shift Register assignments
  assign shift_reg_data_in  =  mimic_data_in_sample;
  assign shift_reg_a_clr    =  !reset_measure_clk_n; 
  
  // Output assignments  
  assign mmc_seq_done    = mimic_done_out;   
  assign mmc_seq_value   = mimic_value_captured;         
  
  
endmodule

`default_nettype wire

//


/* -----------------------------------------------------------------------------
// module description                                                        
----------------------------------------------------------------------------- */
//
module ddr2_64bit_phy_alt_mem_phy_mimic_debug(
        // Inputs
        
        // Clocks 
        measure_clk,    // full rate clock from PLL

        // Active low reset                             
        reset_measure_clk_n, 

        mimic_recapture_debug_data, // from user board button
        
        mmc_seq_done,   // mimic calibration finished for the current PLL phase
        mmc_seq_value   // result value of the mimic calibration        

        );


   // Parameters 

   parameter NUM_DEBUG_SAMPLES_TO_STORE = 4096;   // can range from 4096 to 524288
   parameter PLL_STEPS_PER_CYCLE        = 24;     // can  range from 16 to 48  
   
   input wire measure_clk;  
   input wire reset_measure_clk_n;

   input wire mimic_recapture_debug_data;

   input wire mmc_seq_done; 
   input wire mmc_seq_value; 


   function integer clogb2;
      input [31:0] value;
      for (clogb2=0; value>0; clogb2=clogb2+1)
          value = value >> 1;
   endfunction // clogb2
   

   parameter RAM_WR_ADDRESS_WIDTH = clogb2(NUM_DEBUG_SAMPLES_TO_STORE - 1); // can range from 12 to 19 


   reg                                       s_clr_ram_wr_address_count; 

   reg [(clogb2(PLL_STEPS_PER_CYCLE)-1) : 0] mimic_sample_count;        

   reg [RAM_WR_ADDRESS_WIDTH-1 : 0 ]         ram_write_address; 
   wire                                      ram_wr_enable;             
   wire [0:0]                                debug_ram_data;            
   reg                                       clear_ram_wr_enable;               

   reg [1:0]                                 mimic_recapture_debug_data_metastable; 

   wire                                      mimic_done_in_dbg; // for internal use, just 1 measure_clk cycles long
   reg                                       mmc_seq_done_r;               
  

   // generate mimic_done_in_debug : a single clock wide pulse based on the rising edge of mmc_seq_done:

   always @ (posedge measure_clk or negedge reset_measure_clk_n)
   begin  
     if (reset_measure_clk_n == 1'b0)      // asynchronous reset (active low)
     begin
         mmc_seq_done_r <= 1'b0;
     end
     else

     begin
         mmc_seq_done_r <= mmc_seq_done;
     end
      
   end


   assign mimic_done_in_dbg   = mmc_seq_done && !mmc_seq_done_r;  

   assign ram_wr_enable       = mimic_done_in_dbg && !clear_ram_wr_enable;
   assign debug_ram_data[0]   = mmc_seq_value;

    

  altsyncram #(

                .clock_enable_input_a   ( "BYPASS"),
                .clock_enable_output_a  ( "BYPASS"),
                .intended_device_family ( "Stratix II"),
                .lpm_hint               ( "ENABLE_RUNTIME_MOD=YES, INSTANCE_NAME=MRAM"),
                .lpm_type               ( "altsyncram"),
                .maximum_depth          ( 4096),
                .numwords_a             ( 4096),
                .operation_mode         ( "SINGLE_PORT"),
                .outdata_aclr_a         ( "NONE"),
                .outdata_reg_a          ( "UNREGISTERED"),
                .power_up_uninitialized ( "FALSE"),
                .widthad_a              ( 12),
                .width_a                ( 1),
                .width_byteena_a        ( 1)
        )
         altsyncram_component (
                .wren_a                 ( ram_wr_enable),
                .clock0                 ( measure_clk),
                .address_a              ( ram_write_address),
                .data_a                 ( debug_ram_data),
                .q_a                    ( )
        );
   

   

  //  Metastability_mimic_recapture_debug_data : 
  always @(posedge measure_clk or negedge reset_measure_clk_n)
  begin

    if (reset_measure_clk_n == 1'b0)
    begin 
        mimic_recapture_debug_data_metastable    <=  2'b0;
    end
    else

    begin
        mimic_recapture_debug_data_metastable[0] <= mimic_recapture_debug_data;
        mimic_recapture_debug_data_metastable[1] <= mimic_recapture_debug_data_metastable[0];   
    end
    
  end 
   


  //mimic_sample_counter : 
  always @(posedge measure_clk or negedge reset_measure_clk_n)
  begin
  
    if (reset_measure_clk_n == 1'b0) 
    begin
        mimic_sample_count <= 0;        // (others => '0'); 
    end  
    else

    begin
      if (mimic_done_in_dbg == 1'b1)
      begin
          mimic_sample_count <= mimic_sample_count + 1'b1;       

          if (mimic_sample_count == PLL_STEPS_PER_CYCLE-1)
          begin
              mimic_sample_count <= 0; //(others => '0');
          end
                
      end 
      
    end 
  
  end 

  

  //RAMWrAddressCounter : 
  always @(posedge measure_clk or negedge reset_measure_clk_n)
  begin
  
      if (reset_measure_clk_n == 1'b0)
      begin
          ram_write_address <= 0;      //(others => '0');   
          clear_ram_wr_enable <= 1'b0;
      end
      else

      begin
  
          if (s_clr_ram_wr_address_count == 1'b1) // then --Active high synchronous reset
          begin
              ram_write_address <= 0;      //(others => '0');
              clear_ram_wr_enable <= 1'b1;         
          end

          else       
          begin
              clear_ram_wr_enable <= 1'b0;   
          
              if (mimic_done_in_dbg == 1'b1)
              begin
                  if (ram_write_address != NUM_DEBUG_SAMPLES_TO_STORE-1)  
                  begin 
                      ram_write_address <= ram_write_address + 1'b1;             
                  end
                  
                  else
                  begin
                      clear_ram_wr_enable <= 1'b1;        
                  end 
              end
          
          end 
      
      end
  
  end 
  
  //ClearRAMWrAddressCounter : 
  always @(posedge measure_clk or negedge reset_measure_clk_n)
  begin
  
      if (reset_measure_clk_n == 1'b0)
      begin
          s_clr_ram_wr_address_count <= 1'b0;  
      end       

      else
      begin
          if (mimic_recapture_debug_data_metastable[1] == 1'b1)
          begin
              s_clr_ram_wr_address_count <= 1'b1;
          end
       
          else if (mimic_sample_count == 0)       
          begin
              s_clr_ram_wr_address_count <= 1'b0;
          end
      
      end 
  
  end 
  
  endmodule

//

`ifdef ALT_MEM_PHY_DEFINES
`else
`include "alt_mem_phy_defines.v"
`endif

//
module ddr2_64bit_phy_alt_mem_phy_reset_pipe (
                                input  wire clock,
                                input  wire pre_clear,
                                output wire reset_out
                              );

parameter PIPE_DEPTH = 4;

    // Declare pipeline registers.
    reg [PIPE_DEPTH - 1 : 0]  ams_pipe;
    integer                   i;

//    begin : RESET_PIPE
        always @(posedge clock or negedge pre_clear)
        begin

            if (pre_clear == 1'b0)
            begin
                ams_pipe <= 0;
            end

            else
            begin
               for (i=0; i< PIPE_DEPTH; i = i + 1)
               begin
                   if (i==0)
                       ams_pipe[i] <= 1'b1;
                   else
                       ams_pipe[i] <= ams_pipe[i-1];
               end
            end // if-else

        end // always
//    end

    assign reset_out = ams_pipe[PIPE_DEPTH-1];


endmodule
