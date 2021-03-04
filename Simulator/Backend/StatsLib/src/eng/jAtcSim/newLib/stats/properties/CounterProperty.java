package eng.jAtcSim.newLib.stats.properties;

import eng.eSystem.validation.EAssert;
import exml.IXPersistable;

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


  //TODEL
//  @Override
//  public void load(XElement elm, XLoadContext ctx) {
//
//  }
//
//  @Override
//  public void save(XElement elm, XSaveContext ctx) {
//    ctx.saveObject(this.count, elm);
//  }
}
