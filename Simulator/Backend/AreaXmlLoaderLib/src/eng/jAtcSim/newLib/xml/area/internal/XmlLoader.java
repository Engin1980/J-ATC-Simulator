package eng.jAtcSim.newLib.xml.area.internal;

import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.context.LoadingContext;

public abstract class XmlLoader<T> implements IXmlLoader<T> {
  protected final LoadingContext context;

  protected XmlLoader(LoadingContext context) {
    this.context = context;
  }
}
