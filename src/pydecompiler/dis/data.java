package pydecompiler.dis;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

interface PycDataI {
  boolean isOpen() throws IOException;

  boolean atEof() throws IOException;

  int getByte() throws IOException;

  int getBuffer(int bytes, byte[] buffer) throws IOException;
}

abstract class PycData implements PycDataI {
  public static PrintStream pyc_output = new PrintStream(System.out);

  int get16() throws IOException {
    /* Ensure endianness */
    int result = getByte() & 0xFF;
    result |= (getByte() & 0xFF) << 8;
    return result;
  }

  int get32() throws IOException {
    /* Ensure endianness */
    int result = getByte() & 0xFF;
    result |= (getByte() & 0xFF) << 8;
    result |= (getByte() & 0xFF) << 16;
    result |= (getByte() & 0xFF) << 24;
    return result;
  }

  // Pyc_INT64 get64()
  long get64() throws IOException {
    /* Ensure endianness */
    long result = getByte() & 0xFF;
    result |= (getByte() & 0xFF) << 8;
    result |= (getByte() & 0xFF) << 16;
    result |= (getByte() & 0xFF) << 24;
    result |= (getByte() & 0xFF) << 32;
    result |= (getByte() & 0xFF) << 40;
    result |= (getByte() & 0xFF) << 48;
    result |= (getByte() & 0xFF) << 56;
    return result;
  }
};

class PycFile extends PycData {
  private InputStream m_stream;

  public void _PycFile() throws IOException {
    if (m_stream != null)
      m_stream.close();
  }

  public boolean isOpen() {
    return (m_stream != null);
  }

  PycFile(String filename) throws IOException {
    m_stream = new FileInputStream(filename);
  }

  public boolean atEof() throws IOException {
    int ch = m_stream.read();
    return ch == -1;
  }

  public int getByte() throws IOException {
    int ch = (byte) m_stream.read();
    return ch;
  }

  public int getBuffer(int bytes, byte[] buffer) throws IOException {
    // read(byte[] b, int off, int len)

    return (int) m_stream.read(buffer, 0, bytes); // /?????
  }
};

class PycBuffer extends PycData {

  public PycBuffer(byte[] buffer, int size) {
    this.m_buffer = buffer;
    this.m_size = size;
  }

  public boolean isOpen() {
    return (m_buffer != null);
  }

  public boolean atEof() {
    return (m_pos == m_size);
  }

  public static int EOF = -1;

  // public int getByte();
  // public int getBuffer(int bytes, void* buffer);

  public int getByte() {
    if (atEof())
      return -1;
    int ch = m_buffer[m_pos];
    ++m_pos;
    return (ch & 0xFF); // Make sure it's just a byte!
  }

  public int getBuffer(int bytes, byte[] buffer) {
    if (m_pos + bytes > m_size)
      bytes = m_size - m_pos;
    // System.arraycopy(buffer, (m_buffer + m_pos), bytes); ??????????
    return bytes;
  }

  private byte[] m_buffer;
  private int m_size, m_pos;
};

// extern FILE* pyc_output;

public class data {

}
