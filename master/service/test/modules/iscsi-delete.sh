#!/bin/bash

#targetcli <internal path> <command> <arguments...>
targetcli=/bin/targetcli
iscsi_list=/sbin/agapita-iscsi-list.py
PATH_IQN="/iscsi/iqn.2020-11.agapita:nodes."
PATH_IQN_LUNS="/tpg1/luns"
PATH_ISCSI_DISKS=/backstores/block/

if [ $# -ne 1 ] || [ $1 == "-h" ] || [ $1 == "-help" ] || [ $1 == "--help" ]
then
        echo "help: $0 <Name of LVM disk>"
        exit 0
fi

target=$($iscsi_list | grep $1 | cut -d'        ' -f1)

#Asegurar que existe el disco en iSCSI
if [ "$target" == "" ]
then
        echo "The disk $1 has not exist in iscsi"
        exit 1
fi

$targetcli "$PATH_IQN$target$PATH_IQN_LUNS" delete $($targetcli "$PATH_IQN$target$PATH_IQN_LUNS" ls | grep "$1" | cut -d' ' -f4)
if [ $? -ne 0 ]
then
        exit 2
fi
$targetcli $PATH_ISCSI_DISKS delete $(echo $1 | rev | cut -d'/' -f1 | rev)
if [ $? -ne 0 ]
then
        $targetcli "$PATH_IQN$target$PATH_IQN_LUNS" create $PATH_ISCSI_DISKS$1 #Si sale mal, se deja como estaba
        exit 2
fi

echo "Disk has remove successful"
exit 0