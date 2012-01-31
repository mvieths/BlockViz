#!/bin/perl

open SGADATA, "rawdata_orfs_eps_p.txt" or die "Couldn't open edge file\n";
open OUTFILE, ">filtered.sif" or die "Couldn't open filtered.sif\n";

print "Filtering interactions...\n";

while ($temp = <SGADATA>)
{
	chomp ($temp);
	@line = split(' ', $temp);
	if ($line[2] ne "NaN" && $line[3] ne "NaN")
	{
		if ($line[3] <= 0.05)
		{
			print OUTFILE "$line[0] $line[2] $line[1]\n";
		}
	}
}
