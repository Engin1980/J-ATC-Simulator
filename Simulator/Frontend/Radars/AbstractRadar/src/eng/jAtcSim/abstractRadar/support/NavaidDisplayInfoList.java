package eng.jAtcSim.abstractRadar.support;

import eng.jAtcSim.newLib.area.Navaid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NavaidDisplayInfoList implements Iterable<NavaidDisplayInfo> {
  private final List<NavaidDisplayInfo> inner = new ArrayList<>();

  public void add(NavaidDisplayInfo ndi) {
    inner.add(ndi);
  }

  public NavaidDisplayInfo getByNavaid(Navaid navaid) {
    NavaidDisplayInfo ret = null;
    for (NavaidDisplayInfo navaidDisplayInfo : inner) {
      if (navaidDisplayInfo.navaid == navaid) {
        ret = navaidDisplayInfo;
        break;
      }
    }
    return ret;
  }

  @Override
  public Iterator<NavaidDisplayInfo> iterator() {
    return inner.iterator();
  }
}
