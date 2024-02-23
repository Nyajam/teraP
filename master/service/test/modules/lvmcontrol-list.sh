#!/bin/bash

DEVICE=/dev/sdc
GROUP=agapita-cluster
PATH=/dev/$GROUP/
HELP="help: $0 -> View of all LVM disk of the group $GROUP, LV is the name of disks and LSize is its size."
lvs=/usr/sbin/lvs

if [ "$1" == "-h" ] || [ "$1" == "-help" ] || [ "$1" == "--help" ]
then
	echo $HELP
	exit 0
fi

lvs -o lv_name,lv_size --configreport vg $GROUP

exit 0
