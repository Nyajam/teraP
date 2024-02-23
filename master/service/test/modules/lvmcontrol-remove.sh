#!/bin/bash

DEVICE=/dev/sdc
GROUP=agapita-cluster
PATH=/dev/$GROUP/
HELP="help: $0 <Name of new volume, NOT SPACES>"
lvremove=/usr/sbin/lvremove

if [ $# -ne 1 ] || [ $1 == "-h" ] || [ $1 == "-help" ] || [ $1 == "--help" ]
then
	echo $HELP
	exit 0
fi

if [ ! -e "$GROUP$1" ]
then
	$lvremove -f $PATH$1
	exit 0
fi

exit 1
