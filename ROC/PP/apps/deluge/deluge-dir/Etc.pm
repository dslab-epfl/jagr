
# -------------------------------------------------------------------

use strict;

package Deluge::Etc;
use vars qw($AUTOLOAD);

# -------------------------------------------------------------------
# External Constants

$Deluge::Etc::AgentCode = "Deluge/0.9.11";
$Deluge::Etc::HeaderTag = "X-Deluge-Ref";

# -------------------------------------------------------------------
# Internal Constants

my (@C_IMAGE_FILE_TYPES) = (
							"\.gif",
							"\.ico",
							"\.icon",
							"\.jpe",
							"\.jpeg",
							"\.jpg",
							"\.png",
							"\.swf",
							"\.tga",
							"\.tif",
							"\.tiff",
							"\.xbm",
							"\.xpm",
						   );

# -------------------------------------------------------------------

sub url_is_image
{
	my ($url, $tag) = @_;
	my ($item);

	($tag eq "img") && (return 1);

	foreach $item (@C_IMAGE_FILE_TYPES) {
		($url =~ m|$item$|i) && (return 1);
	}
}

# -------------------------------------------------------------------

sub scheme_is_legal
{
    my ($scheme, $secure_ok) = @_;

	(($scheme eq "http") || ($scheme eq "ftp")) && (return 1);
	($secure_ok) && ($scheme eq "https") && (return 1);
	
	return 0;
}


# -------------------------------------------------------------------

sub min
{
    my ($x, $y) = @_;

	($x < $y) ? (return $x) : (return $y);
}
