package io.github.creezzi.depgraph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/** Maps each node to the nodes it depends on. Nodes must be mutually comparable for stable output. */
public final class DependencyGraph<T extends Comparable<T>> {
    private final Map<T, Set<T>> deps = new TreeMap<>();

    @SafeVarargs
    public final void add(T node, T... needs) {
        deps.computeIfAbsent(node, k -> new HashSet<>());
        for (T need : needs) {
            deps.get(node).add(need);
            deps.computeIfAbsent(need, k -> new HashSet<>());
        }
    }

    public Set<T> nodes() {
        return new LinkedHashSet<>(deps.keySet());
    }

    /** Returns one cycle as [a, b, ..., a] if the graph has one. */
    public Optional<List<T>> findCycle() {
        Map<T, Integer> state = new HashMap<>(); // 1 = visiting, 2 = done
        Deque<T> stack = new ArrayDeque<>();
        for (T node : deps.keySet()) {
            if (!state.containsKey(node)) {
                List<T> found = visit(node, state, stack);
                if (found != null) {
                    return Optional.of(found);
                }
            }
        }
        return Optional.empty();
    }

    private List<T> visit(T node, Map<T, Integer> state, Deque<T> stack) {
        state.put(node, 1);
        stack.addLast(node);
        for (T next : new java.util.TreeSet<>(deps.get(node))) {
            Integer s = state.get(next);
            if (s != null && s == 1) {
                List<T> path = new ArrayList<>(stack);
                List<T> cycle = new ArrayList<>(path.subList(path.indexOf(next), path.size()));
                cycle.add(next);
                return cycle;
            }
            if (s == null) {
                List<T> found = visit(next, state, stack);
                if (found != null) {
                    return found;
                }
            }
        }
        stack.removeLast();
        state.put(node, 2);
        return null;
    }

    /** Groups of nodes whose dependencies are all in earlier groups. */
    public List<List<T>> layers() {
        findCycle().ifPresent(c -> {
            throw new IllegalStateException("dependency cycle: " + c);
        });
        Map<T, Set<T>> remaining = new TreeMap<>();
        deps.forEach((k, v) -> remaining.put(k, new HashSet<>(v)));
        List<List<T>> result = new ArrayList<>();
        while (!remaining.isEmpty()) {
            List<T> ready = new ArrayList<>();
            remaining.forEach((k, v) -> {
                if (v.isEmpty()) {
                    ready.add(k);
                }
            });
            ready.forEach(remaining::remove);
            remaining.values().forEach(v -> v.removeAll(ready));
            result.add(ready);
        }
        return result;
    }

    public List<T> order() {
        List<T> out = new ArrayList<>();
        layers().forEach(out::addAll);
        return out;
    }

    /** Everything that transitively depends on the given node. */
    public Set<T> dependents(T node) {
        Set<T> seen = new LinkedHashSet<>();
        Deque<T> queue = new ArrayDeque<>(List.of(node));
        while (!queue.isEmpty()) {
            T current = queue.poll();
            deps.forEach((k, v) -> {
                if (v.contains(current) && seen.add(k)) {
                    queue.add(k);
                }
            });
        }
        return seen;
    }
}
