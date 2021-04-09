package exml;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlyStack;
import eng.eSystem.eXml.XElement;
import eng.eSystem.events.Event;
import exml.base.XContext;

public class Log {

  public enum Type {
    object,
    xml,
    field,
    message
  }

  public static class LogEventArgs {
    public final String message;
    public final int indent;
    public final Type type;

    LogEventArgs(Type type, String message, int indent) {
      this.type = type;
      this.message = message;
      this.indent = indent;
    }
  }

  public static class LogVerboseEventArgs extends LogEventArgs {
    public final IReadOnlyStack<XElement> elementStack;
    public final IReadOnlyStack<Object> objectStack;

    public LogVerboseEventArgs(Type type, String message, int indent, XContext ctx) {
      super(type, message, indent);
      this.elementStack = ctx.getCurrentElement().copy();
      this.objectStack = ctx.getCurrentObject().copy();
    }
  }

  public static final String PACKAGE_KEY = "%package";
  public static final String TO_STRING_KEY = "%toString";
  public static final String CLASS_NAME_KEY = "%className";
  public static final String FIELD_NAME_KEY = "%fieldName";
  public static final String XELEMENT_NAME = "%xmlName";
  public static final String XELEMENT_XPATH = "%xmlPath";


  private final XContext ctx;
  public final Event<XContext, LogEventArgs> onLog;
  public final Event<XContext, LogVerboseEventArgs> onLogVerbose;
  private int indent = 0;
  private String objectMessageFormat = String.format("%s (%s.%s)", TO_STRING_KEY, PACKAGE_KEY, CLASS_NAME_KEY);
  private String fieldMessageFormat = String.format("%s.%s.%s", PACKAGE_KEY, CLASS_NAME_KEY, FIELD_NAME_KEY);
  private String xElementMessageFormat = String.format("%s", XELEMENT_XPATH);

  public Log(XContext ctx) {
    this.ctx = ctx;
    this.onLog = new Event<>(ctx);
    this.onLogVerbose = new Event<>(ctx);
  }

  public void decreaseIndent() {
    this.indent--;
  }

  public void increaseIndent() {
    this.indent++;
  }

  public void logElement(XElement elm) {
    IMap<String, String> mp = EMap.of(
            XELEMENT_NAME, elm.getName(),
            XELEMENT_XPATH, elm.toXPath());
    String msg = replaceKeys(xElementMessageFormat, mp);
    logInternal(Type.xml, msg);
  }

  public void logField(Object obj, String fieldName) {
    IMap<String, String> mp = EMap.of(
            PACKAGE_KEY, obj == null ? "" : obj.getClass().getPackageName(),
            TO_STRING_KEY, obj == null ? "(null)" : obj.toString(),
            CLASS_NAME_KEY, obj == null ? "(null)" : obj.getClass().getSimpleName(),
            FIELD_NAME_KEY, fieldName);
    String msg = replaceKeys(fieldMessageFormat, mp);
    logInternal(Type.field, msg);
  }

  public void logField(Class<?> cls, String fieldName) {
    IMap<String, String> mp = EMap.of(
            PACKAGE_KEY, cls.getPackageName(),
            TO_STRING_KEY, cls.toString(),
            CLASS_NAME_KEY, cls.getSimpleName(),
            FIELD_NAME_KEY, fieldName);
    String msg = replaceKeys(fieldMessageFormat, mp);
    logInternal(Type.field, msg);
  }

  public void logObject(Object obj) {
    IMap<String, String> mp = EMap.of(
            PACKAGE_KEY, obj == null ? "" : obj.getClass().getPackageName(),
            TO_STRING_KEY, obj == null ? "(null)" : obj.toString(),
            CLASS_NAME_KEY, obj == null ? "(null)" : obj.getClass().getSimpleName()
    );
    String msg = replaceKeys(objectMessageFormat, mp);
    logInternal(Type.field, msg);
  }

  public void logText(String s, Object... params) {
    String tmp = String.format(s, params);
    logInternal(Type.message, tmp);
  }

  private String replaceKeys(String format, IMap<String, String> mp) {
    String ret = format;
    for (String key : mp.getKeys()) {
      while (ret.contains(key))
        ret = ret.replace(key, mp.get(key));
    }
    return ret;
  }

  private void logInternal(Type type, String message) {
    if (onLogVerbose.hasListeners())
      onLogVerbose.raise(new LogVerboseEventArgs(type, message, this.indent, this.ctx));
    if (onLog.hasListeners())
      onLog.raise(new LogEventArgs(type, message, this.indent));
  }
}
