# Bash library for bash-completion DejaGnu testsuite


# @param $1  Char to add to $COMP_WORDBREAKS
add_comp_wordbreak_char() {
    [[ "${COMP_WORDBREAKS//[^$1]}" ]] || COMP_WORDBREAKS+=$1
} # add_comp_wordbreak_char()


# Diff environment files to detect if environment is unmodified
# @param $1  File 1
# @param $2  File 2
# @param $3  Additional sed script
diff_env() {
    diff "$1" "$2" | sed -e "
# Remove diff line indicators
        /^[0-9,]\{1,\}[acd]/d
# Remove diff block separators
        /---/d
# Remove underscore variable
        /[<>] _=/d
# Remove PPID bash variable
        /[<>] PPID=/d
# Remove BASH_REMATCH bash variable
        /[<>] BASH_REMATCH=/d
# Remove functions starting with underscore
        /[<>] declare -f _/d
        $3"
} # diff_env()


# Output array elements, sorted and separated by newline
# Unset variable after outputting.
# @param $1  Name of array variable to process
echo_array() {
    local name=$1[@]
    printf "%s\n" "${!name}" | sort
} # echo_array()
