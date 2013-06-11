#! /bin/bash
# Pack a release for distribution

if [ "$1/A" == "/A" ]; then
    echo usage: distro.sh RELEASE-ID
    exit 1
fi

RELEASE="/soi/release/$1"

if [ -d $RELEASE ]; then
    cd $RELEASE
    7z a -tzip soi.zip ./bin ./etc ./lib ./sql ./local
    cd $RELEASE/soi
    7z a -tzip ../soi.$1.zip *
else
    echo "Error could not locate [$RELEASE]"
    exit 1
fi

