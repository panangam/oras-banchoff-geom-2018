#############################
#
#  File:     /Data/StageTools/TFB/Cartan-M3/Cartan-M3-02b.cs
#  Created:  Mon Aug 26 17:29:48 EDT 2013
#  By:       CenterStage v3.2
#

::cs::File Version 3.2


####################

::class::Group Create Cartan-M3 {
  {##################################################

set Faces \{
  124 125 134 136 156 235 236 246 345 456
\}

foreach f $Faces \{set RP2([split $f \"\"]) 1\}
unset f

##################################################

proc Outline \{faces \{t .25\}\} \{
  if \{$t == 1\} \{return $faces\}
  if \{$t == 0\} \{return \{\}\}
  set flist \{\}
  foreach face $faces \{
    set f [lindex $face 0]
    switch [llength $f] \{
      1 - 2 \{lappend flist $face\}
      3 \{
          let (p_0,p_1,p_2) = f
          let P = (p_0 + p_1 + p_2) / 3
          foreach i \{0 1 2\} \\
            \{let q_i = p_i + t (P - p_i)\}
          foreach i \{0 1 2\} j \{1 2 0\} \{
            let f = (p_i,p_j,q_j,q_i)
            lappend flist [lreplace $face 0 0 $f]
          \}
       \}
      4 \{
          let (p_0,p_1,p_2,p_3) = f
          let P = ((p_0 + p_1) + (p_2 + p_3)) / 4
          foreach i \{0 1 2 3\} \\
            \{let q_i = p_i + t (P - p_i)\}
          foreach i \{0 1 2 3\} j \{1 2 3 0\} \{
            let f = (p_i,p_j,q_j,q_i)
            lappend flist [lreplace $face 0 0 $f]
          \}
       \}
    \}
  \}
  return $flist
\}

##################################################
#
#  The number of elements
#
let nn = 6

#
#  Convert permutations in standard format
#  like  (12)(345)  to internal format.
#
proc Permutation \{P\} \{
  variables nn
  regsub -all \{ +\} $P \{\} P
  if \{![regexp \{^(\\(\\d+\\))*$\} $P]\} \\
    \{Error \"'$P' doesn't look like a permutation\"\}
  set Q \{\}
  for \{set i 0\}  \{$i < $nn\} \{incr i\} \{lappend Q $i\}
  set p [string index $P 0]
  foreach c [split [string range $P 1 end] \"\"] \{
    if \{$c != \"(\"\} \{
      if \{$p == \"(\"\} \{set first $c; incr c -1\} else \{
        if \{$c == \")\"\} \{set c $first\}
        set Q [lreplace $Q $p $p [incr c -1]]
      \}
    \}
    set p $c
  \}
  return $Q
\}

#
#  Apply a permutation P to a vector of elements V
#
proc ApplyPerm \{P V\} \{
  set VV \{\}
  foreach i $V \{lappend VV [lindex $P $i]\}
  return $VV
\}

#
#  Produce the group (list of permutations) generated
#  by a collections of permutations (in standard form).
#
proc Group \{generators\} \{
  set N \{\}; set gg \{\}
  set G([Permutation \{\}]) 1
  foreach g $generators \{
    set g [Permutation $g]; lappend gg $g
    lappend N $g; set G($g) 1
  \}
  while \{[llength $N] > 0\} \{
    set nn $N; set N \{\}
    foreach g $gg \{
      foreach n $nn \{
        set gn [ApplyPerm $g $n]
        if \{![info exists G($gn)]\} \\
          \{lappend N $gn; set G($gn) 1\}
      \}
    \}
  \}
  return [lsort [array names G]]
\}

#
#  Perform a function on each element of a group
#
proc Action \{G f \{V \"\"\}\} \{
  set A \{\}
  foreach g $G \{
    if \{$V != \"\"\} \{set g [ApplyPerm $g $V]\}
    lappend A [uplevel [list $f $g]]
  \}
  return $A
\}

##################################################

Window Controls -rows \{1 1 1 1\} -columns \{1 0\}
Frame vbox -in Controls -at \{0 0\} \\
  -relief raised -bd 2 \\
  -columns \{1 1 1 1 1 1 1\} -rows \{0 1 0 1\}
Frame vcbox -in vbox -at \{0 4 7 1\} \\
  -columns \{\{0 2\} 1 1 1 \{0 2\}\} -rows \{1 \{0 2\}\}
Frame fbox -in Controls -at \{0 1\} \\
  -relief raised -bd 2 \\
  -columns \{1 \{0 10\}\}  -rows \{0 1 0 1 0 1\}
Frame pbox -in Controls -at \{0 2\} \\
  -relief raised -bd 2 \\
  -columns 1 -rows 1
}
  {selected 1 each}
  {{set z} 0 0 {{{1 0 0} 50 Red} {{1 .5 0} 0     Orange} {{1 1 0} 50 Yellow} {{0 1 0} 50 Green} {{0 1 1} 50 Cyan} {{0 0 1} 50 Blue} {{1 0 1} 50 Purple} {{0 0 0} 0      Black} {{1 1 1} 0      White}} 1}
  {flat 1.0 1 1 0 0 0 0 0 0 0 0 1.0 1}
  {{} {}}
}

####################

::class::Group Create Cartan-M3/6D {
  {##################################################

let VP = \{
  (0,0,0,0,0,0)
  (1,0,0,0,0,0)
  (0,1,0,0,0,0)
  (0,0,1,0,0,0)
  (0,0,0,1,0,0)
  (0,0,0,0,1,0)
  (0,0,0,0,0,1)
\}

set Vertices \{\}

proc MakeVertices \{\} \{
  variables Vertices VP s
  set Vertices \{\}
  let ss = (1+s)/2
  foreach j \{1 2 3 4 5 6\} \{
    foreach k \{1 2 3 4 5 6\} \{
      if \{$j != $k\} \{
	let (a,b) = VP@(j,k)
        let P = ss a - (1-ss) b
        lappend Vertices \"p$j$k:\" $P
      \}
    \}
  \}
\}

##################################################

let aa = 13/3
let bb = 7/3

set VS \{
  p12: (-1,-1,1)
  p13: (1,-1,-1)
  p14: (-1,1,-1)
  p15: (1,1,1)
  p16: (.125,.25,.25)*0

  p21: (1,1,-1)*aa
  p23: (3,-1,-3)
  p24: (-1,3,-3)
  p25: (3,3,1)
  p26: (1,1,-1)*bb

  p31: (-1,1,1)*aa
  p32: (-3,-1,3)
  p34: (-3,3,-1)
  p35: (1,3,3)
  p36: (-1,1,1)*bb

  p41: (1,-1,1)*aa
  p42: (-1,-3,3)
  p43: (3,-3,-1)
  p45: (3,1,3)
  p46: (1,-1,1)*bb

  p51: (-1,-1,-1)*aa
  p52: (-3,-3,1)
  p53: (1,-3,-3)
  p54: (-3,1,-3)
  p56: (-1,-1,-1)*bb

  p61: (.125,-.25,-.25)*0
  p62: (-2,-2,2)
  p63: (2,-2,-2)
  p64: (-2,2,-2)
  p65: (2,2,2)
\}

##################################################

let r = 0

Setup \{
  if \{$schematic\} \{
    set Vertices $VS
    let r = .075
  \} else \{
    MakeVertices
    let r = .0125
  \}
  set M $MM
  if \{[llength [lindex $MM 0]] > 1\} \{
    set M [lindex $MM 0]
    foreach m [lrange $MM 1 end] \{let M = M * m\}
  \}
\}

##################################################

CheckBox schematic 0 -in vcbox -at \{2 0\} \\
  -title \"Schematic layout\"

Slider s -1 1 0 -title \"Position of Slice\" \\
  -in Controls -at \{0 3\}

##################################################

vproc XU \{a\} \{Rotate \{1 0 0 0 0 0\} \{0 0 0 1 0 0\} $a\}
vproc YU \{a\} \{Rotate \{0 1 0 0 0 0\} \{0 0 0 1 0 0\} $a\}
vproc ZU \{a\} \{Rotate \{0 0 1 0 0 0\} \{0 0 0 1 0 0\} $a\}
vproc XV \{a\} \{Rotate \{1 0 0 0 0 0\} \{0 0 0 0 1 0\} $a\}
vproc YV \{a\} \{Rotate \{0 1 0 0 0 0\} \{0 0 0 0 1 0\} $a\}
vproc ZV \{a\} \{Rotate \{0 0 1 0 0 0\} \{0 0 0 0 1 0\} $a\}
vproc UV \{a\} \{Rotate \{0 0 0 1 0 0\} \{0 0 0 0 1 0\} $a\}
vproc XW \{a\} \{Rotate \{1 0 0 0 0 0\} \{0 0 0 0 0 1\} $a\}
vproc YW \{a\} \{Rotate \{0 1 0 0 0 0\} \{0 0 0 0 0 1\} $a\}
vproc ZW \{a\} \{Rotate \{0 0 1 0 0 0\} \{0 0 0 0 0 1\} $a\}
vproc UW \{a\} \{Rotate \{0 0 0 1 0 0\} \{0 0 0 0 0 1\} $a\}
vproc VW \{a\} \{Rotate \{0 0 0 0 1 0\} \{0 0 0 0 0 1\} $a\}

vproc D \{\{a -acos(1/sqrt(6))\}\} \{
  let a = $a
  Rotate \{1 1 1 1 1 1\} \{0 0 0 0 0 1\} $a
\}

TypeIn MM \"\" -evaluate -in pbox -at \{0 1 2 1\} \\
  -title \"Rotate:\"

Axes \{x y z u v w\} -> \{x y z\}

##################################################
}
  {always 1 each}
  {{set w} 0 0 {{{1 0 0} 50 Red} {{1 .5 0} 0     Orange} {{1 1 0} 50 Yellow} {{0 1 0} 50 Green} {{0 1 1} 50 Cyan} {{0 0 1} 50 Blue} {{1 0 1} 50 Purple} {{0 0 0} 0      Black} {{1 1 1} 0      White}} 1}
  {flat 1.0 1 1 0 0 0 0 0 0 0 0 1.0 1}
  {{} {}}
}

####################

::class::Polyhedron Create Cartan-M3/6D/Edges {
  {##################################################

Vertices \{$Vertices\}

Faces \{
  [if \{$p1\} \{list \{
    \{p12 p13\} \{p12 p14\} \{p12 p15\}
    \{p13 p14\} \{p13 p15\} \{p14 p15\}
    \{p16 p12\} \{p16 p13\} \{p16 p14\} \{p16 p15\}
  \} <- \"color \{[lindex $PC 1]\}\" \}]
  [if \{$p2\} \{list \{
    \{p23 p24\} \{p23 p25\} \{p24 p25\}
    \{p21 p23\} \{p21 p24\} \{p21 p25\}
    \{p26 p21\} \{p26 p23\} \{p26 p24\} \{p26 p25\}
  \} <- \"color \{[lindex $PC 2]\}\" \}]
  [if \{$p3\} \{list \{
    \{p32 p34\} \{p32 p35\} \{p34 p35\}
    \{p31 p32\} \{p31 p34\} \{p31 p35\}
    \{p36 p31\} \{p36 p32\} \{p36 p34\} \{p36 p35\}
  \} <- \"color \{[lindex $PC 3]\}\" \}]
  [if \{$p4\} \{list \{
    \{p42 p43\} \{p42 p45\} \{p43 p45\}
    \{p41 p42\} \{p41 p43\} \{p41 p45\}
    \{p46 p41\} \{p46 p42\} \{p46 p43\} \{p46 p45\}
  \} <- \"color \{[lindex $PC 4]\}\" \}]
  [if \{$p5\} \{list \{
    \{p52 p53\} \{p52 p54\} \{p53 p54\}
    \{p51 p52\} \{p51 p53\} \{p51 p54\}
    \{p56 p51\} \{p56 p52\} \{p56 p53\} \{p56 p54\}
  \} <- \"color \{[lindex $PC 5]\}\" \}]
  [if \{$p6\} \{list \{
    \{p62 p63\} \{p62 p64\} \{p62 p65\}
    \{p63 p64\} \{p63 p65\} \{p64 p65\}
    \{p61 p62\} \{p61 p63\} \{p61 p64\} \{p61 p65\}
  \} <- \"color \{[lindex $PC 6]\}\" \}]

  [if \{$m1\} \{list \{
    \{p21 p31\} \{p21 p41\} \{p21 p51\}
    \{p31 p41\} \{p31 p51\} \{p41 p51\}
    \{p61 p21\} \{p61 p31\} \{p61 p41\} \{p61 p51\}
  \} <- \"color \{[lindex $MC 1]\}\" \}]
  [if \{$m2\} \{list \{
    \{p12 p32\} \{p12 p42\} \{p12 p52\}
    \{p32 p42\} \{p32 p52\} \{p42 p52\}
    \{p62 p12\} \{p62 p32\} \{p62 p42\} \{p62 p52\}
  \} <- \"color \{[lindex $MC 2]\}\" \}]
  [if \{$m3\} \{list \{
    \{p13 p23\} \{p13 p43\} \{p13 p53\}
    \{p23 p43\} \{p23 p53\} \{p43 p53\}
    \{p63 p13\} \{p63 p23\} \{p63 p43\} \{p63 p53\}
  \} <- \"color \{[lindex $MC 3]\}\" \}]
  [if \{$m4\} \{list \{
    \{p14 p24\} \{p14 p34\} \{p14 p54\}
    \{p24 p34\} \{p24 p54\} \{p34 p54\}
    \{p64 p14\} \{p64 p24\} \{p64 p34\} \{p64 p54\}
  \} <- \"color \{[lindex $MC 4]\}\" \}]
  [if \{$m5\} \{list \{
    \{p15 p25\} \{p15 p35\} \{p15 p45\}
    \{p25 p35\} \{p25 p45\} \{p35 p45\}
    \{p65 p15\} \{p65 p25\} \{p65 p35\} \{p65 p45\}
  \} <- \"color \{[lindex $MC 5]\}\" \}]
  [if \{$m6\} \{list \{
    \{p26 p36\} \{p26 p46\} \{p26 p56\}
    \{p36 p46\} \{p36 p56\} \{p46 p56\}
    \{p16 p26\} \{p16 p36\} \{p16 p46\} \{p16 p56\}
  \} <- \"color \{[lindex $MC 6]\}\" \}]
\}

##################################################

set DarkColors \{
  \{0 0 0\}
  \{1 1 1\} \{1 0 0\} \{0 1 0\}
  \{0 0 1\} \{1 1 0\} \{0 0 0\}
\}

set LightColors \{
  \{0 0 0\}
  \{.75 .75 .75\} \{1 .5 .5\} \{.5 1 .5\}
  \{.5 .5 1\} \{1 1 .5\} \{.25 .25 .25\}
\}

Setup \{
  set PC $DarkColors
  if \{$light\} \{set MC $LightColors\} \\
    else \{set MC $DarkColors\}
\}

##################################################

Frame vtitle -in vbox -at \{0 0 7 1\} \\
  -title \"Show Simplex for Vertex:\"
Frame plus -in vbox -at \{0 1\} -title \"  +  \"
CheckBox p1 1 -in vbox -at \{1 1\} -title 1 -fg white
CheckBox p2 1 -in vbox -at \{2 1\} -title 2 -fg red3
CheckBox p3 1 -in vbox -at \{3 1\} -title 3 -fg green3
CheckBox p4 0 -in vbox -at \{4 1\} -title 4 -fg blue3
CheckBox p5 1 -in vbox -at \{5 1\} -title 5 -fg yellow
CheckBox p6 1 -in vbox -at \{6 1\} -title 6 -fg grey30

Frame minus -in vbox -at \{0 3\} -title \"  -  \"
CheckBox m1 1 -in vbox -at \{1 3\} -title 1 -fg white
CheckBox m2 1 -in vbox -at \{2 3\} -title 2 -fg red3
CheckBox m3 1 -in vbox -at \{3 3\} -title 3 -fg green3
CheckBox m4 1 -in vbox -at \{4 3\} -title 4 -fg blue3
CheckBox m5 0 -in vbox -at \{5 3\} -title 5 -fg yellow
CheckBox m6 1 -in vbox -at \{6 3\} -title 6 -fg grey30

CheckBox light 0 -in vcbox -at \{1 0\} \\
  -title \"Lighten negatives\"

##################################################

Button Standard -in  vcbox -at \{3 0\} -command Reset

proc Reset \{\} \{
  variables schematic
  if \{$schematic\} \{
    Self CheckBox \{
      p1 Set 1 0
      p2 Set 1 0
      p3 Set 1 0
      p4 Set 1 0
      p5 Set 1 0
      p6 Set 0 0
      m1 Set 0 0
      m2 Set 1 0
      m3 Set 1 0
      m4 Set 1 0
      m5 Set 1 0
      m6 Set 0 1
    \}
  \} else \{
    Self CheckBox \{
      p1 Set 1 0
      p2 Set 1 0
      p3 Set 1 0
      p4 Set 0 0
      p5 Set 1 0
      p6 Set 1 0
      m1 Set 1 0
      m2 Set 1 0
      m3 Set 1 0
      m4 Set 1 0
      m5 Set 0 0
      m6 Set 1 1
    \}
  \}
\}

##################################################

Transform \\
  \{if !schematic \\
    \{if \{llength(:M:)> 0\} \{Matrix M\}\} \\
    \{Rotate \{1 1 1 1 1 1\} \{0 0 0 0 0 1\} acos(1/sqrt(6))\} \\
  \}

Axes \{x y z u v w\} -> \{x y z\}

##################################################
}
  {hide 1 each}
  {{set w} 0 1 {{{1 0 0} 50 Red} {{1 .5 0} 0     Orange} {{1 1 0} 50 Yellow} {{0 1 0} 50 Green} {{0 1 1} 50 Cyan} {{0 0 1} 50 Blue} {{1 0 1} 50 Purple} {{0 0 0} 0      Black} {{1 1 1} 0      White}} 1}
  {flat 1.0 3 1 0 0 0 0 0 0 0 0 1.0 3}
  {0 {}}
}

####################

::class::Polyhedron Create Cartan-M3/6D/Prisms {
  {##################################################

Vertices \{$Vertices\}
Faces \{$F\}

##################################################

proc Permute \{P X\} \{
  set P [Permutation $P]
  set p \{\}; set i 1
  foreach j $P \{lappend p $i [expr $j + 1]; incr i\}
  return [string map $p $X]
\}

##################################################

proc MakeFaces \{F\} \{
  variables RP2
  set FF \{\}
  foreach fv $F \{
    set f [lindex $fv 0]; set v [lindex $fv 1]
    set isRP2 [info exists RP2($f)]
    if \{$isRP2 || [llength $v] < 3\} \{
      if \{$isRP2\} \{set c \{0 1 1\}\} else \{set c \{.67 .67 .67\}\}
      lappend FF [list [MakePlus $f $v] <- [Color $c]]
    \} else \{
      set isRP2 [info exists RP2($v)]
      if \{$isRP2\} \{set c \{1 0 1\}\} else \{set c \{.67 .67 .67\}\}
      lappend FF [list [MakeMinus $f $v] <- [Color $c]]
    \}
  \}
  return [join $FF]
\}

##################################################

proc MakePlus \{f v\} \{
  set FF \{\}
  foreach i $v \{lappend FF [MakePlusTriangle $f $i]\}
  switch [llength $v] \{
    2 \{
      if \{[llength $f] == 2\} \{
        lappend FF [MakeSquare $f $v]
      \} else \{
        let (a,b) = v
        lappend FF [MakePlusTube $f $a $b]
      \}
    \}
    3 \{
      let (a,b,c) = v
      lappend FF [MakePlusTube $f $a $b]
      lappend FF [MakePlusTube $f $b $c]
      lappend FF [MakePlusTube $f $c $a]
    \}
  \}
  return [join $FF]
\}

##################################################

proc MakeMinus \{f v\} \{
  set FF \{\}
  foreach i $f \{lappend FF [MakeMinusTriangle $i $v]\}
  switch [llength $f] \{
    2 \{
      if \{[llength $v] == 2\} \{
        lappend FF [MakeSquare $f $v]
      \} else \{
        let (a,b) = f
        lappend FF [MakeMinusTube $v $a $b]
      \}
    \}
    3 \{
      let (a,b,c) = f
      lappend FF [MakeMinusTube $v $a $b]
      lappend FF [MakeMinusTube $v $b $c]
      lappend FF [MakeMinusTube $v $c $a]
    \}
  \}
  return [join $FF]
\}

##################################################

proc MakePlusTriangle \{f j\} \{
  set F \{\}
  foreach i $f \{lappend F \"p$i$j\"\}
  return [list $F]
\}

proc MakeMinusTriangle \{i f\} \{
  set F \{\}
  foreach j $f \{lappend F \"p$i$j\"\}
  return [list $F]
\}

##################################################

proc MakeSquare \{f v\} \{
  let (i1,i2) = f
  let (j1,j2) = v
  return [list \"p$i1$j1 p$i1$j2 p$i2$j2 p$i2$j1\"]
\}

##################################################

proc MakePlusTube \{f j1 j2\} \{
  set FF \{\}
  foreach i1 $f i2 [concat [lrange $f 1 end] [lindex $f 0]] \{
    lappend FF \"p$i1$j1 p$i1$j2 p$i2$j2 p$i2$j1\"
  \}
  return $FF
\}

proc MakeMinusTube \{f i1 i2\} \{
  set FF \{\}
  foreach j1 $f j2 [concat [lrange $f 1 end] [lindex $f 0]] \{
    lappend FF \"p$i1$j1 p$i1$j2 p$i2$j2 p$i2$j1\"
  \}
  return $FF
\}

##################################################

proc PermuteFaces \{g\} \{
  variables F colored
  set FF \{\}
  if \{$colored\} \{set cc [Color]\}
  foreach \{ff a c\} $F \{
    set FFF \{\}
    foreach f $ff \{lappend FFF [PermuteFace $f $g]\}
    if \{!$colored\} \{set cc [list color [lindex $c 1]]\}
    lappend FF $FFF <- $cc
  \}
  return $FF
\}

##################################################

proc Color \{\{c \"\"\}\} \{
  variables ci colored
  if \{!$colored && $c != \"\"\} \{return [list color $c]\}
  return [list color [incr ci] normalize]
\}

##################################################

proc PermuteFace \{f g\} \{
  set F \{\}
  foreach v $f \{
    let (p,a,b) = split(v,\"\")
    let (a,b) = (g@(a-1)+1,g@(b-1)+1)
    lappend F \"p$a$b\"
  \}
  return $F
\}

##################################################

proc Complement \{f\} \{
  variables plus minus
  let f = split(f,\"\")
  array set V \{1 1 2 2 3 3 4 4 5 5 6 6\}
  foreach i $f \{unset V($i)\}
  return [join [lsort [array names V]] \"\"]
\}

##################################################

proc CheckCell \{f\} \{
  if \{[string length $f] > 3 || [string length $f] == 0\} \\
    \{Error \"'$f' should be a point, edge or triangle\"\}
  regsub -all \{[1-6]\} $f \{\} F
  if \{$F != \"\"\} \{Error \"'$f' contains illegal vertices '$F'\"\}
\}

##################################################

proc MakePair \{f v\} \{
  variables RP2
  CheckCell $f; CheckCell $v
  set f [lsort [split $f \"\"]];
  set v [lsort [split $v \"\"]]
  if \{[llength $f] + [llength $v] < 4\} \\
    \{Error \"Cell pairs must use at least 4 vertices\"\}
  foreach i $v \{set V($i) 1\}
  foreach i $f \{
    if \{[info exists V($i)]\} \\
      \{Error \"Cell pairs should not contain common vertices\"\}
  \}
  set v [lsort [array names V]]
  return [list $f $v]
\}

##################################################

Setup \{
  set F \{\}
  regsub -all \{(^|\\n) *#[^\\n]*\} $FV \{\} FV
  regsub -all \{/\\*([^*]|\\*[^/])*\\*/\} $FV \{\} FV
  if \{[regexp \{[()]\} $FV]\} \{
    set FV [string map \{( \{( \} ) \{ )\} , \{ , \}\} $FV]
    let FV = $FV
  \}
  foreach fv $FV \{
    if \{[llength $fv] == 1\} \{lappend fv [Complement $fv]\}
    if \{[llength $fv] != 2\} \{Error \"'$fv' should be a pair of cells\"\}
    foreach f [lindex $fv 0] \{
      foreach v [lindex $fv 1] \\
        \{lappend F [MakePair $f $v]\}
    \}
  \}
  set ci 0
  set F [MakeFaces $F]

  set G \{\}
  foreach i \{1 2 3\} g \{\"(123)(456)\" \"(23)(56)\" \"(13562)\"\} \\
    \{if \{[set g$i]\} \{lappend G $g\}\}

  if \{[llength $G] > 0\} \{
    set ci 0
    set F [join [Action [Group $G] PermuteFaces]]
  \}
\}

##################################################

Frame ftitle -in fbox -at \{0 0 2 1\} \\
  -title \"Show Faces Formed by:\"
TypeIn FV \"\" -in fbox -at \{0 1\} \\
  -lines 5 -title \" \"

Frame gtitle -in fbox -at \{0 2 2 1\} \\
  -title \"Apply Action of:\"
Frame gbox -in fbox -at \{0 3 2 1\}
CheckBox g1 0 -in gbox -at \{0 1\} -title \"(123)(456)\"
CheckBox g2 0 -in gbox -at \{1 1\} -title \"(23)(56)\"
CheckBox g3 0 -in gbox -at \{2 1\} -title \"(13562)\"

TypeIn outline \"1/2\" -evaluate -in pbox -at \{0 0\} \\
  -title \"Portion to show:\" -width 8

CheckBox colored 0 -in pbox -at \{1 0\} \\
  -title \"multi-colored\"

##################################################

Transform \{lApply Outline $outline\} \\
  \{if !schematic \\
    \{if \{llength(:M:)> 0\} \{Matrix M\}\} \\
    \{Rotate \{1 1 1 1 1 1\} \{0 0 0 0 0 1\} acos(1/sqrt(6))\} \\
  \}

Axes \{x y z u v w\} -> \{x y z\}

##################################################
}
  {always 1 each}
  {{set w} 0 1 {{{1 0.25098039 0.25098039} 50 Red} {{1 0.56470588 0.25098039} 0 Orange} {{1 1 0.37647059} 50 Yellow} {{0.37647059 1 0.37647059} 50 Green} {{0.37647059 1 1} 50 Cyan} {{0.25098039 0.25098039 1} 50 Blue} {{1 0.25098039 1} 50 Purple} {{0 0 0} 0 Black} {{1 1 1} 0 White}} 0}
  {flat 1.0 3 1 0 0 0 0 0 0 0 0 1.0 3}
  {{} {}}
}

####################

::class::Surface Create Cartan-M3/EdgeTubes {
  {##################################################

Domain \{\{0 2pi 6\} \{0 2n-1 2n-1\}\}

Function \{u v\} \{
  let (x,y,z) = X + r cos(u) U1 + r sin(u) U2
\} \{
  let k = int(v)
  let (a,b,U1,U2,c) = TT@int(k/2)
  if \{$k % 2 == 0\} \{set X $a\} else \{set X $b\}
\}

##################################################

ColorFunction Inherit \{Inherit(v)\}

proc Inherit \{v\} \{
  variables TT
  let c = (TT@int(v/2))@4
  return $c
\}

##################################################

proc TubeData \{E\} \{
  set EE \{\}
  foreach e $E \{
    let ((a,b),c) = e
    let (x,y,z) = b-a
    if \{$x == 0 && $y == 0\} \{
      let U1 = (1,0,0)
      let U2 = (0,1,0)
    \} else \{
      let U1 = Unit(-y,x,0)
      let U2 = Unit(x,y,z) >< U1
    \}
    lappend EE [list $a $b $U1 $U2 $c]
  \}
  return $EE
\}

##################################################

proc GetEdges \{E\} \{
  set EE \{\}
  foreach e $E \{
    set f [lindex $e 0]
    set c [lindex $e 2]
    if \{[llength $f] == 2\} \{lappend EE [list $f $c]\}
  \}
  return $EE
\}

##################################################

Setup \{
  set TT [TubeData [GetEdges [Object GetFullFaces]]]
  set n [llength $TT]
\}

##################################################
}
  {always 1 each}
  {{::_color::Function Inherit} 0 1 {{{1 0 0} 50 Red} {{1 .5 0} 0     Orange} {{1 1 0} 50 Yellow} {{0 1 0} 50 Green} {{0 1 1} 50 Cyan} {{0 0 1} 50 Blue} {{1 0 1} 50 Purple} {{0 0 0} 0      Black} {{1 1 1} 0      White}} 1}
  {smooth 1.0 1 1 0 0 0 0 0 0 0 0 1.0 1}
  {{} 0}
  {::sd::BandsU 1 0 .25}
}

####################

::class::Group Create Cartan-M3/Panels {
  {Frame bbox -in Controls -at \{1 0 1 4\} \\
  -columns \{\{0 8\} 1 \{0 8\}\} -rows \{2 1 1 1 2 1 1 1 1 2 1 2\} \\
  -bg grey80 -relief ridge -bd 4

}
  {always 1 each}
  {{set z} 0 0 {{{1 0 0} 50 Red} {{1 .5 0} 0     Orange} {{1 1 0} 50 Yellow} {{0 1 0} 50 Green} {{0 1 1} 50 Cyan} {{0 0 1} 50 Blue} {{1 0 1} 50 Purple} {{0 0 0} 0      Black} {{1 1 1} 0      White}} 1}
  {flat 1.0 1 1 0 0 0 0 0 0 0 0 1.0 1}
  {{} {}}
}

####################

::class::Group Create Cartan-M3/Panels/CenterStage {
  {Button Quit -command \{exit\} -in bbox -at \{1 10\} -bg grey85

bind all <Control-S><Control-C> \{wm deiconify .\}
wm iconify .

Frame message -in Controls -at \{0 4 2 1\} \\
  -bd 2 -relief ridge -title \"Messages\"

proc ::SetMessage \{self\} \{
  set w [::object::$self Frame message WidgetName]
  set w [grid slaves $w]
  $w configure -font smallfont \\
    -textvariable ::Message::message 
\}

after 10 \{
  ::SetMessage Cartan-M3/Panels/CenterStage
  ::cs::GUI Display Cartan-M3/Panels/Info
  ::cs::GUI Display Cartan-M3/Panels/Samples
  ::cs::GUI Display Cartan-M3
  ::object::Cartan-M3 Update
\}}
  {always 1 each}
  {{set z} 0 0 {{{1 0 0} 50 Red} {{1 .5 0} 0     Orange} {{1 1 0} 50 Yellow} {{0 1 0} 50 Green} {{0 1 1} 50 Cyan} {{0 0 1} 50 Blue} {{1 0 1} 50 Purple} {{0 0 0} 0      Black} {{1 1 1} 0      White}} 1}
  {flat 1.0 1 1 0 0 0 0 0 0 0 0 1.0 1}
  {{} {}}
}

####################

::class::Group Create Cartan-M3/Panels/Help {
  {Window Help -rows \{0 1 0\} -columns 1 -disabled

Frame mbox -in Help -at \{0 0\} -bd 2
Frame dbox -columns \{1 1 1 1 1 1\} \\
  -in Help -at \{0 2\} -bd 4

###############################################

Button Help -in bbox -at \{1 1\} -bg grey85 \\
  -command \{
    Self PopUp Topic Set 1 0
    Self Window Help Disable
    Self Window Help Enable
  \}

###############################################

proc HelpName \{name\} \{
  file join \\
   [file dirname [::cs::File get filename]] \\
   help $name
\}

proc MakeChoices \{Choices\} \{
  variables TopicList TopicValues \\
            TopicFiles TopicLines
  set TopicList \{\}; set TopicValues \{\}
  set TopicFiles \{\}; set TopicLines \{\}; set i 0
  foreach \{title file\} $Choices \{
    if \{$title == \"-\"\} \{
      set TopicLines [concat $i $TopicLines]
    \} else \{
      lappend TopicList $title
      lappend TopicValues [incr i]
      lappend TopicFiles [HelpName $file]
    \}
  \}
\}

proc ::AddSeparators \{w lines\} \{
  set w [$w PopUp Topic WidgetName]
  foreach i $lines \{$w.menu insert $i separator\}
\}

set Topics \{\{(No Help System Found)\} \{help.txt\}\}
catch \{source [HelpName \"help.tcl\"]\}

set TopicList \{\}
set TopicValues \{\}
set TopicFiles \{\}
set TopicLines \{\}

MakeChoices $Topics

###############################################

PopUp Topic $TopicList \\
  -title \"Help on\" \\
  -values $TopicValues \\
  -command \{Select $Topic\}\\
  -in mbox -at \{0 0\}

TypeOut Info -in Help -at \{0 1\} \\
  -title \" \" -lines 20 -width 60

after 100 [list ::AddSeparators [Self] $TopicLines]

###############################################

Button Prev -in dbox -at \{1 0\} -command \{Show -1\}
Button Next -in dbox -at \{2 0\} -command \{Show 1\}

Button Done -in dbox -at \{4 0\} \\
  -command \{Self Window Help Disable\}

###############################################

proc Show \{n\} \{
  variables Topic TopicValues
  set m [llength $TopicValues]
  let n = Max(1,Min(m,Topic+n))
  Self PopUp Topic Set $n 0
\}

proc Select \{n\} \{
  variables Info TopicFiles
  set Info [GetHelp [lindex $TopicFiles [expr \{$n-1\}]]]
  Self Button \{Prev Enable; Next Enable\}
  if \{$n == 1\} \{Self Button Prev Disable\}
  if \{$n == [llength $TopicFiles]\} \\
    \{Self Button Next Disable\}
\}

proc GetHelp \{name\} \{
  if \{[file tail $name] == \"-\"\} \{return \"\"\}
  if \{![file readable $name]\} \\
    \{return \"file:\\n\\n  $name\\n\\nnot found!\"\}
  set hfile [open $name]
  set help [read $hfile]
  close $hfile
  regsub -all \{ *\\\\\\n *\} $help \{ \} help
  return $help
\}

###############################################

}
  {always 1 each}
  {{set z} 0 0 {{{1 0 0} 50 Red} {{1 .5 0} 0     Orange} {{1 1 0} 50 Yellow} {{0 1 0} 50 Green} {{0 1 1} 50 Cyan} {{0 0 1} 50 Blue} {{1 0 1} 50 Purple} {{0 0 0} 0      Black} {{1 1 1} 0      White}} 1}
  {flat 1.0 1 1 0 0 0 0 0 0 0 0 1.0 1}
  {{} {}}
}

####################

::class::Group Create Cartan-M3/Panels/Info {
  {Window Information \\
  -rows \{\{0 10\} 1 0\} -columns \{1 \{0 8\}\} -disabled

Frame dbox -columns \{\{0 20\} 1 2 1 \{0 10\}\} \\
  -in Information -at \{0 2\} -bd 4

###############################################

Button Info -in bbox -at \{1 2\} -bg grey85 \\
  -command \{
    Self Window Information Disable
    Self Window Information Enable
  \}

TypeIn Info \"\" -in Information -at \{0 1\} \\
  -title \" \" -lines 10 -width 45

Button Clear -in dbox -at \{1 0\} \\
  -command \{Self TypeIn Info Set \"\" 0\}

Button Done -in dbox -at \{3 0\} \\
  -command \{Self Window Information Disable\}

###############################################

proc LoadFile \{name\} \{
  variables Info
  if \{$Info == \"\"\} \\
    \{Self Window Information Disable\} else \\
    \{Self Window Information Enable\}
\}


}
  {always 1 each}
  {{set z} 0 0 {{{1 0 0} 50 Red} {{1 .5 0} 0     Orange} {{1 1 0} 50 Yellow} {{0 1 0} 50 Green} {{0 1 1} 50 Cyan} {{0 0 1} 50 Blue} {{1 0 1} 50 Purple} {{0 0 0} 0      Black} {{1 1 1} 0      White}} 1}
  {flat 1.0 1 1 0 0 0 0 0 0 0 0 1.0 1}
  {{} {}}
}

####################

::class::Group Create Cartan-M3/Panels/Load-Save {
  {###############################################

set object \"Cartan-M3\"

set widgets \{
    Cartan-M3/6D TypeIn MM
    Cartan-M3/6D Slider s
    Cartan-M3/6D CheckBox schematic
    Cartan-M3/6D/Prisms TypeIn \{FV outline\}
    Cartan-M3/6D/Prisms CheckBox \{g1 g2 g3 colored\}
    Cartan-M3/6D/Edges CheckBox \{p1 p2 p3 p4 p5 p6\}
    Cartan-M3/6D/Edges CheckBox \{m1 m2 m3 m4 m5 m6\}
    Cartan-M3/6D/Edges CheckBox light
    Cartan-M3/Panels/Info TypeIn Info
\}

set info \"Cartan-M3/Panels/Info\"

###############################################
###############################################

Button Reset  -command \{Reset\}  -in bbox -at \{1 5\} -bg grey85  -title \"New\"
Button Load   -command \{Load\}   -in bbox -at \{1 6\} -bg grey85
Button SaveAs -command \{SaveAs\} -in bbox -at \{1 7\} -bg grey85 -title \"Save As\"
Button Save   -command \{Save\}   -in bbox -at \{1 8\} -bg grey85

###############################################

set defaultName \"Untitled.data\"
set LastLoad $defaultName

set self [namespace current]

proc Save \{\} \{
  variables LastLoad defaultName
  if \{$LastLoad == $defaultName\} \{SaveAs\} \\
    else \{SaveData $LastLoad\}
\}

proc SaveAs \{\} \{
  variables LastLoad self
  _fname(NewFile) \"Save Data As:\" $LastLoad \\
    \"$\{self\}::SaveData %N\" \".data\"
\}

proc Load \{\} \{
  variables LastLoad self
  _fname(OldFile) \"Load Data From:\" $LastLoad \\
  \"$\{self\}::LoadData %N\" \".data\"
\}

proc Reset \{\} \{
  variables LastLoad defaultName info
  set LastLoad $defaultName
  ::object::$info TypeIn Info Set \"\" 0
  object::Cartan-M3/6D/Prisms \{
    CheckBox \{g1 Set 0 0; g2 Set 0 0; g3 Set 0 0\}
    TypeIn FV Set \"\"
  \}
\}

###############################################

proc SaveData \{name\} \{
  variables LastLoad widgets object
  set LastLoad $name
  set outfile [open $name w]
  foreach \{obj type V\} $widgets \{
    foreach v $V \{
      set cmd \"::object::$obj $type $v Get\"
      if \{$type == \"TypeIn\"\} \{
        set w [::object::$obj $type $v WidgetName]
        set cmd \"$w get\"
        if \{[::object:::$obj $type $v get type] == \"text\"\} \\
          \{set cmd \"$w get 0.0 end\"\}
      \}
      puts $outfile \\
        \"catch \{::object::$obj $type $v Set \{[string trim [eval $cmd]]\} 0\}\"
    \}
  \}
  puts $outfile \"after idle \{catch \{\"
  puts $outfile \"  ::cs::GUI Display $object\"
  puts $outfile \"  ::object::$object Update\"
  puts $outfile \"\}\}\"
  close $outfile
\}

###############################################

proc LoadData \{name\} \{
  variables LastLoad info
  set LastLoad $name
  set w \"::object::$info\"
  $w TypeIn Info Set \"\" 0
  source $name
  $w ::oo::Root::Object::RunInit
  $w Run [list LoadFile $name]
\}

###############################################
}
  {hide 1 each}
  {{set z} 0 0 {{{1 0 0} 50 Red} {{1 .5 0} 0     Orange} {{1 1 0} 50 Yellow} {{0 1 0} 50 Green} {{0 1 1} 50 Cyan} {{0 0 1} 50 Blue} {{1 0 1} 50 Purple} {{0 0 0} 0      Black} {{1 1 1} 0      White}} 1}
  {flat 1.0 1 1 0 0 0 0 0 0 0 0 1.0 1}
  {{} {}}
}

####################

::class::Group Create Cartan-M3/Panels/Samples {
  {Window Samples -rows \{0 1 0\} -columns 1 -disabled

Frame mbox -in Samples -at \{0 0\} -bd 2\\
  -columns \{0 \{0 10\} 0\}

Frame dbox -columns \{1 1 1 1 1 1 1 1\} \\
  -in Samples -at \{0 2\} -bd 4

###############################################

Button Samples -in bbox -at \{1 3\} -bg grey85 \\
  -command \{
    Select $Choice
    Self Window Samples Disable
    Self Window Samples Enable
  \}

###############################################

proc SampleData \{file\} \{
  set lines \{\}
  catch \{
    set infile [open $file]
    set lines [read $infile]
    close $infile
  \}
  if \{[regexp \{Info Set \\\{([^\\\}]*)\\\} 0\} $lines match info]\}  \\
    \{return $info\}
  return \"(No description found)\"
\}

proc MakeChoices \{\} \{
  variables ChoiceList ChoiceValues ChoiceData
  set ChoiceList \{\}
  catch \{unset ChoiceData\}
  set i 0
  set pattern \\
    [file join \\
      [file dirname [::cs::File get filename]] \\
      samples *.data \\
    ]
  foreach file [lsort -dictionary [glob -nocomplain $pattern]] \{
    lappend ChoiceList [file root [file tail $file]]
    lappend ChoiceValues [incr i]
    set ChoiceData($i) [list $file [SampleData $file]]
  \}
  if \{[llength $ChoiceList] == 0\} \\
    \{set ChoiceList \{\{(No Samples Found)\}\}\}
\}

set ChoiceList \{\}
set ChoiceValues \{\}
set ChoiceData() \{\}

MakeChoices

###############################################

PopUp Choice $ChoiceList \\
  -title \"Sample:\" \\
  -values $ChoiceValues \\
  -command \{Select $Choice\}\\
  -in mbox -at \{0 0\}

Button Refresh -in mbox -at \{2 0\} \\
  -title \"Update List\" \\
  -command \{Self Define; [Self] PopUp Choice Set 1\}

TypeIn Info \"\" -in Samples -at \{0 1\} \\
  -title \" \" -lines 10 -width 45 -disabled

###############################################

Button Prev -in dbox -at \{1 0\} -command \{Show -1\}
Button Next -in dbox -at \{2 0\} -command \{Show 1\}

Button Load -in dbox -at \{4 0\} -command \{LoadSample\}

Button Done -in dbox -at \{6 0\} \\
  -command \{Self Window Samples Disable\}

###############################################

proc Show \{n\} \{
  variables Choice ChoiceValues
  set m [llength $ChoiceValues]
  let n = Max(1,Min(m,Choice+n))
  Self PopUp Choice Set $n 0
\}

proc Select \{n\} \{
  if \{$n == \"(No Samples Found)\"\} return
  variables ChoiceData ChoiceValues
  Self TypeIn Info Set [lindex $ChoiceData($n) 1] 0
  Self Button \{Prev Enable; Next Enable\}
  if \{$n == 1\} \{Self Button Prev Disable\}
  if \{$n == [llength $ChoiceValues]\} \\
    \{Self Button Next Disable\}
\}

proc LoadSample \{\} \{
  variables Choice ChoiceData
  Sibling Load-Save Run \\
    [list LoadData [lindex $ChoiceData($Choice) 0]]
  Self Window Samples Disable
\}

###############################################}
  {always 1 each}
  {{set z} 0 0 {{{1 0 0} 50 Red} {{1 .5 0} 0     Orange} {{1 1 0} 50 Yellow} {{0 1 0} 50 Green} {{0 1 1} 50 Cyan} {{0 0 1} 50 Blue} {{1 0 1} 50 Purple} {{0 0 0} 0      Black} {{1 1 1} 0      White}} 1}
  {flat 1.0 1 1 0 0 0 0 0 0 0 0 1.0 1}
  {{} {}}
}
