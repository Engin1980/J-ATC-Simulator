/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.world;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.lib.world.approaches.GaRoute;
import eng.jAtcSim.lib.world.approaches.IafRoute;

/**
 *
 * @author Marek
 */
public class ActiveRunway extends Runway<ActiveRunway, ActiveRunwayThreshold> {

  public static IList<ActiveRunway> loadList(IReadOnlyList<XElement> sources,
                                             int airportAltitude, NavaidList navaids,
                                             IReadOnlyList<DARoute> routes,
                                             IReadOnlyList<IafRoute> iafRoutes,
                                             IReadOnlyList<GaRoute> gaRoutes){
    IList<ActiveRunway> ret = new EList<>();

    for (XElement source : sources) {
      ActiveRunway activeRunway = ActiveRunway.load(source,
          airportAltitude, navaids, routes, iafRoutes, gaRoutes);
      ret.add(activeRunway);
    }

    return ret;
  }

  public static ActiveRunway load(XElement source, int airportAltitude, NavaidList navaids,
                                  IReadOnlyList<DARoute> routes,
                                  IReadOnlyList<IafRoute> iafRoutes,
                                  IReadOnlyList<GaRoute> gaRoutes){
    ActiveRunway ret;
    IList<ActiveRunwayThreshold> thresholds = ActiveRunwayThreshold.loadList(source.getChild("thresholds").getChildren(),
        airportAltitude, navaids, routes, iafRoutes, gaRoutes);
    assert thresholds.size() == 2;
    ret = new ActiveRunway(thresholds);
    return ret;
  }

  public ActiveRunway(IList<ActiveRunwayThreshold> thresholds) {
    super(thresholds);
    thresholds.forEach(q->q.setParent(this));
  }

  @Override
  public String getName() {
    return getThresholdA().getName() + "-" + getThresholdB().getName();
  }

  @Override
  public String toString() {
    return this.getName() + "{rwy}";
  }
}
