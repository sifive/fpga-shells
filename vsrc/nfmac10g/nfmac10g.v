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

module nfmac10g # (
    parameter C_TX_SUBSYS_EN = 1,
    parameter C_RX_SUBSYS_EN = 1
    ) (

    // Clks and resets
    input                    tx_clk0,
    input                    rx_clk0,
    input                    reset,
    input                    tx_dcm_locked,
    input                    rx_dcm_locked,

    // Flow control
    input        [7:0]       tx_ifg_delay,
    input        [15:0]      pause_val,
    input                    pause_req,

    // Conf and status vectors
    input        [79:0]      tx_configuration_vector,
    input        [79:0]      rx_configuration_vector,
    output       [1:0]       status_vector,

    // Statistic Vector Signals
    output       [25:0]      tx_statistics_vector,
    output                   tx_statistics_valid,
    output       [29:0]      rx_statistics_vector,
    output                   rx_statistics_valid,

    // XGMII
    output       [63:0]      xgmii_txd,
    output       [7:0]       xgmii_txc,
    input        [63:0]      xgmii_rxd,
    input        [7:0]       xgmii_rxc,

    // Tx AXIS
    input                    tx_axis_aresetn,
    input        [63:0]      tx_axis_tdata,
    input        [7:0]       tx_axis_tkeep,
    input                    tx_axis_tvalid,
    output                   tx_axis_tready,
    input                    tx_axis_tlast,
    input        [0:0]       tx_axis_tuser,

    // Rx AXIS
    input                    rx_axis_aresetn,
    output       [63:0]      rx_axis_tdata,
    output       [7:0]       rx_axis_tkeep,
    output                   rx_axis_tvalid,
    output                   rx_axis_tlast,
    output       [0:0]       rx_axis_tuser
    );

    //-------------------------------------------------------
    // Local clk
    //-------------------------------------------------------
    wire                     tx_clk;
    wire                     rx_clk;
    wire                     tx_rst;
    wire                     rx_rst;

    //-------------------------------------------------------
    // Local Rx
    //-------------------------------------------------------
    wire         [31:0]      rx_good_frames;
    wire         [31:0]      rx_bad_frames;

    //-------------------------------------------------------
    // tx_rst_mod
    //-------------------------------------------------------
    rst_mod tx_rst_mod (
        .clk(tx_clk),                                          // I
        .reset(reset),                                         // I
        .dcm_locked(tx_dcm_locked),                            // I
        .rst(tx_rst)                                           // O
        );

    //-------------------------------------------------------
    // rx_rst_mod
    //-------------------------------------------------------
    rst_mod rx_rst_mod (
        .clk(rx_clk),                                          // I
        .reset(reset),                                         // I
        .dcm_locked(rx_dcm_locked),                            // I
        .rst(rx_rst)                                           // O
        );

    //-------------------------------------------------------
    // assigns
    //-------------------------------------------------------
    assign tx_clk = tx_clk0;
    assign rx_clk = rx_clk0;
    assign status_vector = 'b0;
    assign tx_statistics_vector = 'b0;
    assign tx_statistics_valid = 'b0;
    assign rx_statistics_vector = 'b0;
    assign rx_statistics_valid = 'b0;

    //-------------------------------------------------------
    // Tx
    //-------------------------------------------------------
    generate if (C_TX_SUBSYS_EN == 1) begin
    tx tx_mod (
        .clk(tx_clk),                                          // I
        .rst(tx_rst),                                          // I
        // Conf vectors
        .configuration_vector(tx_configuration_vector),        // I [79:0]
        // XGMII
        .xgmii_txd(xgmii_txd),                                 // I [63:0]
        .xgmii_txc(xgmii_txc),                                 // I [7:0]
        // AXIS
        .axis_aresetn(tx_axis_aresetn),                        // I
        .axis_tdata(tx_axis_tdata),                            // I [63:0]
        .axis_tkeep(tx_axis_tkeep),                            // I [7:0]
        .axis_tvalid(tx_axis_tvalid),                          // I
        .axis_tready(tx_axis_tready),                          // O
        .axis_tlast(tx_axis_tlast),                            // I
        .axis_tuser(tx_axis_tuser)                             // I [0:0]
        );
    end
    else begin // C_TX_SUBSYS_EN
        assign xgmii_txd = 64'h0707070707070707;
        assign xgmii_txc = 8'hFF;
        assign tx_axis_tready = 1'b0;
    end endgenerate

    //-------------------------------------------------------
    // Rx
    //-------------------------------------------------------
    generate if (C_RX_SUBSYS_EN == 1) begin
    rx rx_mod (
        .clk(rx_clk),                                          // I
        .rst(rx_rst),                                          // I
        // Stats
        .good_frames(rx_good_frames),                          // O [31:0]
        .bad_frames(rx_bad_frames),                            // O [31:0]
        // Conf vectors
        .configuration_vector(rx_configuration_vector),        // I [79:0]
        // XGMII
        .xgmii_rxd(xgmii_rxd),                                 // I [63:0]
        .xgmii_rxc(xgmii_rxc),                                 // I [7:0]
        // AXIS
        .axis_aresetn(rx_axis_aresetn),                        // I
        .axis_tdata(rx_axis_tdata),                            // O [63:0]
        .axis_tkeep(rx_axis_tkeep),                            // O [7:0]
        .axis_tvalid(rx_axis_tvalid),                          // O
        .axis_tlast(rx_axis_tlast),                            // O
        .axis_tuser(rx_axis_tuser)                             // O [0:0]
        );
    end
    else begin // C_RX_SUBSYS_EN
        assign rx_good_frames = 'b0;
        assign rx_bad_frames = 'b0;
        assign rx_axis_tdata = 'b0;
        assign rx_axis_tkeep = 'b0;
        assign rx_axis_tvalid = 1'b0;
        assign rx_axis_tlast = 1'b0;
        assign rx_axis_tuser = 'b0;
    end endgenerate

endmodule // nfmac10g

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////