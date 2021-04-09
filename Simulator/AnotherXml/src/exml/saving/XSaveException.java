package exml.saving;

import exml.base.XException;

public class XSaveException extends XException {

  public XSaveException(String message, XSaveContext ctx) {
    this(message, null,  ctx);
  }

  public XSaveException(String message, Throwable cause, XSaveContext ctx) {
    super(message, cause, ctx);
  }
}
