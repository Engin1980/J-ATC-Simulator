package exml.saving.internal;

import eng.eSystem.collections.ESet;
import eng.eSystem.collections.ISet;
import eng.eSystem.utilites.ReflectionUtils;
import exml.saving.XSaveContext;
import exml.saving.XSaveException;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class RedundancyChecker {
  private final XSaveContext ctx;
  private final ISet<Object> processedObjects = new ESet<>();

  public RedundancyChecker(XSaveContext ctx) {
    this.ctx = ctx;
  }

  public void add(Object obj) {
    if (!isObjectToCheckCyclicSave(obj)) return;

    if (processedObjects.contains(obj))
      throw new XSaveException(sf("Object '%s' (%s) is already being saved (cyclic dependency).", obj, obj.getClass()), ctx);
    else
      processedObjects.add(obj);
  }

  public void remove(Object obj) {
    if (isObjectToCheckCyclicSave(obj))
      processedObjects.remove(obj);
  }

  private boolean isObjectToCheckCyclicSave(Object obj) {
    return obj != null
            && obj.getClass().isEnum() == false
            && (obj instanceof String) == false
            && ReflectionUtils.ClassUtils.isPrimitiveOrWrappedPrimitive(obj.getClass()) == false;
  }

}
