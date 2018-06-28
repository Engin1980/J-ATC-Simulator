package eng.jAtcSim.lib.global;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.lib.Acc;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DelayedList<T> {

    class DelayedItem<T> {
      public final T item;
      public int delayLeft;

      public DelayedItem(T item, int delay) {
        this.item = item;
        this.delayLeft = delay;
      }

      private DelayedItem(){ item = null;}
    }
    private final int minimalDelay;
    private final int maximalDelay;
    private int currentDelay = 0;
    private List<DelayedItem<T>> inner = new LinkedList<>();

    private DelayedList() {
      minimalDelay = 0;
      maximalDelay = 0;
    }


    public DelayedList(int minimalDelay, int maximalDelay) {
      this.minimalDelay = minimalDelay;
      this.maximalDelay = maximalDelay;
      newRandomDelay();
    }

    public void newRandomDelay() {
      this.currentDelay = Acc.rnd().nextInt(minimalDelay, maximalDelay + 1);
    }

    /**
     * Adds item with random delay
     *
     * @param item
     */
    public void add(T item) {
      int minDelay = Math.min(getLastDelay(maximalDelay), currentDelay);
      DelayedItem<T> delayedItem = new DelayedItem(item, minDelay);
      inner.add(delayedItem);
    }

    public void add(IReadOnlyList<T> items){
      this.add(items.toList());
    }

    public void add(Collection<? extends T> items) {
      int minDelay = Math.min(getLastDelay(maximalDelay), currentDelay);
      for (T item : items) {
        DelayedItem delayedMessage = new DelayedItem(item, minDelay);
        inner.add(delayedMessage);
      }
    }

    public void addNoDelay(Collection<? extends T> items) {
      int minDelay = 0;
      for (T item : items) {
        DelayedItem delayedMessage = new DelayedItem(item, minDelay);
        inner.add(delayedMessage);
      }
    }

    public IList<T> getAndElapse() {
      lowerDelay();
      IList<T> ret = new EList<>();
      while (inner.isEmpty() == false) {
        DelayedItem<T> delayedItem = inner.get(0);
        if (delayedItem.delayLeft > 0) break;

        inner.remove(0);
        ret.add(delayedItem.item);
      }
      return ret;
    }

    public void clear() {
      inner.clear();
    }

    public int size() {
      return inner.size();
    }

    public T get(int index) {
      return inner.get(index).item;
    }

    public void removeAt(int index) {
      inner.remove(index);
    }

    private void lowerDelay() {
      for (DelayedItem di : inner) {
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
