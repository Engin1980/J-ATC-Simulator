package eng.jAtcSim.lib.global.newSources;

import eng.eSystem.exceptions.EApplicationException;

public abstract class Source<T> {
  private boolean initialized = false;
  protected void setInitialized(){
    this.initialized = true;
  }

  public final T getContent(){
    if (!initialized)
      throw new EApplicationException("Source must be initialized before the content is accessed.");
    else
      return _getContent();
  }

  protected abstract T _getContent();
}
