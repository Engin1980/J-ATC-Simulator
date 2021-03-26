package eng.jAtcSim.newLib.shared;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.*;

import exml.IXPersistable;
import exml.annotations.XConstructor;

public class PlaneCategoryDefinitions implements IXPersistable {
  private final static PlaneCategoryDefinitions ALL = new PlaneCategoryDefinitions("ABCD");
  private IReadOnlyList<Character> inner;

  public static PlaneCategoryDefinitions getAll() {
    return ALL;
  }

  @XConstructor

  private PlaneCategoryDefinitions() {
  }

  private PlaneCategoryDefinitions(IReadOnlyList<Character> inner) {
    this.inner = inner;
  }

  public PlaneCategoryDefinitions(char c){
    c = ensureValidAndNormalize(c);
    this.inner = EList.of(c);
  }

  public PlaneCategoryDefinitions(String chars) {
    IList<Character> tmp = new EList<>();
    for (int i = 0; i < chars.length(); i++) {
      char c = chars.charAt(i);
      c = ensureValidAndNormalize(c);
      tmp.add(c);
    }
    this.inner = tmp;
  }

  public boolean containsAny(char[] categories){
    for (char c : categories) {
      if (contains(c))
        return true;
    }
    return false;
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

  public PlaneCategoryDefinitions makeClone() {
    PlaneCategoryDefinitions ret = new PlaneCategoryDefinitions(new EList(this.inner));
    return ret;
  }

  private char ensureValidAndNormalize(char category) {
    if (category < 'A' || category > 'E')
      throw new IllegalArgumentException("Category must be between A-E.");
    if (Character.isLowerCase(category))
      category = Character.toUpperCase(category);
    return category;
  }
}
