package eng.jAtcSim.lib.world.approaches;

import eng.eSystem.utilites.CollectionUtils;
import eng.eSystem.xmlSerialization.XmlOptional;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.global.KeyList;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.parsing.Parser;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.ShortParser;
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

  private String gaRoute;
  private SpeechList<IAtcCommand> _gaCommands;
  @XmlOptional
  private KeyList<IafRoute, Navaid> iafRoutes = null;
  private int radial;
  private RunwayThreshold parent;

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
      case visual:
        ret = tryGetFromVisual(apps, category, currentPlaneLocation);
        break;
      case ndb:
      case vor:
        ret = tryGetFromUnprecise(apps, category, type, currentPlaneLocation);
        break;
      default:
        throw new UnsupportedOperationException();
    }
    return ret;
  }

  public static SpeechList<IAtcCommand> parseRoute(String route) {
    Parser p = new ShortParser();
    SpeechList<IAtcCommand> ret = p.parseMultipleCommands(route);
    return ret;
  }

  private static Navaid tryGetIafNavaidCloseToPlaneLocation(Approach approach, Coordinate planeLocation){
    List<Navaid> iafNavs = CollectionUtils.select(approach.getIafRoutes(), q->q.getNavaid());
    Navaid ret = CollectionUtils.tryGetFirst(iafNavs, o -> Coordinates.getDistanceInNM(planeLocation, o.getCoordinate()) < 2);
    return ret;
  }

  private static CurrentApproachInfo tryGetFromILS(List<Approach> apps, char category, ApproachType type, Coordinate planeLocation) {
    CurrentApproachInfo ret;
    IlsApproach tmp = (IlsApproach) CollectionUtils.tryGetFirst(apps, o -> o instanceof IlsApproach);
    IlsApproach.Type catKey = typeToIlsType(type);
    IlsApproach.Category cat = tmp.getCategories().get(catKey);

    Navaid iaf = tryGetIafNavaidCloseToPlaneLocation(tmp, planeLocation);

    IafRoute iafRoute = tmp.getIafRoutes().tryGet(iaf);
    SpeechList<IFromAtc> iafCommands;
    if (iafRoute != null)
      iafCommands = new SpeechList<>(iafRoute.getRouteCommands());
    else
      iafCommands = new SpeechList<>();
    SpeechList<IFromAtc> gaCommands = new SpeechList<>(tmp.getGaCommands());
    Coordinate faf = Coordinates.getCoordinate(
        tmp.getParent().getCoordinate(), Headings.getOpposite(tmp.getRadial()), 10);
    Coordinate mapt = tmp.getParent().getCoordinate();
    int course = tmp.getRadial();
    int mda = cat.getDA(category);


    ret = new CurrentApproachInfo(
        tmp.getParent(), iafCommands, gaCommands, type, faf, mapt, course, mda);
    return ret;
  }

  private static CurrentApproachInfo tryGetFromVisual(List<Approach> apps, char category, Coordinate currentLocation) {
    CurrentApproachInfo ret;
    VisualApproach tmp = (VisualApproach) CollectionUtils.tryGetFirst(apps, o -> o instanceof VisualApproach);

    SpeechList<IFromAtc> iafCommands = new SpeechList<>();
    SpeechList<IFromAtc> gaCommands = new SpeechList<>(tmp.getGaCommands());

    Coordinate faf = currentLocation;
    Coordinate mapt = Coordinates.getCoordinate(
        tmp.getParent().getCoordinate(),
        Headings.getOpposite(tmp.getParent().getCourse()),
        4);
    int course = (int) Math.round(tmp.getParent().getCourse());
    int mda = tmp.getParent().getParent().getParent().getAltitude() + 500;


    ret = new CurrentApproachInfo(
        tmp.getParent(), iafCommands, gaCommands, ApproachType.visual, faf, mapt, course, mda);
    return ret;
  }

  private static CurrentApproachInfo tryGetFromUnprecise(List<Approach> apps, char category, Approach.ApproachType type, Coordinate planeLocation) {
    CurrentApproachInfo ret;
    List<Approach> lst = CollectionUtils.where(apps, o -> o instanceof UnpreciseApproach );
    UnpreciseApproach.Type utype = typeToUnpreciseType(type);
    UnpreciseApproach tmp = (UnpreciseApproach) CollectionUtils.tryGetFirst(lst, o -> ((UnpreciseApproach) o).getType() == utype);

    Navaid iaf = tryGetIafNavaidCloseToPlaneLocation(tmp, planeLocation);

    IafRoute iafRoute = tmp.getIafRoutes().tryGet(iaf);
    SpeechList<IFromAtc> iafCommands;
    if (iafRoute != null)
      iafCommands = new SpeechList<>(iafRoute.getRouteCommands());
    else
      iafCommands = new SpeechList<>();
    SpeechList<IFromAtc> gaCommands = new SpeechList<>(tmp.getGaCommands());
    Coordinate faf = tmp.getFaf().getCoordinate();
    Coordinate mapt =  tmp.getMAPt();
    int course = tmp.getRadial();
    int mda = tmp.getMDA(category);

    ret = new CurrentApproachInfo(
        tmp.getParent(), iafCommands, gaCommands, ApproachType.visual, faf, mapt, course, mda);
    return ret;
  }

  private static CurrentApproachInfo tryGetFromGnss(List<Approach> apps, char category, Coordinate planeLocation) {
    CurrentApproachInfo ret;
    GnssApproach tmp = (GnssApproach) CollectionUtils.tryGetFirst(apps, o -> o instanceof  GnssApproach);

    Navaid iaf = tryGetIafNavaidCloseToPlaneLocation(tmp, planeLocation);
    IafRoute iafRoute = tmp.getIafRoutes().tryGet(iaf);
    SpeechList<IFromAtc> iafCommands;
    if (iafRoute != null)
      iafCommands = new SpeechList<>(iafRoute.getRouteCommands());
    else
      iafCommands = new SpeechList<>();
    SpeechList<IFromAtc> gaCommands = new SpeechList<>(tmp.getGaCommands());
    Coordinate faf = Coordinates.getCoordinate(
        tmp.getParent().getCoordinate(), Headings.getOpposite(tmp.getRadial()), 10);;
    Coordinate mapt = tmp.getParent().getCoordinate();
    int course = tmp.getRadial();
    int mda = tmp.getDA(category);

    ret = new CurrentApproachInfo(
        tmp.getParent(), iafCommands, gaCommands, ApproachType.visual, faf, mapt, course, mda);
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

  public KeyList<IafRoute, Navaid> getIafRoutes() {
    return iafRoutes;
  }

  public SpeechList<IAtcCommand> getGaCommands() {
    return _gaCommands;
  }

  public int getRadial() {
    return radial;
  }

  public void bind() {
    _gaCommands = parseRoute(gaRoute);

    if (this.iafRoutes == null){
      this.iafRoutes = new KeyList<>();
      this.iafRoutes.addAll(this.getParent().getIafRoutes());
    }

    for (IafRoute iafRoute : iafRoutes) {
      iafRoute.bind();
    }

    _bind();
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
