LOCAL_VAR_OFFSET:
        .long   4
FUNCTION_BITMAP_OFFSET:
        .long   3
NUM_PARAMETERS_OFFSET:
        .long   2
NUM_LOCAL_VARS_OFFSET:
        .long   1
STATIC_LINK_OFFSET:
        .long   -2
PARAM_OFFSET:
        .long   -3
PARAMS_IN_REGISTERS:
        .long   6
SIZE_INFO_OFFSET:
        .zero   4
BITMAP_OFFSET:
        .long   1
OBJECT_VTABLE_POINTER_OFFSET:
        .long   2
OBJECT_DATA_OFFSET:
        .long   3
ARRAY_DATA_OFFSET:
        .long   2
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
collect_garbage:
        push    rbp
        mov     rbp, rsp
        mov     QWORD PTR [rbp-8], rdi
        nop
        pop     rbp
        ret
allocate_heap:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 48
        mov     DWORD PTR [rbp-20], edi
        mov     QWORD PTR [rbp-32], rsi
        mov     QWORD PTR [rbp-40], rdx
        mov     DWORD PTR [rbp-24], ecx
        mov     QWORD PTR [rbp-48], r8
        mov     eax, DWORD PTR [rbp-24]
        movsx   rdx, eax
        mov     rax, QWORD PTR [rbp-40]
        add     rax, rdx
        mov     QWORD PTR [rbp-8], rax
        mov     rax, QWORD PTR [rbp-32]
        cmp     rax, QWORD PTR [rbp-8]
        jb      .L4
        mov     rdx, QWORD PTR [rbp-8]
        mov     rax, QWORD PTR [rbp-40]
        mov     rsi, rdx
        mov     rdi, rax
        call    swap
.L4:
        mov     eax, DWORD PTR [rbp-20]
        movsx   rdx, eax
        mov     rax, QWORD PTR [rbp-32]
        lea     rcx, [rdx+rax]
        mov     eax, DWORD PTR [rbp-24]
        movsx   rdx, eax
        mov     rax, QWORD PTR [rbp-40]
        add     rax, rdx
        cmp     rcx, rax
        jbe     .L5
        mov     rax, QWORD PTR [rbp-48]
        mov     rdi, rax
        call    collect_garbage
.L5:
        mov     eax, DWORD PTR [rbp-20]
        movsx   rdx, eax
        mov     rax, QWORD PTR [rbp-32]
        lea     rcx, [rdx+rax]
        mov     eax, DWORD PTR [rbp-24]
        movsx   rdx, eax
        mov     rax, QWORD PTR [rbp-40]
        add     rax, rdx
        cmp     rcx, rax
        jbe     .L6
        mov     rax, -1
        jmp     .L7
.L6:
        mov     rax, QWORD PTR [rbp-32]
.L7:
        leave
        ret