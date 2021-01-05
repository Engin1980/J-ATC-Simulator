package exml;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.ToDoException;

public interface IPlainObjectSimPersistable extends ISimPersistable {

  default void save(XElement elm, XContext ctx) {
    ctx.saver.saveRemainingFields(this, elm);
  }

  default void load(XElement elm, XContext ctx) {
    throw new ToDoException();
  }
}
