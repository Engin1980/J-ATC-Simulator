package eng.jAtcSim.newLib.area;

import eng.eSystem.eXml.XElement;
import eng.eSystem.events.EventAnonymousSimple;

public abstract class Parentable<T> {
  private T parent;

  public void setParent(T parent){
    assert parent != null;
    assert this.parent == null;
    this.parent = parent;
  }

  public T getParent(){
    assert this.parent != null;
    return this.parent;
  }
}
