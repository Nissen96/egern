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
        .string "%4d: %10d %15d\n"
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
        addq    %rdx, %rax
        movq    (%rax), %rcx
        movq    from_space(%rip), %rax
        movl    -4(%rbp), %edx
        movslq  %edx, %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        movq    (%rax), %rdx
        movl    -4(%rbp), %eax
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
.LC6:
        .string "Index %d: %d\n"
check_pointer:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $32, %rsp
        movq    %rdi, -24(%rbp)
        movl    $-16, -4(%rbp)
        jmp     .L14
.L15:
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
.L14:
        cmpl    $15, -4(%rbp)
        jle     .L15
        nop
        nop
        leave
        ret
in_to_space:
        pushq   %rbp
        movq    %rsp, %rbp
        movq    %rdi, -8(%rbp)
        cmpq    $0, -8(%rbp)
        je      .L17
        movq    to_space(%rip), %rax
        cmpq    %rax, -8(%rbp)
        jb      .L17
        movq    to_space(%rip), %rax
        movq    heap_size(%rip), %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        cmpq    %rax, -8(%rbp)
        jnb     .L17
        movl    $1, %eax
        jmp     .L19
.L17:
        movl    $0, %eax
.L19:
        popq    %rbp
        ret
in_from_space:
        pushq   %rbp
        movq    %rsp, %rbp
        movq    %rdi, -8(%rbp)
        cmpq    $0, -8(%rbp)
        je      .L21
        movq    from_space(%rip), %rax
        cmpq    %rax, -8(%rbp)
        jb      .L21
        movq    from_space(%rip), %rax
        movq    heap_size(%rip), %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        cmpq    %rax, -8(%rbp)
        jnb     .L21
        movl    $1, %eax
        jmp     .L23
.L21:
        movl    $0, %eax
.L23:
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
        jmp     .L25
.L29:
        movl    -8(%rbp), %eax
        cltq
        movq    -24(%rbp), %rdx
        subq    %rax, %rdx
        movq    %rdx, %rax
        subq    $1, %rax
        movl    -304(%rbp,%rax,4), %eax
        movl    %eax, -28(%rbp)
        cmpl    $0, -28(%rbp)
        je      .L26
        movl    -8(%rbp), %eax
        cltq
        leaq    0(,%rax,8), %rdx
        movq    -16(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, -40(%rbp)
        movl    -4(%rbp), %eax
        cmpl    -316(%rbp), %eax
        jne     .L27
        movq    -40(%rbp), %rax
        jmp     .L30
.L27:
        addl    $1, -4(%rbp)
.L26:
        addl    $1, -8(%rbp)
.L25:
        movl    -8(%rbp), %eax
        cltq
        cmpq    %rax, -24(%rbp)
        jg      .L29
        movl    $0, %eax
.L30:
        leave
        ret
chase:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $320, %rsp
        movq    %rdi, -312(%rbp)
        jmp     .L32
.L35:
        movl    $0, %eax
        leaq    0(,%rax,8), %rdx
        movq    -312(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movl    %eax, -24(%rbp)
        movl    $1, %eax
        leaq    0(,%rax,8), %rdx
        movq    -312(%rbp), %rax
        addq    %rax, %rdx
        leaq    -304(%rbp), %rax
        movq    %rax, %rsi
        movq    %rdx, %rdi
        call    set_bitmap
        movq    current_to_space_pointer(%rip), %rax
        movq    %rax, -32(%rbp)
        movq    current_to_space_pointer(%rip), %rax
        movl    -24(%rbp), %edx
        movslq  %edx, %rdx
        movl    $2, %ecx
        addq    %rcx, %rdx
        salq    $3, %rdx
        addq    %rdx, %rax
        movq    %rax, current_to_space_pointer(%rip)
        movl    -24(%rbp), %eax
        addl    $2, %eax
        sall    $3, %eax
        movslq  %eax, %rdx
        movq    -312(%rbp), %rcx
        movq    -32(%rbp), %rax
        movq    %rcx, %rsi
        movq    %rax, %rdi
        call    memcpy
        movq    $0, -8(%rbp)
        movq    $0, -16(%rbp)
        movl    $0, -20(%rbp)
.L34:
        movq    -16(%rbp), %rax
        movq    %rax, -40(%rbp)
        movl    -20(%rbp), %edx
        movq    -32(%rbp), %rax
        movl    %edx, %esi
        movq    %rax, %rdi
        call    get_pointer_field
        movq    %rax, -16(%rbp)
        addl    $1, -20(%rbp)
        movq    -40(%rbp), %rax
        movq    %rax, %rdi
        call    in_from_space
        testl   %eax, %eax
        je      .L33
        movl    $1, %eax
        leaq    0(,%rax,8), %rdx
        movq    -40(%rbp), %rax
        addq    %rax, %rdx
        leaq    -304(%rbp), %rax
        movq    %rax, %rsi
        movq    %rdx, %rdi
        call    set_bitmap
        movl    -304(%rbp), %eax
        cmpl    $1, %eax
        jne     .L33
        movq    -40(%rbp), %rax
        addq    $16, %rax
        movq    (%rax), %rax
        movq    %rax, %rdi
        call    in_to_space
        testl   %eax, %eax
        jne     .L33
        movq    -40(%rbp), %rax
        movq    %rax, -8(%rbp)
.L33:
        cmpq    $0, -16(%rbp)
        jne     .L34
        movq    -312(%rbp), %rax
        leaq    16(%rax), %rdx
        movq    -32(%rbp), %rax
        movq    %rax, (%rdx)
        movq    -8(%rbp), %rax
        movq    %rax, -312(%rbp)
.L32:
        cmpq    $0, -312(%rbp)
        jne     .L35
        nop
        nop
        leave
        ret
forward:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $32, %rsp
        movq    %rdi, -24(%rbp)
        movq    -24(%rbp), %rax
        movq    %rax, %rdi
        call    in_to_space
        testl   %eax, %eax
        je      .L37
        movq    -24(%rbp), %rax
        jmp     .L38
.L37:
        movq    -24(%rbp), %rax
        movl    $0, %esi
        movq    %rax, %rdi
        call    get_pointer_field
        movq    %rax, -8(%rbp)
        cmpq    $0, -8(%rbp)
        je      .L39
        movq    -8(%rbp), %rax
        movq    %rax, %rdi
        call    in_to_space
        testl   %eax, %eax
        jne     .L39
        movq    -24(%rbp), %rax
        movq    %rax, %rdi
        call    chase
.L39:
        movq    -8(%rbp), %rax
.L38:
        leave
        ret
.LC7:
        .string "Var %d = %d (pointer)\n"
visit_pointer_vars:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $48, %rsp
        movq    %rdi, -24(%rbp)
        movq    %rsi, -32(%rbp)
        movq    %rdx, -40(%rbp)
        movl    $0, -4(%rbp)
        jmp     .L41
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
        je      .L42
        movl    -4(%rbp), %eax
        negl    %eax
        cltq
        leaq    0(,%rax,8), %rdx
        movq    -40(%rbp), %rax
        addq    %rdx, %rax
        movq    (%rax), %rax
        movq    %rax, -16(%rbp)
        movq    from_space(%rip), %rax
        cmpq    %rax, -16(%rbp)
        jb      .L47
        movq    to_space(%rip), %rax
        cmpq    %rax, -16(%rbp)
        jnb     .L47
        movq    -16(%rbp), %rdx
        movl    -4(%rbp), %eax
        movl    %eax, %esi
        movl    $.LC7, %edi
        movl    $0, %eax
        call    printf
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
.L42:
        addl    $1, -4(%rbp)
.L41:
        movl    -4(%rbp), %eax
        cltq
        cmpq    %rax, -24(%rbp)
        jg      .L46
        jmp     .L40
.L47:
        nop
.L40:
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
.LC8:
        .string "Num parameters: %d\n"
.LC9:
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
        movl    $.LC8, %edi
        movl    $0, %eax
        call    printf
        movq    -72(%rbp), %rax
        movq    %rax, %rsi
        movl    $.LC9, %edi
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
        jmp     .L50
.L51:
        movl    -64(%rbp), %eax
        cltq
        movl    -368(%rbp,%rax,4), %ecx
        movq    -88(%rbp), %rax
        movl    -64(%rbp), %edx
        movslq  %edx, %rdx
        movl    %ecx, (%rax,%rdx,4)
        addl    $1, -64(%rbp)
.L50:
        movl    -64(%rbp), %eax
        cltq
        cmpq    %rax, -56(%rbp)
        jg      .L51
        movl    $0, -60(%rbp)
        jmp     .L52
.L53:
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
.L52:
        movl    -60(%rbp), %eax
        cltq
        cmpq    %rax, -72(%rbp)
        jg      .L53
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
        movl    $10, %edi
        call    putchar
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
.LC10:
        .string "Collecting Garbage:"
collect_garbage:
        pushq   %rbp
        movq    %rsp, %rbp
        subq    $16, %rsp
        movq    %rdi, -8(%rbp)
        movl    $.LC10, %edi
        call    puts
        jmp     .L55
.L56:
        movq    -8(%rbp), %rax
        movq    %rax, %rdi
        call    scan_stack_frame
        movq    -8(%rbp), %rax
        movq    (%rax), %rax
        movq    %rax, -8(%rbp)
.L55:
        cmpq    $0, -8(%rbp)
        jne     .L56
        movq    current_to_space_pointer(%rip), %rax
        movq    %rax, current_heap_pointer(%rip)
        nop
        leave
        ret
.LC11:
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
        jb      .L58
        movq    to_space(%rip), %rdx
        movq    from_space(%rip), %rax
        movq    %rdx, %rsi
        movq    %rax, %rdi
        call    swap
.L58:
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
        jbe     .L59
        movl    $0, %eax
        call    print_heap
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
        jbe     .L59
        movq    stderr(%rip), %rax
        movq    %rax, %rcx
        movl    $14, %edx
        movl    $1, %esi
        movl    $.LC11, %edi
        call    fwrite
        movl    $1, %edi
        call    exit
.L59:
        movq    current_heap_pointer(%rip), %rax
        leave
        ret