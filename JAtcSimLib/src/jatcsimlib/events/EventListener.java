package jatcsimlib.events;

public abstract class EventListener<Tsender, TeventArgs> {
  public abstract void raise (Tsender parent, TeventArgs e);
}
