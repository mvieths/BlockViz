#!/bin/perl

open SGADATA, "rawdata_orfs_edges.txt" or die "Couldn't open edge file\n";

%positive_hash = {};
%negative_hash = {};

$type = "both";
if ($ARGV[0] eq "")
{
	$type = "both";
}
else
{
	$type = "$ARGV[0]";
}

print "Looking for $type interactions...\n";

while ($temp = <SGADATA>)
{
	chomp ($temp);
	@line = split(' ', $temp);
	if ($line[1] ne "NaN")
	{
		if (($type eq "positive")
			|| ($type eq "both"))
		{
			if ($line[1] >= 0)
			{
				$positive_hash{$line[0]} = "$line[2] $positive_hash{$line[0]}";
			}
		}
		if (($type eq "negative")
			|| ($type eq "both"))
		{
			if ($line[1] <= 0)
			{
				$negative_hash{$line[0]} = "$line[2] $negative_hash{$line[0]}";
			}
		}
	}
}

if (($type eq "positive")
	|| ($type eq "both"))
{
	open POSITIVE, ">positive_network.sif" or die "Couldn't open positive outfile\n";
	foreach $key (keys %positive_hash)
	{
		if ($key =~ m/HASH/g)
		{
			print "weird key\n";
		}
		else
		{
			print POSITIVE "$key gg $positive_hash{$key}\n";
		}
	}
}
if (($type eq "negative")
	|| ($type eq "both"))
{
	open NEGATIVE, ">negative_network.sif" or die "Couldn't open negative outfile\n";
	foreach $key (keys %negative_hash)
	{
		if ($key =~ m/HASH/g)
		{
			print "weird key\n";
		}
		else
		{
			print NEGATIVE "$key gg $negative_hash{$key}\n";
		}
	}
}

