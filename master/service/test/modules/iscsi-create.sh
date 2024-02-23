#!/bin/bash

#targetcli <internal path> <command> <arguments...>
targetcli=/bin/targetcli
PATH_IQN="/iscsi/iqn.2020-11.agapita:nodes.$1/"
PATH_IQN_LUNS=$PATH_IQN"tpg1/luns"
PATH_ISCSI_DISKS=/backstores/block/

#Ayuda
if [ $# -ne 2 ] || [ $1 == "-h" ] || [ $1 == "-help" ] || [ $1 == "--help" ]
then
	echo "help: $0 <Name of conection> <Name of LVM disk>"
	exit 0
fi

#Asegurar que exista la iqn
if [ "$($targetcli $PATH_IQN ls)" == "" ]
then
	echo "This conection has not exist $1"
	exit 1
fi

#Asegurar que existe el lvm (el disco)
if [ ! -e $2 ]
then
	echo "The disk $2 has not exist (in LVM group)"
	exit 2
fi

#Asegurar que el disco no este creado en iSCSI
if [ "$($targetcli $PATH_ISCSI_DISKS ls | grep "$2")" != "" ]
then
	echo "This disk is in use (in iSCSI)"
	exit 3
fi

disk=$(echo $2 | rev | cut -d'/' -f1 | rev)

$targetcli $PATH_ISCSI_DISKS create $disk $2
if [ $? -ne 0 ]
then
	exit 4
fi
$targetcli $PATH_IQN_LUNS create $PATH_ISCSI_DISKS$disk
if [ $? -ne 0 ]
then
	$targetcli $PATH_ISCSI_DISKS delete $disk #Si sale mal, se deja como estaba
	exit 4
fi

echo "Disk has create successful"
exit 0
