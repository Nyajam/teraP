#!/bin/bash

DEVICE=/dev/sdc
GROUP=agapita-cluster
PATH=/dev/$GROUP/
HELP="help: $0 <Name of new volume, NOT SPACES> <Size in Gygabytes>"
lvcreate=/usr/sbin/lvcreate
pvcreate=/usr/sbin/pvcreate
vgcreate=/usr/sbin/vgcreate

if [ $# -ne 2 ] || [ $1 == "-h" ] || [ $1 == "-help" ] || [ $1 == "--help" ]
then
	echo $HELP
	exit 0
fi

if [ ! -e $PATH ] #Si no hubiera LVM previamente creado
then
	$pvcreate $DEVICE #Crea una unidad para lvm (lo particiona)
	$vgcreate $GROUP $DEVICE #Crea un grupo de volumenes en lvm
fi

if [[ "$2" =~ ^[0-9]+$ ]] && [ ! -e "$GROUP$1" ]
then
	$lvcreate -L $2"G" -n $1 $GROUP #Crea un volumen nuevo con el nombre dado
	exit 0
fi

exit 1
