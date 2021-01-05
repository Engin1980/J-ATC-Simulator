package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.validation.EAssert;
import exml.ISimPersistable;
import exml.XContext;

public abstract class Source<T> implements ISimPersistable {
  private boolean initialized = false;
  private T content;

  @Override
  public void save(XElement elm, XContext ctx) {
    ctx.saver.ignoreFields(this, "initialized", "content");
    ctx.saver.saveRemainingFields(this, elm);
  }

  @Override
  public void load(XElement elm, XContext ctx) {

  }

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
