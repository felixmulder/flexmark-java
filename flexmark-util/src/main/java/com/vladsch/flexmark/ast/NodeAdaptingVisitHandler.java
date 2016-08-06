package com.vladsch.flexmark.ast;

/**
 * intended to be extended with specific handler function. see {@link VisitHandler}
 * @param <N> subclass of {@link Node}
 * @param <A> subclass of {@link NodeAdaptingVisitor}
 */
public abstract class NodeAdaptingVisitHandler<N extends Node, A extends NodeAdaptingVisitor<N>> {
    final protected Class<? extends N> myClass;
    final protected A myAdapter;

    public NodeAdaptingVisitHandler(Class<? extends N> aClass, A adapter) {
        myClass = aClass;
        myAdapter = adapter;
    }

    public Class<? extends N> getNodeType() {
        return myClass;
    }

    public A getNodeAdapter() {
        return myAdapter;
    }

    // implement whatever function interface is desired for the adapter
    //@Override
    //public void render(Node ast, NodeRendererContext context, HtmlWriter html) {
    //    //noinspection unchecked
    //    myAdapter.render((N)ast, context, html);
    //}
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeAdaptingVisitHandler<?,?> other = (NodeAdaptingVisitHandler<?,?>) o;

        if (myClass != other.myClass) return false;
        return myAdapter == other.myAdapter;
    }

    @Override
    public int hashCode() {
        int result = myClass.hashCode();
        result = 31 * result + myAdapter.hashCode();
        return result;
    }
}