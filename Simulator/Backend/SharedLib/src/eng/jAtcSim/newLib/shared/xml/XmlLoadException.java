package eng.jAtcSim.newLib.shared.xml;

import eng.jAtcSim.newLib.shared.exceptions.ApplicationException;

public class XmlLoadException extends ApplicationException {
  public XmlLoadException(String message) {
    super("Xml loading error. " + message);
  }
}
