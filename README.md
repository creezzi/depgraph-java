# depgraph

A generic dependency graph with topological ordering, cycle detection and build layers.

## Usage

```java
DependencyGraph<String> g = new DependencyGraph<>();
g.add("app", "lib", "log");
g.add("lib", "log");
g.order();   // [log, lib, app]
g.layers();  // [[log], [lib], [app]]
```

## Test

```bash
mvn test
```

## License

MIT
