#!/usr/bin/env bash
set -ue
. build/tmp/prep.sh
################################################################
echo "...build struct generaot"
ant -f immutable-collections.xml compile.module.build
 exit
echo "...run struct generator"
ant -f immutable-collections.xml

echo "...build everything"
ant -f immutable-collections.xml
