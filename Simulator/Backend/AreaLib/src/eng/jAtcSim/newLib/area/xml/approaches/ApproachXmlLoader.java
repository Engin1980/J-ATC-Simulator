package eng.jAtcSim.newLib.area.xml.approaches;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.NavaidList;
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.approaches.ApproachEntry;
import eng.jAtcSim.newLib.area.approaches.ApproachFactory;
import eng.jAtcSim.newLib.area.approaches.factories.ApproachEntryFactory;
import eng.jAtcSim.newLib.area.approaches.factories.ThresholdInfo;
import eng.jAtcSim.newLib.area.approaches.locations.ILocation;
import eng.jAtcSim.newLib.area.routes.GaRoute;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.area.xml.XmlLoaderWithNavaids;
import eng.jAtcSim.newLib.area.xml.XmlMappingDictinary;
import eng.jAtcSim.newLib.shared.xml.XmlLoadException;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

public class ApproachXmlLoader extends XmlLoaderWithNavaids<Approach> {

  private final XmlMappingDictinary<IafRoute> iafRoutes;
  private final XmlMappingDictinary<GaRoute> gaRoutes;
  private final ThresholdInfo threshold;

  public ApproachXmlLoader(NavaidList navaids, XmlMappingDictinary<IafRoute> iafRoutes, XmlMappingDictinary<GaRoute> gaRoutes, ThresholdInfo threshold) {
    super(navaids);
    this.iafRoutes = iafRoutes;
    this.gaRoutes = gaRoutes;
    this.threshold = threshold;
  }

  @Override
  public Approach load(XElement source) {
    Approach ret;
    switch (source.getName()) {
      case "ilsApproach":
        ret = loadILS(source);
        break;
      case "gnssApproach":
        ret=loadGnss(source);
        break;
      case "unpreciseApproach":
        ret=loadUnprecise(source);
        break;
      case "customApproach":
        ret= loadCustom(source);
        break;
      default:
        throw new XmlLoadException("Unknown approach type " + source.getName() + ".");
    }
    return ret;
  }

  private Approach loadGnss(XElement source){
    XmlLoaderUtils.setContext(source);
    String gaMapping = XmlLoaderUtils.loadString("gaMapping");
    String iafMapping = XmlLoaderUtils.loadString("iafMapping");
    int daA = XmlLoaderUtils.loadInteger("daA");
    int daB = XmlLoaderUtils.loadInteger("daB");
    int daC = XmlLoaderUtils.loadInteger("daC");
    int daD = XmlLoaderUtils.loadInteger("daD");
    int radial = XmlLoaderUtils.loadInteger("radial");
    int initialAltitude = XmlLoaderUtils.loadInteger("initialAltitude");
    Double tmp = XmlLoaderUtils.loadDouble("glidePathPercentage", 3d);
    double slope = convertGlidePathDegreesToSlope(tmp);

    // build approach entry
    IList<ApproachEntry> entries = new EList<>();
    IReadOnlyList<IafRoute> iafRoutes = this.iafRoutes.get(iafMapping);
    for (IafRoute iafRoute : iafRoutes) {
      ILocation approachEntryLocation = ApproachFactory.Entry.Location.createApproachEntryLocationFoRoute(iafRoute, this.navaids);
      ApproachEntry entry = new ApproachEntry(approachEntryLocation, iafRoute);
      entries.add(entry);
    }
    
    ApproachEntry ae;
    ae = ApproachEntryFactory.createForIls(this.threshold);
    entries.add(ae);
    for (IafRoute iafRoute : this.getParent().getParent().getParent().getIafRoutes().where(q -> q.isMappingMatch(iafMapping))) {
      ae = ApproachEntry.createForIaf(iafRoute);
      entries.add(ae);
    }

    // ga route
    this.gaRoute = this.getParent().getParent().getParent().getGaRoutes().getFirst(q -> q.isMappingMatch(gaMapping));

    // build stages
    this.stages = new EList<>();
    stages.add(
        new RadialWithDescendStage(
            this.getParent().getCoordinate(),
            this.getParent().getCourseInt(),
            this.getParent().getParent().getParent().getAltitude(),
            slope,
            new AltitudeExitCondition(AltitudeExitCondition.eDirection.below, daA, daB, daC, daD)));
    stages.add(new CheckAirportVisibilityStage());
    stages.add(new LandingStage());
  }

  private static double convertGlidePathDegreesToSlope(double gpDegrees) {
    return Math.tan(Math.toRadians(gpDegrees));
  }
}
