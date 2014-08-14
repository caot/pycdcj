package pydecompiler.util;

import java.util.ArrayDeque;

import pydecompiler.dis.ASTNode;

public class FastStack extends ArrayDeque {
  public FastStack() {
  }

  public FastStack(int size) {
    super(size);
  }

  public void push(ASTNode node) {
    if (node == null)
      node = new ASTNode();
    super.push(node);
    count++;
  }

  public int size() {
    return count;
  }

  public Object pop() {
    count--;
    if (super.isEmpty())
      return new ASTNode();
    return super.pop();
  }

  public boolean empty() {
    return this.isEmpty();
  }

  public ASTNode top() {
    if (super.isEmpty())
      return new ASTNode();
    return (ASTNode) super.peek();
  }

  public ASTNode peek() {
    return (ASTNode) super.peek();
  }

  private int count;
}
