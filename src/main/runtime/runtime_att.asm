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
        pushq   %rbp
        movq    %rsp, %rbp
        movq    %rdi, -40(%rbp)
        movq    %rsi, -48(%rbp)
        movq    -40(%rbp), %rax
        movq    %rax, -16(%rbp)
        movl    $7, -4(%rbp)
        jmp     .L2
.L5:
        movl    $7, -8(%rbp)
        jmp     .L3
.L4:
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
.L3:
        cmpl    $0, -8(%rbp)
        jns     .L4
        subl    $1, -4(%rbp)
.L2:
        cmpl    $0, -4(%rbp)
        jns     .L5
        nop
        nop
        popq    %rbp
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
        jg      .L8
        movl    -4(%rbp), %eax
        jmp     .L9
.L8:
        movl    -8(%rbp), %eax
.L9:
        popq    %rbp
        ret
max:
        pushq   %rbp
        movq    %rsp, %rbp
        movl    %edi, -4(%rbp)
        movl    %esi, -8(%rbp)
        movl    -4(%rbp), %eax
        cmpl    -8(%rbp), %eax
        jl      .L11
        movl    -4(%rbp), %eax
        jmp     .L12
.L11:
        movl    -8(%rbp), %eax
.L12:
        popq    %rbp
        ret
in_to_space:
        pushq   %rbp
        movq    %rsp, %rbp
        movq    %rdi, -8(%rbp)
        cmpq    $0, -8(%rbp)
        je      .L14
        movq    to_space(%rip), %rax
        cmpq    %rax, -8(%rbp)
        jb      .L14
        movq    to_space(%rip), %rax
        movq    heap_size(%rip), %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        cmpq    %rax, -8(%rbp)
        jnb     .L14
        movl    $1, %eax
        jmp     .L16
.L14:
        movl    $0, %eax
.L16:
        popq    %rbp
        ret
in_from_space:
        pushq   %rbp
        movq    %rsp, %rbp
        movq    %rdi, -8(%rbp)
        cmpq    $0, -8(%rbp)
        je      .L18
        movq    from_space(%rip), %rax
        cmpq    %rax, -8(%rbp)
        jb      .L18
        movq    from_space(%rip), %rax
        movq    heap_size(%rip), %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        cmpq    %rax, -8(%rbp)
        jnb     .L18
        movl    $1, %eax
        jmp     .L20
.L18:
        movl    $0, %eax
.L20:
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
        jne     .L22
        movq    -280(%rbp), %rax
        jmp     .L25
.L22:
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
        je      .L24
        movl    $1, %eax
        leaq    0(,%rax,8), %rdx
        movq    -280(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        jmp     .L25
.L24:
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
.L25:
        leave
        ret
forward_heap_fields:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $272, %rsp
        jmp     .L27
.L31:
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
        jmp     .L28
.L30:
        movl    -4(%rbp), %eax
        cltq
        movl    -272(%rbp,%rax,4), %eax
        movl    %eax, -12(%rbp)
        cmpl    $0, -12(%rbp)
        je      .L29
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
.L29:
        addl    $1, -4(%rbp)
.L28:
        movl    -4(%rbp), %eax
        cmpl    -8(%rbp), %eax
        jl      .L30
        movq    scan(%rip), %rax
        movl    -8(%rbp), %edx
        movslq  %edx, %rdx
        movl    $2, %ecx
        addq    %rcx, %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        movq    %rax, scan(%rip)
.L27:
        movq    scan(%rip), %rdx
        movq    current_to_space_pointer(%rip), %rax
        cmpq    %rax, %rdx
        jb      .L31
        nop
        nop
        leave
        ret
visit_params:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $48, %rsp
        movq    %rdi, -24(%rbp)
        movq    %rsi, -32(%rbp)
        movq    %rdx, -40(%rbp)
        movl    $0, -4(%rbp)
        jmp     .L33
.L35:
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
        je      .L34
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
.L34:
        addl    $1, -4(%rbp)
.L33:
        movl    -4(%rbp), %eax
        cltq
        cmpq    %rax, -24(%rbp)
        jg      .L35
        movl    $0, %eax
        call    forward_heap_fields
        nop
        leave
        ret
visit_variables:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $48, %rsp
        movq    %rdi, -24(%rbp)
        movq    %rsi, -32(%rbp)
        movq    %rdx, -40(%rbp)
        movl    $0, -4(%rbp)
        jmp     .L37
.L41:
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
        je      .L38
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
        je      .L42
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
.L38:
        addl    $1, -4(%rbp)
.L37:
        movl    -4(%rbp), %eax
        cltq
        cmpq    %rax, -24(%rbp)
        jg      .L41
        jmp     .L40
.L42:
        nop
.L40:
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
        pushq   %r12
        pushq   %rbx
        subq    $784, %rsp
        movq    %rdi, -680(%rbp)
        movq    %rsi, -688(%rbp)
        movl    %edx, -692(%rbp)
        movq    %rsp, %rax
        movq    %rax, %r12
        movl    $1, %eax
        movq    %rax, %rdx
        movl    $0, %eax
        subq    %rdx, %rax
        salq    $3, %rax
        movq    %rax, %rdx
        movq    -680(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, -48(%rbp)
        movl    $2, %eax
        movq    %rax, %rdx
        movl    $0, %eax
        subq    %rdx, %rax
        salq    $3, %rax
        movq    %rax, %rdx
        movq    -680(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, -40(%rbp)
        movl    $3, %eax
        salq    $3, %rax
        negq    %rax
        movq    %rax, %rdx
        movq    -680(%rbp), %rax
        addq    %rax, %rdx
        leaq    -416(%rbp), %rax
        movq    %rax, %rsi
        movq    %rdx, %rdi
        call    set_bitmap
        movl    $6, %eax
        movl    %eax, %edx
        movq    -48(%rbp), %rax
        movl    %edx, %esi
        movl    %eax, %edi
        call    min
        movl    %eax, -60(%rbp)
        movq    -48(%rbp), %rax
        movl    %eax, %edx
        movl    $6, %eax
        subl    %eax, %edx
        movl    %edx, %eax
        movl    $0, %esi
        movl    %eax, %edi
        call    max
        movl    %eax, -64(%rbp)
        movl    -60(%rbp), %edx
        movslq  %edx, %rax
        subq    $1, %rax
        movq    %rax, -72(%rbp)
        movslq  %edx, %rax
        movq    %rax, %r14
        movl    $0, %r15d
        movslq  %edx, %rax
        movq    %rax, -720(%rbp)
        movq    $0, -712(%rbp)
        movslq  %edx, %rax
        leaq    0(,%rax,4), %rdx
        movl    $16, %eax
        subq    $1, %rax
        addq    %rdx, %rax
        movl    $16, %ebx
        movl    $0, %edx
        divq    %rbx
        imulq   $16, %rax, %rax
        subq    %rax, %rsp
        movq    %rsp, %rax
        addq    $3, %rax
        shrq    $2, %rax
        salq    $2, %rax
        movq    %rax, -80(%rbp)
        movl    -64(%rbp), %edx
        movslq  %edx, %rax
        subq    $1, %rax
        movq    %rax, -88(%rbp)
        movslq  %edx, %rax
        movq    %rax, -736(%rbp)
        movq    $0, -728(%rbp)
        movslq  %edx, %rax
        movq    %rax, -752(%rbp)
        movq    $0, -744(%rbp)
        movslq  %edx, %rax
        leaq    0(,%rax,4), %rdx
        movl    $16, %eax
        subq    $1, %rax
        addq    %rdx, %rax
        movl    $16, %ebx
        movl    $0, %edx
        divq    %rbx
        imulq   $16, %rax, %rax
        subq    %rax, %rsp
        movq    %rsp, %rax
        addq    $3, %rax
        shrq    $2, %rax
        salq    $2, %rax
        movq    %rax, -96(%rbp)
        movq    -40(%rbp), %rax
        leaq    -1(%rax), %rdx
        movq    %rdx, -104(%rbp)
        movq    %rax, %rdx
        movq    %rdx, -768(%rbp)
        movq    $0, -760(%rbp)
        movq    %rax, %rdx
        movq    %rdx, -784(%rbp)
        movq    $0, -776(%rbp)
        leaq    0(,%rax,4), %rdx
        movl    $16, %eax
        subq    $1, %rax
        addq    %rdx, %rax
        movl    $16, %ebx
        movl    $0, %edx
        divq    %rbx
        imulq   $16, %rax, %rax
        subq    %rax, %rsp
        movq    %rsp, %rax
        addq    $3, %rax
        shrq    $2, %rax
        salq    $2, %rax
        movq    %rax, -112(%rbp)
        movl    $0, -56(%rbp)
        jmp     .L44
.L45:
        movl    -56(%rbp), %eax
        cltq
        movl    -416(%rbp,%rax,4), %ecx
        movq    -96(%rbp), %rax
        movl    -56(%rbp), %edx
        movslq  %edx, %rdx
        movl    %ecx, (%rax,%rdx,4)
        addl    $1, -56(%rbp)
.L44:
        movl    -56(%rbp), %eax
        cmpl    -64(%rbp), %eax
        jl      .L45
        movl    $0, -52(%rbp)
        jmp     .L46
.L47:
        movl    -56(%rbp), %eax
        leal    1(%rax), %edx
        movl    %edx, -56(%rbp)
        movl    -60(%rbp), %edx
        subl    -52(%rbp), %edx
        subl    $1, %edx
        cltq
        movl    -416(%rbp,%rax,4), %ecx
        movq    -80(%rbp), %rax
        movslq  %edx, %rdx
        movl    %ecx, (%rax,%rdx,4)
        addl    $1, -52(%rbp)
.L46:
        movl    -52(%rbp), %eax
        cmpl    -60(%rbp), %eax
        jl      .L47
        movl    $0, -52(%rbp)
        jmp     .L48
.L49:
        movl    -56(%rbp), %eax
        leal    1(%rax), %edx
        movl    %edx, -56(%rbp)
        cltq
        movl    -416(%rbp,%rax,4), %ecx
        movq    -112(%rbp), %rax
        movl    -52(%rbp), %edx
        movslq  %edx, %rdx
        movl    %ecx, (%rax,%rdx,4)
        addl    $1, -52(%rbp)
.L48:
        movl    -52(%rbp), %eax
        cltq
        cmpq    %rax, -40(%rbp)
        jg      .L49
        movl    $4, %eax
        salq    $3, %rax
        negq    %rax
        movq    %rax, %rdx
        movq    -680(%rbp), %rax
        addq    %rax, %rdx
        movq    -112(%rbp), %rcx
        movq    -40(%rbp), %rax
        movq    %rcx, %rsi
        movq    %rax, %rdi
        call    visit_variables
        cmpl    $0, -692(%rbp)
        je      .L50
        movq    -688(%rbp), %rax
        leaq    16(%rax), %rdx
        movq    -80(%rbp), %rcx
        movl    -60(%rbp), %eax
        cltq
        movq    %rcx, %rsi
        movq    %rax, %rdi
        call    visit_params
.L50:
        movq    -680(%rbp), %rax
        movq    (%rax), %rax
        movq    %rax, -120(%rbp)
        cmpq    $0, -120(%rbp)
        je      .L51
        movq    %rsp, %rax
        movq    %rax, %rbx
        movl    $1, %eax
        movq    %rax, %rdx
        movl    $0, %eax
        subq    %rdx, %rax
        salq    $3, %rax
        movq    %rax, %rdx
        movq    -120(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, -128(%rbp)
        movl    $3, %eax
        salq    $3, %rax
        negq    %rax
        movq    %rax, %rdx
        movq    -120(%rbp), %rax
        addq    %rax, %rdx
        leaq    -672(%rbp), %rax
        movq    %rax, %rsi
        movq    %rdx, %rdi
        call    set_bitmap
        movq    -128(%rbp), %rax
        movl    %eax, %edx
        movl    $6, %eax
        subl    %eax, %edx
        movl    %edx, %eax
        movl    $0, %esi
        movl    %eax, %edi
        call    max
        movl    %eax, -132(%rbp)
        movl    $6, %eax
        movl    %eax, %edx
        movq    -48(%rbp), %rax
        movl    %edx, %esi
        movl    %eax, %edi
        call    min
        movl    %eax, -136(%rbp)
        movl    -136(%rbp), %eax
        movslq  %eax, %rdx
        subq    $1, %rdx
        movq    %rdx, -144(%rbp)
        movslq  %eax, %rdx
        movq    %rdx, -800(%rbp)
        movq    $0, -792(%rbp)
        movslq  %eax, %rdx
        movq    %rdx, -816(%rbp)
        movq    $0, -808(%rbp)
        cltq
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
        movq    %rax, -152(%rbp)
        movl    $0, -56(%rbp)
        jmp     .L52
.L53:
        movl    -132(%rbp), %edx
        movl    -56(%rbp), %eax
        addl    %edx, %eax
        cltq
        movl    -672(%rbp,%rax,4), %ecx
        movq    -152(%rbp), %rax
        movl    -56(%rbp), %edx
        movslq  %edx, %rdx
        movl    %ecx, (%rax,%rdx,4)
        addl    $1, -56(%rbp)
.L52:
        movl    -56(%rbp), %eax
        cmpl    -136(%rbp), %eax
        jl      .L53
        movl    -132(%rbp), %eax
        cltq
        movq    $-3, %rdx
        subq    %rdx, %rax
        leaq    0(,%rax,8), %rdx
        movq    -680(%rbp), %rax
        addq    %rax, %rdx
        movq    -152(%rbp), %rcx
        movl    -136(%rbp), %eax
        cltq
        movq    %rcx, %rsi
        movq    %rax, %rdi
        call    visit_params
        movq    %rbx, %rsp
.L51:
        movl    $6, %eax
        cmpq    %rax, -48(%rbp)
        jl      .L54
        movq    $-3, %rax
        salq    $3, %rax
        negq    %rax
        movq    %rax, %rdx
        movq    -680(%rbp), %rax
        addq    %rax, %rdx
        movq    -96(%rbp), %rcx
        movl    -64(%rbp), %eax
        cltq
        movq    %rcx, %rsi
        movq    %rax, %rdi
        call    visit_params
.L54:
        movq    %r12, %rsp
        nop
        leaq    -32(%rbp), %rsp
        popq    %rbx
        popq    %r12
        popq    %r14
        popq    %r15
        popq    %rbp
        ret
collect_garbage:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $32, %rsp
        movq    %rdi, -24(%rbp)
        movq    %rsi, -32(%rbp)
        movq    to_space(%rip), %rax
        movq    %rax, scan(%rip)
        movl    $1, -4(%rbp)
        jmp     .L56
.L57:
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
.L56:
        cmpq    $0, -24(%rbp)
        jne     .L57
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
.LC0:
        .string "Out of memory\n"
allocate_heap:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $48, %rsp
        movq    %rdi, -24(%rbp)
        movq    %rsi, -32(%rbp)
        movq    %rdx, -40(%rbp)
        movq    -24(%rbp), %rax
        movq    (%rax), %rax
        movq    %rax, -8(%rbp)
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
        jbe     .L59
        movl    $0, %eax
        call    swap_spaces
.L59:
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
        jbe     .L60
        movq    -40(%rbp), %rdx
        movq    -32(%rbp), %rax
        movq    %rdx, %rsi
        movq    %rax, %rdi
        call    collect_garbage
        movq    current_heap_pointer(%rip), %rax
        movq    -8(%rbp), %rdx
        salq    $3, %rdx
        leaq    (%rax,%rdx), %rcx
        movq    to_space(%rip), %rax
        movq    heap_size(%rip), %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        cmpq    %rax, %rcx
        jbe     .L60
        movq    stderr(%rip), %rax
        movq    %rax, %rcx
        movl    $14, %edx
        movl    $1, %esi
        movl    $.LC0, %edi
        call    fwrite
        movl    $1, %edi
        call    exit
.L60:
        movq    current_heap_pointer(%rip), %rax
        movq    %rax, -16(%rbp)
        movq    current_heap_pointer(%rip), %rax
        movq    -8(%rbp), %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        movq    %rax, current_heap_pointer(%rip)
        movq    -16(%rbp), %rax
        leave
        ret