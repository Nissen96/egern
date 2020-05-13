#include <stdio.h>
#include <stdlib.h>
#include <string.h>

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
const long int DATA_OFFSET = 2;

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

void check_pointer(long int* ptr) {
    for (int i = -16; i < 16; ++i) {
        printf("Index %d: %d\n", i, ptr[i]);
    }
}

int in_to_space(long int* ptr) {
    return ptr != NULL && ptr >= to_space && ptr < to_space + heap_size;
}

int in_from_space(long int* ptr) {
    return ptr != NULL && ptr >= from_space && ptr < from_space + heap_size;
}

long int* get_pointer_field(long int* ptr, int k) {
    // Get bitmap
    long int* fields = ptr + DATA_OFFSET;
    long int num_fields = *(ptr + SIZE_INFO_OFFSET);
    int bitmap[64];
    set_bitmap(ptr + BITMAP_OFFSET, bitmap);

    // Find k'th pointer field
    int pointers_found = 0;
    for (int i = 0; i < num_fields; ++i) {
        int is_pointer = bitmap[num_fields - i - 1];
        if (is_pointer) {
            long int* field = (long int*) fields[i];
            if (pointers_found == k) return field;

            pointers_found++;
        }
    }
    return NULL;
}

long int* forward(long int* ptr) {
    if (in_to_space(ptr)) {
        return ptr;
    }

    int bitmap[64];
    int num_fields = *(ptr + SIZE_INFO_OFFSET);
    set_bitmap(ptr + BITMAP_OFFSET, bitmap);

    int long* field = get_pointer_field(ptr, 0);
    if (in_to_space(field)) {
        return field;
    }

    memcpy(current_to_space_pointer, ptr, (num_fields + DATA_OFFSET) * 8);
    ptr[DATA_OFFSET] = (long int) current_to_space_pointer;
    current_to_space_pointer += DATA_OFFSET + num_fields;
    return (long int*) ptr[DATA_OFFSET];
}

void visit_pointer_vars(long int num_vars, int* bitmap, long int* variables) {
    for (int i = 0; i < num_vars; ++i) {
        int is_pointer = bitmap[num_vars - i - 1];
        if (is_pointer) {
            long int* variable = (long int*) variables[-i];
            if (!in_from_space(variable)) {
                return;  // reached uninitialized variable
            }

            // Forward variable to to-space and store forward address
            variables[-i] = (long int) forward(variable);
        }
    }

    int child_bitmap[64];
    while (scan < current_to_space_pointer) {
        set_bitmap(scan + BITMAP_OFFSET, child_bitmap);
        num_vars = *(scan + SIZE_INFO_OFFSET);

        for (int i = 0; i < num_vars; ++i) {
            int is_pointer = child_bitmap[i];
            if (is_pointer) {
                scan[DATA_OFFSET + i] = (long int) forward((long int*) scan[DATA_OFFSET + i]);
            }
        }
        scan += DATA_OFFSET + num_vars;
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

    int param_bitmap[num_params];
    int var_bitmap[num_vars];
    int i, j;
    for (i = 0; i < num_params; i++) param_bitmap[i] = bitmap[i];
    for (j = 0; j < num_vars; j++) var_bitmap[j] = bitmap[i++];

    //check_pointer(rbp - LOCAL_VAR_OFFSET);
    visit_pointer_vars(num_vars, var_bitmap, rbp - LOCAL_VAR_OFFSET);
}

void collect_garbage(long int* rbp) {
    // Visit and scan all stack frames
    printf("Collecting Garbage:\n");
    scan = to_space;
    while (rbp != 0) {
        printf("RBP: %d\n", rbp);
        scan_stack_frame(rbp);
        rbp = (long int*) *rbp;
    }
    memset(from_space, 0, heap_size * 8);
    current_heap_pointer = current_to_space_pointer;
}

long int* allocate_heap(long int size, long int* rbp) {
    from_space = heap_pointer;
    to_space = from_space + heap_size;

    if (current_heap_pointer >= to_space) {
        swap_spaces();
    }
    current_to_space_pointer = to_space;

    print_heap();

    // Run garbage collection if heap limit is reached
    if (current_heap_pointer + size > from_space + heap_size) {
        collect_garbage(rbp);
        print_heap();

        // Out of memory
        if (current_heap_pointer + size > to_space + heap_size) {
            fprintf(stderr, "Out of memory\n");
            exit(1);
        }
    }

    return current_heap_pointer;
}