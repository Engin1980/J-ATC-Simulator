package exml.saving.internal;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.utilites.ReflectionUtils;
import exml.saving.XSaveContext;
import exml.saving.XSaveException;

import java.util.Objects;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class RedundancyChecker {
  private static class Record {
    public final String key;
    public final Object object;

    public Record(String key, Object object) {
      this.key = key;
      this.object = object;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Record record = (Record) o;
      return key.equals(record.key) && object.equals(record.object);
    }

    @Override
    public int hashCode() {
      return Objects.hash(key, object);
    }
  }

  private final XSaveContext ctx;
  private final IList<Record> processedObjects = new EList<>();

  public RedundancyChecker(XSaveContext ctx) {
    this.ctx = ctx;
  }

  public void add(String key, Object obj) {
    if (!isObjectToCheckCyclicSave(obj)) return;

    Record r = new Record(key, obj);
    if (processedObjects.contains(r)) {
      StringBuilder sb = new StringBuilder();
      for (Record processedObject : processedObjects) {
        if (sb.length() > 0)
          sb.append(" ==> ");
        sb.append(sf("[%s] %s", processedObject.key, processedObject.object));
      }
      throw new XSaveException(sf("Object '%s' (%s) is already being saved (cyclic dependency). Sequence: %s",
              obj,
              obj.getClass(),
              sb.toString()), ctx);
    } else
      processedObjects.add(r);
  }

  public void remove(String key, Object obj) {
    if (isObjectToCheckCyclicSave(obj)) {
      Record r = processedObjects.getFirst(q -> q.key.equals(key) && q.object == obj);
      processedObjects.remove(r);
    }
  }

  private boolean isObjectToCheckCyclicSave(Object obj) {
    return obj != null
            && obj.getClass().isEnum() == false
            && (obj instanceof String) == false
            && ReflectionUtils.ClassUtils.isPrimitiveOrWrappedPrimitive(obj.getClass()) == false;
  }

}
