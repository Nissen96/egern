# Egern
The Egern programming language - written for the bachelor project: 'Constructing a Compiler for an Object-Oriented Language'

Created by: Alexander Køllund Nissen, Jacob V. M. Nielsen, Frederik Vogt Bolding.

## Dependencies
- Java
- GCC

## Instructions
Bash scripts for a given platform can be found in `/src/test/%PLATFORM%` - i.e. `/src/test/linux`.

- `test` can be used to run a single test from the `/src/test/success` folder and compares to results from `/src/test/expected`.
- `test_all` runs all tests to make sure everything is working!
- `compile` can be used to compile a custom file and outputs Assembly
- `run` both compiles and runs a custom file
