#!/bin/sh

for i in success/*.egern; do
    echo $i
    macos/run.sh $i
done
rm ko.s a.out >/dev/null 2>&1
exit 0
