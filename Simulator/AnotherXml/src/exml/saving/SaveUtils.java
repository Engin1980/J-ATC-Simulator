package exml.saving;

import eng.eSystem.utilites.ReflectionUtils;

class SaveUtils {

  public static boolean isTypeSame(Class<?> a, Class<?> b) {
    if (a.isPrimitive())
      if (b.isPrimitive())
        return a.equals(b);
      else
        return ReflectionUtils.ClassUtils.tryWrapPrimitive(a).equals(b);
    else if (b.isPrimitive())
      return a.equals(ReflectionUtils.ClassUtils.tryWrapPrimitive(b));
    else
      return a.equals(b);
  }
}
