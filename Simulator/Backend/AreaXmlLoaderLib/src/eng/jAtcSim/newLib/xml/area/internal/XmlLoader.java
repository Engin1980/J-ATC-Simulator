package eng.jAtcSim.newLib.xml.area.internal;

import eng.jAtcSim.newLib.xml.area.internal.context.Context;

public abstract class XmlLoader {
  public final Context context;

  public XmlLoader(Context context) {
    this.context = context;
  }
}
