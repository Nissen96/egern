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
allocate_heap:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $48, %rsp
        movl    %edi, -20(%rbp)
        movq    %rsi, -32(%rbp)
        movq    %rdx, -40(%rbp)
        movl    %ecx, -24(%rbp)
        movl    -24(%rbp), %eax
        movslq  %eax, %rdx
        movq    -40(%rbp), %rax
        addq    %rdx, %rax
        movq    %rax, -8(%rbp)
        movq    -32(%rbp), %rax
        cmpq    -8(%rbp), %rax
        jb      .L3
        movq    -8(%rbp), %rdx
        movq    -40(%rbp), %rax
        movq    %rdx, %rsi
        movq    %rax, %rdi
        call    swap
.L3:
        movl    -20(%rbp), %eax
        movslq  %eax, %rdx
        movq    -32(%rbp), %rax
        addq    %rdx, %rax
        movq    %rax, -16(%rbp)
        movl    -20(%rbp), %eax
        movslq  %eax, %rdx
        movq    -32(%rbp), %rax
        leaq    (%rdx,%rax), %rcx
        movl    -24(%rbp), %eax
        movl    %eax, %edx
        shrl    $31, %edx
        addl    %edx, %eax
        sarl    %eax
        movslq  %eax, %rdx
        movq    -40(%rbp), %rax
        addq    %rdx, %rax
        cmpq    %rax, %rcx
        jb      .L4
        movl    $0, %eax
        call    collect_garbage
.L4:
        movl    -20(%rbp), %eax
        sall    $3, %eax
        addq    %rax, %rbx
        movq    -32(%rbp), %rax
        leave
        ret
collect_garbage:
        pushq   %rbp
        movq    %rsp, %rbp
        nop
        popq    %rbp
        ret
