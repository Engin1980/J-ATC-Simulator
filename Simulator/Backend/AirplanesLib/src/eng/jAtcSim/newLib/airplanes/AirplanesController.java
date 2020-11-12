package eng.jAtcSim.newLib.airplanes;

import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.ToDoException;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.airplanes.modules.AirplaneFlightModule;
import eng.jAtcSim.newLib.airplanes.modules.AtcModule;
import eng.jAtcSim.newLib.airplanes.modules.DivertModule;
import eng.jAtcSim.newLib.airplanes.modules.EmergencyModule;
import eng.jAtcSim.newLib.airplanes.modules.sha.ShaModule;
import eng.jAtcSim.newLib.airplanes.modules.speeches.AfterCommandList;
import eng.jAtcSim.newLib.airplanes.modules.speeches.RoutingModule;
import eng.jAtcSim.newLib.airplanes.pilots.*;
import eng.jAtcSim.newLib.airplanes.templates.AirplaneTemplate;
import eng.jAtcSim.newLib.airplanes.templates.ArrivalAirplaneTemplate;
import eng.jAtcSim.newLib.airplanes.templates.DepartureAirplaneTemplate;
import eng.jAtcSim.newLib.area.routes.GaRoute;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.mood.Mood;
import eng.jAtcSim.newLib.shared.*;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.newXmlUtils.implementations.ItemsSerializer;
import eng.newXmlUtils.implementations.ObjectSerializer;

public class AirplanesController {
  public static AirplanesController load(XElement element, IMap<String, Object> context) {
    //TODEL
    throw new ToDoException();
//    AirplanesController ret = new AirplanesController();
//
//    IReadOnlyList<AtcId> atcs = (IReadOnlyList<AtcId>) context.get("atcs");
//
//    EMap<Class<?>, Deserializer> dess = new EMap<>();
//    dess.set(AtcId.class, SharedXmlUtils.DeserializersDynamic.getAtcIdDeserializer(atcs));
//
//    XmlLoadUtils.Field.restoreFields(element, ret, new String[]{"departureInitialAtcId", "arrivalInitialAtId"}, dess);
//    XmlLoadUtils.Field.restoreField(element, ret, "planes",
//            new ItemsDeserializer(e -> Airplane.load(e, context), ret.planes));
//    return ret;
  }

  public static void prepareXmlContext(eng.newXmlUtils.XmlContext ctx) {
    ctx.sdfManager.setSerializer(AirplanesController.class,
            new ObjectSerializer()
                    .withValueClassCheck(AirplanesController.class)
                    .withIgnoredField("publicPlanes"));

    ctx.sdfManager.setSerializer(AirplaneList.class,
            new ItemsSerializer());

    ctx.sdfManager.setSerializer(Airplane.AirplaneImpl.class, (e, v, c) -> {
      e.setContent(((Airplane.AirplaneImpl) v).getCallsign().toString());
      e.setAttribute("__type", Airplane.AirplaneImpl.class.getName());
    });

    ctx.sdfManager.setSerializer(Airplane.class,
            new ObjectSerializer()
                    .withValueClassCheck(Airplane.class)
                    .withIgnoredFields("cvr", "fdr", "cqr", "rdr", "wrt", "speechCache")
                    .withCustomFieldFormatter("airplaneType", (AirplaneType q) -> q.name));
    {
      ctx.sdfManager.setSerializer(ShaModule.class, new ObjectSerializer().withIgnoredFields("plane", "rdr", "wrt"));
      {
        ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.airplanes.modules.sha");
        ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.airplanes.modules.sha.navigators");
      }
      ctx.sdfManager.setSerializer(AtcModule.class, new ObjectSerializer().withIgnoredFields("plane", "rdr", "wrt"));
      ctx.sdfManager.setSerializer(DivertModule.class, new ObjectSerializer().withIgnoredFields("plane", "rdr", "wrt"));
      ctx.sdfManager.setSerializer(EmergencyModule.class, new ObjectSerializer().withIgnoredFields("plane", "rdr", "wrt"));
      ctx.sdfManager.setSerializer(AirplaneFlightModule.class, new ObjectSerializer().withIgnoredFields("plane", "rdr", "wrt"));
      ctx.sdfManager.setSerializer(RoutingModule.class, new ObjectSerializer().withIgnoredFields("plane", "rdr", "wrt", "cqr"));
      {
        ctx.sdfManager.setSerializer(DelayedList.class, new ObjectSerializer());
        ctx.sdfManager.setSerializer("eng.jAtcSim.newLib.shared.DelayedList$DelayedItem", new ObjectSerializer());

        ctx.sdfManager.setSerializer(AfterCommandList.class, new ObjectSerializer());
        ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.airplanes.modules.speeches");

        ctx.sdfManager.setSerializer(SpeechList.class, new ItemsSerializer());
        ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.speeches.base");
        ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.speeches.airplane.airplane2atc");
        ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.speeches.airplane.airplane2atc.responses");
        ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.speeches.airplane.atc2airplane");
        ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands");
        ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.speeches.atc.atc2user");
        ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.speeches.atc.planeSwitching");
        ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.speeches.atc.user2atc");
        ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.speeches.system.system2user");
        ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.speeches.system.user2system");
      }
      ctx.sdfManager.setSerializer(Mood.class, new ObjectSerializer());
      ctx.sdfManager.setSerializer(TakeOffPilot.class, new ObjectSerializer().withIgnoredFields("rdr", "wrt"));
      ctx.sdfManager.setSerializer(ApproachPilot.class, new ObjectSerializer().withIgnoredFields("rdr", "wrt"));
      {
        ctx.sdfManager.setSerializer(IafRoute.class, new ObjectSerializer());
        ctx.sdfManager.setSerializer(GaRoute.class, new ObjectSerializer());
        ctx.sdfManager.setSerializer(PlaneCategoryDefinitions.class, new ObjectSerializer());
      }
      ctx.sdfManager.setSerializer(ArrivalPilot.class, new ObjectSerializer().withIgnoredFields("rdr", "wrt"));
      ctx.sdfManager.setSerializer(HoldingPointPilot.class, new ObjectSerializer().withIgnoredFields("rdr", "wrt"));
      ctx.sdfManager.setSerializer(HoldPilot.class, new ObjectSerializer().withIgnoredFields("rdr", "wrt"));
      ctx.sdfManager.setSerializer(DeparturePilot.class, new ObjectSerializer().withIgnoredFields("rdr", "wrt"));
    }
  }


  private final AirplaneList<Airplane> planes = new AirplaneList<>(
          q -> q.getReader().getCallsign(),
          q -> q.getReader().getSqwk());
  private final AirplaneList<IAirplane> publicPlanes = new AirplaneList<>(
          q -> q.getCallsign(),
          q -> q.getSqwk());
  private AtcId departureInitialAtcId;
  private AtcId arrivalInitialAtId;

  public AirplaneList<IAirplane> getPlanes() {
    return publicPlanes;
  }

  public void init() {
    this.arrivalInitialAtId = Context.getShared().getAtcs().getFirst(q -> q.getType() == AtcType.ctr);
    this.departureInitialAtcId = Context.getShared().getAtcs().getFirst(q -> q.getType() == AtcType.twr);
  }

  public IAirplane registerPlane(AirplaneTemplate at, Squawk sqwk) {
    Airplane airplane;

    if (at instanceof DepartureAirplaneTemplate) {
      airplane = Airplane.createDeparture((DepartureAirplaneTemplate) at, sqwk, departureInitialAtcId);
    } else if (at instanceof ArrivalAirplaneTemplate) {
      airplane = Airplane.createArrival((ArrivalAirplaneTemplate) at, sqwk, arrivalInitialAtId);
    } else
      throw new EApplicationException("Unknown airplane template type " + at.getClass().getName());

    this.planes.add(airplane);
    this.publicPlanes.add(airplane.getReader());

    Context.getMessaging().getMessenger().registerListener(
            Participant.createAirplane(airplane.getReader().getCallsign()));

    return airplane.getReader();
  }

  public void throwEmergency() {
    Airplane p = this.planes
            .where(q -> q.getReader().getState().is(AirplaneState.departingLow,
                    AirplaneState.departingHigh, AirplaneState.arrivingHigh,
                    AirplaneState.arrivingLow, AirplaneState.arrivingCloseFaf))
            .tryGetRandom();
    if (p != null)
      p.getWriter().raiseEmergency();
  }

  public void unregisterPlane(Callsign callsign) {
    planes.remove(q -> q.getReader().getCallsign().equals(callsign));
    publicPlanes.remove(q -> q.getCallsign().equals(callsign));
  }

  public void updatePlanes() {
    for (Airplane plane : planes) {
      try {
        plane.elapseSecond();
      } catch (Exception ex) {
        throw new EApplicationException("Error processing elapseSecond() on plane " + plane.getReader().getCallsign() + ".", ex);
      }
    }
  }

//TODEL
//
//  private boolean isInSeparationConflictWithTraffic(ArrivalAirplaneTemplate template) {
//    Integer checkedAtEntryPointSeconds = null;
//
//    boolean ret = false;
//
//    for (Airplane plane : this.planes) {
//      IAirplane rdr = plane.getReader();
//      if (rdr.isDeparture())
//        continue;
//      if (rdr.getAtc().getTunedAtc().getType() != AtcType.ctr)
//        continue;
//
//      if (template.getEntryPoint().getNavaid().equals(rdr.getRouting().getEntryExitPoint()) == false)
//        continue;
//
//      double dist = Coordinates.getDistanceInNM(
//          rdr.getRouting().getEntryExitPoint().getCoordinate(), rdr.getCoordinate());
//      int atEntryPointSeconds = (int) (dist / rdr.getSha().getSpeed() * 3600);
//
//      if (checkedAtEntryPointSeconds == null) {
//        dist = Coordinates.getDistanceInNM(
//            template.getEntryPoint().getNavaid().getCoordinate(), template.getCoordinate());
//        checkedAtEntryPointSeconds = (int) (dist / template.getSpeed() * 3600);
//      }
//
//      if (Math.abs(atEntryPointSeconds - checkedAtEntryPointSeconds) < 120) {
//        ret = true;
//        break;
//      }
//    }
//    return ret;
//  }
}
