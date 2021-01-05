package eng.jAtcSim.newLib.stats.properties;

import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;
import exml.IXPersistable;
import exml.XContext;

public class CounterProperty implements IXPersistable {
  private int count = 0;

  public void add() {
    count++;
  }

  public void add(int count) {
    EAssert.isTrue(count >= 0);
    this.count += count;
  }

  public int getCount() {
    return count;
  }

  @Override
  public void load(XElement elm, XContext ctx) {

  }

  @Override
  public void save(XElement elm, XContext ctx) {
    ctx.saver.saveObject(this.count, elm);
  }
}
