
# root completion
set -l root
complete -c basicExample -n "not __fish_seen_subcommand_from $root" -l timeUnit -f -a 'NANOSECONDS MICROSECONDS MILLISECONDS SECONDS MINUTES HOURS DAYS'  -d ''
complete -c basicExample -n "not __fish_seen_subcommand_from $root" -s u -d ''
complete -c basicExample -n "not __fish_seen_subcommand_from $root" -l timeout -d ''
complete -c basicExample -n "not __fish_seen_subcommand_from $root" -s t -d ''
