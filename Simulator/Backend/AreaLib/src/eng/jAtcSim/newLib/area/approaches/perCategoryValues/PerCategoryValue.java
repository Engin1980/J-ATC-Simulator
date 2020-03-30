package eng.jAtcSim.newLib.area.approaches.perCategoryValues;

import eng.eSystem.collections.*;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class PerCategoryValue<T extends Number> {
  private final T a;
  private final T b;
  private final T c;
  private final T d;

  public PerCategoryValue(T a, T b, T c, T d) {
    this.a = a;
    this.b = b;
    this.c = c;
    this.d = d;
  }

  public PerCategoryValue(T value){
    this(value, value, value,value);
  }

  public T get(char category){
    switch (category) {
      case 'a':
      case 'A':
        return this.a;
      case 'b':
      case 'B':
        return this.b;
      case 'c':
      case 'C':
        return this.c;
      case 'd':
      case 'D':
        return this.d;
      default:
        throw new EEnumValueUnsupportedException(category);
    }
  }
}
