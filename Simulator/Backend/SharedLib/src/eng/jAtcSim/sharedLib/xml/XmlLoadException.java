package eng.jAtcSim.sharedLib.xml;

import eng.eSystem.collections.*;
import eng.jAtcSim.sharedLib.exceptions.ApplicationException;

public class XmlLoadException extends ApplicationException {
  public XmlLoadException(String message) {
    super("Xml loading error. " + message);
  }
}
