package eng.jAtcSim.lib.world.approaches;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
import eng.jAtcSim.lib.world.Parentable;
import eng.jAtcSim.lib.world.approaches.entryLocations.FixRelatedApproachEntryLocation;
import eng.jAtcSim.lib.world.approaches.entryLocations.IApproachEntryLocation;
import eng.jAtcSim.lib.world.approaches.stages.IApproachStage;
import eng.jAtcSim.lib.world.xml.XmlLoader;

import javax.swing.text.StyledEditorKit;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class Approach extends Parentable<ActiveRunwayThreshold> {

  public static IList<Approach> loadList(IReadOnlyList<XElement> sources, IReadOnlyList<IafRoute> iafRoutes){
    IList<Approach> ret = new EList<>();

    for (XElement source : sources) {
      if (source.getName().equals("ilsApproach")){
        IList<Approach> tmp = loadIlss(source,iafRoutes);
        ret.add(tmp);
      } else if (source.getName().equals("gnssApproach"))
      {
        Approach tmp = loadGnss(source, iafRoutes);
        ret.add(tmp);
      }
      else if (source.getName().equals("unpreciseApproach")){
        Approach tmp = loadUnprecise(source, iafRoutes);
        ret.add(tmp);
      } else if (source.getName().equals("customApproach")){
        Approach tmp = loadCustom(source, iafRoutes);
        ret.add(tmp);
      } else {
        throw new EApplicationException(sf("Unknown approach type '%s'.", source.getName() );
      }
    }
    return ret;
  }

  public static IList<Approach> loadIlss(XElement source, IReadOnlyList<IafRoute> iafRoutes){
    XmlLoader.setContext(source);
    Double tmp = XmlLoader.loadDouble("glidePathPercentage", false);
    double glidePathPercentage = tmp != null ? tmp : 3;

    IList<ApproachEntry> entries = new EList<>();
    entries.add(new ApproachEntry(
        new FixRelatedApproachEntryLocation()
    ));

    for (XElement child : source.getChild("categories").getChildren("category")) {
      XmlLoader.setContext(child);
      int daA = XmlLoader.loadInteger("daA",true);
      int daB = XmlLoader.loadInteger("daB",true);
      int daC = XmlLoader.loadInteger("daC",true);
      int daD = XmlLoader.loadInteger("daD",true);
      String ilsType = XmlLoader.loadString("type",true);

      ApproachType approachType = ilsType.equals("I") ? ApproachType.ils_I :
          ilsType.equals("II") ? ApproachType.ils_II :
              ilsType.equals("III") ? ApproachType.ils_III : ApproachType.visual;
      if (approachType == ApproachType.visual)
        throw new EApplicationException(sf("Unknown approach type '%s'.", ilsType));



      Approach app = new Approach(type,)

          ga-routes should be shared too !
    }

  }

  public enum ApproachType {
    ils_I,
    ils_II,
    ils_III,
    ndb,
    vor,
    gnss,
    visual
  }

  private final IList<ApproachEntry> entries;
  private final IList<IApproachStage> stages;
  private final SpeechList<IAtcCommand> gaCommands;
  private final ApproachType type;

  private Approach(ApproachType type,
                   IList<ApproachEntry> entries, IList<IApproachStage> stages, SpeechList<IAtcCommand> gaCommands) {
    this.entries = entries;
    this.stages = stages;
    this.gaCommands = gaCommands;
    this.type = type;
  }

  //  public Approach(ApproachType type, PlaneCategoryDefinitions planeCategories, SpeechList<IAtcCommand> gaCommands,
//                  IApproachEntryLocation entryLocation, IList<IApproachStage> stages,
//                  IList<IafRoute> iafRoutes, ActiveRunwayThreshold parent) {
//    throw new EApplicationException("Must be implemented.");
////    this.planeCategories = planeCategories;
////    this.gaCommands = gaCommands;
////    this.iafRoutes = iafRoutes;
////    this.parent = parent;
////    this.type = type;
////    this.entryLocation = entryLocation;
////    this.stages = stages;
//  }

  public IReadOnlyList<ApproachEntry> getEntries() {
    return entries;
  }

  public IReadOnlyList<IApproachStage> getStages() {
    return stages;
  }

  public SpeechList<IAtcCommand> getGaCommands() {
    return gaCommands;
  }

  public ApproachType getType() {
    return type;
  }
}
