//
// Copyright (c) 2016 University of Cambridge All rights reserved.
//
// Author: Marco Forconesi
//
// This software was developed with the support of 
// Prof. Gustavo Sutter and Prof. Sergio Lopez-Buedo and
// University of Cambridge Computer Laboratory NetFPGA team.
//
// @NETFPGA_LICENSE_HEADER_START@
//
// Licensed to NetFPGA C.I.C. (NetFPGA) under one or more
// contributor license agreements.  See the NOTICE file distributed with this
// work for additional information regarding copyright ownership.  NetFPGA
// licenses this file to you under the NetFPGA Hardware-Software License,
// Version 1.0 (the "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at:
//
//   http://www.netfpga-cic.org
//
// Unless required by applicable law or agreed to in writing, Work distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations under the License.
//
// @NETFPGA_LICENSE_HEADER_END@

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
`timescale 1ns / 1ps
//`default_nettype none

module xgmii2axis (

    // Clks and resets
    input                    clk,
    input                    rst,

    // Stats
    output reg   [31:0]      good_frames,
    output reg   [31:0]      bad_frames,

    // Conf vectors
    input        [79:0]      configuration_vector,

    // XGMII
    input        [63:0]      xgmii_d,
    input        [7:0]       xgmii_c,

    // AXIS
    input                    aresetn,
    output reg   [63:0]      tdata,
    output reg   [7:0]       tkeep,
    output reg               tvalid,
    output reg               tlast,
    output reg   [0:0]       tuser
    );

    `include "xgmii_includes.vh"
    // localparam
    localparam SRES     = 8'b00000001;
    localparam IDLE     = 8'b00000010;
    localparam ST_LANE0 = 8'b00000100;
    localparam ST_LANE4 = 8'b00001000;
    localparam FIN      = 8'b00010000;
    localparam D_LANE4  = 8'b00100000;
    localparam FINL4    = 8'b01000000;
    localparam s7       = 8'b10000000;

    //-------------------------------------------------------
    // Local output
    //-------------------------------------------------------
    reg                      synch;
    wire                     inv_aresetn;

    //-------------------------------------------------------
    // Local adapter
    //-------------------------------------------------------
    reg          [7:0]       fsm = 'b1;
    reg          [63:0]      tdata_i;
    reg          [7:0]       tkeep_i;
    reg          [7:0]       last_tkeep_i;
    reg                      tvalid_i;
    reg                      tlast_i;
    reg          [0:0]       tuser_i;
    reg          [63:0]      tdata_d0;
    reg                      tvalid_d0;
    wire         [63:0]      d;
    wire         [7:0]       c;
    reg          [63:0]      d_reg;
    reg          [7:0]       c_reg;
    reg                      inbound_frame;
    reg          [15:0]      len;
    reg          [31:0]      aux_dw;
    reg                      chk_tchar;

    //-------------------------------------------------------
    // Local CRC32
    //-------------------------------------------------------
    reg          [31:0]      crc_32;
    reg          [31:0]      crc_32_7B;
    reg          [31:0]      crc_32_6B;
    reg          [31:0]      crc_32_5B;
    reg          [31:0]      crc_32_4B;
    reg          [31:0]      crc_32_3B;
    reg          [31:0]      crc_32_2B;
    reg          [31:0]      crc_32_1B;
    reg          [31:0]      rcved_crc;
    reg          [31:0]      calcted_crc;

    //-------------------------------------------------------
    // assigns
    //-------------------------------------------------------
    assign d = xgmii_d;
    assign c = xgmii_c;
    assign inv_aresetn = ~aresetn;

    ////////////////////////////////////////////////
    // output
    ////////////////////////////////////////////////
    always @(posedge clk) begin
        
        if (inv_aresetn) begin  // aresetn
            tvalid <= 1'b0;
            synch <= 1'b0;
        end

        else begin  // not aresetn

            if (!inbound_frame || synch) begin
                synch <= 1'b1;
                tdata <= tdata_i;
                tkeep <= tkeep_i;
                tvalid <= tvalid_i;
                tlast <= tlast_i;
                tuser <= tuser_i;
            end
            else begin
                tvalid <= 1'b0;
            end

        end     // not aresetn
    end  //always

    ////////////////////////////////////////////////
    // adapter
    ////////////////////////////////////////////////
    always @(posedge clk) begin
        
        if (rst) begin  // rst
            tvalid_i <= 1'b0;
            tvalid_d0 <= 1'b0;
            fsm <= SRES;
        end

        else begin  // not rst

            if (tvalid && tlast && tuser[0]) begin
                good_frames <= good_frames + 1;
            end

            if (tvalid && tlast && ~tuser[0]) begin
                bad_frames <= bad_frames + 1;
            end

            tdata_i <= tdata_d0;
            tvalid_i <= tvalid_d0;

            case (fsm)

                SRES : begin
                    good_frames <= 'b0;
                    bad_frames <= 'b0;
                    fsm <= IDLE;
                end

                IDLE : begin
                    tvalid_d0 <= 1'b0;
                    tlast_i <= 1'b0;
                    tuser_i <= 'b0;
                    crc_32 <= CRC802_3_PRESET;
                    inbound_frame <= 1'b0;
                    d_reg <= d;
                    c_reg <= c;
                    len <= 0;
                    if (sof_lane0(d,c)) begin
                        inbound_frame <= 1'b1;
                        fsm <= ST_LANE0;
                    end
                    else if (sof_lane4(d,c)) begin
                        inbound_frame <= 1'b1;
                        fsm <= ST_LANE4;
                    end
                end

                ST_LANE0 : begin
                    tdata_d0 <= d;
                    tvalid_d0 <= 1'b1;
                    tkeep_i <= 8'hFF;
                    tlast_i <= 1'b0;
                    tuser_i <= 'b0;
                    d_reg <= d;
                    c_reg <= c;
                    crc_32 <= crc8B(crc_32,d);
                    crc_32_7B <= crc7B(crc_32,d[55:0]);
                    crc_32_6B <= crc6B(crc_32,d[47:0]);
                    crc_32_5B <= crc5B(crc_32,d[39:0]);
                    crc_32_4B <= crc4B(crc_32,d[31:0]);

                    case (c)
                        8'h0 : begin
                            len <= len + 8;
                        end
                        8'hFF : begin
                            len <= len;
                            tkeep_i <= 8'h0F;
                            tvalid_d0 <= 1'b0;
                            tlast_i <= 1'b1;
                            if ((~crc_rev(crc_32_4B) == d_reg[63:32]) && is_tchar(d[7:0])) begin
                                tuser_i[0] <= 1'b1;
                            end
                            fsm <= IDLE;
                        end
                        8'hFE : begin
                            len <= len + 1;
                            tkeep_i <= 8'h1F;
                            tvalid_d0 <= 1'b0;
                            tlast_i <= 1'b1;
                            if ((~crc_rev(crc_32_5B) == {d[7:0], d_reg[63:40]}) && is_tchar(d[15:8])) begin
                                tuser_i[0] <= 1'b1;
                            end
                            fsm <= IDLE;
                        end
                        8'hFC : begin
                            len <= len + 2;
                            tkeep_i <= 8'h3F;
                            tvalid_d0 <= 1'b0;
                            tlast_i <= 1'b1;
                            if ((~crc_rev(crc_32_6B) == {d[15:0], d_reg[63:48]}) && is_tchar(d[23:16])) begin
                                tuser_i[0] <= 1'b1;
                            end
                            fsm <= IDLE;
                        end
                        8'hF8 : begin
                            len <= len + 3;
                            tkeep_i <= 8'h7F;
                            tvalid_d0 <= 1'b0;
                            tlast_i <= 1'b1;
                            if ((~crc_rev(crc_32_7B) == {d[23:0], d_reg[63:56]}) && is_tchar(d[31:24])) begin
                                tuser_i[0] <= 1'b1;
                            end
                            fsm <= IDLE;
                        end
                        8'hF0 : begin
                            len <= len + 4;
                            tvalid_d0 <= 1'b0;
                            tlast_i <= 1'b1;
                            if ((~crc_rev(crc_32) == d[31:0]) && is_tchar(d[39:32])) begin
                                tuser_i[0] <= 1'b1;
                            end
                            fsm <= IDLE;
                        end
                        8'hE0 : begin
                            len <= len + 5;
                            last_tkeep_i <= 8'h01;
                            rcved_crc <= d[39:8];
                            calcted_crc <= crc1B(crc_32,d[7:0]);
                            chk_tchar <= is_tchar(d[47:40]);
                            fsm <= FIN;
                        end
                        8'hC0 : begin
                            len <= len + 6;
                            last_tkeep_i <= 8'h03;
                            rcved_crc <= d[47:16];
                            calcted_crc <= crc2B(crc_32,d[15:0]);
                            chk_tchar <= is_tchar(d[55:48]);
                            fsm <= FIN;
                        end
                        8'h80 : begin
                            len <= len + 7;
                            last_tkeep_i <= 8'h07;
                            rcved_crc <= d[55:24];
                            calcted_crc <= crc3B(crc_32,d[23:0]);
                            chk_tchar <= is_tchar(d[63:56]);
                            fsm <= FIN;
                        end
                        default : begin
                            tlast_i <= 1'b1;
                            tvalid_d0 <= 1'b0;
                            tvalid_i <= 1'b1;
                            fsm <= IDLE;
                        end
                    endcase
                end

                FIN : begin
                    tkeep_i <= last_tkeep_i;
                    tlast_i <= 1'b1;
                    tvalid_d0 <= 1'b0;
                    crc_32 <= CRC802_3_PRESET;
                    if ((~crc_rev(calcted_crc) == rcved_crc) && chk_tchar) begin
                        tuser_i[0] <= 1'b1;
                    end
                    if (sof_lane4(d,c)) begin
                        fsm <= ST_LANE4;
                    end
                    else begin
                        fsm <= IDLE;
                    end
                end

                ST_LANE4 : begin
                    len <= 4;
                    tlast_i <= 1'b0;
                    tuser_i <= 'b0;
                    crc_32 <= crc4B(crc_32,d[63:32]);
                    aux_dw <= d[63:32];
                    if (!c) begin
                        fsm <= D_LANE4;
                    end
                    else begin
                        fsm <= IDLE;
                    end
                end

                D_LANE4 : begin
                    tdata_d0 <= {d[31:0], aux_dw};
                    tvalid_d0 <= 1'b1;
                    tkeep_i <= 8'hFF;
                    aux_dw <= d[63:32];
                    d_reg <= d;
                    c_reg <= c;
                    crc_32 <= crc8B(crc_32,d);
                    crc_32_4B <= crc4B(crc_32,d[31:0]);
                    crc_32_5B <= crc5B(crc_32,d[39:0]);
                    crc_32_6B <= crc6B(crc_32,d[47:0]);
                    crc_32_7B <= crc7B(crc_32,d[55:0]);

                    case (c)
                        8'h0 : begin
                            len <= len + 8;
                        end
                        8'hFF : begin
                            len <= len;
                            tvalid_d0 <= 1'b0;
                            tlast_i <= 1'b1;
                            if ((~crc_rev(crc_32_4B) == d_reg[63:32]) && is_tchar(d[7:0])) begin
                                tuser_i[0] <= 1'b1;
                            end
                            fsm <= IDLE;
                        end
                        8'hFE : begin
                            len <= len + 1;
                            last_tkeep_i <= 8'h01;
                            rcved_crc <= {d[7:0], aux_dw[31:8]};
                            calcted_crc <= crc_32_5B;
                            chk_tchar <= is_tchar(d[15:8]);
                            fsm <= FINL4;
                        end
                        8'hFC : begin
                            len <= len + 2;
                            last_tkeep_i <= 8'h03;
                            rcved_crc <= {d[15:0], aux_dw[31:16]};
                            calcted_crc <= crc_32_6B;
                            chk_tchar <= is_tchar(d[23:16]);
                            fsm <= FINL4;
                        end
                        8'hF8 : begin
                            len <= len + 3;
                            last_tkeep_i <= 8'h07;
                            rcved_crc <= {d[23:0], aux_dw[31:24]};
                            calcted_crc <= crc_32_7B;
                            chk_tchar <= is_tchar(d[31:24]);
                            fsm <= FINL4;
                        end
                        8'hF0 : begin
                            len <= len + 4;
                            last_tkeep_i <= 8'h0F;
                            rcved_crc <= d[31:0];
                            calcted_crc <= crc_32;
                            chk_tchar <= is_tchar(d[39:32]);
                            fsm <= FIN;
                        end
                        8'hE0 : begin
                            len <= len + 5;
                            last_tkeep_i <= 8'h1F;
                            rcved_crc <= d[39:8];
                            calcted_crc <= crc1B(crc_32,d[7:0]);
                            chk_tchar <= is_tchar(d[47:40]);
                            fsm <= FIN;
                        end
                        8'hC0 : begin
                            len <= len + 6;
                            last_tkeep_i <= 8'h3F;
                            rcved_crc <= d[47:16];
                            calcted_crc <= crc2B(crc_32,d[15:0]);
                            chk_tchar <= is_tchar(d[55:48]);
                            fsm <= FIN;
                        end
                        8'h80 : begin
                            len <= len + 7;
                            last_tkeep_i <= 8'h7F;
                            rcved_crc <= d[55:24];
                            calcted_crc <= crc3B(crc_32,d[23:0]);
                            chk_tchar <= is_tchar(d[63:56]);
                            fsm <= FIN;
                        end
                        default : begin
                            tlast_i <= 1'b1;
                            tvalid_d0 <= 1'b0;
                            tvalid_i <= 1'b1;
                            fsm <= IDLE;
                        end
                    endcase
                end

                FINL4 : begin
                    len <= 0;
                    tkeep_i <= last_tkeep_i;
                    tlast_i <= 1'b1;
                    tvalid_d0 <= 1'b0;
                    crc_32 <= CRC802_3_PRESET;
                    if ((~crc_rev(calcted_crc) == rcved_crc) && chk_tchar) begin
                        tuser_i[0] <= 1'b1;
                    end
                    if (sof_lane0(d,c)) begin
                        fsm <= ST_LANE0;
                    end
                    else if (sof_lane4(d,c)) begin
                        fsm <= ST_LANE4;
                    end
                    else begin
                        fsm <= IDLE;
                    end
                end

                default : begin
                    fsm <= IDLE;
                end

            endcase
        end     // not rst
    end  //always

endmodule // xgmii2axis

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////