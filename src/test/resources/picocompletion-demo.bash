#!/usr/bin/env bash

# This is a sample command script.
# Any script that invokes your application can function as the command script.
# Pass the name of this command script when generating the autocompletion function.
#
# For example:
# $ java -jar picocli-1.0.0.jar -n picocompletion-demo.bash 'picocli.AutoCompleteTest$TopLevel'

LIBS=`cygpath -w "/cygdrive/c/Users/remko/IdeaProjects/pico-cli/build/libs"`
VERSION=1.0.0-SNAPSHOT
CP="${LIBS}/picocli-${VERSION}.jar;${LIBS}/picocli-${VERSION}-tests.jar"
java -cp "${CP}" 'picocli.AutoCompleteTest$TopLevel' $@

