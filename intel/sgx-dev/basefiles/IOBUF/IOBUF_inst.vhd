	component IOBUF is
		port (
			dataout : out   std_logic_vector(0 downto 0);                    -- export
			datain  : in    std_logic_vector(0 downto 0) := (others => 'X'); -- export
			oe      : in    std_logic_vector(0 downto 0) := (others => 'X'); -- export
			padio   : inout std_logic_vector(0 downto 0) := (others => 'X')  -- export
		);
	end component IOBUF;

	u0 : component IOBUF
		port map (
			dataout => CONNECTED_TO_dataout, --   dout.export
			datain  => CONNECTED_TO_datain,  --    din.export
			oe      => CONNECTED_TO_oe,      --     oe.export
			padio   => CONNECTED_TO_padio    -- pad_io.export
		);

