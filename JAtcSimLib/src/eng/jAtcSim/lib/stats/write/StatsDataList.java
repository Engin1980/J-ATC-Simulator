package eng.jAtcSim.lib.stats.write;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.moods.MoodResult;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.serialization.LoadSave;

public class StatsDataList {
  private EList<StatsData> inner = new EList();

  @XmlIgnore
  private IMap<StatsData, XElement> presavedElements = new EMap<>();

  public void createNewSet() {
    StatsData ws;
    ws = inner.tryGetLast();
    if (ws != null) {
      ws.toTime = Acc.now().clone();
    }
    ws = new StatsData(Acc.now().clone());
    inner.add(ws);
  }

  public StatsData getCurrent() {
    return inner.getLast();
  }

  public IReadOnlyList<StatsData> getByTime(ETime time) {
    IList<StatsData> ret = inner.where(q -> q.fromTime.isAfter(time)).orderBy(q -> q.fromTime);
    return ret;
  }

  public IList<MoodResult> getFullMoodHistory() {
    EList<MoodResult> ret = new EList<>();
    inner.forEach(q -> ret.add(q.planesMood.getList()));
    return ret;
  }

  public void save(XElement elm) {
    XElement tmp = new XElement("writeSetList");
    elm.addElement(tmp);
    elm = tmp;
    tmp = new XElement("inner");
    tmp.setAttribute("__itemClass", "eng.jAtcSim.lib.stats.write.StatsData");
    elm.addElement(tmp);
    elm = tmp;

    for (StatsData statsData : inner) {
      tmp = saveItem(statsData);
      elm.addElement(tmp);
    }
  }

  public IReadOnlyList<StatsData> getAll() {
    return inner;
  }

  private XElement saveItem(StatsData statsData) {
    XElement ret;

    ret = presavedElements.tryGet(statsData);
    if (ret == null){
      ret = LoadSave.saveIntoElement("item", statsData);
      presavedElements.set(statsData, ret);
    }
    ret.releaseFromParent();
    return ret;
  }

  public static StatsDataList load (XElement elm) {
    StatsDataList ret = new StatsDataList();

    elm = elm.getChild("writeSetList");
    elm = elm.getChild("inner");

    for (XElement itemElement : elm.getChildren("item")) {
      StatsData statsData = loadItem(itemElement);
      ret.inner.add(statsData);
      ret.presavedElements.set(statsData, itemElement);
    }

    return ret;
  }

  private static StatsData loadItem(XElement itemElement) {
    StatsData ret = new StatsData(null);
    LoadSave.loadFromElement(itemElement, ret);
    return ret;
  }
}
