package eng.jAtcSim.newLib.area.global.newSources;

import eng.eSystem.exceptions.ApplicationException;

public abstract class Source<T> {
  private boolean initialized = false;
  protected void setInitialized(){
    this.initialized = true;
  }

  public final T getContent(){
    if (!initialized)
      throw new ApplicationException("Source must be initialized before the content is accessed.");
    else
      return _getContent();
  }

  protected abstract T _getContent();
}
