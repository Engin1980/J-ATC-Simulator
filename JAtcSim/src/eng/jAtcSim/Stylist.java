package eng.jAtcSim;

import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;

import java.awt.*;
import java.util.function.Consumer;

public class Stylist {

  public static abstract class Filter {
    public abstract boolean accepts(Component component);
  }

  public static class AndFilter extends Filter{
    private IList<Filter> inner = new EList<>();

    public AndFilter(Filter ... filters) {
      this.inner.add(filters);
    }

    @Override
    public boolean accepts(Component component) {
      for (Filter filter : inner) {
        if (filter.accepts(component) == false)
          return false;
      }
      return true;
    }
  }

  public static class TypeFilter extends Filter {
    private Class type;
    private boolean alsoDescendants;

    public TypeFilter(Class type, boolean alsoDescendants) {
      this.type = type;
      this.alsoDescendants = alsoDescendants;
    }

    @Override
    public boolean accepts(Component component) {
      boolean ret;
      if (alsoDescendants)
        ret = this.type.isAssignableFrom(component.getClass());
      else
        ret = this.type.equals(component.getClass());
      return ret;
    }
  }

  public static class ParentNameFilter extends Filter{
    private final String name;
    private final boolean lookRecursively;

    public ParentNameFilter(String name, boolean lookRecursively) {
      this.name = name;
      this.lookRecursively = lookRecursively;
    }

    @Override
    public boolean accepts(Component component) {
      Component parent = component.getParent();
      while (parent != null){
        if (parent.getName().equals(this.name))
          return true;
        if (this.lookRecursively)
          parent = parent.getParent();
        else
          parent = null;
      }
      return false;
    }
  }

  public static class ParentTypeFilter extends Filter{
    private final Class type;
    private final boolean lookRecursively;

    public ParentTypeFilter(Class type, boolean lookRecursively) {
      this.type = type;
      this.lookRecursively = lookRecursively;
    }

    @Override
    public boolean accepts(Component component) {
      Component parent = component.getParent();
      while (parent != null){
        if (parent.getClass().equals(this.type))
          return true;
        if (this.lookRecursively)
          parent = parent.getParent();
        else
          parent = null;
      }
      return false;
    }
  }

  public static class NameFilter extends Filter{
    private final String name;

    public NameFilter(String name) {
      this.name = name;
    }

    @Override
    public boolean accepts(Component component) {
      boolean ret = component.getName() != null && component.getName().equals(this.name);
      return ret;
    }
  }

  private static IList<Tuple<Filter, Consumer<Component>>> inner  = new EList<>();

  public static void add(Filter filter, Consumer<Component> style) {
    inner.add(new Tuple(filter, style));
  }

  public static void apply(Component component, boolean applyOnContent) {

    for (Tuple<Filter, Consumer<Component>> item : inner) {
      Filter filter = item.getA();
      if (filter.accepts(component)) {
        item.getB().accept(component);
      }
    }

    if (applyOnContent && component instanceof java.awt.Container) {
      Container container = (Container) component;
      for (Component item : container.getComponents()) {
        apply(item, applyOnContent);
      }
    }
  }

}
