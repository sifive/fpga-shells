//Author qiu bin 
//Email : chat1@126.dom

`define ADDR_WIDTH  26
`define DATA_WIDTH  128

module ddr2_read_write
(
	clk,
	rst_n,
	local_init_done,
	local_address,
	local_burstbegin,
	local_ready,
	local_read_req,
	local_rdata,
	local_rdata_valid,
	local_write_req,
	local_wdata,
	led
);


input                     clk;
input                     rst_n;
input                     local_init_done;
output [`ADDR_WIDTH - 1:0]local_address;
output                    local_burstbegin;
input                     local_ready;
output                    local_read_req;
input  [`DATA_WIDTH - 1:0]local_rdata;
input                     local_rdata_valid;
output                    local_write_req;
output [`DATA_WIDTH - 1:0]local_wdata;
output                    led;
                        
//
reg  [3  :0]              table_address_index;
reg  [`ADDR_WIDTH - 1:0]  table_address_out;
reg  [3  :0]              table_data_index;
reg  [`DATA_WIDTH - 1:0]  table_data_out;

parameter MAX_TABLE_INDEX = 15;

//address and corresponding data to write and read for verify
always @(*)
begin
	case (table_address_index)
	0 : table_address_out <=  `ADDR_WIDTH'h0000000;
	1 : table_address_out <=  `ADDR_WIDTH'h0400000;
	2 : table_address_out <=  `ADDR_WIDTH'h0800000;
	3 : table_address_out <=  `ADDR_WIDTH'h0c00000;
	4 : table_address_out <=  `ADDR_WIDTH'h1000000;
	5 : table_address_out <=  `ADDR_WIDTH'h1400000;
	6 : table_address_out <=  `ADDR_WIDTH'h1800000;
	7 : table_address_out <=  `ADDR_WIDTH'h1c00000;
	8 : table_address_out <=  `ADDR_WIDTH'h2000000;
	9 : table_address_out <=  `ADDR_WIDTH'h2400000;
	10: table_address_out <=  `ADDR_WIDTH'h2800000;
	11: table_address_out <=  `ADDR_WIDTH'h2c00000;
	12: table_address_out <=  `ADDR_WIDTH'h3000000;
	13: table_address_out <=  `ADDR_WIDTH'h3400000;
	14: table_address_out <=  `ADDR_WIDTH'h3800000;
	15: table_address_out <=  `ADDR_WIDTH'h3c00000;
	default:table_address_out <=  `ADDR_WIDTH'h0;
	endcase
end

always @(*)
begin
	case (table_data_index)
	0 : table_data_out <=   {`DATA_WIDTH/2{2'b01}};
	1 : table_data_out <=   {`DATA_WIDTH/2{2'b10}};
	2 : table_data_out <=   {`DATA_WIDTH/2{2'b00}};     
	3 : table_data_out <=   {`DATA_WIDTH/2{2'b11}};     
	default: table_data_out <=  table_data_index;
	endcase
end


//state machine
reg [2:0] state;
parameter
Idle = 0,
Write = 1,
Read = 2,
Passed = 3,
Failed = 4;
wire write_finished;
wire read_finished;
wire pass_no_fail;
reg pass_no_fail_reg;

always @(posedge clk or negedge rst_n)
if (rst_n == 0)
	state <= Idle;
else begin
	case (state)
	Idle : begin
		if (local_init_done)
			state <= Write;
	end
	Write : begin
		if (write_finished)
			state <= Read;
	end
	Read : begin
		if (read_finished && pass_no_fail)
			state <= Passed;
		else if (read_finished)
			state <= Failed;
	end
	default : state <= state;
	endcase
end

assign write_finished = state == Write && table_address_index == MAX_TABLE_INDEX && local_write_req && local_ready;
assign read_finished = state == Read && table_data_index == MAX_TABLE_INDEX && local_rdata_valid;
assign pass_no_fail = pass_no_fail_reg && local_rdata == table_data_out;

always @(posedge clk or negedge rst_n)
if (rst_n == 0)
	pass_no_fail_reg <= 1;
else if (local_rdata_valid)
	pass_no_fail_reg <= pass_no_fail;
	
//led output
reg [27:0] led_counter;

always @(posedge clk or negedge rst_n)
if (rst_n == 0)
	led_counter <= 0;
else
	led_counter <= led_counter + 1;
	
assign led = state == Passed? led_counter[27] : (
			 state == Failed? 0 : 1);

//table address index 
always @(posedge clk or negedge rst_n)
if (rst_n == 0)
	table_address_index <= 0;
else if (write_finished)
	table_address_index <= 0;
else if ((local_write_req || local_read_req)&& local_ready)
	table_address_index <= table_address_index + 1;

//table data index 
always @(posedge clk or negedge rst_n)
if (rst_n == 0)
	table_data_index <= 0;
else if (write_finished)
	table_data_index <= 0;
else if (local_ready && local_write_req || local_rdata_valid)
	table_data_index <= table_data_index + 1;
	

//output to ddr controller
reg [`ADDR_WIDTH - 1:0] local_address_reg;
always @(posedge clk or negedge rst_n)
if (!rst_n)
	local_address_reg <= 0;
else
	local_address_reg <= local_address;
	
assign local_address =   table_address_out;
assign local_burstbegin = local_address != local_address_reg || table_address_index == 0;
assign local_read_req = state == Read;
assign local_write_req = state == Write;
assign local_wdata = table_data_out;

endmodule
