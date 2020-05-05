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
.LC0:
        .string "Bitmap:         "
.LC1:
        .string "%u"
.LC2:
        .string ""
printBitmap:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $48, %rsp
        movq    %rdi, -40(%rbp)
        movq    %rsi, -48(%rbp)
        movq    -48(%rbp), %rax
        movq    %rax, -16(%rbp)
        movl    $.LC0, %edi
        movl    $0, %eax
        call    printf
        movq    -40(%rbp), %rax
        subl    $1, %eax
        movl    %eax, -4(%rbp)
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
        movzbl  -17(%rbp), %eax
        movl    %eax, %esi
        movl    $.LC1, %edi
        movl    $0, %eax
        call    printf
        subl    $1, -8(%rbp)
.L3:
        cmpl    $0, -8(%rbp)
        jns     .L4
        subl    $1, -4(%rbp)
.L2:
        cmpl    $0, -4(%rbp)
        jns     .L5
        movl    $.LC2, %edi
        call    puts
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
.LC3:
        .string "Num parameters: %d\n"
.LC4:
        .string "Num variables:  %d\n"
scan_stack_frame:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $16, %rsp
        movq    %rdi, -8(%rbp)
        movl    $1, %eax
        salq    $3, %rax
        negq    %rax
        movq    %rax, %rdx
        movq    -8(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, %rsi
        movl    $.LC3, %edi
        movl    $0, %eax
        call    printf
        movl    $2, %eax
        salq    $3, %rax
        negq    %rax
        movq    %rax, %rdx
        movq    -8(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, %rsi
        movl    $.LC4, %edi
        movl    $0, %eax
        call    printf
        movl    $3, %eax
        salq    $3, %rax
        negq    %rax
        movq    %rax, %rdx
        movq    -8(%rbp), %rax
        addq    %rdx, %rax
        movq    %rax, %rsi
        movl    $8, %edi
        call    printBitmap
        nop
        leave
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
        jmp     .L9
.L10:
        movq    -8(%rbp), %rax
        movq    %rax, %rdi
        call    scan_stack_frame
        movq    -8(%rbp), %rax
        movq    (%rax), %rax
        movq    %rax, -8(%rbp)
.L9:
        cmpq    $0, -8(%rbp)
        jne     .L10
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
        subq    $64, %rsp
        movq    %rdi, -24(%rbp)
        movq    %rsi, -32(%rbp)
        movq    %rdx, -40(%rbp)
        movq    %rcx, -48(%rbp)
        movq    %r8, -56(%rbp)
        movq    -24(%rbp), %rax
        shrq    $3, %rax
        movq    %rax, -24(%rbp)
        movq    -48(%rbp), %rax
        shrq    $3, %rax
        movq    %rax, -48(%rbp)
        movq    -48(%rbp), %rax
        leaq    0(,%rax,8), %rdx
        movq    -40(%rbp), %rax
        addq    %rdx, %rax
        movq    %rax, -8(%rbp)
        movq    -32(%rbp), %rax
        cmpq    -8(%rbp), %rax
        jb      .L12
        movq    -8(%rbp), %rdx
        movq    -40(%rbp), %rax
        movq    %rdx, %rsi
        movq    %rax, %rdi
        call    swap
.L12:
        movq    -24(%rbp), %rax
        leaq    0(,%rax,8), %rdx
        movq    -32(%rbp), %rax
        addq    %rax, %rdx
        movq    -48(%rbp), %rax
        leaq    0(,%rax,8), %rcx
        movq    -40(%rbp), %rax
        addq    %rcx, %rax
        cmpq    %rax, %rdx
        jbe     .L13
        movq    -56(%rbp), %rax
        movq    %rax, %rdi
        call    collect_garbage
.L13:
        movq    -24(%rbp), %rax
        leaq    0(,%rax,8), %rdx
        movq    -32(%rbp), %rax
        addq    %rax, %rdx
        movq    -48(%rbp), %rax
        leaq    0(,%rax,8), %rcx
        movq    -40(%rbp), %rax
        addq    %rcx, %rax
        cmpq    %rax, %rdx
        jbe     .L14
        movq    stderr(%rip), %rax
        movq    %rax, %rcx
        movl    $14, %edx
        movl    $1, %esi
        movl    $.LC7, %edi
        call    fwrite
        movl    $1, %edi
        call    exit
.L14:
        movq    -32(%rbp), %rax
        leave
        ret