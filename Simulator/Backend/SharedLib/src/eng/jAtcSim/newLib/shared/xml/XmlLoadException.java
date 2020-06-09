package eng.jAtcSim.newLib.shared.xml;

import eng.eSystem.exceptions.EApplicationException;

public class XmlLoadException extends EApplicationException {
  public XmlLoadException(String message) {
    super("Xml loading error. " + message);
  }
}
