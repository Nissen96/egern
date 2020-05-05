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
set_bitmap:
        push    rbp
        mov     rbp, rsp
        mov     QWORD PTR [rbp-40], rdi
        mov     QWORD PTR [rbp-48], rsi
        mov     rax, QWORD PTR [rbp-40]
        mov     QWORD PTR [rbp-16], rax
        mov     DWORD PTR [rbp-4], 7
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
        mov     eax, DWORD PTR [rbp-4]
        lea     edx, [0+rax*8]
        mov     eax, DWORD PTR [rbp-8]
        add     eax, edx
        cdqe
        lea     rdx, [0+rax*4]
        mov     rax, QWORD PTR [rbp-48]
        add     rdx, rax
        movzx   eax, BYTE PTR [rbp-17]
        mov     DWORD PTR [rdx], eax
        sub     DWORD PTR [rbp-8], 1
.L3:
        cmp     DWORD PTR [rbp-8], 0
        jns     .L4
        sub     DWORD PTR [rbp-4], 1
.L2:
        cmp     DWORD PTR [rbp-4], 0
        jns     .L5
        nop
        nop
        pop     rbp
        ret
.LC0:
        .string "Bitmap:         "
.LC1:
        .string "%d"
print_bitmap:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 32
        mov     DWORD PTR [rbp-20], edi
        mov     QWORD PTR [rbp-32], rsi
        mov     edi, OFFSET FLAT:.LC0
        mov     eax, 0
        call    printf
        mov     DWORD PTR [rbp-4], 0
        jmp     .L7
.L8:
        mov     eax, DWORD PTR [rbp-4]
        cdqe
        lea     rdx, [0+rax*4]
        mov     rax, QWORD PTR [rbp-32]
        add     rax, rdx
        mov     eax, DWORD PTR [rax]
        mov     esi, eax
        mov     edi, OFFSET FLAT:.LC1
        mov     eax, 0
        call    printf
        add     DWORD PTR [rbp-4], 1
.L7:
        mov     eax, DWORD PTR [rbp-4]
        cmp     eax, DWORD PTR [rbp-20]
        jl      .L8
        mov     edi, 10
        call    putchar
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
forward:
        push    rbp
        mov     rbp, rsp
        mov     QWORD PTR [rbp-8], rdi
        nop
        pop     rbp
        ret
.LC2:
        .string "Var %d = %d is pointer\n"
visit_pointer_vars:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 48
        mov     QWORD PTR [rbp-24], rdi
        mov     QWORD PTR [rbp-32], rsi
        mov     QWORD PTR [rbp-40], rdx
        mov     DWORD PTR [rbp-4], 0
        jmp     .L12
.L14:
        mov     eax, DWORD PTR [rbp-4]
        cdqe
        lea     rdx, [0+rax*4]
        mov     rax, QWORD PTR [rbp-32]
        add     rax, rdx
        mov     eax, DWORD PTR [rax]
        mov     DWORD PTR [rbp-8], eax
        cmp     DWORD PTR [rbp-8], 0
        je      .L13
        mov     eax, DWORD PTR [rbp-4]
        cdqe
        sub     rax, QWORD PTR [rbp-24]
        add     rax, 1
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-40]
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        mov     QWORD PTR [rbp-16], rax
        mov     rdx, QWORD PTR [rbp-16]
        mov     eax, DWORD PTR [rbp-4]
        mov     esi, eax
        mov     edi, OFFSET FLAT:.LC2
        mov     eax, 0
        call    printf
        mov     rax, QWORD PTR [rbp-16]
        mov     rdi, rax
        call    forward
.L13:
        add     DWORD PTR [rbp-4], 1
.L12:
        mov     eax, DWORD PTR [rbp-4]
        cdqe
        cmp     QWORD PTR [rbp-24], rax
        jg      .L14
        nop
        nop
        leave
        ret
visit_pointer_params:
        push    rbp
        mov     rbp, rsp
        mov     QWORD PTR [rbp-8], rdi
        mov     QWORD PTR [rbp-16], rsi
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
        push    r15
        push    r14
        push    r13
        push    r12
        push    rbx
        sub     rsp, 376
        mov     QWORD PTR [rbp-376], rdi
        mov     rax, rsp
        mov     rbx, rax
        mov     eax, 1
        sal     rax, 3
        neg     rax
        mov     rdx, rax
        mov     rax, QWORD PTR [rbp-376]
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        mov     QWORD PTR [rbp-56], rax
        mov     eax, 2
        sal     rax, 3
        neg     rax
        mov     rdx, rax
        mov     rax, QWORD PTR [rbp-376]
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        mov     QWORD PTR [rbp-72], rax
        mov     eax, 3
        sal     rax, 3
        neg     rax
        mov     rdx, rax
        mov     rax, QWORD PTR [rbp-376]
        add     rdx, rax
        lea     rax, [rbp-368]
        mov     rsi, rax
        mov     rdi, rdx
        call    set_bitmap
        mov     rax, QWORD PTR [rbp-56]
        mov     rsi, rax
        mov     edi, OFFSET FLAT:.LC3
        mov     eax, 0
        call    printf
        mov     rax, QWORD PTR [rbp-72]
        mov     rsi, rax
        mov     edi, OFFSET FLAT:.LC4
        mov     eax, 0
        call    printf
        mov     rax, QWORD PTR [rbp-56]
        mov     edx, eax
        mov     rax, QWORD PTR [rbp-72]
        add     eax, edx
        mov     edx, eax
        lea     rax, [rbp-368]
        mov     rsi, rax
        mov     edi, edx
        call    print_bitmap
        mov     rax, QWORD PTR [rbp-56]
        lea     rdx, [rax-1]
        mov     QWORD PTR [rbp-80], rdx
        mov     rdx, rax
        mov     QWORD PTR [rbp-400], rdx
        mov     QWORD PTR [rbp-392], 0
        mov     rdx, rax
        mov     QWORD PTR [rbp-416], rdx
        mov     QWORD PTR [rbp-408], 0
        lea     rdx, [0+rax*4]
        mov     eax, 16
        sub     rax, 1
        add     rax, rdx
        mov     esi, 16
        mov     edx, 0
        div     rsi
        imul    rax, rax, 16
        sub     rsp, rax
        mov     rax, rsp
        add     rax, 3
        shr     rax, 2
        sal     rax, 2
        mov     QWORD PTR [rbp-88], rax
        mov     rax, QWORD PTR [rbp-72]
        lea     rdx, [rax-1]
        mov     QWORD PTR [rbp-96], rdx
        mov     rdx, rax
        mov     r14, rdx
        mov     r15d, 0
        mov     rdx, rax
        mov     r12, rdx
        mov     r13d, 0
        lea     rdx, [0+rax*4]
        mov     eax, 16
        sub     rax, 1
        add     rax, rdx
        mov     ecx, 16
        mov     edx, 0
        div     rcx
        imul    rax, rax, 16
        sub     rsp, rax
        mov     rax, rsp
        add     rax, 3
        shr     rax, 2
        sal     rax, 2
        mov     QWORD PTR [rbp-104], rax
        mov     DWORD PTR [rbp-64], 0
        jmp     .L17
.L18:
        mov     eax, DWORD PTR [rbp-64]
        cdqe
        mov     ecx, DWORD PTR [rbp-368+rax*4]
        mov     rax, QWORD PTR [rbp-88]
        mov     edx, DWORD PTR [rbp-64]
        movsx   rdx, edx
        mov     DWORD PTR [rax+rdx*4], ecx
        add     DWORD PTR [rbp-64], 1
.L17:
        mov     eax, DWORD PTR [rbp-64]
        cdqe
        cmp     QWORD PTR [rbp-56], rax
        jg      .L18
        mov     DWORD PTR [rbp-60], 0
        jmp     .L19
.L20:
        mov     eax, DWORD PTR [rbp-64]
        lea     edx, [rax+1]
        mov     DWORD PTR [rbp-64], edx
        cdqe
        mov     ecx, DWORD PTR [rbp-368+rax*4]
        mov     rax, QWORD PTR [rbp-104]
        mov     edx, DWORD PTR [rbp-60]
        movsx   rdx, edx
        mov     DWORD PTR [rax+rdx*4], ecx
        add     DWORD PTR [rbp-60], 1
.L19:
        mov     eax, DWORD PTR [rbp-60]
        cdqe
        cmp     QWORD PTR [rbp-72], rax
        jg      .L20
        mov     eax, 4
        sal     rax, 3
        neg     rax
        mov     rdx, rax
        mov     rax, QWORD PTR [rbp-376]
        add     rdx, rax
        mov     rcx, QWORD PTR [rbp-104]
        mov     rax, QWORD PTR [rbp-72]
        mov     rsi, rcx
        mov     rdi, rax
        call    visit_pointer_vars
        mov     rsp, rbx
        nop
        lea     rsp, [rbp-40]
        pop     rbx
        pop     r12
        pop     r13
        pop     r14
        pop     r15
        pop     rbp
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
        jmp     .L22
.L23:
        mov     rax, QWORD PTR [rbp-8]
        mov     rdi, rax
        call    scan_stack_frame
        mov     rax, QWORD PTR [rbp-8]
        mov     rax, QWORD PTR [rax]
        mov     QWORD PTR [rbp-8], rax
.L22:
        cmp     QWORD PTR [rbp-8], 0
        jne     .L23
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
        sub     rsp, 32
        mov     QWORD PTR [rbp-8], rdi
        mov     QWORD PTR [rbp-16], rsi
        mov     QWORD PTR [rbp-24], rdx
        mov     rax, QWORD PTR heap_pointer[rip]
        mov     QWORD PTR from_space[rip], rax
        mov     rax, QWORD PTR from_space[rip]
        mov     rdx, QWORD PTR [rbp-16]
        sal     rdx, 3
        add     rax, rdx
        mov     QWORD PTR to_space[rip], rax
        mov     rdx, QWORD PTR current_heap_pointer[rip]
        mov     rax, QWORD PTR to_space[rip]
        cmp     rdx, rax
        jb      .L25
        mov     rdx, QWORD PTR to_space[rip]
        mov     rax, QWORD PTR from_space[rip]
        mov     rsi, rdx
        mov     rdi, rax
        call    swap
.L25:
        mov     rax, QWORD PTR current_heap_pointer[rip]
        mov     rdx, QWORD PTR [rbp-8]
        sal     rdx, 3
        lea     rcx, [rax+rdx]
        mov     rax, QWORD PTR from_space[rip]
        mov     rdx, QWORD PTR [rbp-16]
        sal     rdx, 3
        add     rax, rdx
        cmp     rcx, rax
        jbe     .L26
        mov     rax, QWORD PTR [rbp-24]
        mov     rdi, rax
        call    collect_garbage
.L26:
        mov     rax, QWORD PTR current_heap_pointer[rip]
        mov     rdx, QWORD PTR [rbp-8]
        sal     rdx, 3
        lea     rcx, [rax+rdx]
        mov     rax, QWORD PTR from_space[rip]
        mov     rdx, QWORD PTR [rbp-16]
        sal     rdx, 3
        add     rax, rdx
        cmp     rcx, rax
        jbe     .L27
        mov     rax, QWORD PTR stderr[rip]
        mov     rcx, rax
        mov     edx, 14
        mov     esi, 1
        mov     edi, OFFSET FLAT:.LC7
        call    fwrite
        mov     edi, 1
        call    exit
.L27:
        mov     rax, QWORD PTR heap_pointer[rip]
        leave
        ret