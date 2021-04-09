package exml.loading;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlyMap;
import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Producer;
import eng.eSystem.functionalInterfaces.Selector;
import exml.Values;
import exml.base.XContext;

public class XLoadContext extends XContext {

  private final IMap<Class<?>, Producer<?>> factories = new EMap<>();
  private final IMap<Class<?>, Selector<String, ?>> parsers = new EMap<>();
  private final IMap<Class<?>, Selector<XElement, ?>> deserializers = new EMap<>();
  private final Values parents = new Values();
  public final XLoadFieldContext fields = new XLoadFieldContext(this);
  public final XLoadObjectContext objects = new XLoadObjectContext(this);

  public void addDefaultParsers() {
    this.setParser(short.class, q -> Short.valueOf(q));
    this.setParser(byte.class, q -> Byte.valueOf(q));
    this.setParser(int.class, q -> Integer.valueOf(q));
    this.setParser(long.class, q -> Long.valueOf(q));
    this.setParser(float.class, q -> Float.valueOf(q));
    this.setParser(double.class, q -> Double.valueOf(q));
    this.setParser(boolean.class, q -> Boolean.valueOf(q));
    this.setParser(char.class, q -> q.charAt(0));
    this.setParser(Short.class, q -> Short.valueOf(q));
    this.setParser(Byte.class, q -> Byte.valueOf(q));
    this.setParser(Integer.class, q -> Integer.valueOf(q));
    this.setParser(Long.class, q -> Long.valueOf(q));
    this.setParser(Float.class, q -> Float.valueOf(q));
    this.setParser(Double.class, q -> Double.valueOf(q));
    this.setParser(Boolean.class, q -> Boolean.valueOf(q));
    this.setParser(Character.class, q -> q.charAt(0));
    this.setParser(String.class, q -> q);
  }

  public IReadOnlyMap<Class<?>, Selector<XElement, ?>> getDeserializers() {
    return deserializers;
  }

  public IReadOnlyMap<Class<?>, Producer<?>> getFactories() {
    return factories;
  }

  public Values getParents() {
    return parents;
  }

  public IReadOnlyMap<Class<?>, Selector<String, ?>> getParsers() {
    return parsers;
  }

  public void ignoreFields(Object obj, String... fieldNames) {
    this.fields.ignoreFields(obj, fieldNames);
  }

  public void loadItems(XElement elm, Object target, Class<?> expectedItemType) {
    this.objects.loadItems(elm, target, expectedItemType);
  }

  public Object loadObject(XElement elm) {
    return this.objects.loadObject(elm, null);
  }

  public <T> T loadObject(XElement elm, Class<T> type) {
    return this.objects.loadObject(elm, type);
  }

  public <T> void setDeserializer(Class<?> type, Selector<XElement, T> deserializer) {
    deserializers.set(type, deserializer);
  }

  public <T> void setFactory(Class<T> type, Producer<T> factory) {
    this.factories.set(type, factory);
  }

  public <T> void setParser(Class<T> type, Selector<String, T> parser) {
    this.parsers.set(type, parser);
  }

  public XLoadContext withDefaultParsers() {
    this.addDefaultParsers();
    return this;
  }
}
