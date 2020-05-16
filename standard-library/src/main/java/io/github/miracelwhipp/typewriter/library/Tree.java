package io.github.miracelwhipp.typewriter.library;

import java.util.ArrayList;
import java.util.List;

public class Tree<Type> {

    private final Type data;
    private final List<Tree<Type>> children;

    public Tree(Type data, List<Tree<Type>> children) {
        this.data = data;
        this.children = children;
    }

    public Tree(Type data) {
        this(data, new ArrayList<>());
    }

    public Type getData() {
        return data;
    }

    public List<Tree<Type>> getChildren() {
        return children;
    }
}
