package eng.jAtcSim.lib.area.approaches;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.area.NavaidList;
import eng.jAtcSim.lib.area.Parentable;
import eng.jAtcSim.lib.area.approaches.stages.LandingStage;
import eng.jAtcSim.lib.area.approaches.stages.RadialWithDescendStage;
import eng.jAtcSim.lib.area.approaches.stages.RouteStage;
import eng.jAtcSim.lib.area.routes.GaRoute;
import eng.jAtcSim.lib.area.routes.IafRoute;
import eng.jAtcSim.lib.exceptions.ToDoException;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.global.UnitProvider;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeAltitudeCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ProceedDirectCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ThenCommand;
import eng.jAtcSim.lib.area.ActiveRunwayThreshold;
import eng.jAtcSim.lib.area.Navaid;
import eng.jAtcSim.lib.area.approaches.entryLocations.FixRelatedApproachEntryLocation;
import eng.jAtcSim.lib.area.approaches.entryLocations.IApproachEntryLocation;
import eng.jAtcSim.lib.area.approaches.entryLocations.RegionalApproachEntryLocation;
import eng.jAtcSim.lib.area.approaches.stages.IApproachStage;
import eng.jAtcSim.lib.area.approaches.stages.checks.CheckAirportVisibilityStage;
import eng.jAtcSim.lib.area.approaches.stages.exitConditions.AltitudeExitCondition;
import eng.jAtcSim.lib.area.approaches.stages.exitConditions.CoordinateCloseExitCondition;
import eng.jAtcSim.lib.area.approaches.stages.exitConditions.CoordinatePassedExitCondition;
import eng.jAtcSim.sharedLib.xml.XmlLoadException;
import eng.jAtcSim.sharedLib.xml.XmlLoader;

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

  public static Approach load(XElement source, ActiveRunwayThreshold parent){
    Approach ret =  new Approach();
    ret.setParent(parent);
    switch (source.getName()){
      case "ilsApproach":
        ret.readAsILS(source);
        break;
      case "gnssApproach":
        ret.readAsGnss(source);
        break;
      case "unpreciseApproach":
        ret.readAsUnprecise(source);
        break;
      case "customApproach":
        ret.readAsCustom(source);
        break;
      default:
        throw new XmlLoadException("Unknown approach type " + source.getName() + ".");
    }
    return ret;
  }

  private void readAsILS(XElement source) {

    XmlLoader.setContext(source);
    Double tmp = XmlLoader.loadDouble("glidePathDegrees", 3d);
    String gaMapping = XmlLoader.loadString("gaMapping");
    String iafMapping = XmlLoader.loadString("iafMapping");
    double slope = convertGlidePathDegreesToSlope(tmp);

    // build approach entries
    this.entries = new EList<>();
    ApproachEntry ae;
    ae = ApproachEntry.createForIls(this.getParent());
    this.entries.add(ae);
    for (IafRoute iafRoute : this.getParent().getParent().getParent().getIafRoutes().where(q -> q.isMappingMatch(iafMapping))) {
      ae = ApproachEntry.createForIaf(iafRoute);
      this.entries.add(ae);
    }

    // ga route
    this.gaRoute = this.getParent().getParent().getParent().getGaRoutes().getFirst(q -> q.isMappingMatch(gaMapping));

    // process ILS categories
    for (XElement child : source.getChild("categories").getChildren("category")) {
      XmlLoader.setContext(child);
      int daA = XmlLoader.loadInteger("daA");
      int daB = XmlLoader.loadInteger("daB");
      int daC = XmlLoader.loadInteger("daC");
      int daD = XmlLoader.loadInteger("daD");
      String ilsType = XmlLoader.loadStringRestricted("type", new String[]{"I", "II", "III"});

      switch (ilsType) {
        case "I":
          this.type = ApproachType.ils_I;
          break;
        case "II":
          this.type = ApproachType.ils_II;
          break;
        case "III":
          this.type = ApproachType.ils_III;
          break;
        default:
          throw new EApplicationException(sf("Unknown approach type '%s' for ILS.", ilsType));
      }

      // build stages
      this.stages = new EList<>();
      stages.add(
          new RadialWithDescendStage(
              this.getParent().getCoordinate(),
              this.getParent().getCourseInt(),
              this.getParent().getParent().getParent().getAltitude(), slope,
              new AltitudeExitCondition(AltitudeExitCondition.eDirection.below, daA, daB, daC, daD)));
      stages.add(new CheckAirportVisibilityStage());
      stages.add(new LandingStage());
    }
  }

  private void readAsGnss(XElement source) {
    XmlLoader.setContext(source);
    String gaMapping = XmlLoader.loadString("gaMapping");
    String iafMapping = XmlLoader.loadString("iafMapping");
    int daA = XmlLoader.loadInteger("daA");
    int daB = XmlLoader.loadInteger("daB");
    int daC = XmlLoader.loadInteger("daC");
    int daD = XmlLoader.loadInteger("daD");
    Double tmp = XmlLoader.loadDouble("glidePathPercentage", 3d);
    double slope = convertGlidePathDegreesToSlope(tmp);

    // build approach entry
    this.entries = new EList<>();
    ApproachEntry ae;
    ae = ApproachEntry.createForIls(this.getParent());
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

  public void readAsUnprecise(XElement source) {
    XmlLoader.setContext(source);
    String gaMapping = XmlLoader.loadString("gaMapping");
    String iafMapping = XmlLoader.loadString("iafMapping");
    String fafName = XmlLoader.loadString("faf");
    String maptName = XmlLoader.loadString("mapt");
    int initialAltitude = XmlLoader.loadInteger("initialAltitude");
    int daA = XmlLoader.loadInteger("mdaA");
    int daB = XmlLoader.loadInteger("mdaB");
    int daC = XmlLoader.loadInteger("mdaC");
    int daD = XmlLoader.loadInteger("mdaD");
    Navaid faf = this.getParent().getParent().getParent().getParent().getNavaids().get(fafName);
    Navaid mapt = this.getParent().getParent().getParent().getParent().getNavaids().getOrGenerate(maptName);
    double inboundRadial = Coordinates.getBearing(faf.getCoordinate(), mapt.getCoordinate());

    // build approach entry
    this.entries = new EList<>();
    ApproachEntry ae;
    ae = ApproachEntry.createForUnprecise(faf.getCoordinate(), inboundRadial);
    this.entries.add(ae);
    for (IafRoute iafRoute : this.getParent().getParent().getParent().getIafRoutes().where(q -> q.isMappingMatch(iafMapping))) {
      ae = ApproachEntry.createForIaf(iafRoute);
      this.entries.add(ae);
    }

    // double slope
    double slope;
    {
      double distance = Coordinates.getDistanceInNM(faf.getCoordinate(), this.getParent().getCoordinate());
      distance = UnitProvider.nmToFt(distance);
      double altDelta = initialAltitude - this.getParent().getParent().getParent().getAltitude();
      slope = altDelta / distance;
    }

    // ga route
    this.gaRoute = this.getParent().getParent().getParent().getGaRoutes().getFirst(q -> q.isMappingMatch(gaMapping));

    // build stages
    this.stages = new EList<>();
    this.stages.add(
        new RouteStage(
            new CoordinateCloseExitCondition(faf.getCoordinate(), 1d),
            new ProceedDirectCommand(faf)));
    this.stages.add(
        new RadialWithDescendStage(faf.getCoordinate(), (int) Math.round(inboundRadial),
            initialAltitude, -slope,
            new CoordinatePassedExitCondition(mapt.getCoordinate(), inboundRadial)));
    this.stages.add(new CheckAirportVisibilityStage());
    this.stages.add(new LandingStage());
  }

  public void readAsCustom(XElement source) {
    throw new ToDoException();
  }

  public static Approach generateDefaultVisualApproach(String thresholdFullName,
                                                       Coordinate thresholdCoordinate, double thresholdCourse,
                                                       int airportAltitude, NavaidList navaids) {

    final double VISUAL_DISTANCE = 8;
    final double VFAF_DISTANCE = 2.5;
    final int FINAL_ALTITUDE =
        (airportAltitude / 1000 + 2) * 1000;
    final int BASE_ALTITUDE =
        (airportAltitude / 1000 + 3) * 1000;

    IApproachEntryLocation entryLocation;
    IList<IAtcCommand> gaCommands = new EList<>();
    gaCommands.add(new ChangeHeadingCommand());
    gaCommands.add(new ChangeAltitudeCommand(
        ChangeAltitudeCommand.eDirection.climb, BASE_ALTITUDE));
    GaRoute gaRoute = GaRoute.create(gaCommands);
    IList<IApproachStage> stages;
    Approach ret;

    Navaid vfaf = navaids.getOrGenerate(
        thresholdFullName + "_VFAF",
        Coordinates.getCoordinate(
            thresholdCoordinate,
            Headings.getOpposite(thresholdCourse),
            VFAF_DISTANCE));
    Navaid vlft = navaids.getOrGenerate(
        thresholdFullName + "_VRGT",
        Coordinates.getCoordinate(vfaf.getCoordinate(),
            Headings.add(thresholdCourse, 90),
            VFAF_DISTANCE));
    Navaid vrgt = navaids.getOrGenerate(
        thresholdFullName + "_VLFT",
        Coordinates.getCoordinate(vfaf.getCoordinate(),
            Headings.add(thresholdCourse, -90),
            VFAF_DISTANCE));

    Coordinate a = Coordinates.getCoordinate(
        vfaf.getCoordinate(), thresholdCourse, VFAF_DISTANCE + VISUAL_DISTANCE);
    Coordinate rb = Coordinates.getCoordinate(
        a, Headings.add(thresholdCourse, 90), VISUAL_DISTANCE);
    Coordinate rc = Coordinates.getCoordinate(vfaf.getCoordinate(), Headings.add(thresholdCourse, 90), VISUAL_DISTANCE);
    Coordinate lb = Coordinates.getCoordinate(
        a, Headings.add(thresholdCourse, -90), VISUAL_DISTANCE);
    Coordinate lc = Coordinates.getCoordinate(
        vfaf.getCoordinate(), Headings.add(thresholdCourse, -90), VISUAL_DISTANCE);

    IList<IAtcCommand> iafRoute;
    IList<ApproachEntry> entries = new EList<>();
    ApproachEntry entry;

    // direct approach
    entryLocation = new FixRelatedApproachEntryLocation(vfaf.getCoordinate(), VISUAL_DISTANCE,
        Headings.add(thresholdCourse, 90), Headings.add(thresholdCourse, -90));
    iafRoute = new EList<>();
    iafRoute.add(
        new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.descend, FINAL_ALTITUDE));
    iafRoute.add(
        new ProceedDirectCommand(vfaf));
    entry = ApproachEntry.createForVisual(vfaf, entryLocation, iafRoute);
    entries.add(entry);


    // right pattern approach
    entryLocation = new RegionalApproachEntryLocation(
        a, rb, rc, vfaf.getCoordinate());
    iafRoute = new EList<>();
    iafRoute.add(new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.descend, BASE_ALTITUDE));
    iafRoute.add(new ProceedDirectCommand(vrgt));
    iafRoute.add(new ThenCommand());
    iafRoute.add(new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.descend, FINAL_ALTITUDE));
    iafRoute.add(new ProceedDirectCommand(vfaf));
    entry = ApproachEntry.createForVisual(vfaf, entryLocation, iafRoute);
    entries.add(entry);


    // left pattern approach
    entryLocation = new RegionalApproachEntryLocation(
        a, lb, lc, vfaf.getCoordinate());
    iafRoute = new EList<>();
    iafRoute.add(new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.descend, BASE_ALTITUDE));
    iafRoute.add(new ProceedDirectCommand(vlft));
    iafRoute.add(new ThenCommand());
    iafRoute.add(new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.descend, FINAL_ALTITUDE));
    iafRoute.add(new ProceedDirectCommand(vfaf));
    entry = ApproachEntry.createForVisual(vfaf, entryLocation, iafRoute);
    entries.add(entry);

    // stages
    stages = new EList<>();
    stages.add(
        new RadialWithDescendStage(thresholdCoordinate, (int) Math.round(thresholdCourse),
            airportAltitude, 3,
            new AltitudeExitCondition(AltitudeExitCondition.eDirection.below, airportAltitude + 1000)));

    ret = new Approach(Approach.ApproachType.visual, entries, stages, gaRoute);
    return ret;
  }

  private static double convertGlidePathDegreesToSlope(double gpDegrees) {
    return Math.tan(Math.toRadians(gpDegrees));
  }
  private IList<ApproachEntry> entries;
  private IList<IApproachStage> stages;
  private GaRoute gaRoute;
  private ApproachType type;

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

  public GaRoute getGaRoute() {
    return gaRoute;
  }

  public IReadOnlyList<IApproachStage> getStages() {
    return stages;
  }

  public ApproachType getType() {
    return type;
  }
}
