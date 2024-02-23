#!/bin/bash

orgDir=$(pwd)
retorno=0

cd master/webPanel/terapweb
./mvnw clean package

if [ $? -ne 0 ]
then
    echo "ERROR en el jar del panel web"
    retorno=$((retorno+1))
fi

exit $retorno