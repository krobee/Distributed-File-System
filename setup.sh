#!/bin/sh

path=$HOME/cs555/PA4
bin=$path/bin
server_list_path=$path/machine-list.txt

for i in `cat $server_list_path`
do
IFS=':' read -r -a array <<< "${i}"
echo 'logging into '${array[0]}
gnome-terminal -x bash -c "ssh -t ${array[0]} 'cd ${bin}; java chunkserver.StartChunkServer ${array[1]}; bash;'" &
done

#java main.Discovery
#java main.PeerNode 8081 -1
#java main.StoreData 8082