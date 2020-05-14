LOCAL_VAR_OFFSET:
        .quad   4
FUNCTION_BITMAP_OFFSET:
        .quad   3
NUM_LOCAL_VARS_OFFSET:
        .quad   2
NUM_PARAMETERS_OFFSET:
        .quad   1
PARAM_OFFSET:
        .quad   -3
SIZE_INFO_OFFSET:
        .zero   8
BITMAP_OFFSET:
        .quad   1
DATA_OFFSET:
        .quad   2
PARAMS_IN_REGISTERS:
        .quad   6
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
min:
        push    rbp
        mov     rbp, rsp
        mov     DWORD PTR [rbp-4], edi
        mov     DWORD PTR [rbp-8], esi
        mov     eax, DWORD PTR [rbp-4]
        cmp     eax, DWORD PTR [rbp-8]
        jg      .L14
        mov     eax, DWORD PTR [rbp-4]
        jmp     .L15
.L14:
        mov     eax, DWORD PTR [rbp-8]
.L15:
        pop     rbp
        ret
max:
        push    rbp
        mov     rbp, rsp
        mov     DWORD PTR [rbp-4], edi
        mov     DWORD PTR [rbp-8], esi
        mov     eax, DWORD PTR [rbp-4]
        cmp     eax, DWORD PTR [rbp-8]
        jl      .L17
        mov     eax, DWORD PTR [rbp-4]
        jmp     .L18
.L17:
        mov     eax, DWORD PTR [rbp-8]
.L18:
        pop     rbp
        ret
.LC6:
        .string "%d: %d\n"
check_pointer:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 32
        mov     QWORD PTR [rbp-24], rdi
        mov     DWORD PTR [rbp-4], -24
        jmp     .L20
.L23:
        cmp     DWORD PTR [rbp-4], 0
        jne     .L21
        mov     edi, 10
        call    putchar
.L21:
        mov     eax, DWORD PTR [rbp-4]
        cdqe
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-24]
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        mov     edx, DWORD PTR [rbp-4]
        movsx   rdx, edx
        lea     rcx, [0+rdx*8]
        mov     rdx, QWORD PTR [rbp-24]
        add     rcx, rdx
        mov     rdx, rax
        mov     rsi, rcx
        mov     edi, OFFSET FLAT:.LC6
        mov     eax, 0
        call    printf
        cmp     DWORD PTR [rbp-4], 0
        jne     .L22
        mov     edi, 10
        call    putchar
.L22:
        add     DWORD PTR [rbp-4], 1
.L20:
        cmp     DWORD PTR [rbp-4], 23
        jle     .L23
        nop
        nop
        leave
        ret
in_to_space:
        push    rbp
        mov     rbp, rsp
        mov     QWORD PTR [rbp-8], rdi
        cmp     QWORD PTR [rbp-8], 0
        je      .L25
        mov     rax, QWORD PTR to_space[rip]
        cmp     QWORD PTR [rbp-8], rax
        jb      .L25
        mov     rax, QWORD PTR to_space[rip]
        mov     rdx, QWORD PTR heap_size[rip]
        sal     rdx, 3
        add     rax, rdx
        cmp     QWORD PTR [rbp-8], rax
        jnb     .L25
        mov     eax, 1
        jmp     .L27
.L25:
        mov     eax, 0
.L27:
        pop     rbp
        ret
in_from_space:
        push    rbp
        mov     rbp, rsp
        mov     QWORD PTR [rbp-8], rdi
        cmp     QWORD PTR [rbp-8], 0
        je      .L29
        mov     rax, QWORD PTR from_space[rip]
        cmp     QWORD PTR [rbp-8], rax
        jb      .L29
        mov     rax, QWORD PTR from_space[rip]
        mov     rdx, QWORD PTR heap_size[rip]
        sal     rdx, 3
        add     rax, rdx
        cmp     QWORD PTR [rbp-8], rax
        jnb     .L29
        mov     eax, 1
        jmp     .L31
.L29:
        mov     eax, 0
.L31:
        pop     rbp
        ret
forward:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 288
        mov     QWORD PTR [rbp-280], rdi
        mov     rax, QWORD PTR [rbp-280]
        mov     rdi, rax
        call    in_from_space
        test    eax, eax
        jne     .L33
        mov     rax, QWORD PTR [rbp-280]
        jmp     .L36
.L33:
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
        mov     eax, 0
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-280]
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        cmp     rax, -1
        sete    al
        movzx   eax, al
        mov     QWORD PTR [rbp-16], rax
        cmp     QWORD PTR [rbp-16], 0
        je      .L35
        mov     eax, 1
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-280]
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        jmp     .L36
.L35:
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
        mov     eax, 0
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-280]
        add     rax, rdx
        mov     QWORD PTR [rax], -1
        mov     rdx, QWORD PTR current_to_space_pointer[rip]
        mov     eax, 1
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
        mov     eax, 1
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-280]
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
.L36:
        leave
        ret
forward_heap_fields:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 272
        jmp     .L38
.L42:
        mov     rax, QWORD PTR scan[rip]
        mov     edx, 1
        sal     rdx, 3
        add     rdx, rax
        lea     rax, [rbp-272]
        mov     rsi, rax
        mov     rdi, rdx
        call    set_bitmap
        mov     rax, QWORD PTR scan[rip]
        mov     edx, 0
        sal     rdx, 3
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        mov     DWORD PTR [rbp-8], eax
        mov     DWORD PTR [rbp-4], 0
        jmp     .L39
.L41:
        mov     eax, DWORD PTR [rbp-4]
        cdqe
        mov     eax, DWORD PTR [rbp-272+rax*4]
        mov     DWORD PTR [rbp-12], eax
        cmp     DWORD PTR [rbp-12], 0
        je      .L40
        mov     rax, QWORD PTR scan[rip]
        mov     edx, DWORD PTR [rbp-4]
        movsx   rdx, edx
        mov     ecx, 2
        add     rdx, rcx
        sal     rdx, 3
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        mov     rdi, rax
        call    forward
        mov     rdx, QWORD PTR scan[rip]
        mov     ecx, DWORD PTR [rbp-4]
        movsx   rcx, ecx
        mov     esi, 2
        add     rcx, rsi
        sal     rcx, 3
        add     rdx, rcx
        mov     QWORD PTR [rdx], rax
.L40:
        add     DWORD PTR [rbp-4], 1
.L39:
        mov     eax, DWORD PTR [rbp-4]
        cmp     eax, DWORD PTR [rbp-8]
        jl      .L41
        mov     rax, QWORD PTR scan[rip]
        mov     edx, DWORD PTR [rbp-8]
        movsx   rdx, edx
        mov     ecx, 2
        add     rdx, rcx
        sal     rdx, 3
        add     rax, rdx
        mov     QWORD PTR scan[rip], rax
.L38:
        mov     rdx, QWORD PTR scan[rip]
        mov     rax, QWORD PTR current_to_space_pointer[rip]
        cmp     rdx, rax
        jb      .L42
        mov     eax, 0
        call    print_heap
        nop
        leave
        ret
visit_register_params:
        push    rbp
        mov     rbp, rsp
        mov     QWORD PTR [rbp-24], rdi
        mov     QWORD PTR [rbp-32], rsi
        mov     DWORD PTR [rbp-4], 0
        jmp     .L44
.L61:
        mov     eax, DWORD PTR [rbp-4]
        cdqe
        mov     rdx, QWORD PTR [rbp-24]
        sub     rdx, rax
        mov     rax, rdx
        sal     rax, 2
        lea     rdx, [rax-4]
        mov     rax, QWORD PTR [rbp-32]
        add     rax, rdx
        mov     eax, DWORD PTR [rax]
        mov     DWORD PTR [rbp-8], eax
        cmp     DWORD PTR [rbp-8], 0
        je      .L62
        cmp     DWORD PTR [rbp-4], 5
        ja      .L46
        mov     eax, DWORD PTR [rbp-4]
        mov     rax, QWORD PTR .L48[0+rax*8]
        jmp     rax
.L48:
        .quad   .L53
        .quad   .L52
        .quad   .L51
        .quad   .L50
        .quad   .L49
        .quad   .L47
.L53:
        movq    %rdi, %rdi
        jmp     .L46
.L52:
        movq    %rsi, %rdi
        jmp     .L46
.L51:
        movq    %rdx, %rdi
        jmp     .L46
.L50:
        movq    %rcx, %rdi
        jmp     .L46
.L49:
        movq    %r8, %rdi
        jmp     .L46
.L47:
        movq    %r9, %rdi
        nop
.L46:
        call    forward
        cmp     DWORD PTR [rbp-4], 5
        ja      .L45
        mov     eax, DWORD PTR [rbp-4]
        mov     rax, QWORD PTR .L55[0+rax*8]
        jmp     rax
.L55:
        .quad   .L60
        .quad   .L59
        .quad   .L58
        .quad   .L57
        .quad   .L56
        .quad   .L54
.L60:
        movq    %rax, %rdi
        jmp     .L45
.L59:
        movq    %rax, %rsi
        jmp     .L45
.L58:
        movq    %rax, %rdx
        jmp     .L45
.L57:
        movq    %rax, %rcx
        jmp     .L45
.L56:
        movq    %rax, %r8
        jmp     .L45
.L54:
        movq    %rax, %r9
        jmp     .L45
.L62:
        nop
.L45:
        add     DWORD PTR [rbp-4], 1
.L44:
        cmp     DWORD PTR [rbp-4], 4
        jle     .L61
        nop
        nop
        pop     rbp
        ret
.LC7:
        .string "Param %d: %d %d\n"
visit_params:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 48
        mov     QWORD PTR [rbp-24], rdi
        mov     QWORD PTR [rbp-32], rsi
        mov     QWORD PTR [rbp-40], rdx
        mov     DWORD PTR [rbp-4], 0
        jmp     .L64
.L66:
        mov     eax, DWORD PTR [rbp-4]
        cdqe
        mov     rdx, QWORD PTR [rbp-24]
        sub     rdx, rax
        mov     rax, rdx
        sal     rax, 2
        lea     rdx, [rax-4]
        mov     rax, QWORD PTR [rbp-32]
        add     rax, rdx
        mov     ecx, DWORD PTR [rax]
        mov     eax, DWORD PTR [rbp-4]
        cdqe
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-40]
        add     rax, rdx
        mov     rdx, QWORD PTR [rax]
        mov     eax, DWORD PTR [rbp-4]
        mov     esi, eax
        mov     edi, OFFSET FLAT:.LC7
        mov     eax, 0
        call    printf
        mov     eax, DWORD PTR [rbp-4]
        cdqe
        mov     rdx, QWORD PTR [rbp-24]
        sub     rdx, rax
        mov     rax, rdx
        sal     rax, 2
        lea     rdx, [rax-4]
        mov     rax, QWORD PTR [rbp-32]
        add     rax, rdx
        mov     eax, DWORD PTR [rax]
        mov     DWORD PTR [rbp-8], eax
        cmp     DWORD PTR [rbp-8], 0
        je      .L65
        mov     eax, DWORD PTR [rbp-4]
        cdqe
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-40]
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        mov     rdi, rax
        call    forward
        mov     edx, DWORD PTR [rbp-4]
        movsx   rdx, edx
        lea     rcx, [0+rdx*8]
        mov     rdx, QWORD PTR [rbp-40]
        add     rdx, rcx
        mov     QWORD PTR [rdx], rax
.L65:
        add     DWORD PTR [rbp-4], 1
.L64:
        mov     eax, DWORD PTR [rbp-4]
        cdqe
        cmp     QWORD PTR [rbp-24], rax
        jg      .L66
        mov     eax, 0
        call    forward_heap_fields
        nop
        leave
        ret
.LC8:
        .string "Var %d: %d %d\n"
visit_variables:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 48
        mov     QWORD PTR [rbp-24], rdi
        mov     QWORD PTR [rbp-32], rsi
        mov     QWORD PTR [rbp-40], rdx
        mov     DWORD PTR [rbp-4], 0
        jmp     .L68
.L72:
        mov     eax, DWORD PTR [rbp-4]
        cdqe
        mov     rdx, QWORD PTR [rbp-24]
        sub     rdx, rax
        mov     rax, rdx
        sal     rax, 2
        lea     rdx, [rax-4]
        mov     rax, QWORD PTR [rbp-32]
        add     rax, rdx
        mov     ecx, DWORD PTR [rax]
        mov     eax, DWORD PTR [rbp-4]
        neg     eax
        cdqe
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-40]
        add     rax, rdx
        mov     rdx, QWORD PTR [rax]
        mov     eax, DWORD PTR [rbp-4]
        mov     esi, eax
        mov     edi, OFFSET FLAT:.LC8
        mov     eax, 0
        call    printf
        mov     eax, DWORD PTR [rbp-4]
        cdqe
        mov     rdx, QWORD PTR [rbp-24]
        sub     rdx, rax
        mov     rax, rdx
        sal     rax, 2
        lea     rdx, [rax-4]
        mov     rax, QWORD PTR [rbp-32]
        add     rax, rdx
        mov     eax, DWORD PTR [rax]
        mov     DWORD PTR [rbp-8], eax
        cmp     DWORD PTR [rbp-8], 0
        je      .L69
        mov     eax, DWORD PTR [rbp-4]
        neg     eax
        cdqe
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-40]
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        mov     QWORD PTR [rbp-16], rax
        mov     rax, QWORD PTR [rbp-16]
        mov     rdi, rax
        call    in_from_space
        test    eax, eax
        je      .L73
        mov     rax, QWORD PTR [rbp-16]
        mov     rdi, rax
        call    forward
        mov     edx, DWORD PTR [rbp-4]
        neg     edx
        movsx   rdx, edx
        lea     rcx, [0+rdx*8]
        mov     rdx, QWORD PTR [rbp-40]
        add     rdx, rcx
        mov     QWORD PTR [rdx], rax
.L69:
        add     DWORD PTR [rbp-4], 1
.L68:
        mov     eax, DWORD PTR [rbp-4]
        cdqe
        cmp     QWORD PTR [rbp-24], rax
        jg      .L72
        jmp     .L71
.L73:
        nop
.L71:
        mov     eax, 0
        call    forward_heap_fields
        nop
        leave
        ret
scan_stack_frame:
        push    rbp
        mov     rbp, rsp
        push    r15
        push    r14
        push    r13
        push    r12
        push    rbx
        sub     rsp, 440
        mov     QWORD PTR [rbp-392], rdi
        mov     QWORD PTR [rbp-400], rsi
        mov     DWORD PTR [rbp-404], edx
        mov     rax, rsp
        mov     rbx, rax
        mov     eax, 1
        mov     rdx, rax
        mov     eax, 0
        sub     rax, rdx
        sal     rax, 3
        mov     rdx, rax
        mov     rax, QWORD PTR [rbp-392]
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        mov     QWORD PTR [rbp-56], rax
        mov     eax, 2
        mov     rdx, rax
        mov     eax, 0
        sub     rax, rdx
        sal     rax, 3
        mov     rdx, rax
        mov     rax, QWORD PTR [rbp-392]
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        mov     QWORD PTR [rbp-72], rax
        mov     eax, 3
        sal     rax, 3
        neg     rax
        mov     rdx, rax
        mov     rax, QWORD PTR [rbp-392]
        add     rdx, rax
        lea     rax, [rbp-384]
        mov     rsi, rax
        mov     rdi, rdx
        call    set_bitmap
        mov     eax, 6
        mov     edx, eax
        mov     rax, QWORD PTR [rbp-56]
        mov     esi, edx
        mov     edi, eax
        call    min
        mov     DWORD PTR [rbp-76], eax
        mov     rax, QWORD PTR [rbp-56]
        mov     edx, eax
        mov     eax, 6
        sub     edx, eax
        mov     eax, edx
        mov     esi, 0
        mov     edi, eax
        call    max
        mov     DWORD PTR [rbp-80], eax
        mov     edx, DWORD PTR [rbp-76]
        movsx   rax, edx
        sub     rax, 1
        mov     QWORD PTR [rbp-88], rax
        movsx   rax, edx
        mov     QWORD PTR [rbp-432], rax
        mov     QWORD PTR [rbp-424], 0
        movsx   rax, edx
        mov     QWORD PTR [rbp-448], rax
        mov     QWORD PTR [rbp-440], 0
        movsx   rax, edx
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
        mov     QWORD PTR [rbp-96], rax
        mov     eax, DWORD PTR [rbp-80]
        movsx   rdx, eax
        sub     rdx, 1
        mov     QWORD PTR [rbp-104], rdx
        movsx   rdx, eax
        mov     QWORD PTR [rbp-464], rdx
        mov     QWORD PTR [rbp-456], 0
        movsx   rdx, eax
        mov     QWORD PTR [rbp-480], rdx
        mov     QWORD PTR [rbp-472], 0
        cdqe
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
        mov     QWORD PTR [rbp-112], rax
        mov     rax, QWORD PTR [rbp-72]
        lea     rdx, [rax-1]
        mov     QWORD PTR [rbp-120], rdx
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
        mov     esi, 16
        mov     edx, 0
        div     rsi
        imul    rax, rax, 16
        sub     rsp, rax
        mov     rax, rsp
        add     rax, 3
        shr     rax, 2
        sal     rax, 2
        mov     QWORD PTR [rbp-128], rax
        mov     DWORD PTR [rbp-64], 0
        jmp     .L75
.L76:
        mov     eax, DWORD PTR [rbp-64]
        cdqe
        mov     ecx, DWORD PTR [rbp-384+rax*4]
        mov     rax, QWORD PTR [rbp-112]
        mov     edx, DWORD PTR [rbp-64]
        movsx   rdx, edx
        mov     DWORD PTR [rax+rdx*4], ecx
        add     DWORD PTR [rbp-64], 1
.L75:
        mov     eax, DWORD PTR [rbp-64]
        cmp     eax, DWORD PTR [rbp-80]
        jl      .L76
        mov     DWORD PTR [rbp-60], 0
        jmp     .L77
.L78:
        mov     eax, DWORD PTR [rbp-64]
        lea     edx, [rax+1]
        mov     DWORD PTR [rbp-64], edx
        mov     edx, DWORD PTR [rbp-76]
        sub     edx, DWORD PTR [rbp-60]
        sub     edx, 1
        cdqe
        mov     ecx, DWORD PTR [rbp-384+rax*4]
        mov     rax, QWORD PTR [rbp-96]
        movsx   rdx, edx
        mov     DWORD PTR [rax+rdx*4], ecx
        add     DWORD PTR [rbp-60], 1
.L77:
        mov     eax, DWORD PTR [rbp-60]
        cmp     eax, DWORD PTR [rbp-76]
        jl      .L78
        mov     DWORD PTR [rbp-60], 0
        jmp     .L79
.L80:
        mov     eax, DWORD PTR [rbp-64]
        lea     edx, [rax+1]
        mov     DWORD PTR [rbp-64], edx
        cdqe
        mov     ecx, DWORD PTR [rbp-384+rax*4]
        mov     rax, QWORD PTR [rbp-128]
        mov     edx, DWORD PTR [rbp-60]
        movsx   rdx, edx
        mov     DWORD PTR [rax+rdx*4], ecx
        add     DWORD PTR [rbp-60], 1
.L79:
        mov     eax, DWORD PTR [rbp-60]
        cdqe
        cmp     QWORD PTR [rbp-72], rax
        jg      .L80
        mov     rax, QWORD PTR [rbp-56]
        mov     edx, eax
        mov     rax, QWORD PTR [rbp-72]
        add     eax, edx
        mov     edx, eax
        lea     rax, [rbp-384]
        mov     rsi, rax
        mov     edi, edx
        call    print_bitmap
        mov     rdx, QWORD PTR [rbp-96]
        mov     eax, DWORD PTR [rbp-76]
        mov     rsi, rdx
        mov     edi, eax
        call    print_bitmap
        mov     rdx, QWORD PTR [rbp-112]
        mov     eax, DWORD PTR [rbp-80]
        mov     rsi, rdx
        mov     edi, eax
        call    print_bitmap
        mov     rax, QWORD PTR [rbp-128]
        mov     rdx, QWORD PTR [rbp-72]
        mov     rsi, rax
        mov     edi, edx
        call    print_bitmap
        mov     rax, QWORD PTR [rbp-392]
        mov     rdi, rax
        call    check_pointer
        mov     eax, 4
        sal     rax, 3
        neg     rax
        mov     rdx, rax
        mov     rax, QWORD PTR [rbp-392]
        add     rdx, rax
        mov     rcx, QWORD PTR [rbp-128]
        mov     rax, QWORD PTR [rbp-72]
        mov     rsi, rcx
        mov     rdi, rax
        call    visit_variables
        cmp     DWORD PTR [rbp-404], 0
        je      .L81
        mov     rax, QWORD PTR [rbp-400]
        mov     rdi, rax
        call    check_pointer
        mov     rax, QWORD PTR [rbp-400]
        lea     rdx, [rax+16]
        mov     rcx, QWORD PTR [rbp-96]
        mov     eax, DWORD PTR [rbp-76]
        cdqe
        mov     rsi, rcx
        mov     rdi, rax
        call    visit_params
.L81:
        mov     eax, 6
        cmp     QWORD PTR [rbp-56], rax
        jl      .L82
        mov     rax, -3
        sal     rax, 3
        neg     rax
        mov     rdx, rax
        mov     rax, QWORD PTR [rbp-392]
        add     rdx, rax
        mov     rcx, QWORD PTR [rbp-112]
        mov     eax, DWORD PTR [rbp-80]
        cdqe
        mov     rsi, rcx
        mov     rdi, rax
        call    visit_params
.L82:
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
.LC9:
        .string "Collecting Garbage:"
collect_garbage:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 32
        mov     QWORD PTR [rbp-24], rdi
        mov     QWORD PTR [rbp-32], rsi
        mov     edi, OFFSET FLAT:.LC9
        call    puts
        mov     rax, QWORD PTR to_space[rip]
        mov     QWORD PTR scan[rip], rax
        mov     DWORD PTR [rbp-4], 1
        jmp     .L84
.L85:
        mov     edx, DWORD PTR [rbp-4]
        mov     rcx, QWORD PTR [rbp-32]
        mov     rax, QWORD PTR [rbp-24]
        mov     rsi, rcx
        mov     rdi, rax
        call    scan_stack_frame
        mov     rax, QWORD PTR [rbp-24]
        mov     rax, QWORD PTR [rax]
        mov     QWORD PTR [rbp-24], rax
        mov     DWORD PTR [rbp-4], 0
.L84:
        cmp     QWORD PTR [rbp-24], 0
        jne     .L85
        mov     eax, 0
        call    print_heap
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
.LC10:
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
        mov     rdx, QWORD PTR heap_size[rip]
        sal     rdx, 3
        add     rax, rdx
        mov     QWORD PTR to_space[rip], rax
        mov     rdx, QWORD PTR current_heap_pointer[rip]
        mov     rax, QWORD PTR to_space[rip]
        cmp     rdx, rax
        jb      .L87
        mov     eax, 0
        call    swap_spaces
.L87:
        mov     rax, QWORD PTR to_space[rip]
        mov     QWORD PTR current_to_space_pointer[rip], rax
        mov     rax, QWORD PTR current_heap_pointer[rip]
        mov     rdx, QWORD PTR [rbp-8]
        sal     rdx, 3
        lea     rcx, [rax+rdx]
        mov     rax, QWORD PTR from_space[rip]
        mov     rdx, QWORD PTR heap_size[rip]
        sal     rdx, 3
        add     rax, rdx
        cmp     rcx, rax
        jbe     .L88
        mov     eax, 0
        call    print_heap
        mov     rdx, QWORD PTR [rbp-24]
        mov     rax, QWORD PTR [rbp-16]
        mov     rsi, rdx
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
        jbe     .L88
        mov     rax, QWORD PTR stderr[rip]
        mov     rcx, rax
        mov     edx, 14
        mov     esi, 1
        mov     edi, OFFSET FLAT:.LC10
        call    fwrite
        mov     edi, 1
        call    exit
.L88:
        mov     rax, QWORD PTR current_heap_pointer[rip]
        leave
        ret