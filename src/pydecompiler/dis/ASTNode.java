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

  ASTNode() {
    m_type = Type.NODE_INVALID;
  }

  ASTNode(Type type) {
    m_type = type;
  }

  Type type() {
    return m_type;
  }

  private int m_refs;
  protected Type m_type;

  public static final ASTNode Node_NULL = new ASTNode(); // // ***************
};

class ASTNodeList extends ASTNode {
  ASTNodeList(LinkedList<ASTNode> nodes) {
    super(Type.NODE_NODELIST);
    m_nodes = nodes;
  }

  LinkedList<ASTNode> nodes() {
    return m_nodes;
  }

  // void removeFirst();
  // void removeLast();
  void append(ASTNode node) {
    m_nodes.add(node);
  }

  void removeLast() {

    m_nodes.remove(m_nodes.size() - 1);
  }

  void removeFirst() {
    m_nodes.remove(0);
  }

  private LinkedList<ASTNode> m_nodes;
};

class ASTObject extends ASTNode {
  ASTObject(PycObject obj) {
    super(Type.NODE_OBJECT);
    m_obj = obj;
  }

  PycObject object() {
    return m_obj;
  }

  private PycObject m_obj;
};

class ASTUnary extends ASTNode {
  enum UnOp {
    UN_POSITIVE, UN_NEGATIVE, UN_INVERT, UN_NOT
  };

  ASTUnary(ASTNode operand, Object op) {
    super(Type.NODE_UNARY);
    m_op = op;
    m_operand = operand;
  }

  ASTNode operand() {
    return m_operand;
  }

  Object op() {
    return m_op;
  }

  static String s_op_strings[] = { "+", "-", "~", "not " };

  String op_str() {
    return s_op_strings[((UnOp) op()).ordinal()];
  }

  protected Object m_op;

  private ASTNode m_operand;
};


class ASTBinary extends ASTNode {
  enum BinOp {
    BIN_ATTR, BIN_POWER, BIN_MULTIPLY, BIN_DIVIDE, BIN_FLOOR, BIN_MODULO,
    BIN_ADD, BIN_SUBTRACT, BIN_LSHIFT, BIN_RSHIFT, BIN_AND, BIN_XOR,
    BIN_OR, BIN_LOG_AND, BIN_LOG_OR, BIN_IP_ADD, BIN_IP_SUBTRACT,
    BIN_IP_MULTIPLY, BIN_IP_DIVIDE, BIN_IP_MODULO, BIN_IP_POWER,
    BIN_IP_LSHIFT, BIN_IP_RSHIFT, BIN_IP_AND, BIN_IP_XOR, BIN_IP_OR,
    BIN_IP_FLOOR,
  };

  ASTBinary(ASTNode left, ASTNode right, Object op) {
    super(Type.NODE_BINARY);
    m_op = op;
    m_left = left;
    m_right = right;
  }

  ASTBinary(ASTNode left, ASTNode right, Object op, Type type) {
    this(left, right, op);
    m_type = type;
  }

  ASTNode left() {
    return m_left;
  }

  ASTNode right() {
    return m_right;
  }

  Object op() {
    return m_op;
  }

  int opordinal() {
    return ((BinOp) m_op).ordinal();
  }

  boolean is_inplace() {
    return opordinal() >= BinOp.BIN_IP_ADD.ordinal();
  }

  static String s_op_strings[] = {
    ".", " ** ", " * ", " / ", " // ", " % ", " + ", " - ",
    " << ", " >> ", " & ", " ^ ", " | ", " and ", " or ",
    " += ", " -= ", " *= ", " /= ", " %= ", " **= ", " <<= ",
    " >>= ", " &= ", " ^= ", " |= ", " //= "
 };

  String op_str() {
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
    CMP_EXCEPTION, CMP_BAD
  };

  ASTCompare(ASTNode left, ASTNode right, Object op) {
    super(left, right, op, Type.NODE_COMPARE);
        }

  static String s_cmp_strings[] = {
    " < ", " <= ", " == ", " != ", " > ", " >= ", " in ", " not in ", " is ", " is not ",
    "<EXCEPTION MATCH>", "<BAD>"
  };

  Object op() {
    return m_op;
  }

  String op_str() {
    return s_cmp_strings[((Integer) op()).intValue()];
  }
};

class ASTSlice extends ASTBinary {
  enum SliceOp {
    SLICE0, SLICE1, SLICE2, SLICE3
  };

  ASTSlice(SliceOp op) {
    super(Node_NULL, Node_NULL, op, Type.NODE_SLICE);
  }

  ASTSlice(SliceOp op, ASTNode left) {
    super(left, Node_NULL, op, Type.NODE_SLICE);
  }

  ASTSlice(SliceOp op, ASTNode left, ASTNode right) {
    super(left, right, op, Type.NODE_SLICE);
  }
};

class ASTStore extends ASTNode {
  ASTStore(ASTNode src, ASTNode dest) {
    super(Type.NODE_STORE);
    m_src = src;
    m_dest = dest;
  }

  ASTNode src() {
    return m_src;
  }

  ASTNode dest() {
    return m_dest;
  }

  private ASTNode m_src;
  private ASTNode m_dest;
};

class ASTReturn extends ASTNode {
  enum RetType {
    RETURN, YIELD
  };

  ASTReturn(ASTNode value) {
    this(value, RetType.RETURN);
  }

  ASTReturn(ASTNode value, RetType rettype) {
    super(Type.NODE_RETURN);
    m_value = value;
    m_rettype = rettype;
  }

  ASTNode value() {
    return m_value;
  }

  RetType rettype() {
    return m_rettype;
  }

  private ASTNode m_value;
  private RetType m_rettype;
};

class ASTName extends ASTNode {
  ASTName(PycString name) {
    super(Type.NODE_NAME);
    m_name = name;
  }

  PycString name() {
    return m_name;
  }

  private PycString m_name;
};

class ASTDelete extends ASTNode {
  public ASTDelete(ASTNode value) {
    super(Type.NODE_DELETE);
    m_value = value;
  }

  ASTNode value() {
    return m_value;
  }

  private ASTNode m_value;
};

class ASTFunction extends ASTNode {
  ASTFunction(ASTNode code, List<ASTNode> defArgs) {
    super(Type.NODE_FUNCTION);
    m_code = code;
    m_defargs = defArgs;
  }

  ASTNode code() {
    return m_code;
  }

  List<ASTNode> defargs() {
    return m_defargs;
  }

  private ASTNode m_code;
  private List<ASTNode> m_defargs;
};

class ASTClass extends ASTNode {
  ASTClass(ASTNode code, ASTNode bases, ASTNode name) {
    super(Type.NODE_CLASS);
    m_code = code;
    m_bases = bases;
    m_name = name;
  }

  ASTNode code() {
    return m_code;
  }

  ASTNode bases() {
    return m_bases;
  }

  ASTNode name() {
    return m_name;
  }

  private ASTNode m_code;
  private ASTNode m_bases;
  private ASTNode m_name;
};

class ASTCall extends ASTNode {
  // public List<ASTNode> pparam_t;
  // List<Pair<ASTNode, ASTNode >> kwparam_t;

  ASTCall(ASTNode func, LinkedList<ASTNode> pparams,
      LinkedList<Pair<ASTNode, ASTNode>> kwparams) {
    super(Type.NODE_CALL);
    m_func = func;
    m_pparams = pparams;
    m_kwparams = kwparams;
    m_var = Node_NULL;
    m_kw = Node_NULL;
  }

  ASTNode func() {
    return m_func;
  }

  LinkedList<ASTNode> pparams() {
    return m_pparams;
  }

  LinkedList<Pair<ASTNode, ASTNode>> kwparams() {
    return m_kwparams;
  }

  ASTNode var() {
    return m_var;
  }

  ASTNode kw() {
    return m_kw;
  }

  boolean hasVar() {
    return m_var != Node_NULL;
  }

  boolean hasKW() {
    return m_kw != Node_NULL;
  }

  void setVar(ASTNode var) {
    m_var = var;
  }

  void setKW(ASTNode kw) {
    m_kw = kw;
  }

  private ASTNode m_func;
  private LinkedList<ASTNode> m_pparams;
  private LinkedList<Pair<ASTNode, ASTNode>> m_kwparams;
  private ASTNode m_var;
  private ASTNode m_kw;
}


class ASTImport extends ASTNode {
  ASTImport(ASTNode name, ASTNode fromlist) {
    super(Type.NODE_IMPORT);
    m_name = name;
    m_fromlist = fromlist;

    m_stores = new LinkedList<ASTStore>();
  }

  ASTNode name() {
    return m_name;
  }

  List<ASTStore> stores() {
    return m_stores;
  }

  void add_store(ASTStore store) {
    m_stores.add(store);
  }

  ASTNode fromlist() {
    return m_fromlist;
  }

  private ASTNode m_name;
  private List<ASTStore> m_stores;

  private ASTNode m_fromlist;
};

class ASTTuple extends ASTNode {
  ASTTuple(List<ASTNode> values) {
    super(Type.NODE_TUPLE);
    m_values = values;
  }

  List<ASTNode> values() {
    return m_values;
  }

  void add(ASTNode name) {
    m_values.add(name);
  }

  private List<ASTNode> m_values;
};

class ASTList extends ASTNode {
  List<ASTNode> value_t;

  ASTList(List<ASTNode> values) {
    super(Type.NODE_LIST);
    m_values = values;
  }

  List<ASTNode> values() {
    return m_values;
  }

  private List<ASTNode> m_values;
};

class ASTMap extends ASTNode {
  ASTMap() {
    super(Type.NODE_MAP);
    m_values = new LinkedList<Pair<ASTNode, ASTNode>>();
  }

  void add(ASTNode key, ASTNode value) {
    m_values.add(new Pair(key, value));
  }

  List<Pair<ASTNode, ASTNode>> values() {
    return m_values;
  }

  private List<Pair<ASTNode, ASTNode>> m_values;
};

class ASTSubscr extends ASTNode {
  ASTSubscr(ASTNode name, ASTNode key) {
    super(Type.NODE_SUBSCR);
    m_name = name;
    m_key = key;
  }

  ASTNode name() {
    return m_name;
  }

  ASTNode key() {
    return m_key;
  }

  private ASTNode m_name;
  private ASTNode m_key;
};

class ASTPrint extends ASTNode {
  ASTPrint(ASTNode value) {
    super(Type.NODE_PRINT);
    m_value = value;
    m_stream = Node_NULL;
  }

  ASTPrint(ASTNode value, ASTNode stream) {
    this(value);
    m_stream = stream;
  }

  ASTNode value() {
    return m_value;
  }

  ASTNode stream() {
    return m_stream;
  }

  private ASTNode m_value;
  private ASTNode m_stream;
};

class ASTConvert extends ASTNode {
  ASTConvert(ASTNode name) {
    super(Type.NODE_CONVERT);
    m_name = name;
  }

  ASTNode name() {
    return m_name;
  }

  private ASTNode m_name;
};

class ASTKeyword extends ASTNode {
  enum Word {
    KW_BREAK, KW_CONTINUE
  };

  ASTKeyword(Word key) {
    super(Type.NODE_KEYWORD);
    m_key = key;
  }

  Word key() {
    return m_key;
  }

  // char* word_str();

  static String s_word_strings[] = { "break", "continue" };

  String word_str() {
    return s_word_strings[key().ordinal()];
  }

  private Word m_key;
};

class ASTRaise extends ASTNode {
  ASTRaise(LinkedList<ASTNode> params) {
    super(Type.NODE_RAISE);
    m_params = params;
  }

  LinkedList<ASTNode> params() {
    return m_params;
  }

  private LinkedList<ASTNode> m_params;
};

class ASTExec extends ASTNode {
  ASTExec(ASTNode stmt, ASTNode glob, ASTNode loc) {
    super(Type.NODE_EXEC);
    m_stmt = stmt;
    m_glob = glob;
    m_loc = loc;
  }

  ASTNode statement() {
    return m_stmt;
  }

  ASTNode globals() {
    return m_glob;
  }

  ASTNode locals() {
    return m_loc;
  }

  private ASTNode m_stmt;
  private ASTNode m_glob;
  private ASTNode m_loc;
};

class ASTBlock extends ASTNode {
  public static enum BlkType {
    BLK_MAIN, BLK_IF, BLK_ELSE, BLK_ELIF, BLK_TRY,
    BLK_CONTAINER, BLK_EXCEPT, BLK_FINALLY,
    BLK_WHILE, BLK_FOR, BLK_WITH
  };

  ASTBlock(BlkType blktype) {
    this(blktype, 0, false);
  }

  ASTBlock(BlkType blktype, int end) {
    this(blktype, end, false);
  }

  ASTBlock(BlkType blktype, int end, boolean inited) {
    super(Type.NODE_BLOCK);
    m_blktype = blktype;
    m_end = end;
    m_inited = inited;
    m_nodes = new LinkedList<ASTNode>();
  }

  BlkType blktype() {
    return m_blktype;
  }

  int end() {
    return m_end;
  }

  LinkedList<ASTNode> nodes() {
    return m_nodes;
  }

  int size() {
    return m_nodes.size();
  }

  // void removeFirst();
  // void removeLast();
  void append(ASTNode node) {
    m_nodes.add(node);
  }

  Object inited() {
    return m_inited;
  }

  void init() {
    m_inited = Boolean.TRUE;
  }

  void init(Object init) {
    m_inited = init;
  }

  void removeLast() {
    m_nodes.removeLast();
  }

  void removeFirst() {
    m_nodes.removeFirst();
  }

  static String s_type_strings[] = { "", "if", "else", "elif", "try",
      "CONTAINER", "except", "finally", "while", "for", "with", };

  String type_str() {
    return s_type_strings[blktype().ordinal()];
  }

  void setEnd(int end) {
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

  ASTCondBlock(BlkType blktype, int end, ASTNode cond) {
    this(blktype, end, cond, false);
    m_cond = cond;
  }

  ASTCondBlock(BlkType blktype, int end, ASTNode cond, boolean negative) {
    super(blktype, end);
    m_cond = cond;
    m_negative = negative;
  }

  void init() {
  }

  void init(InitCond init) {
    m_inited = init;
  }

  ASTNode cond() {
    return m_cond;
  }

  boolean negative() {
    return m_negative;
  }

  private ASTNode m_cond;
  private boolean m_negative;
}


class ASTIterBlock extends ASTBlock {
  ASTIterBlock(BlkType blktype, int end, ASTNode iter) {
    super(blktype, end);
    m_iter = iter;
    m_idx = null;
    m_comp = false;
  }

  ASTNode iter() {
    return m_iter;
  }

  ASTNode index() {
    return m_idx;
  }

  boolean isComprehension() {
    return m_comp;
  }

  void setIndex(ASTNode idx) {
    m_idx = idx;
    init();
  }

  void setComprehension(boolean comp) {
    m_comp = comp;
  }

  private ASTNode m_iter;
  private ASTNode m_idx;
  private boolean m_comp;
}


class ASTContainerBlock extends ASTBlock {
  ASTContainerBlock(int finally_) {
    this(finally_, 0);
  }

  ASTContainerBlock(int finally_, int except) {
    super(BlkType.BLK_CONTAINER, 0);
    m_finally = finally_;
    m_except = except;
  }

  boolean hasFinally() {
    return m_finally != 0;
  }

  boolean hasExcept() {
    return m_except != 0;
  }

  int finally_() {
    return m_finally;
  }

  int except() {
    return m_except;
  }

  void setExcept(int except) {
    m_except = except;
  }

  private int m_finally;
  private int m_except;
}


class ASTWithBlock extends ASTBlock {
  ASTWithBlock(int end) {
    super(BlkType.BLK_WITH, end);
  }

  ASTNode expr() {
    return m_expr;
  }

  ASTNode var() {
    return m_var;
  }

  void setExpr(ASTNode expr) {
    m_expr = expr;
    init();
  }

  void setVar(ASTNode var) {
    m_var = var;
  }

  private ASTNode m_expr;
  private ASTNode m_var; // optional value
}


class ASTComprehension extends ASTNode {
  ASTComprehension(ASTNode result) {
    super(Type.NODE_COMPREHENSION);
    m_result = result;
  }

  ASTNode result() {
    return m_result;
  }

  List<ASTIterBlock> generators() {
    return m_generators;
  }

  void addGenerator(ASTIterBlock gen) {
    m_generators.add(gen);
  }

  private ASTNode m_result;
  private List<ASTIterBlock> m_generators;
}
