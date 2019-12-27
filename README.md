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
To get all the dependencies in you .m2 repos use the following commands:
````bash
mvn dependency:resolve
mvn dependency:resolve -Dclassifier=javadoc
mvn dependency:resolve -Dclassifier=sources
````