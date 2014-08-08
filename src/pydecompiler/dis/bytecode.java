package pydecompiler.dis;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pydecompiler.dis.Pyc.Opcode;


class Pyc {
  public static enum Opcode {
    /* No parameter word */
    STOP_CODE, POP_TOP, ROT_TWO, ROT_THREE, DUP_TOP, DUP_TOP_TWO,
    UNARY_POSITIVE, UNARY_NEGATIVE, UNARY_NOT, UNARY_CONVERT, UNARY_CALL,
    UNARY_INVERT, BINARY_POWER, BINARY_MULTIPLY, BINARY_DIVIDE, BINARY_MODULO,
    BINARY_ADD, BINARY_SUBTRACT, BINARY_SUBSCR, BINARY_CALL,
    SLICE_0, SLICE_1, SLICE_2, SLICE_3, STORE_SLICE_0, STORE_SLICE_1,
    STORE_SLICE_2, STORE_SLICE_3, DELETE_SLICE_0, DELETE_SLICE_1,
    DELETE_SLICE_2, DELETE_SLICE_3, STORE_SUBSCR, DELETE_SUBSCR,
    BINARY_LSHIFT, BINARY_RSHIFT, BINARY_AND, BINARY_XOR, BINARY_OR,
    PRINT_EXPR, PRINT_ITEM, PRINT_NEWLINE, BREAK_LOOP, RAISE_EXCEPTION,
    LOAD_LOCALS, RETURN_VALUE, LOAD_GLOBALS, EXEC_STMT, BUILD_FUNCTION,
    POP_BLOCK, END_FINALLY, BUILD_CLASS, ROT_FOUR, NOP, LIST_APPEND,
    BINARY_FLOOR_DIVIDE, BINARY_TRUE_DIVIDE, INPLACE_FLOOR_DIVIDE,
    INPLACE_TRUE_DIVIDE, STORE_MAP, INPLACE_ADD, INPLACE_SUBTRACT,
    INPLACE_MULTIPLY, INPLACE_DIVIDE, INPLACE_MODULO, INPLACE_POWER,
    GET_ITER, PRINT_ITEM_TO, PRINT_NEWLINE_TO, INPLACE_LSHIFT,
    INPLACE_RSHIFT, INPLACE_AND, INPLACE_XOR, INPLACE_OR, WITH_CLEANUP,
    IMPORT_STAR, YIELD_VALUE, LOAD_BUILD_CLASS, STORE_LOCALS,
    POP_EXCEPT, SET_ADD, YIELD_FROM,

    /* Has parameter word */
    PYC_HAVE_ARG,
    // STORE_NAME_A(PYC_HAVE_ARG.ordinal()) // = PYC_HAVE_ARG
    DELETE_NAME_A, UNPACK_TUPLE_A,
    UNPACK_LIST_A, UNPACK_ARG_A, STORE_ATTR_A, DELETE_ATTR_A,
    STORE_GLOBAL_A, DELETE_GLOBAL_A, UNPACK_VARARG_A, LOAD_CONST_A,
    LOAD_NAME_A, BUILD_TUPLE_A, BUILD_LIST_A, BUILD_MAP_A, LOAD_ATTR_A,
    COMPARE_OP_A, IMPORT_NAME_A, IMPORT_FROM_A, JUMP_FORWARD_A,
    JUMP_IF_FALSE_A, JUMP_IF_TRUE_A, JUMP_ABSOLUTE_A, FOR_LOOP_A,
    LOAD_LOCAL_A, LOAD_GLOBAL_A, SET_FUNC_ARGS_A, SETUP_LOOP_A,
    SETUP_EXCEPT_A, SETUP_FINALLY_A, RESERVE_FAST_A, LOAD_FAST_A,
    STORE_FAST_A, DELETE_FAST_A, SET_LINENO_A, RAISE_VARARGS_A,
    CALL_FUNCTION_A, MAKE_FUNCTION_A, BUILD_SLICE_A, CALL_FUNCTION_VAR_A,
    CALL_FUNCTION_KW_A, CALL_FUNCTION_VAR_KW_A, UNPACK_SEQUENCE_A, FOR_ITER_A,
    DUP_TOPX_A, BUILD_SET_A, JUMP_IF_FALSE_OR_POP_A, JUMP_IF_TRUE_OR_POP_A,
    POP_JUMP_IF_FALSE_A, POP_JUMP_IF_TRUE_A, CONTINUE_LOOP_A, MAKE_CLOSURE_A,
    LOAD_CLOSURE_A, LOAD_DEREF_A, STORE_DEREF_A, DELETE_DEREF_A,
    EXTENDED_ARG_A, SETUP_WITH_A, SET_ADD_A, MAP_ADD_A, UNPACK_EX_A,
    LIST_APPEND_A,

    PYC_LAST_OPCODE,
    PYC_INVALID_OPCODE(-1),
    STORE_NAME_A(PYC_HAVE_ARG.ordinal()) // = PYC_HAVE_ARG
    ;

    private int opcode;
    private static Map<Integer, Opcode> map = new HashMap<Integer, Opcode>();

    static {
      for (Opcode opcode : Opcode.values()) {
        map.put(opcode.opcode, opcode);
      }
    }

    private Opcode() {
    }

    private Opcode(final int opcode) {
      this.opcode = opcode;
    }

    public static Opcode get(int opcode) {
      return map.get(opcode);
    }
  };

  // public static final Opcode PYC_INVALID_OPCODE = (Opcode) (-1);
  // public static final Opcode PYC_HAVE_ARG = Opcode.STORE_NAME_A;


  public static String OpcodeName(Opcode opcode) {
    String[] opcode_names = new String[] {
    "STOP_CODE", "POP_TOP", "ROT_TWO", "ROT_THREE", "DUP_TOP", "DUP_TOP_TWO",
    "UNARY_POSITIVE", "UNARY_NEGATIVE", "UNARY_NOT", "UNARY_CONVERT",
    "UNARY_CALL", "UNARY_INVERT", "BINARY_POWER", "BINARY_MULTIPLY",
    "BINARY_DIVIDE", "BINARY_MODULO", "BINARY_ADD", "BINARY_SUBTRACT",
    "BINARY_SUBSCR", "BINARY_CALL", "SLICE_0", "SLICE_1", "SLICE_2", "SLICE_3",
    "STORE_SLICE_0", "STORE_SLICE_1", "STORE_SLICE_2", "STORE_SLICE_3",
    "DELETE_SLICE_0", "DELETE_SLICE_1", "DELETE_SLICE_2", "DELETE_SLICE_3",
    "STORE_SUBSCR", "DELETE_SUBSCR", "BINARY_LSHIFT", "BINARY_RSHIFT",
    "BINARY_AND", "BINARY_XOR", "BINARY_OR", "PRINT_EXPR", "PRINT_ITEM",
    "PRINT_NEWLINE", "BREAK_LOOP", "RAISE_EXCEPTION", "LOAD_LOCALS",
    "RETURN_VALUE", "LOAD_GLOBALS", "EXEC_STMT", "BUILD_FUNCTION", "POP_BLOCK",
    "END_FINALLY", "BUILD_CLASS", "ROT_FOUR", "NOP", "LIST_APPEND",
    "BINARY_FLOOR_DIVIDE", "BINARY_TRUE_DIVIDE", "INPLACE_FLOOR_DIVIDE",
    "INPLACE_TRUE_DIVIDE", "STORE_MAP", "INPLACE_ADD", "INPLACE_SUBTRACT",
    "INPLACE_MULTIPLY", "INPLACE_DIVIDE", "INPLACE_MODULO", "INPLACE_POWER",
    "GET_ITER", "PRINT_ITEM_TO", "PRINT_NEWLINE_TO", "INPLACE_LSHIFT",
    "INPLACE_RSHIFT", "INPLACE_AND", "INPLACE_XOR", "INPLACE_OR",
    "WITH_CLEANUP", "IMPORT_STAR", "YIELD_VALUE", "LOAD_BUILD_CLASS",
    "STORE_LOCALS", "POP_EXCEPT", "SET_ADD", "YIELD_FROM",

    "STORE_NAME", "DELETE_NAME", "UNPACK_TUPLE", "UNPACK_LIST", "UNPACK_ARG",
    "STORE_ATTR", "DELETE_ATTR", "STORE_GLOBAL", "DELETE_GLOBAL",
    "UNPACK_VARARG", "LOAD_CONST", "LOAD_NAME", "BUILD_TUPLE", "BUILD_LIST",
    "BUILD_MAP", "LOAD_ATTR", "COMPARE_OP", "IMPORT_NAME", "IMPORT_FROM",
    "JUMP_FORWARD", "JUMP_IF_FALSE", "JUMP_IF_TRUE", "JUMP_ABSOLUTE",
    "FOR_LOOP", "LOAD_LOCAL", "LOAD_GLOBAL", "SET_FUNC_ARGS", "SETUP_LOOP",
    "SETUP_EXCEPT", "SETUP_FINALLY", "RESERVE_FAST", "LOAD_FAST",
    "STORE_FAST", "DELETE_FAST", "SET_LINENO", "RAISE_VARARGS",
    "CALL_FUNCTION", "MAKE_FUNCTION", "BUILD_SLICE", "CALL_FUNCTION_VAR",
    "CALL_FUNCTION_KW", "CALL_FUNCTION_VAR_KW", "UNPACK_SEQUENCE", "FOR_ITER",
    "DUP_TOPX", "BUILD_SET", "JUMP_IF_FALSE_OR_POP", "JUMP_IF_TRUE_OR_POP",
    "POP_JUMP_IF_FALSE", "POP_JUMP_IF_TRUE", "CONTINUE_LOOP", "MAKE_CLOSURE",
    "LOAD_CLOSURE", "LOAD_DEREF", "STORE_DEREF", "DELETE_DEREF",
    "EXTENDED_ARG", "SETUP_WITH", "SET_ADD", "MAP_ADD", "UNPACK_EX",
    "LIST_APPEND"
    };

    if (opcode.ordinal() < 0)
      return "<INVALID>";

    if (opcode.ordinal() < Opcode.PYC_LAST_OPCODE.ordinal())
      return opcode_names[opcode.ordinal()];

    // char[] badcode = new char[10];
    // snprintf(badcode, 10, "<%d>", opcode);
    return opcode.name();
  };

  public static Opcode ByteToOpcode(int maj, int min, int opcode) {
    switch (maj) {
    // case 1:
    // switch (min) {
    // case 0: return python_10_map(opcode);
    // case 1: return python_11_map(opcode);
    // case 3: return python_13_map(opcode);
    // case 4: return python_14_map(opcode);
    // case 5: return python_15_map(opcode);
    // case 6: return python_16_map(opcode);
    // }
    // break;
    case 2:
        switch (min) {
      // case 0: return python_20_map(opcode);
      // case 1: return python_21_map(opcode);
      // case 2: return python_22_map(opcode);
      // case 3: return python_23_map(opcode);
      // case 4: return python_24_map(opcode);
      // case 5: return python_25_map(opcode);
      // case 6: return python_26_map(opcode);
        case 7: return python_27.python_27_map(opcode);
        }
        break;
//    case 3:
//        switch (min) {
//        case 0: return python_30_map(opcode);
//        case 1: return python_31_map(opcode);
//        case 2: return python_32_map(opcode);
//        case 3: return python_33_map(opcode);
//        }
//        break;
    }
    return Opcode.PYC_INVALID_OPCODE;
  }

  public static boolean IsConstArg(Opcode opcode) {
    return (opcode == Opcode.LOAD_CONST_A) || (opcode == Opcode.RESERVE_FAST_A);
  }

  public static boolean IsNameArg(Opcode opcode) {
    return (opcode == Opcode.DELETE_ATTR_A) || (opcode == Opcode.DELETE_GLOBAL_A) ||
           (opcode == Opcode.DELETE_NAME_A) || (opcode == Opcode.IMPORT_FROM_A) ||
           (opcode == Opcode.IMPORT_NAME_A) || (opcode == Opcode.LOAD_ATTR_A) ||
           (opcode == Opcode.LOAD_GLOBAL_A) || (opcode == Opcode.LOAD_LOCAL_A) ||
           (opcode == Opcode.LOAD_NAME_A)   || (opcode == Opcode.STORE_ATTR_A) ||
           (opcode == Opcode.STORE_GLOBAL_A) || (opcode == Opcode.STORE_NAME_A);
  }

  public static boolean IsVarNameArg(Opcode opcode) {
    return (opcode == Opcode.DELETE_FAST_A) || (opcode == Opcode.LOAD_FAST_A) ||
           (opcode == Opcode.STORE_FAST_A);
  }

  public static boolean IsCellArg(Opcode opcode) {
    return (opcode == Opcode.LOAD_CLOSURE_A) || (opcode == Opcode.LOAD_DEREF_A) ||
           (opcode == Opcode.STORE_DEREF_A);
  }

  public static boolean IsJumpOffsetArg(Opcode opcode) {
    return (opcode == Opcode.JUMP_FORWARD_A) || (opcode == Opcode.JUMP_IF_FALSE_A) ||
           (opcode == Opcode.JUMP_IF_TRUE_A) || (opcode == Opcode.SETUP_LOOP_A) ||
           (opcode == Opcode.SETUP_FINALLY_A) || (opcode == Opcode.SETUP_EXCEPT_A) ||
           (opcode == Opcode.FOR_LOOP_A);
  }
}


public class bytecode {
  static PrintStream pyc_output = PycData.pyc_output;

  static void print_const(PycObject obj, PycModule mod) {
    switch (obj.type()) {
    case PycObject.Type.TYPE_STRING:
    case PycObject.Type.TYPE_STRINGREF:
    case PycObject.Type.TYPE_INTERNED:
      PycString.OutputString((PycString) obj, (mod.majorVer() == 3) ? 'b' : 0);
      break;
    case PycObject.Type.TYPE_UNICODE:
      PycString.OutputString((PycString) obj, (mod.majorVer() == 3) ? 0 : 'u');
      break;
    case PycObject.Type.TYPE_TUPLE: {
      pyc_output.printf("(");
      List<PycObject> values = ((PycTuple) obj).values();
      Iterator<PycObject> it = values.iterator();
      if (it.hasNext()) {
        print_const(it.next(), mod);
        while (it.hasNext()) {
          pyc_output.printf(", ");
          print_const(it.next(), mod);
        }
      }
      if (values.size() == 1)
        pyc_output.printf(",)");
      else
        pyc_output.printf(")");
    }
      break;
    case PycObject.Type.TYPE_LIST: {
      pyc_output.printf("[");
      List<PycObject> values = ((PycList) obj).values();
      Iterator<PycObject> it = values.iterator();
      if (it.hasNext()) {
        print_const(it.next(), mod);
        while (it.hasNext()) {
          pyc_output.printf(", ");
          print_const(it.next(), mod);
        }
      }
      pyc_output.printf("]");
    }
      break;
    case PycObject.Type.TYPE_DICT: {
      pyc_output.printf("{");
      List<PycObject> keys = ((PycDict) obj).keys();
      List<PycObject> values = ((PycDict) obj).values();
      Iterator<PycObject> ki = keys.iterator();
      Iterator<PycObject> vi = values.iterator();
      if (ki.hasNext()) {
        print_const(ki.next(), mod);
        pyc_output.printf(": ");
        print_const(vi.next(), mod);
        while (ki.hasNext()) {
          pyc_output.printf(", ");
          print_const(ki.next(), mod);
          pyc_output.printf(": ");
          print_const(vi.next(), mod);
        }
      }
      pyc_output.printf("}");
    }
      break;
    case PycObject.Type.TYPE_SET: {
      pyc_output.printf("{");
      Set<PycObject> values = ((PycSet) obj).values();
      Iterator<PycObject> it = values.iterator();
      if (it.hasNext()) {
        print_const(it.next(), mod);
        while (it.hasNext()) {
          pyc_output.printf(", ");
          print_const(it.next(), mod);
        }
      }
      pyc_output.printf("}");
    }
      break;
    case PycObject.Type.TYPE_NONE:
      pyc_output.printf("None");
      break;
    case PycObject.Type.TYPE_TRUE:
      pyc_output.printf("True");
      break;
    case PycObject.Type.TYPE_FALSE:
      pyc_output.printf("False");
      break;
    case PycObject.Type.TYPE_INT:
      pyc_output.printf("%d", ((PycInt) obj).value());
      break;
    case PycObject.Type.TYPE_LONG:
      pyc_output.printf("%s", ((PycLong) obj).repr());
      break;
    case PycObject.Type.TYPE_FLOAT:
      pyc_output.printf("%s", ((PycFloat) obj).value());
      break;
    case PycObject.Type.TYPE_COMPLEX:
      pyc_output.printf("(%s+%sj)", ((PycComplex) obj).value(), ((PycComplex) obj).imag());
      break;
    case PycObject.Type.TYPE_BINARY_FLOAT:
      pyc_output.printf("%g", ((PycCFloat) obj).value());
      break;
    case PycObject.Type.TYPE_BINARY_COMPLEX:
      pyc_output.printf("(%g+%gj)", ((PycCComplex) obj).value(), ((PycCComplex) obj).imag());
      break;
    case PycObject.Type.TYPE_CODE:
    case PycObject.Type.TYPE_CODE2:
      pyc_output.printf("<CODE> %s", ((PycCode) obj).name().value());
      break;
    }
  }

  static void bc_print(PycCode code, PycModule mod, Opcode opcode, int pos, int operand) {
    pyc_output.printf("%-24s", Pyc.OpcodeName(opcode));

    if (opcode.ordinal() >= Opcode.PYC_HAVE_ARG.ordinal()) {
      String operand_ = Integer.toString(operand);

      if (Pyc.IsConstArg(opcode)) {
        pyc_output.printf("%d: ", operand);
        print_const(code.getConst(operand), mod);
      } else if (Pyc.IsNameArg(opcode)) {
        pyc_output.printf("%d: %s", operand, code.getName(operand).value());
      } else if (Pyc.IsVarNameArg(opcode)) {
        pyc_output.printf("%d: %s", operand, code.getVarName(operand).value());
      } else if (Pyc.IsCellArg(opcode)) {
        pyc_output.printf("%d: ", operand);
        print_const(code.getCellVar(operand), mod);
      } else if (Pyc.IsJumpOffsetArg(opcode)) {
        pyc_output.printf("%d (to %d)", operand, pos + operand);
      } else {
        pyc_output.printf(operand_);
      }
    }
    pyc_output.printf("\n");
  }


  static Args bc_next(PycBuffer source, PycCode code, PycModule mod, Opcode opcode, int operand, int pos, boolean is_disasm) throws IOException {
    if (is_disasm)
      pyc_output.printf("%-7d ", pos); // Current bytecode position

    opcode = Pyc.ByteToOpcode(mod.majorVer(), mod.minorVer(), source.getByte());
    operand = 0;
    boolean haveExtArg = false;
    pos += 1;

    if (opcode == Opcode.EXTENDED_ARG_A) {
      operand = source.get16() << 16;
      opcode = Opcode.get(source.getByte());
      haveExtArg = true;
      pos += 3;
    }

    if (opcode.ordinal() >= Opcode.PYC_HAVE_ARG.ordinal()) {
      // If we have an extended arg, we want to OR the lower part,
      // else we want the whole thing (in case it's negative). We use
      // the boolean so that values between 0x8000 and 0xFFFF can be stored
      // without becoming negative
      if (haveExtArg)
        operand |= (source.get16() & 0xFFFF);
      else
        operand = source.get16();
      pos += 2;
    }

    if (is_disasm)
      bc_print(code, mod, opcode, pos, operand);

    return new Args(source, code, mod, opcode, operand, pos);
  }

  static void bc_disasm(PycCode code, PycModule mod, int indent) throws IOException {
    PycBuffer source = new PycBuffer(code.code().value().getBytes(), code.code().length());

    Opcode opcode = null;
    int operand = 0;
    int pos = 0;
    while (!source.atEof()) {
      for (int i = 0; i < indent; i++)
        pyc_output.printf("    ");

      Args args = bc_next(source, code, mod, opcode, operand, pos, true);
      pos = args.pos;
    }
  }

}

class Args {
  PycBuffer source;
  PycCode code;
  PycModule mod;
  Opcode opcode;
  int operand;
  int pos;

  Args (PycBuffer source, PycCode code, PycModule mod, Opcode opcode, int operand, int pos) {
    this.source = source;
    this.code = code;
    this.mod = mod;
    this.opcode = opcode;
    this.operand = operand;
    this.pos = pos;
  }
}