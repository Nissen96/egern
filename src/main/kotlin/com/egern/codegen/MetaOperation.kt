package com.egern.codegen

enum class MetaOperation : Arg {
    CalleeSave,
    CallerSave,
    CallerRestore,
    CalleeRestore,
    Print,
    MainCalleeSave,
    MainCalleeRestore,
    ProgramPrologue,
    CalleePrologue,
    CalleeEpilogue,
    AllocateStackSpace,
    DeallocateStackSpace
}