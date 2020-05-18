package eng.jAtcSim.newLib.shared.xml;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Action;
import eng.eSystem.functionalInterfaces.Action1;
import eng.eSystem.functionalInterfaces.Selector;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;


public class XmlLoaderUtils {

  public static <T> IList<T> loadList(XElement source, IList<T> target,
                                      Selector<XElement, Boolean> itemElementSelector,
                                      Selector<XElement, T> fromElementLoader) {
    for (XElement child : source.getChildren()) {
      if (itemElementSelector.getValue(child) == false) continue; // child item element not accepted
      T item = fromElementLoader.getValue(child);
      target.add(item);
    }
    return target;
  }

  public static <T> IList<T> loadList(XElement source, IList<T> target,
                                      Selector<XElement, T> fromElementLoader) {
    return loadList(source, target, e -> true, fromElementLoader);
  }

  public static <K, V> IMap<K, V> loadMap(XElement source, IMap<K, V> target,
                                          Selector<XElement, Boolean> entryElementSelector,
                                          Selector<XElement, K> keyLoader,
                                          Selector<XElement, V> valueLoader) {

    for (XElement child : source.getChildren()) {
      if (entryElementSelector.getValue(child) == false) continue; // child entry element not accepted
      K key = keyLoader.getValue(child);
      V value = valueLoader.getValue(child);
      target.set(key, value);
    }
    return target;
  }

  public static <K, V> IMap<K, V> loadMap(XElement source, IMap<K, V> target,
                                          Selector<XElement, K> keyLoader,
                                          Selector<XElement, V> valueLoader) {
    return loadMap(source, target, e -> true, keyLoader, valueLoader);
  }

  public static <T> T loadObject(XElement source, T target) {
    return loadObject(source, target, new EMap<>());
  }

  public static <T> T loadObject(XElement source, T target,
                                    IMap<String, Action1<XElement>> customLoaders){
    throw new ToDoException();
  }
}
