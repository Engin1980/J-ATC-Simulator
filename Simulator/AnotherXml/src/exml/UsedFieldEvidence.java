package exml;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.ESet;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.ISet;
import eng.eSystem.utilites.ReflectionUtils;
import exml.annotations.XIgnored;

import java.lang.reflect.Modifier;

public class UsedFieldEvidence {
  private static ISet<String> getAllFieldsToPersist(Class<?> cls) {
    ISet<String> ret = ReflectionUtils.ClassUtils.getFields(cls)
            .where(q -> Modifier.isStatic(q.getModifiers()) == false)
            .where(q -> q.getAnnotationsByType(XIgnored.class).length == 0)
            .where(q -> q.getName().equals(Constants.INNER_CLASS_REFERENCE_FIELD_NAME) == false)
            .select(q -> q.getName())
            .toSet();
    return ret;
  }

  private final IMap<Object, ISet<String>> inner = new EMap<>();

  public void add(Object object, String... fieldNames) {
    for (String fieldName : fieldNames) {
      inner.getOrSet(object, () -> new ESet<>()).add(fieldName);
    }
  }

  public void add(Object object, Iterable<String> fieldNames) {
    for (String fieldName : fieldNames) {
      inner.getOrSet(object, () -> new ESet<>()).add(fieldName);
    }
  }

  public ISet<String> getRemainingFields(Object object) {
    ISet<String> ret = getAllFieldsToPersist(object.getClass());
    ISet<String> usedFields = inner.tryGet(object).orElseGet(() -> new ESet<>());
    ret.tryRemoveMany(usedFields);
    return ret;
  }

  public void reset(Object obj) {
    inner.getOrSet(obj, () -> new ESet<>()).clear();
  }
}
