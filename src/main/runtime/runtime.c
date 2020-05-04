void swap(void* a, void* b) {
    void* temp = a;
    a = b;
    b = temp;
}

void* allocate_heap(int size, void* current_heap_pointer, void* heap1_pointer, int heap_size) {
    void* heap2_pointer = heap1_pointer + heap_size;
    if (current_heap_pointer >= heap2_pointer) {
        swap(heap1_pointer, heap2_pointer);
    }
    void* a = current_heap_pointer + size;
    if (current_heap_pointer + size >= heap1_pointer + heap_size / 2) {
        collect_garbage();
    }

    asm("addq    %%rax, %%rbx" : : "a" (size * 8));
    return current_heap_pointer;
}

void collect_garbage() {
    
}
