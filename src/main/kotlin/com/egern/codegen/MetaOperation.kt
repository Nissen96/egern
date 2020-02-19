package com.egern.codegen

enum class MetaOperation : Arg {
    CalleeSave,
    CallerSave,
    CallerRestore,
    CalleeRestore,
    Print,
    ProgramPrologue,
    CalleePrologue,
    CalleeEpilogue,
    AllocateStackSpace,
    DeallocateStackSpace
}