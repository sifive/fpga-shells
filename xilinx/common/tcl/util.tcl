# See LICENSE for license details.

# Helper function that recursively includes files given a directory and a
# pattern/suffix extensions
proc recglob { basedir pattern } {
  set dirlist [glob -nocomplain -directory $basedir -type d *]
  set findlist [glob -nocomplain -directory $basedir $pattern]
  foreach dir $dirlist {
    set reclist [recglob $dir $pattern]
    set findlist [concat $findlist $reclist]
  }
  return $findlist
}

# Helper function to find all subdirectories containing ".vh" files
proc findincludedir { basedir pattern } {
  set vhfiles [recglob $basedir $pattern]
  set vhdirs {}
  foreach match $vhfiles {
    lappend vhdirs [file dir $match]
  }
  set uniquevhdirs [lsort -unique $vhdirs]
  return $uniquevhdirs
}
