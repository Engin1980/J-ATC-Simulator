package eng.jAtcSim.lib.global;

import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class ECollections {

  public interface NumberSelector<T> {
    double getValue(T item);
  }

  public static <T> T tryGetFirst(Iterable<T> lst, Predicate<T> predicate){
    T ret = null;
    for (T t : lst) {
      if (predicate.test(t)){
        ret = t;
        break;
      }
    }
    return ret;
  }
  public static <T> double sum(Iterable<T> lst, NumberSelector<T> selector){
    double sum = 0;
    for (T t : lst) {
      sum += selector.getValue(t);
    }
    return sum;
  }

  public static <T> T getRandom(List<T> lst) {
    int index = (int) ( Math.random() * lst.size());
    T ret = lst.get(index);
    return ret;
  }
}
