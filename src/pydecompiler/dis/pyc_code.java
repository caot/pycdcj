package pydecompiler.dis;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

class PycCode extends PycObject {

  public static class CodeFlags {
    public static final int CO_OPTIMIZED = 0x1;
    public static final int CO_NEWLOCALS = 0x2;
    public static final int CO_VARARGS = 0x4;
    public static final int CO_VARKEYWORDS = 0x8;
    public static final int CO_NESTED = 0x10;
    public static final int CO_GENERATOR = 0x20;
    public static final int CO_NOFREE = 0x40;
    public static final int CO_GENERATOR_ALLOWED = 0x1000;
    public static final int CO_FUTURE_DIVISION = 0x2000;
    public static final int CO_FUTURE_ABSOLUTE_IMPORT = 0x4000;
    public static final int CO_FUTURE_WITH_STATEMENT = 0x8000;
    public static final int CO_FUTURE_PRINT_FUNCTION = 0x10000;
    public static final int CO_FUTURE_UNICODE_LITERALS = 0x20000;
    public static final int CO_FUTURE_BARRY_AS_BDFL = 0x40000;
  };

  PycCode() {
    super(Type.TYPE_CODE);
    m_globalsUsed = new HashSet<PycObject>();
  }

  PycCode(int type) {
    super(type);
  }

  int argCount() {
    return m_argCount;
  }

  int kwOnlyArgCount() {
    return m_kwOnlyArgCount;
  }

  int numLocals() {
    return m_numLocals;
  }

  int stackSize() {
    return m_stackSize;
  }

  int flags() {
    return m_flags;
  }

  PycString code() {
    return m_code;
  }

  PycSequence consts() {
    return m_consts;
  }

  PycSequence names() {
    return m_names;
  }

  PycSequence varNames() {
    return m_varNames;
  }

  PycSequence freeVars() {
    return m_freeVars;
  }

  PycSequence cellVars() {
    return m_cellVars;
  }

  PycString fileName() {
    return m_fileName;
  }

  PycString name() {
    return m_name;
  }

  int firstLine() {
    return m_firstLine;
  }

  PycString lnTable() {
    return m_lnTable;
  }

  PycObject getConst(int idx) {
    return m_consts.get(idx);
  }

  PycString getName(int idx) {
    return (PycString) m_names.get(idx);
  }

  PycString getVarName(int idx) {
    return (PycString) m_varNames.get(idx);
  }

  PycObject getCellVar(int idx) {
    return (idx >= m_cellVars.size()) ? m_freeVars.get(idx - m_cellVars.size()) : m_cellVars.get(idx);
  }

  Set<PycObject> getGlobals() {
    return m_globalsUsed;
  }

  void markGlobal(PycString varname) {
    m_globalsUsed.add(varname);
    // m_globalsUsed.unique();
  }

  void load(PycData stream, PycModule mod) throws IOException {
    if (mod.verCompare(1, 3) >= 0 && mod.verCompare(2, 3) < 0)
      m_argCount = stream.get16();
    else if (mod.verCompare(2, 3) >= 0)
      m_argCount = stream.get32();

    if (mod.majorVer() >= 3)
      m_kwOnlyArgCount = stream.get32();

    if (mod.verCompare(1, 3) >= 0 && mod.verCompare(2, 3) < 0)
      m_numLocals = stream.get16();
    else if (mod.verCompare(2, 3) >= 0)
      m_numLocals = stream.get32();

    if (mod.verCompare(1, 5) >= 0 && mod.verCompare(2, 3) < 0)
      m_stackSize = stream.get16();
    else if (mod.verCompare(2, 3) >= 0)
      m_stackSize = stream.get32();

    if (mod.verCompare(1, 3) >= 0 && mod.verCompare(2, 3) < 0)
      m_flags = stream.get16();
    else if (mod.verCompare(2, 3) >= 0)
      m_flags = stream.get32();

    m_code = (PycString) LoadObject(stream, mod);
    m_consts = (PycTuple) LoadObject(stream, mod);
    m_names = (PycTuple) LoadObject(stream, mod);

    if (mod.verCompare(1, 3) >= 0)
      m_varNames = (PycTuple) LoadObject(stream, mod);

    if (mod.verCompare(2, 1) >= 0)
      m_freeVars = (PycTuple) LoadObject(stream, mod);

    if (mod.verCompare(2, 1) >= 0)
      m_cellVars = (PycTuple) LoadObject(stream, mod);

    m_fileName = (PycString) LoadObject(stream, mod);
    m_name = (PycString) LoadObject(stream, mod);

    if (mod.verCompare(1, 5) >= 0 && mod.verCompare(2, 3) < 0)
      m_firstLine = stream.get16();
    else if (mod.verCompare(2, 3) >= 0)
      m_firstLine = stream.get32();

    if (mod.verCompare(1, 5) >= 0)
      m_lnTable = (PycString) LoadObject(stream, mod);
  }

  int m_argCount, m_kwOnlyArgCount, m_numLocals, m_stackSize, m_flags;
  PycString m_code;
  PycSequence m_consts;
  PycSequence m_names;
  PycSequence m_varNames;
  PycSequence m_freeVars;
  PycSequence m_cellVars;
  PycString m_fileName;
  PycString m_name;
  int m_firstLine;
  PycString m_lnTable;
  Set<PycObject> m_globalsUsed; /* Global vars used in this code */
}

public class pyc_code {

}
