package exml.loading;

import eng.eSystem.eXml.XElement;

public class XLoadException extends RuntimeException {

  private final XElement element;
  private final Object object;

  public XElement getElement() {
    return element;
  }

  public Object getObject() {
    return object;
  }

  public XLoadException(String message, XLoadContext ctx) {
    this(message, null,  ctx);
  }

  public XLoadException(String message, Throwable cause, XLoadContext ctx) {
    super(message, cause);
    this.element = ctx.currentElement;
    this.object = ctx.currentObject;
  }
}
