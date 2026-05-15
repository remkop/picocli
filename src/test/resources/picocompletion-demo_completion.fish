
# _picocli_picocompletion_demo completion
set -l _picocli_picocompletion_demo sub1 sub1-alias sub2 sub2-alias
complete -c picocompletion-demo --condition "not __fish_seen_subcommand_from $_picocli_picocompletion_demo" --long-option version --short-option V -d ''
complete -c picocompletion-demo --condition "not __fish_seen_subcommand_from $_picocli_picocompletion_demo" --long-option help --short-option h -d ''
complete -c picocompletion-demo --no-files --condition "not __fish_seen_subcommand_from $_picocli_picocompletion_demo" --arguments sub1 -d 'First level subcommand 1'
complete -c picocompletion-demo --condition "__fish_seen_subcommand_from sub1" --long-option num -d 'a number'
complete -c picocompletion-demo --condition "__fish_seen_subcommand_from sub1" --long-option str -d 'a String'
complete -c picocompletion-demo --condition "__fish_seen_subcommand_from sub1" --long-option candidates --no-files --arguments 'aaa bbb ccc'  -d 'with candidates'
complete -c picocompletion-demo --no-files --condition "not __fish_seen_subcommand_from $_picocli_picocompletion_demo" --arguments sub1-alias -d 'First level subcommand 1'
complete -c picocompletion-demo --condition "__fish_seen_subcommand_from sub1-alias" --long-option num -d 'a number'
complete -c picocompletion-demo --condition "__fish_seen_subcommand_from sub1-alias" --long-option str -d 'a String'
complete -c picocompletion-demo --condition "__fish_seen_subcommand_from sub1-alias" --long-option candidates --no-files --arguments 'aaa bbb ccc'  -d 'with candidates'
complete -c picocompletion-demo --no-files --condition "not __fish_seen_subcommand_from $_picocli_picocompletion_demo" --arguments sub2 -d 'First level subcommand 2'
complete -c picocompletion-demo --condition "__fish_seen_subcommand_from sub2" --long-option num2 -d 'another number'
complete -c picocompletion-demo --condition "__fish_seen_subcommand_from sub2" --long-option directory --short-option d -d 'a directory'
complete -c picocompletion-demo --no-files --condition "not __fish_seen_subcommand_from $_picocli_picocompletion_demo" --arguments sub2-alias -d 'First level subcommand 2'
complete -c picocompletion-demo --condition "__fish_seen_subcommand_from sub2-alias" --long-option num2 -d 'another number'
complete -c picocompletion-demo --condition "__fish_seen_subcommand_from sub2-alias" --long-option directory --short-option d -d 'a directory'

# _picocli_picocompletion_demo_sub2 completion
set -l _picocli_picocompletion_demo_sub2 subsub1 sub2child1-alias subsub2 sub2child2-alias subsub3 sub2child3-alias
complete -c picocompletion-demo --no-files --condition "not __fish_seen_subcommand_from $_picocli_picocompletion_demo_sub2" --condition '__fish_seen_subcommand_from sub2' --arguments subsub1 -d 'Second level sub-subcommand 1'
complete -c picocompletion-demo --condition "__fish_seen_subcommand_from subsub1" --condition '__fish_seen_subcommand_from sub2' --long-option host --short-option h -d 'a host'
complete -c picocompletion-demo --no-files --condition "not __fish_seen_subcommand_from $_picocli_picocompletion_demo_sub2" --condition '__fish_seen_subcommand_from sub2' --arguments sub2child1-alias -d 'Second level sub-subcommand 1'
complete -c picocompletion-demo --condition "__fish_seen_subcommand_from sub2child1-alias" --condition '__fish_seen_subcommand_from sub2' --long-option host --short-option h -d 'a host'
complete -c picocompletion-demo --no-files --condition "not __fish_seen_subcommand_from $_picocli_picocompletion_demo_sub2" --condition '__fish_seen_subcommand_from sub2' --arguments subsub2 -d 'Second level sub-subcommand 2'
complete -c picocompletion-demo --condition "__fish_seen_subcommand_from subsub2" --condition '__fish_seen_subcommand_from sub2' --long-option timeUnit --short-option u --no-files --arguments 'NANOSECONDS MICROSECONDS MILLISECONDS SECONDS MINUTES HOURS DAYS'  -d ''
complete -c picocompletion-demo --condition "__fish_seen_subcommand_from subsub2" --condition '__fish_seen_subcommand_from sub2' --long-option timeout --short-option t -d ''
complete -c picocompletion-demo --no-files --condition "not __fish_seen_subcommand_from $_picocli_picocompletion_demo_sub2" --condition '__fish_seen_subcommand_from sub2' --arguments sub2child2-alias -d 'Second level sub-subcommand 2'
complete -c picocompletion-demo --condition "__fish_seen_subcommand_from sub2child2-alias" --condition '__fish_seen_subcommand_from sub2' --long-option timeUnit --short-option u --no-files --arguments 'NANOSECONDS MICROSECONDS MILLISECONDS SECONDS MINUTES HOURS DAYS'  -d ''
complete -c picocompletion-demo --condition "__fish_seen_subcommand_from sub2child2-alias" --condition '__fish_seen_subcommand_from sub2' --long-option timeout --short-option t -d ''
complete -c picocompletion-demo --no-files --condition "not __fish_seen_subcommand_from $_picocli_picocompletion_demo_sub2" --condition '__fish_seen_subcommand_from sub2' --arguments subsub3 -d 'Second level sub-subcommand 3'
complete -c picocompletion-demo --no-files --condition "not __fish_seen_subcommand_from $_picocli_picocompletion_demo_sub2" --condition '__fish_seen_subcommand_from sub2' --arguments sub2child3-alias -d 'Second level sub-subcommand 3'

# _picocli_picocompletion_demo_sub2alias completion
set -l _picocli_picocompletion_demo_sub2alias subsub1 sub2child1-alias subsub2 sub2child2-alias subsub3 sub2child3-alias
complete -c picocompletion-demo --no-files --condition "not __fish_seen_subcommand_from $_picocli_picocompletion_demo_sub2alias" --condition '__fish_seen_subcommand_from sub2-alias' --arguments subsub1 -d 'Second level sub-subcommand 1'
complete -c picocompletion-demo --condition "__fish_seen_subcommand_from subsub1" --condition '__fish_seen_subcommand_from sub2-alias' --long-option host --short-option h -d 'a host'
complete -c picocompletion-demo --no-files --condition "not __fish_seen_subcommand_from $_picocli_picocompletion_demo_sub2alias" --condition '__fish_seen_subcommand_from sub2-alias' --arguments sub2child1-alias -d 'Second level sub-subcommand 1'
complete -c picocompletion-demo --condition "__fish_seen_subcommand_from sub2child1-alias" --condition '__fish_seen_subcommand_from sub2-alias' --long-option host --short-option h -d 'a host'
complete -c picocompletion-demo --no-files --condition "not __fish_seen_subcommand_from $_picocli_picocompletion_demo_sub2alias" --condition '__fish_seen_subcommand_from sub2-alias' --arguments subsub2 -d 'Second level sub-subcommand 2'
complete -c picocompletion-demo --condition "__fish_seen_subcommand_from subsub2" --condition '__fish_seen_subcommand_from sub2-alias' --long-option timeUnit --short-option u --no-files --arguments 'NANOSECONDS MICROSECONDS MILLISECONDS SECONDS MINUTES HOURS DAYS'  -d ''
complete -c picocompletion-demo --condition "__fish_seen_subcommand_from subsub2" --condition '__fish_seen_subcommand_from sub2-alias' --long-option timeout --short-option t -d ''
complete -c picocompletion-demo --no-files --condition "not __fish_seen_subcommand_from $_picocli_picocompletion_demo_sub2alias" --condition '__fish_seen_subcommand_from sub2-alias' --arguments sub2child2-alias -d 'Second level sub-subcommand 2'
complete -c picocompletion-demo --condition "__fish_seen_subcommand_from sub2child2-alias" --condition '__fish_seen_subcommand_from sub2-alias' --long-option timeUnit --short-option u --no-files --arguments 'NANOSECONDS MICROSECONDS MILLISECONDS SECONDS MINUTES HOURS DAYS'  -d ''
complete -c picocompletion-demo --condition "__fish_seen_subcommand_from sub2child2-alias" --condition '__fish_seen_subcommand_from sub2-alias' --long-option timeout --short-option t -d ''
complete -c picocompletion-demo --no-files --condition "not __fish_seen_subcommand_from $_picocli_picocompletion_demo_sub2alias" --condition '__fish_seen_subcommand_from sub2-alias' --arguments subsub3 -d 'Second level sub-subcommand 3'
complete -c picocompletion-demo --no-files --condition "not __fish_seen_subcommand_from $_picocli_picocompletion_demo_sub2alias" --condition '__fish_seen_subcommand_from sub2-alias' --arguments sub2child3-alias -d 'Second level sub-subcommand 3'
