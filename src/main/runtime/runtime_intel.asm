swap:
        push    rbp
        mov     rbp, rsp
        mov     QWORD PTR [rbp-24], rdi
        mov     QWORD PTR [rbp-32], rsi
        mov     rax, QWORD PTR [rbp-24]
        mov     QWORD PTR [rbp-8], rax
        mov     rax, QWORD PTR [rbp-32]
        mov     QWORD PTR [rbp-24], rax
        mov     rax, QWORD PTR [rbp-8]
        mov     QWORD PTR [rbp-32], rax
        nop
        pop     rbp
        ret
allocate_heap:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 48
        mov     DWORD PTR [rbp-20], edi
        mov     QWORD PTR [rbp-32], rsi
        mov     QWORD PTR [rbp-40], rdx
        mov     DWORD PTR [rbp-24], ecx
        mov     eax, DWORD PTR [rbp-24]
        movsx   rdx, eax
        mov     rax, QWORD PTR [rbp-40]
        add     rax, rdx
        mov     QWORD PTR [rbp-8], rax
        mov     rax, QWORD PTR [rbp-32]
        cmp     rax, QWORD PTR [rbp-8]
        jb      .L3
        mov     rdx, QWORD PTR [rbp-8]
        mov     rax, QWORD PTR [rbp-40]
        mov     rsi, rdx
        mov     rdi, rax
        call    swap
.L3:
        mov     eax, DWORD PTR [rbp-20]
        movsx   rdx, eax
        mov     rax, QWORD PTR [rbp-32]
        lea     rcx, [rdx+rax]
        mov     eax, DWORD PTR [rbp-24]
        mov     edx, eax
        shr     edx, 31
        add     eax, edx
        sar     eax
        movsx   rdx, eax
        mov     rax, QWORD PTR [rbp-40]
        add     rax, rdx
        cmp     rcx, rax
        jb      .L4
        mov     eax, 0
        call    collect_garbage
.L4:
        mov     eax, DWORD PTR [rbp-20]
        sal     eax, 3
        add     rbx, rax
        mov     rax, QWORD PTR [rbp-32]
        leave
        ret
collect_garbage:
        push    rbp
        mov     rbp, rsp
        nop
        pop     rbp
        ret
