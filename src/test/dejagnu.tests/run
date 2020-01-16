#!/bin/bash


# Print some helpful messages.
usage() {
    echo "Run bash-completion tests"
    echo
    echo "The 'tool' is determined automatically from filenames."
    echo "Unrecognized options are passed through to dejagnu by default."
    echo
    echo "Interesting options:"
    echo "  --log-dir=      Directory where to write the <tool>.log and <tool>.sum log files to."
    echo "  --tool=         Name for the set of dejagnu tests. Only 'completion' is supported."
    echo "  --tool_exec=    Test against a different bash executable."
    echo "  --buffer-size   Change expect match buffer size from our default of 20000 bytes."
    echo "  --debug         Create a dbg.log in the test directory with detailed expect match information."
    echo "  --timeout       Change expect timeout from the default of 10 seconds."
    echo "  --debug-xtrace  Create an xtrace.log in the test directory with set -x output."
    echo
    echo "Example run: ./run unit/_get_cword.exp unit/compgen.exp"
}


# Try to set the tool variable; or fail if trying to set different values.
set_tool() {
    if [[ $tool ]]; then
        if [[ $tool != $1 ]]; then
            echo "Tool spec mismatch ('$tool' and '$1'). See --usage."
            exit 1
        fi
    else
        tool=$1
    fi
}


cd "$(dirname "${BASH_SOURCE[0]}")"

log_dir="log"

# Loop over the arguments.
args=()
while [[ $# > 0 ]]; do
    case "$1" in
        --help|--usage) usage; exit 1;;
        --buffer-size) shift; buffer_size=$1;;
        --buffer-size=*) buffer_size=${1/--buffer-size=};;
        --log-dir) shift; log_dir=$1;;
        --log-dir=*) log_dir=${1/--log-dir=};;
        --debug-xtrace) args+=(OPT_BASH_XTRACE=1);;
        --timeout) shift; timeout=$1;;
        --timeout=*) timeout=${1/--timeout=};;
        --tool=*) set_tool "${1#/--tool=}";;
        --tool) shift; set_tool "$1";;
        completion/*.exp|*/completion/*.exp|unit/*.exp|*/unit/*.exp)
            arg=${1%/*}
            set_tool "${arg##*/}"
            args+=("${1##*/}")
            ;;
        *) args+=("$1")
    esac
    shift
done

[[ -n $buffer_size ]] && args+=("OPT_BUFFER_SIZE=$buffer_size")
[[ -n $timeout ]] && args+=("OPT_TIMEOUT=$timeout")
[[ -z $tool ]] && { echo "Must specify tool somehow"; exit 1; }

mkdir -p "$log_dir"
runtest --outdir "$log_dir" --tool "$tool" "${args[@]}"
rc=$?
[[ $rc -ne 0 && -n "$CI" ]] && cat "$log_dir/$tool.log"
exit $rc
