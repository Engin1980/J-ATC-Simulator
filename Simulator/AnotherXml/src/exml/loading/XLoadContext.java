package exml.loading;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Producer;
import eng.eSystem.functionalInterfaces.Selector;
import exml.Log;
import exml.Values;

public class XLoadContext {

  protected final Log log = new Log();
  protected final IMap<Class<?>, Producer<?>> factories = new EMap<>();
  protected final IMap<Class<?>, Selector<String, ?>> parsers = new EMap<>();
  protected final IMap<Class<?>, Selector<XElement, ?>> deserializers = new EMap<>();
  public final Values values = new Values();
  public final Values parents = new Values();
  public final XLoadFieldContext fields= new XLoadFieldContext(this);
  public final XLoadObjectContext objects = new XLoadObjectContext(this);
  protected XElement currentElement;
  protected Object currentObject;

  public void ingoreFields(Object obj, String... fieldNames) {
    this.fields.ignoreFields(obj, fieldNames);
  }

  public void loadItems(XElement elm, Object target, Class<?> expectedItemType) {
    this.objects.loadItems(elm, target, expectedItemType);
  }

  public <T> T loadObject(XElement elm, Class<T> type) {
    return this.objects.loadObject(elm, type);
  }

  public Object loadObject(XElement elm) {
    return this.objects.loadObject(elm, null);
  }

  public <T> void setDeserializer(Class<?> type, Selector<XElement, T> deserializer) {
    deserializers.set(type, deserializer);
  }

  public <T> void setParser(Class<T> type, Selector<String, T> parser) {
    this.parsers.set(type, parser);
  }


}
