# Egern
The Egern programming language - written for the bachelor project: 'Constructing a Compiler for an Object-Oriented Language' at the University of Southern Denmark. 

Created by: Alexander Køllund Nissen, Jacob V. M. Nielsen, Frederik Vogt Bolding.

## Dependencies
- Java
- GCC

## Instructions
The project is built with the Gradle build script `gradlew` found in the root directory of the project. The compiler may be run by invoking this script with the `run` argument, i.e. `./gradlew run`. 

A number of useful scripts can additionally be found in the `/src/test/` directory. They are separated into platform specific subdirectories, `/src/test/%PLATFORM%`. Bash scripts are available in `/src/test/linux`. All scripts are run from the `/src/test/` directory, such as running `./linux/run`. Available Bash scripts include: 

- `compile` can be used to compile a custom file and outputs Assembly
- `run` both compiles and runs a custom file
- `test` can be used to run a single test from the `/src/test/success` folder. The result is compared to the corresponding file in `/src/test/expected`.
- `test_all` runs all tests to make sure everything is working! This only runs tests from `/src/test/success`
- `compilep` runs the compiler on a custom file and outputs the source code from the constructed AST. Information from each symbol table is also output
