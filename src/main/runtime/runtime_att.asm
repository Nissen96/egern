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
        movq    tospace(%rip), %rax
        movq    %rax, -8(%rbp)
        movq    fromspace(%rip), %rax
        movq    %rax, tospace(%rip)
        movq    -8(%rbp), %rax
        movq    %rax, fromspace(%rip)
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
        movq    tospace(%rip), %rax
        cmpq    %rax, -8(%rbp)
        jb      .L14
        movq    tospace(%rip), %rax
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
        movq    fromspace(%rip), %rax
        cmpq    %rax, -8(%rbp)
        jb      .L18
        movq    fromspace(%rip), %rax
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
        movq    current_tospace_pointer(%rip), %rax
        movq    -280(%rbp), %rcx
        movq    %rcx, %rsi
        movq    %rax, %rdi
        call    memcpy
        movl    $0, %eax
        leaq    0(,%rax,8), %rdx
        movq    -280(%rbp), %rax
        addq    %rdx, %rax
        movq    $-1, (%rax)
        movq    current_tospace_pointer(%rip), %rdx
        movl    $1, %eax
        leaq    0(,%rax,8), %rcx
        movq    -280(%rbp), %rax
        addq    %rcx, %rax
        movq    %rdx, (%rax)
        movq    current_tospace_pointer(%rip), %rax
        movl    -4(%rbp), %edx
        movslq  %edx, %rdx
        movl    $2, %ecx
        addq    %rcx, %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        movq    %rax, current_tospace_pointer(%rip)
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
        movq    current_tospace_pointer(%rip), %rax
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
        negl    %eax
        cltq
        leaq    0(,%rax,8), %rdx
        movq    -40(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, %rdi
        call    forward
        movl    -4(%rbp), %edx
        negl    %edx
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
visit_caller_saved_params:
        pushq   %rbp
        movq    %rsp, %rbp
        pushq   %r15
        pushq   %r14
        pushq   %r13
        pushq   %r12
        pushq   %rbx
        subq    $344, %rsp
        movq    %rdi, -376(%rbp)
        movq    %rsp, %rax
        movq    %rax, %rbx
        movq    -376(%rbp), %rax
        movq    (%rax), %rax
        movq    %rax, -64(%rbp)
        cmpq    $0, -64(%rbp)
        jne     .L37
        movq    %rbx, %rsp
        jmp     .L36
.L37:
        movl    $1, %eax
        movq    %rax, %rdx
        movl    $0, %eax
        subq    %rdx, %rax
        salq    $3, %rax
        movq    %rax, %rdx
        movq    -64(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movl    %eax, -68(%rbp)
        movl    $3, %eax
        salq    $3, %rax
        negq    %rax
        movq    %rax, %rdx
        movq    -64(%rbp), %rax
        addq    %rax, %rdx
        leaq    -368(%rbp), %rax
        movq    %rax, %rsi
        movq    %rdx, %rdi
        call    set_bitmap
        movl    -68(%rbp), %eax
        movl    $6, %edx
        subl    %edx, %eax
        movl    $0, %esi
        movl    %eax, %edi
        call    max
        movl    %eax, -72(%rbp)
        movl    $6, %eax
        movl    %eax, %edx
        movl    -68(%rbp), %eax
        movl    %edx, %esi
        movl    %eax, %edi
        call    min
        movl    %eax, -76(%rbp)
        movl    -76(%rbp), %eax
        movslq  %eax, %rdx
        subq    $1, %rdx
        movq    %rdx, -88(%rbp)
        movslq  %eax, %rdx
        movq    %rdx, %r14
        movl    $0, %r15d
        movslq  %eax, %rdx
        movq    %rdx, %r12
        movl    $0, %r13d
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
        movq    %rax, -96(%rbp)
        movl    $0, -52(%rbp)
        jmp     .L39
.L40:
        movl    -72(%rbp), %edx
        movl    -52(%rbp), %eax
        addl    %edx, %eax
        cltq
        movl    -368(%rbp,%rax,4), %ecx
        movq    -96(%rbp), %rax
        movl    -52(%rbp), %edx
        movslq  %edx, %rdx
        movl    %ecx, (%rax,%rdx,4)
        addl    $1, -52(%rbp)
.L39:
        movl    -52(%rbp), %eax
        cmpl    -76(%rbp), %eax
        jl      .L40
        movl    $1, %eax
        movq    %rax, %rdx
        movl    $0, %eax
        subq    %rdx, %rax
        salq    $3, %rax
        movq    %rax, %rdx
        movq    -376(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movl    %eax, -100(%rbp)
        movl    -100(%rbp), %eax
        movl    $6, %edx
        subl    %edx, %eax
        movl    $0, %esi
        movl    %eax, %edi
        call    max
        movl    %eax, -104(%rbp)
        movq    $-3, %rax
        movl    %eax, %edx
        movl    -104(%rbp), %eax
        subl    %eax, %edx
        movl    -100(%rbp), %eax
        subl    %eax, %edx
        movl    %edx, %eax
        subl    $7, %eax
        movl    %eax, -108(%rbp)
        movl    -108(%rbp), %eax
        cltq
        salq    $3, %rax
        negq    %rax
        movq    %rax, %rdx
        movq    -376(%rbp), %rax
        addq    %rax, %rdx
        movq    -96(%rbp), %rcx
        movl    -76(%rbp), %eax
        cltq
        movq    %rcx, %rsi
        movq    %rax, %rdi
        call    visit_params
        movq    %rbx, %rsp
.L36:
        leaq    -40(%rbp), %rsp
        popq    %rbx
        popq    %r12
        popq    %r13
        popq    %r14
        popq    %r15
        popq    %rbp
        ret
visit_variables:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $48, %rsp
        movq    %rdi, -24(%rbp)
        movq    %rsi, -32(%rbp)
        movq    %rdx, -40(%rbp)
        movl    $0, -4(%rbp)
        jmp     .L42
.L46:
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
        je      .L43
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
        je      .L47
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
.L43:
        addl    $1, -4(%rbp)
.L42:
        movl    -4(%rbp), %eax
        cltq
        cmpq    %rax, -24(%rbp)
        jg      .L46
        jmp     .L45
.L47:
        nop
.L45:
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
        subq    $472, %rsp
        movq    %rdi, -408(%rbp)
        movq    %rsi, -416(%rbp)
        movl    %edx, -420(%rbp)
        movq    %rsp, %rax
        movq    %rax, %r13
        movl    $1, %eax
        movq    %rax, %rdx
        movl    $0, %eax
        subq    %rdx, %rax
        salq    $3, %rax
        movq    %rax, %rdx
        movq    -408(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, -56(%rbp)
        movl    $2, %eax
        movq    %rax, %rdx
        movl    $0, %eax
        subq    %rdx, %rax
        salq    $3, %rax
        movq    %rax, %rdx
        movq    -408(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, -72(%rbp)
        movl    $3, %eax
        salq    $3, %rax
        negq    %rax
        movq    %rax, %rdx
        movq    -408(%rbp), %rax
        addq    %rax, %rdx
        leaq    -400(%rbp), %rax
        movq    %rax, %rsi
        movq    %rdx, %rdi
        call    set_bitmap
        movq    -56(%rbp), %rax
        movl    %eax, %edx
        movl    $6, %eax
        subl    %eax, %edx
        movl    %edx, %eax
        movl    $0, %esi
        movl    %eax, %edi
        call    max
        movl    %eax, -76(%rbp)
        movl    -76(%rbp), %edx
        movslq  %edx, %rax
        subq    $1, %rax
        movq    %rax, -88(%rbp)
        movslq  %edx, %rax
        movq    %rax, -448(%rbp)
        movq    $0, -440(%rbp)
        movslq  %edx, %rax
        movq    %rax, -464(%rbp)
        movq    $0, -456(%rbp)
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
        movl    $0, -64(%rbp)
        jmp     .L49
.L50:
        movl    -64(%rbp), %eax
        cltq
        movl    -400(%rbp,%rax,4), %ecx
        movq    -96(%rbp), %rax
        movl    -64(%rbp), %edx
        movslq  %edx, %rdx
        movl    %ecx, (%rax,%rdx,4)
        addl    $1, -64(%rbp)
.L49:
        movl    -64(%rbp), %eax
        cmpl    -76(%rbp), %eax
        jl      .L50
        movl    $6, %eax
        movl    %eax, %edx
        movq    -56(%rbp), %rax
        movl    %edx, %esi
        movl    %eax, %edi
        call    min
        movl    %eax, -100(%rbp)
        movl    -100(%rbp), %eax
        movq    %rsp, %rdx
        movq    %rdx, %rbx
        movslq  %eax, %rdx
        subq    $1, %rdx
        movq    %rdx, -112(%rbp)
        movslq  %eax, %rdx
        movq    %rdx, -480(%rbp)
        movq    $0, -472(%rbp)
        movslq  %eax, %rdx
        movq    %rdx, -496(%rbp)
        movq    $0, -488(%rbp)
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
        movq    %rax, -120(%rbp)
        movl    $0, -60(%rbp)
        jmp     .L51
.L52:
        movl    -64(%rbp), %eax
        leal    1(%rax), %edx
        movl    %edx, -64(%rbp)
        movl    -100(%rbp), %edx
        subl    -60(%rbp), %edx
        subl    $1, %edx
        cltq
        movl    -400(%rbp,%rax,4), %ecx
        movq    -120(%rbp), %rax
        movslq  %edx, %rdx
        movl    %ecx, (%rax,%rdx,4)
        addl    $1, -60(%rbp)
.L51:
        movl    -60(%rbp), %eax
        cmpl    -100(%rbp), %eax
        jl      .L52
        movq    -72(%rbp), %rax
        movq    %rsp, %rdx
        movq    %rdx, %r12
        leaq    -1(%rax), %rdx
        movq    %rdx, -128(%rbp)
        movq    %rax, %rdx
        movq    %rdx, -512(%rbp)
        movq    $0, -504(%rbp)
        movq    %rax, %rdx
        movq    %rdx, %r14
        movl    $0, %r15d
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
        movq    %rax, -136(%rbp)
        movl    $0, -60(%rbp)
        jmp     .L53
.L54:
        movl    -64(%rbp), %eax
        leal    1(%rax), %edx
        movl    %edx, -64(%rbp)
        cltq
        movl    -400(%rbp,%rax,4), %ecx
        movq    -136(%rbp), %rax
        movl    -60(%rbp), %edx
        movslq  %edx, %rdx
        movl    %ecx, (%rax,%rdx,4)
        addl    $1, -60(%rbp)
.L53:
        movl    -60(%rbp), %eax
        cltq
        cmpq    %rax, -72(%rbp)
        jg      .L54
        movl    $4, %eax
        salq    $3, %rax
        negq    %rax
        movq    %rax, %rdx
        movq    -408(%rbp), %rax
        addq    %rax, %rdx
        movq    -136(%rbp), %rcx
        movq    -72(%rbp), %rax
        movq    %rcx, %rsi
        movq    %rax, %rdi
        call    visit_variables
        cmpl    $0, -420(%rbp)
        je      .L55
        movq    -416(%rbp), %rax
        leaq    64(%rax), %rdx
        movq    -120(%rbp), %rcx
        movl    -100(%rbp), %eax
        cltq
        movq    %rcx, %rsi
        movq    %rax, %rdi
        call    visit_params
.L55:
        movq    -408(%rbp), %rax
        movq    %rax, %rdi
        call    visit_caller_saved_params
        movl    $6, %eax
        cmpq    %rax, -56(%rbp)
        jl      .L56
        movq    $-3, %rax
        salq    $3, %rax
        negq    %rax
        movq    %rax, %rdx
        movq    -408(%rbp), %rax
        addq    %rax, %rdx
        movq    -96(%rbp), %rcx
        movl    -76(%rbp), %eax
        cltq
        movq    %rcx, %rsi
        movq    %rax, %rdi
        call    visit_params
.L56:
        movq    %r12, %rsp
        movq    %rbx, %rsp
        movq    %r13, %rsp
        nop
        leaq    -40(%rbp), %rsp
        popq    %rbx
        popq    %r12
        popq    %r13
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
        movq    tospace(%rip), %rax
        movq    %rax, scan(%rip)
        movl    $1, -4(%rbp)
        jmp     .L58
.L59:
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
.L58:
        cmpq    $0, -24(%rbp)
        jne     .L59
        movq    heap_size(%rip), %rax
        salq    $3, %rax
        movq    %rax, %rdx
        movq    fromspace(%rip), %rax
        movl    $0, %esi
        movq    %rax, %rdi
        call    memset
        movq    current_tospace_pointer(%rip), %rax
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
        movq    heap_pointer(%rip), %rax
        movq    %rax, fromspace(%rip)
        movq    fromspace(%rip), %rax
        movq    heap_size(%rip), %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        movq    %rax, tospace(%rip)
        movq    current_heap_pointer(%rip), %rdx
        movq    tospace(%rip), %rax
        cmpq    %rax, %rdx
        jbe     .L61
        movl    $0, %eax
        call    swap_spaces
.L61:
        movq    tospace(%rip), %rax
        movq    %rax, current_tospace_pointer(%rip)
        movq    current_heap_pointer(%rip), %rax
        movq    -24(%rbp), %rdx
        salq    $3, %rdx
        leaq    (%rax,%rdx), %rcx
        movq    fromspace(%rip), %rax
        movq    heap_size(%rip), %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        cmpq    %rax, %rcx
        jbe     .L62
        movq    -40(%rbp), %rdx
        movq    -32(%rbp), %rax
        movq    %rdx, %rsi
        movq    %rax, %rdi
        call    collect_garbage
        movq    current_heap_pointer(%rip), %rax
        movq    -24(%rbp), %rdx
        salq    $3, %rdx
        leaq    (%rax,%rdx), %rcx
        movq    tospace(%rip), %rax
        movq    heap_size(%rip), %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        cmpq    %rax, %rcx
        jbe     .L62
        movq    stderr(%rip), %rax
        movq    %rax, %rcx
        movl    $14, %edx
        movl    $1, %esi
        movl    $.LC0, %edi
        call    fwrite
        movl    $1, %edi
        call    exit
.L62:
        movq    current_heap_pointer(%rip), %rax
        movq    %rax, -8(%rbp)
        movq    current_heap_pointer(%rip), %rax
        movq    -24(%rbp), %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        movq    %rax, current_heap_pointer(%rip)
        movq    -8(%rbp), %rax
        leave
        ret