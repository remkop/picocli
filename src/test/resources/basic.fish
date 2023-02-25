
# root completion
set -l root
complete -c basicExample --condition "not __fish_seen_subcommand_from $root" --long-option timeUnit --short-option u --no-files --arguments 'NANOSECONDS MICROSECONDS MILLISECONDS SECONDS MINUTES HOURS DAYS'  -d ''
complete -c basicExample --condition "not __fish_seen_subcommand_from $root" --long-option timeout --short-option t -d ''
