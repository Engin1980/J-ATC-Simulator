package eng.jAtcSim.newLib.area.approaches;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.area.approaches.entryLocations.FixRelatedApproachEntryLocation;
import eng.jAtcSim.newLib.area.approaches.entryLocations.IApproachEntryLocation;
import eng.jAtcSim.newLib.area.approaches.entryLocations.RegionalApproachEntryLocation;
import eng.jAtcSim.newLib.area.approaches.stages.IApproachStage;
import eng.jAtcSim.newLib.area.approaches.stages.LandingStage;
import eng.jAtcSim.newLib.area.approaches.stages.RadialWithDescendStage;
import eng.jAtcSim.newLib.area.approaches.stages.RouteStage;
import eng.jAtcSim.newLib.area.approaches.stages.checks.CheckAirportVisibilityStage;
import eng.jAtcSim.newLib.area.approaches.stages.exitConditions.AltitudeExitCondition;
import eng.jAtcSim.newLib.area.approaches.stages.exitConditions.CoordinateCloseExitCondition;
import eng.jAtcSim.newLib.area.approaches.stages.exitConditions.CoordinatePassedExitCondition;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.Parentable;
import eng.jAtcSim.newLib.area.routes.GaRoute;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.shared.UnitProvider;
import eng.jAtcSim.newLib.shared.xml.XmlLoadException;
import eng.jAtcSim.newLib.area.speeches.IAtcCommand;
import eng.jAtcSim.newLib.area.speeches.atc2airplane.ChangeAltitudeCommand;
import eng.jAtcSim.newLib.area.speeches.atc2airplane.ChangeHeadingCommand;
import eng.jAtcSim.newLib.area.speeches.atc2airplane.ProceedDirectCommand;
import eng.jAtcSim.newLib.area.speeches.atc2airplane.ThenCommand;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

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

  public static Approach generateDefaultVisualApproach(ActiveRunwayThreshold threshold) {

    final double VISUAL_DISTANCE = 8;
    final double VFAF_DISTANCE = 2.5;
    final int FINAL_ALTITUDE =
        (threshold.getParent().getParent().getAltitude() / 1000 + 2) * 1000;
    final int BASE_ALTITUDE =
        (threshold.getParent().getParent().getAltitude() / 1000 + 3) * 1000;

    IApproachEntryLocation entryLocation;
    IList<IAtcCommand> gaCommands = new EList<>();
    gaCommands.add(ChangeHeadingCommand.createContinueCurrentHeading());
    gaCommands.add(ChangeAltitudeCommand.createClimb(BASE_ALTITUDE));
    GaRoute gaRoute = GaRoute.create(gaCommands);
    IList<IApproachStage> stages;
    Approach ret;

    Navaid vfaf = threshold.getParent().getParent().getParent().getNavaids().getOrGenerate(
        threshold.getFullName() + "_VFAF",
        Coordinates.getCoordinate(
            threshold.getCoordinate(),
            Headings.getOpposite(threshold.getCourse()),
            VFAF_DISTANCE));
    Navaid vlft = threshold.getParent().getParent().getParent().getNavaids().getOrGenerate(
        threshold.getFullName() + "_VRGT",
        Coordinates.getCoordinate(vfaf.getCoordinate(),
            Headings.add(threshold.getCourse(), 90),
            VFAF_DISTANCE));
    Navaid vrgt = threshold.getParent().getParent().getParent().getNavaids().getOrGenerate(
        threshold.getFullName() + "_VLFT",
        Coordinates.getCoordinate(vfaf.getCoordinate(),
            Headings.add(threshold.getCourse(), -90),
            VFAF_DISTANCE));

    Coordinate a = Coordinates.getCoordinate(
        vfaf.getCoordinate(), threshold.getCourse(), VFAF_DISTANCE + VISUAL_DISTANCE);
    Coordinate rb = Coordinates.getCoordinate(
        a, Headings.add(threshold.getCourse(), 90), VISUAL_DISTANCE);
    Coordinate rc = Coordinates.getCoordinate(vfaf.getCoordinate(), Headings.add(threshold.getCourse(), 90), VISUAL_DISTANCE);
    Coordinate lb = Coordinates.getCoordinate(
        a, Headings.add(threshold.getCourse(), -90), VISUAL_DISTANCE);
    Coordinate lc = Coordinates.getCoordinate(
        vfaf.getCoordinate(), Headings.add(threshold.getCourse(), -90), VISUAL_DISTANCE);

    IList<IAtcCommand> iafRoute;
    IList<ApproachEntry> entries = new EList<>();
    ApproachEntry entry;

    // direct approach
    entryLocation = new FixRelatedApproachEntryLocation(vfaf.getCoordinate(), VISUAL_DISTANCE,
        Headings.add(threshold.getCourse(), 90), Headings.add(threshold.getCourse(), -90));
    iafRoute = new EList<>();
    iafRoute.add(ChangeAltitudeCommand.createDescend(FINAL_ALTITUDE));
    iafRoute.add(ProceedDirectCommand.create(vfaf));
    entry = ApproachEntry.createForVisual(threshold.getParent().getParent(), vfaf, entryLocation, iafRoute);
    entries.add(entry);


    // right pattern approach
    entryLocation = new RegionalApproachEntryLocation(
        a, rb, rc, vfaf.getCoordinate());
    iafRoute = new EList<>();
    iafRoute.add(ChangeAltitudeCommand.createDescend(BASE_ALTITUDE));
    iafRoute.add(ProceedDirectCommand.create(vrgt));
    iafRoute.add(ThenCommand.create());
    iafRoute.add(ChangeAltitudeCommand.createDescend(FINAL_ALTITUDE));
    iafRoute.add(ProceedDirectCommand.create(vfaf));
    entry = ApproachEntry.createForVisual(threshold.getParent().getParent(), vfaf, entryLocation, iafRoute);
    entries.add(entry);


    // left pattern approach
    entryLocation = new RegionalApproachEntryLocation(
        a, lb, lc, vfaf.getCoordinate());
    iafRoute = new EList<>();
    iafRoute.add(ChangeAltitudeCommand.createDescend(BASE_ALTITUDE));
    iafRoute.add(ProceedDirectCommand.create(vlft));
    iafRoute.add(ThenCommand.create());
    iafRoute.add(ChangeAltitudeCommand.createDescend(FINAL_ALTITUDE));
    iafRoute.add(ProceedDirectCommand.create(vfaf));
    entry = ApproachEntry.createForVisual(threshold.getParent().getParent(), vfaf, entryLocation, iafRoute);
    entries.add(entry);

    // stages
    stages = new EList<>();
    stages.add(
        new RadialWithDescendStage(
            threshold.getCoordinate(),
            threshold.getCourseInt(),
            threshold.getParent().getParent().getAltitude(), 3,
            new AltitudeExitCondition(
                AltitudeExitCondition.eDirection.below,
                threshold.getParent().getParent().getAltitude() + 1000)));

    ret = new Approach();
    ret.type = Approach.ApproachType.visual;
    ret.entries = entries;
    ret.stages = stages;
    ret.gaRoute = gaRoute;
    ret.setParent(threshold);
    return ret;
  }

  public static Approach load(XElement source, ActiveRunwayThreshold parent) {
    Approach ret = new Approach();
    ret.setParent(parent);
    switch (source.getName()) {
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

  private static double convertGlidePathDegreesToSlope(double gpDegrees) {
    return Math.tan(Math.toRadians(gpDegrees));
  }

  private IList<ApproachEntry> entries;
  private IList<IApproachStage> stages;
  private GaRoute gaRoute;
  private ApproachType type;

  private Approach() {
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

  public void readAsCustom(XElement source) {
    throw new ToDoException();
  }

  public void readAsUnprecise(XElement source) {
    XmlLoaderUtils.setContext(source);
    String gaMapping = XmlLoaderUtils.loadString("gaMapping");
    String iafMapping = XmlLoaderUtils.loadString("iafMapping");
    String fafName = XmlLoaderUtils.loadString("faf");
    String maptName = XmlLoaderUtils.loadString("mapt");
    int initialAltitude = XmlLoaderUtils.loadInteger("initialAltitude");
    //TODO why the following values are ignored?
    int daA = XmlLoaderUtils.loadInteger("mdaA");
    int daB = XmlLoaderUtils.loadInteger("mdaB");
    int daC = XmlLoaderUtils.loadInteger("mdaC");
    int daD = XmlLoaderUtils.loadInteger("mdaD");
    Navaid faf = this.getParent().getParent().getParent().getParent().getNavaids().get(fafName);
    Navaid mapt = this.getParent().getParent().getParent().getParent().getNavaids().getOrGenerate(
        maptName, this.getParent().getParent().getParent());
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
            ProceedDirectCommand.create(faf)));
    this.stages.add(
        new RadialWithDescendStage(faf.getCoordinate(), (int) Math.round(inboundRadial),
            initialAltitude, -slope,
            new CoordinatePassedExitCondition(mapt.getCoordinate(), inboundRadial)));
    this.stages.add(new CheckAirportVisibilityStage());
    this.stages.add(new LandingStage());
  }

  private void readAsGnss(XElement source) {
    XmlLoaderUtils.setContext(source);
    String gaMapping = XmlLoaderUtils.loadString("gaMapping");
    String iafMapping = XmlLoaderUtils.loadString("iafMapping");
    int daA = XmlLoaderUtils.loadInteger("daA");
    int daB = XmlLoaderUtils.loadInteger("daB");
    int daC = XmlLoaderUtils.loadInteger("daC");
    int daD = XmlLoaderUtils.loadInteger("daD");
    Double tmp = XmlLoaderUtils.loadDouble("glidePathPercentage", 3d);
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

  private void readAsILS(XElement source) {

    XmlLoaderUtils.setContext(source);
    Double tmp = XmlLoaderUtils.loadDouble("glidePathDegrees", 3d);
    String gaMapping = XmlLoaderUtils.loadString("gaMapping");
    String iafMapping = XmlLoaderUtils.loadString("iafMapping");
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
      XmlLoaderUtils.setContext(child);
      int daA = XmlLoaderUtils.loadInteger("daA");
      int daB = XmlLoaderUtils.loadInteger("daB");
      int daC = XmlLoaderUtils.loadInteger("daC");
      int daD = XmlLoaderUtils.loadInteger("daD");
      String ilsType = XmlLoaderUtils.loadStringRestricted("type", new String[]{"I", "II", "III"});

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
}
