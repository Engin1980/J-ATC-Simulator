package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.xml.area.internal.context.LoadingContext;

public abstract class XmlLoader<T> {

  protected final LoadingContext context;

  protected XmlLoader(LoadingContext context) {
    this.context = context;
  }

  public abstract T load(XElement ctx);

  protected void log(int indent, String pattern, Object... data) {
    for (int i = 0; i < indent; i++) {
      System.out.print("  ");
    }
    System.out.printf(pattern, data);
    System.out.println();
  }
}
