package eng.jAtcSim.lib.stats.write;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.moods.MoodResult;
import eng.jAtcSim.lib.global.ETime;

public class StatsDataList {
  private EList<StatsData> inner = new EList();

  public void createNewSet(){
    StatsData ws;
    ws = inner.tryGetLast( );
    if (ws != null){
      ws.toTime = Acc.now().clone();
    }
    ws = new StatsData(Acc.now().clone());
    inner.add(ws);
  }

  public StatsData getCurrent(){
    return inner.getLast();
  }

  public IReadOnlyList<StatsData> getByTime(ETime time){
    IList<StatsData> ret = inner.where(q->q.fromTime.isAfter(time)).orderBy(q->q.fromTime);
    return ret;
  }

  public IList<MoodResult> getFullMoodHistory() {
    EList<MoodResult> ret = new EList<>();
    inner.forEach(q->ret.add(q.planesMood.getList()));
    return ret;
  }
}
