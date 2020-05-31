macos/compile.sh $1 > ko.nasm || exit 1
nasm -f macho64 ko.nasm || exit 1
ld ko.o -lSystem || exit 1
rm ko.nasm >/dev/null 2>&1
rm ko.o >/dev/null 2>&1
./a.out || exit 1
rm a.out >/dev/null 2>&1
exit 0