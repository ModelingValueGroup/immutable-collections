#!/usr/bin/env bash
set -ue
. out/prep.sh
################################################################
echo "...make the poms with the dependencies"
makeAllPoms \
    "http://www.dclare-lang.org" \
    "https://github.com/ModelingValueGroup/immutable-collections.git" \
    "$OUR_VERSION" \
    "${units[@]}"
