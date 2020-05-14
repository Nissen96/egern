#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// STACK FRAME - CONSTANT OFFSETS FROM RBP
const long int LOCAL_VAR_OFFSET = 4;
const long int FUNCTION_BITMAP_OFFSET = 3;
const long int NUM_LOCAL_VARS_OFFSET = 2;
const long int NUM_PARAMETERS_OFFSET = 1;
const long int PARAM_OFFSET = -3;

// OBJECT AND ARRAY INFO (admin info common to both)
const long int SIZE_INFO_OFFSET = 0;
const long int BITMAP_OFFSET = 1;
const long int DATA_OFFSET = 2;

const long int PARAMS_IN_REGISTERS = 6;
typedef enum { RDI, RSI, RDX, RCX, R8, R9 } PARAM_REGISTERS;

long int* heap_pointer;
long int* from_space;
long int* to_space;
long int heap_size;
long int* current_heap_pointer;
long int* current_to_space_pointer;
long int* scan;

void print_heap() {
    printf("       FROM-SPACE       TO-SPACE\n");
    printf("       (%d)      (%d)\n", from_space, to_space);
    for (int i = 0; i < heap_size; ++i) {
        printf("%4d: %10d %15d %10d %10d\n", i, from_space[i], to_space[i], from_space + i, to_space + i);
    }
    printf("\n\n");
}

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

void swap_spaces() {
    long int* temp = to_space;
    to_space = from_space;
    from_space = temp;
}

int min(int a, int b) {
    if (a <= b) return a;
    return b;
}

int max(int a, int b) {
    if (a >= b) return a;
    return b;
}

void check_pointer(long int* ptr) {
    for (int i = -24; i < 24; ++i) {
        if (i == 0) printf("\n");
        printf("%d: %d\n", ptr + i, ptr[i]);
        if (i == 0) printf("\n");
    }
}

int in_to_space(long int* ptr) {
    return ptr != NULL && ptr >= to_space && ptr < to_space + heap_size;
}

int in_from_space(long int* ptr) {
    return ptr != NULL && ptr >= from_space && ptr < from_space + heap_size;
}

long int* forward(long int* ptr) {
    // ptr is already in to-space
    if (!in_from_space(ptr)) {
        return ptr;
    }

    int bitmap[64];
    int num_fields = ptr[SIZE_INFO_OFFSET];
    set_bitmap(ptr + BITMAP_OFFSET, bitmap);

    // Check if p points to an already copied record
    // This is marked by setting the first field (size info) to -1
    long int is_forwarded = ptr[SIZE_INFO_OFFSET] == -1;
    if (is_forwarded) {
        // The forwarded pointer overrides the bitmap
        return (long int*) ptr[BITMAP_OFFSET];
    }

    // ptr points to not yet copied record - copy and mark
    memcpy(current_to_space_pointer, ptr, (num_fields + DATA_OFFSET) * 8);
    ptr[SIZE_INFO_OFFSET] = -1;
    ptr[BITMAP_OFFSET] = (long int) current_to_space_pointer;
    current_to_space_pointer += DATA_OFFSET + num_fields;
    return (long int*) ptr[BITMAP_OFFSET];
}

void forward_heap_fields() {
    int bitmap[64];
    while (scan < current_to_space_pointer) {
        set_bitmap(scan + BITMAP_OFFSET, bitmap);
        int num_vars = *(scan + SIZE_INFO_OFFSET);

        for (int i = 0; i < num_vars; ++i) {
            // Only forward pointer fields
            int is_pointer = bitmap[i];
            if (is_pointer) {
                scan[DATA_OFFSET + i] = (long int) forward((long int*) scan[DATA_OFFSET + i]);
            }
        }
        scan += DATA_OFFSET + num_vars;
    }
    print_heap();
}

void visit_params(long int num_params, int* bitmap, long int* params) {
    // Forward parameters on stack
    for (int i = 0; i < num_params; ++i) {
        printf("Param %d: %d %d\n", i, params[i], bitmap[num_params - i - 1]);
        int is_pointer = bitmap[num_params - i - 1];
        if (is_pointer) {
            params[i] = (long int) forward((long int*) params[i]);
        }
    }

    forward_heap_fields();
}

void visit_variables(long int num_vars, int* bitmap, long int* variables) {
    // Forward local variable pointers
    for (int i = 0; i < num_vars; ++i) {
        printf("Var %d: %d %d\n", i, variables[-i], bitmap[num_vars - i - 1]);
        int is_pointer = bitmap[num_vars - i - 1];
        if (is_pointer) {
            long int* variable = (long int*) variables[-i];
            if (!in_from_space(variable)) {
                break;  // reached uninitialized variable
            }

            // Forward variable to to-space and store forward address
            variables[-i] = (long int) forward(variable);
        }
    }

    forward_heap_fields();
}

void scan_stack_frame(long int* rbp, long int* rsp, int is_top_frame) {
    // Get stack frame info
    long int num_params = rbp[-NUM_PARAMETERS_OFFSET];
    long int num_vars = rbp[-NUM_LOCAL_VARS_OFFSET];
    int bitmap[64];
    set_bitmap(rbp - FUNCTION_BITMAP_OFFSET, bitmap);

    int register_params = min(num_params, PARAMS_IN_REGISTERS);
    int stack_params = max(num_params - PARAMS_IN_REGISTERS, 0);
    int register_param_bitmap[register_params];
    int stack_param_bitmap[stack_params];
    int var_bitmap[num_vars];

    int i, j;
    for (i = 0; i < stack_params; i++) stack_param_bitmap[i] = bitmap[i];
    for (j = 0; j < register_params; j++) register_param_bitmap[register_params - j - 1] = bitmap[i++];
    for (j = 0; j < num_vars; j++) var_bitmap[j] = bitmap[i++];

    print_bitmap(num_params + num_vars, bitmap);
    print_bitmap(register_params, register_param_bitmap);
    print_bitmap(stack_params, stack_param_bitmap);
    print_bitmap(num_vars, var_bitmap);

    check_pointer(rbp);
    visit_variables(num_vars, var_bitmap, rbp - LOCAL_VAR_OFFSET);

    // Handle params in registers
    if (is_top_frame) {
        check_pointer(rsp);
        visit_params(register_params, register_param_bitmap, rsp + 2);
    // Register params are caller-saved
    } else {
        //visit_params(register_params, register_param_bitmap, rbp - );
    }

    // Handle remaining params on stack
    if (num_params >= PARAMS_IN_REGISTERS) {
        visit_params(stack_params, stack_param_bitmap, rbp - PARAM_OFFSET);
    }
}

void collect_garbage(long int* rbp, long int* rsp) {
    // Visit and scan all stack frames
    printf("Collecting Garbage:\n");
    scan = to_space;
    int is_top_frame = 1;
    while (rbp != 0) {
        scan_stack_frame(rbp, rsp, is_top_frame);
        rbp = (long int*) *rbp;
        is_top_frame = 0;
    }
    print_heap();
    memset(from_space, 0, heap_size * 8);
    current_heap_pointer = current_to_space_pointer;
}

long int* allocate_heap(long int size, long int* rbp, long int* rsp) {
    from_space = heap_pointer;
    to_space = from_space + heap_size;

    if (current_heap_pointer >= to_space) {
        swap_spaces();
    }
    current_to_space_pointer = to_space;

    // Run garbage collection if heap limit is reached
    if (current_heap_pointer + size > from_space + heap_size) {
        print_heap();
        collect_garbage(rbp, rsp);
        print_heap();

        // Out of memory
        if (current_heap_pointer + size > to_space + heap_size) {
            fprintf(stderr, "Out of memory\n");
            exit(1);
        }
    }

    return current_heap_pointer;
}