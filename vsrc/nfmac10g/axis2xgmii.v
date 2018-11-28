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

module axis2xgmii (

    // Clks and resets
    input                    clk,
    input                    rst,

    // Stats
    output reg   [31:0]      good_frames,
    output reg   [31:0]      bad_frames,

    // Conf vectors
    input        [79:0]      configuration_vector,

    // internal
    output reg               lane4_start,
    output       [1:0]       dic_o,

    // XGMII
    output       [63:0]      xgmii_d,
    output       [7:0]       xgmii_c,

    // AXIS
    input        [63:0]      tdata,
    input        [7:0]       tkeep,
    input                    tvalid,
    output reg               tready,
    input                    tlast,
    input        [0:0]       tuser
    );

    `include "xgmii_includes.vh"
    // localparam
    localparam SRES            = 24'b000000000000000000000001;
    localparam IDLE_L0         = 24'b000000000000000000000010;
    localparam ST_LANE0        = 24'b000000000000000000000100;
    localparam QW_IDLE         = 24'b000000000000000000001000;
    localparam L0_FIN_8B       = 24'b000000000000000000100000;
    localparam T_LANE4         = 24'b000000000000000001000000;
    localparam L0_FIN_7B_6B_5B = 24'b000000000000000010000000;
    localparam T_LANE3         = 24'b000000000000000100000000;
    localparam DW_IDLE         = 24'b000000000000001000000000;
    localparam T_LANE2         = 24'b000000000000010000000000;
    localparam T_LANE1         = 24'b000000000000100000000000;
    localparam L0_FIN_4B       = 24'b000000000001000000000000;
    localparam T_LANE0         = 24'b000000000010000000000000;
    localparam L0_FIN_3B_2B_1B = 24'b000000000100000000000000;
    localparam T_LANE7         = 24'b000000001000000000000000;
    localparam T_LANE6         = 24'b000000010000000000000000;
    localparam T_LANE5         = 24'b000000100000000000000000;
    localparam ST_LANE4        = 24'b000001000000000000000000;
    localparam ST_LANE4_D      = 24'b000010000000000000000000;
    localparam L4_FIN_8B       = 24'b000100000000000000000000;
    localparam L4_FIN_7B_6B_5B = 24'b001000000000000000000000;
    localparam L4_FIN_4B       = 24'b010000000000000000000000;
    localparam L4_FIN_3B_2B_1B = 24'b100000000000000000000000;

    //-------------------------------------------------------
    // Local adapter
    //-------------------------------------------------------
    reg          [23:0]      fsm = 'b1;
    reg          [63:0]      tdata_i;
    reg          [7:0]       tkeep_i;
    reg          [63:0]      d;
    reg          [7:0]       c;
    reg          [31:0]      aux_dw;
    reg          [1:0]       dic;

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
    reg          [31:0]      aux_var_crc;
    reg          [31:0]      calcted_crc4B;
    reg          [31:0]      crc_reg;

    //-------------------------------------------------------
    // assigns
    //-------------------------------------------------------
    assign xgmii_d = d;
    assign xgmii_c = c;
    assign dic_o = dic;

    ////////////////////////////////////////////////
    // adapter
    ////////////////////////////////////////////////
    always @(posedge clk) begin

        if (rst) begin  // rst
            d <= QW_IDLE_D;
            c <= QW_IDLE_C;
            tready <= 1'b0;
            fsm <= SRES;
        end

        else begin  // not rst

            case (fsm)

                SRES : begin
                    dic <= 'b0;
                    tready <= 1'b1;
                    fsm <= IDLE_L0;
                end

                IDLE_L0 : begin
                    d <= QW_IDLE_D;
                    c <= QW_IDLE_C;
                    tdata_i <= tdata;
                    tkeep_i <= tkeep;
                    lane4_start <= 1'b0;
                    if (tvalid) begin
                        crc_32 <= crc8B(CRC802_3_PRESET,tdata);
                        d <= PREAMBLE_LANE0_D;
                        c <= PREAMBLE_LANE0_C;
                        fsm <= ST_LANE0;
                    end
                    else begin
                        if (dic) begin
                            dic <= dic - 1;
                        end
                    end
                end

                ST_LANE0 : begin
                    tready <= 1'b0;
                    tdata_i <= tdata;
                    tkeep_i <= tkeep;
                    d <= tdata_i;
                    c <= 8'b0;
                    crc_32 <= crc8B(crc_32,tdata);
                    crc_32_7B <= crc7B(crc_32,tdata[55:0]);
                    crc_32_6B <= crc6B(crc_32,tdata[47:0]);
                    crc_32_5B <= crc5B(crc_32,tdata[39:0]);
                    crc_32_4B <= crc4B(crc_32,tdata[31:0]);
                    crc_32_3B <= crc3B(crc_32,tdata[23:0]);
                    crc_32_2B <= crc2B(crc_32,tdata[15:0]);
                    crc_32_1B <= crc1B(crc_32,tdata[7:0]);

                    casex ({tuser[0], tlast, tkeep[7:3]})
                        {2'b1x, 5'hxx} : begin
                            d[7:0] <= XGMII_ERROR_L0_D;
                            d[63:56] <= T;
                            c <= XGMII_ERROR_L0_C;
                            c[7] <= 1'b1;
                            fsm <= QW_IDLE;
                        end
                        {2'b00, 5'hxx} : begin
                            tready <= 1'b1;
                        end
                        {2'b01, 5'b1xxxx} : begin
                            fsm <= L0_FIN_8B;
                        end
                        {2'b01, 5'b0xx1x} : begin
                            fsm <= L0_FIN_7B_6B_5B;
                        end
                        {2'b01, 5'bxxx01} : begin
                            fsm <= L0_FIN_4B;
                        end
                        {2'b01, 5'bxxxx0} : begin
                            fsm <= L0_FIN_3B_2B_1B;
                        end
                    endcase
                end

                QW_IDLE : begin
                    d <= QW_IDLE_D;
                    c <= QW_IDLE_C;
                    tready <= 1'b1;
                    fsm <= IDLE_L0;
                end

                L0_FIN_8B : begin
                    d <= tdata_i;
                    c <= 8'h00;
                    calcted_crc4B <= ~crc_rev(crc_32);
                    fsm <= T_LANE4;
                end

                T_LANE4 : begin
                    d <= {{3{I}}, T, calcted_crc4B};
                    c <= 8'hF0;
                    fsm <= QW_IDLE;
                end

                L0_FIN_7B_6B_5B : begin
                    casex (tkeep_i[6:4])
                        3'b1xx : begin
                            aux_var_crc = ~crc_rev(crc_32_7B);
                            d <= {aux_var_crc[7:0], tdata_i[55:0]};
                            fsm <= T_LANE3;
                        end
                        3'b01x : begin
                            aux_var_crc = ~crc_rev(crc_32_6B);
                            d <= {aux_var_crc[15:0], tdata_i[47:0]};
                            fsm <= T_LANE2;
                        end
                        3'b001 : begin
                            aux_var_crc = ~crc_rev(crc_32_5B);
                            d <= {aux_var_crc[23:0], tdata_i[39:0]};
                            fsm <= T_LANE1;
                        end
                    endcase
                    c <= 8'b0;
                    crc_reg <= aux_var_crc;
                end

                T_LANE3 : begin
                    d <= {{4{I}}, T, crc_reg[31:8]};
                    c <= 8'hF8;
                    if (!dic) begin
                        dic <= 'h3;
                        tready <= 1'b1;
                        fsm <= DW_IDLE;
                    end
                    else begin
                        dic <= dic - 1;
                        fsm <= QW_IDLE;
                    end
                end

                DW_IDLE : begin
                    d <= QW_IDLE_D;
                    c <= QW_IDLE_C;
                    tdata_i <= tdata;
                    tkeep_i <= tkeep;
                    if (tvalid) begin
                        crc_32 <= crc8B(CRC802_3_PRESET,tdata);
                        d <= PREAMBLE_LANE4_D;
                        c <= PREAMBLE_LANE4_C;
                        fsm <= ST_LANE4;
                    end
                    else begin
                        fsm <= IDLE_L0;
                    end
                end

                T_LANE2 : begin
                    d <= {{5{I}}, T, crc_reg[31:16]};
                    c <= 8'hFC;
                    if (dic < 2) begin
                        dic <= dic + 2;
                        tready <= 1'b1;
                        fsm <= DW_IDLE;
                    end
                    else begin
                        dic <= dic - 2;
                        fsm <= QW_IDLE;
                    end
                end

                T_LANE1 : begin
                    d <= {{6{I}}, T, crc_reg[31:24]};
                    c <= 8'hFE;
                    if (dic < 3) begin
                        dic <= dic + 1;
                        tready <= 1'b1;
                        fsm <= DW_IDLE;
                    end
                    else begin
                        dic <= 'b0;
                        fsm <= QW_IDLE;
                    end
                end

                L0_FIN_4B : begin
                    d <= {~crc_rev(crc_32_4B), tdata_i[31:0]};
                    c <= 8'b0;
                    fsm <= T_LANE0;
                end

                T_LANE0 : begin
                    d <= {{7{I}}, T};
                    c <= 8'hFF;
                    tready <= 1'b1;
                    fsm <= DW_IDLE;
                end

                L0_FIN_3B_2B_1B : begin
                    casex (tkeep_i[2:0])
                        3'b1xx : begin
                            d <= {T, ~crc_rev(crc_32_3B), tdata_i[23:0]};
                            c <= 8'h80;
                            fsm <= T_LANE7;
                        end
                        3'b01x : begin
                            d <= {I, T, ~crc_rev(crc_32_2B), tdata_i[15:0]};
                            c <= 8'hC0;
                            fsm <= T_LANE6;
                        end
                        3'b001 : begin
                            d <= {{2{I}}, T, ~crc_rev(crc_32_1B), tdata_i[7:0]};
                            c <= 8'hE0;
                            fsm <= T_LANE5;
                        end
                    endcase
                end

                T_LANE7 : begin
                    d <= QW_IDLE_D;
                    c <= QW_IDLE_C;
                    tready <= 1'b1;
                    if (!dic) begin
                        dic <= 'h3;
                        fsm <= IDLE_L0;
                    end
                    else begin
                        dic <= dic - 1;
                        fsm <= DW_IDLE;
                    end
                end

                T_LANE6 : begin
                    d <= QW_IDLE_D;
                    c <= QW_IDLE_C;
                    tready <= 1'b1;
                    if (dic < 2) begin
                        dic <= dic + 2;
                        fsm <= IDLE_L0;
                    end
                    else begin
                        dic <= dic - 2;
                        fsm <= DW_IDLE;
                    end
                end

                T_LANE5 : begin
                    d <= QW_IDLE_D;
                    c <= QW_IDLE_C;
                    tready <= 1'b1;
                    if (dic < 3) begin
                        dic <= dic + 1;
                        fsm <= IDLE_L0;
                    end
                    else begin
                        dic <= 'b0;
                        fsm <= DW_IDLE;
                    end
                end

                ST_LANE4 : begin
                    tdata_i <= tdata;
                    tkeep_i <= tkeep;
                    aux_dw <= tdata_i[63:32];
                    d <= {tdata_i[31:0], PREAMBLE_LANE4_END_D};
                    c <= PREAMBLE_LANE4_END_C;
                    crc_32 <= crc8B(crc_32,tdata);
                    lane4_start <= 1'b1;
                    if (tuser[0]) begin
                        d[7:0] <= XGMII_ERROR_L0_D;
                        d[63:56] <= T;
                        c <= XGMII_ERROR_L0_C;
                        c[7] <= 1'b1;
                        tready <= 1'b0;
                        fsm <= QW_IDLE;
                    end
                    else begin
                        fsm <= ST_LANE4_D;
                    end
                end

                ST_LANE4_D : begin
                    tready <= 1'b0;
                    tdata_i <= tdata;
                    tkeep_i <= tkeep;
                    aux_dw <= tdata_i[63:32];
                    d <= {tdata_i[31:0], aux_dw};
                    c <= 8'b0;
                    crc_32 <= crc8B(crc_32,tdata);
                    crc_32_7B <= crc7B(crc_32,tdata[55:0]);
                    crc_32_6B <= crc6B(crc_32,tdata[47:0]);
                    crc_32_5B <= crc5B(crc_32,tdata[39:0]);
                    crc_32_4B <= crc4B(crc_32,tdata[31:0]);
                    crc_32_3B <= crc3B(crc_32,tdata[23:0]);
                    crc_32_2B <= crc2B(crc_32,tdata[15:0]);
                    crc_32_1B <= crc1B(crc_32,tdata[7:0]);

                    casex ({tuser[0], tlast, tkeep[7:3]})
                        {2'b1x, 5'hxx} : begin
                            d[39:32] <= XGMII_ERROR_L4_D;
                            c <= XGMII_ERROR_L4_C;
                            fsm <= QW_IDLE;
                        end
                        {2'b00, 5'hxx} : begin
                            tready <= 1'b1;
                        end
                        {2'b01, 5'b1xxxx} : begin
                            fsm <= L4_FIN_8B;
                        end
                        {2'b01, 5'b0xx1x} : begin
                            fsm <= L4_FIN_7B_6B_5B;
                        end
                        {2'b01, 5'bxxx01} : begin
                            fsm <= L4_FIN_4B;
                        end
                        {2'b01, 5'bxxxx0} : begin
                            fsm <= L4_FIN_3B_2B_1B;
                        end
                    endcase
                end

                L4_FIN_8B : begin
                    d <= {tdata_i[31:0], aux_dw};
                    c <= 8'b0;
                    tdata_i[31:0] <= tdata_i[63:32];
                    crc_32_4B <= crc_32;
                    fsm <= L0_FIN_4B;
                end

                L4_FIN_7B_6B_5B : begin
                    c <= 8'b0;
                    crc_32_1B <= crc_32_5B;
                    crc_32_2B <= crc_32_6B;
                    crc_32_3B <= crc_32_7B;
                    tdata_i[31:0] <= tdata_i[63:32];
                    tkeep_i[2:0] <= tkeep_i[6:4];
                    d <= {tdata_i[31:0], aux_dw};
                    fsm <= L0_FIN_3B_2B_1B;
                end

                L4_FIN_4B : begin
                    d <= {tdata_i[31:0], aux_dw};
                    c <= 8'b0;
                    calcted_crc4B <= ~crc_rev(crc_32_4B);
                    fsm <= T_LANE4;
                end

                L4_FIN_3B_2B_1B : begin
                    casex (tkeep_i[2:0])
                        3'b1xx : begin
                            aux_var_crc = ~crc_rev(crc_32_3B);
                            d <= {aux_var_crc[7:0], tdata_i[23:0], aux_dw};
                            fsm <= T_LANE3;
                        end
                        3'b01x : begin
                            aux_var_crc = ~crc_rev(crc_32_2B);
                            d <= {aux_var_crc[15:0], tdata_i[15:0], aux_dw};
                            fsm <= T_LANE2;
                        end
                        3'b001 : begin
                            aux_var_crc = ~crc_rev(crc_32_1B);
                            d <= {aux_var_crc[23:0], tdata_i[7:0], aux_dw};
                            fsm <= T_LANE1;
                        end
                    endcase
                    c <= 8'b0;
                    crc_reg <= aux_var_crc;
                end

                default : begin
                    fsm <= SRES;
                end

            endcase
        end     // not rst
    end  //always

endmodule // axis2xgmii

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////