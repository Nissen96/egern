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
        jg      .L8
        mov     eax, DWORD PTR [rbp-4]
        jmp     .L9
.L8:
        mov     eax, DWORD PTR [rbp-8]
.L9:
        pop     rbp
        ret
max:
        push    rbp
        mov     rbp, rsp
        mov     DWORD PTR [rbp-4], edi
        mov     DWORD PTR [rbp-8], esi
        mov     eax, DWORD PTR [rbp-4]
        cmp     eax, DWORD PTR [rbp-8]
        jl      .L11
        mov     eax, DWORD PTR [rbp-4]
        jmp     .L12
.L11:
        mov     eax, DWORD PTR [rbp-8]
.L12:
        pop     rbp
        ret
in_to_space:
        push    rbp
        mov     rbp, rsp
        mov     QWORD PTR [rbp-8], rdi
        cmp     QWORD PTR [rbp-8], 0
        je      .L14
        mov     rax, QWORD PTR to_space[rip]
        cmp     QWORD PTR [rbp-8], rax
        jb      .L14
        mov     rax, QWORD PTR to_space[rip]
        mov     rdx, QWORD PTR heap_size[rip]
        sal     rdx, 3
        add     rax, rdx
        cmp     QWORD PTR [rbp-8], rax
        jnb     .L14
        mov     eax, 1
        jmp     .L16
.L14:
        mov     eax, 0
.L16:
        pop     rbp
        ret
in_from_space:
        push    rbp
        mov     rbp, rsp
        mov     QWORD PTR [rbp-8], rdi
        cmp     QWORD PTR [rbp-8], 0
        je      .L18
        mov     rax, QWORD PTR from_space[rip]
        cmp     QWORD PTR [rbp-8], rax
        jb      .L18
        mov     rax, QWORD PTR from_space[rip]
        mov     rdx, QWORD PTR heap_size[rip]
        sal     rdx, 3
        add     rax, rdx
        cmp     QWORD PTR [rbp-8], rax
        jnb     .L18
        mov     eax, 1
        jmp     .L20
.L18:
        mov     eax, 0
.L20:
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
        jne     .L22
        mov     rax, QWORD PTR [rbp-280]
        jmp     .L25
.L22:
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
        je      .L24
        mov     eax, 1
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-280]
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        jmp     .L25
.L24:
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
.L25:
        leave
        ret
forward_heap_fields:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 272
        jmp     .L27
.L31:
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
        jmp     .L28
.L30:
        mov     eax, DWORD PTR [rbp-4]
        cdqe
        mov     eax, DWORD PTR [rbp-272+rax*4]
        mov     DWORD PTR [rbp-12], eax
        cmp     DWORD PTR [rbp-12], 0
        je      .L29
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
.L29:
        add     DWORD PTR [rbp-4], 1
.L28:
        mov     eax, DWORD PTR [rbp-4]
        cmp     eax, DWORD PTR [rbp-8]
        jl      .L30
        mov     rax, QWORD PTR scan[rip]
        mov     edx, DWORD PTR [rbp-8]
        movsx   rdx, edx
        mov     ecx, 2
        add     rdx, rcx
        sal     rdx, 3
        add     rax, rdx
        mov     QWORD PTR scan[rip], rax
.L27:
        mov     rdx, QWORD PTR scan[rip]
        mov     rax, QWORD PTR current_to_space_pointer[rip]
        cmp     rdx, rax
        jb      .L31
        nop
        nop
        leave
        ret
visit_params:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 48
        mov     QWORD PTR [rbp-24], rdi
        mov     QWORD PTR [rbp-32], rsi
        mov     QWORD PTR [rbp-40], rdx
        mov     DWORD PTR [rbp-4], 0
        jmp     .L33
.L35:
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
        je      .L34
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
.L34:
        add     DWORD PTR [rbp-4], 1
.L33:
        mov     eax, DWORD PTR [rbp-4]
        cdqe
        cmp     QWORD PTR [rbp-24], rax
        jg      .L35
        mov     eax, 0
        call    forward_heap_fields
        nop
        leave
        ret
visit_variables:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 48
        mov     QWORD PTR [rbp-24], rdi
        mov     QWORD PTR [rbp-32], rsi
        mov     QWORD PTR [rbp-40], rdx
        mov     DWORD PTR [rbp-4], 0
        jmp     .L37
.L41:
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
        je      .L38
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
        je      .L42
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
.L38:
        add     DWORD PTR [rbp-4], 1
.L37:
        mov     eax, DWORD PTR [rbp-4]
        cdqe
        cmp     QWORD PTR [rbp-24], rax
        jg      .L41
        jmp     .L40
.L42:
        nop
.L40:
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
        push    r12
        push    rbx
        sub     rsp, 784
        mov     QWORD PTR [rbp-680], rdi
        mov     QWORD PTR [rbp-688], rsi
        mov     DWORD PTR [rbp-692], edx
        mov     rax, rsp
        mov     r12, rax
        mov     eax, 1
        mov     rdx, rax
        mov     eax, 0
        sub     rax, rdx
        sal     rax, 3
        mov     rdx, rax
        mov     rax, QWORD PTR [rbp-680]
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        mov     QWORD PTR [rbp-48], rax
        mov     eax, 2
        mov     rdx, rax
        mov     eax, 0
        sub     rax, rdx
        sal     rax, 3
        mov     rdx, rax
        mov     rax, QWORD PTR [rbp-680]
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        mov     QWORD PTR [rbp-40], rax
        mov     eax, 3
        sal     rax, 3
        neg     rax
        mov     rdx, rax
        mov     rax, QWORD PTR [rbp-680]
        add     rdx, rax
        lea     rax, [rbp-416]
        mov     rsi, rax
        mov     rdi, rdx
        call    set_bitmap
        mov     eax, 6
        mov     edx, eax
        mov     rax, QWORD PTR [rbp-48]
        mov     esi, edx
        mov     edi, eax
        call    min
        mov     DWORD PTR [rbp-60], eax
        mov     rax, QWORD PTR [rbp-48]
        mov     edx, eax
        mov     eax, 6
        sub     edx, eax
        mov     eax, edx
        mov     esi, 0
        mov     edi, eax
        call    max
        mov     DWORD PTR [rbp-64], eax
        mov     edx, DWORD PTR [rbp-60]
        movsx   rax, edx
        sub     rax, 1
        mov     QWORD PTR [rbp-72], rax
        movsx   rax, edx
        mov     r14, rax
        mov     r15d, 0
        movsx   rax, edx
        mov     QWORD PTR [rbp-720], rax
        mov     QWORD PTR [rbp-712], 0
        movsx   rax, edx
        lea     rdx, [0+rax*4]
        mov     eax, 16
        sub     rax, 1
        add     rax, rdx
        mov     ebx, 16
        mov     edx, 0
        div     rbx
        imul    rax, rax, 16
        sub     rsp, rax
        mov     rax, rsp
        add     rax, 3
        shr     rax, 2
        sal     rax, 2
        mov     QWORD PTR [rbp-80], rax
        mov     edx, DWORD PTR [rbp-64]
        movsx   rax, edx
        sub     rax, 1
        mov     QWORD PTR [rbp-88], rax
        movsx   rax, edx
        mov     QWORD PTR [rbp-736], rax
        mov     QWORD PTR [rbp-728], 0
        movsx   rax, edx
        mov     QWORD PTR [rbp-752], rax
        mov     QWORD PTR [rbp-744], 0
        movsx   rax, edx
        lea     rdx, [0+rax*4]
        mov     eax, 16
        sub     rax, 1
        add     rax, rdx
        mov     ebx, 16
        mov     edx, 0
        div     rbx
        imul    rax, rax, 16
        sub     rsp, rax
        mov     rax, rsp
        add     rax, 3
        shr     rax, 2
        sal     rax, 2
        mov     QWORD PTR [rbp-96], rax
        mov     rax, QWORD PTR [rbp-40]
        lea     rdx, [rax-1]
        mov     QWORD PTR [rbp-104], rdx
        mov     rdx, rax
        mov     QWORD PTR [rbp-768], rdx
        mov     QWORD PTR [rbp-760], 0
        mov     rdx, rax
        mov     QWORD PTR [rbp-784], rdx
        mov     QWORD PTR [rbp-776], 0
        lea     rdx, [0+rax*4]
        mov     eax, 16
        sub     rax, 1
        add     rax, rdx
        mov     ebx, 16
        mov     edx, 0
        div     rbx
        imul    rax, rax, 16
        sub     rsp, rax
        mov     rax, rsp
        add     rax, 3
        shr     rax, 2
        sal     rax, 2
        mov     QWORD PTR [rbp-112], rax
        mov     DWORD PTR [rbp-56], 0
        jmp     .L44
.L45:
        mov     eax, DWORD PTR [rbp-56]
        cdqe
        mov     ecx, DWORD PTR [rbp-416+rax*4]
        mov     rax, QWORD PTR [rbp-96]
        mov     edx, DWORD PTR [rbp-56]
        movsx   rdx, edx
        mov     DWORD PTR [rax+rdx*4], ecx
        add     DWORD PTR [rbp-56], 1
.L44:
        mov     eax, DWORD PTR [rbp-56]
        cmp     eax, DWORD PTR [rbp-64]
        jl      .L45
        mov     DWORD PTR [rbp-52], 0
        jmp     .L46
.L47:
        mov     eax, DWORD PTR [rbp-56]
        lea     edx, [rax+1]
        mov     DWORD PTR [rbp-56], edx
        mov     edx, DWORD PTR [rbp-60]
        sub     edx, DWORD PTR [rbp-52]
        sub     edx, 1
        cdqe
        mov     ecx, DWORD PTR [rbp-416+rax*4]
        mov     rax, QWORD PTR [rbp-80]
        movsx   rdx, edx
        mov     DWORD PTR [rax+rdx*4], ecx
        add     DWORD PTR [rbp-52], 1
.L46:
        mov     eax, DWORD PTR [rbp-52]
        cmp     eax, DWORD PTR [rbp-60]
        jl      .L47
        mov     DWORD PTR [rbp-52], 0
        jmp     .L48
.L49:
        mov     eax, DWORD PTR [rbp-56]
        lea     edx, [rax+1]
        mov     DWORD PTR [rbp-56], edx
        cdqe
        mov     ecx, DWORD PTR [rbp-416+rax*4]
        mov     rax, QWORD PTR [rbp-112]
        mov     edx, DWORD PTR [rbp-52]
        movsx   rdx, edx
        mov     DWORD PTR [rax+rdx*4], ecx
        add     DWORD PTR [rbp-52], 1
.L48:
        mov     eax, DWORD PTR [rbp-52]
        cdqe
        cmp     QWORD PTR [rbp-40], rax
        jg      .L49
        mov     eax, 4
        sal     rax, 3
        neg     rax
        mov     rdx, rax
        mov     rax, QWORD PTR [rbp-680]
        add     rdx, rax
        mov     rcx, QWORD PTR [rbp-112]
        mov     rax, QWORD PTR [rbp-40]
        mov     rsi, rcx
        mov     rdi, rax
        call    visit_variables
        cmp     DWORD PTR [rbp-692], 0
        je      .L50
        mov     rax, QWORD PTR [rbp-688]
        lea     rdx, [rax+16]
        mov     rcx, QWORD PTR [rbp-80]
        mov     eax, DWORD PTR [rbp-60]
        cdqe
        mov     rsi, rcx
        mov     rdi, rax
        call    visit_params
.L50:
        mov     rax, QWORD PTR [rbp-680]
        mov     rax, QWORD PTR [rax]
        mov     QWORD PTR [rbp-120], rax
        cmp     QWORD PTR [rbp-120], 0
        je      .L51
        mov     rax, rsp
        mov     rbx, rax
        mov     eax, 1
        mov     rdx, rax
        mov     eax, 0
        sub     rax, rdx
        sal     rax, 3
        mov     rdx, rax
        mov     rax, QWORD PTR [rbp-120]
        add     rax, rdx
        mov     rax, QWORD PTR [rax]
        mov     QWORD PTR [rbp-128], rax
        mov     eax, 3
        sal     rax, 3
        neg     rax
        mov     rdx, rax
        mov     rax, QWORD PTR [rbp-120]
        add     rdx, rax
        lea     rax, [rbp-672]
        mov     rsi, rax
        mov     rdi, rdx
        call    set_bitmap
        mov     rax, QWORD PTR [rbp-128]
        mov     edx, eax
        mov     eax, 6
        sub     edx, eax
        mov     eax, edx
        mov     esi, 0
        mov     edi, eax
        call    max
        mov     DWORD PTR [rbp-132], eax
        mov     eax, 6
        mov     edx, eax
        mov     rax, QWORD PTR [rbp-48]
        mov     esi, edx
        mov     edi, eax
        call    min
        mov     DWORD PTR [rbp-136], eax
        mov     eax, DWORD PTR [rbp-136]
        movsx   rdx, eax
        sub     rdx, 1
        mov     QWORD PTR [rbp-144], rdx
        movsx   rdx, eax
        mov     QWORD PTR [rbp-800], rdx
        mov     QWORD PTR [rbp-792], 0
        movsx   rdx, eax
        mov     QWORD PTR [rbp-816], rdx
        mov     QWORD PTR [rbp-808], 0
        cdqe
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
        mov     QWORD PTR [rbp-152], rax
        mov     DWORD PTR [rbp-56], 0
        jmp     .L52
.L53:
        mov     edx, DWORD PTR [rbp-132]
        mov     eax, DWORD PTR [rbp-56]
        add     eax, edx
        cdqe
        mov     ecx, DWORD PTR [rbp-672+rax*4]
        mov     rax, QWORD PTR [rbp-152]
        mov     edx, DWORD PTR [rbp-56]
        movsx   rdx, edx
        mov     DWORD PTR [rax+rdx*4], ecx
        add     DWORD PTR [rbp-56], 1
.L52:
        mov     eax, DWORD PTR [rbp-56]
        cmp     eax, DWORD PTR [rbp-136]
        jl      .L53
        mov     eax, DWORD PTR [rbp-132]
        cdqe
        mov     rdx, -3
        sub     rax, rdx
        lea     rdx, [0+rax*8]
        mov     rax, QWORD PTR [rbp-680]
        add     rdx, rax
        mov     rcx, QWORD PTR [rbp-152]
        mov     eax, DWORD PTR [rbp-136]
        cdqe
        mov     rsi, rcx
        mov     rdi, rax
        call    visit_params
        mov     rsp, rbx
.L51:
        mov     eax, 6
        cmp     QWORD PTR [rbp-48], rax
        jl      .L54
        mov     rax, -3
        sal     rax, 3
        neg     rax
        mov     rdx, rax
        mov     rax, QWORD PTR [rbp-680]
        add     rdx, rax
        mov     rcx, QWORD PTR [rbp-96]
        mov     eax, DWORD PTR [rbp-64]
        cdqe
        mov     rsi, rcx
        mov     rdi, rax
        call    visit_params
.L54:
        mov     rsp, r12
        nop
        lea     rsp, [rbp-32]
        pop     rbx
        pop     r12
        pop     r14
        pop     r15
        pop     rbp
        ret
collect_garbage:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 32
        mov     QWORD PTR [rbp-24], rdi
        mov     QWORD PTR [rbp-32], rsi
        mov     rax, QWORD PTR to_space[rip]
        mov     QWORD PTR scan[rip], rax
        mov     DWORD PTR [rbp-4], 1
        jmp     .L56
.L57:
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
.L56:
        cmp     QWORD PTR [rbp-24], 0
        jne     .L57
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
.LC0:
        .string "Out of memory\n"
allocate_heap:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 48
        mov     QWORD PTR [rbp-24], rdi
        mov     QWORD PTR [rbp-32], rsi
        mov     QWORD PTR [rbp-40], rdx
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
        jbe     .L59
        mov     eax, 0
        call    swap_spaces
.L59:
        mov     rax, QWORD PTR to_space[rip]
        mov     QWORD PTR current_to_space_pointer[rip], rax
        mov     rax, QWORD PTR current_heap_pointer[rip]
        mov     rdx, QWORD PTR [rbp-24]
        sal     rdx, 3
        lea     rcx, [rax+rdx]
        mov     rax, QWORD PTR from_space[rip]
        mov     rdx, QWORD PTR heap_size[rip]
        sal     rdx, 3
        add     rax, rdx
        cmp     rcx, rax
        jbe     .L60
        mov     rdx, QWORD PTR [rbp-40]
        mov     rax, QWORD PTR [rbp-32]
        mov     rsi, rdx
        mov     rdi, rax
        call    collect_garbage
        mov     rax, QWORD PTR current_heap_pointer[rip]
        mov     rdx, QWORD PTR [rbp-24]
        sal     rdx, 3
        lea     rcx, [rax+rdx]
        mov     rax, QWORD PTR to_space[rip]
        mov     rdx, QWORD PTR heap_size[rip]
        sal     rdx, 3
        add     rax, rdx
        cmp     rcx, rax
        jbe     .L60
        mov     rax, QWORD PTR stderr[rip]
        mov     rcx, rax
        mov     edx, 14
        mov     esi, 1
        mov     edi, OFFSET FLAT:.LC0
        call    fwrite
        mov     edi, 1
        call    exit
.L60:
        mov     rax, QWORD PTR current_heap_pointer[rip]
        mov     QWORD PTR [rbp-8], rax
        mov     rax, QWORD PTR current_heap_pointer[rip]
        mov     rdx, QWORD PTR [rbp-24]
        sal     rdx, 3
        add     rax, rdx
        mov     QWORD PTR current_heap_pointer[rip], rax
        mov     rax, QWORD PTR [rbp-8]
        leave
        ret