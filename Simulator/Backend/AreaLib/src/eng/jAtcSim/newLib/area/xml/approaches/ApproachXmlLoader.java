package eng.jAtcSim.newLib.area.xml.approaches;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.NavaidList;
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.approaches.ApproachEntry;
import eng.jAtcSim.newLib.area.approaches.ApproachFactory;
import eng.jAtcSim.newLib.area.approaches.ApproachStage;
import eng.jAtcSim.newLib.area.approaches.behaviors.FlyRadialWithDescentBehavior;
import eng.jAtcSim.newLib.area.approaches.behaviors.LandingBehavior;
import eng.jAtcSim.newLib.area.approaches.conditions.*;
import eng.jAtcSim.newLib.area.approaches.factories.ThresholdInfo;
import eng.jAtcSim.newLib.area.approaches.locations.ILocation;
import eng.jAtcSim.newLib.area.approaches.perCategoryValues.IntegerPerCategoryValue;
import eng.jAtcSim.newLib.area.routes.GaRoute;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.area.xml.XmlLoaderWithNavaids;
import eng.jAtcSim.newLib.area.xml.XmlMappingDictinary;
import eng.jAtcSim.newLib.shared.enums.ApproachType;
import eng.jAtcSim.newLib.shared.xml.XmlLoadException;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.ICommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.ChangeAltitudeCommand;

public class ApproachXmlLoader extends XmlLoaderWithNavaids<Approach> {

  private static double convertGlidePathDegreesToSlope(double gpDegrees) {
    return Math.tan(Math.toRadians(gpDegrees));
  }

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
        ret = loadGnss(source);
        break;
      case "unpreciseApproach":
        ret = loadUnprecise(source);
        break;
      case "customApproach":
        ret = loadCustom(source);
        break;
      default:
        throw new XmlLoadException("Unknown approach type " + source.getName() + ".");
    }
    return ret;
  }

  private Approach loadGnss(XElement source) {
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
      ILocation entryLocation = ApproachFactory.Entry.Location.createApproachEntryLocationFoRoute(iafRoute, this.navaids);
      ApproachEntry entry = new ApproachEntry(entryLocation, iafRoute);
      entries.add(entry);
    }

    // estimate faf by slope and daA
    {
      ILocation entryLocation = ApproachFactory.Entry.Location.createApproachEntryLocationForFAF(
          threshold.coordinate, threshold.altitude, radial, initialAltitude, slope);
      ApproachEntry entry = new ApproachEntry(entryLocation, null);
      entries.add(entry);
    }

    GaRoute gaRoute = gaRoutes.get(gaMapping).getFirst();
    IList<ICommand> beforeStagesCommands = EList.of(
        ChangeAltitudeCommand.createDescend(initialAltitude));

    IList<ApproachStage> stages = new EList<>();
    { // radial descent stage
      ICondition exitCondition = AggregatingCondition.create(
          AggregatingCondition.eConditionAggregator.and,
          PlaneShaCondition.createAsMinimalAltitude(new IntegerPerCategoryValue(daA, daB, daC, daD)),
          RunwayThresholdVisibilityCondition.create()
      );
      ICondition errorCondition = AggregatingCondition.create(
          AggregatingCondition.eConditionAggregator.or,
          PlaneShaCondition.createAsMinimalAltitude(new IntegerPerCategoryValue(daA, daB, daC, daD)),
          PlaneOrderedAltitudeDifference.create(new IntegerPerCategoryValue(1000))
      );
      stages.add(new ApproachStage(
          new FlyRadialWithDescentBehavior(threshold.coordinate, radial, threshold.altitude, slope),
          exitCondition,
          errorCondition,
          "GNSS radial " + threshold.name
      ));
    }
    {
      // landing stage
      stages.add(new ApproachStage(
          new LandingBehavior(),
          null,
          RunwayThresholdVisibilityCondition.create(),
          "GNSS landing " + threshold.name
      ));
    }

    Approach ret = new Approach(ApproachType.gnss, entries, beforeStagesCommands, stages, gaRoute);

    return ret;
  }
}
