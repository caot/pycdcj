package pydecompiler.dis;

import java.io.IOException;
import java.io.PrintStream;

class PycString extends PycObject {
  private byte[] m_value = new byte[0];
  private int m_length;

  public PycString() {
    super(Type.TYPE_STRING);
  }

  public PycString(int type) {
    super(type);
  }

  int length() {
    return m_length;
  }

  String value() {
    if (m_value == null)
      return "";
    return new String(m_value);
  }

  void load(PycData stream, PycModule mod) throws IOException {
    if (m_value != null)
      m_value = null;

    if (type() == Type.TYPE_STRINGREF) {
      PycString str = mod.getIntern(stream.get32());
      m_length = str.length();
      if (m_length != 0) {
        // m_value = new byte[m_length + 1];
        m_value = str.value().getBytes();
        // m_value[m_length] = 0;
      } else {
        m_value = null;
      }
    } else {
      m_length = stream.get32();
      if (m_length != 0) {
        m_value = new byte[m_length];
        stream.getBuffer(m_length, m_value);
        // m_value[m_length] = 0;
      } else {
        m_value = null;
      }

      if (type() == Type.TYPE_INTERNED)
        mod.intern(this);
    }
  }

  boolean isEqual(PycObject obj) {
    if (type() != obj.type())
      return false;

    PycString strObj = (PycString) obj;
    return this.isEqual(new String(strObj.m_value));
  }

  boolean isEqual(String str) {
    return m_value.equals(str);
  }

  public static void OutputString(PycString str, char prefix) {
    OutputString(str, prefix, false, pyc_output);
  }

  static PrintStream pyc_output = PycData.pyc_output;
  public static void OutputString(PycString str, char prefix, boolean triple) {
    OutputString(str, prefix, triple, pyc_output);
  }
  
      

  public static void OutputString(PycString str, char prefix, boolean triple, PrintStream F) {
    if (prefix != 0)
      F.append(prefix);

    String ch_ = str.value();
    int i = 0;
    int len = str.length();
    if (ch_ == null) {
      F.printf("''");
      return;
    }

    char ch = 0;
    // Determine preferred quote style (Emulate Python's method)
    boolean useQuotes = false;
    while (len-- > 0) {
      ch = ch_.charAt(i);
      if (ch == '\'') {
        useQuotes = true;
      } else if (ch == '"') {
        useQuotes = false;
        break;
      }
      i++;
    }
    ch_ = str.value();
    len = str.length();

    // Output the string
    if (triple)
      F.printf(useQuotes ? "\"\"\"" : "'''");
    else
      F.append(useQuotes ? '"' : '\'');
    i = 0;
    while (len-- > 0) {
      ch = ch_.charAt(i);
      if (ch < 0x20 || ch == 0x7F) {
        if (ch == '\r') {
          F.printf("\\r");
        } else if (ch == '\n') {
          if (triple)
            F.append('\n');
          else
            F.printf("\\n");
        } else if (ch == '\t') {
          F.printf("\\t");
        } else {
          F.printf("\\x%x", (ch & 0xFF));
        }
      } else if (ch >= 0x80) {
        if (str.type() == Type.TYPE_UNICODE) {
          // Unicode stored as UTF-8... Let the stream interpret it
          F.append(ch);
        } else {
          F.printf("\\x%x", (ch & 0xFF));
        }
      } else {
        if (!useQuotes && ch == '\'')
          F.printf("\\'");
        else if (useQuotes && ch == '"')
          F.printf("\\\"");
        else if (ch == '\\')
          F.printf("\\\\");
        else
          F.append(ch);
      }
      i++;
    }
    if (triple)
      F.printf(useQuotes ? "\"\"\"" : "'''");
    else
      F.append(useQuotes ? '"' : '\'');
  }
}

public class pyc_string {

}
