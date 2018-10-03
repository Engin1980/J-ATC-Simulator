package eng.jAtcSim.lib.stats.write;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.stats.Statistics;

public class WriteSetList {
  private EList<WriteSet> inner = new EList();

  public void createNewSet(){
    WriteSet ws = new WriteSet(Acc.now().clone());
    inner.add(ws);
  }

  public WriteSet getCurrent(){
    return inner.getLast();
  }

  public IReadOnlyList<WriteSet> getByTime(ETime time){
    IList<WriteSet> ret = inner.where(q->q.fromTime.isAfter(time)).orderBy(q->q.fromTime);
    return ret;
  }
}
