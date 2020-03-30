package eng.jAtcSim.newLib.area.approaches.factories;

import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.area.approaches.ApproachEntry;
import eng.jAtcSim.newLib.area.approaches.locations.FixRelatedLocation;
import eng.jAtcSim.newLib.area.approaches.locations.ILocation;
import eng.jAtcSim.newLib.area.routes.IafRoute;

public class ApproachEntryFactory {
  public static ApproachEntry createForIls(ThresholdInfo threshold, IafRoute iafRoute) {
    ILocation ael = new FixRelatedLocation(threshold.getCoordinate(),
        (int) Headings.subtract(threshold.getCourse(), 15),
        (int) Headings.add(threshold.getCourse(),15),
        15d);
    return new ApproachEntry(ael, iafRoute);
  }
}
