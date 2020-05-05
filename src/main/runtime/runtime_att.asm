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
        pushq   %rbp
        movq    %rsp, %rbp
        movq    %rdi, -24(%rbp)
        movq    %rsi, -32(%rbp)
        movq    -24(%rbp), %rax
        movq    %rax, -8(%rbp)
        movq    -32(%rbp), %rax
        movq    %rax, -24(%rbp)
        movq    -8(%rbp), %rax
        movq    %rax, -32(%rbp)
        nop
        popq    %rbp
        ret
collect_garbage:
        pushq   %rbp
        movq    %rsp, %rbp
        movq    %rdi, -8(%rbp)
        nop
        popq    %rbp
        ret
allocate_heap:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $48, %rsp
        movl    %edi, -20(%rbp)
        movq    %rsi, -32(%rbp)
        movq    %rdx, -40(%rbp)
        movl    %ecx, -24(%rbp)
        movq    %r8, -48(%rbp)
        movl    -24(%rbp), %eax
        movslq  %eax, %rdx
        movq    -40(%rbp), %rax
        addq    %rdx, %rax
        movq    %rax, -8(%rbp)
        movq    -32(%rbp), %rax
        cmpq    -8(%rbp), %rax
        jb      .L4
        movq    -8(%rbp), %rdx
        movq    -40(%rbp), %rax
        movq    %rdx, %rsi
        movq    %rax, %rdi
        call    swap
.L4:
        movl    -20(%rbp), %eax
        movslq  %eax, %rdx
        movq    -32(%rbp), %rax
        leaq    (%rdx,%rax), %rcx
        movl    -24(%rbp), %eax
        movslq  %eax, %rdx
        movq    -40(%rbp), %rax
        addq    %rdx, %rax
        cmpq    %rax, %rcx
        jbe     .L5
        movq    -48(%rbp), %rax
        movq    %rax, %rdi
        call    collect_garbage
.L5:
        movl    -20(%rbp), %eax
        movslq  %eax, %rdx
        movq    -32(%rbp), %rax
        leaq    (%rdx,%rax), %rcx
        movl    -24(%rbp), %eax
        movslq  %eax, %rdx
        movq    -40(%rbp), %rax
        addq    %rdx, %rax
        cmpq    %rax, %rcx
        jbe     .L6
        movq    $-1, %rax
        jmp     .L7
.L6:
        movq    -32(%rbp), %rax
.L7:
        leave
        ret