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

module rst_mod (

    // Clks and resets
    input                    clk,
    input                    reset,
    input                    dcm_locked,

    // Output
    output reg               rst
    );

    // localparam
    localparam s0  = 8'b00000001;
    localparam s1  = 8'b00000010;
    localparam s2  = 8'b00000100;
    localparam s3  = 8'b00001000;
    localparam s4  = 8'b00010000;
    localparam s5  = 8'b00100000;
    localparam s6  = 8'b01000000;
    localparam s7  = 8'b10000000;

    //-------------------------------------------------------
    // Local gen_rst
    //-------------------------------------------------------
    reg          [7:0]       fsm = 'b1;

    ////////////////////////////////////////////////
    // gen_rst
    ////////////////////////////////////////////////
    always @(posedge clk or posedge reset) begin
        
        if (reset) begin  // reset
            rst <= 1'b1;
            fsm <= s0;
        end

        else begin  // not reset

            case (fsm)

                s0 : begin
                    rst <= 1'b1;
                    fsm <= s1;
                end

                s1 : fsm <= s2;
                s2 : fsm <= s3;
                s3 : fsm <= s4;

                s4 : begin
                    if (dcm_locked) begin
                        fsm <= s5;
                    end
                end

                s5 : begin
                    rst <= 1'b0;
                end

                default : begin
                    fsm <= s0;
                end

            endcase
        end     // not reset
    end  //always

endmodule // rst_mod

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////