package pydecompiler.dis;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

class PycInt extends PycObject {
  PycInt() {
    super(Type.TYPE_INT);
  }

  PycInt(int value) {
    this();
    this.m_value = value;
  }

  boolean isEqual(PycObject obj) {
    return type() == ((PycInt) obj).type() && m_value == ((PycInt) obj).m_value;
  }

  // void load(class PycData stream, PycModule mod);
  void load(PycData stream, PycModule mod) throws IOException {
    m_value = stream.get32();
  }

  int value() {
    return m_value;
  }

  int m_value;
}

class PycLong extends PycObject {
  PycLong() {
    super(Type.TYPE_LONG);
  }

  PycLong(int type) {
    super(type);
  }

  // boolean isEqual(PycRef<PycObject> obj);
  //
  // void load(class PycData stream, class PycModule mod);

  int size() {
    return m_size;
  }

  List<Integer> value() {
    return m_value;
  }

  void load(PycData stream, PycModule mod) throws IOException {
    if (type() == Type.TYPE_INT64) {
      int lo = stream.get32();
      int hi = stream.get32();
      m_value.add((lo) & 0xFFFF);
      m_value.add((lo >> 16) & 0xFFFF);
      m_value.add((hi) & 0xFFFF);
      m_value.add((hi >> 16) & 0xFFFF);
      m_size = (hi & 0x80000000) != 0 ? -4 : 4;
    } else {
      m_size = stream.get32();
      int actualSize = m_size >= 0 ? m_size : -m_size;
      for (int i = 0; i < actualSize; i++)
        m_value.add(stream.get16());
    }
  }

  boolean isEqual(PycObject obj) {
    if (type() != obj.type())
      return false;

    PycLong longObj = (PycLong) obj;
    if (m_size != longObj.m_size)
      return false;
    Iterator<Integer> it1 = m_value.iterator();
    Iterator<Integer> it2 = longObj.m_value.iterator();
    for (Integer i = it1.next(); i != m_value.get(m_value.size() - 1);) {
      if (it1.next() != it2.next())
        return false;
      // ++it1, ++it2;
    }
    return true;
  }

  String repr() {
    // Longs are printed as hex, since it's easier (and faster) to convert
    // arbitrary-length integers to a power of two than an arbitrary base

    if (m_size == 0)
      return "0x0L";

    // Realign to 32 bits, since Python uses only 15
    ArrayList<Integer> bits = new ArrayList<Integer>();
    Integer bit;
    ListIterator<Integer> iter;
    int shift = 0, temp = 0;
    for (iter = m_value.listIterator(); iter.hasNext() && (bit = iter.next()) != m_value.get(m_value.size() - 1);) {
      temp |= bit << shift;
      shift += 15;
      if (shift >= 32) {
        bits.add(temp);
        shift -= 32;
        temp = bit >> (15 - shift);
      }
    }
    if (temp != 0)
      bits.add(temp);

    char[] accum = new char[3 + (bits.size() * 8) + 2];
    int aptr = 0;

    if (m_size < 0)
      accum[aptr++] = '-';
    accum[aptr++] = '0';
    accum[aptr++] = 'x';

    iter = bits.listIterator(bits.size());

    // aptr += snprintf(aptr, 9, "%X", iter.previous());
    // while (iter.hasPrevious())
    // aptr += snprintf(aptr, 9, "%08X", iter.previous());
    accum[aptr++] = 'L';
    accum[aptr++] = 0;
    return accum.toString();
  }

  private int m_size;
  private List<Integer> m_value;
}

class PycFloat extends PycObject {
  public PycFloat() {
    super(Type.TYPE_FLOAT);
  }

  public PycFloat(int type) {
    super(type);
  }

  PycFloat(String value) {
    this();
    this.m_value = value.getBytes();
  }

  // bool isEqual(PycRef<PycObject> obj);
  //
  // void load(class PycData stream, class PycModule mod);
  void load(PycData stream, PycModule mod) throws IOException {
    int len = stream.getByte();
    if (m_value != null)
      m_value = null;
    if (len > 0) {
      m_value = new byte[len + 1];
      stream.getBuffer(len, m_value);
      m_value[len] = 0;
    } else {
      m_value = null;
    }
  }

  boolean isEqual(PycObject obj) {
    if (type() != obj.type())
      return false;

    PycFloat floatObj = (PycFloat) obj;
    if (m_value == floatObj.m_value)
      return true;
    return m_value.equals(floatObj.m_value);
  }

  String value() {
    return new String(m_value);
  }

  private byte[] m_value; // Floats are stored as strings
}

class PycComplex extends PycFloat {
  public PycComplex() {
    super(Type.TYPE_COMPLEX);
  }

  // ~PycComplex() { if (m_imag) delete[] m_imag; }

  // bool isEqual(PycRef<PycObject> obj);

  // void load(class PycData stream, class PycModule mod);
  void load(PycData stream, PycModule mod) throws IOException {
    super.load(stream, mod);

    int len = stream.getByte();
    if (m_imag != null)
      m_imag = null;
    if (len > 0) {
      m_imag = new byte[len + 1];
      stream.getBuffer(len, m_imag);
      m_imag[len] = 0;
    } else {
      m_imag = null;
    }
  }

  boolean isEqual(PycObject obj) {
    if (!super.isEqual(obj))
      return false;

    PycComplex floatObj = (PycComplex) obj;
    if (m_imag == floatObj.m_imag)
      return true;
    return m_imag.equals(floatObj.m_imag);
  }

  String imag() {
    return new String(m_imag);
  }

  private byte[] m_imag;
}

class PycCFloat extends PycObject {
  public PycCFloat() {
    super(Type.TYPE_BINARY_FLOAT);
  }

  public PycCFloat(int type) {
    super(type);
    m_value = 0.0;
  }

  boolean isEqual(PycObject obj) {
    return type() == obj.type() && m_value == ((PycCFloat) obj).m_value;
  }

  void load(PycData stream, PycModule mod) throws IOException {
    //long i64 = stream.get64();
    // memcpy(&m_value, &i64, sizeof(Pyc_INT64));
    
    byte[] i64 = stream.get64a();
    ByteBuffer buf = ByteBuffer.allocate(8).put(i64);
    // Flips this buffer.  The limit is set to the current position and then
    // the position is set to zero.
    buf.flip();
    /* Ensure endianness */
    buf.order(buf.order() == ByteOrder.BIG_ENDIAN ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
    m_value = buf.getDouble();
  }

  double value() {
    return m_value;
  }

  private double m_value;
}

class PycCComplex extends PycCFloat {
  public PycCComplex() {
    super(Type.TYPE_BINARY_COMPLEX);
  }

  public PycCComplex(int type) {
    super(type);
  }

  boolean isEqual(PycObject obj) {
    return super.isEqual(obj) && m_imag == ((PycCComplex) obj).m_imag;
  }

  // void load(class PycData stream, class PycModule mod);

  void load(PycData stream, PycModule mod) throws IOException {
    super.load(stream, mod);
    m_imag = (double) stream.get64();
  }

  double imag() {
    return m_imag;
  }

  private double m_imag;
}

public class pyc_numeric {

}
