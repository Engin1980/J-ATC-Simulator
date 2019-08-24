package eng.jAtcSim.lib.world.approaches;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.exceptions.ToDoException;
import eng.jAtcSim.lib.global.UnitProvider;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ProceedDirectCommand;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.NavaidList;
import eng.jAtcSim.lib.world.Parentable;
import eng.jAtcSim.lib.world.approaches.stages.IApproachStage;
import eng.jAtcSim.lib.world.approaches.stages.LandingStage;
import eng.jAtcSim.lib.world.approaches.stages.RadialWithDescendStage;
import eng.jAtcSim.lib.world.approaches.stages.RouteStage;
import eng.jAtcSim.lib.world.approaches.stages.checks.CheckAirportVisibilityStage;
import eng.jAtcSim.lib.world.approaches.stages.exitConditions.AltitudeExitCondition;
import eng.jAtcSim.lib.world.approaches.stages.exitConditions.CoordinateCloseExitCondition;
import eng.jAtcSim.lib.world.approaches.stages.exitConditions.CoordinatePassedExitCondition;
import eng.jAtcSim.lib.world.xml.XmlLoader;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class Approach extends Parentable<ActiveRunwayThreshold> {

  public enum ApproachType {
    ils_I,
    ils_II,
    ils_III,
    ndb,
    vor,
    gnss,
    visual
  }

  public static IList<Approach> loadList(IReadOnlyList<XElement> sources,
                                         Coordinate thresholdCoordinate, int thresholdCourse, int thresholdAltitude,
                                         NavaidList navaids,
                                         IReadOnlyList<IafRoute> iafRoutes,
                                         IReadOnlyList<GaRoute> gaRoutes) {
    IList<Approach> ret = new EList<>();

    for (XElement source : sources) {
      if (source.getName().equals("ilsApproach")) {
        IList<Approach> tmp = loadIlss(source, thresholdCoordinate, thresholdCourse, thresholdAltitude,
             iafRoutes, gaRoutes);
        ret.add(tmp);
      } else if (source.getName().equals("gnssApproach")) {
        Approach tmp = loadGnss(source, thresholdCoordinate, thresholdCourse, thresholdAltitude,
            iafRoutes, gaRoutes);
        ret.add(tmp);
      } else if (source.getName().equals("unpreciseApproach")) {
        Approach tmp = loadUnprecise(source, thresholdCoordinate, thresholdAltitude,
            iafRoutes, gaRoutes, navaids);
        ret.add(tmp);
      } else if (source.getName().equals("customApproach")) {
        Approach tmp = loadCustom(source);
        ret.add(tmp);
      } else {
        throw new EApplicationException(sf("Unknown approach type '%s'.", source.getName()));
      }
    }
    return ret;
  }

  private static double convertGlidePathDegreesToSlope(double gpDegrees){
    return Math.tan(Math.toRadians(gpDegrees));
  }

  public static IList<Approach> loadIlss(XElement source, Coordinate thresholdCoordinate, int thresholdCourse, int thresholdAltitude, IReadOnlyList<IafRoute> iafRoutes, IReadOnlyList<GaRoute> gaRoutes) {
    IList<Approach> ret = new EList<>();
    XmlLoader.setContext(source);
    Double tmp = XmlLoader.loadDouble("glidePathDegrees", false);
    String gaMapping = XmlLoader.loadString("gaMapping", true);
    String iafMapping = XmlLoader.loadString("iafMapping", true);
    if (tmp == null) tmp = 3d;
    double slope = convertGlidePathDegreesToSlope(tmp);

    // build approach entry
    IList<ApproachEntry> entries = new EList<>();
    ApproachEntry ae;
    ae = ApproachEntry.createForIls(thresholdCoordinate, thresholdCourse);
    entries.add(ae);
    for (IafRoute iafRoute : iafRoutes.where(q -> q.isMappingMatch(iafMapping))) {
      ae = ApproachEntry.createForIaf(iafRoute);
      entries.add(ae);
    }

    // ga route
    GaRoute gaRoute = gaRoutes.getFirst(q -> q.isMappingMatch(gaMapping));

    // process ILS categories
    for (XElement child : source.getChild("categories").getChildren("category")) {
      XmlLoader.setContext(child);
      int daA = XmlLoader.loadInteger("daA", true);
      int daB = XmlLoader.loadInteger("daB", true);
      int daC = XmlLoader.loadInteger("daC", true);
      int daD = XmlLoader.loadInteger("daD", true);
      String ilsType = XmlLoader.loadString("type", true);

      ApproachType approachType = ilsType.equals("I") ?
          ApproachType.ils_I : ilsType.equals("II") ?
          ApproachType.ils_II : ilsType.equals("III") ?
          ApproachType.ils_III : ApproachType.visual;
      if (approachType == ApproachType.visual)
        throw new EApplicationException(sf("Unknown approach type '%s'.", ilsType));

      // build stages
      IList<IApproachStage> stages = new EList<>();
      stages.add(
          new RadialWithDescendStage(thresholdCoordinate, thresholdCourse,
              thresholdAltitude, slope,
              new AltitudeExitCondition(AltitudeExitCondition.eDirection.below, daA, daB, daC, daD)));
      stages.add(new CheckAirportVisibilityStage());
      stages.add(new LandingStage());

      Approach app = new Approach(approachType, entries, stages, gaRoute);

      ret.add(app);
    }

    return ret;
  }

  public static Approach loadGnss(XElement source, Coordinate thresholdCoordinate, int thresholdCourse, int thresholdAltitude, IReadOnlyList<IafRoute> iafRoutes, IReadOnlyList<GaRoute> gaRoutes) {
    XmlLoader.setContext(source);
    String gaMapping = XmlLoader.loadString("gaMapping", true);
    String iafMapping = XmlLoader.loadString("iafMapping", true);
    int daA = XmlLoader.loadInteger("daA", true);
    int daB = XmlLoader.loadInteger("daB", true);
    int daC = XmlLoader.loadInteger("daC", true);
    int daD = XmlLoader.loadInteger("daD", true);
    Double tmp = XmlLoader.loadDouble("glidePathPercentage", false);
    if (tmp == null) tmp = 3d;
    double slope = convertGlidePathDegreesToSlope(tmp);

    // build approach entry
    IList<ApproachEntry> entries = new EList<>();
    ApproachEntry ae;
    ae = ApproachEntry.createForIls(thresholdCoordinate, thresholdCourse);
    entries.add(ae);
    for (IafRoute iafRoute : iafRoutes.where(q -> q.isMappingMatch(iafMapping))) {
      ae = ApproachEntry.createForIaf(iafRoute);
      entries.add(ae);
    }

    // ga route
    GaRoute gaRoute = gaRoutes.getFirst(q -> q.isMappingMatch(gaMapping));

    // build stages
    IList<IApproachStage> stages = new EList<>();
    stages.add(
        new RadialWithDescendStage(thresholdCoordinate, thresholdCourse,
            thresholdAltitude, slope,
            new AltitudeExitCondition(AltitudeExitCondition.eDirection.below, daA, daB, daC, daD)));
    stages.add(new CheckAirportVisibilityStage());
    stages.add(new LandingStage());

    Approach ret = new Approach(ApproachType.gnss, entries, stages, gaRoute);
    return ret;
  }

  public static Approach loadUnprecise(XElement source, Coordinate thresholdCoordinate, int thresholdAltitude,
                                       IReadOnlyList<IafRoute> iafRoutes, IReadOnlyList<GaRoute> gaRoutes,
                                       NavaidList navaids) {
    XmlLoader.setContext(source);
    String gaMapping = XmlLoader.loadString("gaMapping", true);
    String iafMapping = XmlLoader.loadString("iafMapping", true);
    String fafName = XmlLoader.loadString("faf",true);
    String maptName = XmlLoader.loadString("mapt",true);
    int initialAltitude = XmlLoader.loadInteger("initialAltitude", true);
//    int outboundRadial = XmlLoader.loadInteger("radial",true);
//    int inboundRadial = Headings.getOpposite(outboundRadial);
    int daA = XmlLoader.loadInteger("mdaA", true);
    int daB = XmlLoader.loadInteger("mdaB", true);
    int daC = XmlLoader.loadInteger("mdaC", true);
    int daD = XmlLoader.loadInteger("mdaD", true);
    Navaid faf = navaids.get(fafName);
    Navaid mapt = navaids.getOrGenerate(maptName);
    double inboundRadial = Coordinates.getBearing(faf.getCoordinate(), mapt.getCoordinate());

    // build approach entry
    IList<ApproachEntry> entries = new EList<>();
    ApproachEntry ae;
    ae = ApproachEntry.createForUnprecise(faf.getCoordinate(), inboundRadial);
    entries.add(ae);
    for (IafRoute iafRoute : iafRoutes.where(q -> q.isMappingMatch(iafMapping))) {
      ae = ApproachEntry.createForIaf(iafRoute);
      entries.add(ae);
    }

    // double slope
    double slope;
    {
      double distance = Coordinates.getDistanceInNM(faf.getCoordinate(), thresholdCoordinate);
      distance = UnitProvider.nmToFt(distance);
      double altDelta = initialAltitude - thresholdAltitude;
      slope = altDelta / distance;
    }

    // ga route
    GaRoute gaRoute = gaRoutes.getFirst(q -> q.isMappingMatch(gaMapping));

    // build stages
    IList<IApproachStage> stages = new EList<>();
    stages.add(
        new RouteStage(
            new CoordinateCloseExitCondition(faf.getCoordinate(), 1d),
            new ProceedDirectCommand(faf)));
    stages.add(
        new RadialWithDescendStage(faf.getCoordinate(), (int) Math.round(inboundRadial),
            initialAltitude, -slope,
            new CoordinatePassedExitCondition(mapt.getCoordinate(), inboundRadial)));
    stages.add(new CheckAirportVisibilityStage());
    stages.add(new LandingStage());

    Approach ret = new Approach(ApproachType.gnss, entries, stages, gaRoute);
    return ret;
  }

  public static Approach loadCustom(XElement source) {
    throw new ToDoException();
  }

  private final IList<ApproachEntry> entries;
  private final IList<IApproachStage> stages;
  private final GaRoute gaRoute;
  private final ApproachType type;

  private Approach(ApproachType type,
                   IList<ApproachEntry> entries, IList<IApproachStage> stages, GaRoute gaRoute) {
    this.entries = entries;
    this.stages = stages;
    this.gaRoute = gaRoute;
    this.type = type;
  }


  public IReadOnlyList<ApproachEntry> getEntries() {
    return entries;
  }

  public ApproachType getType() {
    return type;
  }

  public IReadOnlyList<IApproachStage> getStages() {
    return stages;
  }

  public GaRoute getGaRoute() {
    return gaRoute;
  }
}
