package pydecompiler.util;

public class Logger {
  public static void log(Object... err) {
    StringBuilder strb = new StringBuilder();
    strb.append(new Exception().getStackTrace()[1].toString().replace("(", " ("));
    String info = strb.toString();
    strb.delete(0, strb.length());
    for (Object str : err)
      strb.append(str);
    strb.append("    ").append(info);
    strb.append("\n");
    System.err.printf(strb.toString());
  }
}
