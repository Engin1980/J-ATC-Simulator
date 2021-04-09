package exml.base;

import eng.eSystem.collections.EStack;
import eng.eSystem.collections.IStack;
import eng.eSystem.eXml.XElement;
import exml.Log;
import exml.Values;

public class XContext {
  private final Log log = new Log(this);
  private final Values values = new Values();
  private final IStack<XElement> currentElement = new EStack<>();
  private final IStack<Object> currentObject = new EStack<>();

  public IStack<XElement> getCurrentElement() {
    return currentElement;
  }

  public IStack<Object> getCurrentObject() {
    return currentObject;
  }

  public Log getLog() {
    return log;
  }

  public Values getValues() {
    return values;
  }
}
