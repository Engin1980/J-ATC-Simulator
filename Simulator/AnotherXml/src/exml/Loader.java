package exml;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Producer;
import eng.eSystem.functionalInterfaces.Selector;
import eng.eSystem.validation.EAssert;
import exml.annotations.XConstructor;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Optional;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class Loader {

  public final XContext ctx;
  public final IMap<Class<?>, Selector<XElement, ?>> deserializers = new EMap<>();
  public final IMap<Class<?>, Producer<?>> factories = new EMap<>();
  public final Values values = new Values();
  public final Values parents = new Values();

  private int indent = 0;
  private final IMap<Object, ISet<String>> usedFields = new EMap<>();
  private XElement currentElement;

  public Loader(XContext ctx) {
    this.ctx = ctx;
  }

  public void ignoreFields(Object obj, String... fieldNames) {
    this.usedFields.getOrSet(obj, () -> new ESet<>()).addMany(fieldNames);
  }

  public <K, V> IMap<K, V> loadEntries(XElement elm, Class<K> expectedKeyType, Class<V> expectedValueType) {
    IMap<K, V> ret = new EMap<>();
    for (XElement childElement : elm.getChildren(Constants.ENTRY_ELEMENT)) {
      XElement keyElement = childElement.getChild(Constants.KEY_ELEMENT);
      K key = this.loadObject(keyElement, expectedKeyType);

      XElement valueElement = childElement.getChild(Constants.VALUE_ELEMENT);
      V value = this.loadObject(valueElement, expectedValueType);

      ret.set(key, value);
    }

    return ret;
  }

  public void loadField(Object obj, String fieldName, XElement elm) {
    log("." + fieldName);
    indent++;
    FieldUtils.loadField(obj, fieldName, elm, this.ctx);
    usedFields.getOrSet(obj, () -> new ESet<>()).add(fieldName);
    indent--;
  }

  public void loadFieldItems(Object obj, String itemsFieldName, Object itemsContainer, Class<?> itemType, XElement elm) {
    FieldUtils.loadFieldItems(obj, itemsFieldName, itemsContainer, itemType, elm, ctx);
    usedFields.getOrSet(obj, () -> new ESet<>()).add(itemsFieldName);
  }

  public <T> IList<T> loadItems(XElement elm, Class<? extends T> expectedItemType) {
    IList<T> ret = new EList<>();
    for (XElement childElement : elm.getChildren(Constants.ITEM_ELEMENT)) {
      T item = this.loadObject(childElement, expectedItemType);
      ret.add(item);
    }
    return ret;
  }

  public void loadItems(XElement elm, Object target, Class<?> expectedItemType) {
    Iterable<Object> items = loadItems(elm, expectedItemType);

    if (target instanceof ISet)
      ((ISet) target).addMany(items);
    else if (target instanceof IList)
      ((IList) target).addMany(items);
    else if (target instanceof java.util.List)
      for (Object item : items) {
        ((java.util.List) target).add(item);
      }
    else
      throw new RuntimeException(sf("Loader does not support items-container-type %s", target.getClass()));
  }

  public <T> T loadObject(XElement elm, Class<T> type) {
    Object ret;

    log("%s (%s)", elm, type == null ? "?" : type.getSimpleName());
    indent++;

    Optional<Class<?>> tmp = tryLoadTypeFromElement(elm);
    if (tmp.isEmpty() && type == null)
      throw new SimPersistenceExeption(sf("Failed to load object type from element '%s'. Attribute '%s' is missing.", elm.toXPath(), Constants.TYPE_ATTRIBUTE));
    else if (tmp.isEmpty() == false)
      type = (Class<T>) tmp.get();

    if (elm.getContent().equals(Constants.NULL)) {
      ret = null;
    } else if (deserializers.containsKey(type)) {
      Selector<XElement, T> deserializer = (Selector<XElement, T>) deserializers.get(type);
      XElement pce = this.currentElement;
      this.currentElement = elm;
      ret = deserializer.invoke(elm);
      this.currentElement = pce;
    } else if (type.isEnum()) {
      ret = loadEnum(elm, type);
    } else if (type.isArray()) {
      IList<Object> lst = loadItems(elm, type.getComponentType());
      ret = convertListToArray(lst, type);
    } else if (IXPersistable.class.isAssignableFrom(type)) {
      ret = loadPersistable(elm, type);
    } else {
      throw new SimPersistenceExeption(sf("No deserializer specified to load an instance of of '%s' from '%s'.", type, elm.toXPath()));
    }

    indent--;

    return (T) ret;
  }

  public Object loadObject(XElement elm) {
    Object ret;
    if (elm.getContent().equals(Constants.NULL))
      return null;
    else {
      ret = loadObject(elm, null);
    }
    return ret;
  }

  public <T> void setDeserializer(Class<?> type, Selector<XElement, T> deserializer) {
    deserializers.set(type, deserializer);
  }

  public void setIgnoredFields(Object obj, String... fieldNames) {
    this.usedFields.getOrSet(obj, () -> new ESet<>()).addMany(fieldNames);
  }

  public <T> void setParser(Class<T> type, Selector<String, T> parser) {
    this.deserializers.set(type, e -> parser.invoke(e.getContent()));
  }

  private <T> T loadPersistable(XElement elm, Class<T> type) {
    EAssert.Argument.isTrue(IXPersistable.class.isAssignableFrom(type));

    IXPersistable ret;
    ret = (IXPersistable) provideInstance(type);
    ret.load(elm, this.ctx);
    fillInstance(ret, elm);
    ret.postLoad(ctx);
    return (T) ret;
  }

  private <T> Object convertListToArray(IList<Object> lst, Class<T> type) {
    Object ret = Array.newInstance(type.getComponentType(), lst.size());
    for (int i = 0; i < lst.size(); i++) {
      Object item = lst.get(i);
      Array.set(ret, i, item);
    }

    return ret;
  }

  private <T> Object loadEnum(XElement elm, Class<T> type) {
    Method parseMethod;
    Object ret;
    ISet<Method> methods = new ESet<>(type.getDeclaredMethods());
    parseMethod = methods
            .where(q -> q.getName().equals("parse"))
            .where(q -> Modifier.isPublic(q.getModifiers()) && Modifier.isStatic(q.getModifiers()))
            .where(q -> q.getParameterCount() == 1 && q.getParameters()[0].getType().equals(String.class))
            .tryGetFirst();
    if (parseMethod != null) {
      try {
        ret = parseMethod.invoke(null, elm.getContent());
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new SimPersistenceExeption(sf("Failed to 'parse' Enum type '%s' from value '%s'", type.getName(), elm.getContent()), e);
      }
    } else
      ret = Enum.valueOf((Class<Enum>) type, elm.getContent());
    return ret;
  }

  private Optional<Class<?>> tryLoadTypeFromElement(XElement elm) {
    String typeName = elm.tryGetAttribute(Constants.TYPE_ATTRIBUTE);
    Optional<Class<?>> ret;
    if (typeName == null) {
      ret = Optional.empty();
    } else {
      try {
        ret = Optional.of(Class.forName(typeName));
      } catch (ClassNotFoundException e) {
        throw new SimPersistenceExeption(sf("Failed to load type '%s' required to deserialize element '%s'.", typeName, elm.toXPath()), e);
      }
    }

    return ret;
  }

  private void fillInstance(Object obj, XElement elm) {
    EAssert.Argument.isNotNull(obj, "obj");
    EAssert.Argument.isNotNull(elm, "elm");

    ISet<String> remainingFields = FieldUtils.getRemainingFields(obj.getClass(), usedFields.tryGet(obj, new ESet<>()));

    for (String remainingField : remainingFields) {
      this.loadField(obj, remainingField, elm);
    }

    usedFields.getOrSet(obj, new ESet<>()).addMany(remainingFields);
  }

  private Object provideInstance(Class<?> type) {
    Object ret;
    if (type.equals(int.class) || type.equals(Integer.class))
      ret = Integer.MIN_VALUE;
    else if (type.equals(byte.class) || type.equals(Byte.class))
      ret = Byte.MIN_VALUE;
    else if (type.equals(short.class) || type.equals(Short.class))
      ret = Short.MIN_VALUE;
    else if (type.equals(long.class) || type.equals(Long.class))
      ret = Long.MIN_VALUE;
    else if (type.equals(float.class) || type.equals(Float.class))
      ret = Float.MIN_VALUE;
    else if (type.equals(double.class) || type.equals(Double.class))
      ret = Double.MIN_VALUE;
    else if (type.isEnum())
      ret = null;
    else
      ret = provideInstanceWithConstructor(type);

    return ret;
  }

  private <T> T provideInstanceWithConstructor(Class<T> type) {
    // 0. try get factory
    // 1. get annotated constructor
    // 2. get public parameterless constructor

    T ret;

    if (factories.containsKey(type))
      ret = getInstanceViaFactory(type);
    else {
      Constructor<T> ctor;
      ctor = tryGetAnnotatedConstructor(type);
      if (ctor == null)
        ctor = tryGetPublicParameterlessConstructor(type);
      if (ctor == null)
        throw new SimPersistenceExeption(sf("Unable to create an instance of '%s'.", type));
      ret = getInstanceViaConstructor(ctor);
    }
    return ret;
  }

  private <T> T getInstanceViaFactory(Class<T> type) {
    T ret;
    try {
      ret = (T) factories.get(type).invoke();
    } catch (Exception e) {
      throw new SimPersistenceExeption(sf("Failed to create a new instance of '%s' via custom factory.", type), e);
    }
    return ret;
  }

  private <T> T getInstanceViaConstructor(Constructor<T> ctor) {
    T ret;

    Object[] params = new Object[ctor.getParameterCount()];
    for (int i = 0; i < ctor.getParameterCount(); i++) {
      params[i] = getDefaultParameterValue(ctor, i);
    }

    try {
      ctor.setAccessible(true);
      ret = ctor.newInstance(params);
      ctor.setAccessible(false);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new SimPersistenceExeption(sf("Failed to create new instance of '%s' via constructor.", ctor.getDeclaringClass()));
    }

    return ret;
  }

  private <T> Object getDefaultParameterValue(Constructor<T> ctor, int index) {
    Class<?> type = ctor.getParameters()[index].getType();
    Object ret;

    if (type.equals(int.class) || type.equals(Integer.class))
      ret = Integer.MIN_VALUE;
    else if (type.equals(byte.class) || type.equals(Byte.class))
      ret = Byte.MIN_VALUE;
    else if (type.equals(short.class) || type.equals(Short.class))
      ret = Short.MIN_VALUE;
    else if (type.equals(long.class) || type.equals(Long.class))
      ret = Long.MIN_VALUE;
    else if (type.equals(float.class) || type.equals(Float.class))
      ret = Float.MIN_VALUE;
    else if (type.equals(double.class) || type.equals(Double.class))
      ret = Double.MIN_VALUE;
    else if (type.equals(boolean.class))
      ret = false;
    else if (type.equals(Boolean.class))
      ret = null;
    else if (type.equals(char.class))
      ret = 0;
    else if (type.equals(Character.class))
      ret = null;
    else if (type.equals(XContext.class))
      ret = this.ctx;
    else
      ret = null;

    return ret;
  }

  private <T> Constructor<T> tryGetPublicParameterlessConstructor(Class<T> type) {
    Constructor<T>[] ctors = (Constructor<T>[]) type.getDeclaredConstructors();
    Optional<Constructor<T>> ret = Arrays.stream(ctors)
            .filter(q -> q.getParameterCount() == 0)
            .filter(q -> Modifier.isPublic(q.getModifiers()))
            .findFirst();
    return ret.isEmpty() ? null : ret.get();
  }

  private <T> Constructor<T> tryGetAnnotatedConstructor(Class<T> type) {
    Constructor<T>[] ctors = (Constructor<T>[]) type.getDeclaredConstructors();
    Optional<Constructor<T>> ret = Arrays.stream(ctors).filter(q -> q.getAnnotation(XConstructor.class) != null).findFirst();
    return ret.isEmpty() ? null : ret.get();
  }

  private void log(String s, Object... params) {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < indent; i++) {
      sb.append(" ");
    }

    sb.append(String.format(s, params));

    System.out.println(sb.toString());
  }
}
