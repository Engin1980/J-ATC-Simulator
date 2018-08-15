package eng.jAtcSim.lib.world.approaches;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.utilites.CollectionUtils;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.xmlSerialization.XmlIgnore;
import eng.eSystem.xmlSerialization.XmlOptional;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.global.UnitProvider;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeAltitudeCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ProceedDirectCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ThenCommand;
import eng.jAtcSim.lib.speaking.parsing.Parser;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.ShortBlockParser;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.RunwayThreshold;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

public abstract class Approach {

  public enum ApproachType {
    ils_I,
    ils_II,
    ils_III,
    ndb,
    vor,
    gnss,
    visual
  }
  @XmlIgnore
  int geographicalRadial;
  private String gaRoute;
  private SpeechList<IAtcCommand> _gaCommands;
  @XmlOptional
  private IList<IafRoute> iafRoutes = new EList<>();
  private int radial;
  private RunwayThreshold parent;
  private int initialAltitude;
  @XmlOptional
  private String includeIafRoutesGroups = null;

  public static CurrentApproachInfo tryGetCurrentApproachInfo(List<Approach> apps, char category, ApproachType type, Coordinate currentPlaneLocation) {
    CurrentApproachInfo ret;

    switch (type) {
      case ils_I:
      case ils_II:
      case ils_III:
        ret = tryGetFromILS(apps, category, type, currentPlaneLocation);
        break;
      case gnss:
        ret = tryGetFromGnss(apps, category, currentPlaneLocation);
        break;
      case ndb:
      case vor:
        ret = tryGetFromUnprecise(apps, category, type, currentPlaneLocation);
        break;
      case visual: // visual is created individually
      default:
        throw new UnsupportedOperationException();
    }
    return ret;
  }

  public static SpeechList<IAtcCommand> parseRoute(String route) {
    Parser p = new ShortBlockParser();
    SpeechList<IAtcCommand> ret = p.parseMultipleCommands(route);
    return ret;
  }

  public static CurrentApproachInfo createVisualApproachInfo(RunwayThreshold threshold, ApproachType type, Coordinate planeLocation) {

    double hdg = Headings.getOpposite(threshold.getCourse());

    int mda = threshold.getParent().getParent().getAltitude() + 300;
    int fafa = (int) NumberUtils.ceil(threshold.getParent().getParent().getAltitude() + 1500, 2);
    int gaa = fafa;
    Coordinate faf = Coordinates.getCoordinate(threshold.getCoordinate(), hdg, 3.3);
    Coordinate mapt = Coordinates.getCoordinate(threshold.getCoordinate(), hdg, 1);
    Navaid ptpNavaid = null;
    Navaid rwyNavaid = null;

    {
      double hdgToFaf = Coordinates.getBearing(planeLocation, faf);
      double deltaHdgAbs = Headings.getDifference(hdgToFaf, threshold.getCourse(), true);
      if (deltaHdgAbs > 100) {
        // needs ptp
        Coordinate ptp = null;
        double deltaHdg = Headings.getDifference(hdgToFaf, threshold.getCourse(), false);
        boolean isRight = (deltaHdgAbs != deltaHdg) ? true : false;
        double ptpRad = isRight ? threshold.getCourse() + 120 : threshold.getCourse() - 120;
        ptpRad = Headings.to(ptpRad);
        ptp = Coordinates.getCoordinate(faf, ptpRad, 2.0);

        String ptpNavaidName = threshold.getParent().getParent().getIcao() + threshold.getName() + (isRight ? "_RBE" : "_LBE");
        ptpNavaid = Acc.area().getNavaids().tryGet(ptpNavaidName);
        if (ptpNavaid == null) {
          ptpNavaid = new Navaid(ptpNavaidName, Navaid.eType.auxiliary, ptp);
          Acc.area().getNavaids().add(ptpNavaid);
        }
      }
    }

    {
      double hdgToRwy = Coordinates.getBearing(planeLocation, threshold.getOtherThreshold().getCoordinate());
      double deltaHdgAbs = Headings.getDifference(hdgToRwy, threshold.getCourse(), true);
      if (deltaHdgAbs > 100) {
        // needs rwy
        Coordinate rwy = null;
        double deltaHdg = Headings.getDifference(hdgToRwy, threshold.getCourse(), false);
        boolean isRight = (deltaHdgAbs != deltaHdg) ? true : false;
        double rwyRad = isRight ? threshold.getCourse() + 90 : threshold.getCourse() - 90;
        rwyRad = Headings.to(rwyRad);
        rwy = Coordinates.getCoordinate(
            threshold.getOtherThreshold().getCoordinate(),
            rwyRad, 1.7);

        String rwyNavaidName = threshold.getParent().getParent().getIcao() + threshold.getName() + (isRight ? "_RBS" : "_LBS");
        rwyNavaid = Acc.area().getNavaids().tryGet(rwyNavaidName);
        if (rwyNavaid == null) {
          rwyNavaid = new Navaid(rwyNavaidName, Navaid.eType.auxiliary, rwy);
          Acc.area().getNavaids().add(rwyNavaid);
        }
      }
    }

    int crs = (int) Math.round(threshold.getCourse());

    SpeechList<IFromAtc> iafCmds = new SpeechList<>();
    iafCmds.add(new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.descend, fafa));
    if (rwyNavaid != null) {
      assert ptpNavaid != null;
      iafCmds.add(new ProceedDirectCommand(rwyNavaid));
      iafCmds.add(new ThenCommand());
    }
    if (ptpNavaid != null) {
      iafCmds.add(new ProceedDirectCommand(ptpNavaid));
      iafCmds.add(new ThenCommand());
      iafCmds.add(new ChangeHeadingCommand());
    }

    SpeechList<IFromAtc> gaCmds = new SpeechList<>();
    gaCmds.add(new ChangeHeadingCommand(crs, ChangeHeadingCommand.eDirection.any));
    gaCmds.add(new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.climb, gaa));

    // 3 is glide-path-default-percentage, rest of the formula is unknown source :-)
    double slope = UnitProvider.nmToFt(Math.tan(3 * Math.PI / 180));

    CurrentApproachInfo ret = new CurrentApproachInfo(
        threshold, false, iafCmds, gaCmds, ApproachType.visual, faf, mapt, crs, mda, slope,
        fafa);

    return ret;
  }

  private static Navaid tryGetIafNavaidCloseToPlaneLocation(Approach approach, Coordinate planeLocation) {
    List<Navaid> iafNavs = CollectionUtils.select(approach.getIafRoutes(), q -> q.getNavaid());
    Navaid ret = CollectionUtils.tryGetFirst(iafNavs, o -> Coordinates.getDistanceInNM(planeLocation, o.getCoordinate()) < 3);
    return ret;
  }

  private static double getSlope(double glidePathPercentage) {
    return UnitProvider.nmToFt(Math.tan(glidePathPercentage * Math.PI / 180));
  }

  private static IafRoute tryGetIafRoute(IList<IafRoute> routes, Navaid iaf, char category) {
    IafRoute ret = routes.tryGetFirst(q -> q.getNavaid().equals(iaf) && q.getCategory().contains(category));
    return ret;
  }

  private static CurrentApproachInfo tryGetFromILS(List<Approach> apps, char category, ApproachType type, Coordinate planeLocation) {
    CurrentApproachInfo ret;
    IlsApproach tmp = (IlsApproach) CollectionUtils.tryGetFirst(apps, o -> o instanceof IlsApproach);
    IlsApproach.Type catKey = typeToIlsType(type);
    IlsApproach.Category cat = tmp.getCategories().getFirst(q -> q.getType() == catKey);

    Navaid iaf = tryGetIafNavaidCloseToPlaneLocation(tmp, planeLocation);

    IafRoute iafRoute = tryGetIafRoute(tmp.getIafRoutes(), iaf, category);
    SpeechList<IFromAtc> iafCommands;
    boolean usingIaf = iafRoute != null;
    if (iafRoute != null)
      iafCommands = new SpeechList<>(iafRoute.getRouteCommands());
    else
      iafCommands = new SpeechList<>();
    iafCommands.add(new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.descend, tmp.getInitialAltitude()));
    SpeechList<IFromAtc> gaCommands = new SpeechList<>(tmp.getGaCommands());
    Coordinate faf = Coordinates.getCoordinate(
        tmp.getParent().getCoordinate(), Headings.getOpposite(tmp.getGeographicalRadial()), 10);
    Coordinate mapt = tmp.getParent().getCoordinate();
    int course = tmp.getGeographicalRadial();
    int mda = cat.getDA(category);
    double slope = getSlope(tmp.getGlidePathPercentage());


    ret = new CurrentApproachInfo(
        tmp.getParent(), usingIaf, iafCommands, gaCommands, type, faf, mapt, course, mda, slope, tmp.getInitialAltitude());
    return ret;
  }

  private static CurrentApproachInfo tryGetFromUnprecise(List<Approach> apps, char category, Approach.ApproachType type, Coordinate planeLocation) {
    CurrentApproachInfo ret;
    List<Approach> lst = CollectionUtils.where(apps, o -> o instanceof UnpreciseApproach);
    UnpreciseApproach.Type utype = typeToUnpreciseType(type);
    UnpreciseApproach tmp = (UnpreciseApproach) CollectionUtils.tryGetFirst(lst, o -> ((UnpreciseApproach) o).getType() == utype);

    Navaid iaf = tryGetIafNavaidCloseToPlaneLocation(tmp, planeLocation);

    IafRoute iafRoute = tryGetIafRoute(tmp.getIafRoutes(), iaf, category);
    boolean usingIaf = iafRoute != null;
    SpeechList<IFromAtc> iafCommands;
    if (iafRoute != null)
      iafCommands = new SpeechList<>(iafRoute.getRouteCommands());
    else
      iafCommands = new SpeechList<>();
    iafCommands.add(new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.descend, tmp.getInitialAltitude()));
    SpeechList<IFromAtc> gaCommands = new SpeechList<>(tmp.getGaCommands());
    Coordinate faf = tmp.getFaf().getCoordinate();
    Coordinate mapt = tmp.getMAPt();
    int course = tmp.getGeographicalRadial();
    int mda = tmp.getMDA(category);

    double faf2maptDistance = Coordinates.getDistanceInNM(faf, mapt);
    double faf2maptAltitude = tmp.getInitialAltitude() - mda;
    double slope = faf2maptAltitude / faf2maptDistance;

    ret = new CurrentApproachInfo(
        tmp.getParent(), usingIaf, iafCommands, gaCommands, type, faf, mapt, course, mda, slope, tmp.getInitialAltitude());
    return ret;
  }

  private static CurrentApproachInfo tryGetFromGnss(List<Approach> apps, char category, Coordinate planeLocation) {
    CurrentApproachInfo ret;
    GnssApproach tmp = (GnssApproach) CollectionUtils.tryGetFirst(apps, o -> o instanceof GnssApproach);

    Navaid iaf = tryGetIafNavaidCloseToPlaneLocation(tmp, planeLocation);
    IafRoute iafRoute = tryGetIafRoute(tmp.getIafRoutes(), iaf, category);
    boolean usingIaf = iafRoute != null;
    SpeechList<IFromAtc> iafCommands;
    if (iafRoute != null)
      iafCommands = new SpeechList<>(iafRoute.getRouteCommands());
    else
      iafCommands = new SpeechList<>();
    iafCommands.add(new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.descend, tmp.getInitialAltitude()));
    SpeechList<IFromAtc> gaCommands = new SpeechList<>(tmp.getGaCommands());
    Coordinate faf = Coordinates.getCoordinate(
        tmp.getParent().getCoordinate(), Headings.getOpposite(tmp.getGeographicalRadial()), 10);
    Coordinate mapt = tmp.getParent().getCoordinate();
    int course = tmp.getGeographicalRadial();
    int mda = tmp.getDA(category);
    double slope = getSlope(tmp.getGlidePathPercentage());

    ret = new CurrentApproachInfo(
        tmp.getParent(), usingIaf, iafCommands, gaCommands, ApproachType.visual, faf, mapt, course, mda, slope, tmp.getInitialAltitude());
    return ret;
  }

  private static UnpreciseApproach.Type typeToUnpreciseType(ApproachType type) {
    switch (type) {
      case vor:
        return UnpreciseApproach.Type.vor;
      case ndb:
        return UnpreciseApproach.Type.ndb;
      default:
        throw new NotImplementedException();
    }
  }

  private static IlsApproach.Type typeToIlsType(ApproachType type) {
    switch (type) {
      case ils_I:
        return IlsApproach.Type.I;
      case ils_II:
        return IlsApproach.Type.II;
      case ils_III:
        return IlsApproach.Type.II;
      default:
        throw new NotImplementedException();
    }
  }

  public abstract String getTypeString();

  public int getInitialAltitude() {
    return initialAltitude;
  }

  public IList<IafRoute> getIafRoutes() {
    return iafRoutes;
  }

  public SpeechList<IAtcCommand> getGaCommands() {
    return _gaCommands;
  }

  public int getGeographicalRadial() {
    return geographicalRadial;
  }

  public int getMagneticalRadial() {
    return radial;
  }

  public void bind() {
    _gaCommands = parseRoute(gaRoute);

    if (this.includeIafRoutesGroups != null) {
      String[] groupNames = this.includeIafRoutesGroups.split(";");
      for (String groupName : groupNames) {
        Airport.SharedIafRoutesGroup group = this.getParent().getParent().getParent().getSharedIafRoutesGroups().tryGetFirst(q -> q.groupName.equals(groupName));
        if (group == null) {
          throw new EApplicationException("Unable to find iaf-route group named " + groupName + " in airport "
              + this.getParent().getParent().getParent().getIcao() + " required for runway approach " + this.getParent().getName() + " " + this.getTypeString() + ".");
        }

        this.iafRoutes.add(group.iafRoutes);
      }
    }

    this.iafRoutes.forEach(q -> q.bind());

    this._bind(); // bind in descendants

    this.geographicalRadial = (int) Math.round(
        Headings.add(this.radial,
            this.getParent().getParent().getParent().getDeclination()));
  }

  public String getGaRoute() {
    return gaRoute;
  }

  public RunwayThreshold getParent() {
    return parent;
  }

  public void setParent(RunwayThreshold parent) {
    this.parent = parent;
  }

  abstract protected void _bind();
}
