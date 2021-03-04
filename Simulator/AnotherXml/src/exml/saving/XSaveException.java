package exml.saving;

import eng.eSystem.eXml.XElement;

public class XSaveException extends RuntimeException {

  private final XElement element;
  private final Object object;

  public XElement getElement() {
    return element;
  }

  public Object getObject() {
    return object;
  }

  public XSaveException(String message, XSaveContext ctx) {
    this(message, null,  ctx);
  }

  public XSaveException(String message, Throwable cause, XSaveContext ctx) {
    super(message, cause);
    this.element = ctx.currentElement;
    this.object = ctx.currentObject;
  }
}
