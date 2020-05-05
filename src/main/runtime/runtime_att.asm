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
.LC0:
        .string "Bitmap:         "
.LC1:
        .string "%d"
print_bitmap:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $32, %rsp
        movl    %edi, -20(%rbp)
        movq    %rsi, -32(%rbp)
        movl    $.LC0, %edi
        movl    $0, %eax
        call    printf
        movl    $0, -4(%rbp)
        jmp     .L7
.L8:
        movl    -4(%rbp), %eax
        cltq
        leaq    0(,%rax,4), %rdx
        movq    -32(%rbp), %rax
        addq    %rdx, %rax
        movl    (%rax), %eax
        movl    %eax, %esi
        movl    $.LC1, %edi
        movl    $0, %eax
        call    printf
        addl    $1, -4(%rbp)
.L7:
        movl    -4(%rbp), %eax
        cmpl    -20(%rbp), %eax
        jl      .L8
        movl    $10, %edi
        call    putchar
        nop
        leave
        ret
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
forward:
        pushq   %rbp
        movq    %rsp, %rbp
        movq    %rdi, -8(%rbp)
        nop
        popq    %rbp
        ret
.LC2:
        .string "Var %d = %d is pointer\n"
visit_pointer_vars:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $48, %rsp
        movq    %rdi, -24(%rbp)
        movq    %rsi, -32(%rbp)
        movq    %rdx, -40(%rbp)
        movl    $0, -4(%rbp)
        jmp     .L12
.L14:
        movl    -4(%rbp), %eax
        cltq
        leaq    0(,%rax,4), %rdx
        movq    -32(%rbp), %rax
        addq    %rdx, %rax
        movl    (%rax), %eax
        movl    %eax, -8(%rbp)
        cmpl    $0, -8(%rbp)
        je      .L13
        movl    -4(%rbp), %eax
        cltq
        subq    -24(%rbp), %rax
        addq    $1, %rax
        leaq    0(,%rax,8), %rdx
        movq    -40(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, -16(%rbp)
        movq    -16(%rbp), %rdx
        movl    -4(%rbp), %eax
        movl    %eax, %esi
        movl    $.LC2, %edi
        movl    $0, %eax
        call    printf
        movq    -16(%rbp), %rax
        movq    %rax, %rdi
        call    forward
.L13:
        addl    $1, -4(%rbp)
.L12:
        movl    -4(%rbp), %eax
        cltq
        cmpq    %rax, -24(%rbp)
        jg      .L14
        nop
        nop
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
.LC3:
        .string "Num parameters: %d\n"
.LC4:
        .string "Num variables:  %d\n"
scan_stack_frame:
        pushq   %rbp
        movq    %rsp, %rbp
        pushq   %r15
        pushq   %r14
        pushq   %r13
        pushq   %r12
        pushq   %rbx
        subq    $376, %rsp
        movq    %rdi, -376(%rbp)
        movq    %rsp, %rax
        movq    %rax, %rbx
        movl    $1, %eax
        salq    $3, %rax
        negq    %rax
        movq    %rax, %rdx
        movq    -376(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, -56(%rbp)
        movl    $2, %eax
        salq    $3, %rax
        negq    %rax
        movq    %rax, %rdx
        movq    -376(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, -72(%rbp)
        movl    $3, %eax
        salq    $3, %rax
        negq    %rax
        movq    %rax, %rdx
        movq    -376(%rbp), %rax
        addq    %rax, %rdx
        leaq    -368(%rbp), %rax
        movq    %rax, %rsi
        movq    %rdx, %rdi
        call    set_bitmap
        movq    -56(%rbp), %rax
        movq    %rax, %rsi
        movl    $.LC3, %edi
        movl    $0, %eax
        call    printf
        movq    -72(%rbp), %rax
        movq    %rax, %rsi
        movl    $.LC4, %edi
        movl    $0, %eax
        call    printf
        movq    -56(%rbp), %rax
        movl    %eax, %edx
        movq    -72(%rbp), %rax
        addl    %edx, %eax
        movl    %eax, %edx
        leaq    -368(%rbp), %rax
        movq    %rax, %rsi
        movl    %edx, %edi
        call    print_bitmap
        movq    -56(%rbp), %rax
        leaq    -1(%rax), %rdx
        movq    %rdx, -80(%rbp)
        movq    %rax, %rdx
        movq    %rdx, -400(%rbp)
        movq    $0, -392(%rbp)
        movq    %rax, %rdx
        movq    %rdx, -416(%rbp)
        movq    $0, -408(%rbp)
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
        movq    %rax, -88(%rbp)
        movq    -72(%rbp), %rax
        leaq    -1(%rax), %rdx
        movq    %rdx, -96(%rbp)
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
        movl    $16, %ecx
        movl    $0, %edx
        divq    %rcx
        imulq   $16, %rax, %rax
        subq    %rax, %rsp
        movq    %rsp, %rax
        addq    $3, %rax
        shrq    $2, %rax
        salq    $2, %rax
        movq    %rax, -104(%rbp)
        movl    $0, -64(%rbp)
        jmp     .L17
.L18:
        movl    -64(%rbp), %eax
        cltq
        movl    -368(%rbp,%rax,4), %ecx
        movq    -88(%rbp), %rax
        movl    -64(%rbp), %edx
        movslq  %edx, %rdx
        movl    %ecx, (%rax,%rdx,4)
        addl    $1, -64(%rbp)
.L17:
        movl    -64(%rbp), %eax
        cltq
        cmpq    %rax, -56(%rbp)
        jg      .L18
        movl    $0, -60(%rbp)
        jmp     .L19
.L20:
        movl    -64(%rbp), %eax
        leal    1(%rax), %edx
        movl    %edx, -64(%rbp)
        cltq
        movl    -368(%rbp,%rax,4), %ecx
        movq    -104(%rbp), %rax
        movl    -60(%rbp), %edx
        movslq  %edx, %rdx
        movl    %ecx, (%rax,%rdx,4)
        addl    $1, -60(%rbp)
.L19:
        movl    -60(%rbp), %eax
        cltq
        cmpq    %rax, -72(%rbp)
        jg      .L20
        movl    $4, %eax
        salq    $3, %rax
        negq    %rax
        movq    %rax, %rdx
        movq    -376(%rbp), %rax
        addq    %rax, %rdx
        movq    -104(%rbp), %rcx
        movq    -72(%rbp), %rax
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
.LC5:
        .string "Collecting Garbage:"
.LC6:
        .string "Done!"
collect_garbage:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $16, %rsp
        movq    %rdi, -8(%rbp)
        movl    $.LC5, %edi
        call    puts
        jmp     .L22
.L23:
        movq    -8(%rbp), %rax
        movq    %rax, %rdi
        call    scan_stack_frame
        movq    -8(%rbp), %rax
        movq    (%rax), %rax
        movq    %rax, -8(%rbp)
.L22:
        cmpq    $0, -8(%rbp)
        jne     .L23
        movl    $.LC6, %edi
        call    puts
        nop
        leave
        ret
.LC7:
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
        movq    -16(%rbp), %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        movq    %rax, to_space(%rip)
        movq    current_heap_pointer(%rip), %rdx
        movq    to_space(%rip), %rax
        cmpq    %rax, %rdx
        jb      .L25
        movq    to_space(%rip), %rdx
        movq    from_space(%rip), %rax
        movq    %rdx, %rsi
        movq    %rax, %rdi
        call    swap
.L25:
        movq    current_heap_pointer(%rip), %rax
        movq    -8(%rbp), %rdx
        salq    $3, %rdx
        leaq    (%rax,%rdx), %rcx
        movq    from_space(%rip), %rax
        movq    -16(%rbp), %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        cmpq    %rax, %rcx
        jbe     .L26
        movq    -24(%rbp), %rax
        movq    %rax, %rdi
        call    collect_garbage
.L26:
        movq    current_heap_pointer(%rip), %rax
        movq    -8(%rbp), %rdx
        salq    $3, %rdx
        leaq    (%rax,%rdx), %rcx
        movq    from_space(%rip), %rax
        movq    -16(%rbp), %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        cmpq    %rax, %rcx
        jbe     .L27
        movq    stderr(%rip), %rax
        movq    %rax, %rcx
        movl    $14, %edx
        movl    $1, %esi
        movl    $.LC7, %edi
        call    fwrite
        movl    $1, %edi
        call    exit
.L27:
        movq    heap_pointer(%rip), %rax
        leave
        ret