package eng.jAtcSim.newLib.xml.area.internal;

import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.context.Context;

public abstract class XmlLoader<T> implements IXmlLoader<T> {
  protected final Context context;

  protected XmlLoader(Context context) {
    this.context = context;
  }
}
