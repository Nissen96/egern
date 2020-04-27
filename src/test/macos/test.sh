#!/bin/sh

# Find expected output
filename="${1##*/}"
expected="expected/${filename%.*}"

# Compile and run
macos/compile.sh $1 > ko.nasm || exit 1
nasm -f macho64 ko.nasm || exit 1
ld ko.o -lSystem || exit 1
rm ko.nasm >/dev/null 2>&1
rm ko.o >/dev/null 2>&1
./a.out > result 2>&1 || exit 1
rm a.out >/dev/null 2>&1

# Compare results
diff result $expected >/dev/null 2>&1
if [ $? -gt 0 ]
then
    echo "Expected:"
    cat $expected
    echo "Actual:"
    cat result
    rm result >/dev/null 2>&1
    exit 1
fi

rm result >/dev/null 2>&1
exit 0
