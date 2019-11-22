#!/usr/bin/env bash

set -ue
. out/prep.sh
################################################################
echo "...getting dependencies from maven"
mvn \
  -f out/artifacts/ALL-SNAPSHOT.pom \
  dependency:copy-dependencies

