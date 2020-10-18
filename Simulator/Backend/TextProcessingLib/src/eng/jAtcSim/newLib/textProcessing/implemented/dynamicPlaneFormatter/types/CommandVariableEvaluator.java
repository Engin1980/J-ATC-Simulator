package eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter.types;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.functionalInterfaces.Selector;
import eng.jAtcSim.newLib.shared.Format;
import eng.jAtcSim.newLib.shared.enums.ApproachType;
import eng.jAtcSim.newLib.shared.enums.LeftRight;
import eng.jAtcSim.newLib.speeches.airplane.IPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.*;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.responses.UnableToEnterApproachFromDifficultPosition;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.*;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.*;
import eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter.DynamicPlaneFormatter;

import java.util.function.Function;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class CommandVariableEvaluator {
  private final DynamicPlaneFormatter parent;
  private IMap<Class<?>, IMap<String, Selector<? extends IPlaneSpeech, String>>> evals = new EMap<>();


  public CommandVariableEvaluator(DynamicPlaneFormatter parentFormatter) {
    this.parent = parentFormatter;

    this.register(AfterAltitudeCommand.class, "alt",
        q -> Format.Altitude.toAlfOrFLLong(q.getAltitude()));
    register(AfterNavaidCommand.class, "navaid",
        q -> q.getNavaidName());
    register(AfterHeadingCommand.class, "heading",
        q -> Format.Heading.to(q.getHeading()));
    register(AfterRadialCommand.class, "radial",
        q -> Format.Heading.to(q.getRadial()));
    register(AfterRadialCommand.class, "navaid",
        q -> q.getNavaidName());
    register(AfterDistanceCommand.class, "distance",
        q -> Format.Distance.to(q.getDistance()));
    register(AfterDistanceCommand.class, "navaid",
        q -> q.getNavaidName());
    register(ChangeAltitudeCommand.class, "altitude",
        q -> Format.Altitude.toAlfOrFLLong(q.getAltitudeInFt()));
    register(ChangeHeadingCommand.class, "heading",
        q -> Format.Heading.to(q.getHeading()));
    register(ChangeSpeedCommand.class, "speed",
        q -> Format.Speed.to(q.getRestriction().value));
    register(AltitudeRestrictionCommand.class, "altitude",
        q -> Format.Altitude.toAlfOrFLLong(q.getRestriction().value));
    register(ClearedForTakeoffCommand.class, "rwy",
        q -> q.getRunwayThresholdName());
    register(ClearedToApproachCommand.class, "rwy",
        q -> q.getThresholdName());
    register(ClearedToApproachCommand.class, "ilsCategory",
        q -> {
          if (q.getType() == ApproachType.ils_I)
            return "I";
          else if (q.getType() == ApproachType.ils_II)
            return "II";
          else if (q.getType() == ApproachType.ils_III)
            return "III";
          else
            return "???";
        });
    register(ContactCommand.class, "atcName",
        q -> q.getAtc().getName());
    register(ContactCommand.class, "atcFrequency",
        q -> Format.Frequency.to(q.getAtc().getFrequency()));
    register(HoldCommand.class, "navaid",
        q -> q.getNavaidName());
    register(HoldCommand.class, "inboundRadial",
        q -> Format.Heading.to(q.getInboundRadial()));
    register(HoldCommand.class, "turnsDirection",
        q -> q.getTurn() == LeftRight.left ? "left" : "right");
    register(ProceedDirectCommand.class, "navaid",
        q -> q.getNavaidName());
    register(ShortcutCommand.class, "navaid",
        q -> q.getNavaidName());
    register(ClearedToRouteCommand.class, "route",
        q -> q.getRouteName());
    register(UnableToEnterApproachFromDifficultPosition.class, "reason",
        q -> q.getReason());
    register(HighOrderedSpeedForApproach.class, "orderedSpeed",
        q -> Format.Speed.to(q.getOrderedSpeed()));
    register(HighOrderedSpeedForApproach.class, "requiredSpeed",
        q -> Format.Speed.to(q.getRequiredSpeed()));
    register(GoodDayNotification.class, "emergency",
        q -> q.isEmergency() ? "may-day" : "");
    register(GoodDayNotification.class, "callsign",
        q -> q.getCallsign().toString());
    register(GoodDayNotification.class, "altitude",
        q -> Format.Altitude.toAlfOrFLLong((int) q.getAltitude()));
    register(GoodDayNotification.class, "targetAltitudeIfDifferent",
        q -> q.getAltitude() != q.getTargetAltitude() ?
            Format.Altitude.toAlfOrFLLong((int) q.getTargetAltitude()) :
            "");
    register(EstablishedOnApproachNotification.class, "rwy",
        q -> q.getThresholdName());
    register(DivertTimeNotification.class, "divertMinutes",
        q -> Integer.toString(q.getMinutesToDivert()));
    register(DivertingNotification.class, "navaid",
        q -> q.getExitNavaidName());

    register(PlaneConfirmation.class, "origin",
        q -> {
          String ret = parent.format(q.getOrigin());
          return ret;
        });
    register(PlaneRejection.class, "origin",
        q -> {
          String ret = parent.format(q.getOrigin());
          return ret;
        });
    register(PlaneRejection.class, "reason",
        q -> q.getReason());
  }

  public <T extends IPlaneSpeech> String eval(T value, String key) {
    String ret;
    Class<? extends IPlaneSpeech> cls = value.getClass();
    Selector<T, String> fun;
    try {
      IMap<String, Selector<? extends IPlaneSpeech, String>> typeEvals = evals.get(cls);
      fun = (Selector<T, String>) typeEvals.get(key);
    } catch (Exception ex) {
      throw new EApplicationException(
          sf("Unable to find lambda function for '%s'.'%s'.", cls.getSimpleName(), key), ex);
    }
    try {
      ret = fun.getValue(value);
    } catch (Exception ex) {
      throw new EApplicationException(
          sf("Unable to evaluate '%s'.'%s' via its lambda function.", cls.getSimpleName(), key), ex);
    }
    return ret;
  }

  private <T extends IPlaneSpeech> void register(
      Class<? extends T> cls,
      String key,
      Selector<T, String> selector) {
    if (evals.containsKey(cls) == false)
      evals.set(cls, new EMap<>());
    IMap<String, Selector<? extends IPlaneSpeech, String>> tmp = evals.get(cls);
    tmp.set(key, selector);
  }
}
