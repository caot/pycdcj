package pydecompiler.dis;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

// http://bear.ces.cwru.edu/eecs_382/c2java.html

public class pycdas extends PycString {
  static PrintStream pyc_output = PycData.pyc_output;

  static String flag_names[] = {
    "CO_OPTIMIZED", "CO_NEWLOCALS", "CO_VARARGS", "CO_VARKEYWORDS",
    "CO_NESTED", "CO_GENERATOR", "CO_NOFREE", "<0x80>", "<0x100>", "<0x200>",
    "<0x400>", "<0x800>", "CO_GENERATOR_ALLOWED", "CO_FUTURE_DIVISION",
    "CO_FUTURE_ABSOLUTE_IMPORT", "CO_FUTURE_WITH_STATEMENT",
    "CO_FUTURE_PRINT_FUNCTION", "CO_FUTURE_UNICODE_LITERALS",
    "CO_FUTURE_BARRY_AS_BDFL", "<0x80000>", "<0x100000>", "<0x200000>",
    "<0x400000>", "<0x800000>", "<0x1000000>", "<0x2000000>", "<0x4000000>",
    "<0x8000000>", "<0x10000000>", "<0x20000000>", "<0x40000000>",
    "<0x80000000>"
  };

  static void print_coflags(long flags) {
    if (flags == 0) {
      pyc_output.printf("\n");
      return;
    }

    pyc_output.printf(" (");
    long f = 1;
    int k = 0;
    while (k < 32) {
      if ((flags & f) != 0) {
        flags &= ~f;
        if (flags == 0)
          pyc_output.printf("%s", flag_names[k]);
        else
          pyc_output.printf("%s | ", flag_names[k]);
      }
      ++k;
      f <<= 1;
    }
    pyc_output.printf(")\n");
  }

  // static void
  static void ivprintf(int indent, String fmt, Object... args) {
    for (int i = 0; i < indent; i++)
      pyc_output.printf("    ");
    pyc_output.printf(fmt, args);
  }

  static void iprintf(int indent, String fmt, double arg, double... args) {
    ivprintf(indent, fmt, arg, args);
  }

  static void iprintf(int indent, String fmt, int arg, int... args) {
    ivprintf(indent, fmt, arg, args);
  }

  static void iprintf(int indent, String fmt, String... args) {
    ivprintf(indent, fmt, (Object[]) args);
  }

  static void output_object(PycObject obj, PycModule mod, int indent) throws IOException {
    switch (obj.type()) {
    case PycObject.Type.TYPE_CODE:
    case PycObject.Type.TYPE_CODE2: {
      PycCode codeObj = (PycCode) obj;
      iprintf(indent, "[Code]\n");
      iprintf(indent + 1, "File Name: %s\n", codeObj.fileName().value());
      iprintf(indent + 1, "Object Name: %s\n", codeObj.name().value());
      iprintf(indent + 1, "Arg Count: %d\n", codeObj.argCount());
      iprintf(indent + 1, "Locals: %d\n", codeObj.numLocals());
      iprintf(indent + 1, "Stack Size: %d\n", codeObj.stackSize());
      iprintf(indent + 1, "Flags: 0x%08X", codeObj.flags());
      print_coflags(codeObj.flags());

      if (codeObj.names() != PycObject.Pyc_NULL) {
        iprintf(indent + 1, "[Names]\n");
        for (int i = 0; i < codeObj.names().size(); i++)
          output_object(codeObj.names().get(i), mod, indent + 2);
      }

      if (codeObj.varNames() != PycObject.Pyc_NULL) {
        iprintf(indent + 1, "[Var Names]\n");
        for (int i = 0; i < codeObj.varNames().size(); i++)
          output_object(codeObj.varNames().get(i), mod, indent + 2);
      }

      if (codeObj.freeVars() != PycObject.Pyc_NULL) {
        iprintf(indent + 1, "[Free Vars]\n");
        for (int i = 0; i < codeObj.freeVars().size(); i++)
          output_object(codeObj.freeVars().get(i), mod, indent + 2);
      }

      if (codeObj.cellVars() != PycObject.Pyc_NULL) {
        iprintf(indent + 1, "[Cell Vars]\n");
        for (int i = 0; i < codeObj.cellVars().size(); i++)
          output_object(codeObj.cellVars().get(i), mod, indent + 2);
      }

      if (codeObj.consts() != PycObject.Pyc_NULL) {
        iprintf(indent + 1, "[Constants]\n");
        for (int i = 0; i < codeObj.consts().size(); i++)
          output_object(codeObj.consts().get(i), mod, indent + 2);
      }

      iprintf(indent + 1, "[Disassembly]\n");
      bytecode.bc_disasm(codeObj, mod, indent + 2);
    }
      break;
    case PycObject.Type.TYPE_STRING:
    case PycObject.Type.TYPE_STRINGREF:
    case PycObject.Type.TYPE_INTERNED:
      iprintf(indent, "");
      OutputString((PycString) obj, (mod.majorVer() == 3) ? 'b' : 0);
      pyc_output.printf("\n");
      break;
    case PycObject.Type.TYPE_UNICODE:
      iprintf(indent, "");
      OutputString((PycString) obj, (mod.majorVer() == 3) ? 0 : 'u');
      pyc_output.printf("\n");
      break;
    case PycObject.Type.TYPE_TUPLE: {
      iprintf(indent, "(\n");
      List<PycObject> values = ((PycTuple) obj).values();
      for (Iterator<PycObject> i = values.iterator(); i.hasNext();)
        output_object(i.next(), mod, indent + 1);
      iprintf(indent, ")\n");
    }
      break;
    case PycObject.Type.TYPE_LIST: {
      iprintf(indent, "[\n");
      List<PycObject> values = ((PycList) obj).values();
      for (Iterator<PycObject> i = values.iterator(); i.hasNext();)
        output_object(i.next(), mod, indent + 1);
      iprintf(indent, "]\n");
    }
      break;
    case PycObject.Type.TYPE_DICT: {
      iprintf(indent, "{\n");
      List<PycObject> keys = ((PycDict) obj).keys();
      List<PycObject> values = ((PycDict) obj).values();
      Iterator<PycObject> ki = keys.iterator();
      Iterator<PycObject> vi = values.iterator();
      while (ki.hasNext()) {
        output_object(ki.next(), mod, indent + 1);
        output_object(vi.next(), mod, indent + 2);
        // ++ki, ++vi;
      }
      iprintf(indent, "}\n");
    }
      break;
    case PycObject.Type.TYPE_SET: {
      iprintf(indent, "{\n");
      Set<PycObject> values = ((PycSet) obj).values();
      for (Iterator<PycObject> i = values.iterator(); i.hasNext();)
        output_object(i.next(), mod, indent + 1);
      iprintf(indent, "}\n");
    }
      break;
    case PycObject.Type.TYPE_NONE:
      iprintf(indent, "None\n");
      break;
    case PycObject.Type.TYPE_FALSE:
      iprintf(indent, "False\n");
      break;
    case PycObject.Type.TYPE_TRUE:
      iprintf(indent, "True\n");
      break;
    case PycObject.Type.TYPE_INT:
      iprintf(indent, "%d\n", ((PycInt) obj).value());
      break;
    case PycObject.Type.TYPE_LONG:
      iprintf(indent, "%s\n", ((PycLong) obj).repr());
      break;
    case PycObject.Type.TYPE_FLOAT:
      iprintf(indent, "%s\n", ((PycFloat) obj).value());
      break;
    case PycObject.Type.TYPE_COMPLEX:
      iprintf(indent, "(%s+%sj)\n", ((PycComplex) obj).value(), ((PycComplex) obj).imag());
      break;
    case PycObject.Type.TYPE_BINARY_FLOAT:
      iprintf(indent, "%g\n", ((PycCFloat) obj).value());
      break;
    case PycObject.Type.TYPE_BINARY_COMPLEX:
      iprintf(indent, "(%g+%gj)\n", ((PycCComplex) obj).value(), ((PycCComplex) obj).imag());
      break;
    default:
      iprintf(indent, "<TYPE: %d>\n", Integer.toString(obj.type()));
    }
  }

  public static String PATHSEP = System.getProperty("path.separator");

  public static String getDir() {
    // URL url = pycdas.class.getResource(".");
    URL url = pycdas.class.getClassLoader().getResource("");
    return url.getPath();
  }

  public static void main(String[] args) throws IOException {

    if (args.length < 1) {
      System.err.printf("No input file specified\n");
      return;
    }

    PycModule mod = new PycModule();
    mod.loadFromFile(getDir() + args[0]);
    int lastIndexOf = args[0].lastIndexOf(PATHSEP);
    String dispname = args[0];
    if (lastIndexOf >= 0)
      dispname = args[0].substring(args[1].lastIndexOf(PATHSEP));
    dispname = (dispname == null) ? args[1] : dispname;
    pyc_output.printf("%s (Python %d.%d%s)\n", dispname, mod.majorVer(), mod.minorVer(), (mod.majorVer() < 3 && mod.isUnicode()) ? " -U" : "");
    output_object(((PycObject) mod.code()), mod, 0);

    return;
  }
}
