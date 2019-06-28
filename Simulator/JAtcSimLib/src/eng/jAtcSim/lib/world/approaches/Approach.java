//package eng.jAtcSim.lib.world.approaches;
//
//import eng.eSystem.collections.IList;
//import eng.eSystem.collections.IReadOnlyList;
//import eng.jAtcSim.lib.airplanes.pilots.approachStages.IApproachStage;
//import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
//import eng.jAtcSim.lib.speaking.SpeechList;
//import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
//import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
//import eng.jAtcSim.lib.world.newApproaches.entryLocations.ApproachEntryLocation;
//import eng.jAtcSim.lib.world.newApproaches.IafRoute;
//
//public class Approach {
//
//  public enum ApproachType {
//    ils_I,
//    ils_II,
//    ils_III,
//    ndb,
//    vor,
//    gnss,
//    visual
//  }
//
//  public ApproachType getType() {
//    return type;
//  }
//
//  //  public static CurrentApproachInfo tryGetCurrentApproachInfo(IList<Approach> apps, char category, ApproachType kind, Coordinate currentPlaneLocation) {
////    CurrentApproachInfo ret;
////
////    switch (kind) {
////      case ils_I:
////      case ils_II:
////      case ils_III:
////        ret = tryGetFromILS(apps, category, kind, currentPlaneLocation);
////        break;
////      case gnss:
////        ret = tryGetFromGnss(apps, category, currentPlaneLocation);
////        break;
////      case ndb:
////      case vor:
////        ret = tryGetFromUnprecise(apps, category, kind, currentPlaneLocation);
////        break;
////      case visual: // visual is created individually
////      default:
////        throw new UnsupportedOperationException();
////    }
////    return ret;
////  }
////
////  public static SpeechList<IAtcCommand> parseRoute(String route) {
////    Parser p = new ShortBlockParser();
////    SpeechList<IAtcCommand> ret = p.parseMultipleCommands(route);
////    return ret;
////  }
////
////  public static ApproachInfo createVisualApproachInfo(ActiveRunwayThreshold threshold, Coordinate planeLocation) {
////
////    double hdg = Headings.getOpposite(threshold.getCourse());
////
////    int mda = threshold.getParent().getParent().getAltitude() + 300;
////    int fafa = (int) NumberUtils.ceil(threshold.getParent().getParent().getAltitude() + 1500, 2);
////    int gaa = fafa;
////    Coordinate faf = Coordinates.getCoordinate(threshold.getCoordinate(), hdg, 3.3);
////    Coordinate mapt = Coordinates.getCoordinate(threshold.getCoordinate(), hdg, 1);
////    Navaid ptpNavaid = null;
////    Navaid rwyNavaid = null;
////
////    {
////      double hdgToFaf = Coordinates.getBearing(planeLocation, faf);
////      double deltaHdgAbs = Headings.getDifference(hdgToFaf, threshold.getCourse(), true);
////      if (deltaHdgAbs > 100) {
////        // needs ptp
////        Coordinate ptp = null;
////        double deltaHdg = Headings.getDifference(hdgToFaf, threshold.getCourse(), false);
////        boolean isRight = (deltaHdgAbs != deltaHdg) ? true : false;
////        double ptpRad = isRight ? threshold.getCourse() + 120 : threshold.getCourse() - 120;
////        ptpRad = Headings.to(ptpRad);
////        ptp = Coordinates.getCoordinate(faf, ptpRad, 2.0);
////
////        String ptpNavaidName = threshold.getParent().getParent().getIcao() + threshold.getName() + (isRight ? "_RBE" : "_LBE");
////        ptpNavaid = Acc.area().getNavaids().tryGet(ptpNavaidName);
////        if (ptpNavaid == null) {
////          ptpNavaid = new Navaid(ptpNavaidName, Navaid.eType.auxiliary, ptp);
////          Acc.area().getNavaids().add(ptpNavaid);
////        }
////      }
////    }
////
////    {
////      double hdgToRwy = Coordinates.getBearing(planeLocation, threshold.getOtherThreshold().getCoordinate());
////      double deltaHdgAbs = Headings.getDifference(hdgToRwy, threshold.getCourse(), true);
////      if (deltaHdgAbs > 100) {
////        // needs rwy
////        Coordinate rwy = null;
////        double deltaHdg = Headings.getDifference(hdgToRwy, threshold.getCourse(), false);
////        boolean isRight = (deltaHdgAbs != deltaHdg) ? true : false;
////        double rwyRad = isRight ? threshold.getCourse() + 90 : threshold.getCourse() - 90;
////        rwyRad = Headings.to(rwyRad);
////        rwy = Coordinates.getCoordinate(
////            threshold.getOtherThreshold().getCoordinate(),
////            rwyRad, 1.7);
////
////        String rwyNavaidName = threshold.getParent().getParent().getIcao() + threshold.getName() + (isRight ? "_RBS" : "_LBS");
////        rwyNavaid = Acc.area().getNavaids().tryGet(rwyNavaidName);
////        if (rwyNavaid == null) {
////          rwyNavaid = new Navaid(rwyNavaidName, Navaid.eType.auxiliary, rwy);
////          Acc.area().getNavaids().add(rwyNavaid);
////        }
////      }
////    }
////
////    int crs = (int) Math.round(threshold.getCourse());
////
////    SpeechList<IFromAtc> iafCmds = new SpeechList<>();
////    iafCmds.add(new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.descend, fafa));
////    if (rwyNavaid != null) {
////      assert ptpNavaid != null;
////      iafCmds.add(new ProceedDirectCommand(rwyNavaid));
////      iafCmds.add(new ThenCommand());
////    }
////    if (ptpNavaid != null) {
////      iafCmds.add(new ProceedDirectCommand(ptpNavaid));
////      iafCmds.add(new ThenCommand());
////      iafCmds.add(new ChangeHeadingCommand());
////    }
////
////    SpeechList<IFromAtc> gaCmds = new SpeechList<>();
////    gaCmds.add(new ChangeHeadingCommand(crs, ChangeHeadingCommand.eDirection.any));
////    gaCmds.add(new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.climb, gaa));
////
////    // 3 is glide-path-default-percentage, rest of the formula is from an unknown source :-)
////    double slope = UnitProvider.nmToFt(Math.tan(3 * Math.PI / 180));
////
////    // stages
////    IList<IApproachStage> stages = new EList<>();
////    IApproachStage stage;
////    if (iafCmds.isEmpty() == false) {
////      stage = new RouteStage(iafCmds);
////      stages.add(stage);
////    }
////    stage = new VisualFinalStage(threshold);
////    stages.add(stage);
////
////
////    ApproachInfo ret = new ApproachInfo(
////        ApproachType.visual,
////        threshold,
////        stages,
////        gaCmds);
////
////    return ret;
////  }
////
////  private static Navaid tryGetIafNavaidCloseToPlaneLocation(Approach approach, Coordinate planeLocation) {
////    List<Navaid> iafNavs = CollectionUtils.select(approach.getIafRoutes(), q -> q.getNavaid());
////    Navaid ret = CollectionUtils.tryGetFirst(iafNavs, o -> Coordinates.getDistanceInNM(planeLocation, o.getCoordinate()) < 3);
////    return ret;
////  }
////
////  private static double getSlope(double glidePathPercentage) {
////    return UnitProvider.nmToFt(Math.tan(glidePathPercentage * Math.PI / 180));
////  }
////
////  private static IafRoute tryGetIafRoute(IList<IafRoute> routes, Navaid iaf, char category) {
////    IafRoute ret = routes.tryGetFirst(q -> q.getNavaid().equals(iaf) && q.getCategory().contains(category));
////    return ret;
////  }
////
////  private static CurrentApproachInfo tryGetFromILS(IList<Approach> apps, char category, ApproachType kind, Coordinate planeLocation) {
////    CurrentApproachInfo ret;
////    IlsApproach ilsApproach = (IlsApproach) CollectionUtils.tryGetFirst(apps, o -> o instanceof IlsApproach);
////    IlsApproach.IlsCategory catKey = typeToIlsType(kind);
////    IlsApproach.Category cat = ilsApproach.getCategories().getFirst(q -> q.getType() == catKey);
////
////    Navaid iaf = tryGetIafNavaidCloseToPlaneLocation(ilsApproach, planeLocation);
////
////    IafRoute iafRoute = tryGetIafRoute(ilsApproach.getIafRoutes(), iaf, category);
////    SpeechList<IFromAtc> iafCommands;
////    boolean usingIaf = iafRoute != null;
////    if (iafRoute != null)
////      iafCommands = new SpeechList<>(iafRoute.getRouteCommands());
////    else
////      iafCommands = new SpeechList<>();
////    iafCommands.add(new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.descend, ilsApproach.getInitialAltitude()));
////    SpeechList<IFromAtc> gaCommands = new SpeechList<>(ilsApproach.getGaCommands());
////    Coordinate faf = Coordinates.getCoordinate(
////        ilsApproach.getParent().getCoordinate(), Headings.getOpposite(ilsApproach.getGeographicalRadial()), 10);
////    Coordinate mapt = ilsApproach.getParent().getCoordinate();
////    int course = ilsApproach.getGeographicalRadial();
////    int mda = cat.getDA(category);
////    double slope = getSlope(ilsApproach.getGlidePathPercentage());
////
////
////    ret = new CurrentApproachInfo(
////        ilsApproach.getParent(), usingIaf, iafCommands, gaCommands, kind, faf, mapt, course, mda, slope, ilsApproach.getInitialAltitude());
////    return ret;
////  }
////
////  private static CurrentApproachInfo tryGetFromUnprecise(IList<Approach> apps, char category, Approach.ApproachType kind, Coordinate planeLocation) {
////    CurrentApproachInfo ret;
////    List<Approach> lst = CollectionUtils.where(apps, o -> o instanceof UnpreciseApproach);
////    UnpreciseApproach.Kind utype = typeToUnpreciseType(kind);
////    UnpreciseApproach tmp = (UnpreciseApproach) CollectionUtils.tryGetFirst(lst, o -> ((UnpreciseApproach) o).getType() == utype);
////
////    Navaid iaf = tryGetIafNavaidCloseToPlaneLocation(tmp, planeLocation);
////
////    IafRoute iafRoute = tryGetIafRoute(tmp.getIafRoutes(), iaf, category);
////    boolean usingIaf = iafRoute != null;
////    SpeechList<IFromAtc> iafCommands;
////    if (iafRoute != null)
////      iafCommands = new SpeechList<>(iafRoute.getRouteCommands());
////    else
////      iafCommands = new SpeechList<>();
////    iafCommands.add(new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.descend, tmp.getInitialAltitude()));
////    SpeechList<IFromAtc> gaCommands = new SpeechList<>(tmp.getGaCommands());
////    Coordinate faf = tmp.getFaf().getCoordinate();
////    Coordinate mapt = tmp.getMAPt();
////    int course = tmp.getGeographicalRadial();
////    int mda = tmp.getMDA(category);
////
////    double faf2maptDistance = Coordinates.getDistanceInNM(faf, mapt);
////    double faf2maptAltitude = tmp.getInitialAltitude() - mda;
////    double slope = faf2maptAltitude / faf2maptDistance;
////
////    ret = new CurrentApproachInfo(
////        tmp.getParent(), usingIaf, iafCommands, gaCommands, kind, faf, mapt, course, mda, slope, tmp.getInitialAltitude());
////    return ret;
////  }
////
////  private static CurrentApproachInfo tryGetFromGnss(IList<Approach> apps, char category, Coordinate planeLocation) {
////    CurrentApproachInfo ret;
////    GnssApproach tmp = (GnssApproach) CollectionUtils.tryGetFirst(apps, o -> o instanceof GnssApproach);
////
////    Navaid iaf = tryGetIafNavaidCloseToPlaneLocation(tmp, planeLocation);
////    IafRoute iafRoute = tryGetIafRoute(tmp.getIafRoutes(), iaf, category);
////    boolean usingIaf = iafRoute != null;
////    SpeechList<IFromAtc> iafCommands;
////    if (iafRoute != null)
////      iafCommands = new SpeechList<>(iafRoute.getRouteCommands());
////    else
////      iafCommands = new SpeechList<>();
////    iafCommands.add(new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.descend, tmp.getInitialAltitude()));
////    SpeechList<IFromAtc> gaCommands = new SpeechList<>(tmp.getGaCommands());
////    Coordinate faf = Coordinates.getCoordinate(
////        tmp.getParent().getCoordinate(), Headings.getOpposite(tmp.getGeographicalRadial()), 10);
////    Coordinate mapt = tmp.getParent().getCoordinate();
////    int course = tmp.getGeographicalRadial();
////    int mda = tmp.getDA(category);
////    double slope = getSlope(tmp.getGlidePathPercentage());
////
////    ret = new CurrentApproachInfo(
////        tmp.getParent(), usingIaf, iafCommands, gaCommands, ApproachType.visual, faf, mapt, course, mda, slope, tmp.getInitialAltitude());
////    return ret;
////  }
////
////  private static UnpreciseApproach.Kind typeToUnpreciseType(ApproachType kind) {
////    switch (kind) {
////      case vor:
////        return UnpreciseApproach.Kind.vor;
////      case ndb:
////        return UnpreciseApproach.Kind.ndb;
////      default:
////        throw new NotImplementedException();
////    }
////  }
////
////  private static IlsApproach.IlsCategory typeToIlsType(ApproachType kind) {
////    switch (kind) {
////      case ils_I:
////        return IlsApproach.IlsCategory.I;
////      case ils_II:
////        return IlsApproach.IlsCategory.II;
////      case ils_III:
////        return IlsApproach.IlsCategory.II;
////      default:
////        throw new NotImplementedException();
////    }
////  }
//
//  private final SpeechList<IAtcCommand> gaCommands;
//  private final PlaneCategoryDefinitions planeCategories;
//  private final IList<IafRoute> iafRoutes;
//  private final ActiveRunwayThreshold parent;
//  private final ApproachType type;
//  private final ApproachEntryLocation entryLocation;
//  private final IList<IApproachStage> stages;
//
//  public IReadOnlyList<IafRoute> getIafRoutes() {
//    return iafRoutes;
//  }
//
//  public SpeechList<IAtcCommand> getGaCommands() {
//    return gaCommands;
//  }
//
//  public Approach(ApproachType type, PlaneCategoryDefinitions planeCategories, SpeechList<IAtcCommand> gaCommands,
//                  ApproachEntryLocation entryLocation, IList<IApproachStage> stages,
//                  IList<IafRoute> iafRoutes, ActiveRunwayThreshold parent) {
//    this.planeCategories = planeCategories;
//    this.gaCommands = gaCommands;
//    this.iafRoutes = iafRoutes;
//    this.parent = parent;
//    this.type = type;
//    this.entryLocation = entryLocation;
//    this.stages = stages;
//  }
//
//  public PlaneCategoryDefinitions getPlaneCategories() {
//    return planeCategories;
//  }
//
//  public ApproachEntryLocation getEntryLocation() {
//    return entryLocation;
//  }
//
//  public IList<IApproachStage> getStages() {
//    return stages;
//  }
//
//  //  public void bind() {
////    gaCommands = parseRoute(gaRoute);
////
////    if (this.includeIafRoutesGroups != null) {
////      String[] groupNames = this.includeIafRoutesGroups.split(";");
////      for (String groupName : groupNames) {
////        Airport.SharedIafRoutesGroup group = this.getParent().getParent().getParent().getSharedIafRoutesGroups().tryGetFirst(q -> q.groupName.equals(groupName));
////        if (group == null) {
////          throw new EApplicationException("Unable to find iaf-route group named " + groupName + " in airport "
////              + this.getParent().getParent().getParent().getIcao() + " required for runway approach " + this.getParent().getName() + " " + this.getTypeString() + ".");
////        }
////
////        this.iafRoutes.add(group.iafRoutes);
////      }
////    }
////
////    this.iafRoutes.forEach(q -> q.bind());
////
////    this._bind(); // bind in descendants
////
////    this.geographicalRadial = (int) Math.round(
////        Headings.add(this.radial,
////            this.getParent().getParent().getParent().getDeclination()));
////  }
//
//  public ActiveRunwayThreshold getParent() {
//    return parent;
//  }
//}
