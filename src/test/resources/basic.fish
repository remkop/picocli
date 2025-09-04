
# _picocli_basicExample completion
set -l _picocli_basicExample
complete -c basicExample --condition "not __fish_seen_subcommand_from $_picocli_basicExample" --long-option timeUnit --short-option u --no-files --arguments 'NANOSECONDS MICROSECONDS MILLISECONDS SECONDS MINUTES HOURS DAYS'  -d ''
complete -c basicExample --condition "not __fish_seen_subcommand_from $_picocli_basicExample" --long-option timeout --short-option t -d ''
