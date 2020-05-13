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
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $16, %rsp
        movl    $.LC0, %edi
        call    puts
        movq    to_space(%rip), %rdx
        movq    from_space(%rip), %rax
        movq    %rax, %rsi
        movl    $.LC1, %edi
        movl    $0, %eax
        call    printf
        movl    $0, -4(%rbp)
        jmp     .L2
.L3:
        movq    to_space(%rip), %rax
        movl    -4(%rbp), %edx
        movslq  %edx, %rdx
        salq    $3, %rdx
        leaq    (%rax,%rdx), %rdi
        movq    from_space(%rip), %rax
        movl    -4(%rbp), %edx
        movslq  %edx, %rdx
        salq    $3, %rdx
        leaq    (%rax,%rdx), %rsi
        movq    to_space(%rip), %rax
        movl    -4(%rbp), %edx
        movslq  %edx, %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        movq    (%rax), %rcx
        movq    from_space(%rip), %rax
        movl    -4(%rbp), %edx
        movslq  %edx, %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        movq    (%rax), %rdx
        movl    -4(%rbp), %eax
        movq    %rdi, %r9
        movq    %rsi, %r8
        movl    %eax, %esi
        movl    $.LC2, %edi
        movl    $0, %eax
        call    printf
        addl    $1, -4(%rbp)
.L2:
        movl    -4(%rbp), %eax
        movslq  %eax, %rdx
        movq    heap_size(%rip), %rax
        cmpq    %rax, %rdx
        jl      .L3
        movl    $.LC3, %edi
        call    puts
        nop
        leave
        ret
set_bitmap:
        pushq   %rbp
        movq    %rsp, %rbp
        movq    %rdi, -40(%rbp)
        movq    %rsi, -48(%rbp)
        movq    -40(%rbp), %rax
        movq    %rax, -16(%rbp)
        movl    $7, -4(%rbp)
        jmp     .L5
.L8:
        movl    $7, -8(%rbp)
        jmp     .L6
.L7:
        movl    -4(%rbp), %eax
        movslq  %eax, %rdx
        movq    -16(%rbp), %rax
        addq    %rdx, %rax
        movzbl  (%rax), %eax
        movzbl  %al, %edx
        movl    -8(%rbp), %eax
        movl    %eax, %ecx
        sarl    %cl, %edx
        movl    %edx, %eax
        andl    $1, %eax
        movb    %al, -17(%rbp)
        movl    -4(%rbp), %eax
        leal    0(,%rax,8), %edx
        movl    -8(%rbp), %eax
        addl    %edx, %eax
        cltq
        leaq    0(,%rax,4), %rdx
        movq    -48(%rbp), %rax
        addq    %rax, %rdx
        movzbl  -17(%rbp), %eax
        movl    %eax, (%rdx)
        subl    $1, -8(%rbp)
.L6:
        cmpl    $0, -8(%rbp)
        jns     .L7
        subl    $1, -4(%rbp)
.L5:
        cmpl    $0, -4(%rbp)
        jns     .L8
        nop
        nop
        popq    %rbp
        ret
.LC4:
        .string "Bitmap:         "
.LC5:
        .string "%d"
print_bitmap:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $32, %rsp
        movl    %edi, -20(%rbp)
        movq    %rsi, -32(%rbp)
        movl    $.LC4, %edi
        movl    $0, %eax
        call    printf
        movl    $0, -4(%rbp)
        jmp     .L10
.L11:
        movl    -4(%rbp), %eax
        cltq
        leaq    0(,%rax,4), %rdx
        movq    -32(%rbp), %rax
        addq    %rdx, %rax
        movl    (%rax), %eax
        movl    %eax, %esi
        movl    $.LC5, %edi
        movl    $0, %eax
        call    printf
        addl    $1, -4(%rbp)
.L10:
        movl    -4(%rbp), %eax
        cmpl    -20(%rbp), %eax
        jl      .L11
        movl    $10, %edi
        call    putchar
        nop
        leave
        ret
swap_spaces:
        pushq   %rbp
        movq    %rsp, %rbp
        movq    to_space(%rip), %rax
        movq    %rax, -8(%rbp)
        movq    from_space(%rip), %rax
        movq    %rax, to_space(%rip)
        movq    -8(%rbp), %rax
        movq    %rax, from_space(%rip)
        nop
        popq    %rbp
        ret
min:
        pushq   %rbp
        movq    %rsp, %rbp
        movl    %edi, -4(%rbp)
        movl    %esi, -8(%rbp)
        movl    -4(%rbp), %eax
        cmpl    -8(%rbp), %eax
        jg      .L14
        movl    -4(%rbp), %eax
        jmp     .L15
.L14:
        movl    -8(%rbp), %eax
.L15:
        popq    %rbp
        ret
max:
        pushq   %rbp
        movq    %rsp, %rbp
        movl    %edi, -4(%rbp)
        movl    %esi, -8(%rbp)
        movl    -4(%rbp), %eax
        cmpl    -8(%rbp), %eax
        jl      .L17
        movl    -4(%rbp), %eax
        jmp     .L18
.L17:
        movl    -8(%rbp), %eax
.L18:
        popq    %rbp
        ret
.LC6:
        .string "Index %d: %d\n"
check_pointer:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $32, %rsp
        movq    %rdi, -24(%rbp)
        movl    $-16, -4(%rbp)
        jmp     .L20
.L21:
        movl    -4(%rbp), %eax
        cltq
        leaq    0(,%rax,8), %rdx
        movq    -24(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rdx
        movl    -4(%rbp), %eax
        movl    %eax, %esi
        movl    $.LC6, %edi
        movl    $0, %eax
        call    printf
        addl    $1, -4(%rbp)
.L20:
        cmpl    $15, -4(%rbp)
        jle     .L21
        nop
        nop
        leave
        ret
in_to_space:
        pushq   %rbp
        movq    %rsp, %rbp
        movq    %rdi, -8(%rbp)
        cmpq    $0, -8(%rbp)
        je      .L23
        movq    to_space(%rip), %rax
        cmpq    %rax, -8(%rbp)
        jb      .L23
        movq    to_space(%rip), %rax
        movq    heap_size(%rip), %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        cmpq    %rax, -8(%rbp)
        jnb     .L23
        movl    $1, %eax
        jmp     .L25
.L23:
        movl    $0, %eax
.L25:
        popq    %rbp
        ret
in_from_space:
        pushq   %rbp
        movq    %rsp, %rbp
        movq    %rdi, -8(%rbp)
        cmpq    $0, -8(%rbp)
        je      .L27
        movq    from_space(%rip), %rax
        cmpq    %rax, -8(%rbp)
        jb      .L27
        movq    from_space(%rip), %rax
        movq    heap_size(%rip), %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        cmpq    %rax, -8(%rbp)
        jnb     .L27
        movl    $1, %eax
        jmp     .L29
.L27:
        movl    $0, %eax
.L29:
        popq    %rbp
        ret
get_pointer_field:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $320, %rsp
        movq    %rdi, -312(%rbp)
        movl    %esi, -316(%rbp)
        movl    $2, %eax
        leaq    0(,%rax,8), %rdx
        movq    -312(%rbp), %rax
        addq    %rdx, %rax
        movq    %rax, -16(%rbp)
        movl    $0, %eax
        leaq    0(,%rax,8), %rdx
        movq    -312(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, -24(%rbp)
        movl    $1, %eax
        leaq    0(,%rax,8), %rdx
        movq    -312(%rbp), %rax
        addq    %rax, %rdx
        leaq    -304(%rbp), %rax
        movq    %rax, %rsi
        movq    %rdx, %rdi
        call    set_bitmap
        movl    $0, -4(%rbp)
        movl    $0, -8(%rbp)
        jmp     .L31
.L35:
        movl    -8(%rbp), %eax
        cltq
        movq    -24(%rbp), %rdx
        subq    %rax, %rdx
        movq    %rdx, %rax
        subq    $1, %rax
        movl    -304(%rbp,%rax,4), %eax
        movl    %eax, -28(%rbp)
        cmpl    $0, -28(%rbp)
        je      .L32
        movl    -8(%rbp), %eax
        cltq
        leaq    0(,%rax,8), %rdx
        movq    -16(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, -40(%rbp)
        movl    -4(%rbp), %eax
        cmpl    -316(%rbp), %eax
        jne     .L33
        movq    -40(%rbp), %rax
        jmp     .L36
.L33:
        addl    $1, -4(%rbp)
.L32:
        addl    $1, -8(%rbp)
.L31:
        movl    -8(%rbp), %eax
        cltq
        cmpq    %rax, -24(%rbp)
        jg      .L35
        movl    $0, %eax
.L36:
        leave
        ret
forward:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $288, %rsp
        movq    %rdi, -280(%rbp)
        movq    -280(%rbp), %rax
        movq    %rax, %rdi
        call    in_to_space
        testl   %eax, %eax
        je      .L38
        movq    -280(%rbp), %rax
        jmp     .L41
.L38:
        movl    $0, %eax
        leaq    0(,%rax,8), %rdx
        movq    -280(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movl    %eax, -4(%rbp)
        movl    $1, %eax
        leaq    0(,%rax,8), %rdx
        movq    -280(%rbp), %rax
        addq    %rax, %rdx
        leaq    -272(%rbp), %rax
        movq    %rax, %rsi
        movq    %rdx, %rdi
        call    set_bitmap
        movq    -280(%rbp), %rax
        movl    $0, %esi
        movq    %rax, %rdi
        call    get_pointer_field
        movq    %rax, -16(%rbp)
        movq    -16(%rbp), %rax
        movq    %rax, %rdi
        call    in_to_space
        testl   %eax, %eax
        je      .L40
        movq    -16(%rbp), %rax
        jmp     .L41
.L40:
        movl    -4(%rbp), %eax
        cltq
        movl    $2, %edx
        addq    %rdx, %rax
        salq    $3, %rax
        movq    %rax, %rdx
        movq    current_to_space_pointer(%rip), %rax
        movq    -280(%rbp), %rcx
        movq    %rcx, %rsi
        movq    %rax, %rdi
        call    memcpy
        movq    current_to_space_pointer(%rip), %rdx
        movl    $2, %eax
        leaq    0(,%rax,8), %rcx
        movq    -280(%rbp), %rax
        addq    %rcx, %rax
        movq    %rdx, (%rax)
        movq    current_to_space_pointer(%rip), %rax
        movl    -4(%rbp), %edx
        movslq  %edx, %rdx
        movl    $2, %ecx
        addq    %rcx, %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        movq    %rax, current_to_space_pointer(%rip)
        movl    $2, %eax
        leaq    0(,%rax,8), %rdx
        movq    -280(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
.L41:
        leave
        ret
visit_pointer_vars:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $320, %rsp
        movq    %rdi, -296(%rbp)
        movq    %rsi, -304(%rbp)
        movq    %rdx, -312(%rbp)
        movl    $0, -4(%rbp)
        jmp     .L43
.L47:
        movl    -4(%rbp), %eax
        cltq
        movq    -296(%rbp), %rdx
        subq    %rax, %rdx
        movq    %rdx, %rax
        salq    $2, %rax
        leaq    -4(%rax), %rdx
        movq    -304(%rbp), %rax
        addq    %rdx, %rax
        movl    (%rax), %eax
        movl    %eax, -16(%rbp)
        cmpl    $0, -16(%rbp)
        je      .L44
        movl    -4(%rbp), %eax
        negl    %eax
        cltq
        leaq    0(,%rax,8), %rdx
        movq    -312(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, -24(%rbp)
        movq    -24(%rbp), %rax
        movq    %rax, %rdi
        call    in_from_space
        testl   %eax, %eax
        je      .L53
        movq    -24(%rbp), %rax
        movq    %rax, %rdi
        call    forward
        movl    -4(%rbp), %edx
        negl    %edx
        movslq  %edx, %rdx
        leaq    0(,%rdx,8), %rcx
        movq    -312(%rbp), %rdx
        addq    %rcx, %rdx
        movq    %rax, (%rdx)
.L44:
        addl    $1, -4(%rbp)
.L43:
        movl    -4(%rbp), %eax
        cltq
        cmpq    %rax, -296(%rbp)
        jg      .L47
        jmp     .L48
.L52:
        movq    scan(%rip), %rax
        movl    $1, %edx
        salq    $3, %rdx
        addq    %rax, %rdx
        leaq    -288(%rbp), %rax
        movq    %rax, %rsi
        movq    %rdx, %rdi
        call    set_bitmap
        movq    scan(%rip), %rax
        movl    $0, %edx
        salq    $3, %rdx
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, -296(%rbp)
        movl    $0, -8(%rbp)
        jmp     .L49
.L51:
        movl    -8(%rbp), %eax
        cltq
        movl    -288(%rbp,%rax,4), %eax
        movl    %eax, -12(%rbp)
        cmpl    $0, -12(%rbp)
        je      .L50
        movq    scan(%rip), %rax
        movl    -8(%rbp), %edx
        movslq  %edx, %rdx
        movl    $2, %ecx
        addq    %rcx, %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, %rdi
        call    forward
        movq    scan(%rip), %rdx
        movl    -8(%rbp), %ecx
        movslq  %ecx, %rcx
        movl    $2, %esi
        addq    %rsi, %rcx
        salq    $3, %rcx
        addq    %rcx, %rdx
        movq    %rax, (%rdx)
.L50:
        addl    $1, -8(%rbp)
.L49:
        movl    -8(%rbp), %eax
        cltq
        cmpq    %rax, -296(%rbp)
        jg      .L51
        movq    scan(%rip), %rax
        movl    $2, %ecx
        movq    -296(%rbp), %rdx
        addq    %rcx, %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        movq    %rax, scan(%rip)
.L48:
        movq    scan(%rip), %rdx
        movq    current_to_space_pointer(%rip), %rax
        cmpq    %rax, %rdx
        jb      .L52
        jmp     .L42
.L53:
        nop
.L42:
        leave
        ret
visit_pointer_params:
        pushq   %rbp
        movq    %rsp, %rbp
        movq    %rdi, -8(%rbp)
        movq    %rsi, -16(%rbp)
        nop
        popq    %rbp
        ret
scan_stack_frame:
        pushq   %rbp
        movq    %rsp, %rbp
        pushq   %r15
        pushq   %r14
        pushq   %r13
        pushq   %r12
        pushq   %rbx
        subq    $424, %rsp
        movq    %rdi, -392(%rbp)
        movl    %esi, -396(%rbp)
        movq    %rsp, %rax
        movq    %rax, %rbx
        movl    $1, %eax
        salq    $3, %rax
        negq    %rax
        movq    %rax, %rdx
        movq    -392(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, -64(%rbp)
        movl    $2, %eax
        salq    $3, %rax
        negq    %rax
        movq    %rax, %rdx
        movq    -392(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, -56(%rbp)
        movl    $3, %eax
        salq    $3, %rax
        negq    %rax
        movq    %rax, %rdx
        movq    -392(%rbp), %rax
        addq    %rax, %rdx
        leaq    -384(%rbp), %rax
        movq    %rax, %rsi
        movq    %rdx, %rdi
        call    set_bitmap
        movq    -64(%rbp), %rax
        movl    $6, %esi
        movl    %eax, %edi
        call    min
        movl    %eax, -76(%rbp)
        movq    -64(%rbp), %rax
        subl    $6, %eax
        movl    $0, %esi
        movl    %eax, %edi
        call    max
        movl    %eax, -80(%rbp)
        movl    -76(%rbp), %edx
        movslq  %edx, %rax
        subq    $1, %rax
        movq    %rax, -88(%rbp)
        movslq  %edx, %rax
        movq    %rax, -416(%rbp)
        movq    $0, -408(%rbp)
        movslq  %edx, %rax
        movq    %rax, -432(%rbp)
        movq    $0, -424(%rbp)
        movslq  %edx, %rax
        leaq    0(,%rax,4), %rdx
        movl    $16, %eax
        subq    $1, %rax
        addq    %rdx, %rax
        movl    $16, %esi
        movl    $0, %edx
        divq    %rsi
        imulq   $16, %rax, %rax
        subq    %rax, %rsp
        movq    %rsp, %rax
        addq    $3, %rax
        shrq    $2, %rax
        salq    $2, %rax
        movq    %rax, -96(%rbp)
        movl    -80(%rbp), %eax
        movslq  %eax, %rdx
        subq    $1, %rdx
        movq    %rdx, -104(%rbp)
        movslq  %eax, %rdx
        movq    %rdx, -448(%rbp)
        movq    $0, -440(%rbp)
        movslq  %eax, %rdx
        movq    %rdx, -464(%rbp)
        movq    $0, -456(%rbp)
        cltq
        leaq    0(,%rax,4), %rdx
        movl    $16, %eax
        subq    $1, %rax
        addq    %rdx, %rax
        movl    $16, %edi
        movl    $0, %edx
        divq    %rdi
        imulq   $16, %rax, %rax
        subq    %rax, %rsp
        movq    %rsp, %rax
        addq    $3, %rax
        shrq    $2, %rax
        salq    $2, %rax
        movq    %rax, -112(%rbp)
        movq    -56(%rbp), %rax
        leaq    -1(%rax), %rdx
        movq    %rdx, -120(%rbp)
        movq    %rax, %rdx
        movq    %rdx, %r14
        movl    $0, %r15d
        movq    %rax, %rdx
        movq    %rdx, %r12
        movl    $0, %r13d
        leaq    0(,%rax,4), %rdx
        movl    $16, %eax
        subq    $1, %rax
        addq    %rdx, %rax
        movl    $16, %esi
        movl    $0, %edx
        divq    %rsi
        imulq   $16, %rax, %rax
        subq    %rax, %rsp
        movq    %rsp, %rax
        addq    $3, %rax
        shrq    $2, %rax
        salq    $2, %rax
        movq    %rax, -128(%rbp)
        movl    $0, -72(%rbp)
        jmp     .L56
.L57:
        movl    -72(%rbp), %eax
        cltq
        movl    -384(%rbp,%rax,4), %ecx
        movq    -112(%rbp), %rax
        movl    -72(%rbp), %edx
        movslq  %edx, %rdx
        movl    %ecx, (%rax,%rdx,4)
        addl    $1, -72(%rbp)
.L56:
        movl    -72(%rbp), %eax
        cmpl    -80(%rbp), %eax
        jl      .L57
        movl    $0, -68(%rbp)
        jmp     .L58
.L59:
        movl    -72(%rbp), %eax
        leal    1(%rax), %edx
        movl    %edx, -72(%rbp)
        cltq
        movl    -384(%rbp,%rax,4), %ecx
        movq    -96(%rbp), %rax
        movl    -68(%rbp), %edx
        movslq  %edx, %rdx
        movl    %ecx, (%rax,%rdx,4)
        addl    $1, -68(%rbp)
.L58:
        movl    -68(%rbp), %eax
        cmpl    -76(%rbp), %eax
        jl      .L59
        movl    $0, -68(%rbp)
        jmp     .L60
.L61:
        movl    -72(%rbp), %eax
        leal    1(%rax), %edx
        movl    %edx, -72(%rbp)
        cltq
        movl    -384(%rbp,%rax,4), %ecx
        movq    -128(%rbp), %rax
        movl    -68(%rbp), %edx
        movslq  %edx, %rdx
        movl    %ecx, (%rax,%rdx,4)
        addl    $1, -68(%rbp)
.L60:
        movl    -68(%rbp), %eax
        cltq
        cmpq    %rax, -56(%rbp)
        jg      .L61
        movl    $4, %eax
        salq    $3, %rax
        negq    %rax
        movq    %rax, %rdx
        movq    -392(%rbp), %rax
        addq    %rax, %rdx
        movq    -128(%rbp), %rcx
        movq    -56(%rbp), %rax
        movq    %rcx, %rsi
        movq    %rax, %rdi
        call    visit_pointer_vars
        movq    %rbx, %rsp
        nop
        leaq    -40(%rbp), %rsp
        popq    %rbx
        popq    %r12
        popq    %r13
        popq    %r14
        popq    %r15
        popq    %rbp
        ret
.LC7:
        .string "Collecting Garbage:"
.LC8:
        .string "RBP: %d\n"
collect_garbage:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $32, %rsp
        movq    %rdi, -24(%rbp)
        movl    $.LC7, %edi
        call    puts
        movq    to_space(%rip), %rax
        movq    %rax, scan(%rip)
        movl    $1, -4(%rbp)
        jmp     .L63
.L64:
        movq    -24(%rbp), %rax
        movq    %rax, %rsi
        movl    $.LC8, %edi
        movl    $0, %eax
        call    printf
        movl    -4(%rbp), %edx
        movq    -24(%rbp), %rax
        movl    %edx, %esi
        movq    %rax, %rdi
        call    scan_stack_frame
        movq    -24(%rbp), %rax
        movq    (%rax), %rax
        movq    %rax, -24(%rbp)
        movl    $0, -4(%rbp)
.L63:
        cmpq    $0, -24(%rbp)
        jne     .L64
        movl    $0, %eax
        call    print_heap
        movq    heap_size(%rip), %rax
        salq    $3, %rax
        movq    %rax, %rdx
        movq    from_space(%rip), %rax
        movl    $0, %esi
        movq    %rax, %rdi
        call    memset
        movq    current_to_space_pointer(%rip), %rax
        movq    %rax, current_heap_pointer(%rip)
        nop
        leave
        ret
.LC9:
        .string "Out of memory\n"
allocate_heap:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $16, %rsp
        movq    %rdi, -8(%rbp)
        movq    %rsi, -16(%rbp)
        movq    heap_pointer(%rip), %rax
        movq    %rax, from_space(%rip)
        movq    from_space(%rip), %rax
        movq    heap_size(%rip), %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        movq    %rax, to_space(%rip)
        movq    current_heap_pointer(%rip), %rdx
        movq    to_space(%rip), %rax
        cmpq    %rax, %rdx
        jb      .L66
        movl    $0, %eax
        call    swap_spaces
.L66:
        movq    to_space(%rip), %rax
        movq    %rax, current_to_space_pointer(%rip)
        movl    $0, %eax
        call    print_heap
        movq    current_heap_pointer(%rip), %rax
        movq    -8(%rbp), %rdx
        salq    $3, %rdx
        leaq    (%rax,%rdx), %rcx
        movq    from_space(%rip), %rax
        movq    heap_size(%rip), %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        cmpq    %rax, %rcx
        jbe     .L67
        movq    -16(%rbp), %rax
        movq    %rax, %rdi
        call    collect_garbage
        movl    $0, %eax
        call    print_heap
        movq    current_heap_pointer(%rip), %rax
        movq    -8(%rbp), %rdx
        salq    $3, %rdx
        leaq    (%rax,%rdx), %rcx
        movq    to_space(%rip), %rax
        movq    heap_size(%rip), %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        cmpq    %rax, %rcx
        jbe     .L67
        movq    stderr(%rip), %rax
        movq    %rax, %rcx
        movl    $14, %edx
        movl    $1, %esi
        movl    $.LC9, %edi
        call    fwrite
        movl    $1, %edi
        call    exit
.L67:
        movq    current_heap_pointer(%rip), %rax
        leave
        ret