package exml.base;

import eng.eSystem.collections.IReadOnlyStack;
import eng.eSystem.eXml.XElement;
import exml.loading.XLoadContext;

public abstract class XException extends RuntimeException {

  private final IReadOnlyStack<XElement> elementStack;
  private final IReadOnlyStack<Object> objectStack;

  public XException(String message, Throwable cause, XContext ctx) {
    super(message, cause);
    this.elementStack = ctx.getCurrentElement().copy();
    this.objectStack = ctx.getCurrentObject().copy();
  }

  public IReadOnlyStack<XElement> getElement() {
    return elementStack;
  }

  public IReadOnlyStack<Object> getObject() {
    return objectStack;
  }

}
