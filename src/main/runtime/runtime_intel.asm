LOCAL_VAR_OFFSET:
        .quad   4
FUNCTION_BITMAP_OFFSET:
        .quad   3
NUM_LOCAL_VARS_OFFSET:
        .quad   2
NUM_PARAMETERS_OFFSET:
        .quad   1
STATIC_LINK_OFFSET:
        .quad   -2
PARAM_OFFSET:
        .quad   -3
PARAMS_IN_REGISTERS:
        .quad   6
SIZE_INFO_OFFSET:
        .zero   8
BITMAP_OFFSET:
        .quad   1
OBJECT_VTABLE_POINTER_OFFSET:
        .quad   2
OBJECT_DATA_OFFSET:
        .quad   3
ARRAY_DATA_OFFSET:
        .quad   2
.LC0:
        .string "Bitmap:         "
.LC1:
        .string "%u"
.LC2:
        .string ""
printBitmap:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 48
        mov     QWORD PTR [rbp-40], rdi
        mov     QWORD PTR [rbp-48], rsi
        mov     rax, QWORD PTR [rbp-48]
        mov     QWORD PTR [rbp-16], rax
        mov     edi, OFFSET FLAT:.LC0
        mov     eax, 0
        call    printf
        mov     rax, QWORD PTR [rbp-40]
        sub     eax, 1
        mov     DWORD PTR [rbp-4], eax
        jmp     .L2
.L5:
        mov     DWORD PTR [rbp-8], 7
        jmp     .L3
.L4:
        mov     eax, DWORD PTR [rbp-4]
        movsx   rdx, eax
        mov     rax, QWORD PTR [rbp-16]
        add     rax, rdx
        movzx   eax, BYTE PTR [rax]
        movzx   edx, al
        mov     eax, DWORD PTR [rbp-8]
        mov     ecx, eax
        sar     edx, cl
        mov     eax, edx
        and     eax, 1
        mov     BYTE PTR [rbp-17], al
        movzx   eax, BYTE PTR [rbp-17]
        mov     esi, eax
        mov     edi, OFFSET FLAT:.LC1
        mov     eax, 0
        call    printf
        sub     DWORD PTR [rbp-8], 1
.L3:
        cmp     DWORD PTR [rbp-8], 0
        jns     .L4
        sub     DWORD PTR [rbp-4], 1
.L2:
        cmp     DWORD PTR [rbp-4], 0
        jns     .L5
        mov     edi, OFFSET FLAT:.LC2
        call    puts
        nop
        leave
        ret
swap:
        push    rbp
        mov     rbp, rsp
        mov     QWORD PTR [rbp-24], rdi
        mov     QWORD PTR [rbp-32], rsi
        mov     rax, QWORD PTR [rbp-24]
        mov     QWORD PTR [rbp-8], rax
        mov     rax, QWORD PTR [rbp-32]
        mov     QWORD PTR [rbp-24], rax
        mov     rax, QWORD PTR [rbp-8]
        mov     QWORD PTR [rbp-32], rax
        nop
        pop     rbp
        ret
.LC3:
        .string "Num parameters: %d\n"
.LC4:
        .string "Num variables:  %d\n"
scan_stack_frame:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 16
        mov     QWORD PTR [rbp-8], rdi
        mov     eax, 1
        sal     rax, 3
        neg     rax
        mov     rdx, rax
        mov     rax, QWORD PTR [rbp-8]
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        mov     rsi, rax
        mov     edi, OFFSET FLAT:.LC3
        mov     eax, 0
        call    printf
        mov     eax, 2
        sal     rax, 3
        neg     rax
        mov     rdx, rax
        mov     rax, QWORD PTR [rbp-8]
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        mov     rsi, rax
        mov     edi, OFFSET FLAT:.LC4
        mov     eax, 0
        call    printf
        mov     eax, 3
        sal     rax, 3
        neg     rax
        mov     rdx, rax
        mov     rax, QWORD PTR [rbp-8]
        add     rax, rdx
        mov     rsi, rax
        mov     edi, 8
        call    printBitmap
        nop
        leave
        ret
.LC5:
        .string "Collecting Garbage:"
.LC6:
        .string "Done!"
collect_garbage:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 16
        mov     QWORD PTR [rbp-8], rdi
        mov     edi, OFFSET FLAT:.LC5
        call    puts
        jmp     .L9
.L10:
        mov     rax, QWORD PTR [rbp-8]
        mov     rdi, rax
        call    scan_stack_frame
        mov     rax, QWORD PTR [rbp-8]
        mov     rax, QWORD PTR [rax]
        mov     QWORD PTR [rbp-8], rax
.L9:
        cmp     QWORD PTR [rbp-8], 0
        jne     .L10
        mov     edi, OFFSET FLAT:.LC6
        call    puts
        nop
        leave
        ret
.LC7:
        .string "Out of memory\n"
allocate_heap:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 64
        mov     QWORD PTR [rbp-24], rdi
        mov     QWORD PTR [rbp-32], rsi
        mov     QWORD PTR [rbp-40], rdx
        mov     QWORD PTR [rbp-48], rcx
        mov     QWORD PTR [rbp-56], r8
        mov     rax, QWORD PTR [rbp-24]
        shr     rax, 3
        mov     QWORD PTR [rbp-24], rax
        mov     rax, QWORD PTR [rbp-48]
        shr     rax, 3
        mov     QWORD PTR [rbp-48], rax
        mov     rax, QWORD PTR [rbp-48]
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-40]
        add     rax, rdx
        mov     QWORD PTR [rbp-8], rax
        mov     rax, QWORD PTR [rbp-32]
        cmp     rax, QWORD PTR [rbp-8]
        jb      .L12
        mov     rdx, QWORD PTR [rbp-8]
        mov     rax, QWORD PTR [rbp-40]
        mov     rsi, rdx
        mov     rdi, rax
        call    swap
.L12:
        mov     rax, QWORD PTR [rbp-24]
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-32]
        add     rdx, rax
        mov     rax, QWORD PTR [rbp-48]
        lea     rcx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-40]
        add     rax, rcx
        cmp     rdx, rax
        jbe     .L13
        mov     rax, QWORD PTR [rbp-56]
        mov     rdi, rax
        call    collect_garbage
.L13:
        mov     rax, QWORD PTR [rbp-24]
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-32]
        add     rdx, rax
        mov     rax, QWORD PTR [rbp-48]
        lea     rcx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-40]
        add     rax, rcx
        cmp     rdx, rax
        jbe     .L14
        mov     rax, QWORD PTR stderr[rip]
        mov     rcx, rax
        mov     edx, 14
        mov     esi, 1
        mov     edi, OFFSET FLAT:.LC7
        call    fwrite
        mov     edi, 1
        call    exit
.L14:
        mov     rax, QWORD PTR [rbp-32]
        leave
        ret