package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.validation.EAssert;
import exml.IXPersistable;
import exml.XContext;
import exml.annotations.XIgnored;

public abstract class Source<T> implements IXPersistable {
  @XIgnored private boolean initialized = false;
  @XIgnored private T content;

  protected void setContent(T content){
    EAssert.Argument.isNotNull(content, "content");
    this.content = content;
    this.initialized = true;
  }

  public final T getContent() {
    if (!initialized)
      throw new EApplicationException("Source must be initialized before the content is accessed.");
    else
      return content;
  }
}
