package pydecompiler.util;

import java.util.ArrayDeque;

public class Stack<E> extends ArrayDeque<E> {
  private static final long serialVersionUID = Stack.class.hashCode() & 2340985798034038923L;

  public E top() {
    return peekFirst();
  }

  public boolean empty() {
    return this.isEmpty();
  }
}
