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


void print_heap() {
    printf("       FROM-SPACE       TO-SPACE\n");
    printf("       (%d)      (%d)\n", from_space, to_space);
    for (int i = 0; i < heap_size; ++i) {
        printf("%4d: %10d %15d\n", i, from_space[i], to_space[i]);
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

void swap(long int* a, long int* b) {
    long int* temp = a;
    a = b;
    b = temp;
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

void chase(long int* ptr) {
    int bitmap[64];

    // Handle entire struct at pointer depth-first
    while (ptr != NULL) {
        int num_fields = *(ptr + SIZE_INFO_OFFSET);
        set_bitmap(ptr + BITMAP_OFFSET, bitmap);

        // Copy everything at ptr to to-space
        long int* to_ptr = current_to_space_pointer;
        current_to_space_pointer += DATA_OFFSET + num_fields;
        memcpy(to_ptr, ptr, (num_fields + 2) * 8);

        // Find last field still pointing to from-space
        long int* next_from_space_field = NULL;
        long int* next_field = NULL;
        int field_num = 0;
        do {
            long int* current_field = next_field;
            next_field = get_pointer_field(to_ptr, field_num);
            ++field_num;

            if (in_from_space(current_field)) {
                set_bitmap(current_field + BITMAP_OFFSET, bitmap);
                if (bitmap[0] == 1 && !in_to_space((long int*) current_field[2])) {
                    next_from_space_field = current_field;
                }
            }
        } while (next_field != NULL);

        ptr[2] = (long int) to_ptr;
        ptr = next_from_space_field;
    }
}

long int* forward(long int* ptr) {
    // Nothing to do if already in to-space
    if (in_to_space(ptr)) {
        return ptr;
    }

    // End condition: first pointer field is already in to-space (fields handled in reverse)
    long int* field = get_pointer_field(ptr, 0);
    if (field != NULL && !in_to_space(field)) {
        chase(ptr);
    }
    return field;
}

void visit_pointer_vars(long int num_vars, int* bitmap, long int* variables) {
    for (int i = 0; i < num_vars; ++i) {
        int is_pointer = bitmap[num_vars - i - 1];
        if (is_pointer) {
            long int* variable = (long int*) variables[-i];
            if (variable < from_space || variable >= to_space) {
                return;  // reached uninitialized variable
            }
            printf("Var %d = %d (pointer)\n", i, variable);

            // Forward variable to to-space and store forward address
            variables[-i] = (long int) forward(variable);
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

    //check_pointer(rbp - LOCAL_VAR_OFFSET);
    visit_pointer_vars(num_vars, var_bitmap, rbp - LOCAL_VAR_OFFSET);

    printf("\n");
}

void collect_garbage(long int* rbp) {
    // Visit and scan all stack frames
    printf("Collecting Garbage:\n");
    while (rbp != 0) {
        scan_stack_frame(rbp);
        rbp = (long int*) *rbp;
    }
    current_heap_pointer = current_to_space_pointer;
}

long int* allocate_heap(long int size, long int* rbp) {
    from_space = heap_pointer;
    to_space = from_space + heap_size;

    if (current_heap_pointer >= to_space) {
        swap(from_space, to_space);
    }
    current_to_space_pointer = to_space;

    // Run garbage collection if heap limit is reached
    if (current_heap_pointer + size > from_space + heap_size) {
        print_heap();
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