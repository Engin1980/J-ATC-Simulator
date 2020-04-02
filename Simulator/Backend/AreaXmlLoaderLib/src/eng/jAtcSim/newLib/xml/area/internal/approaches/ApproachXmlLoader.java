package eng.jAtcSim.newLib.xml.area.internal.approaches;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.approaches.ApproachEntry;
import eng.jAtcSim.newLib.area.approaches.ApproachFactory;
import eng.jAtcSim.newLib.area.approaches.ApproachStage;
import eng.jAtcSim.newLib.area.approaches.behaviors.FlyRadialWithDescentBehavior;
import eng.jAtcSim.newLib.area.approaches.behaviors.LandingBehavior;
import eng.jAtcSim.newLib.area.approaches.conditions.*;
import eng.jAtcSim.newLib.area.approaches.locations.FixRelatedLocation;
import eng.jAtcSim.newLib.area.approaches.locations.ILocation;
import eng.jAtcSim.newLib.area.approaches.perCategoryValues.DoublePerCategoryValue;
import eng.jAtcSim.newLib.area.approaches.perCategoryValues.IntegerPerCategoryValue;
import eng.jAtcSim.newLib.area.routes.GaRoute;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.shared.enums.ApproachType;
import eng.jAtcSim.newLib.shared.xml.XmlLoadException;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.ICommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.ChangeAltitudeCommand;
import eng.jAtcSim.newLib.xml.area.internal.XmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.context.Context;

public class ApproachXmlLoader extends XmlLoader<Approach> {

  private static double convertGlidePathDegreesToSlope(double gpDegrees) {
    return Math.tan(Math.toRadians(gpDegrees));
  }

  protected ApproachXmlLoader(Context context) {
    super(context);
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
    Double glidePathPercentage = XmlLoaderUtils.loadDouble("glidePathPercentage", 3d);
    double slope = convertGlidePathDegreesToSlope(glidePathPercentage);

    // build approach entry
    IList<ApproachEntry> entries = new EList<>();
    IReadOnlyList<IafRoute> iafRoutes = context.airport.iafMappings.get(iafMapping);
    for (IafRoute iafRoute : iafRoutes) {
      ILocation entryLocation = ApproachFactory.Entry.Location.createApproachEntryLocationForRoute(iafRoute, context.area.navaids);
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
          PlaneShaCondition.createAsMinimalAltitude(IntegerPerCategoryValue.create(daA, daB, daC, daD)),
          RunwayThresholdVisibilityCondition.create()
      );
      ICondition errorCondition = AggregatingCondition.create(
          AggregatingCondition.eConditionAggregator.or,
          PlaneShaCondition.createAsMinimalAltitude(IntegerPerCategoryValue.create(daA, daB, daC, daD)),
          PlaneOrderedAltitudeDifference.create(IntegerPerCategoryValue.create(1000))
      );
      stages.add(new ApproachStage(
          FlyRadialWithDescentBehavior.create(threshold.coordinate, radial, threshold.altitude,
              DoublePerCategoryValue.create(slope)),
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

  private Approach loadUnprecise(XElement source) {
    XmlLoaderUtils.setContext(source);
    String gaMapping = XmlLoaderUtils.loadString("gaMapping");
    String iafMapping = XmlLoaderUtils.loadString("iafMapping");
    int mdaA = XmlLoaderUtils.loadInteger("mdaA");
    int mdaB = XmlLoaderUtils.loadInteger("mdaB");
    int mdaC = XmlLoaderUtils.loadInteger("mdaC");
    int mdaD = XmlLoaderUtils.loadInteger("mdaD");
    int radial = XmlLoaderUtils.loadInteger("radial");
    ApproachType approachType = XmlLoaderUtils.loadEnum("type", ApproachType.class);
    EAssert.isTrue(approachType == ApproachType.ndb || approachType == ApproachType.vor);
    String fafName = XmlLoaderUtils.loadString("faf");
    String maptName = XmlLoaderUtils.loadString("mapt");
    int initialAltitude = XmlLoaderUtils.loadInteger("initialAltitude");
//    double slope = ?? get from trheshold +

    Navaid faf = navaids.getWithPBD(fafName);
    Navaid mapt = navaids.getWithPBD(maptName);
    tady dopsat načetl jsem tyhle dva a někde se pro ně musí dynamciyk počítat slope

    // build approach entry
    IList<ApproachEntry> entries = new EList<>();
    IReadOnlyList<IafRoute> iafRoutes = this.iafRoutes.get(iafMapping);
    for (IafRoute iafRoute : iafRoutes) {
      ILocation entryLocation = ApproachFactory.Entry.Location.createApproachEntryLocationForRoute(iafRoute, this.navaids);
      ApproachEntry entry = new ApproachEntry(entryLocation, iafRoute);
      entries.add(entry);
    }

    // entry via faf before faf
    {
      ILocation entryLocation = ApproachFactory.Entry.Location.createApproachEntryLocationForFAF(
          fafName, threshold.course, navaids);
      ApproachEntry entry = new ApproachEntry(entryLocation, null);
      entries.add(entry);
    }
    // entry before mapt to faf
    {
      ILocation entryLocation = ApproachFactory.Entry.Location.createApproachEntryLocationBetweenMaptAndFaf(
          maptName, fafName, navaids);
      ApproachEntry entry = new ApproachEntry(entryLocation, null);
      entries.add(entry);
    }

    GaRoute gaRoute = gaRoutes.get(gaMapping).getFirst();
    IList<ICommand> beforeStagesCommands = EList.of(
        ChangeAltitudeCommand.createDescend(initialAltitude));

    IList<ApproachStage> stages = new EList<>();
    { // radial descent stage
      Navaid mapt = navaids.get(maptName);
      ICondition exitCondition = AggregatingCondition.create(
          AggregatingCondition.eConditionAggregator.and,
          LocationCondition.create(new FixRelatedLocation(mapt.getCoordinate(), 0, 359, 0.5)),
          PlaneShaCondition.create(
              IntegerPerCategoryValue.create(mdaA, mdaB, mdaC, mdaD),
              IntegerPerCategoryValue.create(mdaA+500, mdaB+500, mdaC+500, mdaD+500),
              null,null,null,null),
          RunwayThresholdVisibilityCondition.create()
      );
      ICondition errorCondition = AggregatingCondition.create(
          AggregatingCondition.eConditionAggregator.or,
          PlaneShaCondition.createAsMinimalAltitude(IntegerPerCategoryValue.create(mdaA, mdaB, mdaC, mdaD)),
          PlaneOrderedAltitudeDifference.create(IntegerPerCategoryValue.create(1000))
      );

      DoublePerCategoryValue slope = DoublePerCategoryValue.create(

      );
      stages.add(new ApproachStage(
          FlyRadialWithDescentBehavior.create(threshold.coordinate, radial, threshold.altitude, slope),
          exitCondition,
          errorCondition,
          approachType + " radial " + threshold.name
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
