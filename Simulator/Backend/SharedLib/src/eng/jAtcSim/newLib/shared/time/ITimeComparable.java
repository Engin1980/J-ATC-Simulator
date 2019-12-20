package eng.jAtcSim.newLib.shared.time;

public interface ITimeComparable<T extends ITime> extends ITime {

  default boolean isAfter(T other) {
    return this.getValue() > other.getValue();
  }

  default boolean isAfterOrEq(T other) {
    return this.getValue() >= other.getValue();
  }

  default boolean isBefore(T other) {
    return this.getValue() < other.getValue();
  }

  default boolean isBeforeOrEq(T other) {
    return this.getValue() <= other.getValue();
  }

  default boolean isBetween(T from, T to) {
    return this.isAfter(from) && this.isBefore(to);
  }

  default boolean isBetweenOrEq(T from, T to) {
    return this.isAfterOrEq(from) && this.isBeforeOrEq(to);
  }
}
