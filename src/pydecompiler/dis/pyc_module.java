package pydecompiler.dis;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

class PycMagic {
  public static final int MAGIC_1_0 = 0x00999902;
  public static final int MAGIC_1_1 = 0x00999903; /* Also covers 1.2 */
  public static final int MAGIC_1_3 = 0x0A0D2E89;
  public static final int MAGIC_1_4 = 0x0A0D1704;
  public static final int MAGIC_1_5 = 0x0A0D4E99;
  public static final int MAGIC_1_6 = 0x0A0DC4FC;

  public static final int MAGIC_2_0 = 0x0A0DC687;
  public static final int MAGIC_2_1 = 0x0A0DEB2A;
  public static final int MAGIC_2_2 = 0x0A0DED2D;
  public static final int MAGIC_2_3 = 0x0A0DF23B;
  public static final int MAGIC_2_4 = 0x0A0DF26D;
  public static final int MAGIC_2_5 = 0x0A0DF2B3;
  public static final int MAGIC_2_6 = 0x0A0DF2D1;
  public static final int MAGIC_2_7 = 0x0A0DF303;

  public static final int MAGIC_3_0 = 0x0A0D0C3A;
  public static final int MAGIC_3_1 = 0x0A0D0C4E;
  public static final int MAGIC_3_2 = 0x0A0D0C6C;
  public static final int MAGIC_3_3 = 0x0A0D0C9E;
}


class PycModule {
  int m_maj = -1;
  int m_min = -1;
  boolean m_unicode = false;
  PycCode m_code;
  List<PycString> m_interns = new LinkedList<PycString>();

  boolean isValid() {
    return (m_maj >= 0) && (m_min >= 0);
  }

  int majorVer() {
    return m_maj;
  }

  int minorVer() {
    return m_min;
  }

  int verCompare(int maj, int min) {
    if (m_maj == maj)
      return m_min - min;
    return m_maj - maj;
  }

  boolean isUnicode() {
    return m_unicode;
  }

  PycCode code() {
    return m_code;
  }

  void intern(PycString str) {
    m_interns.add(str);
  }

  private void setVersion(int magic) {
    // Default for versions that don't support unicode selection
    m_unicode = false;

    switch (magic) {
    case PycMagic.MAGIC_1_0:
      m_maj = 1;
      m_min = 0;
      break;
    case PycMagic.MAGIC_1_1:
      m_maj = 1;
      m_min = 1;
      break;
    case PycMagic.MAGIC_1_3:
      m_maj = 1;
      m_min = 3;
      break;
    case PycMagic.MAGIC_1_4:
      m_maj = 1;
      m_min = 4;
      break;
    case PycMagic.MAGIC_1_5:
      m_maj = 1;
      m_min = 5;
      break;

    /* Starting with 1.6, Python adds +1 for unicode mode (-U) */
    case PycMagic.MAGIC_1_6 + 1:
      m_unicode = true;
      /* Fall through */
    case PycMagic.MAGIC_1_6:
      m_maj = 1;
      m_min = 6;
      break;
    case PycMagic.MAGIC_2_0 + 1:
      m_unicode = true;
      /* Fall through */
    case PycMagic.MAGIC_2_0:
      m_maj = 2;
      m_min = 0;
      break;
    case PycMagic.MAGIC_2_1 + 1:
      m_unicode = true;
      /* Fall through */
    case PycMagic.MAGIC_2_1:
      m_maj = 2;
      m_min = 1;
      break;
    case PycMagic.MAGIC_2_2 + 1:
      m_unicode = true;
      /* Fall through */
    case PycMagic.MAGIC_2_2:
      m_maj = 2;
      m_min = 2;
      break;
    case PycMagic.MAGIC_2_3 + 1:
      m_unicode = true;
      /* Fall through */
    case PycMagic.MAGIC_2_3:
      m_maj = 2;
      m_min = 3;
      break;
    case PycMagic.MAGIC_2_4 + 1:
      m_unicode = true;
      /* Fall through */
    case PycMagic.MAGIC_2_4:
      m_maj = 2;
      m_min = 4;
      break;
    case PycMagic.MAGIC_2_5 + 1:
      m_unicode = true;
      /* Fall through */
    case PycMagic.MAGIC_2_5:
      m_maj = 2;
      m_min = 5;
      break;
    case PycMagic.MAGIC_2_6 + 1:
      m_unicode = true;
      /* Fall through */
    case PycMagic.MAGIC_2_6:
      m_maj = 2;
      m_min = 6;
      break;
    case PycMagic.MAGIC_2_7 + 1:
      m_unicode = true;
      /* Fall through */
    case PycMagic.MAGIC_2_7:
      m_maj = 2;
      m_min = 7;
      break;

    /* 3.0 and above are always unicode */
    case PycMagic.MAGIC_3_0 + 1:
      m_maj = 3;
      m_min = 0;
      m_unicode = true;
      break;
    case PycMagic.MAGIC_3_1 + 1:
      m_maj = 3;
      m_min = 1;
      m_unicode = true;
      break;

    /* 3.2 stops using the unicode increment */
    case PycMagic.MAGIC_3_2:
      m_maj = 3;
      m_min = 2;
      m_unicode = true;
      break;

    case PycMagic.MAGIC_3_3:
      m_maj = 3;
      m_min = 3;
      m_unicode = true;
      break;

    /* Bad Magic detected */
    default:
      m_maj = -1;
      m_min = -1;
    }
  }

  void loadFromFile(String filename) throws IOException {
    PycFile in = new PycFile(filename);
    if (!in.isOpen()) {
      System.err.printf("Error opening file %s\n", filename);
      return;
    }
    setVersion(in.get32());
    if (!isValid()) {
      System.err.printf("Bad MAGIC!\n");
      return;
    }
    in.get32(); // Timestamp -- who cares?

    if (verCompare(3, 3) >= 0)
      in.get32(); // Size parameter added in Python 3.3

    m_code = (PycCode) PycObject.LoadObject(in, this);
  }

  PycString getIntern(int ref) {
    Iterator<PycString> it = m_interns.iterator();
    for (int i = 0; i < ref; i++, it.next())
      /* move forward to ref */
      ;
    return it.next();
  }

}

public class pyc_module {

}
