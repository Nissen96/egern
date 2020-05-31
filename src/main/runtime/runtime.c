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

long int* heap_pointer;
long int* fromspace;
long int* tospace;
long int heap_size;
long int* current_heap_pointer;
long int* current_tospace_pointer;
long int* scan;
long int* vtable_pointer;

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

void swap_spaces() {
    long int* temp = tospace;
    tospace = fromspace;
    fromspace = temp;
}

int min(int a, int b) {
    if (a <= b) return a;
    return b;
}

int max(int a, int b) {
    if (a >= b) return a;
    return b;
}

int in_to_space(long int* ptr) {
    return ptr != NULL && ptr >= tospace && ptr < tospace + heap_size;
}

int in_from_space(long int* ptr) {
    return ptr != NULL && ptr >= fromspace && ptr < fromspace + heap_size;
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
    memcpy(current_tospace_pointer, ptr, (num_fields + DATA_OFFSET) * 8);
    ptr[SIZE_INFO_OFFSET] = -1;
    ptr[BITMAP_OFFSET] = (long int) current_tospace_pointer;
    current_tospace_pointer += DATA_OFFSET + num_fields;
    return (long int*) ptr[BITMAP_OFFSET];
}

void forward_heap_fields() {
    int bitmap[64];

    // Forward everything pointed to by already forwarded blocks in heap
    while (scan < current_tospace_pointer) {
        set_bitmap(scan + BITMAP_OFFSET, bitmap);
        int num_vars = *(scan + SIZE_INFO_OFFSET);

        for (int i = 0; i < num_vars; ++i) {
            // Only forward current object's pointer fields
            int is_pointer = bitmap[i];
            if (is_pointer) {
                scan[DATA_OFFSET + i] = (long int) forward((long int*) scan[DATA_OFFSET + i]);
            }
        }
        scan += DATA_OFFSET + num_vars;
    }
}

void visit_params(long int num_params, int* bitmap, long int* params) {
    // Forward parameters on stack
    for (int i = 0; i < num_params; ++i) {
        int is_pointer = bitmap[num_params - i - 1];
        if (is_pointer) {
            params[-i] = (long int) forward((long int*) params[-i]);
        }
    }

    forward_heap_fields();
}

void visit_caller_saved_params(long int* rbp) {
    // Handle register saved parameters from enclosing scope - caller-saved and more easily accessible from current scope
    long int* prev_rbp = (long int*) *rbp;
    if (prev_rbp == 0) {
        return;
    }

    // Get bitmap from enclosing scope
    int prev_num_params = prev_rbp[-NUM_PARAMETERS_OFFSET];
    int bitmap[64];
    set_bitmap(prev_rbp - FUNCTION_BITMAP_OFFSET, bitmap);
    int prev_num_stack_params = max(prev_num_params - PARAMS_IN_REGISTERS, 0);
    int prev_num_register_params = min(prev_num_params, PARAMS_IN_REGISTERS);
    int register_param_bitmap[prev_num_register_params];
    for (int i = 0; i < prev_num_register_params; i++) register_param_bitmap[i] = bitmap[prev_num_stack_params + i];
    
    // Caller-saved params lie before all parameters stored in the current scope, plus the two first caller-saved registers
    int num_params = rbp[-NUM_PARAMETERS_OFFSET];
    int num_stack_params = max(num_params - PARAMS_IN_REGISTERS, 0);
    int param_offset = PARAM_OFFSET - num_stack_params - num_params - 8 + 1;
    visit_params(prev_num_register_params, register_param_bitmap, rbp - param_offset);
}

void visit_variables(long int num_vars, int* bitmap, long int* variables) {
    // Forward local variable pointers
    for (int i = 0; i < num_vars; ++i) {
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

    // Extract bitmaps for stack-allocated params, register params, and variables from the combined bitmap
    int i, j;
    int num_stack_params = max(num_params - PARAMS_IN_REGISTERS, 0);
    int stack_param_bitmap[num_stack_params];
    for (i = 0; i < num_stack_params; i++) stack_param_bitmap[i] = bitmap[i];

    int num_register_params = min(num_params, PARAMS_IN_REGISTERS);
    int register_param_bitmap[num_register_params];
    for (j = 0; j < num_register_params; j++) register_param_bitmap[num_register_params - j - 1] = bitmap[i++];

    int var_bitmap[num_vars];
    for (j = 0; j < num_vars; j++) var_bitmap[j] = bitmap[i++];

    visit_variables(num_vars, var_bitmap, rbp - LOCAL_VAR_OFFSET);

    // Handle params in registers for the top frame (caller saved just before allocation)
    if (is_top_frame) {
        visit_params(num_register_params, register_param_bitmap, rsp + 8);
    }

    // Handle register saved parameters from enclosing scope
    visit_caller_saved_params(rbp);

    // Handle remaining params on stack
    if (num_params >= PARAMS_IN_REGISTERS) {
        visit_params(num_stack_params, stack_param_bitmap, rbp - PARAM_OFFSET);
    }
}

void collect_garbage(long int* rbp, long int* rsp) {
    // Iterate over every stack frame, starting from the current base pointer
    scan = tospace;
    int is_top_frame = 1;
    while (rbp != 0) {
        scan_stack_frame(rbp, rsp, is_top_frame);
        rbp = (long int*) *rbp;
        is_top_frame = 0;
    }

    // Wipe fromspace
    memset(fromspace, 0, heap_size * 8);
    current_heap_pointer = current_tospace_pointer;
}

long int* allocate_heap(long int size, long int* rbp, long int* rsp) {
    // Set fromspace to the currently used heap and tospace the other
    fromspace = heap_pointer;
    tospace = fromspace + heap_size;
    if (current_heap_pointer > tospace) {
        swap_spaces();
    }
    current_tospace_pointer = tospace;

    // Run garbage collection if heap limit is reached
    if (current_heap_pointer + size > fromspace + heap_size) {
        collect_garbage(rbp, rsp);

        // Out of memory
        if (current_heap_pointer + size > tospace + heap_size) {
            fprintf(stderr, "Out of memory\n");
            exit(1);
        }
    }

    // Allocation successful - offset heap pointer by allocated bytes and return start address of allocated block
    long int *heap_return_pointer = current_heap_pointer;
    current_heap_pointer += size;
    return heap_return_pointer;
}