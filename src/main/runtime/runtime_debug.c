void print_heap() {
    printf("       FROM-SPACE       TO-SPACE\n");
    printf("       (%d)      (%d)\n", fromspace, tospace);
    for (int i = 0; i < heap_size; ++i) {
        printf("%4d: %10d %15d %10d %10d\n", i, fromspace[i], tospace[i], fromspace + i, tospace + i);
    }
    printf("\n\n");
}

void print_bitmap(int size, int* bitmap) {
    printf("Bitmap:         ");
    for (int i = 0; i < size; ++i) printf("%d", bitmap[i]);
    printf("\n");
}

void check_pointer(long int* ptr) {
    for (int i = -24; i < 24; ++i) {
        if (i == 0) printf("\n");
        printf("%d: %d\n", ptr + i, ptr[i]);
        if (i == 0) printf("\n");
    }
}