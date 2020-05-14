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
        .string "%d: %d\n"
check_pointer:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $32, %rsp
        movq    %rdi, -24(%rbp)
        movl    $-24, -4(%rbp)
        jmp     .L20
.L23:
        cmpl    $0, -4(%rbp)
        jne     .L21
        movl    $10, %edi
        call    putchar
.L21:
        movl    -4(%rbp), %eax
        cltq
        leaq    0(,%rax,8), %rdx
        movq    -24(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movl    -4(%rbp), %edx
        movslq  %edx, %rdx
        leaq    0(,%rdx,8), %rcx
        movq    -24(%rbp), %rdx
        addq    %rdx, %rcx
        movq    %rax, %rdx
        movq    %rcx, %rsi
        movl    $.LC6, %edi
        movl    $0, %eax
        call    printf
        cmpl    $0, -4(%rbp)
        jne     .L22
        movl    $10, %edi
        call    putchar
.L22:
        addl    $1, -4(%rbp)
.L20:
        cmpl    $23, -4(%rbp)
        jle     .L23
        nop
        nop
        leave
        ret
in_to_space:
        pushq   %rbp
        movq    %rsp, %rbp
        movq    %rdi, -8(%rbp)
        cmpq    $0, -8(%rbp)
        je      .L25
        movq    to_space(%rip), %rax
        cmpq    %rax, -8(%rbp)
        jb      .L25
        movq    to_space(%rip), %rax
        movq    heap_size(%rip), %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        cmpq    %rax, -8(%rbp)
        jnb     .L25
        movl    $1, %eax
        jmp     .L27
.L25:
        movl    $0, %eax
.L27:
        popq    %rbp
        ret
in_from_space:
        pushq   %rbp
        movq    %rsp, %rbp
        movq    %rdi, -8(%rbp)
        cmpq    $0, -8(%rbp)
        je      .L29
        movq    from_space(%rip), %rax
        cmpq    %rax, -8(%rbp)
        jb      .L29
        movq    from_space(%rip), %rax
        movq    heap_size(%rip), %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        cmpq    %rax, -8(%rbp)
        jnb     .L29
        movl    $1, %eax
        jmp     .L31
.L29:
        movl    $0, %eax
.L31:
        popq    %rbp
        ret
forward:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $288, %rsp
        movq    %rdi, -280(%rbp)
        movq    -280(%rbp), %rax
        movq    %rax, %rdi
        call    in_from_space
        testl   %eax, %eax
        jne     .L33
        movq    -280(%rbp), %rax
        jmp     .L36
.L33:
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
        movl    $0, %eax
        leaq    0(,%rax,8), %rdx
        movq    -280(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        cmpq    $-1, %rax
        sete    %al
        movzbl  %al, %eax
        movq    %rax, -16(%rbp)
        cmpq    $0, -16(%rbp)
        je      .L35
        movl    $1, %eax
        leaq    0(,%rax,8), %rdx
        movq    -280(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        jmp     .L36
.L35:
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
        movl    $0, %eax
        leaq    0(,%rax,8), %rdx
        movq    -280(%rbp), %rax
        addq    %rdx, %rax
        movq    $-1, (%rax)
        movq    current_to_space_pointer(%rip), %rdx
        movl    $1, %eax
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
        movl    $1, %eax
        leaq    0(,%rax,8), %rdx
        movq    -280(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
.L36:
        leave
        ret
forward_heap_fields:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $272, %rsp
        jmp     .L38
.L42:
        movq    scan(%rip), %rax
        movl    $1, %edx
        salq    $3, %rdx
        addq    %rax, %rdx
        leaq    -272(%rbp), %rax
        movq    %rax, %rsi
        movq    %rdx, %rdi
        call    set_bitmap
        movq    scan(%rip), %rax
        movl    $0, %edx
        salq    $3, %rdx
        addq    %rdx, %rax
        movq    (%rax), %rax
        movl    %eax, -8(%rbp)
        movl    $0, -4(%rbp)
        jmp     .L39
.L41:
        movl    -4(%rbp), %eax
        cltq
        movl    -272(%rbp,%rax,4), %eax
        movl    %eax, -12(%rbp)
        cmpl    $0, -12(%rbp)
        je      .L40
        movq    scan(%rip), %rax
        movl    -4(%rbp), %edx
        movslq  %edx, %rdx
        movl    $2, %ecx
        addq    %rcx, %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, %rdi
        call    forward
        movq    scan(%rip), %rdx
        movl    -4(%rbp), %ecx
        movslq  %ecx, %rcx
        movl    $2, %esi
        addq    %rsi, %rcx
        salq    $3, %rcx
        addq    %rcx, %rdx
        movq    %rax, (%rdx)
.L40:
        addl    $1, -4(%rbp)
.L39:
        movl    -4(%rbp), %eax
        cmpl    -8(%rbp), %eax
        jl      .L41
        movq    scan(%rip), %rax
        movl    -8(%rbp), %edx
        movslq  %edx, %rdx
        movl    $2, %ecx
        addq    %rcx, %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        movq    %rax, scan(%rip)
.L38:
        movq    scan(%rip), %rdx
        movq    current_to_space_pointer(%rip), %rax
        cmpq    %rax, %rdx
        jb      .L42
        movl    $0, %eax
        call    print_heap
        nop
        leave
        ret
visit_register_params:
        pushq   %rbp
        movq    %rsp, %rbp
        movq    %rdi, -24(%rbp)
        movq    %rsi, -32(%rbp)
        movl    $0, -4(%rbp)
        jmp     .L44
.L61:
        movl    -4(%rbp), %eax
        cltq
        movq    -24(%rbp), %rdx
        subq    %rax, %rdx
        movq    %rdx, %rax
        salq    $2, %rax
        leaq    -4(%rax), %rdx
        movq    -32(%rbp), %rax
        addq    %rdx, %rax
        movl    (%rax), %eax
        movl    %eax, -8(%rbp)
        cmpl    $0, -8(%rbp)
        je      .L62
        cmpl    $5, -4(%rbp)
        ja      .L46
        movl    -4(%rbp), %eax
        movq    .L48(,%rax,8), %rax
        jmp     *%rax
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
        cmpl    $5, -4(%rbp)
        ja      .L45
        movl    -4(%rbp), %eax
        movq    .L55(,%rax,8), %rax
        jmp     *%rax
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
        addl    $1, -4(%rbp)
.L44:
        cmpl    $4, -4(%rbp)
        jle     .L61
        nop
        nop
        popq    %rbp
        ret
.LC7:
        .string "Param %d: %d %d\n"
visit_params:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $48, %rsp
        movq    %rdi, -24(%rbp)
        movq    %rsi, -32(%rbp)
        movq    %rdx, -40(%rbp)
        movl    $0, -4(%rbp)
        jmp     .L64
.L66:
        movl    -4(%rbp), %eax
        cltq
        movq    -24(%rbp), %rdx
        subq    %rax, %rdx
        movq    %rdx, %rax
        salq    $2, %rax
        leaq    -4(%rax), %rdx
        movq    -32(%rbp), %rax
        addq    %rdx, %rax
        movl    (%rax), %ecx
        movl    -4(%rbp), %eax
        cltq
        leaq    0(,%rax,8), %rdx
        movq    -40(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rdx
        movl    -4(%rbp), %eax
        movl    %eax, %esi
        movl    $.LC7, %edi
        movl    $0, %eax
        call    printf
        movl    -4(%rbp), %eax
        cltq
        movq    -24(%rbp), %rdx
        subq    %rax, %rdx
        movq    %rdx, %rax
        salq    $2, %rax
        leaq    -4(%rax), %rdx
        movq    -32(%rbp), %rax
        addq    %rdx, %rax
        movl    (%rax), %eax
        movl    %eax, -8(%rbp)
        cmpl    $0, -8(%rbp)
        je      .L65
        movl    -4(%rbp), %eax
        cltq
        leaq    0(,%rax,8), %rdx
        movq    -40(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, %rdi
        call    forward
        movl    -4(%rbp), %edx
        movslq  %edx, %rdx
        leaq    0(,%rdx,8), %rcx
        movq    -40(%rbp), %rdx
        addq    %rcx, %rdx
        movq    %rax, (%rdx)
.L65:
        addl    $1, -4(%rbp)
.L64:
        movl    -4(%rbp), %eax
        cltq
        cmpq    %rax, -24(%rbp)
        jg      .L66
        movl    $0, %eax
        call    forward_heap_fields
        nop
        leave
        ret
.LC8:
        .string "Var %d: %d %d\n"
visit_variables:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $48, %rsp
        movq    %rdi, -24(%rbp)
        movq    %rsi, -32(%rbp)
        movq    %rdx, -40(%rbp)
        movl    $0, -4(%rbp)
        jmp     .L68
.L72:
        movl    -4(%rbp), %eax
        cltq
        movq    -24(%rbp), %rdx
        subq    %rax, %rdx
        movq    %rdx, %rax
        salq    $2, %rax
        leaq    -4(%rax), %rdx
        movq    -32(%rbp), %rax
        addq    %rdx, %rax
        movl    (%rax), %ecx
        movl    -4(%rbp), %eax
        negl    %eax
        cltq
        leaq    0(,%rax,8), %rdx
        movq    -40(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rdx
        movl    -4(%rbp), %eax
        movl    %eax, %esi
        movl    $.LC8, %edi
        movl    $0, %eax
        call    printf
        movl    -4(%rbp), %eax
        cltq
        movq    -24(%rbp), %rdx
        subq    %rax, %rdx
        movq    %rdx, %rax
        salq    $2, %rax
        leaq    -4(%rax), %rdx
        movq    -32(%rbp), %rax
        addq    %rdx, %rax
        movl    (%rax), %eax
        movl    %eax, -8(%rbp)
        cmpl    $0, -8(%rbp)
        je      .L69
        movl    -4(%rbp), %eax
        negl    %eax
        cltq
        leaq    0(,%rax,8), %rdx
        movq    -40(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, -16(%rbp)
        movq    -16(%rbp), %rax
        movq    %rax, %rdi
        call    in_from_space
        testl   %eax, %eax
        je      .L73
        movq    -16(%rbp), %rax
        movq    %rax, %rdi
        call    forward
        movl    -4(%rbp), %edx
        negl    %edx
        movslq  %edx, %rdx
        leaq    0(,%rdx,8), %rcx
        movq    -40(%rbp), %rdx
        addq    %rcx, %rdx
        movq    %rax, (%rdx)
.L69:
        addl    $1, -4(%rbp)
.L68:
        movl    -4(%rbp), %eax
        cltq
        cmpq    %rax, -24(%rbp)
        jg      .L72
        jmp     .L71
.L73:
        nop
.L71:
        movl    $0, %eax
        call    forward_heap_fields
        nop
        leave
        ret
scan_stack_frame:
        pushq   %rbp
        movq    %rsp, %rbp
        pushq   %r15
        pushq   %r14
        pushq   %r13
        pushq   %r12
        pushq   %rbx
        subq    $440, %rsp
        movq    %rdi, -392(%rbp)
        movq    %rsi, -400(%rbp)
        movl    %edx, -404(%rbp)
        movq    %rsp, %rax
        movq    %rax, %rbx
        movl    $1, %eax
        movq    %rax, %rdx
        movl    $0, %eax
        subq    %rdx, %rax
        salq    $3, %rax
        movq    %rax, %rdx
        movq    -392(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, -56(%rbp)
        movl    $2, %eax
        movq    %rax, %rdx
        movl    $0, %eax
        subq    %rdx, %rax
        salq    $3, %rax
        movq    %rax, %rdx
        movq    -392(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, -72(%rbp)
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
        movl    $6, %eax
        movl    %eax, %edx
        movq    -56(%rbp), %rax
        movl    %edx, %esi
        movl    %eax, %edi
        call    min
        movl    %eax, -76(%rbp)
        movq    -56(%rbp), %rax
        movl    %eax, %edx
        movl    $6, %eax
        subl    %eax, %edx
        movl    %edx, %eax
        movl    $0, %esi
        movl    %eax, %edi
        call    max
        movl    %eax, -80(%rbp)
        movl    -76(%rbp), %edx
        movslq  %edx, %rax
        subq    $1, %rax
        movq    %rax, -88(%rbp)
        movslq  %edx, %rax
        movq    %rax, -432(%rbp)
        movq    $0, -424(%rbp)
        movslq  %edx, %rax
        movq    %rax, -448(%rbp)
        movq    $0, -440(%rbp)
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
        movq    %rdx, -464(%rbp)
        movq    $0, -456(%rbp)
        movslq  %eax, %rdx
        movq    %rdx, -480(%rbp)
        movq    $0, -472(%rbp)
        cltq
        leaq    0(,%rax,4), %rdx
        movl    $16, %eax
        subq    $1, %rax
        addq    %rdx, %rax
        movl    $16, %ecx
        movl    $0, %edx
        divq    %rcx
        imulq   $16, %rax, %rax
        subq    %rax, %rsp
        movq    %rsp, %rax
        addq    $3, %rax
        shrq    $2, %rax
        salq    $2, %rax
        movq    %rax, -112(%rbp)
        movq    -72(%rbp), %rax
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
        movl    $0, -64(%rbp)
        jmp     .L75
.L76:
        movl    -64(%rbp), %eax
        cltq
        movl    -384(%rbp,%rax,4), %ecx
        movq    -112(%rbp), %rax
        movl    -64(%rbp), %edx
        movslq  %edx, %rdx
        movl    %ecx, (%rax,%rdx,4)
        addl    $1, -64(%rbp)
.L75:
        movl    -64(%rbp), %eax
        cmpl    -80(%rbp), %eax
        jl      .L76
        movl    $0, -60(%rbp)
        jmp     .L77
.L78:
        movl    -64(%rbp), %eax
        leal    1(%rax), %edx
        movl    %edx, -64(%rbp)
        movl    -76(%rbp), %edx
        subl    -60(%rbp), %edx
        subl    $1, %edx
        cltq
        movl    -384(%rbp,%rax,4), %ecx
        movq    -96(%rbp), %rax
        movslq  %edx, %rdx
        movl    %ecx, (%rax,%rdx,4)
        addl    $1, -60(%rbp)
.L77:
        movl    -60(%rbp), %eax
        cmpl    -76(%rbp), %eax
        jl      .L78
        movl    $0, -60(%rbp)
        jmp     .L79
.L80:
        movl    -64(%rbp), %eax
        leal    1(%rax), %edx
        movl    %edx, -64(%rbp)
        cltq
        movl    -384(%rbp,%rax,4), %ecx
        movq    -128(%rbp), %rax
        movl    -60(%rbp), %edx
        movslq  %edx, %rdx
        movl    %ecx, (%rax,%rdx,4)
        addl    $1, -60(%rbp)
.L79:
        movl    -60(%rbp), %eax
        cltq
        cmpq    %rax, -72(%rbp)
        jg      .L80
        movq    -56(%rbp), %rax
        movl    %eax, %edx
        movq    -72(%rbp), %rax
        addl    %edx, %eax
        movl    %eax, %edx
        leaq    -384(%rbp), %rax
        movq    %rax, %rsi
        movl    %edx, %edi
        call    print_bitmap
        movq    -96(%rbp), %rdx
        movl    -76(%rbp), %eax
        movq    %rdx, %rsi
        movl    %eax, %edi
        call    print_bitmap
        movq    -112(%rbp), %rdx
        movl    -80(%rbp), %eax
        movq    %rdx, %rsi
        movl    %eax, %edi
        call    print_bitmap
        movq    -128(%rbp), %rax
        movq    -72(%rbp), %rdx
        movq    %rax, %rsi
        movl    %edx, %edi
        call    print_bitmap
        movq    -392(%rbp), %rax
        movq    %rax, %rdi
        call    check_pointer
        movl    $4, %eax
        salq    $3, %rax
        negq    %rax
        movq    %rax, %rdx
        movq    -392(%rbp), %rax
        addq    %rax, %rdx
        movq    -128(%rbp), %rcx
        movq    -72(%rbp), %rax
        movq    %rcx, %rsi
        movq    %rax, %rdi
        call    visit_variables
        cmpl    $0, -404(%rbp)
        je      .L81
        movq    -400(%rbp), %rax
        movq    %rax, %rdi
        call    check_pointer
        movq    -400(%rbp), %rax
        leaq    16(%rax), %rdx
        movq    -96(%rbp), %rcx
        movl    -76(%rbp), %eax
        cltq
        movq    %rcx, %rsi
        movq    %rax, %rdi
        call    visit_params
.L81:
        movl    $6, %eax
        cmpq    %rax, -56(%rbp)
        jl      .L82
        movq    $-3, %rax
        salq    $3, %rax
        negq    %rax
        movq    %rax, %rdx
        movq    -392(%rbp), %rax
        addq    %rax, %rdx
        movq    -112(%rbp), %rcx
        movl    -80(%rbp), %eax
        cltq
        movq    %rcx, %rsi
        movq    %rax, %rdi
        call    visit_params
.L82:
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
.LC9:
        .string "Collecting Garbage:"
collect_garbage:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $32, %rsp
        movq    %rdi, -24(%rbp)
        movq    %rsi, -32(%rbp)
        movl    $.LC9, %edi
        call    puts
        movq    to_space(%rip), %rax
        movq    %rax, scan(%rip)
        movl    $1, -4(%rbp)
        jmp     .L84
.L85:
        movl    -4(%rbp), %edx
        movq    -32(%rbp), %rcx
        movq    -24(%rbp), %rax
        movq    %rcx, %rsi
        movq    %rax, %rdi
        call    scan_stack_frame
        movq    -24(%rbp), %rax
        movq    (%rax), %rax
        movq    %rax, -24(%rbp)
        movl    $0, -4(%rbp)
.L84:
        cmpq    $0, -24(%rbp)
        jne     .L85
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
.LC10:
        .string "Out of memory\n"
allocate_heap:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $32, %rsp
        movq    %rdi, -8(%rbp)
        movq    %rsi, -16(%rbp)
        movq    %rdx, -24(%rbp)
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
        jb      .L87
        movl    $0, %eax
        call    swap_spaces
.L87:
        movq    to_space(%rip), %rax
        movq    %rax, current_to_space_pointer(%rip)
        movq    current_heap_pointer(%rip), %rax
        movq    -8(%rbp), %rdx
        salq    $3, %rdx
        leaq    (%rax,%rdx), %rcx
        movq    from_space(%rip), %rax
        movq    heap_size(%rip), %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        cmpq    %rax, %rcx
        jbe     .L88
        movl    $0, %eax
        call    print_heap
        movq    -24(%rbp), %rdx
        movq    -16(%rbp), %rax
        movq    %rdx, %rsi
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
        jbe     .L88
        movq    stderr(%rip), %rax
        movq    %rax, %rcx
        movl    $14, %edx
        movl    $1, %esi
        movl    $.LC10, %edi
        call    fwrite
        movl    $1, %edi
        call    exit
.L88:
        movq    current_heap_pointer(%rip), %rax
        leave
        ret