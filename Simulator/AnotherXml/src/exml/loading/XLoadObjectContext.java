package exml.loading;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Selector;
import eng.eSystem.validation.EAssert;
import exml.Constants;
import exml.IXPersistable;
import exml.SharedUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class XLoadObjectContext {
  private final XLoadContext ctx;

  XLoadObjectContext(XLoadContext ctx) {
    this.ctx = ctx;
  }

  public <K, V> void loadEntries(XElement elm, Object targetMap, Class<K> expectedKeyType, Class<V> expectedValueType) {
    EAssert.Argument.isTrue(
            targetMap instanceof Map || targetMap instanceof IMap,
            "'targetMap' value must be instance of Map or IMap.");

    IMap<K, V> tmp = loadEntries(elm, expectedKeyType, expectedValueType);

    if (targetMap instanceof IMap)
      ((IMap) targetMap).setMany(tmp);
    else if (targetMap instanceof Map) {
      Map map = (Map) targetMap;
      map.putAll(tmp.toJavaMap());
    } else
      EAssert.fail();
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

  public <T> IList<T> loadItems(XElement elm, Class<? extends T> expectedItemType) {
    IList<T> ret = new EList<>();
    for (XElement childElement : elm.getChildren(Constants.ITEM_ELEMENT)) {
      T item = this.loadObject(childElement, expectedItemType);
      ret.add(item);
    }
    return ret;
  }

  /**
   * Load iterable into target list.
   *
   * @param elm              Source element with items
   * @param targetList       Target container. Must be ISet, Set, IList or List.
   * @param expectedItemType Expected type of one item.
   */
  public void loadItems(XElement elm, Object targetList, Class<?> expectedItemType) {
    EAssert.Argument.isTrue(targetList instanceof ISet || targetList instanceof IList || targetList instanceof List,
            "'targetList' must be an instance of ISet, Set, IList or List");
    Iterable<Object> items = loadItems(elm, expectedItemType);

    if (targetList instanceof ISet)
      ((ISet) targetList).addMany(items);
    else if (targetList instanceof Set) {
      for (Object item : items) {
        ((java.util.Set) targetList).add(item);
      }
    } else if (targetList instanceof IList)
      ((IList) targetList).addMany(items);
    else if (targetList instanceof java.util.List)
      for (Object item : items) {
        ((java.util.List) targetList).add(item);
      }
    else
      EAssert.fail();
  }

  public <T> T loadObject(XElement elm, Class<T> type) {
    ctx.log.log("%s (%s)", elm, type == null ? "?" : type.getSimpleName());
    if (elm.getContent().equals(Constants.NULL)) {
      return null;
    }

    ctx.log.increaseIndent();

    Object ret;
    Optional<Class<?>> tmp = tryLoadTypeFromElement(elm);
    if (tmp.isEmpty() && type == null)
      throw new XLoadException(sf("Type must be found in element, but there is no required attribute '%s'.", Constants.TYPE_ATTRIBUTE), ctx);
    else if (tmp.isEmpty() == false)
      type = (Class<T>) tmp.get();

    if (elm.getContent().equals(Constants.NULL)) {
      ret = null;
    } else if (ctx.deserializers.containsKey(type)) {
      Selector<XElement, T> deserializer = (Selector<XElement, T>) ctx.deserializers.get(type);
      XElement pce = this.ctx.currentElement;
      this.ctx.currentElement = elm;
      ret = deserializer.invoke(elm);
      this.ctx.currentElement = pce;
    } else if (ctx.parsers.containsKey(type)) {
      Selector<String, T> parser = (Selector<String, T>) ctx.parsers.get(type);
      ret = parser.invoke(elm.getContent());
    } else if (type.isEnum()) {
      ret = LoadUtils.loadEnum(elm.getContent(), type);
    } else if (type.isArray()) {
      IList<Object> lst = loadItems(elm, type.getComponentType());
      ret = SharedUtils.convertListToArray(lst, type);
    } else if (IXPersistable.class.isAssignableFrom(type)) {
      ret = loadPersistable(elm, type);
    } else {
      throw new XLoadException(sf("No deserializer/parser specified for type '%s' loaded from '%s'.", type, elm.toXPath()), ctx);
    }

    ctx.log.decreaseIndent();

    return (T) ret;
  }

  private <T> T loadPersistable(XElement elm, Class<T> type) {
    EAssert.Argument.isTrue(IXPersistable.class.isAssignableFrom(type));

    IXPersistable ret;
    ret = (IXPersistable) ConstructionUtils.provideInstance(type, ctx);
    ret.xLoad(elm, this.ctx);
    ctx.fields.loadAllRemaningFields(ret, elm);
    ret.xPostLoad(ctx);
    return (T) ret;
  }

  private Optional<Class<?>> tryLoadTypeFromElement(XElement elm) {
    Optional<String> typeName = elm.tryGetAttribute(Constants.TYPE_ATTRIBUTE);
    Optional<Class<?>> ret = typeName.map(q -> {
      try {
        return Class.forName(q);
      } catch (ClassNotFoundException e) {
        throw new XLoadException(sf("Failed to load type '%s' required to deserialize element '%s'.", typeName, elm.toXPath()), e, ctx);
      }
    });

    return ret;
  }
}
