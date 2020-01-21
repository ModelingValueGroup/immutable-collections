# immutable-collections
a heavily optimised immutable collections framework for java

## focus points
- intrinsically immutable semantics (unlike java native Unmodifiable Collections)
- low memory footprint through optimised sharing of sub-structures
- thread safe by design without synchronisation
- fast updating and comparing
- optimised compare and merge (n-way)
- Set, Map, List, QualifiedSet.

## maven dependencies
To get all the dependencies in your ```lib``` folder: use the following commands:
````bash
mvn dependency:copy-dependencies -Dmdep.stripVersion=true -DoutputDirectory=lib
mvn dependency:copy-dependencies -Dmdep.stripVersion=true -DoutputDirectory=lib -Dclassifier=javadoc
mvn dependency:copy-dependencies -Dmdep.stripVersion=true -DoutputDirectory=lib -Dclassifier=sources
````
