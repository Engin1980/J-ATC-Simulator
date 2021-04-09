package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.eXml.XElement;
import eng.eSystem.events.Event;
import eng.eSystem.events.EventAnonymous;
import eng.jAtcSim.newLib.xml.area.internal.context.LoadingContext;

public abstract class XmlLoader<T> {

  protected final LoadingContext context;

  public static final EventAnonymous<String> onLog = new EventAnonymous<>();

  protected XmlLoader(LoadingContext context) {
    this.context = context;
  }

  public abstract T load(XElement ctx);

  protected static void log(int indent, String pattern, Object... data) {
    String tmp = String.format(pattern, data);
    if (indent > 0){
      String ptrn= "%-"+indent+"s%s";
      tmp = String.format(ptrn, "", tmp);
    }

    onLog.raise(tmp);
  }
}
