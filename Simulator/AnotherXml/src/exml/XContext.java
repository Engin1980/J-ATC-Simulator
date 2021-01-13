package exml;

public class XContext {
  public final Saver saver;
  public final Loader loader;

  public XContext() {
    this.saver = new Saver(this);
    this.loader = new Loader(this);
  }
}
