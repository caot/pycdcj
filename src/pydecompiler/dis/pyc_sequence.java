package pydecompiler.dis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

abstract class PycSequence extends PycObject {
  PycSequence(int type) {
    super(type);
  }

  int size() {
    return m_size;
  }

  abstract PycObject get(int idx);

  // {
  // return null;
  // }

  int m_size;
}

class PycTuple extends PycSequence {
  private List<PycObject> m_values = new ArrayList<PycObject>();

  public PycTuple() {
    super(Type.TYPE_TUPLE);
  }

  public PycTuple(int type) {
    super(type);
  }

  // boolean isEqual(PycObject> obj);

  // void load(class PycData stream, class PycModule mod);
  void load(PycData stream, PycModule mod) throws IOException {
    m_size = stream.get32();
    // m_values.resize(m_size);
    for (int i = 0; i < m_size; i++)
      m_values.add(LoadObject(stream, mod));
  }

  boolean isEqual(PycObject obj) {
    if (type() != obj.type())
      return false;

    PycTuple tupleObj = (PycTuple) obj;
    if (m_size != tupleObj.m_size)
      return false;
    Iterator<PycObject> it1 = m_values.iterator();
    Iterator<PycObject> it2 = tupleObj.m_values.iterator();
    while (it1 != m_values.get(m_values.size() - 1)) {
      if (!it1.next().isEqual(it2.next()))
        return false;
      // ++it1, ++it2;
    }
    return true;
  }

  public List<PycObject> values() {
    return m_values;
  }

  public PycObject get(int idx) {
    if (m_values.size() == 0)
      return null;
    if (idx > m_values.size())
      idx = m_values.size() - 1;
    return m_values.get(idx);
  }

}

class PycList extends PycSequence {

  List<PycObject> value_t;

  PycList() {
    super(Type.TYPE_LIST);

  }

  PycList(int type) {
    super(type);
  }

  // boolean isEqual(PycObject obj);

  // void load(class PycData stream, class PycModule mod);
  void load(PycData stream, PycModule mod) throws IOException {
    m_size = stream.get32();
    for (int i = 0; i < m_size; i++)
      m_values.add(LoadObject(stream, mod));
  }

  boolean isEqual(PycObject obj) {
    if (type() != obj.type())
      return false;

    PycList listObj = (PycList) obj;
    if (m_size != listObj.m_size)
      return false;
    Iterator<PycObject> it1 = m_values.iterator();
    Iterator<PycObject> it2 = listObj.m_values.iterator();
    while (it1 != m_values.get(m_values.size() - 1)) {
      if (!it1.next().isEqual(it2.next()))
        return false;
      // ++it1, ++it2;
    }
    return true;
  }

  List<PycObject> values() {
    return m_values;
  }

  PycObject get(int idx) {
    Iterator<PycObject> it = m_values.iterator();
    for (int i = 0; i < idx; i++)
      it.next();
    return it.next();
  }

  private List<PycObject> m_values;
}

class PycDict extends PycSequence {

  List<PycObject> key_t;
  List<PycObject> value_t;

  PycDict() {
    super(Type.TYPE_DICT);
  }

  PycDict(int type) {
    super(type);
  }

  // bool isEqual(PycObject> obj);

  // void load(class PycData stream, class PycModule mod);

  // PycObject get(PycObject key);
  void load(PycData stream, PycModule mod) throws IOException {
    PycObject key, val;
    for (;;) {
      key = LoadObject(stream, mod);
      if (key == Pyc_NULL)
        break;
      val = LoadObject(stream, mod);
      m_keys.add(key);
      m_values.add(val);
    }
  }

  boolean isEqual(PycObject obj) {
    if (type() != obj.type())
      return false;

    PycDict dictObj = (PycDict) obj;
    if (m_size != dictObj.m_size)
      return false;

    Iterator<PycObject> ki1 = m_keys.iterator();
    Iterator<PycObject> ki2 = dictObj.m_keys.iterator();
    while (ki1 != m_keys.get(m_values.size() - 1)) {
      if (!ki1.next().isEqual(ki2.next()))
        return false;
      // ++ki1, ++ki2;
    }

    Iterator<PycObject> vi1 = m_values.iterator();
    Iterator<PycObject> vi2 = dictObj.m_values.iterator();
    while (vi1 != m_values.get(m_values.size() - 1)) {
      if (!vi1.next().isEqual(vi2.next()))
        return false;
      // ++vi1, ++vi2;
    }
    return true;
  }

  PycObject get(PycObject key) {
    Iterator<PycObject> ki = m_keys.iterator();
    Iterator<PycObject> vi = m_values.iterator();
    while (ki != m_keys.get(m_values.size() - 1)) {
      if (ki.next().isEqual(key))
        return vi.next();
      // ++ki, ++vi;
    }
    return Pyc_NULL; // Disassembly shouldn't get non-existant keys
  }

  List<PycObject> keys() {
    return m_keys;
  }

  List<PycObject> values() {
    return m_values;
  }

  PycObject get(int idx) {
    Iterator<PycObject> it = m_values.iterator();
    for (int i = 0; i < idx; i++)
      it.next();
    return it.next();
  }

  private List<PycObject> m_keys;
  private List<PycObject> m_values;
}

class PycSet extends PycSequence {
  private Set<PycObject> m_values;

  PycSet() {
    super(Type.TYPE_SET);
  }

  PycSet(int type) {
    super(type);
  }

  // boolean isEqual(PycObject obj);

  // void load(class PycData stream, class PycModule mod);
  void load(PycData stream, PycModule mod) throws IOException {
    m_size = stream.get32();
    for (int i = 0; i < m_size; i++)
      m_values.add(LoadObject(stream, mod));
  }

  boolean isEqual(PycObject obj) {
    if (type() != obj.type())
      return false;

    PycSet setObj = (PycSet) obj;
    if (m_size != setObj.m_size)
      return false;
    Iterator<PycObject> it1 = m_values.iterator();
    Iterator<PycObject> it2 = setObj.m_values.iterator();
    for (PycObject o = it1.next(); o != m_values.toArray(new PycObject[0])[m_values.size() - 1];) {
      if (!it1.next().isEqual(it2.next()))
        return false;
      // ++it1, ++it2;
    }
    return true;
  }

  Set<PycObject> values() {
    return m_values;
  }

  PycObject get(int idx) {
    Iterator<PycObject> it = m_values.iterator();
    for (int i = 0; i < idx; i++)
      it.next();

    return it.next();
  }

};

public class pyc_sequence {
}
