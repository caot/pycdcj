package pydecompiler.util;

import java.util.ArrayDeque;

public class FastStack<E> extends ArrayDeque<E> {
  public FastStack() {
  }

  public FastStack(int size) {
    super(size);
  }

  public void push(E node) {
    super.push(node);
    count++;
  }

  public int size() {
    return count;
  }

  public E pop() {
    count--;
    return super.pop();
  }

  public boolean empty() {
    return this.isEmpty();
  }

  public E top() {
    return peek();
  }

  public E peek() {
    return super.peek();
  }

  private int count;
}
