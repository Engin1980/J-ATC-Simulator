package eng.jAtcSim.newLib.shared.xml;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.collections.IReadOnlyMap;
import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Action1;
import eng.eSystem.functionalInterfaces.Producer;
import eng.eSystem.functionalInterfaces.Selector;

import java.util.Map;

public class XmlSaverUtils {

  public interface ToElementSaver<TDataType>{
    void save(TDataType data, XElement element);
  }

  public static <K,V> void saveMap(IReadOnlyMap<K, V> data, XElement target,
                                   Selector<Map.Entry<K,V>, String> entryXmlNameSelector,
                                   ToElementSaver<K> keyToElementSaver,
                                   ToElementSaver<V> valueToElementSaver){
    for (Map.Entry<K, V> entry : data.getEntries()) {
      String entryElementName = entryXmlNameSelector.getValue(entry);
      XElement elm = new XElement(entryElementName);
      keyToElementSaver.save(entry.getKey(), elm);
      valueToElementSaver.save(entry.getValue(), elm);
      target.addElement(elm);
    }
  }

  public static <T> void saveList(IReadOnlyList<T> data, XElement target,
                                  Selector<T, String> itemXmlNameSelector,
                                  ToElementSaver<T> itemToElementSaver){
    for (T item : data) {
      String itemElementName = itemXmlNameSelector.getValue(item);
      XElement elm = new XElement(itemElementName);
      itemToElementSaver.save(item, elm);
      target.addElement(elm);
    }
  }

  public static <T> XElement saveObject(T data, String xmlElementName) {
    return saveObject(data, xmlElementName, new EMap<>());
  }

  public static <T> XElement saveObject(T data, String xmlElementName, IMap<String, Action1<XElement>> fieldCustomSavers) {

  }
}
