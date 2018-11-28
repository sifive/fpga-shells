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

module padding_ctrl (

    // Clks and resets
    input                    clk,
    input                    rst,

    // AXIS In
    input                    aresetn,
    input        [63:0]      s_axis_tdata,
    input        [7:0]       s_axis_tkeep,
    input                    s_axis_tvalid,
    output reg               s_axis_tready,
    input                    s_axis_tlast,
    input        [0:0]       s_axis_tuser,

    // AXIS Out
    output reg   [63:0]      m_axis_tdata,
    output reg   [7:0]       m_axis_tkeep,
    output reg               m_axis_tvalid,
    input                    m_axis_tready,
    output reg               m_axis_tlast,
    output reg   [0:0]       m_axis_tuser,

    // internal
    input                    lane4_start,
    input        [1:0]       dic
    );

    `include "xgmii_includes.vh"
    // localparam
    localparam SRES           = 8'b00000001;
    localparam IDLE           = 8'b00000010;
    localparam ST             = 8'b00000100;
    localparam PAD_CHK        = 8'b00001000;
    localparam W3             = 8'b00010000;
    localparam W2             = 8'b00100000;
    localparam ERR_W_LAST     = 8'b01000000;
    localparam s7             = 8'b10000000;

    //-------------------------------------------------------
    // Local
    //-------------------------------------------------------
    wire                     inv_aresetn;

    //-------------------------------------------------------
    // Local adapter
    //-------------------------------------------------------
    reg          [7:0]       fsm = 'b1;
    reg          [4:0]       trn;
    reg          [63:0]      m_axis_tdata_d0;
    reg                      m_axis_tvalid_d0;
    reg          [7:0]       last_tkeep;

    //-------------------------------------------------------
    // assigns
    //-------------------------------------------------------
    assign inv_aresetn = ~aresetn;

    ////////////////////////////////////////////////
    // adapter
    ////////////////////////////////////////////////
    always @(posedge clk) begin
        
        if (inv_aresetn) begin  // rst
            s_axis_tready <= 1'b0;
            m_axis_tvalid <= 1'b0;
            fsm <= SRES;
        end

        else begin  // not rst

            m_axis_tdata <= m_axis_tdata_d0;
            m_axis_tvalid <= m_axis_tvalid_d0;
            m_axis_tlast <= 1'b0;
            m_axis_tuser[0] <= 1'b0;

            case (fsm)

                SRES : begin
                    m_axis_tuser <= 'b0;
                    if (m_axis_tready) begin
                        s_axis_tready <= 1'b1;
                        fsm <= IDLE;
                    end
                end

                IDLE : begin
                    m_axis_tdata_d0 <= s_axis_tdata;
                    m_axis_tkeep <= 8'hFF;
                    trn <= 1;
                    if (s_axis_tvalid) begin
                        m_axis_tvalid_d0 <= 1'b1;
                        fsm <= ST;
                    end
                end

                ST : begin
                    m_axis_tdata_d0 <= s_axis_tdata;
                    s_axis_tready <= 1'b0;
                    if (!trn[4]) begin
                        trn[3:0] <= trn[3:0] + 1;
                    end
                    if (trn[3]) begin
                        trn[4] <= 1'b1;
                    end
                    fsm <= PAD_CHK;

                    casex ({s_axis_tvalid, s_axis_tlast, s_axis_tuser[0], s_axis_tkeep})
                        {3'b0xx, 8'hxx} : begin
                            m_axis_tuser[0] <= 1'b1;
                            m_axis_tvalid_d0 <= 1'b0;
                            fsm <= W2;
                        end
                        {3'b101, 8'hxx} : begin
                            m_axis_tuser[0] <= 1'b1;
                            m_axis_tvalid_d0 <= 1'b0;
                            s_axis_tready <= 1'b1;
                            fsm <= ERR_W_LAST;
                        end
                        {3'b111, 8'hxx} : begin
                            m_axis_tuser[0] <= 1'b1;
                            m_axis_tvalid_d0 <= 1'b0;
                            fsm <= W2;
                        end
                        {3'b100, 8'bxxxxxxxx} : begin
                            s_axis_tready <= 1'b1;
                            fsm <= ST;
                        end
                        {3'b110, 8'b1xxxxxxx} : begin
                            last_tkeep <= 8'hFF;
                        end
                        {3'b110, 8'b01xxxxxx} : begin
                            m_axis_tdata_d0 <= {8'b0, s_axis_tdata[55:0]};
                            last_tkeep <= 8'h7F;
                        end
                        {3'b110, 8'bx01xxxxx} : begin
                            m_axis_tdata_d0 <= {16'b0, s_axis_tdata[47:0]};
                            last_tkeep <= 8'h3F;
                        end
                        {3'b110, 8'bxx01xxxx} : begin
                            m_axis_tdata_d0 <= {24'b0, s_axis_tdata[39:0]};
                            last_tkeep <= 8'h1F;
                        end
                        {3'b110, 8'bxxx01xxx} : begin
                            m_axis_tdata_d0 <= {32'b0, s_axis_tdata[31:0]};
                            last_tkeep <= 8'h0F;
                        end
                        {3'b110, 8'bxxxx01xx} : begin
                            m_axis_tdata_d0 <= {40'b0, s_axis_tdata[23:0]};
                            last_tkeep <= 8'h07;
                        end
                        {3'b110, 8'bxxxxx01x} : begin
                            m_axis_tdata_d0 <= {48'b0, s_axis_tdata[15:0]};
                            last_tkeep <= 8'h03;
                        end
                        {3'b110, 8'bxxxxxx01} : begin
                            m_axis_tdata_d0 <= {56'b0, s_axis_tdata[7:0]};
                            last_tkeep <= 8'h01;
                        end
                    endcase
                end

                PAD_CHK : begin
                    m_axis_tdata_d0 <= 'b0;
                    last_tkeep <= 8'h0F;
                    trn <= trn + 1;
                    if (trn >= 8) begin
                        m_axis_tvalid_d0 <= 1'b0;
                        m_axis_tlast <= 1'b1;
                        m_axis_tkeep <= last_tkeep;
                        casex ({lane4_start, dic, last_tkeep, trn[4]})
                            // L0
                            {1'b0, 2'b00, 8'b01xxxxxx, 1'bx} : begin    // 7f
                                fsm <= W2;
                            end
                            {1'b0, 2'b00, 8'bx01xxxxx, 1'bx} : begin    // 3f
                                fsm <= W2;
                            end
                            {1'b0, 2'b01, 8'bx01xxxxx, 1'bx} : begin    // 3f
                                fsm <= W2;
                            end
                            {1'b0, 2'b00, 8'bxx01xxxx, 1'bx} : begin    // 1f
                                fsm <= W2;
                            end
                            {1'b0, 2'b01, 8'bxx01xxxx, 1'bx} : begin    // 1f
                                fsm <= W2;
                            end
                            {1'b0, 2'b10, 8'bxx01xxxx, 1'bx} : begin    // 1f
                                fsm <= W2;
                            end
                            {1'b0, 2'bxx, 8'bxxx0xxxx, 1'b1} : begin    // 0f, 07, 03, 01
                                fsm <= W2;
                            end

                            // L4
                            {1'b1, 2'b00, 8'bxxxx01xx, 1'b1} : begin    // 07
                                fsm <= W2;
                            end
                            {1'b1, 2'b00, 8'bxxxxx01x, 1'b1} : begin    // 03
                                fsm <= W2;
                            end
                            {1'b1, 2'b01, 8'bxxxxx01x, 1'b1} : begin    // 03
                                fsm <= W2;
                            end
                            {1'b1, 2'b00, 8'bxxxxxx01, 1'b1} : begin    // 01
                                fsm <= W2;
                            end
                            {1'b1, 2'b01, 8'bxxxxxx01, 1'b1} : begin    // 01
                                fsm <= W2;
                            end
                            {1'b1, 2'b10, 8'bxxxxxx01, 1'b1} : begin    // 01
                                fsm <= W2;
                            end

                            // 8-trn
                            {1'b0, 2'bxx, 8'bxxx0xxxx, 1'b0} : begin    // 0f, 07, 03, 01
                                m_axis_tkeep <= 8'h0F;
                                fsm <= W2;
                            end
                            {1'bx, 2'bxx, 8'bxxxx0xxx, 1'b0} : begin    // 07, 03, 01
                                m_axis_tkeep <= 8'h0F;
                                fsm <= W3;
                            end
                            default : begin
                                fsm <= W3;
                            end
                        endcase
                    end
                end

                W3 : fsm <= W2;

                W2 : begin
                    s_axis_tready <= 1'b1;
                    fsm <= IDLE;
                end

                ERR_W_LAST : begin
                    if (!s_axis_tvalid || s_axis_tlast) begin
                        s_axis_tready <= 1'b0;
                        fsm <= W2;
                    end
                end

                default : begin
                    fsm <= SRES;
                end

            endcase
        end     // not rst
    end  //always

endmodule // padding_ctrl

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////