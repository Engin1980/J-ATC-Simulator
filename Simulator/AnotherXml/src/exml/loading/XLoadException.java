package exml.loading;

import exml.base.XException;

public class XLoadException extends XException {

  public XLoadException(String message, XLoadContext ctx) {
    this(message, null,  ctx);
  }

  public XLoadException(String message, Throwable cause, XLoadContext ctx) {
    super(message, cause, ctx);
  }
}
