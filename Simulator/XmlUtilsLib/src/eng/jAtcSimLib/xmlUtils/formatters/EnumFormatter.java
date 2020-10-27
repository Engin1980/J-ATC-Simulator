package eng.jAtcSimLib.xmlUtils.formatters;

import eng.eSystem.validation.EAssert;
import eng.jAtcSimLib.xmlUtils.Formatter;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class EnumFormatter implements Formatter<Object> {
  @Override
  public String invoke(Object t) {
    EAssert.Argument.isTrue(t.getClass().isEnum(), sf("This formatter should be used only for enum, but provided value is '%s'", t.getClass().toString()));
    String s = t.toString();
    return s;
  }
}
