#!/bin/bash

# NOTE: I tried setting up bash_completion_lib within ./lib files, but DejaGnu
#       isn't initialized at that point (i.e. output of `expect' is shown on
#       stdout - `open_logs' hasn't run yet?).  And running code from a library
#       file isn't probably a good idea either.
exec "${bashcomp_bash:-$BASH}" \
     "$(dirname "${BASH_SOURCE[0]}")/run" --tool completion $*
