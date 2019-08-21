package eng.jAtcSim.lib.world;

import eng.eSystem.events.EventAnonymousSimple;

public abstract class Parentable<T> {
  private T parent;
  private final eng.eSystem.events.EventAnonymousSimple onParentSet = new EventAnonymousSimple();

  public EventAnonymousSimple getOnParentSet() {
    return onParentSet;
  }

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
