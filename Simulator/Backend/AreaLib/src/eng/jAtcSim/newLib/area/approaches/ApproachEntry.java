package eng.jAtcSim.newLib.area.approaches;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.*;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.approaches.behaviors.FlyIafRouteBehavior;
import eng.jAtcSim.newLib.area.approaches.behaviors.FlyRadialBehavior;
import eng.jAtcSim.newLib.area.approaches.conditions.FlyRouteBehaviorEmptyCondition;
import eng.jAtcSim.newLib.area.approaches.conditions.PlaneShaCondition;
import eng.jAtcSim.newLib.area.approaches.conditions.locations.FixRelatedLocation;
import eng.jAtcSim.newLib.area.contextLocal.Context;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeHeadingCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ToNavaidCommand;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ApproachEntry {

  private static class HeadingAndCoordinate {
    public final Coordinate coordinate;
    public final int heading;
    public final int range;

    HeadingAndCoordinate(int heading, Coordinate coordinate, int range) {
      this.heading = heading;
      this.coordinate = coordinate;
      this.range = range;
    }
  }

  public static ApproachEntry create(ISet<ApproachEntryCondition> entryConditions) {
    return new ApproachEntry(entryConditions, PlaneCategoryDefinitions.getAll(), null);
  }

  public static ApproachEntry create(ISet<ApproachEntryCondition> entryConditions, PlaneCategoryDefinitions categoryDefinitions, IList<ApproachStage> entryStages) {
    return new ApproachEntry(entryConditions, categoryDefinitions, entryStages);
  }

  public static ApproachEntry createIaf(IafRoute iafRoute) {
    EAssert.Argument.isNotNull(iafRoute, "iafRoute");

    FlyIafRouteBehavior frb = new FlyIafRouteBehavior(iafRoute);
    ApproachStage iafStage = ApproachStage.create(
            "IAF via " + iafRoute.getNavaid(),
            frb,
            new FlyRouteBehaviorEmptyCondition()
    );

    ISet<ApproachEntryCondition> entryConditions = createApproachEntryConditionForRoute(iafRoute);

    return new ApproachEntry(entryConditions, iafRoute.getCategory(), EList.of(iafStage));
  }

  private static HeadingAndCoordinate getOptimalEntryHeadingForRoute(IafRoute route) {
    EAssert.Argument.isNotNull(route);
    EAssert.Argument.isTrue(route.getRouteCommands().isEmpty() == false);

    HeadingAndCoordinate ret = null;

    Navaid firstNavaid = route.getNavaid();
    for (ICommand routeCommand : route.getRouteCommands()) {
      if (routeCommand instanceof ChangeHeadingCommand && !((ChangeHeadingCommand) routeCommand).isCurrentHeading()) {
        ChangeHeadingCommand changeHeadingCommand = (ChangeHeadingCommand) routeCommand;
        ret = new HeadingAndCoordinate(
                changeHeadingCommand.getHeading(),
                firstNavaid.getCoordinate(),
                25);
      } else if (routeCommand instanceof FlyRadialBehavior) {
        FlyRadialBehavior flyRadialBehavior = (FlyRadialBehavior) routeCommand;
        ret = new HeadingAndCoordinate(
                (int) Math.round(flyRadialBehavior.getInboundRadialWithDeclination()),
                firstNavaid.getCoordinate(),
                25);
        break;
      } else if (routeCommand instanceof ToNavaidCommand) {
        Navaid secondNavaid = Context.getArea().getNavaids().getWithPBD(((ToNavaidCommand) routeCommand).getNavaidName());
        ret = new HeadingAndCoordinate(
                (int) Coordinates.getBearing(firstNavaid.getCoordinate(), secondNavaid.getCoordinate()),
                firstNavaid.getCoordinate(),
                15);
        break;
      }
    }

    EAssert.isNotNull(ret, sf("Unable to detect entry heading for iaf-route via '%s'.", route.getNavaid()));
    return ret;
  }

  private static ISet<ApproachEntryCondition> createApproachEntryConditionForRoute(IafRoute route) {
    //IDEA this should somehow allow set custom entry location?
    HeadingAndCoordinate hac = getOptimalEntryHeadingForRoute(route);
    int fromRadial = (int) Headings.add(hac.heading, -115);
    int toRadial = (int) Headings.add(hac.heading, 115);

    ISet<ApproachEntryCondition> ret = new ESet<>();
    ApproachEntryCondition aec;

    aec = ApproachEntryCondition.create(
            FixRelatedLocation.create(route.getNavaid().getCoordinate(), 3),
            ApproachEntryCondition.ApproachRejectionReason.invalidLocation);
    ret.add(aec);

    aec = ApproachEntryCondition.create(
            PlaneShaCondition.create(
                    PlaneShaCondition.eType.heading, fromRadial, toRadial),
            ApproachEntryCondition.ApproachRejectionReason.invalidHeading);
    ret.add(aec);

    return ret;
  }

  private final IList<ApproachStage> entryStages = new EList<>();
  private final ISet<ApproachEntryCondition> entryConditions;
  private final PlaneCategoryDefinitions categoryDefinitions;
  private String tag;

  private ApproachEntry(ISet<ApproachEntryCondition> entryConditions, PlaneCategoryDefinitions categoryDefinitions, IList<ApproachStage> entryStages) {
    EAssert.Argument.isNotNull(entryConditions, "entryConditions");
    EAssert.Argument.isNotNull(categoryDefinitions, "categoryDefinitions");

    this.entryConditions = entryConditions;
    if (entryStages != null)
      this.entryStages.addMany(entryStages);
    this.categoryDefinitions = categoryDefinitions;
  }

  public ISet<ApproachEntryCondition> getEntryConditions() {
    return entryConditions;
  }

  public IReadOnlyList<ApproachStage> getEntryStages() {
    return entryStages;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public boolean isForCategory(char c) {
    return this.categoryDefinitions.contains(c);
  }

  @Override
  public String toString() {
    EStringBuilder sb = new EStringBuilder();
    sb.appendFormat("Approach Entry '%s' ('%d' entries, '%d' stages)",
            this.tag, this.entryConditions.size(), this.entryStages.size());
    return sb.toString();
  }

  public ApproachEntry withTag(String tag) {
    this.tag = tag;
    return this;
  }
}
