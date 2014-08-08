package pydecompiler.util;

import pydecompiler.dis.ASTNode;

public class FastStack {
  public FastStack(int size) {
    m_size = size;
    
    m_ptr = -1;
    m_stack = new ASTNode[m_size];
  }

  public FastStack(FastStack copy) {
    m_size = copy.m_size;
    m_ptr = copy.m_ptr;
    
    m_stack = new ASTNode[m_size];

    for (int i = 0; i <= m_ptr; i++)
      m_stack[i] = copy.m_stack[i];
  }

//  public FastStack operator(FastStack copy) {
//    replace(copy);
//    return this;
//  }

  public void push(ASTNode node) {
    if (m_size == m_ptr + 1)
      grow(1);

    m_stack[++m_ptr] = node;
    count++;
  }

  public void pop() {
    if (m_ptr > -1)
      m_stack[m_ptr--] = ASTNode.Node_NULL;
    count--;
  }

  public ASTNode peek() {
    if (m_ptr > -1) {
      if (m_stack[m_ptr] == null)
        System.err.println("m_ptr : " +  m_ptr + ", " + m_stack[m_ptr]);
      return m_stack[m_ptr];
    } else
      return ASTNode.Node_NULL;
  }

  public void replace(FastStack copy) {
    if (copy == this)
      return;
    m_stack = null;

    m_size = copy.m_size;
    m_ptr = copy.m_ptr;
    m_stack = new ASTNode[m_size];
    for (int i = 0; i <= m_ptr; i++)
      m_stack[i] = copy.m_stack[i];
  }

  public void grow(int inc) {
    m_size += inc;
    ASTNode[] tmp = new ASTNode[m_size];

    for (int i = 0; i <= m_ptr; i++)
      tmp[i] = m_stack[i];

    m_stack = null;
    m_stack = tmp;
  }

  private ASTNode[] m_stack;
  private int m_size, m_ptr, count;
}
