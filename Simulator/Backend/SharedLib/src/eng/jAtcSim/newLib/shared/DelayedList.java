package eng.jAtcSim.newLib.shared;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.contextLocal.Context;
import eng.jAtcSimLib.xmlUtils.Deserializer;
import eng.jAtcSimLib.xmlUtils.Serializer;
import eng.jAtcSimLib.xmlUtils.XmlLoadUtils;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;
import eng.jAtcSimLib.xmlUtils.deserializers.ItemsDeserializer;
import eng.jAtcSimLib.xmlUtils.deserializers.ObjectDeserializer;
import eng.jAtcSimLib.xmlUtils.serializers.ItemsSerializer;
import eng.jAtcSimLib.xmlUtils.serializers.ObjectSerializer;
import eng.newXmlUtils.XmlContext;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Predicate;

public class DelayedList<T> {

  private static class DelayedItem<T> {
    public final T item;
    public int delayLeft;

    private DelayedItem(T item, int delay) {
      this.item = item;
      this.delayLeft = delay;
    }
  }

  private final int minimalDelay;
  private final int maximalDelay;
  private int currentDelay = 0;
  private final IList<DelayedItem<T>> inner = new EList<>(LinkedList.class);

  public DelayedList(int minimalDelay, int maximalDelay) {
    this.minimalDelay = minimalDelay;
    this.maximalDelay = maximalDelay;
    newRandomDelay();
  }

  /**
   * Adds item with random delay
   *
   * @param item<T> Item to add
   */
  public void add(T item) {
    int minDelay = Math.min(getLastDelay(maximalDelay), currentDelay);
    DelayedItem<T> delayedItem = new DelayedItem<>(item, minDelay);
    inner.add(delayedItem);
  }

  public void add(IReadOnlyList<T> items) {
    this.add(items.toJavaList());
  }

  public void add(Collection<? extends T> items) {
    int minDelay = Math.min(getLastDelay(maximalDelay), currentDelay);
    for (T item : items) {
      DelayedItem<T> delayedMessage = new DelayedItem<>(item, minDelay);
      inner.add(delayedMessage);
    }
  }

  public void addNoDelay(Collection<? extends T> items) {
    int minDelay = 0;
    for (T item : items) {
      DelayedItem<T> delayedMessage = new DelayedItem<>(item, minDelay);
      inner.add(delayedMessage);
    }
  }

  public void clear() {
    inner.clear();
  }

  public T get(int index) {
    return inner.get(index).item;
  }

  public IReadOnlyList<T> getAll() {
    return inner.select(q -> q.item);
  }

  public IList<T> getAndElapse() {
    lowerDelay();
    IList<T> ret = new EList<>();
    while (inner.isEmpty() == false) {
      DelayedItem<T> delayedItem = inner.get(0);
      if (delayedItem.delayLeft > 0) break;

      inner.removeAt(0);
      ret.add(delayedItem.item);
    }
    return ret;
  }

  public boolean isAny(Predicate<T> predicate) {
    for (DelayedItem<T> item : inner) {
      if (predicate.test(item.item))
        return true;
    }
    return false;
  }

  public DelayedList<T> load(XElement element, IMap<Class<?>, Deserializer> customDeserializers, Class<T> itemType) {
    XmlLoadUtils.Field.restoreFields(element, this, "minimalDelay", "maximalDelay", "currentDelay");

    XmlLoadUtils.Field.restoreField(element, this, "inner",
            new ItemsDeserializer(e -> {
              int delayLeft = XmlLoadUtils.Field.loadFieldValue(e, "delayLeft", int.class);
              T item = XmlLoadUtils.Field.loadFieldValue(e, "item",
                      ObjectDeserializer.createDeepDeserializer()
                              .useDeserializers(customDeserializers));
              DelayedItem<T> tmp = new DelayedItem<>(item, delayLeft);
              return tmp;
            }, this.inner));
    return this;
  }

  public void newRandomDelay() {
    this.currentDelay = Context.getApp().getRnd().nextInt(minimalDelay, maximalDelay + 1);
  }

  public void removeAt(int index) {
    inner.removeAt(index);
  }

  public int size() {
    return inner.size();
  }

  private void lowerDelay() {
    for (DelayedItem<T> di : inner) {
      di.delayLeft = di.delayLeft - 1;
    }
  }

  private int getLastDelay(int valueIfEmpty) {
    if (inner.isEmpty())
      return valueIfEmpty;
    else
      return inner.get(inner.size() - 1).delayLeft;
  }

}
