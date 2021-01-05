package exml;

public class XContext {
  public static XContext createSave() {
    return new XContext();
  }

  public final SimSave saver;
  public final XParent parent = new XParent();

  public XContext() {
    this.saver = new SimSave(this);
  }
}
