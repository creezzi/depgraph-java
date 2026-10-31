package io.github.creezzi.depgraph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DependencyGraphTest {
    @Test
    void ordersDependenciesFirst() {
        DependencyGraph<String> g = new DependencyGraph<>();
        g.add("app", "lib", "log");
        g.add("lib", "log");
        assertEquals(List.of("log", "lib", "app"), g.order());
        assertEquals(List.of(List.of("log"), List.of("lib"), List.of("app")), g.layers());
    }

    @Test
    void independentNodesShareALayer() {
        DependencyGraph<String> g = new DependencyGraph<>();
        g.add("c", "a", "b");
        assertEquals(List.of(List.of("a", "b"), List.of("c")), g.layers());
    }

    @Test
    void detectsCycles() {
        DependencyGraph<String> g = new DependencyGraph<>();
        g.add("a", "b");
        g.add("b", "c");
        g.add("c", "a");
        assertEquals(List.of("a", "b", "c", "a"), g.findCycle().orElseThrow());
        IllegalStateException e = assertThrows(IllegalStateException.class, g::order);
        assertTrue(e.getMessage().contains("[a, b, c, a]"));
    }

    @Test
    void selfDependencyIsACycle() {
        DependencyGraph<String> g = new DependencyGraph<>();
        g.add("a", "a");
        assertEquals(List.of("a", "a"), g.findCycle().orElseThrow());
    }

    @Test
    void acyclicGraphHasNoCycle() {
        DependencyGraph<Integer> g = new DependencyGraph<>();
        g.add(1, 2);
        assertTrue(g.findCycle().isEmpty());
    }

    @Test
    void dependentsAreTransitive() {
        DependencyGraph<String> g = new DependencyGraph<>();
        g.add("app", "lib");
        g.add("lib", "log");
        g.add("tool", "log");
        assertEquals(Set.of("lib", "app", "tool"), g.dependents("log"));
        assertEquals(Set.of(), g.dependents("app"));
    }
}
