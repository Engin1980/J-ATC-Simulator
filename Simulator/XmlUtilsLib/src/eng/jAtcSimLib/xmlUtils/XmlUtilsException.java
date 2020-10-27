package eng.jAtcSimLib.xmlUtils;

import eng.eSystem.exceptions.EApplicationException;

public class XmlUtilsException extends EApplicationException {

  public XmlUtilsException(String message) {
    super(message);
  }

  public XmlUtilsException(String message, Throwable cause) {
    super(message, cause);
  }
}
