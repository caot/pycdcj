package pydecompiler.dis;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import pydecompiler.util.FastStack;
import pydecompiler.util.Logger;
import pydecompiler.util.Pair;
import pydecompiler.util.Stack;


public class ASTree {
  /* Use this to determine if an error occurred (and therefore, if we should
   * avoid cleaning the output tree) */
  static boolean cleanBuild;

  /* Keep track of whether we're in a print statement, so we can make
   * chained prints (print x, y, z) prettier */
  static boolean inPrint;

  /* Use this to prevent printing return keywords and newlines in lambdas. */
  static boolean inLambda = false;

  /* Use this to keep track of whether we need to print out the list of global
   * variables that we are using (such as inside a function). */
  static boolean printGlobals = false;

  static boolean LOCK_DEBUG = false;
  static boolean STACK_DEBUG = false;
  static boolean BLOCK_DEBUG = false;

  static ASTNode BuildFromCode(PycCode code, PycModule mod) throws IOException {
    PycBuffer source = new PycBuffer(code.code().value().getBytes(), code.code().length());

    FastStack<ASTNode> stack = new FastStack<ASTNode>((mod.majorVer() == 1) ? 20 : code.stackSize());
    FastStack<FastStack<ASTNode>> stack_hist = new FastStack<FastStack<ASTNode>>();

    Stack<ASTBlock> blocks = new Stack<ASTBlock>();
    ASTBlock defblock = new ASTBlock(ASTBlock.BlkType.BLK_MAIN);
    defblock.init();
    ASTBlock curblock = defblock;
    blocks.push(defblock);

    Pyc.Opcode opcode = null;
    int operand = 0;
    int curpos = 0;
    int pos = 0;
    int unpack = 0;
    boolean else_pop = false;
    boolean need_try = false;
    boolean is_disasm = true;

    while (!source.atEof()) {
      if (BLOCK_DEBUG || STACK_DEBUG) {
        System.err.printf("%-7d", pos);
        if (STACK_DEBUG)
          System.err.printf("%-5d", (int) stack_hist.size() + 1);

        if (BLOCK_DEBUG) {
          for (int i = 0; i < blocks.size(); i++)
            System.err.printf("    ");
          System.err.printf("%s (%d)", curblock.type_str(), curblock.end());
        }
        System.err.printf("\n");
      }
      curpos = pos;
      Args args = bytecode.bc_next(source, code, mod, opcode, operand, pos, is_disasm, stack, stack_hist);

      // source = args.source;
      // code = args.code;
      // mod = args.mod;
      opcode = args.opcode;
      operand = args.operand;
      pos = args.pos;

      if (need_try && opcode != Pyc.Opcode.SETUP_EXCEPT_A) {
        need_try = false;

        /* Store the current stack for the except/finally statement(s) */
        stack_hist.push(stack);
        ASTBlock tryblock = new ASTBlock(ASTBlock.BlkType.BLK_TRY, curblock.end(), true);
        blocks.push(tryblock);
        curblock = blocks.top();
      } else if (else_pop
          && opcode != Pyc.Opcode.JUMP_FORWARD_A
          && opcode != Pyc.Opcode.JUMP_IF_FALSE_A
          && opcode != Pyc.Opcode.JUMP_IF_FALSE_OR_POP_A
          && opcode != Pyc.Opcode.POP_JUMP_IF_FALSE_A
          && opcode != Pyc.Opcode.JUMP_IF_TRUE_A
          && opcode != Pyc.Opcode.JUMP_IF_TRUE_OR_POP_A
          && opcode != Pyc.Opcode.POP_JUMP_IF_TRUE_A
          && opcode != Pyc.Opcode.POP_BLOCK) {
        else_pop = false;

        ASTBlock prev = curblock;
        while (prev.end() < pos 
            && prev.blktype() != ASTBlock.BlkType.BLK_MAIN) {
          if (prev.blktype() != ASTBlock.BlkType.BLK_CONTAINER) {
            if (prev.end() == 0) {
              break;
            }

            /*
             * We want to keep the stack the same, but we need to pop a level
             * off the history.
             */
            // stack = stack_hist.top();
            if (!stack_hist.empty())
              stack_hist.pop();
          }

          blocks.pop();

          if (blocks.empty())
            break;

          curblock = blocks.top();
          curblock.append((ASTNode) prev);

          prev = curblock;
        }
      }

      switch (opcode) {
      case BINARY_ADD: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode left = stack.top();
        stack.pop();
        stack.push(new ASTBinary(left, right, ASTBinary.BinOp.BIN_ADD));
      }
        break;
      case BINARY_AND: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode left = stack.top();
        stack.pop();
        stack.push(new ASTBinary(left, right, ASTBinary.BinOp.BIN_AND));
      }
        break;
      case BINARY_DIVIDE: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode left = stack.top();
        stack.pop();
        stack.push(new ASTBinary(left, right, ASTBinary.BinOp.BIN_DIVIDE));
      }
        break;
      case BINARY_FLOOR_DIVIDE: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode left = stack.top();
        stack.pop();
        stack.push(new ASTBinary(left, right, ASTBinary.BinOp.BIN_FLOOR));
      }
        break;
      case BINARY_LSHIFT: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode left = stack.top();
        stack.pop();
        stack.push(new ASTBinary(left, right, ASTBinary.BinOp.BIN_LSHIFT));
      }
        break;
      case BINARY_MODULO: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode left = stack.top();
        stack.pop();
        stack.push(new ASTBinary(left, right, ASTBinary.BinOp.BIN_MODULO));
      }
        break;
      case BINARY_MULTIPLY: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode left = stack.top();
        stack.pop();
        stack.push(new ASTBinary(left, right, ASTBinary.BinOp.BIN_MULTIPLY));
      }
        break;
      case BINARY_OR: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode left = stack.top();
        stack.pop();
        stack.push(new ASTBinary(left, right, ASTBinary.BinOp.BIN_OR));
      }
        break;
      case BINARY_POWER: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode left = stack.top();
        stack.pop();
        stack.push(new ASTBinary(left, right, ASTBinary.BinOp.BIN_POWER));
      }
        break;
      case BINARY_RSHIFT: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode left = stack.top();
        stack.pop();
        stack.push(new ASTBinary(left, right, ASTBinary.BinOp.BIN_RSHIFT));
      }
        break;
      case BINARY_SUBSCR: {
        ASTNode subscr = stack.top();
        stack.pop();
        ASTNode src = stack.top();
        stack.pop();
        stack.push(new ASTSubscr(src, subscr));
      }
        break;
      case BINARY_SUBTRACT: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode left = stack.top();
        stack.pop();
        stack.push(new ASTBinary(left, right, ASTBinary.BinOp.BIN_SUBTRACT));
      }
        break;
      case BINARY_TRUE_DIVIDE: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode left = stack.top();
        stack.pop();
        stack.push(new ASTBinary(left, right, ASTBinary.BinOp.BIN_DIVIDE));
      }
        break;
      case BINARY_XOR: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode left = stack.top();
        stack.pop();
        stack.push(new ASTBinary(left, right, ASTBinary.BinOp.BIN_XOR));
      }
        break;
      case BREAK_LOOP:
        curblock.append(new ASTKeyword(ASTKeyword.Word.KW_BREAK));
        break;
      case BUILD_CLASS: {
        ASTNode code_ = stack.top();
        stack.pop();
        ASTNode bases = stack.top();
        stack.pop();
        ASTNode name = stack.top();
        stack.pop();
        stack.push(new ASTClass(code_, bases, name));
      }
        break;
      case BUILD_FUNCTION: {
        ASTNode code_ = stack.top();
        stack.pop();
        stack.push(new ASTFunction(code_, new LinkedList<ASTNode>()));
      }
        break;
      case BUILD_LIST_A: {
        LinkedList<ASTNode> values = new LinkedList<ASTNode>();
        for (int i = 0; i < operand; i++) {
          values.addFirst(stack.top());
          stack.pop();
        }
        stack.push(new ASTList(values));
      }
        break;
      case BUILD_MAP_A:
        stack.push(new ASTMap());
        break;
      case STORE_MAP: {
        ASTNode key = stack.top();
        stack.pop();
        ASTNode value = stack.top();
        stack.pop();
        ASTMap map = (ASTMap) stack.top();
        map.add(key, value);
      }
        break;
      case BUILD_SLICE_A: {
        if (operand == 2) {
          ASTNode end = stack.top();
          stack.pop();
          ASTNode start = stack.top();
          stack.pop();

          if (start.type() == ASTNode.Type.NODE_OBJECT 
              && ((ASTObject) start).object() == PycObject.Pyc_None) {
            start = ASTNode.Node_NULL;
          }

          if (end.type() == ASTNode.Type.NODE_OBJECT 
              && ((ASTObject) end).object() == PycObject.Pyc_None) {
            end = ASTNode.Node_NULL;
          }

          if (start == ASTNode.Node_NULL && end == ASTNode.Node_NULL) {
            stack.push(new ASTSlice(ASTSlice.SliceOp.SLICE0));
          } else if (start == ASTNode.Node_NULL) {
            stack.push(new ASTSlice(ASTSlice.SliceOp.SLICE2, start, end));
          } else if (end == ASTNode.Node_NULL) {
            stack.push(new ASTSlice(ASTSlice.SliceOp.SLICE1, start, end));
          } else {
            stack.push(new ASTSlice(ASTSlice.SliceOp.SLICE3, start, end));
          }
        } else if (operand == 3) {
          ASTNode step = stack.top();
          stack.pop();
          ASTNode end = stack.top();
          stack.pop();
          ASTNode start = stack.top();
          stack.pop();

          if (start.type() == ASTNode.Type.NODE_OBJECT 
              && ((ASTObject) start).object() == PycObject.Pyc_None) {
            start = ASTNode.Node_NULL;
          }

          if (end.type() == ASTNode.Type.NODE_OBJECT 
              && ((ASTObject) end).object() == PycObject.Pyc_None) {
            end = ASTNode.Node_NULL;
          }

          if (step.type() == ASTNode.Type.NODE_OBJECT 
              && ((ASTObject) step).object() == PycObject.Pyc_None) {
            step = ASTNode.Node_NULL;
          }

          /* We have to do this as a slice where one side is another slice */
          /* [[a:b]:c] */

          if (start == ASTNode.Node_NULL && end == ASTNode.Node_NULL) {
            stack.push(new ASTSlice(ASTSlice.SliceOp.SLICE0));
          } else if (start == ASTNode.Node_NULL) {
            stack.push(new ASTSlice(ASTSlice.SliceOp.SLICE2, start, end));
          } else if (end == ASTNode.Node_NULL) {
            stack.push(new ASTSlice(ASTSlice.SliceOp.SLICE1, start, end));
          } else {
            stack.push(new ASTSlice(ASTSlice.SliceOp.SLICE3, start, end));
          }

          ASTNode lhs = stack.top();
          stack.pop();

          if (step == ASTNode.Node_NULL) {
            stack.push(new ASTSlice(ASTSlice.SliceOp.SLICE1, lhs, step));
          } else {
            stack.push(new ASTSlice(ASTSlice.SliceOp.SLICE3, lhs, step));
          }
        }
      }
        break;
      case BUILD_TUPLE_A: {
        LinkedList<ASTNode> values = new LinkedList<ASTNode>();
        for (int i = 0; i < operand; i++) {
          values.addFirst(stack.top());
          stack.pop();
        }
        stack.push(new ASTTuple(values));
      }
        break;
      case CALL_FUNCTION_A: {
        int kwparams = (operand & 0xFF00) >> 8;
        int pparams = (operand & 0xFF);
        LinkedList<Pair<ASTNode, ASTNode>> kwparamList = new LinkedList<Pair<ASTNode, ASTNode>>();
        LinkedList<ASTNode> pparamList = new LinkedList<ASTNode>();
        for (int i = 0; i < kwparams; i++) {
          ASTNode val = stack.top();
          stack.pop();
          ASTNode key = stack.top();
          stack.pop();
          kwparamList.addFirst(new Pair<ASTNode, ASTNode>(key, val));
        }
        for (int i = 0; i < pparams; i++) {
          ASTNode param = stack.top();
          stack.pop();
          if (param.type() == ASTNode.Type.NODE_FUNCTION) {
            ASTNode code_ = ((ASTFunction) param).code();
            PycCode code_src = (PycCode) ((ASTObject) code_).object();
            PycString function_name = code_src.name();
            if (function_name.isEqual("<lambda>")) {
              pparamList.addFirst(param);
            } else {
              // Decorator used
              ASTNode name = new ASTName(function_name);
              curblock.append(new ASTStore(param, name));

              pparamList.addFirst(name);
            }
          } else {
            pparamList.addFirst(param);
          }
        }
        ASTNode func = stack.top();
        stack.pop();
        stack.push(new ASTCall(func, pparamList, kwparamList));
      }
        break;
      case CALL_FUNCTION_VAR_A: {
        ASTNode var = stack.top();
        stack.pop();
        int kwparams = (operand & 0xFF00) >> 8;
        int pparams = (operand & 0xFF);
        LinkedList<Pair<ASTNode, ASTNode>> kwparamList = new LinkedList<Pair<ASTNode, ASTNode>>();
        LinkedList<ASTNode> pparamList = new LinkedList<ASTNode>();
        for (int i = 0; i < kwparams; i++) {
          ASTNode val = stack.top();
          stack.pop();
          ASTNode key = stack.top();
          stack.pop();
          kwparamList.addFirst(new Pair(key, val));
        }
        for (int i = 0; i < pparams; i++) {
          pparamList.addFirst(stack.top());
          stack.pop();
        }
        ASTNode func = stack.top();
        stack.pop();

        ASTNode call = new ASTCall(func, pparamList, kwparamList);
        ((ASTCall) call).setVar(var);
        stack.push(call);
      }
        break;
      case CALL_FUNCTION_KW_A:
      case CALL_FUNCTION_VAR_KW_A: {
        ASTNode kw = stack.top();
        stack.pop();

        ASTNode var = null;
        if (opcode == Pyc.Opcode.CALL_FUNCTION_VAR_KW_A) {
          var = stack.top();
          stack.pop();
        }
        int kwparams = (operand & 0xFF00) >> 8;
        int pparams = (operand & 0xFF);
        LinkedList<Pair<ASTNode, ASTNode>> kwparamList = new LinkedList<Pair<ASTNode, ASTNode>>();
        LinkedList<ASTNode> pparamList = new LinkedList<ASTNode>();
        for (int i = 0; i < kwparams; i++) {
          ASTNode val = stack.top();
          stack.pop();
          ASTNode key = stack.top();
          stack.pop();
          kwparamList.addFirst(new Pair<ASTNode, ASTNode>(key, val));
        }
        for (int i = 0; i < pparams; i++) {
          pparamList.addFirst(stack.top());
          stack.pop();
        }
        ASTNode func = stack.top();
        stack.pop();

        ASTNode call = new ASTCall(func, pparamList, kwparamList);
        ((ASTCall) call).setKW(kw);
        if (opcode == Pyc.Opcode.CALL_FUNCTION_VAR_KW_A)
          ((ASTCall) call).setVar(var);
        stack.push(call);
      }
        break;
      case CONTINUE_LOOP_A:
        curblock.append(new ASTKeyword(ASTKeyword.Word.KW_CONTINUE));
        break;
      case COMPARE_OP_A: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode left = stack.top();
        // stack.pop();
        // stack.push(left);
        stack.push(right);
        stack.push(new ASTCompare(left, right, operand));
      }
        break;
      case DELETE_ATTR_A: {
        ASTNode name = stack.top();
        stack.pop();
        curblock.append(new ASTDelete(new ASTBinary(name, new ASTName(code.getName(operand)), ASTBinary.BinOp.BIN_ATTR)));
      }
        break;
      case DELETE_GLOBAL_A:
        code.markGlobal(code.getName(operand));
        /* Fall through */
      case DELETE_NAME_A: {
        PycString varname = code.getName(operand);
        // c++ const char* ends with '\0', but java needs to check length
        if (varname.value().length() > 1 && varname.value().charAt(0) == '_' && varname.value().charAt(1) == '[') {
          /* Don't show deletes that are a result of list comps. */
          break;
        }

        ASTNode name = new ASTName(varname);
        curblock.append(new ASTDelete(name));
      }
        break;
      case DELETE_FAST_A: {
        ASTNode name;

        if (mod.verCompare(1, 3) < 0)
          name = new ASTName(code.getName(operand));
        else
          name = new ASTName(code.getVarName(operand));

        if (((ASTName) name).name().value().charAt(0) == '_' 
            && ((ASTName) name).name().value().charAt(1) == '[') {
          /* Don't show deletes that are a result of list comps. */
          break;
        }

        curblock.append(new ASTDelete(name));
      }
        break;
      case DELETE_SLICE_0: {
        ASTNode name = stack.top();
        stack.pop();

        curblock.append(new ASTDelete(new ASTSubscr(name, new ASTSlice(ASTSlice.SliceOp.SLICE0))));
      }
        break;
      case DELETE_SLICE_1: {
        ASTNode upper = stack.top();
        stack.pop();
        ASTNode name = stack.top();
        stack.pop();

        curblock.append(new ASTDelete(new ASTSubscr(name, new ASTSlice(ASTSlice.SliceOp.SLICE1, upper))));
      }
        break;
      case DELETE_SLICE_2: {
        ASTNode lower = stack.top();
        stack.pop();
        ASTNode name = stack.top();
        stack.pop();

        curblock.append(new ASTDelete(new ASTSubscr(name, new ASTSlice(ASTSlice.SliceOp.SLICE2, ASTNode.Node_NULL, lower))));
      }
        break;
      case DELETE_SLICE_3: {
        ASTNode lower = stack.top();
        stack.pop();
        ASTNode upper = stack.top();
        stack.pop();
        ASTNode name = stack.top();
        stack.pop();

        curblock.append(new ASTDelete(new ASTSubscr(name, new ASTSlice(ASTSlice.SliceOp.SLICE3, upper, lower))));
      }
        break;
      case DELETE_SUBSCR: {
        ASTNode key = stack.top();
        stack.pop();
        ASTNode name = stack.top();
        stack.pop();

        curblock.append(new ASTDelete(new ASTSubscr(name, key)));
      }
        break;
      case DUP_TOP:
        stack.push(stack.top());
        break;
      case DUP_TOP_TWO: {
        ASTNode first = stack.top();
        stack.pop();
        ASTNode second = stack.top();

        stack.push(first);
        stack.push(second);
        stack.push(first);
      }
        break;
      case DUP_TOPX_A: {
        Stack<ASTNode> first = new Stack<ASTNode>();
        Stack<ASTNode> second = new Stack<ASTNode>();

        for (int i = 0; i < operand; i++) {
          ASTNode node = stack.top();
          stack.pop();
          first.push(node);
          second.push(node);
        }

        while (!first.isEmpty()) {
          stack.push(first.top());
          first.pop();
        }

        while (!second.isEmpty()) {
          stack.push(second.top());
          second.pop();
        }
      }
        break;
      case END_FINALLY: {
        boolean isFinally = false;
        if (curblock.blktype() == ASTBlock.BlkType.BLK_FINALLY) {
          ASTBlock final_ = curblock;
          blocks.pop();

          stack = stack_hist.top();
          stack_hist.pop();

          curblock = blocks.top();
          curblock.append((ASTNode) final_);
          isFinally = true;
        } else if (curblock.blktype() == ASTBlock.BlkType.BLK_EXCEPT) {
          /* Turn it into an else statement. */
          blocks.pop();
          ASTBlock prev = curblock;
          if (curblock.size() != 0) {
            blocks.top().append((ASTNode) curblock);
          }

          if (!blocks.empty())
            curblock = blocks.top();

          if (curblock.end() != pos || ((ASTContainerBlock) curblock).hasFinally()) {
            ASTBlock elseblk = new ASTBlock(ASTBlock.BlkType.BLK_ELSE, prev.end());
            elseblk.init();
            blocks.push(elseblk);
            curblock = blocks.top();
          } else {
            stack = stack_hist.top();
            stack_hist.pop();
          }
        }

        if (curblock.blktype() == ASTBlock.BlkType.BLK_CONTAINER) {
          /* This marks the end of the except block(s). */
          ASTContainerBlock cont = (ASTContainerBlock) curblock;
          if (!cont.hasFinally() || isFinally) {
            /* If there's no finally block, pop the container. */
            blocks.pop();
            curblock = blocks.top();
            curblock.append((ASTNode) cont);
          }
        }
      }
        break;
      case EXEC_STMT: {
        ASTNode loc = stack.top();
        stack.pop();
        ASTNode glob = stack.top();
        stack.pop();
        ASTNode stmt = stack.top();
        stack.pop();

        curblock.append(new ASTExec(stmt, glob, loc));
      }
        break;
      case FOR_ITER_A: {
        ASTNode iter = stack.top(); // Iterable
        stack.pop();
        /* Pop it? Don't pop it? */

        boolean comprehension = false;
        ASTBlock top = blocks.top();
        if (top.blktype() == ASTBlock.BlkType.BLK_WHILE) {
          blocks.pop();
        } else {
          comprehension = true;
        }
        ASTIterBlock forblk = new ASTIterBlock(ASTBlock.BlkType.BLK_FOR, top.end(), iter);
        forblk.setComprehension(comprehension);
        blocks.push(forblk);
        curblock = blocks.top();

        stack.push(ASTNode.Node_NULL);
      }
        break;
      case FOR_LOOP_A: {
        ASTNode curidx = stack.top(); // Current index
        stack.pop();
        ASTNode iter = stack.top(); // Iterable
        stack.pop();

        boolean comprehension = false;
        ASTBlock top = blocks.top();
        if (top.blktype() == ASTBlock.BlkType.BLK_WHILE) {
          blocks.pop();
        } else {
          comprehension = true;
        }
        ASTIterBlock forblk = new ASTIterBlock(ASTBlock.BlkType.BLK_FOR, top.end(), iter);
        forblk.setComprehension(comprehension);
        blocks.push(forblk);
        curblock = blocks.top();

        /*
         * Python Docs say: "push the sequence, the incremented counter, and the
         * current item onto the stack."
         */
        stack.push(iter);
        stack.push(curidx);
        stack.push(ASTNode.Node_NULL); // We can totally hack this >_>
      }
        break;
      case GET_ITER:
        /* We just entirely ignore this */
        break;
      case IMPORT_NAME_A:
        if (mod.majorVer() == 1) {
          stack.push(new ASTImport(new ASTName(code.getName(operand)), ASTNode.Node_NULL));
        } else {
          ASTNode fromlist = stack.top();
          stack.pop();
          if (mod.verCompare(2, 5) >= 0)
            stack.pop(); // Level -- we don't care
          stack.push(new ASTImport(new ASTName(code.getName(operand)), fromlist));
        }
        break;
      case IMPORT_FROM_A:
        stack.push(new ASTName(code.getName(operand)));
        break;
      case IMPORT_STAR: {
        ASTNode import_ = stack.top();
        stack.pop();
        curblock.append(new ASTStore(import_, ASTNode.Node_NULL));
      }
        break;
      case INPLACE_ADD: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode src = stack.top();
        stack.pop();
        stack.push(new ASTBinary(src, right, ASTBinary.BinOp.BIN_IP_ADD));
      }
        break;
      case INPLACE_AND: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode left = stack.top();
        stack.pop();
        stack.push(new ASTBinary(left, right, ASTBinary.BinOp.BIN_IP_AND));
      }
        break;
      case INPLACE_DIVIDE: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode src = stack.top();
        stack.pop();
        stack.push(new ASTBinary(src, right, ASTBinary.BinOp.BIN_IP_DIVIDE));
      }
        break;
      case INPLACE_FLOOR_DIVIDE: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode left = stack.top();
        stack.pop();
        stack.push(new ASTBinary(left, right, ASTBinary.BinOp.BIN_IP_FLOOR));
      }
        break;
      case INPLACE_LSHIFT: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode left = stack.top();
        stack.pop();
        stack.push(new ASTBinary(left, right, ASTBinary.BinOp.BIN_IP_LSHIFT));
      }
        break;
      case INPLACE_MODULO: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode left = stack.top();
        stack.pop();
        stack.push(new ASTBinary(left, right, ASTBinary.BinOp.BIN_IP_MODULO));
      }
        break;
      case INPLACE_MULTIPLY: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode src = stack.top();
        stack.pop();
        stack.push(new ASTBinary(src, right, ASTBinary.BinOp.BIN_IP_MULTIPLY));
      }
        break;
      case INPLACE_OR: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode left = stack.top();
        stack.pop();
        stack.push(new ASTBinary(left, right, ASTBinary.BinOp.BIN_IP_OR));
      }
        break;
      case INPLACE_POWER: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode left = stack.top();
        stack.pop();
        stack.push(new ASTBinary(left, right, ASTBinary.BinOp.BIN_IP_POWER));
      }
        break;
      case INPLACE_RSHIFT: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode left = stack.top();
        stack.pop();
        stack.push(new ASTBinary(left, right, ASTBinary.BinOp.BIN_IP_RSHIFT));
      }
        break;
      case INPLACE_SUBTRACT: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode src = stack.top();
        stack.pop();
        stack.push(new ASTBinary(src, right, ASTBinary.BinOp.BIN_IP_SUBTRACT));
      }
        break;
      case INPLACE_TRUE_DIVIDE: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode left = stack.top();
        stack.pop();
        stack.push(new ASTBinary(left, right, ASTBinary.BinOp.BIN_IP_DIVIDE));
      }
        break;
      case INPLACE_XOR: {
        ASTNode right = stack.top();
        stack.pop();
        ASTNode left = stack.top();
        stack.pop();
        stack.push(new ASTBinary(left, right, ASTBinary.BinOp.BIN_IP_XOR));
      }
        break;
      case JUMP_IF_FALSE_A:
      case JUMP_IF_TRUE_A:
      case JUMP_IF_FALSE_OR_POP_A:
      case JUMP_IF_TRUE_OR_POP_A:
      case POP_JUMP_IF_FALSE_A:
      case POP_JUMP_IF_TRUE_A: {
        ASTNode cond = stack.top();
        ASTCondBlock ifblk;
        ASTCondBlock.InitCond popped = ASTCondBlock.InitCond.UNINITED;

        if (opcode == Pyc.Opcode.POP_JUMP_IF_FALSE_A 
            || opcode == Pyc.Opcode.POP_JUMP_IF_TRUE_A) {
          /* Pop condition before the jump */
          stack.pop();
          popped = ASTCondBlock.InitCond.PRE_POPPED;
        }

        /* Store the current stack for the else statement(s) */
        stack_hist.push(stack);

        if (opcode == Pyc.Opcode.JUMP_IF_FALSE_OR_POP_A 
            || opcode == Pyc.Opcode.JUMP_IF_TRUE_OR_POP_A) {
          /* Pop condition only if condition is met */
          stack.pop();
          popped = ASTCondBlock.InitCond.POPPED;
        }

        /* "Jump if true" means "Jump if not false" */
        boolean neg = opcode == Pyc.Opcode.JUMP_IF_TRUE_A 
            || opcode == Pyc.Opcode.JUMP_IF_TRUE_OR_POP_A 
            || opcode == Pyc.Opcode.POP_JUMP_IF_TRUE_A;

        int offs = operand;
        if (opcode == Pyc.Opcode.JUMP_IF_FALSE_A 
            || opcode == Pyc.Opcode.JUMP_IF_TRUE_A) {
          /* Offset is relative in these cases */
          offs = pos + operand;
        }

        if (cond.type() == ASTNode.Type.NODE_COMPARE 
            && ((ASTCompare) cond).op() == ASTCompare.CompareOp.CMP_EXCEPTION) {
          if (curblock.blktype() == ASTBlock.BlkType.BLK_EXCEPT 
              && ((ASTCondBlock) curblock).cond() == ASTNode.Node_NULL) {
            blocks.pop();
            curblock = blocks.top();

            stack_hist.pop();
          }
          ifblk = new ASTCondBlock(ASTBlock.BlkType.BLK_EXCEPT, offs, ((ASTCompare) cond).right(), false);
        } else if (curblock.blktype() == ASTBlock.BlkType.BLK_ELSE 
            && curblock.size() == 0) {
          /* Collapse into elif statement */
          blocks.pop();
          stack = stack_hist.top();
          stack_hist.pop();
          ifblk = new ASTCondBlock(ASTBlock.BlkType.BLK_ELIF, offs, cond, neg);
        } else if (curblock.size() == 0 && curblock.inited() 
            && curblock.blktype() == ASTBlock.BlkType.BLK_WHILE) {
          /* The condition for a while loop */
          ASTBlock top = blocks.top();
          blocks.pop();
          ifblk = new ASTCondBlock(top.blktype(), offs, cond, neg);

          /* We don't store the stack for loops! Pop it! */
          stack_hist.pop();
        } else if (curblock.size() == 0 && curblock.end() <= offs 
            && ( curblock.blktype() == ASTBlock.BlkType.BLK_IF 
              || curblock.blktype() == ASTBlock.BlkType.BLK_ELIF 
              || curblock.blktype() == ASTBlock.BlkType.BLK_WHILE)) {
          ASTNode newcond;
          ASTCondBlock top = (ASTCondBlock) curblock;
          ASTNode cond1 = top.cond();
          blocks.pop();

          if (curblock.blktype() == ASTBlock.BlkType.BLK_WHILE) {
            stack_hist.pop();
          } else {
            FastStack s_top = stack_hist.top();
            stack_hist.pop();
            stack_hist.pop();
            stack_hist.push(s_top);
          }

          if ((curblock.end() == offs || curblock.end() == curpos) 
              && !top.negative()) {
            /* if blah and blah */
            newcond = new ASTBinary(cond1, cond, ASTBinary.BinOp.BIN_LOG_AND);
          } else {
            /* if blah or blah */
            newcond = new ASTBinary(cond1, cond, ASTBinary.BinOp.BIN_LOG_OR);
          }
          ifblk = new ASTCondBlock(top.blktype(), offs, newcond, neg);
        } else if (curblock.blktype() == ASTBlock.BlkType.BLK_FOR && curblock.size() == 1) {
          ifblk = new ASTCondBlock(ASTBlock.BlkType.BLK_ELIF, offs, cond, neg);
        } else {
          /* Plain old if statement */
          ifblk = new ASTCondBlock(ASTBlock.BlkType.BLK_IF, offs, cond, neg);
        }

        if (popped != ASTCondBlock.InitCond.UNINITED)
          ifblk.init(popped);

        stack.push(ifblk);
        blocks.push(ifblk);
        curblock = blocks.top();
      }
        break;
      case JUMP_ABSOLUTE_A: {
        if (operand < pos) {
          if (curblock.blktype() == ASTBlock.BlkType.BLK_FOR 
              && ((ASTIterBlock) curblock).isComprehension()) {
            ASTNode top = stack.top();

            if (top.type() == ASTNode.Type.NODE_COMPREHENSION) {
              ASTComprehension comp = (ASTComprehension) top;

              comp.addGenerator((ASTIterBlock) curblock);
            }

            blocks.pop();
            curblock = blocks.top();
          }

          if (curblock.blktype() == ASTBlock.BlkType.BLK_CONTAINER ||
              curblock.blktype() == ASTBlock.BlkType.BLK_EXCEPT) {
            ; // pass
          } else {
            if (curblock.blktype() == ASTBlock.BlkType.BLK_ELSE) {
              stack = stack_hist.top();
              stack_hist.pop();

              blocks.pop();
              blocks.top().append((ASTNode) curblock);
              curblock = blocks.top();

              if (curblock.blktype() == ASTBlock.BlkType.BLK_CONTAINER 
                  && !((ASTContainerBlock) curblock).hasFinally()) {
                blocks.pop();
                blocks.top().append((ASTNode) curblock);
                curblock = blocks.top();
              }
            } else {
              curblock.append(new ASTKeyword(ASTKeyword.Word.KW_CONTINUE));
            }

            /* We're in a loop, this jumps back to the start */
            /* I think we'll just ignore this case... */
            break; // Bad idea? Probably!
          }
        }

        if (curblock.blktype() == ASTBlock.BlkType.BLK_CONTAINER) {
          ASTContainerBlock cont = (ASTContainerBlock) curblock;
          if (cont.hasExcept()) {
            stack_hist.push(stack);
            curblock.setEnd(pos + operand);
            ASTBlock except = new ASTCondBlock(ASTBlock.BlkType.BLK_EXCEPT, pos + operand, ASTNode.Node_NULL, false);
            except.init();
            blocks.push(except);
            curblock = blocks.top();
          }
          break;
        }

        if (!stack_hist.empty()) {
          stack = stack_hist.top();
          stack_hist.pop();
        }

        ASTBlock prev = curblock;
        ASTBlock nil = null;
        boolean push = true;

        do {
          if (blocks.empty())
            prev = nil;
          blocks.pop();

          if (blocks.empty())
            prev = nil;
          blocks.top().append((ASTNode) prev);

          if (prev.blktype() == ASTBlock.BlkType.BLK_IF 
              || prev.blktype() == ASTBlock.BlkType.BLK_ELIF) {
            if (push) {
              stack_hist.push(stack);
            }
            ASTBlock next = new ASTBlock(ASTBlock.BlkType.BLK_ELSE, blocks.top().end());
            if (prev.m_inited == ASTCondBlock.InitCond.PRE_POPPED) {
              next.init(ASTCondBlock.InitCond.PRE_POPPED);
            }

            blocks.push(next);
            prev = nil;
          } else if (prev.blktype() == ASTBlock.BlkType.BLK_EXCEPT) {
            if (push) {
              stack_hist.push(stack);
            }
            ASTBlock next = new ASTCondBlock(ASTBlock.BlkType.BLK_EXCEPT, blocks.top().end(), ASTNode.Node_NULL, false);
            next.init();

            blocks.push(next);
            prev = nil;
          } else if (prev.blktype() == ASTBlock.BlkType.BLK_ELSE) {
            /* Special case */
            prev = blocks.top();
            if (!push) {
              stack = stack_hist.top();
              stack_hist.pop();
            }
            push = false;
          } else {
            prev = nil;
          }

        } while (prev != nil);

        if (blocks.empty())
          break;
        curblock = blocks.top();

        if (curblock.blktype() == ASTBlock.BlkType.BLK_EXCEPT) {
          curblock.setEnd(pos + operand);
        }
      }
        break;
      case JUMP_FORWARD_A: {
        if (curblock.blktype() == ASTBlock.BlkType.BLK_CONTAINER) {
          ASTContainerBlock cont = (ASTContainerBlock) curblock;
          if (cont.hasExcept()) {
            stack_hist.push(stack);

            curblock.setEnd(pos + operand);
            ASTBlock except = new ASTCondBlock(ASTBlock.BlkType.BLK_EXCEPT, pos + operand, ASTNode.Node_NULL, false);
            except.init();
            blocks.push(except);
            curblock = blocks.top();
          }
          break;
        }

        if ((curblock.blktype() == ASTBlock.BlkType.BLK_WHILE 
            && !curblock.inited()) 
            || (curblock.blktype() == ASTBlock.BlkType.BLK_IF 
            && curblock.size() == 0)) {
          PycObject fakeint = new PycInt(1);
          ASTNode truthy = new ASTObject(fakeint);

          stack.push(truthy);
          break;
        }

        if (!stack_hist.empty()) {
          stack = stack_hist.top();
          stack_hist.pop();
        }

        ASTBlock prev = curblock;
        ASTBlock nil = null;
        boolean push = true;

        do {
          if (!blocks.empty())
            blocks.pop();

          if (!blocks.empty())
            blocks.top().append((ASTNode) prev);

          if (prev.blktype() == ASTBlock.BlkType.BLK_IF 
              || prev.blktype() == ASTBlock.BlkType.BLK_ELIF) {
            if (operand == 0) {
              prev = nil;
              continue;
            }

            if (push) {
              stack_hist.push(stack);
            }
            ASTBlock next = new ASTBlock(ASTBlock.BlkType.BLK_ELSE, pos + operand);
            if (prev.m_inited == ASTCondBlock.InitCond.PRE_POPPED) {
              next.init(ASTCondBlock.InitCond.PRE_POPPED);
            }

            blocks.push(next);
            prev = nil;
          } else if (prev.blktype() == ASTBlock.BlkType.BLK_EXCEPT) {
            if (operand == 0) {
              prev = nil;
              continue;
            }

            if (push) {
              stack_hist.push(stack);
            }
            ASTBlock next = new ASTCondBlock(ASTBlock.BlkType.BLK_EXCEPT, pos + operand, ASTNode.Node_NULL, false);
            next.init();

            blocks.push(next);
            prev = nil;
          } else if (prev.blktype() == ASTBlock.BlkType.BLK_ELSE) {
            /* Special case */
            prev = blocks.top();
            if (!push) {
              stack = stack_hist.top();
              stack_hist.pop();
            }
            push = false;

            if (prev.blktype() == ASTBlock.BlkType.BLK_MAIN) {
              /* Something went out of control! */
              prev = nil;
            }
          } else if (prev.blktype() == ASTBlock.BlkType.BLK_TRY && 
                     prev.end() < pos + operand) {
            /* Need to add an except/finally block */
            stack = stack_hist.top();
            stack.pop();

            if (blocks.top().blktype() == ASTBlock.BlkType.BLK_CONTAINER) {
              ASTContainerBlock cont = (ASTContainerBlock) (blocks.top());
              if (cont.hasExcept()) {
                if (push) {
                  stack_hist.push(stack);
                }

                ASTBlock except = new ASTCondBlock(ASTBlock.BlkType.BLK_EXCEPT, pos + operand, ASTNode.Node_NULL, false);
                except.init();
                blocks.push(except);
              }
            } else {
              System.err.printf("Something TERRIBLE happened!!\n");
            }
            prev = nil;
          } else {
            prev = nil;
          }

        } while (prev != nil);

        if (!blocks.empty())
          curblock = blocks.top();

        if (curblock.blktype() == ASTBlock.BlkType.BLK_EXCEPT) {
          curblock.setEnd(pos + operand);
        }
      }
        break;
      case LIST_APPEND:
      case LIST_APPEND_A: {
        ASTNode value = stack.top();
        stack.pop();

        ASTNode list = stack.top();

        if (curblock.blktype() == ASTBlock.BlkType.BLK_FOR 
            && ((ASTIterBlock) curblock).isComprehension()) {
          stack.push(new ASTComprehension(value));
        } else {
          stack.push(new ASTSubscr(list, value)); /* Total hack */
        }
      }
        break;
      case LOAD_ATTR_A: {
        ASTNode name = stack.top();
        if (name.type() != ASTNode.Type.NODE_IMPORT) {
          stack.pop();
          stack.push(new ASTBinary(name, new ASTName(code.getName(operand)), ASTBinary.BinOp.BIN_ATTR));
        }
      }
        break;
      case LOAD_CLOSURE_A: {
        /* Ignore this */
        ASTObject t_ob = new ASTObject(code.getConst(operand));
        stack.push(t_ob);
      }
        break;
      case LOAD_CONST_A: {
        ASTObject t_ob = new ASTObject(code.getConst(operand));

        if (t_ob.object().type() == PycObject.Type.TYPE_TUPLE 
            && ((PycTuple) t_ob.object()).values().size() > 0) {
          LinkedList<ASTNode> values = new LinkedList<ASTNode>();
          // values.add(t_ob);
          stack.push(new ASTTuple(values));
        } else if (t_ob.object().type() == PycObject.Type.TYPE_NONE) {
          stack.push(ASTNode.Node_NULL);
        } else {
          stack.push(t_ob);
        }
      }
        break;
      case LOAD_DEREF_A:
        stack.push(new ASTName((PycString) (code.getCellVar(operand))));
        break;
      case LOAD_FAST_A:
        if (mod.verCompare(1, 3) < 0)
          stack.push(new ASTName(code.getName(operand)));
        else
          stack.push(new ASTName(code.getVarName(operand)));
        break;
      case LOAD_GLOBAL_A:
        stack.push(new ASTName(code.getName(operand)));
        break;
      case LOAD_LOCALS:
        stack.push(new ASTNode(ASTNode.Type.NODE_LOCALS));
        break;
      case LOAD_NAME_A: {
        stack.push(new ASTName(code.getName(operand)));

        ASTBlock.BlkType type = curblock.blktype();
        int end = curblock.end();

        if (type == ASTBlock.BlkType.BLK_IF && end == pos) {
          ASTNode newcond;
          ASTNode cond = stack.top();

          ASTCondBlock top = (ASTCondBlock) curblock;
          ASTNode cond1 = top.cond();

          if (!top.negative()) {
            newcond = new ASTBinary(cond1, cond, ASTBinary.BinOp.BIN_LOG_AND);
          } else {
            newcond = new ASTBinary(cond1, cond, ASTBinary.BinOp.BIN_LOG_OR);
          }

          curblock.append(newcond);

          ASTNode curblockfront = curblock.nodes().getFirst();
          stack.pop();
          stack.push(curblockfront);

          ASTBlock btop = blocks.top();
          btop.removeLast();
        }
        ;
      }
        break;
      case MAKE_CLOSURE_A:
      case MAKE_FUNCTION_A: {
        ASTNode _code = stack.top();
        stack.pop();
        LinkedList<ASTNode> defArgs = new LinkedList<ASTNode>();
        for (int i = 0; i < operand; i++) {
          defArgs.addFirst(stack.top());
          stack.pop();
        }
        stack.push(new ASTFunction(_code, defArgs));
      }
        break;
      case POP_BLOCK: {
        if (curblock.blktype() == ASTBlock.BlkType.BLK_CONTAINER ||
            curblock.blktype() == ASTBlock.BlkType.BLK_FINALLY) {
          /* These should only be popped by an END_FINALLY */
          break;
        }

        if (curblock.blktype() == ASTBlock.BlkType.BLK_WITH) {
          // This should only be popped by a WITH_CLEANUP
          break;
        }

        ASTBlock tmp;

        if (!curblock.nodes().isEmpty() && 
            curblock.nodes().getLast().type() == ASTNode.Type.NODE_KEYWORD) {
          curblock.removeLast();
        }

        if (curblock.blktype() == ASTBlock.BlkType.BLK_IF 
            || curblock.blktype() == ASTBlock.BlkType.BLK_ELIF 
            || curblock.blktype() == ASTBlock.BlkType.BLK_ELSE 
            || curblock.blktype() == ASTBlock.BlkType.BLK_TRY
            || curblock.blktype() == ASTBlock.BlkType.BLK_EXCEPT 
            || curblock.blktype() == ASTBlock.BlkType.BLK_FINALLY) {
          if (!stack_hist.empty()) {
            // if (stack.top() != null)
            stack = stack_hist.top();
            stack_hist.pop();
          }
        }

        tmp = curblock;
        blocks.pop();

        if (!blocks.empty())
          curblock = blocks.top();

        if (!(tmp.blktype() == ASTBlock.BlkType.BLK_ELSE 
            && tmp.nodes().isEmpty())) {
          curblock.append(tmp);
        }

        if (tmp.blktype() == ASTBlock.BlkType.BLK_FOR && tmp.end() >= pos) {
          stack_hist.push(stack);

          ASTBlock blkelse = new ASTBlock(ASTBlock.BlkType.BLK_ELSE, tmp.end());
          blocks.push(blkelse);
          curblock = blocks.top();
        }

        if (curblock.blktype() == ASTBlock.BlkType.BLK_TRY 
            && tmp.blktype() != ASTBlock.BlkType.BLK_FOR 
            && tmp.blktype() != ASTBlock.BlkType.BLK_WHILE) {
          stack = stack_hist.top();
          stack_hist.pop();

          tmp = curblock;
          blocks.pop();
          curblock = blocks.top();

          if (!(tmp.blktype() == ASTBlock.BlkType.BLK_ELSE 
              && tmp.nodes().isEmpty())) {
            curblock.append(tmp);
          }
        }

        if (curblock.blktype() == ASTBlock.BlkType.BLK_CONTAINER) {
          ASTContainerBlock cont = (ASTContainerBlock) curblock;

          if (tmp.blktype() == ASTBlock.BlkType.BLK_ELSE && !cont.hasFinally()) {

            /* Pop the container */
            blocks.pop();
            curblock = blocks.top();
            curblock.append(cont);

          } else if ((tmp.blktype() == ASTBlock.BlkType.BLK_ELSE && cont.hasFinally()) 
              || (tmp.blktype() == ASTBlock.BlkType.BLK_TRY && !cont.hasExcept())) {

            /* Add the finally block */
            stack_hist.push(stack);

            ASTBlock final_ = new ASTBlock(ASTBlock.BlkType.BLK_FINALLY, 0, true);
            blocks.push(final_);
            curblock = blocks.top();
          }
        }

        if (curblock.blktype() == ASTBlock.BlkType.BLK_FOR 
            && curblock.end() == pos) {
          blocks.pop();
          blocks.top().append((ASTNode) curblock);
          curblock = blocks.top();
        }
      }
        break;
      case POP_EXCEPT:
        /* Do nothing. */
        break;
      case POP_TOP: {
        ASTNode value = stack.top();
        if (stack.isEmpty())
          break;
        stack.pop();
        if (!curblock.inited()) {
          if (curblock.blktype() == ASTBlock.BlkType.BLK_WITH) {
            ((ASTWithBlock) curblock).setExpr(value);
          } else {
            ((ASTCondBlock) curblock).init();
          }
          break;
        } else if (curblock.blktype() == ASTBlock.BlkType.BLK_CONTAINER ||
                   curblock.blktype() == ASTBlock.BlkType.BLK_EXCEPT
            ) {
          break;
        } else if (value.type() == ASTNode.Type.NODE_INVALID
                || value.type() == ASTNode.Type.NODE_BINARY
                || value.type() == ASTNode.Type.NODE_NAME
                || value.type() == ASTNode.Type.NODE_COMPARE
                    && ((ASTCompare)value).op() == ASTCompare.CompareOp.CMP_EXCEPTION
            ) {
          break;
        }

        if (value.type() != ASTNode.Type.NODE_OBJECT)
          curblock.append(value);

        if (curblock.blktype() == ASTBlock.BlkType.BLK_FOR 
            && ((ASTIterBlock) curblock).isComprehension()) {
          /* This relies on some really uncertain logic... 
           * If it's a comprehension, the only POP_TOP should be 
           * a call to append the iter to the list.
           */
          if (value.type() == ASTNode.Type.NODE_CALL) {
            ASTNode res = ((ASTCall) value).pparams().getFirst();

            stack.push(new ASTComprehension(res));
          }
        }
      }
        break;
      case PRINT_ITEM:
        curblock.append(new ASTPrint(stack.top()));
        stack.pop();
        break;
      case PRINT_ITEM_TO: {
        ASTNode stream = stack.top();
        stack.pop();

        curblock.append(new ASTPrint(stack.top(), stream));
        stack.pop();
        break;
      }
      case PRINT_NEWLINE:
        curblock.append(new ASTPrint(ASTNode.Node_NULL));
        break;
      case PRINT_NEWLINE_TO:
        curblock.append(new ASTPrint(ASTNode.Node_NULL, stack.top()));
        stack.pop();
        break;
      case RAISE_VARARGS_A: {
        LinkedList<ASTNode> paramList = new LinkedList<ASTNode>();
        for (int i = 0; i < operand; i++) {
          paramList.addFirst(stack.top());
          stack.pop();
        }
        stack.push(new ASTRaise(paramList));
        curblock.append(stack.peek());
//        curblock.append(new ASTRaise(paramList));

        if ((curblock.blktype() == ASTBlock.BlkType.BLK_IF 
            || curblock.blktype() == ASTBlock.BlkType.BLK_ELSE) 
            && stack_hist.size() > 0 
            && (mod.verCompare(2, 6) >= 0)) {
          stack = stack_hist.top();
          stack_hist.pop();

          ASTBlock prev = curblock;
          blocks.pop();
          if (!blocks.empty()) {
            curblock = blocks.top();
            curblock.append((ASTNode) prev);
          }

          args = bytecode.bc_next(source, code, mod, opcode, operand, pos, is_disasm, stack, stack_hist);
          opcode = args.opcode;
          operand = args.operand;
          pos = args.pos;
        }
      }
        break;
      case RETURN_VALUE: {
        ASTNode value = stack.top();
        stack.pop();
        curblock.append(new ASTReturn(value));

        if ((curblock.blktype() == ASTBlock.BlkType.BLK_IF 
            || curblock.blktype() == ASTBlock.BlkType.BLK_ELSE) 
            && stack_hist.size() > 0 
            && (mod.verCompare(2, 6) >= 0)) {
          stack = stack_hist.top();
          stack_hist.pop();

          ASTBlock prev = curblock;
          blocks.pop();
          if (!blocks.empty()) {
          curblock = blocks.top();
          }
          curblock.append((ASTNode) prev);
        }
      }
        break;
      case ROT_TWO: {
        ASTNode one = stack.top();
        stack.pop();
        ASTNode two = stack.top();
        stack.pop();

        stack.push(one);
        stack.push(two);
      }
        break;
      case ROT_THREE: {
        ASTNode one = stack.top();
        stack.pop();
        ASTNode two = stack.top();
        stack.pop();
        ASTNode three = stack.top();
        stack.pop();
        stack.push(one);
        stack.push(three);
        stack.push(two);
      }
        break;
      case ROT_FOUR: {
        ASTNode one = stack.top();
        stack.pop();
        ASTNode two = stack.top();
        stack.pop();
        ASTNode three = stack.top();
        stack.pop();
        ASTNode four = stack.top();
        stack.pop();
        stack.push(one);
        stack.push(four);
        stack.push(three);
        stack.push(two);
      }
        break;
      case SET_LINENO_A:
        // Ignore
        break;
      case SETUP_WITH_A: {
        ASTBlock withblock = new ASTWithBlock(pos + operand);
        blocks.push(withblock);
        curblock = blocks.top();
      }
        break;
      case WITH_CLEANUP: {
        // Stack top should be a None. Ignore it.
        ASTNode none = stack.top();
        stack.pop();

        if (none != ASTNode.Node_NULL) {
          System.err.printf("Something TERRIBLE happened!\n");
          break;
        }

        if (curblock.blktype() == ASTBlock.BlkType.BLK_WITH 
            && curblock.end() == curpos) {
          ASTBlock with = curblock;
          blocks.pop();
          curblock = blocks.top();
          curblock.append((ASTNode) with);
        } else {
          System.err.printf("Something TERRIBLE happened! No matching with block found for WITH_CLEANUP at %d\n", curpos);
        }
      }
        break;
      case SETUP_EXCEPT_A: {
        if (curblock.blktype() == ASTBlock.BlkType.BLK_CONTAINER) {
          ((ASTContainerBlock) curblock).setExcept(pos + operand);
        } else {
          ASTBlock next = new ASTContainerBlock(0, pos + operand);
          // stack.push(next);
          blocks.push(next);
        }

        /* Store the current stack for the except/finally statement(s) */
        stack_hist.push(stack);
        ASTBlock tryblock = new ASTBlock(ASTBlock.BlkType.BLK_TRY, pos + operand, true);
        blocks.push(tryblock);
        curblock = blocks.top();

        need_try = false;
      }
        break;
      case SETUP_FINALLY_A: {
        ASTBlock next = new ASTContainerBlock(pos + operand);
        blocks.push(next);
        curblock = blocks.top();

        need_try = true;
      }
        break;
      case SETUP_LOOP_A: {
        ASTBlock next = new ASTCondBlock(ASTBlock.BlkType.BLK_WHILE, pos + operand, ASTNode.Node_NULL, false);
        blocks.push(next);
        // stack.push(next);
        curblock = blocks.top();
      }
        break;
      case SLICE_0: {
        ASTNode name = stack.top();
        stack.pop();

        ASTNode slice = new ASTSlice(ASTSlice.SliceOp.SLICE0);
        stack.push(new ASTSubscr(name, slice));
      }
        break;
      case SLICE_1: {
        ASTNode lower = stack.top();
        stack.pop();
        ASTNode name = stack.top();
        stack.pop();

        ASTNode slice = new ASTSlice(ASTSlice.SliceOp.SLICE1, lower);
        stack.push(new ASTSubscr(name, slice));
      }
        break;
      case SLICE_2: {
        ASTNode upper = stack.top();
        stack.pop();
        ASTNode name = stack.top();
        stack.pop();

        ASTNode slice = new ASTSlice(ASTSlice.SliceOp.SLICE2, ASTNode.Node_NULL, upper);
        stack.push(new ASTSubscr(name, slice));
      }
        break;
      case SLICE_3: {
        ASTNode upper = stack.top();
        stack.pop();
        ASTNode lower = stack.top();
        stack.pop();
        ASTNode name = stack.top();
        stack.pop();

        ASTNode slice = new ASTSlice(ASTSlice.SliceOp.SLICE3, lower, upper);
        stack.push(new ASTSubscr(name, slice));
      }
        break;
      case STORE_ATTR_A: {
        if (unpack > 0) {
          ASTNode name = stack.top();
          stack.pop();
          ASTNode attr = new ASTBinary(name, new ASTName(code.getName(operand)), ASTBinary.BinOp.BIN_ATTR);

          ASTNode tup = stack.top();
          if (tup.type() == ASTNode.Type.NODE_TUPLE) {
            stack.pop();

            ASTTuple tuple = (ASTTuple) tup;
            tuple.add(attr);

            stack.push(tuple);
          } else {
            System.err.printf("Something TERRIBLE happened!\n");
          }

          if (--unpack <= 0) {
            tup = stack.top();
            stack.pop();
            ASTNode seq = stack.top();
            stack.pop();

            curblock.append(new ASTStore(seq, tup));
          }
        } else {
          ASTNode name = stack.top();
          stack.pop();
          ASTNode value = stack.top();
          stack.pop();
          ASTNode attr = new ASTBinary(name, new ASTName(code.getName(operand)), ASTBinary.BinOp.BIN_ATTR);

          curblock.append(new ASTStore(value, attr));
        }
      }
        break;
      case STORE_DEREF_A: {
        if (unpack > 0) {
          ASTNode name = new ASTName((PycString) (code.getCellVar(operand)));

          ASTNode tup = stack.top();
          if (tup.type() == ASTNode.Type.NODE_TUPLE) {
            stack.pop();

            ASTTuple tuple = (ASTTuple) tup;
            tuple.add(name);

            stack.push(tuple);
          } else {
            System.err.printf("Something TERRIBLE happened!\n");
          }

          if (--unpack <= 0) {
            tup = stack.top();
            stack.pop();
            ASTNode seq = stack.top();
            stack.pop();

            curblock.append(new ASTStore(seq, tup));
          }
        } else {
          ASTNode value = stack.top();
          stack.pop();
          ASTNode name = new ASTName((PycString) code.getCellVar(operand));
          curblock.append(new ASTStore(value, name));
        }
      }
        break;
      case STORE_FAST_A: {
        if (unpack > 0) {
          ASTNode name;

          if (mod.verCompare(1, 3) < 0)
            name = new ASTName(code.getName(operand));
          else
            name = new ASTName(code.getVarName(operand));

          ASTNode tup = stack.top();
          if (tup.type() == ASTNode.Type.NODE_TUPLE) {
            stack.pop();

            ASTTuple tuple = (ASTTuple) tup;
            tuple.add(name);

            stack.push(tuple);
          } else {
            System.err.printf("Something TERRIBLE happened!\n");
          }

          if (--unpack <= 0) {
            tup = stack.top();
            stack.pop();
            ASTNode seq = stack.top();
            stack.pop();

            if (curblock.blktype() == ASTBlock.BlkType.BLK_FOR 
                && !curblock.inited()) {
              ((ASTIterBlock) curblock).setIndex(tup);
            } else {
              curblock.append(new ASTStore(seq, tup));
            }
          }
        } else {
          ASTNode value = stack.top();
          stack.pop();
          ASTNode name;

          if (mod.verCompare(1, 3) < 0)
            name = new ASTName(code.getName(operand));
          else
            name = new ASTName(code.getVarName(operand));

          if (((ASTName) name).name().value().length() > 1 
              && ((ASTName) name).name().value().charAt(0) == '_' 
              && ((ASTName) name).name().value().charAt(1) == '[') {
            /* Don't show stores of list comp append objects. */
            break;
          }

          if (curblock.blktype() == ASTBlock.BlkType.BLK_FOR 
              && !curblock.inited()) {
            ((ASTIterBlock) curblock).setIndex(name);
          } else if (curblock.blktype() == ASTBlock.BlkType.BLK_WITH
              && !curblock.inited()) {
            ((ASTWithBlock) curblock).setExpr(value);
            ((ASTWithBlock) curblock).setVar(name);
          } else if (value.type() == ASTNode.Type.NODE_IMPORT) {
            ASTImport import_ = (ASTImport) value;
            import_.add_store(new ASTStore(value, name));
          } else {
            curblock.append(new ASTStore(value, name));
          }
        }
      }
        break;
      case STORE_GLOBAL_A: {
        ASTNode name = new ASTName(code.getName(operand));

        if (unpack > 0) {
          ASTNode tup = stack.top();
          if (tup.type() == ASTNode.Type.NODE_TUPLE) {
            stack.pop();

            ASTTuple tuple = (ASTTuple) tup;
            tuple.add(name);

            stack.push(tuple);
          } else {
            System.err.printf("Something TERRIBLE happened!\n");
          }

          if (--unpack <= 0) {
            tup = stack.top();
            stack.pop();
            ASTNode seq = stack.top();
            stack.pop();

            if (curblock.blktype() == ASTBlock.BlkType.BLK_FOR 
                && !curblock.inited()) {
              ((ASTIterBlock) curblock).setIndex(tup);
            } else {
              curblock.append(new ASTStore(seq, tup));
            }
          }
        } else {
          ASTNode value = stack.top();
          stack.pop();

          if (value instanceof ASTName && 
              ((ASTName) name).name() == ((ASTName) value).name()) {
            ASTImport import_ = (ASTImport) (stack.top());
            import_.add_store(new ASTStore(value, name));
          } else {
            curblock.append(new ASTStore(value, name));
          }
        }

        /* Mark the global as used */
        code.markGlobal(((ASTName) name).name());
      }
        break;
      case STORE_NAME_A: {
        if (unpack > 0) {
          ASTNode name = new ASTName(code.getName(operand));

          ASTNode tup = stack.top();
          if (tup.type() == ASTNode.Type.NODE_TUPLE) {
            stack.pop();

            ASTTuple tuple = (ASTTuple) tup;
            tuple.add(name);

            stack.push(tuple);
          } else {
            System.err.printf("Something TERRIBLE happened!\n");
          }

          if (--unpack <= 0) {
            tup = stack.top();
            stack.pop();
            ASTNode seq = stack.top();
            stack.pop();

            if (curblock.blktype() == ASTBlock.BlkType.BLK_FOR
                && !curblock.inited()) {
              ((ASTIterBlock) curblock).setIndex(tup);
            } else {
              curblock.append(new ASTStore(seq, tup));
            }
          }
        } else {
          ASTNode value = stack.top();
//          if (value.type() != ASTNode.Type.NODE_IMPORT)
            stack.pop();

          PycString varname = code.getName(operand);

          if (varname.value().length() > 1 && 
              varname.value().charAt(0) == '_' && varname.value().charAt(1) == '[') {
            /* Don't show stores of list comp append objects. */
            break;
          }

          ASTNode name = new ASTName(varname);

          if (curblock.blktype() == ASTBlock.BlkType.BLK_FOR 
              && !curblock.inited()) {
            ((ASTIterBlock) curblock).setIndex(name);
          } else if (value.type() == ASTNode.Type.NODE_IMPORT) {
            ASTImport import_ = (ASTImport) value;
            
            import_.add_store(new ASTStore(value, name));
            // curblock.append(import_);
          } else if (curblock.blktype() == ASTBlock.BlkType.BLK_WITH 
              && !curblock.inited()) {
            ((ASTWithBlock) curblock).setExpr(value);
            ((ASTWithBlock) curblock).setVar(name);
          } else {
            LinkedList<ASTNode> nodes = curblock.nodes();
            if (nodes.size() > 0) {
              ASTNode b = (ASTNode) nodes.getLast();

              if (b.type() == ASTNode.Type.NODE_BLOCK) {
                curblock.removeLast();
              }
            }

            curblock.append(new ASTStore(value, name));

            if (value.type() == ASTNode.Type.NODE_INVALID)
              break;
          }
        }
      }
        break;
      case STORE_SLICE_0: {
        ASTNode dest = stack.top();
        stack.pop();
        ASTNode value = stack.top();
        stack.pop();

        curblock.append(new ASTStore(value, new ASTSubscr(dest, new ASTSlice(ASTSlice.SliceOp.SLICE0))));
      }
        break;
      case STORE_SLICE_1: {
        ASTNode upper = stack.top();
        stack.pop();
        ASTNode dest = stack.top();
        stack.pop();
        ASTNode value = stack.top();
        stack.pop();

        curblock.append(new ASTStore(value, new ASTSubscr(dest, new ASTSlice(ASTSlice.SliceOp.SLICE1, upper))));
      }
        break;
      case STORE_SLICE_2: {
        ASTNode lower = stack.top();
        stack.pop();
        ASTNode dest = stack.top();
        stack.pop();
        ASTNode value = stack.top();
        stack.pop();

        curblock.append(new ASTStore(value, new ASTSubscr(dest, new ASTSlice(ASTSlice.SliceOp.SLICE2, ASTNode.Node_NULL, lower))));
      }
        break;
      case STORE_SLICE_3: {
        ASTNode lower = stack.top();
        stack.pop();
        ASTNode upper = stack.top();
        stack.pop();
        ASTNode dest = stack.top();
        stack.pop();
        ASTNode value = stack.top();
        stack.pop();

        curblock.append(new ASTStore(value, new ASTSubscr(dest, new ASTSlice(ASTSlice.SliceOp.SLICE3, upper, lower))));
      }
        break;
      case STORE_SUBSCR: {
        if (unpack > 0) {
          ASTNode subscr = stack.top();
          stack.pop();
          ASTNode dest = stack.top();
          stack.pop();

          ASTNode save = new ASTSubscr(dest, subscr);

          ASTNode tup = stack.top();
          if (tup.type() == ASTNode.Type.NODE_TUPLE) {
            stack.pop();

            ASTTuple tuple = (ASTTuple) tup;
            tuple.add(save);

            stack.push(tuple);
          } else {
            System.err.printf("Something TERRIBLE happened!\n");
          }

          if (--unpack <= 0) {
            tup = stack.top();
            stack.pop();
            ASTNode seq = stack.top();
            stack.pop();

            curblock.append(new ASTStore(seq, tup));
          }
        } else {
          ASTNode subscr = stack.top();
          stack.pop();
          ASTNode dest = stack.top();
          stack.pop();
          ASTNode src = stack.top();
          stack.pop();

          if (dest.type() == ASTNode.Type.NODE_MAP) {
            ((ASTMap) dest).add(subscr, src);
          } else {
            curblock.append(new ASTStore(src, new ASTSubscr(dest, subscr)));
          }
        }
      }
        break;
      case UNARY_CALL: {
        ASTNode func = stack.top();
        stack.pop();
        stack.push(new ASTCall(func, new LinkedList<ASTNode>(), new LinkedList<Pair<ASTNode, ASTNode>>()));
      }
        break;
      case UNARY_CONVERT: {
        ASTNode name = stack.top();
        stack.pop();
        stack.push(new ASTConvert(name));
      }
        break;
      case UNARY_INVERT: {
        ASTNode arg = stack.top();
        stack.pop();
        stack.push(new ASTUnary(arg, ASTUnary.UnOp.UN_INVERT));
      }
        break;
      case UNARY_NEGATIVE: {
        ASTNode arg = stack.top();
        stack.pop();
        stack.push(new ASTUnary(arg, ASTUnary.UnOp.UN_NEGATIVE));
      }
        break;
      case UNARY_NOT: {
        ASTNode arg = stack.top();
        stack.pop();
        stack.push(new ASTUnary(arg, ASTUnary.UnOp.UN_NOT));
      }
        break;
      case UNARY_POSITIVE: {
        ASTNode arg = stack.top();
        stack.pop();
        stack.push(new ASTUnary(arg, ASTUnary.UnOp.UN_POSITIVE));
      }
        break;
      case UNPACK_LIST_A:
      case UNPACK_TUPLE_A:
      case UNPACK_SEQUENCE_A: {
        unpack = operand;

        List<ASTNode> vals = new LinkedList<ASTNode>(); // /////////????????????????????

        stack.push(new ASTTuple(vals));
      }
        break;
      case YIELD_VALUE: {
        ASTNode value = stack.top();
        stack.pop();
        curblock.append(new ASTReturn(value, ASTReturn.RetType.YIELD));
      }
        break;
      default:
        Logger.log("Unsupported opcode: " + Pyc.OpcodeName(opcode));
        cleanBuild = false;
        return new ASTNodeList(defblock.nodes());
      }

      else_pop = ((curblock.blktype() == ASTBlock.BlkType.BLK_ELSE) 
          || (curblock.blktype() == ASTBlock.BlkType.BLK_IF) 
          || (curblock.blktype() == ASTBlock.BlkType.BLK_ELIF)) 
          && (curblock.end() == pos);


      Logger.log("    blocks size: ", blocks.size(), ", stack size: ", stack.size(), ", stack_hist size: ", stack_hist.size(), ", stack peek", stack != null && stack.size() > 0 ? stack.peek() : " 000 ");
    }

    if (stack.size() > 0) {
       Logger.log("Warning: Stack is not empty!");

      while (stack.size() > 0) {
        Logger.log("    " + stack.top());
        stack.pop();
      }
    }

    if (stack_hist.size() > 0) {
      Logger.log("Warning: Stack history is not empty!");

      while (stack_hist.size() > 0) {
        Logger.log("    " + stack_hist.top());
        stack_hist.pop();
      }
    }

    if (blocks.size() > 1) {
      Logger.log("Warning: block stack is not empty!");

      while (blocks.size() > 1) {
        ASTBlock tmp = blocks.top();
        Logger.log("    " + tmp);
        blocks.pop();

        blocks.top().append(tmp);
      }
    }

    cleanBuild = true;
    return new ASTNodeList(defblock.nodes());
  }


  static int cmp_prec(ASTNode parent, ASTNode child) {
    /* Determine whether the parent has higher precedence than therefore
       child, so we don't flood the source code with extraneous parens.
       Else we'd have expressions like (((a + b) + c) + d) when therefore
       equivalent, a + b + c + d would suffice. */

    if (parent.type() == ASTNode.Type.NODE_UNARY 
        && ((ASTUnary) parent).op() == ASTUnary.UnOp.UN_NOT)
      return 1; // Always parenthesize not(x)
    if (child.type() == ASTNode.Type.NODE_BINARY) {
      ASTBinary binChild = (ASTBinary) child;
      if (parent.type() == ASTNode.Type.NODE_BINARY)
        return binChild.opordinal() - ((ASTBinary) parent).opordinal();
      else if (parent.type() == ASTNode.Type.NODE_COMPARE)
        return (binChild.op() == ASTBinary.BinOp.BIN_LOG_AND ||
                binChild.op() == ASTBinary.BinOp.BIN_LOG_OR) ? 1 : -1;
      else if (parent.type() == ASTNode.Type.NODE_UNARY)
        return (binChild.op() == ASTBinary.BinOp.BIN_POWER) ? -1 : 1;
    } else if (child.type() == ASTNode.Type.NODE_UNARY) {
      ASTUnary unChild = (ASTUnary) child;
      if (parent.type() == ASTNode.Type.NODE_BINARY) {
        ASTBinary binParent = (ASTBinary) parent;
        if (binParent.op() == ASTBinary.BinOp.BIN_LOG_AND || 
            binParent.op() == ASTBinary.BinOp.BIN_LOG_OR)
          return -1;
        else if (unChild.op() == ASTUnary.UnOp.UN_NOT)
          return 1;
        else if (binParent.op() == ASTBinary.BinOp.BIN_POWER)
          return 1;
        else
          return -1;
      } else if (parent.type() == ASTNode.Type.NODE_COMPARE) {
        return (unChild.op() == ASTUnary.UnOp.UN_NOT) ? 1 : -1;
      } else if (parent.type() == ASTNode.Type.NODE_UNARY) {
        return ((ASTUnary.UnOp) unChild.op()).ordinal() - ((ASTUnary.UnOp) ((ASTUnary) parent).op()).ordinal();
      }
    } else if (child.type() == ASTNode.Type.NODE_COMPARE) {
      ASTCompare cmpChild = (ASTCompare) child;
      if (parent.type() == ASTNode.Type.NODE_BINARY)
        return (((ASTBinary) parent).op() == ASTBinary.BinOp.BIN_LOG_AND ||
                ((ASTBinary) parent).op() == ASTBinary.BinOp.BIN_LOG_OR) ? -1 : 1;
      else if (parent.type() == ASTNode.Type.NODE_COMPARE)
        return cmpChild.opordinal() - ((ASTCompare) parent).opordinal();
      else if (parent.type() == ASTNode.Type.NODE_UNARY)
        return (((ASTUnary) parent).op() == ASTUnary.UnOp.UN_NOT) ? -1 : 1;
    }

    /* For normal nodes, don't parenthesize anything */
    return -1;
  }

  static PrintStream pyc_output = PycData.pyc_output;

  static void print_ordered(ASTNode parent, ASTNode child, PycModule mod) throws IOException {
    if (child.type() == ASTNode.Type.NODE_BINARY ||
        child.type() == ASTNode.Type.NODE_COMPARE) {
      if (cmp_prec(parent, child) > 0) {
        pyc_output.printf("(");
        print_src(child, mod);
        pyc_output.printf(")");
      } else {
        print_src(child, mod);
      }
    } else if (child.type() == ASTNode.Type.NODE_UNARY) {
      if (cmp_prec(parent, child) > 0) {
        pyc_output.printf("(");
        print_src(child, mod);
        pyc_output.printf(")");
      } else {
        print_src(child, mod);
      }
    } else {
      print_src(child, mod);
    }
  }

  static void start_line(int indent) {
    if (inPrint || inLambda)
      return;
    for (int i = 0; i < indent; i++)
      pyc_output.printf("    ");
  }

  static void end_line() {
    if (inPrint || inLambda)
      return;
    pyc_output.printf("\n");
  }

  static int cur_indent = -1;

  static void print_block(ASTBlock blk, PycModule mod) throws IOException {
    LinkedList<ASTNode> lines = blk.nodes();

    if (lines.size() == 0) {
      ASTNode pass = new ASTNode(ASTNode.Type.NODE_PASS);
      start_line(cur_indent);
      print_src(pass, mod);
    }

    for (Iterator<ASTNode> ln = lines.iterator(); ln.hasNext();) {
      ASTNode next = ln.next();

      if (next.type() == ASTNode.Type.NODE_KEYWORD) {
        continue;
      }

      if (next.type() != ASTNode.Type.NODE_NODELIST) {
        start_line(cur_indent);
      }
      print_src(next, mod);
      if (ln.hasNext()) {
        end_line();
      }
    }
  }

  static void print_src(ASTNode node, PycModule mod) throws IOException {
    if (node == ASTNode.Node_NULL) {
      pyc_output.printf("None");
      cleanBuild = true;
      return;
    }

    switch (node.type()) {
    case NODE_BINARY:
    case NODE_COMPARE: {
      ASTBinary bin = (ASTBinary) node;
      print_ordered(node, bin.left(), mod);
      pyc_output.printf("%s", bin.op_str());
      print_ordered(node, bin.right(), mod);
    }
      break;
    case NODE_UNARY: {
      ASTUnary un = (ASTUnary) node;
      pyc_output.printf("%s", un.op_str());
      print_ordered(node, un.operand(), mod);
    }
      break;
    case NODE_CALL: {
      ASTCall call = (ASTCall) node;
      print_src(call.func(), mod);
      pyc_output.printf("(");
      boolean first = true;
      for (Iterator<ASTNode> p = call.pparams().iterator(); p.hasNext();) {
        if (!first)
          pyc_output.printf(", ");
        print_src(p.next(), mod);
        first = false;
      }
      for (Iterator<Pair<ASTNode, ASTNode>> p = call.kwparams().iterator(); p.hasNext();) {
        if (!first)
          pyc_output.printf(", ");
        Pair<ASTNode, ASTNode> next = p.next();
        ASTNode A = next.getKey();
        if (A instanceof ASTName)
          pyc_output.printf("%s = ", ((ASTName) A).name().value());
        else {
          PycObject obj = ((ASTObject) A).object();
          if (obj instanceof PycString)
            pyc_output.printf("%s = ", ((PycString) obj).value());
          else
            pyc_output.printf("%s = ", ((ASTObject) A).object());
        }

        ASTNode B = next.getValue();
        if (B instanceof ASTName)
          print_src((ASTName) B, mod);
        else
          print_src(B, mod);

        first = false;
      }
      if (call.hasVar()) {
        if (!first)
          pyc_output.printf(", ");
        pyc_output.printf("*");
        print_src(call.var(), mod);
        first = false;
      }
      if (call.hasKW()) {
        if (!first)
          pyc_output.printf(", ");
        pyc_output.printf("**");
        print_src(call.kw(), mod);
        first = false;
      }
      pyc_output.printf(")");
    }
      break;
    case NODE_DELETE: {
      pyc_output.printf("del ");
      print_src(((ASTDelete) node).value(), mod);
    }
      break;
    case NODE_EXEC: {
      ASTExec exec = (ASTExec) node;
      pyc_output.printf("exec ");
      print_src(exec.statement(), mod);

      if (exec.globals() != ASTNode.Node_NULL) {
        pyc_output.printf(" in ");
        print_src(exec.globals(), mod);

        if (exec.locals() != ASTNode.Node_NULL 
            && exec.globals() != exec.locals()) {
          pyc_output.printf(", ");
          print_src(exec.locals(), mod);
        }
      }
    }
      break;
    case NODE_KEYWORD:
      pyc_output.printf("%s", ((ASTKeyword) node).word_str());
      break;
    case NODE_LIST: {
      List<ASTNode> values = ((ASTList) node).values();
      pyc_output.printf("[");
      boolean first = true;
      cur_indent++;
      for (Iterator<ASTNode> b = values.iterator(); b.hasNext();) {
        if (!first)
          pyc_output.printf(", ");
        print_src(b.next(), mod);
        first = false;
      }
      cur_indent--;
      pyc_output.printf("]");
    }
      break;
    case NODE_COMPREHENSION: {
      ASTComprehension comp = (ASTComprehension) node;
      List<ASTIterBlock> values = comp.generators();

      pyc_output.printf("[ ");
      print_src(comp.result(), mod);

      for (Iterator<ASTIterBlock> it = values.iterator(); it.hasNext();) {
        ASTIterBlock next = it.next();

        pyc_output.printf(" for ");
        print_src(next.index(), mod);
        pyc_output.printf(" in ");
        print_src(next.iter(), mod);
      }
      pyc_output.printf(" ]");
    }
      break;
    case NODE_MAP: {
      List<Pair<ASTNode, ASTNode>> values = ((ASTMap) node).values();
      pyc_output.printf("{");
      boolean first = true;
      cur_indent++;
      for (Iterator<Pair<ASTNode, ASTNode>> b = values.iterator(); b.hasNext();) {
        if (first)
          pyc_output.printf("\n");
        else
          pyc_output.printf(",\n");

        Pair<ASTNode, ASTNode> next = b.next();
        start_line(cur_indent);
        print_src(next.getKey(), mod);
        pyc_output.printf(": ");
        print_src(next.getValue(), mod);
        first = false;
      }
      cur_indent--;
      pyc_output.printf(" }");
    }
      break;
    case NODE_NAME:
      pyc_output.printf("%s", ((ASTName) node).name().value());
      break;
    case NODE_NODELIST: {
      cur_indent++;
      List<ASTNode> lines = ((ASTNodeList) node).nodes();
      for (Iterator<ASTNode> ln = lines.iterator(); ln.hasNext();) {
        start_line(cur_indent);
        print_src(ln.next(), mod);
        end_line();
      }
      cur_indent--;
    }
      break;
    case NODE_BLOCK: {
      ASTBlock blk = (ASTBlock) node;
      if (blk.blktype() == ASTBlock.BlkType.BLK_ELSE && ((ASTBlock) node).size() == 0)
        break;

      if (blk.blktype() == ASTBlock.BlkType.BLK_CONTAINER) {
        end_line();
        print_block(blk, mod);
        end_line();
        break;
      }
      inPrint = false;

      pyc_output.printf("%s", blk.type_str());

      if (blk.blktype() == ASTBlock.BlkType.BLK_IF 
          || blk.blktype() == ASTBlock.BlkType.BLK_ELIF 
          || blk.blktype() == ASTBlock.BlkType.BLK_WHILE) {
        if (((ASTCondBlock) blk).negative())
          pyc_output.printf(" not ");
        else
          pyc_output.printf(" ");

        print_src(((ASTCondBlock) blk).cond(), mod);
      } else if (blk.blktype() == ASTBlock.BlkType.BLK_FOR) {
        pyc_output.printf(" ");
        print_src(((ASTIterBlock) blk).index(), mod);
        pyc_output.printf(" in ");
        print_src(((ASTIterBlock) blk).iter(), mod);
      } else if (blk.blktype() == ASTBlock.BlkType.BLK_EXCEPT 
          && ((ASTCondBlock) blk).cond() != ASTNode.Node_NULL) {
        pyc_output.printf(" ");
        print_src(((ASTCondBlock) blk).cond(), mod);
      } else if (blk.blktype() == ASTBlock.BlkType.BLK_WITH) {
        pyc_output.printf(" ");
        print_src(((ASTWithBlock) blk).expr(), mod);
        ASTNode var = ((ASTWithBlock) blk).var();
        if (var != ASTNode.Node_NULL) {
          pyc_output.printf(" as ");
          print_src(var, mod);
        }
      } else if (blk.blktype() == ASTBlock.BlkType.BLK_MAIN) {
        break;
      }
      pyc_output.printf(":\n");

      cur_indent++;
      print_block(blk, mod);
      if (inPrint) {
        pyc_output.printf(",");
      }
      cur_indent--;
      inPrint = false;
    }
      break;
    case NODE_OBJECT: {
      PycObject obj = ((ASTObject) node).object();
      if (obj instanceof PycCode) {
        PycObject obj_ = ((PycCode) obj).consts().get(0);
        if (obj_ instanceof PycString && ((PycString) obj_).length() > 0) {
          PycString.OutputString((PycString) obj_, '\t', true);
          pyc_output.printf("\n");
        }
      }
      if (obj.type() == PycObject.Type.TYPE_CODE) {
        PycCode code = (PycCode) obj;
        decompyle(code, mod);
      } else {
        bytecode.print_const(obj, mod);
      }
    }
      break;
    case NODE_PASS:
      pyc_output.printf("pass");
      break;
    case NODE_PRINT:
      ASTPrint _node = (ASTPrint) node;
      if (_node.value() == ASTNode.Node_NULL) {
        if (!inPrint) {
          pyc_output.printf("print ");
          if (_node.stream() != ASTNode.Node_NULL) {
            pyc_output.printf(">>");
            print_src(_node.stream(), mod);
          }
        }
        inPrint = false;
      } else if (!inPrint) {
        pyc_output.printf("print ");
        if (_node.stream() != ASTNode.Node_NULL) {
          pyc_output.printf(">>");
          print_src(_node.stream(), mod);
          pyc_output.printf(", ");
        }
        print_src(_node.value(), mod);
        inPrint = true;
      } else {
        pyc_output.printf(", ");
        print_src(_node.value(), mod);
      }
      break;
    case NODE_RAISE: {
      ASTRaise raise = (ASTRaise) node;
      pyc_output.printf("raise ");
      boolean first = true;
      for (Iterator<ASTNode> p = raise.params().iterator(); p.hasNext();) {
        if (!first)
          pyc_output.printf(", ");
        print_src(p.next(), mod);
        first = false;
      }
    }
      break;
    case NODE_RETURN: {
      ASTReturn ret = (ASTReturn) node;
      if (!inLambda) {
        switch (ret.rettype()) {
        case RETURN:
          pyc_output.printf("return ");
          break;
        case YIELD:
          pyc_output.printf("yield ");
          break;
        }
      }
      print_src(ret.value(), mod);
    }
      break;
    case NODE_SLICE: {
      ASTSlice slice = (ASTSlice) node;

      if (slice.op() == ASTSlice.SliceOp.SLICE1) {
        print_src(slice.left(), mod);
      }
      pyc_output.printf(":");
      if (slice.op() == ASTSlice.SliceOp.SLICE2) {
        print_src(slice.right(), mod);
      }
    }
      break;
    case NODE_IMPORT: {
      ASTImport import_ = (ASTImport) node;
      if (import_.stores().isEmpty()) {
        List<ASTStore> stores = import_.stores();

        pyc_output.printf("from ");
        if (import_.name().type() == ASTNode.Type.NODE_IMPORT)
          print_src(((ASTImport) import_.name()).name(), mod);
        else
          print_src(import_.name(), mod);
        pyc_output.printf(" import ");

        Iterator<ASTStore> ii = stores.iterator();
        if (stores.size() == 1) {
          ASTStore next = ii.next();
          print_src(next.src(), mod);

          String s1 = ((ASTName) next.src()).name().value();
          String s2 = ((ASTName) next.dest()).name().value();
          if (!s1.equals(s2)) {
            pyc_output.printf(" as ");
            print_src(next.dest(), mod);
          }
        } else {
          boolean first = true;
          for (; ii.hasNext();) {
            ASTStore next = ii.next();
            if (!first)
              pyc_output.printf(", ");
            print_src(next.src(), mod);
            first = false;

            String s1 = ((ASTName) next.src()).name().value();
            String s2 = ((ASTName) next.dest()).name().value();
            if (!s1.equals(s2)) {
              pyc_output.printf(" as ");
              print_src(next.dest(), mod);
            }
          }
        }
      } else {
        pyc_output.printf("import ");
        print_src(import_.name(), mod);
      }
    }
      break;
    case NODE_FUNCTION: {
      /* Actual named functions are NODE_STORE with a name */
      pyc_output.printf("lambda ");
      ASTNode code = ((ASTFunction) node).code();
      PycCode code_src = (PycCode) ((ASTObject) code).object();
      List<ASTNode> defargs = ((ASTFunction) node).defargs();
      Iterator<ASTNode> da = defargs.iterator();
      for (int i = 0; i < code_src.argCount(); i++) {
        if (i > 0)
          pyc_output.printf(", ");
        pyc_output.printf("%s", code_src.getVarName(i).value());
        if ((code_src.argCount() - i) <= (int) defargs.size()) {
          pyc_output.printf(" = ");
          print_src(da.next(), mod);
        }
      }
      pyc_output.printf(": ");

      inLambda = true;
      print_src(code, mod);
      inLambda = false;
    }
      break;
    case NODE_STORE: {
      ASTNode src = ((ASTStore) node).src();
      ASTNode dest = ((ASTStore) node).dest();
      if (src.type() == ASTNode.Type.NODE_FUNCTION) {
        ASTNode code = ((ASTFunction) src).code();
        PycCode code_src = (PycCode) ((ASTObject) code).object();
        boolean isLambda = false;

        if (code_src.name().value().equals("<lambda>")) {
          pyc_output.printf("\n");
          start_line(cur_indent);
          print_src(dest, mod);
          pyc_output.printf(" = lambda ");
          isLambda = true;
        } else {
          pyc_output.printf("\n");
          start_line(cur_indent);
          pyc_output.printf("def ");
          print_src(dest, mod);
          pyc_output.printf("(");
        }

        List<ASTNode> defargs = ((ASTFunction) src).defargs();
        Iterator<ASTNode> da = defargs.iterator();
        boolean first = true;
        for (int i = 0; i < code_src.argCount(); i++) {
          if (!first)
            pyc_output.printf(", ");
          pyc_output.printf("%s", code_src.getVarName(i).value());
          if ((code_src.argCount() - i) <= (int) defargs.size()) {
            pyc_output.printf(" = ");
            print_src(da.next(), mod);
          }
          first = false;
        }
        if (code_src.flags() == PycCode.CodeFlags.CO_VARARGS) {
          if (!first)
            pyc_output.printf(", ");
          pyc_output.printf("*%s", code_src.getVarName(code_src.argCount()).value());
          first = false;
        }
        if (code_src.flags() == PycCode.CodeFlags.CO_VARKEYWORDS) {
          if (!first)
            pyc_output.printf(", ");

          int idx = code_src.argCount();
          if (code_src.flags() == PycCode.CodeFlags.CO_VARARGS) {
            idx++;
          }
          pyc_output.printf("**%s", code_src.getVarName(idx).value());
          first = false;
        }

        if (isLambda) {
          pyc_output.printf(": ");
        } else {
          pyc_output.printf("):\n");
          printGlobals = true;
        }

        boolean preLambda = inLambda;
        inLambda |= isLambda;

        print_src(code, mod);

        inLambda = preLambda;
      } else if (src.type() == ASTNode.Type.NODE_CLASS) {
        pyc_output.printf("\n");
        start_line(cur_indent);
        pyc_output.printf("class ");
        print_src(dest, mod);

        // ASTTuple bases = (ASTTuple)((ASTClass)src).bases();
        List<ASTNode> values = new LinkedList<ASTNode>();
        ASTNode bases = ((ASTClass) src).bases();
        Logger.log("\n    '''---->bases class name: " + bases.getClass().getName() + "'''\n");
        if (bases instanceof ASTTuple)
          values = ((ASTTuple) bases).values();
        else if (values.size() > 0) {
          pyc_output.printf("(");
          boolean first = true;
          for (Iterator<ASTNode> b = values.iterator(); b.hasNext();) {
            if (!first)
              pyc_output.printf(", ");
            print_src(b.next(), mod);
            first = false;
          }
          pyc_output.printf("):\n");
        } else {
          // Don't put parens if there are no base classes
          pyc_output.printf(":\n");
        }

        ASTNode code = ((ASTFunction) ((ASTCall) ((ASTClass) src).code()).func()).code();
        print_src(code, mod);
      } else if (src.type() == ASTNode.Type.NODE_IMPORT) {
        ASTImport import_ = (ASTImport) src;
        if (import_.fromlist() != ASTNode.Node_NULL) {
          PycObject fromlist = ((ASTObject) import_.fromlist()).object();
          if (fromlist != PycObject.Pyc_None) {
            pyc_output.printf("from ");
            if (import_.name().type() == ASTNode.Type.NODE_IMPORT)
              print_src(((ASTImport) import_.name()).name(), mod);
            else
              print_src(import_.name(), mod);
            pyc_output.printf(" import ");
            if (fromlist.type() == PycObject.Type.TYPE_TUPLE) {
              boolean first = true;
              for (Iterator<PycObject> ii = ((PycTuple) fromlist).values().iterator(); ii.hasNext();) {
                if (!first)
                  pyc_output.printf(", ");
                pyc_output.printf("%s", ((PycString) ii.next()).value());
                first = false;
              }
            } else {
              pyc_output.printf("%s", ((PycString) fromlist).value());
            }
          } else {
            pyc_output.printf("import ");
            print_src(import_.name(), mod);
          }
        } else {
          pyc_output.printf("import ");
          ASTNode import_name = import_.name();
          print_src(import_name, mod);
          if (!((ASTName) dest).name().isEqual((PycObject) ((ASTName) import_name).name())) {
            pyc_output.printf(" as ");
            print_src(dest, mod);
          }
        }
      } else {
        if (src.type() == ASTNode.Type.NODE_BINARY && ((ASTBinary) src).is_inplace()) {
          print_src(src, mod);
          break;
        }

        if (dest.type() == ASTNode.Type.NODE_NAME && ((ASTName) dest).name().isEqual("__doc__")) {
          if (src.type() == ASTNode.Type.NODE_OBJECT) {
            PycObject obj = ((ASTObject) src).object();
            if (// obj.type() == PycObject.Type.TYPE_STRING ||
            obj.type() == PycObject.Type.TYPE_INTERNED || obj.type() == PycObject.Type.TYPE_STRINGREF)
              PycString.OutputString((PycString) obj, (mod.majorVer() == 3) ? 'b' : 0, true);
            else if (obj.type() == PycObject.Type.TYPE_UNICODE)
              PycString.OutputString((PycString) obj, (mod.majorVer() == 3) ? 0 : 'u', true);
          } else {
            print_src(dest, mod);
            pyc_output.printf(" = ");
            print_src(src, mod);
          }
        } else {
          print_src(dest, mod);
          pyc_output.printf(" = ");
          print_src(src, mod);
        }
      }
    }
      break;
    case NODE_SUBSCR: {
      print_src(((ASTSubscr) node).name(), mod);
      pyc_output.printf("[");
      print_src(((ASTSubscr) node).key(), mod);
      pyc_output.printf("]");
    }
      break;
    case NODE_CONVERT: {
      pyc_output.printf("`");
      print_src(((ASTConvert) node).name(), mod);
      pyc_output.printf("`");
    }
      break;
    case NODE_TUPLE: {
      List<ASTNode> values = ((ASTTuple) node).values();
      pyc_output.printf("(");
      boolean first = true;
      for (Iterator<ASTNode> b = values.iterator(); b.hasNext();) {
        if (!first)
          pyc_output.printf(", ");
        print_src(b.next(), mod);
        first = false;
      }
      if (values.size() == 1)
        pyc_output.printf(",)");
      else
        pyc_output.printf(")");
    }
      break;
    default:
      pyc_output.printf("<NODE:%d>", node.type());
      System.err.printf("Unsupported Node type: %d\n", node.type());
      cleanBuild = false;
      return;
    }

    cleanBuild = true;
  }

  static void decompyle(PycCode code, PycModule mod) throws IOException {
    ASTNode source = BuildFromCode(code, mod);

    ASTNodeList clean = (ASTNodeList) source;
    if (cleanBuild) {
      // The Python compiler adds some stuff that we don't really care
      // about, and would add extra code for re-compilation anyway.
      // We strip these lines out here, and then add a "pass" statement
      // if the cleaned up code is isEmpty
      if (clean.nodes().getFirst().type() == ASTNode.Type.NODE_STORE) {
        ASTStore store = (ASTStore) clean.nodes().getFirst();
        if (store.src().type() == ASTNode.Type.NODE_NAME && store.dest().type() == ASTNode.Type.NODE_NAME) {
          ASTName src = ((ASTName) store.src());
          ASTName dest = ((ASTName) store.dest());
          if (src.name().isEqual("__name__") && dest.name().isEqual("__module__")) {
            // __module__ = __name__
            clean.removeFirst();
          }
        }
      }
      if (clean.nodes().getLast().type() == ASTNode.Type.NODE_RETURN) {
        ASTReturn ret = (ASTReturn) clean.nodes().getLast();

        if (ret.value() == ASTNode.Node_NULL || ret.value().type() == ASTNode.Type.NODE_LOCALS) {
          clean.removeLast(); // Always an extraneous return statement
        }
      }
    }
    // This is outside the clean check so a source block will always
    // be compilable, even if decompylation failed.
    if (clean.nodes().size() == 0)
      clean.append(new ASTNode(ASTNode.Type.NODE_PASS));

    inPrint = false;
    boolean part1clean = cleanBuild;

    Set<PycObject> globs = code.getGlobals();
    if (printGlobals && globs.size() > 0) {
      start_line(cur_indent + 1);
      pyc_output.printf("global ");
      boolean first = true;
      for (Iterator<PycObject> it = globs.iterator(); it.hasNext();) {
        if (!first)
          pyc_output.printf(", ");
        pyc_output.printf("%s", ((PycString) it.next()).value());
        first = false;
      }
      pyc_output.printf("\n");
      printGlobals = false;
    }

    print_src(source, mod);

    if (!cleanBuild || !part1clean) {
      start_line(cur_indent);
      pyc_output.printf("# WARNING: Decompyle incomplete\n");
    }
  }
}
