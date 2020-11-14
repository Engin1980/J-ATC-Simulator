package eng.jAtcSim.newLib.airplanes;

import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.airplanes.modules.AirplaneFlightModule;
import eng.jAtcSim.newLib.airplanes.modules.AtcModule;
import eng.jAtcSim.newLib.airplanes.modules.DivertModule;
import eng.jAtcSim.newLib.airplanes.modules.EmergencyModule;
import eng.jAtcSim.newLib.airplanes.modules.sha.ShaModule;
import eng.jAtcSim.newLib.airplanes.modules.speeches.AfterCommandList;
import eng.jAtcSim.newLib.airplanes.modules.speeches.RoutingModule;
import eng.jAtcSim.newLib.airplanes.pilots.*;
import eng.jAtcSim.newLib.area.routes.GaRoute;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.mood.Mood;
import eng.jAtcSim.newLib.shared.DelayedList;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.shared.xml.XmlContextInit;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ItemsSerializer;
import eng.newXmlUtils.implementations.ObjectSerializer;

public class AirplaneXmlContextInit {
  public static void prepareXmlContext(XmlContext ctx) {
    if (XmlContextInit.checkCanBeInitialized(ctx, "airplane") == false) return;

    ctx.sdfManager.setSerializer(AirplanesController.class,
            new ObjectSerializer()
                    .withValueClassCheck(AirplanesController.class)
                    .withIgnoredFields("publicPlanes"));

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
}
