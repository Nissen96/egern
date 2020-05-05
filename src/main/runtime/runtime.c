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

long int* heap_pointer;
long int* from_space;
long int* to_space;
long int* current_heap_pointer;


void set_bitmap(long int* ptr, int* bitmap) {
    unsigned char* b = (unsigned char*) ptr;
    unsigned char byte;

    int i, j;
    for (i = sizeof(long int) - 1; i >= 0; i--) {
        for (j = 7; j >= 0; j--) {
            byte = (b[i] >> j) & 1;
            bitmap[8 * i + j] = byte;
        }
    }
}

void print_bitmap(int size, int* bitmap) {
    printf("Bitmap:         ");
    for (int i = 0; i < size; ++i) printf("%d", bitmap[i]);
    printf("\n");
}

void swap(long int* a, long int* b) {
    long int* temp = a;
    a = b;
    b = temp;
}

void forward(long int* ptr) {

}

void visit_pointer_vars(long int num_vars, int* bitmap, long int* variables) {
    for (int i = 0; i < num_vars; ++i) {
        int is_pointer = bitmap[i];

        if (is_pointer) {
            long int* variable = (long int*) variables[-num_vars + i + 1];
            printf("Var %d = %d is pointer\n", i, variable);
            forward(variable);
        }
    }
}

void visit_pointer_params(char* bitmap, long int* params) {

}

void scan_stack_frame(long int* rbp) {
    // Get stack frame info
    long int num_params = *(rbp - NUM_PARAMETERS_OFFSET);
    long int num_vars = *(rbp - NUM_LOCAL_VARS_OFFSET);
    int bitmap[64];
    set_bitmap(rbp - FUNCTION_BITMAP_OFFSET, bitmap);

    printf("Num parameters: %d\n", num_params);
    printf("Num variables:  %d\n", num_vars);
    print_bitmap(num_params + num_vars, bitmap);

    int param_bitmap[num_params];
    int var_bitmap[num_vars];
    int i, j;
    for (i = 0; i < num_params; i++) param_bitmap[i] = bitmap[i];
    for (j = 0; j < num_vars; j++) var_bitmap[j] = bitmap[i++];
    visit_pointer_vars(num_vars, var_bitmap, rbp - LOCAL_VAR_OFFSET);
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

long int* allocate_heap(long int size, long int heap_size, long int* rbp) {
    from_space = heap_pointer;
    to_space = from_space + heap_size;

    if (current_heap_pointer >= to_space) {
        swap(from_space, to_space);
    }

    /*printf("Heap pointer: %d\n", from_space);
    printf("Heap size:    %d\n", heap_size);
    printf("Current:      %d\n", current_heap_pointer);
    printf("Allocation:   %d\n", size);

    printf("New current:  %d\n", current_heap_pointer + size);
    printf("Heap 1 end:   %d\n\n", from_space + heap_size);*/

    // Run garbage collection if heap limit is reached
    if (current_heap_pointer + size > from_space + heap_size) {
        collect_garbage(rbp);
    }

    // Out of memory
    if (current_heap_pointer + size > from_space + heap_size) {
        fprintf(stderr, "Out of memory\n");
        exit(1);
    }

    return heap_pointer;
}