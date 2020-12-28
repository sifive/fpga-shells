//Legal Notice: (C)2014 Altera Corporation. All rights reserved.  Your
//use of Altera Corporation's design tools, logic functions and other
//software and tools, and its AMPP partner logic functions, and any
//output files any of the foregoing (including device programming or
//simulation files), and any associated documentation or information are
//expressly subject to the terms and conditions of the Altera Program
//License Subscription Agreement or other applicable license agreement,
//including, without limitation, that your use is for the sole purpose
//of programming logic devices manufactured by Altera and sold by Altera
//or its authorized distributors.  Please refer to the applicable
//agreement for further details.

// synthesis translate_off
`timescale 1ns / 1ps
// synthesis translate_on

// turn off superfluous verilog processor warnings 
// altera message_level Level1 
// altera message_off 10034 10035 10036 10037 10230 10240 10030 

module ddr2_64bit_example_driver (
                                   // inputs:
                                    clk,
                                    local_rdata,
                                    local_rdata_valid,
                                    local_ready,
                                    reset_n,

                                   // outputs:
                                    local_bank_addr,
                                    local_be,
                                    local_burstbegin,
                                    local_col_addr,
                                    local_cs_addr,
                                    local_read_req,
                                    local_row_addr,
                                    local_size,
                                    local_wdata,
                                    local_write_req,
                                    pnf_per_byte,
                                    pnf_persist,
                                    test_complete,
                                    test_status
                                 )
  /* synthesis ALTERA_ATTRIBUTE = "MESSAGE_DISABLE=14130;MESSAGE_DISABLE=14110" */ ;

  output  [  2: 0] local_bank_addr;
  output  [ 15: 0] local_be;
  output           local_burstbegin;
  output  [  9: 0] local_col_addr;
  output           local_cs_addr;
  output           local_read_req;
  output  [ 13: 0] local_row_addr;
  output  [  2: 0] local_size;
  output  [127: 0] local_wdata;
  output           local_write_req;
  output  [ 15: 0] pnf_per_byte;
  output           pnf_persist;
  output           test_complete;
  output  [  7: 0] test_status;
  input            clk;
  input   [127: 0] local_rdata;
  input            local_rdata_valid;
  input            local_ready;
  input            reset_n;

  wire    [ 19: 0] COUNTER_VALUE;
  wire    [  2: 0] LOCAL_BURST_LEN_s;
  wire    [  2: 0] MAX_BANK;
  wire             MAX_CHIPSEL;
  wire    [  9: 0] MAX_COL;
  wire    [ 13: 0] MAX_ROW;
  wire    [ 13: 0] MAX_ROW_PIN;
  wire             MIN_CHIPSEL;
  wire    [  4: 0] addr_value;
  wire             avalon_burst_mode;
  reg     [  2: 0] bank_addr;
  reg     [ 15: 0] be;
  reg     [  2: 0] burst_beat_count;
  reg              burst_begin;
  reg     [  9: 0] col_addr;
  wire    [ 15: 0] compare;
  reg     [ 15: 0] compare_reg;
  reg     [ 15: 0] compare_valid;
  reg     [ 15: 0] compare_valid_reg;
  reg              cs_addr;
  wire    [127: 0] dgen_data;
  reg              dgen_enable;
  reg     [127: 0] dgen_ldata;
  reg              dgen_load;
  wire             dgen_pause;
  wire             enable_be;
  reg              full_burst_on;
  reg              last_rdata_valid;
  reg              last_wdata_req;
  wire    [  2: 0] local_bank_addr;
  wire    [ 15: 0] local_be;
  wire             local_burstbegin;
  wire    [  9: 0] local_col_addr;
  wire             local_cs_addr;
  wire             local_read_req;
  wire    [ 13: 0] local_row_addr;
  wire    [  2: 0] local_size;
  wire    [127: 0] local_wdata;
  wire             local_write_req;
  wire    [  9: 0] max_col_value;
  wire             p_burst_begin;
  wire             p_read_req;
  reg              p_state_on;
  wire             pause_be;
  wire    [ 15: 0] pnf_per_byte;
  reg              pnf_persist;
  reg              pnf_persist1;
  wire             pnf_persist_compare;
  wire             powerdn_on;
  reg              rdata_valid_flag;
  reg              rdata_valid_flag_reg;
  reg              rdata_valid_flag_reg_2;
  wire             reached_max_address;
  reg              read_req;
  reg     [  7: 0] reads_remaining;
  reg              reset_address;
  reg              reset_be;
  reg              reset_data;
  wire             restart_LFSR_n;
  reg     [ 13: 0] row_addr;
  wire             selfrfsh_on;
  wire    [  2: 0] size;
  reg     [  4: 0] state;
  reg              test_addr_pin;
  reg              test_addr_pin_mode;
  wire             test_addr_pin_on;
  reg              test_complete;
  reg              test_dm_pin;
  reg              test_dm_pin_mode;
  wire             test_dm_pin_on;
  reg              test_incomplete_writes;
  reg              test_incomplete_writes_mode;
  wire             test_incomplete_writes_on;
  reg              test_seq_addr;
  reg              test_seq_addr_mode;
  wire             test_seq_addr_on;
  wire    [  7: 0] test_status;
  reg              wait_first_write_data;
  wire    [127: 0] wdata;
  wire             wdata_req;
  reg              write_req;
  reg     [  7: 0] writes_remaining;
  //

  //Turn on this mode to test sequential address
  assign test_seq_addr_on = 1'b1;

  //Turn on this mode to test all address pins by a One-hot pattern address generator
  assign test_addr_pin_on = 1'b1;

  //Turn on this mode to make use of dm pins
  assign test_dm_pin_on = 1'b1;

  //Turn on this mode to exercise write process in Full rate, size 1 or DDR3 Half rate, size 1
  assign test_incomplete_writes_on = 1'b1;

  //restart_LFSR_n is an active low signal, set it to 1'b0 to restart LFSR data generator after a complete test
  assign restart_LFSR_n = 1'b1;

  //Change COUNTER_VALUE to control the period of power down and self refresh mode
  assign COUNTER_VALUE = 150;

  //Change MAX_ROW to test more or lesser row address in test_seq_addr_mode, maximum value is 2^(row bits) -1, while minimum value is 0
  assign MAX_ROW = 3;

  //Change MAX_COL to test more or lesser column address in test_seq_addr_mode, maximum value is 2^(column bits) - (LOCAL_BURST_LEN_s * dwidth_ratio (aka half-rate (4) or full-rate (2))), while minimum value is 0 for Half rate and (LOCAL_BURST_LEN_s * dwidth_ratio) for Full rate
  assign MAX_COL = 16;

  //Decrease MAX_BANK to test lesser bank address, minimum value is 0
  assign MAX_BANK = 7;

  //Decrease MAX_CHIPSEL to test lesser memory chip, minimum value is MIN_CHIPSEL
  assign MAX_CHIPSEL = 1;

  //

  assign MIN_CHIPSEL = 0;
  assign MAX_ROW_PIN = {14{1'b1}};
  assign max_col_value = ((addr_value == 2) == 0)? MAX_COL :
    (MAX_COL + 2);

  assign powerdn_on = 1'b0;
  assign selfrfsh_on = 1'b0;
  assign local_burstbegin = burst_begin | p_burst_begin;
  assign avalon_burst_mode = 1;
  //
  //One hot decoder for test_status signal
  assign test_status[0] = test_seq_addr_mode;
  assign test_status[1] = test_incomplete_writes_mode;
  assign test_status[2] = test_dm_pin_mode;
  assign test_status[3] = test_addr_pin_mode;
  assign test_status[4] = 0;
  assign test_status[5] = 0;
  assign test_status[6] = 0;
  assign test_status[7] = test_complete;
  assign p_read_req = 0;
  assign p_burst_begin = 0;
  assign local_cs_addr = cs_addr;
  assign local_row_addr = row_addr;
  assign local_bank_addr = bank_addr;
  assign local_col_addr = col_addr;
  assign local_write_req = write_req;
  assign local_wdata = wdata;
  assign local_read_req = read_req | p_read_req;
  assign wdata = (reset_data == 0)? dgen_data :
    128'd0;

  //The LOCAL_BURST_LEN_s is a signal used insted of the parameter LOCAL_BURST_LEN
  assign LOCAL_BURST_LEN_s = 2;
  //LOCAL INTERFACE (AVALON)
  assign wdata_req = write_req & local_ready;

  // Generate new data (enable lfsr) when writing or reading valid data
  assign dgen_pause = ~ ((wdata_req & ~reset_data) | (local_rdata_valid));

  assign enable_be = (wdata_req & test_dm_pin_mode & ~reset_data) | (test_dm_pin_mode & local_rdata_valid);
  assign pnf_per_byte = compare_valid_reg;
  assign pause_be = (reset_data & test_dm_pin_mode) | ~test_dm_pin_mode;
  assign local_be = be;
  assign local_size = size;
  assign size = (full_burst_on == 0)? 1'd1 :
    LOCAL_BURST_LEN_s[2 : 0];

  assign reached_max_address = ((test_dm_pin_mode | test_addr_pin_mode | state == 5'd9) & (row_addr == MAX_ROW_PIN)) || ((test_seq_addr_mode | test_incomplete_writes_mode) & (col_addr == (max_col_value)) & (row_addr == MAX_ROW) & (bank_addr == MAX_BANK) & (cs_addr == MAX_CHIPSEL));
  assign addr_value = ((test_incomplete_writes_mode & write_req & ~full_burst_on) == 0)? 4 :
    2;

  assign pnf_persist_compare = (rdata_valid_flag_reg_2 == 0)? 1'd1 :
    pnf_persist1;

  ddr2_64bit_ex_lfsr8 LFSRGEN_0_lfsr_inst
    (
      .clk (clk),
      .data (dgen_data[7 : 0]),
      .enable (dgen_enable),
      .ldata (dgen_ldata[7 : 0]),
      .load (dgen_load),
      .pause (dgen_pause),
      .reset_n (reset_n)
    );

  defparam LFSRGEN_0_lfsr_inst.seed = 1;

  // 8 bit comparator per local byte lane
  assign compare[0] = (dgen_data[7 : 0] & {8 {be[0]}}) === local_rdata[7 : 0];

  ddr2_64bit_ex_lfsr8 LFSRGEN_1_lfsr_inst
    (
      .clk (clk),
      .data (dgen_data[15 : 8]),
      .enable (dgen_enable),
      .ldata (dgen_ldata[15 : 8]),
      .load (dgen_load),
      .pause (dgen_pause),
      .reset_n (reset_n)
    );

  defparam LFSRGEN_1_lfsr_inst.seed = 11;

  // 8 bit comparator per local byte lane
  assign compare[1] = (dgen_data[15 : 8] & {8 {be[1]}}) === local_rdata[15 : 8];

  ddr2_64bit_ex_lfsr8 LFSRGEN_2_lfsr_inst
    (
      .clk (clk),
      .data (dgen_data[23 : 16]),
      .enable (dgen_enable),
      .ldata (dgen_ldata[23 : 16]),
      .load (dgen_load),
      .pause (dgen_pause),
      .reset_n (reset_n)
    );

  defparam LFSRGEN_2_lfsr_inst.seed = 21;

  // 8 bit comparator per local byte lane
  assign compare[2] = (dgen_data[23 : 16] & {8 {be[2]}}) === local_rdata[23 : 16];

  ddr2_64bit_ex_lfsr8 LFSRGEN_3_lfsr_inst
    (
      .clk (clk),
      .data (dgen_data[31 : 24]),
      .enable (dgen_enable),
      .ldata (dgen_ldata[31 : 24]),
      .load (dgen_load),
      .pause (dgen_pause),
      .reset_n (reset_n)
    );

  defparam LFSRGEN_3_lfsr_inst.seed = 31;

  // 8 bit comparator per local byte lane
  assign compare[3] = (dgen_data[31 : 24] & {8 {be[3]}}) === local_rdata[31 : 24];

  ddr2_64bit_ex_lfsr8 LFSRGEN_4_lfsr_inst
    (
      .clk (clk),
      .data (dgen_data[39 : 32]),
      .enable (dgen_enable),
      .ldata (dgen_ldata[39 : 32]),
      .load (dgen_load),
      .pause (dgen_pause),
      .reset_n (reset_n)
    );

  defparam LFSRGEN_4_lfsr_inst.seed = 41;

  // 8 bit comparator per local byte lane
  assign compare[4] = (dgen_data[39 : 32] & {8 {be[4]}}) === local_rdata[39 : 32];

  ddr2_64bit_ex_lfsr8 LFSRGEN_5_lfsr_inst
    (
      .clk (clk),
      .data (dgen_data[47 : 40]),
      .enable (dgen_enable),
      .ldata (dgen_ldata[47 : 40]),
      .load (dgen_load),
      .pause (dgen_pause),
      .reset_n (reset_n)
    );

  defparam LFSRGEN_5_lfsr_inst.seed = 51;

  // 8 bit comparator per local byte lane
  assign compare[5] = (dgen_data[47 : 40] & {8 {be[5]}}) === local_rdata[47 : 40];

  ddr2_64bit_ex_lfsr8 LFSRGEN_6_lfsr_inst
    (
      .clk (clk),
      .data (dgen_data[55 : 48]),
      .enable (dgen_enable),
      .ldata (dgen_ldata[55 : 48]),
      .load (dgen_load),
      .pause (dgen_pause),
      .reset_n (reset_n)
    );

  defparam LFSRGEN_6_lfsr_inst.seed = 61;

  // 8 bit comparator per local byte lane
  assign compare[6] = (dgen_data[55 : 48] & {8 {be[6]}}) === local_rdata[55 : 48];

  ddr2_64bit_ex_lfsr8 LFSRGEN_7_lfsr_inst
    (
      .clk (clk),
      .data (dgen_data[63 : 56]),
      .enable (dgen_enable),
      .ldata (dgen_ldata[63 : 56]),
      .load (dgen_load),
      .pause (dgen_pause),
      .reset_n (reset_n)
    );

  defparam LFSRGEN_7_lfsr_inst.seed = 71;

  // 8 bit comparator per local byte lane
  assign compare[7] = (dgen_data[63 : 56] & {8 {be[7]}}) === local_rdata[63 : 56];

  ddr2_64bit_ex_lfsr8 LFSRGEN_8_lfsr_inst
    (
      .clk (clk),
      .data (dgen_data[71 : 64]),
      .enable (dgen_enable),
      .ldata (dgen_ldata[71 : 64]),
      .load (dgen_load),
      .pause (dgen_pause),
      .reset_n (reset_n)
    );

  defparam LFSRGEN_8_lfsr_inst.seed = 81;

  // 8 bit comparator per local byte lane
  assign compare[8] = (dgen_data[71 : 64] & {8 {be[8]}}) === local_rdata[71 : 64];

  ddr2_64bit_ex_lfsr8 LFSRGEN_9_lfsr_inst
    (
      .clk (clk),
      .data (dgen_data[79 : 72]),
      .enable (dgen_enable),
      .ldata (dgen_ldata[79 : 72]),
      .load (dgen_load),
      .pause (dgen_pause),
      .reset_n (reset_n)
    );

  defparam LFSRGEN_9_lfsr_inst.seed = 91;

  // 8 bit comparator per local byte lane
  assign compare[9] = (dgen_data[79 : 72] & {8 {be[9]}}) === local_rdata[79 : 72];

  ddr2_64bit_ex_lfsr8 LFSRGEN_10_lfsr_inst
    (
      .clk (clk),
      .data (dgen_data[87 : 80]),
      .enable (dgen_enable),
      .ldata (dgen_ldata[87 : 80]),
      .load (dgen_load),
      .pause (dgen_pause),
      .reset_n (reset_n)
    );

  defparam LFSRGEN_10_lfsr_inst.seed = 101;

  // 8 bit comparator per local byte lane
  assign compare[10] = (dgen_data[87 : 80] & {8 {be[10]}}) === local_rdata[87 : 80];

  ddr2_64bit_ex_lfsr8 LFSRGEN_11_lfsr_inst
    (
      .clk (clk),
      .data (dgen_data[95 : 88]),
      .enable (dgen_enable),
      .ldata (dgen_ldata[95 : 88]),
      .load (dgen_load),
      .pause (dgen_pause),
      .reset_n (reset_n)
    );

  defparam LFSRGEN_11_lfsr_inst.seed = 111;

  // 8 bit comparator per local byte lane
  assign compare[11] = (dgen_data[95 : 88] & {8 {be[11]}}) === local_rdata[95 : 88];

  ddr2_64bit_ex_lfsr8 LFSRGEN_12_lfsr_inst
    (
      .clk (clk),
      .data (dgen_data[103 : 96]),
      .enable (dgen_enable),
      .ldata (dgen_ldata[103 : 96]),
      .load (dgen_load),
      .pause (dgen_pause),
      .reset_n (reset_n)
    );

  defparam LFSRGEN_12_lfsr_inst.seed = 121;

  // 8 bit comparator per local byte lane
  assign compare[12] = (dgen_data[103 : 96] & {8 {be[12]}}) === local_rdata[103 : 96];

  ddr2_64bit_ex_lfsr8 LFSRGEN_13_lfsr_inst
    (
      .clk (clk),
      .data (dgen_data[111 : 104]),
      .enable (dgen_enable),
      .ldata (dgen_ldata[111 : 104]),
      .load (dgen_load),
      .pause (dgen_pause),
      .reset_n (reset_n)
    );

  defparam LFSRGEN_13_lfsr_inst.seed = 131;

  // 8 bit comparator per local byte lane
  assign compare[13] = (dgen_data[111 : 104] & {8 {be[13]}}) === local_rdata[111 : 104];

  ddr2_64bit_ex_lfsr8 LFSRGEN_14_lfsr_inst
    (
      .clk (clk),
      .data (dgen_data[119 : 112]),
      .enable (dgen_enable),
      .ldata (dgen_ldata[119 : 112]),
      .load (dgen_load),
      .pause (dgen_pause),
      .reset_n (reset_n)
    );

  defparam LFSRGEN_14_lfsr_inst.seed = 141;

  // 8 bit comparator per local byte lane
  assign compare[14] = (dgen_data[119 : 112] & {8 {be[14]}}) === local_rdata[119 : 112];

  ddr2_64bit_ex_lfsr8 LFSRGEN_15_lfsr_inst
    (
      .clk (clk),
      .data (dgen_data[127 : 120]),
      .enable (dgen_enable),
      .ldata (dgen_ldata[127 : 120]),
      .load (dgen_load),
      .pause (dgen_pause),
      .reset_n (reset_n)
    );

  defparam LFSRGEN_15_lfsr_inst.seed = 151;

  // 8 bit comparator per local byte lane
  assign compare[15] = (dgen_data[127 : 120] & {8 {be[15]}}) === local_rdata[127 : 120];

  //
  //-----------------------------------------------------------------
  //Main clocked process
  //-----------------------------------------------------------------
  //Read / Write control state machine & address counter
  //-----------------------------------------------------------------
  always @(posedge clk or negedge reset_n)
    begin
      if (reset_n == 0)
        begin
          //Reset - asynchronously force all register outputs LOW
          state <= 5'd0;

          write_req <= 1'b0;
          read_req <= 1'b0;
          burst_begin <= 1'b0;
          burst_beat_count <= 0;
          dgen_load <= 1'b0;
          wait_first_write_data <= 1'b0;
          test_complete <= 1'b0;
          reset_data <= 1'b0;
          reset_be <= 1'b0;
          writes_remaining <= 0;
          reads_remaining <= 0;
          test_addr_pin <= 1'b0;
          test_dm_pin <= 1'b0;
          test_seq_addr <= 1'b0;
          test_incomplete_writes <= 1'b0;
          test_addr_pin_mode <= 1'b0;
          test_dm_pin_mode <= 1'b0;
          test_seq_addr_mode <= 1'b0;
          test_incomplete_writes_mode <= 1'b0;
          full_burst_on <= 1'b1;
          p_state_on <= 1'b0;
          dgen_enable <= 1'b1;
        end
      else 
        begin
          if (write_req & local_ready)
            begin
              if (wdata_req)
                  writes_remaining <= writes_remaining + (size - 1);
              else 
                writes_remaining <= writes_remaining + size;
            end
          else if ((wdata_req) & (writes_remaining > 0))
              //size
              writes_remaining <= writes_remaining - 1'b1;

          else 
            writes_remaining <= writes_remaining;
          if ((read_req | p_read_req) & local_ready)
            begin
              if (local_rdata_valid)
                  reads_remaining <= reads_remaining + (size - 1);
              else 
                reads_remaining <= reads_remaining + size;
            end
          else if ((local_rdata_valid) & (reads_remaining > 0))
              reads_remaining <= reads_remaining - 1'b1;
          else 
            reads_remaining <= reads_remaining;
          case (state)
          
              5'd0: begin
                  test_addr_pin <= test_addr_pin_on;
                  test_dm_pin <= test_dm_pin_on;
                  test_seq_addr <= test_seq_addr_on;
                  test_incomplete_writes <= test_incomplete_writes_on;
                  test_complete <= 1'b0;
                  state <= 5'd1;
              end // 5'd0 
          
              5'd1: begin
                  //Reset just in case!
                  reset_address <= 1'b0;
          
                  reset_be <= 1'b0;
                  write_req <= 1'b1;
                  writes_remaining <= 1'b0;
                  reads_remaining <= 1'b0;
                  wait_first_write_data <= 1'b1;
                  dgen_enable <= 1'b1;
                  if (test_seq_addr == 1'b1)
                    begin
                      test_seq_addr_mode <= 1;
                      if (avalon_burst_mode == 0)
                        begin
                          state <= 5'd5;
                          burst_begin <= 1'b1;
                        end
                      else if (avalon_burst_mode == 1)
                        begin
                          state <= 5'd13;
                          burst_begin <= 1'b1;
                        end
                    end
                  else if (test_incomplete_writes == 1'b1)
                    begin
                      full_burst_on <= 1'b0;
                      test_incomplete_writes_mode <= 1;
                      state <= 5'd5;
                      if (avalon_burst_mode == 1)
                          burst_begin <= 1'b1;
                    end
                  else if (test_dm_pin == 1'b1)
                    begin
                      reset_data <= 1'b1;
                      test_dm_pin_mode <= 1;
                      if (avalon_burst_mode == 0)
                        begin
                          burst_begin <= 1'b1;
                          state <= 5'd2;
                        end
                      else 
                        begin
                          burst_begin <= 1'b1;
                          state <= 5'd10;
                        end
                    end
                  else if (test_addr_pin == 1'b1)
                    begin
                      test_addr_pin_mode <= 1;
                      if (avalon_burst_mode == 0)
                        begin
                          burst_begin <= 1'b1;
                          state <= 5'd5;
                        end
                      else if (avalon_burst_mode == 1)
                        begin
                          state <= 5'd13;
                          burst_begin <= 1'b1;
                        end
                    end
                  else 
                    begin
                      write_req <= 1'b0;
                      wait_first_write_data <= 1'b0;
                      state <= 5'd9;
                    end
              end // 5'd1 
          
              5'd10: begin
                  wait_first_write_data <= 1'b0;
                  burst_begin <= 1'b0;
                  if (write_req & local_ready)
                    begin
                      burst_beat_count <= burst_beat_count + 1'b1;
                      state <= 5'd11;
                    end
              end // 5'd10 
          
              5'd11: begin
                  if (write_req & local_ready)
                      if (burst_beat_count == size - 1'b1)
                        begin
                          burst_beat_count <= 0;
                          burst_begin <= 1'b1;
                          if (reached_max_address)
                              state <= 5'd12;
                          else 
                            state <= 5'd10;
                        end
                      else 
                        burst_beat_count <= burst_beat_count + 1'b1;
              end // 5'd11 
          
              5'd12: begin
                  burst_begin <= 1'b0;
                  if (write_req & local_ready)
                      state <= 5'd3;
              end // 5'd12 
          
              5'd13: begin
                  wait_first_write_data <= 1'b0;
                  burst_begin <= 1'b0;
                  reset_be <= 1'b0;
                  if (write_req & local_ready)
                    begin
                      burst_beat_count <= burst_beat_count + 1'b1;
                      state <= 5'd14;
                    end
              end // 5'd13 
          
              5'd14: begin
                  if (write_req & local_ready)
                      if (burst_beat_count == size - 1'b1)
                        begin
                          burst_beat_count <= 0;
                          burst_begin <= 1'b1;
                          if (reached_max_address)
                              state <= 5'd15;
                          else 
                            state <= 5'd13;
                        end
                      else 
                        burst_beat_count <= burst_beat_count + 1'b1;
              end // 5'd14 
          
              5'd15: begin
                  if (write_req & local_ready)
                    begin
                      reset_address <= 1'b1;
                      burst_begin <= 1'b0;
                      state <= 5'd6;
                    end
              end // 5'd15 
          
              5'd16: begin
                  dgen_load <= 1'b0;
                  reset_be <= 1'b0;
                  if (local_ready == 1'b0)
                    begin
                      read_req <= 1'b1;
                      burst_begin <= 1'b0;
                    end
                  else if (local_ready & read_req)
                      if (reached_max_address)
                        begin
                          read_req <= 1'b0;
                          burst_begin <= 1'b0;
                          state <= 5'd8;
                        end
                      else 
                        begin
                          read_req <= 1'b1;
                          burst_begin <= 1'b1;
                        end
              end // 5'd16 
          
              5'd2: begin
                  wait_first_write_data <= 1'b0;
                  if (write_req & local_ready)
                      if (reached_max_address)
                        begin
                          write_req <= 1'b0;
                          burst_begin <= 1'b0;
                          state <= 5'd3;
                        end
              end // 5'd2 
          
              5'd3: begin
                  if (avalon_burst_mode == 0)
                    begin
                      if (!wdata_req)
                          if (writes_remaining == 0)
                            begin
                              reset_be <= 1'b1;
                              reset_address <= 1'b1;
                              dgen_load <= 1'b1;
                              state <= 5'd4;
                            end
                    end
                  else if (write_req & local_ready)
                    begin
                      reset_be <= 1'b1;
                      write_req <= 1'b0;
                      reset_address <= 1'b1;
                      dgen_load <= 1'b1;
                      state <= 5'd4;
                    end
              end // 5'd3 
          
              5'd4: begin
                  reset_address <= 1'b0;
                  dgen_load <= 1'b0;
                  reset_be <= 1'b0;
                  reset_data <= 1'b0;
                  write_req <= 1'b1;
                  if (avalon_burst_mode == 0)
                    begin
                      burst_begin <= 1'b1;
                      state <= 5'd5;
                    end
                  else 
                    begin
                      burst_begin <= 1'b1;
                      state <= 5'd13;
                    end
              end // 5'd4 
          
              5'd5: begin
                  wait_first_write_data <= 1'b0;
                  if (local_ready == 1'b0)
                    begin
                      write_req <= 1'b1;
                      burst_begin <= 1'b0;
                    end
                  else if (write_req & local_ready)
                      if (reached_max_address)
                        begin
                          reset_address <= 1'b1;
                          write_req <= 1'b0;
                          burst_begin <= 1'b0;
                          state <= 5'd6;
                          if (test_incomplete_writes_mode)
                              full_burst_on <= 1'b1;
                        end
                      else 
                        begin
                          write_req <= 1'b1;
                          burst_begin <= 1'b1;
                        end
              end // 5'd5 
          
              5'd6: begin
                  reset_address <= 1'b0;
                  if (avalon_burst_mode == 0)
                    begin
                      if (writes_remaining == 0)
                        begin
                          dgen_load <= 1'b1;
                          reset_be <= 1'b1;
                          read_req <= 1'b1;
                          burst_begin <= 1'b1;
                          state <= 5'd7;
                        end
                    end
                  else if (test_incomplete_writes_mode)
                    begin
                      dgen_load <= 1'b1;
                      read_req <= 1'b1;
                      burst_begin <= 1'b1;
                      state <= 5'd16;
                    end
                  else if (write_req & local_ready)
                    begin
                      write_req <= 1'b0;
                      dgen_load <= 1'b1;
                      reset_be <= 1'b1;
                      read_req <= 1'b1;
                      burst_begin <= 1'b1;
                      state <= 5'd16;
                    end
              end // 5'd6 
          
              5'd7: begin
                  dgen_load <= 1'b0;
                  reset_be <= 1'b0;
                  if (local_ready & read_req)
                      if (reached_max_address)
                        begin
                          read_req <= 1'b0;
                          burst_begin <= 1'b0;
                          state <= 5'd8;
                        end
              end // 5'd7 
          
              5'd8: begin
                  if (reads_remaining == 1'b0)
                    begin
                      reset_address <= 1'b1;
                      if (test_seq_addr)
                        begin
                          test_seq_addr <= 1'b0;
                          test_seq_addr_mode <= 1'b0;
                          state <= 5'd1;
                        end
                      else if (test_incomplete_writes)
                        begin
                          test_incomplete_writes <= 1'b0;
                          test_incomplete_writes_mode <= 1'b0;
                          state <= 5'd1;
                        end
                      else if (test_dm_pin)
                        begin
                          test_dm_pin <= 1'b0;
                          test_dm_pin_mode <= 1'b0;
                          state <= 5'd1;
                        end
                      else if (test_addr_pin)
                        begin
                          test_addr_pin_mode <= 1'b0;
                          dgen_load <= 1'b1;
                          state <= 5'd9;
                        end
                      else 
                        state <= 5'd9;
                    end
              end // 5'd8 
          
              5'd9: begin
                  reset_address <= 1'b0;
                  reset_be <= 1'b0;
                  dgen_load <= 1'b0;
                  if (powerdn_on == 1'b0 & selfrfsh_on == 1'b0)
                    begin
                      test_complete <= 1'b1;
                      p_state_on <= 1'b0;
                      dgen_enable <= restart_LFSR_n;
                      state <= 5'd0;
                    end
                  else if (reached_max_address & reads_remaining == 0)
                    begin
                      p_state_on <= 1'b1;
                      reset_address <= 1'b1;
                      reset_be <= 1'b1;
                      dgen_load <= 1'b1;
                    end
              end // 5'd9 
          
          endcase // state
        end
    end


  //
  //-----------------------------------------------------------------
  //Logics that detect the first read data
  //-----------------------------------------------------------------
  always @(posedge clk or negedge reset_n)
    begin
      if (reset_n == 0)
          rdata_valid_flag <= 1'b0;
      else if (local_rdata_valid)
          rdata_valid_flag <= 1'b1;
    end


  //
  //-----------------------------------------------------------------
  //Address Generator Process
  //-----------------------------------------------------------------
  always @(posedge clk or negedge reset_n)
    begin
      if (reset_n == 0)
        begin
          cs_addr <= 0;
          bank_addr <= 0;
          row_addr <= 0;
          col_addr <= 0;
        end
      else if (reset_address)
        begin
          cs_addr <= MIN_CHIPSEL;
          row_addr <= 0;
          bank_addr <= 0;
          col_addr <= 0;
        end
      else if (((local_ready & write_req & (test_dm_pin_mode | test_addr_pin_mode)) & (state == 5'd2 | state == 5'd5 | state == 5'd10 | state == 5'd13)) | ((local_ready & read_req & (test_dm_pin_mode | test_addr_pin_mode)) & (state == 5'd7 | state == 5'd16)) | ((local_ready & p_read_req) & (state == 5'd9)))
        begin
          col_addr[9 : 2] <= {col_addr[8 : 2],col_addr[9]};
          row_addr[13 : 0] <= {row_addr[12 : 0],row_addr[13]};
          if (row_addr == 14'd0)
            begin
              col_addr <= 10'd4;
              row_addr <= 14'd1;
            end
          else if (row_addr == {1'b1,{13{1'b0}}})
            begin
              col_addr <= {{7{1'b1}},{3{1'b0}}};
              row_addr <= {{13{1'b1}},1'b0};
            end
          else if (row_addr == {1'b0,{13{1'b1}}})
            begin
              col_addr <= {{8{1'b1}},{2{1'b0}}};
              row_addr <= {14{1'b1}};
            end
          if (bank_addr == MAX_BANK)
              bank_addr <= 0;
          else 
            bank_addr <= bank_addr + 1'b1;
          if (cs_addr == MAX_CHIPSEL)
              cs_addr <= MIN_CHIPSEL;
          else 
            cs_addr <= cs_addr + 1'b1;
        end
      else if ((local_ready & write_req & (test_seq_addr_mode | test_incomplete_writes_mode) &  (state == 5'd2 | state == 5'd5 | state == 5'd10 | state == 5'd13)) | ((local_ready & read_req & (test_seq_addr_mode | test_incomplete_writes_mode)) & (state == 5'd7 | state == 5'd16)))
          if (col_addr >= max_col_value)
            begin
              col_addr <= 0;
              if (row_addr == MAX_ROW)
                begin
                  row_addr <= 0;
                  if (bank_addr == MAX_BANK)
                    begin
                      bank_addr <= 0;
                      if (cs_addr == MAX_CHIPSEL)
                          //reached_max_count <= TRUE
                          //(others => '0')
                          cs_addr <= MIN_CHIPSEL;

                      else 
                        cs_addr <= cs_addr + 1'b1;
                    end
                  else 
                    bank_addr <= bank_addr + 1'b1;
                end
              else 
                row_addr <= row_addr + 1'b1;
            end
          else 
            col_addr <= col_addr + addr_value;
    end


  //
  //-----------------------------------------------------------------
  //Byte Enable Generator Process
  //-----------------------------------------------------------------
  always @(posedge clk or negedge reset_n)
    begin
      if (reset_n == 0)
          be <= {16{1'b1}};
      else if (reset_be)
          be <= 16'd1;
      else if (enable_be)
          be[15 : 0] <= {be[14 : 0],be[15]};
      else if (pause_be)
          be <= {16{1'b1}};
      else 
        be <= be;
    end


  //------------------------------------------------------------
  //LFSR re-load data storage
  //Comparator masking and test pass signal generation
  //------------------------------------------------------------
  always @(posedge clk or negedge reset_n)
    begin
      if (reset_n == 0)
        begin
          dgen_ldata <= 0;
          last_wdata_req <= 1'b0;
          //all ones
          compare_valid <= {16 {1'b1}};

          //all ones
          compare_valid_reg <= {16 {1'b1}};

          pnf_persist <= 1'b0;
          pnf_persist1 <= 1'b0;
          //all ones
          compare_reg <= {16 {1'b1}};

          last_rdata_valid <= 1'b0;
          rdata_valid_flag_reg <= 1'b0;
          rdata_valid_flag_reg_2 <= 1'b0;
        end
      else 
        begin
          last_wdata_req <= wdata_req;
          last_rdata_valid <= local_rdata_valid;
          rdata_valid_flag_reg <= rdata_valid_flag;
          rdata_valid_flag_reg_2 <= rdata_valid_flag_reg;
          compare_reg <= compare;
          if (wait_first_write_data)
              dgen_ldata <= dgen_data;
          //Enable the comparator result when read data is valid
          if (last_rdata_valid)
              compare_valid <= compare_reg;
          //Create the overall persistent passnotfail output
          if (&compare_valid & rdata_valid_flag_reg & pnf_persist_compare)
              pnf_persist1 <= 1'b1;
          else 
            pnf_persist1 <= 1'b0;
          //Extra register stage to help Tco / Fmax on comparator output pins
          compare_valid_reg <= compare_valid;

          pnf_persist <= pnf_persist1;
        end
    end



endmodule

