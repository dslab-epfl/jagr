#!/bin/sh

if [ -z $1 ]; then
	echo "Usage : autobrick.sh {smallest id of bricks} {Number of bricks to run}"
	exit 1
fi;

if [ -z $2 ]; then
	echo "Usage : autobrick.sh {smallest id of bricks} {Number of bricks to run}"
	exit 1
fi;

cnt=$1
num=$2
num=$((cnt+num))
until [ $cnt == $num ] 
do
	./brick.sh $cnt&
	cnt=$((cnt + 1))
done
