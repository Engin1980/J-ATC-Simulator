package eng.jAtcSim.lib.global;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.lib.exceptions.ERuntimeException;

public class PlaneCategoryDefinitions {
  private final static PlaneCategoryDefinitions ALL = new PlaneCategoryDefinitions("ABCD");
  private IReadOnlyList<Character> inner;

  public static PlaneCategoryDefinitions getAll() {
    return ALL;
  }

  public PlaneCategoryDefinitions(String chars) {
    IList<Character> tmp = new EList();
    for (int i = 0; i < chars.length(); i++) {
      char c = chars.charAt(i);
      c = ensureValidAndNormalize(c);
      tmp.add(c);
    }
    this.inner = tmp;
  }

  public boolean contains(char category) {
    category = ensureValidAndNormalize(category);


    boolean ret = inner.contains(category);
    return ret;
  }

  @Override
  public String toString() {
    EStringBuilder ret = new EStringBuilder();
    ret.appendItems(inner, q -> q.toString(), "");
    return ret.toString();
  }

  private char ensureValidAndNormalize(char category) {
    if (category < 'A' || category > 'E')
      throw new ERuntimeException("Category must be between A-E.");
    if (Character.isLowerCase(category))
      category = Character.toUpperCase(category);
    return category;
  }
}
