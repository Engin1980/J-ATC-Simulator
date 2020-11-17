package eng.newXmlUtils.utils;

import eng.eSystem.collections.*;
import eng.eSystem.utilites.ReflectionUtils;
import eng.newXmlUtils.EXmlException;
import eng.newXmlUtils.annotations.XmlConstructorParameter;
import eng.newXmlUtils.base.InstanceFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class InternalObjectUtils {

  private static final IMap<Class, Object> predefinedValues;
//  private static final IMap<Predicate<Class<?>>, Selector<String, Object>> predefinedParsers;//TODEL

  static {
    predefinedValues = new EMap<>();
    predefinedValues.set(byte.class, (byte) 0);
    predefinedValues.set(short.class, (short) 0);
    predefinedValues.set(long.class, 0l);
    predefinedValues.set(int.class, 0);
    predefinedValues.set(float.class, 0f);
    predefinedValues.set(double.class, 0d);
    predefinedValues.set(boolean.class, false);
    predefinedValues.set(char.class, '?');

    //TODEL
//    predefinedParsers = new EMap<>();
//    predefinedParsers.set(q -> byte.class.equals(q), q -> Byte.valueOf(q));
//    predefinedParsers.set(q -> short.class.equals(q), q -> Byte.valueOf(q));
//    predefinedParsers.set(q -> int.class.equals(q), q -> Byte.valueOf(q));
//    predefinedParsers.set(q -> long.class.equals(q), q -> Byte.valueOf(q));
//    predefinedParsers.set(q -> float.class.equals(q), q -> Byte.valueOf(q));
//    predefinedParsers.set(q -> double.class.equals(q), q -> Byte.valueOf(q));
//    predefinedParsers.set(q -> boolean.class.equals(q), q -> Byte.valueOf(q));
//    predefinedParsers.set(q -> char.class.equals(q), q -> Byte.valueOf(q));
//    predefinedParsers.set(q -> Byte.class.equals(q), q -> Byte.valueOf(q));
//    predefinedParsers.set(q -> Short.class.equals(q), q -> Byte.valueOf(q));
//    predefinedParsers.set(q -> Integer.class.equals(q), q -> Byte.valueOf(q));
//    predefinedParsers.set(q -> Long.class.equals(q), q -> Byte.valueOf(q));
//    predefinedParsers.set(q -> Float.class.equals(q), q -> Byte.valueOf(q));
//    predefinedParsers.set(q -> Double.class.equals(q), q -> Byte.valueOf(q));
//    predefinedParsers.set(q -> Boolean.class.equals(q), q -> Byte.valueOf(q));
//    predefinedParsers.set(q -> Character.class.equals(q), q -> Byte.valueOf(q));
//    predefinedParsers.set(q -> String.class.equals(q), q -> Byte.valueOf(q));
//    predefinedParsers.set(q -> q.isEnum(), q -> Byte.valueOf(q));
  }


  public static InstanceFactory<?> tryGetPublicConstructorFactory(Class<?> type) {
    InstanceFactory<?> ret;
    try {
      Constructor<?> ctor = type.getConstructor();
      ret = ((c) -> {
        try {
          return ctor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
          throw new EXmlException(sf("Failed to invoke public parameter-less constructor for type '%s'.", type), e);
        }
      });
    } catch (NoSuchMethodException e) {
      ret = null;
    }
    return ret;
  }

  public static InstanceFactory<?> tryGetAnnotatedConstructorFactory(Class<?> type) {
    InstanceFactory<?> ret;

    IList<Constructor<?>> ctors = new EList<>(type.getDeclaredConstructors());
    Constructor<?> ctor = ctors.tryGetFirst(q -> q.getDeclaredAnnotation(eng.newXmlUtils.annotations.XmlConstructor.class) != null);
    if (ctor != null) {

      Object[] ctorParams = analyseConstrutorParameters(ctor);

      ret = ((c) -> {
        try {
          ctor.setAccessible(true);
          Object tmp = ctor.newInstance(ctorParams);
          ctor.setAccessible(false);
          return tmp;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
          throw new EXmlException(sf("Failed to invoke private parameter-less @XmlConstructor for type '%s'.", type), e);
        }
      });
    } else
      ret = null;
    return ret;
  }

  private static Object[] analyseConstrutorParameters(Constructor<?> ctor) {
    Object[] ret = new Object[ctor.getParameterCount()];
    IReadOnlySet<XmlConstructorParameter> xmlParams = new ESet<>(ctor.getDeclaredAnnotationsByType(XmlConstructorParameter.class));

    for (int i = 0; i < ret.length; i++) {
      Parameter param = ctor.getParameters()[i];
      int ti = i;
      XmlConstructorParameter xmlParam = xmlParams.tryGetFirst(q -> q.index() == ti);
      Object value = xmlParam == null ? getDefaultValue(param) : getPredefinedValue(param, xmlParam);
      ret[i] = value;
    }
    return ret;
  }

  private static Object getPredefinedValue(Parameter param, XmlConstructorParameter xmlParam) {
    Object ret;
    Class<?> targetType = param.getType();
    if (ReflectionUtils.ClassUtils.isPrimitive(targetType))
      ret = invokeValueOf(ReflectionUtils.ClassUtils.tryWrapPrimitive(targetType), xmlParam.valueString());
    else if (ReflectionUtils.ClassUtils.isWrappedPrimitive(targetType))
      ret = invokeValueOf(targetType, xmlParam.valueString());
    else if (targetType.isEnum())
      ret = Enum.valueOf((Class<Enum>) targetType, xmlParam.valueString());
    else if (targetType.equals(String.class))
      ret = xmlParam.valueString();
    else
      throw new EXmlException(sf("Unable to parse custom-valued xml-constructor parameter '%s'. Don't know how.", param.getName()));
    return ret;
  }

  private static Object invokeValueOf(Class<?> wrappedType, String value) {
    Object ret;
    try {
      ret = wrappedType.getMethod("valueOf", String.class).invoke(null, value);
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new EXmlException(sf("Failed to find or invoke 'valueOf(...)' method of type '%s'.", wrappedType), e);
    }
    return ret;
  }

  private static Object getDefaultValue(Parameter param) {
    Object ret;
    if (ReflectionUtils.ClassUtils.isWrappedPrimitive(param.getType()))
      ret = predefinedValues.get(ReflectionUtils.ClassUtils.tryUnwapToPrimitive(param.getType()));
    else if (ReflectionUtils.ClassUtils.isPrimitive(param.getType()))
      ret = predefinedValues.get(param.getType());
    else
      ret = null;
    return ret;
  }
}
