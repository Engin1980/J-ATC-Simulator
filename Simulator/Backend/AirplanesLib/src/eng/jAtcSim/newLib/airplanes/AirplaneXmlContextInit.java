package eng.jAtcSim.newLib.airplanes;

import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplaneType.AirplaneTypes;
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
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.DelayedList;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.shared.xml.XmlContextInit;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ItemsDeserializer;
import eng.newXmlUtils.implementations.ItemsSerializer;
import eng.newXmlUtils.implementations.ObjectDeserializer;
import eng.newXmlUtils.implementations.ObjectSerializer;

public class AirplaneXmlContextInit {
  public static void prepareXmlContext(XmlContext ctx) {
    if (XmlContextInit.checkCanBeInitialized(ctx, "airplane") == false) return;

    ctx.sdfManager.setSerializer(AirplanesController.class,
            new ObjectSerializer()
                    .withValueClassCheck(AirplanesController.class)
                    .withIgnoredFields("publicPlanes")
                    .withCustomFieldSerializer("planes", new ItemsSerializer()));
    ctx.sdfManager.setDeserializer(AirplanesController.class,
            new ObjectDeserializer<AirplanesController>()
                    .withIgnoredFields("publicPlanes")
                    .withCustomFieldDeserialization(
                            "planes",
                            new ItemsDeserializer().withInstanceFactory(c ->
                                    new AirplaneList<Airplane>(q -> q.getReader().getCallsign(), q -> q.getReader().getSqwk())))
                    .withAfterLoadAction((q, c) -> {
                      // q.init(); invoked when AirplanesModule is loaded
                      c.values.set("planes", q.getPlanes());
                    }));

    ctx.sdfManager.setFormatter(Airplane.AirplaneImpl.class, q -> q.getCallsign().toString());
    ctx.sdfManager.setParser(Airplane.AirplaneImpl.class, (q, c) -> {
      AirplaneList<IAirplane> lst = (AirplaneList<IAirplane>) c.values.get("planes");
      Callsign clsgn = new Callsign(q);
      IAirplane plane = lst.get(clsgn);
      Airplane.AirplaneImpl ret = (Airplane.AirplaneImpl) plane;
      return ret;
    });

    ctx.sdfManager.setSerializer(Airplane.class,
            new ObjectSerializer()
                    .withValueClassCheck(Airplane.class)
                    .withIgnoredFields("cvr", "fdr", "rdr", "wrt", "speechCache")
                    .withCustomFieldFormatter("airplaneType", (AirplaneType q) -> q.name));
    ctx.sdfManager.setDeserializer(Airplane.class,
            new ObjectDeserializer<Airplane>()
                    .withIgnoredFields("cvr", "fdr", "rdr", "wrt", "speechCache")
                    .withCustomFieldDeserialization("airplaneType", (q, c) -> c.values.get(AirplaneTypes.class).getByName(q.getContent()))
                    .withBeforeLoadAction((q, c) -> c.values.set(q))
                    .withAfterLoadAction((q, c) -> {
                      q.initRecorders();
                      c.values.remove(q);
                    }));

    {
      ctx.sdfManager.setSerializer(ShaModule.class, new ObjectSerializer().withIgnoredFields("plane", "rdr", "wrt"));
      ctx.sdfManager.setDeserializer(ShaModule.class, new ObjectDeserializer<ShaModule>()
              .withIgnoredFields("plane", "rdr", "wrt")
              .withInstanceFactory(c -> new ShaModule(
                      c.values.get(Airplane.class),
                      0,
                      0,
                      0,
                      c.values.get(Airplane.class).getReader().getType(),
                      0)));
      {
        ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.airplanes.modules.sha");
        ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.airplanes.modules.sha.navigators");
      }

      ctx.sdfManager.setSerializer(AtcModule.class, new ObjectSerializer().withIgnoredFields("plane", "rdr", "wrt"));
      ctx.sdfManager.setDeserializer(AtcModule.class, new ObjectDeserializer<AtcModule>()
              .withIgnoredFields("plane", "rdr", "wrt")
              .withInstanceFactory(c -> new AtcModule(c.values.get(Airplane.class), AtcId.getEmpty())));

      ctx.sdfManager.setSerializer(DivertModule.class, new ObjectSerializer().withIgnoredFields("plane", "rdr", "wrt"));
      ctx.sdfManager.setDeserializer(DivertModule.class, new ObjectDeserializer<DivertModule>()
              .withIgnoredFields("plane", "rdr", "wrt")
              .withInstanceFactory(c -> new DivertModule(c.values.get(Airplane.class))));

      ctx.sdfManager.setSerializer(EmergencyModule.class, new ObjectSerializer().withIgnoredFields("plane", "rdr", "wrt"));
      ctx.sdfManager.setDeserializer(EmergencyModule.class, new ObjectDeserializer<EmergencyModule>()
              .withIgnoredFields("plane", "rdr", "wrt"));

      ctx.sdfManager.setSerializer(AirplaneFlightModule.class, new ObjectSerializer().withIgnoredFields("plane", "rdr", "wrt"));
      ctx.sdfManager.setDeserializer(AirplaneFlightModule.class, new ObjectDeserializer<AirplaneFlightModule>()
              .withIgnoredFields("plane", "rdr", "wrt")
              .withInstanceFactory(c -> new AirplaneFlightModule(new Callsign("???? ???"),
                      0, new EDayTimeStamp(0), true)));

      ctx.sdfManager.setSerializer(RoutingModule.class, new ObjectSerializer().withIgnoredFields("plane", "rdr", "wrt", "cqr"));
      ctx.sdfManager.setDeserializer(RoutingModule.class, new ObjectDeserializer<RoutingModule>()
              .withIgnoredFields("plane", "rdr", "wrt", "cqr")
              .withInstanceFactory(c -> new RoutingModule(c.values.get(Airplane.class), null)));
      {
        ctx.sdfManager.setSerializer(DelayedList.class, new ObjectSerializer());
        ctx.sdfManager.setDeserializer(DelayedList.class, new ObjectDeserializer<DelayedList<?>>());

        ctx.sdfManager.setSerializer("eng.jAtcSim.newLib.shared.DelayedList$DelayedItem", new ObjectSerializer());
        ctx.sdfManager.setDeserializer("eng.jAtcSim.newLib.shared.DelayedList$DelayedItem", new ObjectDeserializer<>());

        ctx.sdfManager.setSerializer(AfterCommandList.class, new ObjectSerializer());
        ctx.sdfManager.setDeserializer(AfterCommandList.class, new ObjectDeserializer<AfterCommandList>());

        ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.airplanes.modules.speeches");

        ctx.sdfManager.setSerializer(SpeechList.class, new ItemsSerializer());
        ctx.sdfManager.setDeserializer(SpeechList.class, new ItemsDeserializer());

        ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.speeches.base");
        ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.speeches.airplane.airplane2atc");
        ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.speeches.airplane.airplane2atc.responses");
        ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.speeches.airplane.atc2airplane");
        ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands");
        ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.speeches.atc.atc2user");
        ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.speeches.atc.planeSwitching");
        ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.speeches.atc.user2atc");
        ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.speeches.system.system2user");
        ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.speeches.system.user2system");
      }
      ctx.sdfManager.setSerializer(Mood.class, new ObjectSerializer());
      ctx.sdfManager.setDeserializer(Mood.class, new ObjectDeserializer<Mood>());

      ctx.sdfManager.setSerializer(TakeOffPilot.class, new ObjectSerializer().withIgnoredFields("rdr", "wrt"));
      ctx.sdfManager.setDeserializer(TakeOffPilot.class, new ObjectDeserializer<TakeOffPilot>()
              .withIgnoredFields("rdr", "wrt")
              .withInstanceFactory(c -> new TakeOffPilot(c.values.get(Airplane.class))));

      ctx.sdfManager.setSerializer(ApproachPilot.class, new ObjectSerializer().withIgnoredFields("rdr", "wrt"));
      ctx.sdfManager.setDeserializer(ApproachPilot.class, new ObjectDeserializer<ApproachPilot>()
              .withIgnoredFields("rdr", "wrt")
              .withInstanceFactory(c -> ApproachPilot.createEmptyToLoad(c.values.get(Airplane.class))));
      {
        ctx.sdfManager.setSerializer(IafRoute.class, new ObjectSerializer());
        ctx.sdfManager.setDeserializer(IafRoute.class, new ObjectDeserializer<IafRoute>());

        ctx.sdfManager.setSerializer(GaRoute.class, new ObjectSerializer());
        ctx.sdfManager.setDeserializer(GaRoute.class, new ObjectDeserializer<GaRoute>());

        ctx.sdfManager.setSerializer(PlaneCategoryDefinitions.class, new ObjectSerializer());
        ctx.sdfManager.setDeserializer(PlaneCategoryDefinitions.class, new ObjectDeserializer<PlaneCategoryDefinitions>());
      }

      ctx.sdfManager.setSerializer(ArrivalPilot.class, new ObjectSerializer().withIgnoredFields("rdr", "wrt"));
      ctx.sdfManager.setDeserializer(ArrivalPilot.class, new ObjectDeserializer<ArrivalPilot>()
              .withIgnoredFields("rdr", "wrt")
              .withInstanceFactory(c -> new ArrivalPilot(c.values.get(Airplane.class))));

      ctx.sdfManager.setSerializer(HoldingPointPilot.class, new ObjectSerializer().withIgnoredFields("rdr", "wrt"));
      ctx.sdfManager.setDeserializer(HoldingPointPilot.class, new ObjectDeserializer<HoldingPointPilot>()
              .withIgnoredFields("rdr", "wrt")
              .withInstanceFactory(c -> new HoldingPointPilot(c.values.get(Airplane.class))));

      ctx.sdfManager.setSerializer(HoldPilot.class, new ObjectSerializer().withIgnoredFields("rdr", "wrt"));
      ctx.sdfManager.setDeserializer(HoldPilot.class, new ObjectDeserializer<HoldPilot>()
              .withIgnoredFields("rdr", "wrt")
              .withInstanceFactory(c -> HoldPilot.createForLoad(c.values.get(Airplane.class))));

      ctx.sdfManager.setSerializer(DeparturePilot.class, new ObjectSerializer().withIgnoredFields("rdr", "wrt"));
      ctx.sdfManager.setDeserializer(DeparturePilot.class, new ObjectDeserializer<DeparturePilot>()
              .withIgnoredFields("rdr", "wrt")
              .withInstanceFactory(c -> new DeparturePilot(c.values.get(Airplane.class))));
    }
  }
}
