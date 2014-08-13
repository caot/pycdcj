package pydecompiler.dis;

import java.util.LinkedList;
import java.util.List;

import pydecompiler.util.Pair;


public class ASTNode {

/* Similar interface to PycObject, so PycRef can work on it... *
 * However, this does *NOT* mean the two are interchangeable!  */

  public enum Type {
    NODE_INVALID, NODE_NODELIST, NODE_OBJECT, NODE_UNARY, NODE_BINARY,
    NODE_COMPARE, NODE_SLICE, NODE_STORE, NODE_RETURN, NODE_NAME,
    NODE_DELETE, NODE_FUNCTION, NODE_CLASS, NODE_CALL, NODE_IMPORT,
    NODE_TUPLE, NODE_LIST, NODE_MAP, NODE_SUBSCR, NODE_PRINT,
    NODE_CONVERT, NODE_KEYWORD, NODE_RAISE, NODE_EXEC, NODE_BLOCK,
    NODE_COMPREHENSION,

    // Empty nodes
    NODE_PASS, NODE_LOCALS
  };

  public ASTNode() {
    this(Type.NODE_INVALID);
  }

  public ASTNode(Type type) {
    m_type = type;
  }

  public Type type() {
    return m_type;
  }

  private int m_refs;
  protected Type m_type;

  public static final ASTNode Node_NULL = new ASTNode();
}


class ASTNodeList extends ASTNode {
  public ASTNodeList(LinkedList<ASTNode> nodes) {
    super(Type.NODE_NODELIST);
    m_nodes = nodes;
  }

  public LinkedList<ASTNode> nodes() {
    return m_nodes;
  }

  public void removeLast() {
    m_nodes.removeLast();
  }

  public void removeFirst() {
    m_nodes.removeFirst();
  }

  public void append(ASTNode node) {
    m_nodes.add(node);
  }

  private LinkedList<ASTNode> m_nodes;
}


class ASTObject extends ASTNode {
  public ASTObject(PycObject obj) {
    super(Type.NODE_OBJECT);
    m_obj = obj;
  }

  public PycObject object() {
    return m_obj;
  }

  private PycObject m_obj;
}


class ASTUnary extends ASTNode {
  public enum UnOp {
    UN_POSITIVE, UN_NEGATIVE, UN_INVERT, UN_NOT
  };

  public ASTUnary(ASTNode operand, Object op) {
    super(Type.NODE_UNARY);
    m_op = op;
    m_operand = operand;
  }

  public ASTNode operand() {
    return m_operand;
  }

  public Object op() {
    return m_op;
  }

  static String s_op_strings[] = { "+", "-", "~", "not " };

  String op_str() {
    return s_op_strings[((UnOp) op()).ordinal()];
  }

  protected Object m_op;
  private ASTNode m_operand;
}


class ASTBinary extends ASTNode {
  public enum BinOp {
    BIN_ATTR, BIN_POWER, BIN_MULTIPLY, BIN_DIVIDE, BIN_FLOOR, BIN_MODULO,
    BIN_ADD, BIN_SUBTRACT, BIN_LSHIFT, BIN_RSHIFT, BIN_AND, BIN_XOR,
    BIN_OR, BIN_LOG_AND, BIN_LOG_OR, BIN_IP_ADD, BIN_IP_SUBTRACT,
    BIN_IP_MULTIPLY, BIN_IP_DIVIDE, BIN_IP_MODULO, BIN_IP_POWER,
    BIN_IP_LSHIFT, BIN_IP_RSHIFT, BIN_IP_AND, BIN_IP_XOR, BIN_IP_OR,
    BIN_IP_FLOOR,
  }

  public ASTBinary(ASTNode left, ASTNode right, Object op) {
    this(left, right, op, Type.NODE_BINARY);
  }

  public ASTBinary(ASTNode left, ASTNode right, Object op, Type type) {
    super(type);
    m_left = left;
    m_right = right;
    m_op = op;
  }

  public ASTNode left() {
    return m_left;
  }

  public ASTNode right() {
    return m_right;
  }

  public Object op() {
    return m_op;
  }

  public int opordinal() {
    return ((BinOp) m_op).ordinal();
  }

  public boolean is_inplace() {
    return opordinal() >= BinOp.BIN_IP_ADD.ordinal();
  }

  static String s_op_strings[] = {
    ".", " ** ", " * ", " / ", " // ", " % ", " + ", " - ",
    " << ", " >> ", " & ", " ^ ", " | ", " and ", " or ",
    " += ", " -= ", " *= ", " /= ", " %= ", " **= ", " <<= ",
    " >>= ", " &= ", " ^= ", " |= ", " //= "
  };

  public String op_str() {
    return s_op_strings[((BinOp) op()).ordinal()];
  }

  protected Object m_op;
  private ASTNode m_left;
  private ASTNode m_right;
}


class ASTCompare extends ASTBinary {
  enum CompareOp {
    CMP_LESS, CMP_LESS_EQUAL, CMP_EQUAL, CMP_NOT_EQUAL, CMP_GREATER,
    CMP_GREATER_EQUAL, CMP_IN, CMP_NOT_IN, CMP_IS, CMP_IS_NOT,
    CMP_EXCEPTION, CMP_BAD;
  };

  ASTCompare(ASTNode left, ASTNode right, int op) {
    super(left, right, CompareOp.values()[op], Type.NODE_COMPARE);
  }

  static String s_cmp_strings[] = {
    " < ", " <= ", " == ", " != ", " > ", " >= ", " in ", " not in ", " is ", " is not ",
    "<EXCEPTION MATCH>", "<BAD>"
  };

  public String op_str() {
    return s_cmp_strings[((CompareOp) op()).ordinal()];
  }
}


class ASTSlice extends ASTBinary {
  public enum SliceOp {
    SLICE0, SLICE1, SLICE2, SLICE3
  };

  public ASTSlice(SliceOp op) {
    this(op, Node_NULL);
  }

  public ASTSlice(SliceOp op, ASTNode left) {
    this(op, left, Node_NULL);
  }

  public ASTSlice(SliceOp op, ASTNode left, ASTNode right) {
    super(left, right, op, Type.NODE_SLICE);
  }
}


class ASTStore extends ASTNode {
  public ASTStore(ASTNode src, ASTNode dest) {
    super(Type.NODE_STORE);
    m_src = src;
    m_dest = dest;
  }

  public ASTNode src() {
    return m_src;
  }

  public ASTNode dest() {
    return m_dest;
  }

  private ASTNode m_src;
  private ASTNode m_dest;
}


class ASTReturn extends ASTNode {
  public enum RetType {
    RETURN, YIELD
  }

  public ASTReturn(ASTNode value) {
    this(value, RetType.RETURN);
  }

  public ASTReturn(ASTNode value, RetType rettype) {
    super(Type.NODE_RETURN);
    m_value = value;
    m_rettype = rettype;
  }

  public ASTNode value() {
    return m_value;
  }

  public RetType rettype() {
    return m_rettype;
  }

  private ASTNode m_value;
  private RetType m_rettype;
}


class ASTName extends ASTNode {
  public ASTName(PycString name) {
    super(Type.NODE_NAME);
    m_name = name;
  }

  public PycString name() {
    return m_name;
  }

  private PycString m_name;
}


class ASTDelete extends ASTNode {
  public ASTDelete(ASTNode value) {
    super(Type.NODE_DELETE);
    m_value = value;
  }

  public ASTNode value() {
    return m_value;
  }

  private ASTNode m_value;
}


class ASTFunction extends ASTNode {
  public ASTFunction(ASTNode code, List<ASTNode> defArgs) {
    super(Type.NODE_FUNCTION);
    m_code = code;
    m_defargs = defArgs;
  }

  public ASTNode code() {
    return m_code;
  }

  public List<ASTNode> defargs() {
    return m_defargs;
  }

  private ASTNode m_code;
  private List<ASTNode> m_defargs;
}


class ASTClass extends ASTNode {
  public ASTClass(ASTNode code, ASTNode bases, ASTNode name) {
    super(Type.NODE_CLASS);
    m_code = code;
    m_bases = bases;
    m_name = name;
  }

  public ASTNode code() {
    return m_code;
  }

  public ASTNode bases() {
    return m_bases;
  }

  public ASTNode name() {
    return m_name;
  }

  private ASTNode m_code;
  private ASTNode m_bases;
  private ASTNode m_name;
}


class ASTCall extends ASTNode {
  public ASTCall(ASTNode func, LinkedList<ASTNode> pparams,
      LinkedList<Pair<ASTNode, ASTNode>> kwparams) {
    super(Type.NODE_CALL);
    m_func = func;
    m_pparams = pparams;
    m_kwparams = kwparams;
    m_var = Node_NULL;
    m_kw = Node_NULL;
  }

  public ASTNode func() {
    return m_func;
  }

  public LinkedList<ASTNode> pparams() {
    return m_pparams;
  }

  public LinkedList<Pair<ASTNode, ASTNode>> kwparams() {
    return m_kwparams;
  }

  public ASTNode var() {
    return m_var;
  }

  public ASTNode kw() {
    return m_kw;
  }

  public boolean hasVar() {
    return m_var != Node_NULL;
  }

  public boolean hasKW() {
    return m_kw != Node_NULL;
  }

  public void setVar(ASTNode var) {
    m_var = var;
  }

  public void setKW(ASTNode kw) {
    m_kw = kw;
  }

  private ASTNode m_func;
  private LinkedList<ASTNode> m_pparams;
  private LinkedList<Pair<ASTNode, ASTNode>> m_kwparams;
  private ASTNode m_var;
  private ASTNode m_kw;
}


class ASTImport extends ASTNode {
  public ASTImport(ASTNode name, ASTNode fromlist) {
    super(Type.NODE_IMPORT);
    m_name = name;
    m_fromlist = fromlist;

    m_stores = new LinkedList<ASTStore>();
  }

  public ASTNode name() {
    return m_name;
  }

  public List<ASTStore> stores() {
    return m_stores;
  }

  public void add_store(ASTStore store) {
    m_stores.add(store);
  }

  public ASTNode fromlist() {
    return m_fromlist;
  }

  private ASTNode m_name;
  private List<ASTStore> m_stores;

  private ASTNode m_fromlist;
}


class ASTTuple extends ASTNode {
  public ASTTuple(List<ASTNode> values) {
    super(Type.NODE_TUPLE);
    m_values = values;
  }

  public List<ASTNode> values() {
    return m_values;
  }

  public void add(ASTNode name) {
    m_values.add(name);
  }

  private List<ASTNode> m_values;
}


class ASTList extends ASTNode {
  public ASTList(List<ASTNode> values) {
    super(Type.NODE_LIST);
    m_values = values;
  }

  public List<ASTNode> values() {
    return m_values;
  }

  private List<ASTNode> m_values;
}


class ASTMap extends ASTNode {
  public ASTMap() {
    super(Type.NODE_MAP);
    m_values = new LinkedList<Pair<ASTNode, ASTNode>>();
  }

  public void add(ASTNode key, ASTNode value) {
    m_values.add(new Pair<ASTNode, ASTNode>(key, value));
  }

  public List<Pair<ASTNode, ASTNode>> values() {
    return m_values;
  }

  private List<Pair<ASTNode, ASTNode>> m_values;
}


class ASTSubscr extends ASTNode {
  public ASTSubscr(ASTNode name, ASTNode key) {
    super(Type.NODE_SUBSCR);
    m_name = name;
    m_key = key;
  }

  public ASTNode name() {
    return m_name;
  }

  public ASTNode key() {
    return m_key;
  }

  private ASTNode m_name;
  private ASTNode m_key;
}


class ASTPrint extends ASTNode {
  public ASTPrint(ASTNode value) {
    this(value, Node_NULL);
  }

  public ASTPrint(ASTNode value, ASTNode stream) {
    super(Type.NODE_PRINT);
    m_value = value;
    m_stream = stream;
  }

  public ASTNode value() {
    return m_value;
  }

  public ASTNode stream() {
    return m_stream;
  }

  private ASTNode m_value;
  private ASTNode m_stream;
}


class ASTConvert extends ASTNode {
  public ASTConvert(ASTNode name) {
    super(Type.NODE_CONVERT);
    m_name = name;
  }

  public ASTNode name() {
    return m_name;
  }

  private ASTNode m_name;
}


class ASTKeyword extends ASTNode {
  enum Word {
    KW_BREAK, KW_CONTINUE
  };

  public ASTKeyword(Word key) {
    super(Type.NODE_KEYWORD);
    m_key = key;
  }

  public Word key() {
    return m_key;
  }

  static String s_word_strings[] = { "break", "continue" };

  public String word_str() {
    return s_word_strings[key().ordinal()];
  }

  private Word m_key;
}


class ASTRaise extends ASTNode {
  public ASTRaise(LinkedList<ASTNode> params) {
    super(Type.NODE_RAISE);
    m_params = params;
  }

  public LinkedList<ASTNode> params() {
    return m_params;
  }

  private LinkedList<ASTNode> m_params;
}


class ASTExec extends ASTNode {
  public ASTExec(ASTNode stmt, ASTNode glob, ASTNode loc) {
    super(Type.NODE_EXEC);
    m_stmt = stmt;
    m_glob = glob;
    m_loc = loc;
  }

  public ASTNode statement() {
    return m_stmt;
  }

  public ASTNode globals() {
    return m_glob;
  }

  public ASTNode locals() {
    return m_loc;
  }

  private ASTNode m_stmt;
  private ASTNode m_glob;
  private ASTNode m_loc;
}


class ASTBlock extends ASTNode {
  public static enum BlkType {
    BLK_MAIN, BLK_IF, BLK_ELSE, BLK_ELIF, BLK_TRY,
    BLK_CONTAINER, BLK_EXCEPT, BLK_FINALLY,
    BLK_WHILE, BLK_FOR, BLK_WITH
  };

  public ASTBlock(BlkType blktype) {
    this(blktype, 0, false);
  }

  public ASTBlock(BlkType blktype, int end) {
    this(blktype, end, false);
  }

  public ASTBlock(BlkType blktype, int end, boolean inited) {
    super(Type.NODE_BLOCK);
    m_blktype = blktype;
    m_end = end;
    m_inited = inited;
    m_nodes = new LinkedList<ASTNode>();
  }

  public BlkType blktype() {
    return m_blktype;
  }

  public int end() {
    return m_end;
  }

  public LinkedList<ASTNode> nodes() {
    return m_nodes;
  }

  public int size() {
    return m_nodes.size();
  }

  public void removeLast() {
    m_nodes.removeLast();
  }

  public void removeFirst() {
    m_nodes.removeFirst();
  }
  
  public void append(ASTNode node) {
    m_nodes.add(node);
  }

  static String s_type_strings[] = { "", "if", "else", "elif", "try",
      "CONTAINER", "except", "finally", "while", "for", "with", };

  public String type_str() {
    return s_type_strings[blktype().ordinal()];
  }

  public boolean inited() {
    return m_inited != BlkType.BLK_MAIN;
  }

  public void init() {
    m_inited = BlkType.BLK_IF; // = 1
  }

  public void init(Object init) {
    m_inited = init;
  }
  
  public void setEnd(int end) {
    m_end = end;
  }

  private BlkType m_blktype;
  private int m_end;
  private LinkedList<ASTNode> m_nodes;

  protected Object m_inited; /* Is the block's definition "complete" */
}


class ASTCondBlock extends ASTBlock {
  enum InitCond {
    UNINITED, POPPED, PRE_POPPED
  };

  public ASTCondBlock(BlkType blktype, int end, ASTNode cond) {
    this(blktype, end, cond, false);
  }

  public ASTCondBlock(BlkType blktype, int end, ASTNode cond, boolean negative) {
    super(blktype, end);
    m_cond = cond;
    m_negative = negative;
  }

  public ASTNode cond() {
    return m_cond;
  }

  public boolean inited() {
    return m_inited != InitCond.UNINITED;
  }

  public void init() {
    m_inited = InitCond.POPPED;
  }

  public boolean negative() {
    return m_negative;
  }

  private ASTNode m_cond;
  private boolean m_negative;
}


class ASTIterBlock extends ASTBlock {
  public ASTIterBlock(BlkType blktype, int end, ASTNode iter) {
    super(blktype, end);
    m_iter = iter;
    m_idx = ASTNode.Node_NULL; ////////////////////////
    m_comp = false;
  }

  public ASTNode iter() {
    return m_iter;
  }

  public ASTNode index() {
    return m_idx;
  }

  public boolean isComprehension() {
    return m_comp;
  }

  public void setIndex(ASTNode idx) {
    m_idx = idx;
    init();
  }

  public void setComprehension(boolean comp) {
    m_comp = comp;
  }

  private ASTNode m_iter;
  private ASTNode m_idx;
  private boolean m_comp;
}


class ASTContainerBlock extends ASTBlock {
  public ASTContainerBlock(int finally_) {
    this(finally_, 0);
  }

  public ASTContainerBlock(int finally_, int except) {
    super(BlkType.BLK_CONTAINER, 0);
    m_finally = finally_;
    m_except = except;
  }

  public boolean hasFinally() {
    return m_finally != 0;
  }

  public boolean hasExcept() {
    return m_except != 0;
  }

  public int finally_() {
    return m_finally;
  }

  public int except() {
    return m_except;
  }

  public void setExcept(int except) {
    m_except = except;
  }

  private int m_finally;
  private int m_except;
}


class ASTWithBlock extends ASTBlock {
  public ASTWithBlock(int end) {
    super(BlkType.BLK_WITH, end);
  }

  public ASTNode expr() {
    return m_expr;
  }

  public ASTNode var() {
    return m_var;
  }

  public void setExpr(ASTNode expr) {
    m_expr = expr;
    init();
  }

  public void setVar(ASTNode var) {
    m_var = var;
  }

  private ASTNode m_expr;
  private ASTNode m_var; // optional value
}


class ASTComprehension extends ASTNode {
  public ASTComprehension(ASTNode result) {
    super(Type.NODE_COMPREHENSION);
    m_result = result;
    m_generators = new LinkedList<ASTIterBlock>();
  }

  public ASTNode result() {
    return m_result;
  }

  public List<ASTIterBlock> generators() {
    return m_generators;
  }

  public void addGenerator(ASTIterBlock gen) {
    m_generators.add(gen);
  }

  private ASTNode m_result;
  private List<ASTIterBlock> m_generators;
}
