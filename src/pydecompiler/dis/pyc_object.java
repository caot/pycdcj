package pydecompiler.dis;

import java.io.IOException;

class PycObject {
  public static class Type {
    // From the Python Marshallers
    public static final char TYPE_NULL = '0';
    public static final char TYPE_NONE = 'N';
    public static final char TYPE_FALSE = 'F';
    public static final char TYPE_TRUE = 'T';
    public static final char TYPE_STOPITER = 'S';
    public static final char TYPE_ELLIPSIS = '.';
    public static final char TYPE_INT = 'i';
    public static final char TYPE_INT64 = 'I';
    public static final char TYPE_FLOAT = 'f';
    public static final char TYPE_BINARY_FLOAT = 'g';
    public static final char TYPE_COMPLEX = 'x';
    public static final char TYPE_BINARY_COMPLEX = 'y';
    public static final char TYPE_LONG = 'l';
    public static final char TYPE_STRING = 's';
    public static final char TYPE_INTERNED = 't';
    public static final char TYPE_STRINGREF = 'R';
    public static final char TYPE_TUPLE = '(';
    public static final char TYPE_LIST = '[';
    public static final char TYPE_DICT = '{';
    public static final char TYPE_CODE = 'c';
    public static final char TYPE_CODE2 = 'C'; // Used in Python 1.0 - 1.2
    public static final char TYPE_UNICODE = 'u';
    public static final char TYPE_UNKNOWN = '?';
    public static final char TYPE_SET = '<';
    public static final char TYPE_FROZENSET = '>';
  };

  public PycObject() {
    m_type = Type.TYPE_UNKNOWN;
  }

  public PycObject(int type) {
    m_type = type;
  }

  public int type() {
    return m_type;
  }

  boolean isEqual(PycObject obj) {
    return this.equals(obj);
  }

  void load(PycData data, PycModule mod) throws IOException {
  }

  private int m_type = PycObject.Type.TYPE_NULL;

  public static PycObject Pyc_NULL = null;
  public static PycObject Pyc_None = new PycObject(PycObject.Type.TYPE_NONE);
  public static PycObject Pyc_Ellipsis = new PycObject(PycObject.Type.TYPE_ELLIPSIS);
  public static PycObject Pyc_StopIteration = new PycObject(PycObject.Type.TYPE_STOPITER);
  public static PycObject Pyc_False = new PycObject(PycObject.Type.TYPE_FALSE);
  public static PycObject Pyc_True = new PycObject(PycObject.Type.TYPE_TRUE);

  static PycObject CreateObject(int type) {
    switch (type) {
    case PycObject.Type.TYPE_NULL:
      return Pyc_NULL;
    case PycObject.Type.TYPE_NONE:
      return Pyc_None;
    case PycObject.Type.TYPE_FALSE:
      return Pyc_False;
    case PycObject.Type.TYPE_TRUE:
      return Pyc_True;
    case PycObject.Type.TYPE_STOPITER:
      return Pyc_StopIteration;
    case PycObject.Type.TYPE_ELLIPSIS:
      return Pyc_Ellipsis;
    case PycObject.Type.TYPE_INT:
      return new PycInt();
    case PycObject.Type.TYPE_INT64:
      return new PycLong(PycObject.Type.TYPE_INT64);
    case PycObject.Type.TYPE_FLOAT:
      return new PycFloat();
    case PycObject.Type.TYPE_BINARY_FLOAT:
      return new PycCFloat();
    case PycObject.Type.TYPE_COMPLEX:
      return new PycComplex();
    case PycObject.Type.TYPE_BINARY_COMPLEX:
      return new PycCComplex();
    case PycObject.Type.TYPE_LONG:
      return new PycLong();
    case PycObject.Type.TYPE_STRING:
      return new PycString();
    case PycObject.Type.TYPE_INTERNED:
      return new PycString(PycObject.Type.TYPE_INTERNED);
    case PycObject.Type.TYPE_STRINGREF:
      return new PycString(PycObject.Type.TYPE_STRINGREF);
    case PycObject.Type.TYPE_TUPLE:
      return new PycTuple();
    case PycObject.Type.TYPE_LIST:
      return new PycList();
    case PycObject.Type.TYPE_DICT:
      return new PycDict();
    case PycObject.Type.TYPE_CODE:
    case PycObject.Type.TYPE_CODE2:
      return new PycCode();
    case PycObject.Type.TYPE_UNICODE:
      return new PycString(PycObject.Type.TYPE_UNICODE);
    case PycObject.Type.TYPE_SET:
      return new PycSet();
    case PycObject.Type.TYPE_FROZENSET:
      return new PycSet(PycObject.Type.TYPE_FROZENSET);
    default:
      System.err.printf("CreateObject: Got unsupported type 0x%X\n", type);
      return Pyc_NULL;
    }
  }

  public static PycObject LoadObject(PycData stream, PycModule mod) throws IOException {
    PycObject obj = CreateObject(stream.getByte());
    if (obj != Pyc_NULL) {

      obj.load(stream, mod);
    }
    return obj;
  }

}

public class pyc_object {

}
