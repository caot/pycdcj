package pydecompiler.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Pair<K, V> extends Entry<K, V> {
  public Pair(K k, V v) {
    super(k, v);
  }
}

// ref: HashMap.Entry
class Entry<K, V> implements Map.Entry<K, V> {
  final K key;
  V value;
  Entry<K, V> next;
  int hash;

  public Entry(K k, V v) {
    this(0, k, v, null);
  }

  Entry(int h, K k, V v, Entry<K, V> n) {
    value = v;
    next = n;
    key = k;
    hash = h;
  }

  public final K getKey() {
    return key;
  }

  public final V getValue() {
    return value;
  }

  public final V setValue(V newValue) {
    V oldValue = value;
    value = newValue;
    return oldValue;
  }

  public final boolean equals(Object o) {
    if (!(o instanceof Map.Entry))
      return false;
    Map.Entry e = (Map.Entry) o;
    Object k1 = getKey();
    Object k2 = e.getKey();
    if (k1 == k2 || (k1 != null && k1.equals(k2))) {
      Object v1 = getValue();
      Object v2 = e.getValue();
      if (v1 == v2 || (v1 != null && v1.equals(v2)))
        return true;
    }
    return false;
  }

  public final int hashCode() {
    if (hash == 0)
      hash = Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
    return hash;
  }

  public final String toString() {
    return getKey() + "=" + getValue();
  }

  /**
   * This method is invoked whenever the value in an entry is overwritten by an
   * invocation of put(k,v) for a key k that's already in the HashMap.
   */
  void recordAccess(HashMap<K, V> m) {
  }

  /**
   * This method is invoked whenever the entry is removed from the table.
   */
  void recordRemoval(HashMap<K, V> m) {
  }
}
