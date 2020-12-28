package require ::quartus::ddr_timing_model

# The clock period of your memory interface. Don't modify this
set ::t(period) 6.666

# The worst case skew between any pair of traces which are nominally matched
set ::t(board_skew) 0.020
set ::t(min_additional_dqs_variation) 0.000
set ::t(max_additional_dqs_variation) 0.000

###########################################################
# Memory timing parameters. See Section 6 of the JEDEC spec.
# ----------------------------------
# tDS/tDH: write timing
set ::t(DS) 0.250
set ::t(DH) 0.250

# Data output timing for non-DQS capture
set ::t(AC) 0.400

# Address and command input timing
set ::t(IS) 0.375
set ::t(IH) 0.375

# DQS to CK input timing
set ::t(DSS) 0.2
set ::t(DSH) 0.2
set ::t(DQSS) 0.25

# DQ to DQS timing on read
set ::t(DQSQ) 0.200
set ::t(QHS) 0.300

# DQS to CK timing on reads
set ::t(DQSCK) 0.350
set ::t(HP) 3.000

# The maximum allowed length of the mimic path depends on the device family
set ::t(mimic_shift) 2.500
# The clock period of the PLL reference clock
set ::t(inclk_period) 40.000

#####################
# FPGA specifications
#####################

# Duty cycle distortion from the device data sheet
set ::t(DCD_total) 0.250
# PLL phase shift error
set ::t(PLL_PSERR) 0.000

###############
# SSN Info
###############

set package_type [get_package_type]
set ::SSN(pushout_o) [expr [get_micro_node_delay -micro SSO -parameters [list IO DQDQSABSOLUTE NONLEVELED MAX] -package_type $package_type -in_fitter]/1000.0]
set ::SSN(pullin_o)  [expr [get_micro_node_delay -micro SSO -parameters [list IO DQDQSABSOLUTE NONLEVELED MIN] -package_type $package_type -in_fitter]/-1000.0]
set ::SSN(pushout_i) [expr [get_micro_node_delay -micro SSI -parameters [list IO DQDQSABSOLUTE NONLEVELED MAX] -package_type $package_type -in_fitter]/1000.0]
set ::SSN(pullin_i)  [expr [get_micro_node_delay -micro SSI -parameters [list IO DQDQSABSOLUTE NONLEVELED MIN] -package_type $package_type -in_fitter]/-1000.0]
set ::SSN(rel_pushout_o) [expr [get_micro_node_delay -micro SSO -parameters [list IO DQDQSRELATIVE NONLEVELED MAX] -package_type $package_type -in_fitter]/1000.0]
set ::SSN(rel_pullin_o)  [expr [get_micro_node_delay -micro SSO -parameters [list IO DQDQSRELATIVE NONLEVELED MIN] -package_type $package_type -in_fitter]/-1000.0]
set ::SSN(rel_pushout_i) [expr [get_micro_node_delay -micro SSI -parameters [list IO DQDQSRELATIVE NONLEVELED MAX] -package_type $package_type -in_fitter]/1000.0]
set ::SSN(rel_pullin_i)  [expr [get_micro_node_delay -micro SSI -parameters [list IO DQDQSRELATIVE NONLEVELED MIN] -package_type $package_type -in_fitter]/-1000.0]

###############
# Board Effects
###############

# Board skews
set ::board(minCK_DQS_skew) -0.010
set ::board(maxCK_DQS_skew) 0.010
set ::board(tpd_inter_DIMM) 0.050
set ::board(intra_DQS_group_skew) 0.020
set ::board(inter_DQS_group_skew) 0.020
set ::board(addresscmd_CK_skew) 0.000
set ::t(additional_addresscmd_tpd) $::board(addresscmd_CK_skew)

# ISI effects
set ::ISI(addresscmd_setup) 0.000
set ::ISI(addresscmd_hold) 0.000
set ::ISI(DQ) 0.000
set ::ISI(DQS) 0.000

set ddr2_64bit_phy_use_flexible_timing 1

