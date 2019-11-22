#!/usr/bin/env bash
set -ue
. out/prep.sh
################################################################
echo "...get our tools"
rm -rf tools
git clone 'https://github.com/ModelingValueGroup/buildTools.git'
( echo ". tools/tools.sh"
  cat out/prep.sh
) >> out/prep.sh-tmp
cp out/prep.sh-tmp out/prep.sh
