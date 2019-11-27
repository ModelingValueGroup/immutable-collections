#!/usr/bin/env bash
set -ue
. out/prep.sh
################################################################
echo "...get our buildTools"



rm -rf tools
git clone 'https://github.com/ModelingValueGroup/buildTools.git'
( echo ". tools/tools.sh"
  cat out/prep.sh
) >> out/prep.sh-tmp
cp out/prep.sh-tmp out/prep.sh
