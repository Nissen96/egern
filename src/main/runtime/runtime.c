#include <stdio.h>
#include <stdlib.h>

// STACK FRAME - CONSTANT OFFSETS FROM RBP
const long int LOCAL_VAR_OFFSET = 4;
const long int FUNCTION_BITMAP_OFFSET = 3;
const long int NUM_LOCAL_VARS_OFFSET = 2;
const long int NUM_PARAMETERS_OFFSET = 1;
const long int STATIC_LINK_OFFSET = -2;
const long int PARAM_OFFSET = -3;

const long int PARAMS_IN_REGISTERS = 6;

// OBJECT AND ARRAY INFO (admin info common to both)
const long int SIZE_INFO_OFFSET = 0;
const long int BITMAP_OFFSET = 1;
const long int OBJECT_VTABLE_POINTER_OFFSET = 2;
const long int OBJECT_DATA_OFFSET = 3;
const long int ARRAY_DATA_OFFSET = 2;

void printBitmap(size_t const size, void const * const ptr){
    unsigned char *b = (unsigned char*) ptr;
    unsigned char byte;
    int i, j;

    printf("Bitmap:         ");
    for (i = size - 1; i >= 0; i--) {
        for (j = 7; j >= 0; j--) {
            byte = (b[i] >> j) & 1;
            printf("%u", byte);
        }
    }
    puts("");
}

void swap(long int* a, long int* b) {
    long int* temp = a;
    a = b;
    b = temp;
}

void scan_stack_frame(long int* rbp) {
    printf("Num parameters: %d\n", *(rbp - NUM_PARAMETERS_OFFSET));
    printf("Num variables:  %d\n", *(rbp - NUM_LOCAL_VARS_OFFSET));
    printBitmap(sizeof(long int), rbp - FUNCTION_BITMAP_OFFSET);
}

void collect_garbage(long int* rbp) {
    // Visit and scan all stack frames
    printf("Collecting Garbage:\n");
    while (rbp != 0) {
        scan_stack_frame(rbp);
        rbp = (long int*) *rbp;
    }
    printf("Done!\n");
}

long int* allocate_heap(long int size, long int* current_heap_pointer, long int* heap1_pointer, long int heap_size, long int* rbp) {
    // Divide sizes to use in pointer arithmetic
    size /= sizeof(long int);
    heap_size /= sizeof(long int);
    
    long int* heap2_pointer = heap1_pointer + heap_size;
    if (current_heap_pointer >= heap2_pointer) {
        swap(heap1_pointer, heap2_pointer);
    }

    // Run garbage collection if heap limit is reached
    if (current_heap_pointer + size > heap1_pointer + heap_size) {
        collect_garbage(rbp);
    }

    // Out of memory
    if (current_heap_pointer + size > heap1_pointer + heap_size) {
        fprintf(stderr, "Out of memory\n");
        exit(1);
    }
    
    return current_heap_pointer;
}