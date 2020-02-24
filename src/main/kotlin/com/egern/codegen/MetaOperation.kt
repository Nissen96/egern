package com.egern.codegen

enum class MetaOperation : Arg {
    CalleeSave,
    CallerSave,
    CallerRestore,
    CalleeRestore,
    Print,
    CalleePrologue,
    CalleeEpilogue,
    AllocateStackSpace,
    DeallocateStackSpace
}