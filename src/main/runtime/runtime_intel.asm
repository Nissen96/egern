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
DATA_OFFSET:
        .quad   2
.LC0:
        .string "       FROM-SPACE       TO-SPACE"
.LC1:
        .string "       (%d)      (%d)\n"
.LC2:
        .string "%4d: %10d %15d %10d %10d\n"
.LC3:
        .string "\n"
print_heap:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 16
        mov     edi, OFFSET FLAT:.LC0
        call    puts
        mov     rdx, QWORD PTR to_space[rip]
        mov     rax, QWORD PTR from_space[rip]
        mov     rsi, rax
        mov     edi, OFFSET FLAT:.LC1
        mov     eax, 0
        call    printf
        mov     DWORD PTR [rbp-4], 0
        jmp     .L2
.L3:
        mov     rax, QWORD PTR to_space[rip]
        mov     edx, DWORD PTR [rbp-4]
        movsx   rdx, edx
        sal     rdx, 3
        lea     rdi, [rax+rdx]
        mov     rax, QWORD PTR from_space[rip]
        mov     edx, DWORD PTR [rbp-4]
        movsx   rdx, edx
        sal     rdx, 3
        lea     rsi, [rax+rdx]
        mov     rax, QWORD PTR to_space[rip]
        mov     edx, DWORD PTR [rbp-4]
        movsx   rdx, edx
        sal     rdx, 3
        add     rax, rdx
        mov     rcx, QWORD PTR [rax]
        mov     rax, QWORD PTR from_space[rip]
        mov     edx, DWORD PTR [rbp-4]
        movsx   rdx, edx
        sal     rdx, 3
        add     rax, rdx
        mov     rdx, QWORD PTR [rax]
        mov     eax, DWORD PTR [rbp-4]
        mov     r9, rdi
        mov     r8, rsi
        mov     esi, eax
        mov     edi, OFFSET FLAT:.LC2
        mov     eax, 0
        call    printf
        add     DWORD PTR [rbp-4], 1
.L2:
        mov     eax, DWORD PTR [rbp-4]
        movsx   rdx, eax
        mov     rax, QWORD PTR heap_size[rip]
        cmp     rdx, rax
        jl      .L3
        mov     edi, OFFSET FLAT:.LC3
        call    puts
        nop
        leave
        ret
set_bitmap:
        push    rbp
        mov     rbp, rsp
        mov     QWORD PTR [rbp-40], rdi
        mov     QWORD PTR [rbp-48], rsi
        mov     rax, QWORD PTR [rbp-40]
        mov     QWORD PTR [rbp-16], rax
        mov     DWORD PTR [rbp-4], 7
        jmp     .L5
.L8:
        mov     DWORD PTR [rbp-8], 7
        jmp     .L6
.L7:
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
.L6:
        cmp     DWORD PTR [rbp-8], 0
        jns     .L7
        sub     DWORD PTR [rbp-4], 1
.L5:
        cmp     DWORD PTR [rbp-4], 0
        jns     .L8
        nop
        nop
        pop     rbp
        ret
.LC4:
        .string "Bitmap:         "
.LC5:
        .string "%d"
print_bitmap:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 32
        mov     DWORD PTR [rbp-20], edi
        mov     QWORD PTR [rbp-32], rsi
        mov     edi, OFFSET FLAT:.LC4
        mov     eax, 0
        call    printf
        mov     DWORD PTR [rbp-4], 0
        jmp     .L10
.L11:
        mov     eax, DWORD PTR [rbp-4]
        cdqe
        lea     rdx, [0+rax*4]
        mov     rax, QWORD PTR [rbp-32]
        add     rax, rdx
        mov     eax, DWORD PTR [rax]
        mov     esi, eax
        mov     edi, OFFSET FLAT:.LC5
        mov     eax, 0
        call    printf
        add     DWORD PTR [rbp-4], 1
.L10:
        mov     eax, DWORD PTR [rbp-4]
        cmp     eax, DWORD PTR [rbp-20]
        jl      .L11
        mov     edi, 10
        call    putchar
        nop
        leave
        ret
swap_spaces:
        push    rbp
        mov     rbp, rsp
        mov     rax, QWORD PTR to_space[rip]
        mov     QWORD PTR [rbp-8], rax
        mov     rax, QWORD PTR from_space[rip]
        mov     QWORD PTR to_space[rip], rax
        mov     rax, QWORD PTR [rbp-8]
        mov     QWORD PTR from_space[rip], rax
        nop
        pop     rbp
        ret
.LC6:
        .string "Index %d: %d\n"
check_pointer:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 32
        mov     QWORD PTR [rbp-24], rdi
        mov     DWORD PTR [rbp-4], -16
        jmp     .L14
.L15:
        mov     eax, DWORD PTR [rbp-4]
        cdqe
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-24]
        add     rax, rdx
        mov     rdx, QWORD PTR [rax]
        mov     eax, DWORD PTR [rbp-4]
        mov     esi, eax
        mov     edi, OFFSET FLAT:.LC6
        mov     eax, 0
        call    printf
        add     DWORD PTR [rbp-4], 1
.L14:
        cmp     DWORD PTR [rbp-4], 15
        jle     .L15
        nop
        nop
        leave
        ret
in_to_space:
        push    rbp
        mov     rbp, rsp
        mov     QWORD PTR [rbp-8], rdi
        cmp     QWORD PTR [rbp-8], 0
        je      .L17
        mov     rax, QWORD PTR to_space[rip]
        cmp     QWORD PTR [rbp-8], rax
        jb      .L17
        mov     rax, QWORD PTR to_space[rip]
        mov     rdx, QWORD PTR heap_size[rip]
        sal     rdx, 3
        add     rax, rdx
        cmp     QWORD PTR [rbp-8], rax
        jnb     .L17
        mov     eax, 1
        jmp     .L19
.L17:
        mov     eax, 0
.L19:
        pop     rbp
        ret
in_from_space:
        push    rbp
        mov     rbp, rsp
        mov     QWORD PTR [rbp-8], rdi
        cmp     QWORD PTR [rbp-8], 0
        je      .L21
        mov     rax, QWORD PTR from_space[rip]
        cmp     QWORD PTR [rbp-8], rax
        jb      .L21
        mov     rax, QWORD PTR from_space[rip]
        mov     rdx, QWORD PTR heap_size[rip]
        sal     rdx, 3
        add     rax, rdx
        cmp     QWORD PTR [rbp-8], rax
        jnb     .L21
        mov     eax, 1
        jmp     .L23
.L21:
        mov     eax, 0
.L23:
        pop     rbp
        ret
get_pointer_field:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 320
        mov     QWORD PTR [rbp-312], rdi
        mov     DWORD PTR [rbp-316], esi
        mov     eax, 2
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-312]
        add     rax, rdx
        mov     QWORD PTR [rbp-16], rax
        mov     eax, 0
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-312]
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        mov     QWORD PTR [rbp-24], rax
        mov     eax, 1
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-312]
        add     rdx, rax
        lea     rax, [rbp-304]
        mov     rsi, rax
        mov     rdi, rdx
        call    set_bitmap
        mov     DWORD PTR [rbp-4], 0
        mov     DWORD PTR [rbp-8], 0
        jmp     .L25
.L29:
        mov     eax, DWORD PTR [rbp-8]
        cdqe
        mov     rdx, QWORD PTR [rbp-24]
        sub     rdx, rax
        mov     rax, rdx
        sub     rax, 1
        mov     eax, DWORD PTR [rbp-304+rax*4]
        mov     DWORD PTR [rbp-28], eax
        cmp     DWORD PTR [rbp-28], 0
        je      .L26
        mov     eax, DWORD PTR [rbp-8]
        cdqe
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-16]
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        mov     QWORD PTR [rbp-40], rax
        mov     eax, DWORD PTR [rbp-4]
        cmp     eax, DWORD PTR [rbp-316]
        jne     .L27
        mov     rax, QWORD PTR [rbp-40]
        jmp     .L30
.L27:
        add     DWORD PTR [rbp-4], 1
.L26:
        add     DWORD PTR [rbp-8], 1
.L25:
        mov     eax, DWORD PTR [rbp-8]
        cdqe
        cmp     QWORD PTR [rbp-24], rax
        jg      .L29
        mov     eax, 0
.L30:
        leave
        ret
forward:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 288
        mov     QWORD PTR [rbp-280], rdi
        mov     rax, QWORD PTR [rbp-280]
        mov     rdi, rax
        call    in_to_space
        test    eax, eax
        je      .L32
        mov     rax, QWORD PTR [rbp-280]
        jmp     .L35
.L32:
        mov     eax, 0
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-280]
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        mov     DWORD PTR [rbp-4], eax
        mov     eax, 1
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-280]
        add     rdx, rax
        lea     rax, [rbp-272]
        mov     rsi, rax
        mov     rdi, rdx
        call    set_bitmap
        mov     rax, QWORD PTR [rbp-280]
        mov     esi, 0
        mov     rdi, rax
        call    get_pointer_field
        mov     QWORD PTR [rbp-16], rax
        mov     rax, QWORD PTR [rbp-16]
        mov     rdi, rax
        call    in_to_space
        test    eax, eax
        je      .L34
        mov     rax, QWORD PTR [rbp-16]
        jmp     .L35
.L34:
        mov     eax, DWORD PTR [rbp-4]
        cdqe
        mov     edx, 2
        add     rax, rdx
        sal     rax, 3
        mov     rdx, rax
        mov     rax, QWORD PTR current_to_space_pointer[rip]
        mov     rcx, QWORD PTR [rbp-280]
        mov     rsi, rcx
        mov     rdi, rax
        call    memcpy
        mov     rdx, QWORD PTR current_to_space_pointer[rip]
        mov     eax, 2
        lea     rcx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-280]
        add     rax, rcx
        mov     QWORD PTR [rax], rdx
        mov     rax, QWORD PTR current_to_space_pointer[rip]
        mov     edx, DWORD PTR [rbp-4]
        movsx   rdx, edx
        mov     ecx, 2
        add     rdx, rcx
        sal     rdx, 3
        add     rax, rdx
        mov     QWORD PTR current_to_space_pointer[rip], rax
        mov     eax, 2
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-280]
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
.L35:
        leave
        ret
visit_pointer_vars:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 320
        mov     QWORD PTR [rbp-296], rdi
        mov     QWORD PTR [rbp-304], rsi
        mov     QWORD PTR [rbp-312], rdx
        mov     DWORD PTR [rbp-4], 0
        jmp     .L37
.L41:
        mov     eax, DWORD PTR [rbp-4]
        cdqe
        mov     rdx, QWORD PTR [rbp-296]
        sub     rdx, rax
        mov     rax, rdx
        sal     rax, 2
        lea     rdx, [rax-4]
        mov     rax, QWORD PTR [rbp-304]
        add     rax, rdx
        mov     eax, DWORD PTR [rax]
        mov     DWORD PTR [rbp-16], eax
        cmp     DWORD PTR [rbp-16], 0
        je      .L38
        mov     eax, DWORD PTR [rbp-4]
        neg     eax
        cdqe
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-312]
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        mov     QWORD PTR [rbp-24], rax
        mov     rax, QWORD PTR [rbp-24]
        mov     rdi, rax
        call    in_from_space
        test    eax, eax
        je      .L47
        mov     rax, QWORD PTR [rbp-24]
        mov     rdi, rax
        call    forward
        mov     edx, DWORD PTR [rbp-4]
        neg     edx
        movsx   rdx, edx
        lea     rcx, [0+rdx*8]
        mov     rdx, QWORD PTR [rbp-312]
        add     rdx, rcx
        mov     QWORD PTR [rdx], rax
.L38:
        add     DWORD PTR [rbp-4], 1
.L37:
        mov     eax, DWORD PTR [rbp-4]
        cdqe
        cmp     QWORD PTR [rbp-296], rax
        jg      .L41
        jmp     .L42
.L46:
        mov     rax, QWORD PTR scan[rip]
        mov     edx, 1
        sal     rdx, 3
        add     rdx, rax
        lea     rax, [rbp-288]
        mov     rsi, rax
        mov     rdi, rdx
        call    set_bitmap
        mov     rax, QWORD PTR scan[rip]
        mov     edx, 0
        sal     rdx, 3
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        mov     QWORD PTR [rbp-296], rax
        mov     DWORD PTR [rbp-8], 0
        jmp     .L43
.L45:
        mov     eax, DWORD PTR [rbp-8]
        cdqe
        mov     eax, DWORD PTR [rbp-288+rax*4]
        mov     DWORD PTR [rbp-12], eax
        cmp     DWORD PTR [rbp-12], 0
        je      .L44
        mov     rax, QWORD PTR scan[rip]
        mov     edx, DWORD PTR [rbp-8]
        movsx   rdx, edx
        mov     ecx, 2
        add     rdx, rcx
        sal     rdx, 3
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        mov     rdi, rax
        call    forward
        mov     rdx, QWORD PTR scan[rip]
        mov     ecx, DWORD PTR [rbp-8]
        movsx   rcx, ecx
        mov     esi, 2
        add     rcx, rsi
        sal     rcx, 3
        add     rdx, rcx
        mov     QWORD PTR [rdx], rax
.L44:
        add     DWORD PTR [rbp-8], 1
.L43:
        mov     eax, DWORD PTR [rbp-8]
        cdqe
        cmp     QWORD PTR [rbp-296], rax
        jg      .L45
        mov     rax, QWORD PTR scan[rip]
        mov     ecx, 2
        mov     rdx, QWORD PTR [rbp-296]
        add     rdx, rcx
        sal     rdx, 3
        add     rax, rdx
        mov     QWORD PTR scan[rip], rax
.L42:
        mov     rdx, QWORD PTR scan[rip]
        mov     rax, QWORD PTR current_to_space_pointer[rip]
        cmp     rdx, rax
        jb      .L46
        jmp     .L36
.L47:
        nop
.L36:
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
        mov     edi, 16
        mov     edx, 0
        div     rdi
        imul    rax, rax, 16
        sub     rsp, rax
        mov     rax, rsp
        add     rax, 3
        shr     rax, 2
        sal     rax, 2
        mov     QWORD PTR [rbp-104], rax
        mov     DWORD PTR [rbp-64], 0
        jmp     .L50
.L51:
        mov     eax, DWORD PTR [rbp-64]
        cdqe
        mov     ecx, DWORD PTR [rbp-368+rax*4]
        mov     rax, QWORD PTR [rbp-88]
        mov     edx, DWORD PTR [rbp-64]
        movsx   rdx, edx
        mov     DWORD PTR [rax+rdx*4], ecx
        add     DWORD PTR [rbp-64], 1
.L50:
        mov     eax, DWORD PTR [rbp-64]
        cdqe
        cmp     QWORD PTR [rbp-56], rax
        jg      .L51
        mov     DWORD PTR [rbp-60], 0
        jmp     .L52
.L53:
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
.L52:
        mov     eax, DWORD PTR [rbp-60]
        cdqe
        cmp     QWORD PTR [rbp-72], rax
        jg      .L53
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
.LC7:
        .string "Collecting Garbage:"
.LC8:
        .string "RBP: %d\n"
collect_garbage:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 16
        mov     QWORD PTR [rbp-8], rdi
        mov     edi, OFFSET FLAT:.LC7
        call    puts
        mov     rax, QWORD PTR to_space[rip]
        mov     QWORD PTR scan[rip], rax
        jmp     .L55
.L56:
        mov     rax, QWORD PTR [rbp-8]
        mov     rsi, rax
        mov     edi, OFFSET FLAT:.LC8
        mov     eax, 0
        call    printf
        mov     rax, QWORD PTR [rbp-8]
        mov     rdi, rax
        call    scan_stack_frame
        mov     rax, QWORD PTR [rbp-8]
        mov     rax, QWORD PTR [rax]
        mov     QWORD PTR [rbp-8], rax
.L55:
        cmp     QWORD PTR [rbp-8], 0
        jne     .L56
        mov     rax, QWORD PTR heap_size[rip]
        sal     rax, 3
        mov     rdx, rax
        mov     rax, QWORD PTR from_space[rip]
        mov     esi, 0
        mov     rdi, rax
        call    memset
        mov     rax, QWORD PTR current_to_space_pointer[rip]
        mov     QWORD PTR current_heap_pointer[rip], rax
        nop
        leave
        ret
.LC9:
        .string "Out of memory\n"
allocate_heap:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 16
        mov     QWORD PTR [rbp-8], rdi
        mov     QWORD PTR [rbp-16], rsi
        mov     rax, QWORD PTR heap_pointer[rip]
        mov     QWORD PTR from_space[rip], rax
        mov     rax, QWORD PTR from_space[rip]
        mov     rdx, QWORD PTR heap_size[rip]
        sal     rdx, 3
        add     rax, rdx
        mov     QWORD PTR to_space[rip], rax
        mov     rdx, QWORD PTR current_heap_pointer[rip]
        mov     rax, QWORD PTR to_space[rip]
        cmp     rdx, rax
        jb      .L58
        mov     eax, 0
        call    swap_spaces
.L58:
        mov     rax, QWORD PTR to_space[rip]
        mov     QWORD PTR current_to_space_pointer[rip], rax
        mov     eax, 0
        call    print_heap
        mov     rax, QWORD PTR current_heap_pointer[rip]
        mov     rdx, QWORD PTR [rbp-8]
        sal     rdx, 3
        lea     rcx, [rax+rdx]
        mov     rax, QWORD PTR from_space[rip]
        mov     rdx, QWORD PTR heap_size[rip]
        sal     rdx, 3
        add     rax, rdx
        cmp     rcx, rax
        jbe     .L59
        mov     rax, QWORD PTR [rbp-16]
        mov     rdi, rax
        call    collect_garbage
        mov     eax, 0
        call    print_heap
        mov     rax, QWORD PTR current_heap_pointer[rip]
        mov     rdx, QWORD PTR [rbp-8]
        sal     rdx, 3
        lea     rcx, [rax+rdx]
        mov     rax, QWORD PTR to_space[rip]
        mov     rdx, QWORD PTR heap_size[rip]
        sal     rdx, 3
        add     rax, rdx
        cmp     rcx, rax
        jbe     .L59
        mov     rax, QWORD PTR stderr[rip]
        mov     rcx, rax
        mov     edx, 14
        mov     esi, 1
        mov     edi, OFFSET FLAT:.LC9
        call    fwrite
        mov     edi, 1
        call    exit
.L59:
        mov     rax, QWORD PTR current_heap_pointer[rip]
        leave
        ret