package pydecompiler.dis;

import java.io.IOException;
import java.io.PrintStream;


public class pycdc {
  static PrintStream pyc_output = PycData.pyc_output;

  public static void main(String[] args) throws IOException {
    if (args.length < 1) {
      System.err.printf("No input file specified\n");
      return;
    }

    PycModule mod = new PycModule();
    mod.loadFromFile(pycdas.getDir() + args[0]);
    int lastIndexOf = args[0].lastIndexOf(pycdas.PATHSEP);
    String dispname = args[0];
    if (lastIndexOf >= 0)
      dispname = args[0].substring(args[1].lastIndexOf(pycdas.PATHSEP));
    dispname = (dispname == null) ? args[1] : dispname;

    pyc_output.printf("# Source Generated with Decompyle++\n");
    pyc_output.printf("# File: %s (Python %d.%d%s)\n\n", dispname, mod.majorVer(), mod.minorVer(),
          (mod.majorVer() < 3 && mod.isUnicode()) ? " Unicode" : "");
    ASTree.decompyle(mod.code(), mod);
  }

}
