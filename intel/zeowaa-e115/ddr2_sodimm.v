module ddr2_sodimm (
	clk,
	rst_n,
	
	mem_odt,
	mem_cs_n,
	mem_cke,
	mem_addr,
	mem_ba,
	mem_ras_n,
	mem_cas_n,
	mem_we_n,
	mem_dm,
	mem_clk,
	mem_clk_n,
	mem_dq,
	mem_dqs,
	
	led,
	LED1
	);
	
	input clk;
	input rst_n;
	
	output		led,LED1;
	
	output	[1:0]	mem_odt;
	output	[1:0]	mem_cs_n;
	output	[1:0]	mem_cke;
	output	[13:0]	mem_addr;
	output	[2:0]	mem_ba;
	output		mem_ras_n;
	output		mem_cas_n;
	output		mem_we_n;
	output	[7:0]	mem_dm;
	inout	[1:0]	mem_clk;
	inout	[1:0]	mem_clk_n;
	inout	[63:0]	mem_dq;
	inout	[7:0]	mem_dqs;


wire               local_init_done;
wire   [25 :0]     local_address;
wire               local_burstbegin;
wire               local_ready;
wire               local_read_req;
wire   [127 :0]    local_rdata;
wire               local_rdata_valid;
wire               local_write_req;
wire   [127 :0]    local_wdata;
wire               phy_clk;
	
	ddr2_64bit ddr2_64bit (
	.local_address(local_address),
	.local_write_req(local_write_req),
	.local_read_req(local_read_req),
	.local_burstbegin(local_burstbegin),
	.local_wdata(local_wdata),
	.local_be(16'hffff),
	.local_size(1),
	.global_reset_n(rst_n),
	.pll_ref_clk(clk),
	.soft_reset_n(rst_n),
	.local_ready(local_ready),
	.local_rdata(local_rdata),
	.local_rdata_valid(local_rdata_valid),
	.local_refresh_ack(),
	.local_init_done(local_init_done),
	.reset_phy_clk_n(),
	.mem_odt(mem_odt),
	.mem_cs_n(mem_cs_n),
	.mem_cke(mem_cke),
	.mem_addr(mem_addr),
	.mem_ba(mem_ba),
	.mem_ras_n(mem_ras_n),
	.mem_cas_n(mem_cas_n),
	.mem_we_n(mem_we_n),
	.mem_dm(mem_dm),
	.phy_clk(phy_clk),
	.aux_full_rate_clk(),
	.aux_half_rate_clk(),
	.reset_request_n(),
	.mem_clk(mem_clk),
	.mem_clk_n(mem_clk_n),
	.mem_dq(mem_dq),
	.mem_dqs(mem_dqs));
	
	

ddr2_read_write ddr2_read_write
(
  .clk (phy_clk),
  .rst_n (rst_n),
  .local_address (local_address),
  .local_burstbegin (local_burstbegin),
  .local_init_done (local_init_done),
  .local_rdata (local_rdata),
  .local_rdata_valid (local_rdata_valid),
  .local_read_req (local_read_req),
  .local_ready (local_ready),
  .local_wdata (local_wdata),
  .local_write_req (local_write_req),
  .led(led)
);

	
endmodule
