@echo off
call ..\..\.\gradlew --quiet -Dorg.gradle.internal.launcher.welcomeMessageEnabled=false run -p ../../ --args="-q" < %1 > ko.nasm
