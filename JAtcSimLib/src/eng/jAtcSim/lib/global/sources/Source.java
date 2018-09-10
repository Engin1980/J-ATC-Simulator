package eng.jAtcSim.lib.global.sources;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;


public abstract class Source<T> {
  @XmlIgnore
  private boolean initialized = false;

  protected void setInitialized(){
    this.initialized = true;
  }

  public boolean isInitialized() {
    return initialized;
  }

  public abstract T _get();

  public final T getContent(){
    if (!initialized)
      throw new EApplicationException("Source must be initialized before its content is getContent.");
    T ret = _get();
    return ret;
  }
}
