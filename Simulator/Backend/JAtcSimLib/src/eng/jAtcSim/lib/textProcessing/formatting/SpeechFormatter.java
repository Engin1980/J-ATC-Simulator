package eng.jAtcSim.lib.textProcessing.formatting;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.exceptions.EXmlException;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.global.DataFormat;
import eng.jAtcSim.lib.speaking.ISpeech;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.*;
import eng.jAtcSim.lib.speaking.fromAtc.commands.*;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.*;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Confirmation;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.rejections.UnableToEnterApproachFromDifficultPosition;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.PlaneSwitchMessage;
import eng.jAtcSim.lib.world.approaches.Approach;

import java.nio.file.Path;
import java.util.function.Function;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class SpeechFormatter implements IFormatter {

  public static class Sentence {

    public static abstract class Block {
    }

    public static class StaticBlock extends Block {
      public String text;

      public StaticBlock(String text) {
        this.text = text;
      }
    }

    public static class VariableBlock extends Block {
      public String variable;

      public VariableBlock(String variable) {
        this.variable = variable;
      }
    }

    public static class ConditionalBlock extends Block {
      public IList<Block> content = new EList<>();
    }

    public final String kind;
    public final String text;
    public IList<Block> content = new EList<>();

    public Sentence(String kind, String text) {
      this.kind = kind;
      this.text = text;
      this.build();
    }

    private void build() {
      build(this.text, this.content);
    }

    private static void build(String text, IList<Block> parent) {
      String tmp = text;
      while (tmp.length() != 0) {
        int indexOfVar = tmp.indexOf('{');
        int indexOfCond = tmp.indexOf('[');

        if (indexOfVar == -1 && indexOfCond == -1) {
          StaticBlock staticBlock = new StaticBlock(tmp);
          parent.add(staticBlock);
          tmp = "";
        } else {
          if (indexOfVar == -1) indexOfVar = text.length();
          if (indexOfCond == -1) indexOfCond = text.length();
          int indexMin = Math.min(indexOfVar, indexOfCond);
          if (indexMin > 0) {
            String staticContent = tmp.substring(0, indexMin);
            StaticBlock staticBlock = new StaticBlock(staticContent);
            parent.add(staticBlock);
          }
          boolean isVar = indexOfVar < indexOfCond;
          int indexOfEnd = getClosingBracketIndex(tmp,
              isVar ? indexOfVar : indexOfCond,
              isVar ? '{' : '[',
              isVar ? '}' : ']');
          if (isVar) {
            VariableBlock variableBlock = new VariableBlock(tmp.substring(indexOfVar + 1, indexOfEnd));
            parent.add(variableBlock);
          } else {
            ConditionalBlock conditionalBlock = new ConditionalBlock();
            String sub = tmp.substring(indexOfCond + 1, indexOfEnd);
            build(sub, conditionalBlock.content);
            parent.add(conditionalBlock);
          }
          tmp = tmp.substring(indexOfEnd + 1);
        }
      }
    }

    private static int getClosingBracketIndex(String text, int fromIndex, char openChar, char closeChar) {
      int level = 0;
      for (int i = 0; i < text.length(); i++) {
        char current = text.charAt(i);
        if (current == openChar)
          level++;
        else if (current == closeChar) {
          level--;
          if (level == 0)
            return i;
        }
      }
      throw new EApplicationException("Unable to find closing bracket " + closeChar + "  for sequence " + text.substring(fromIndex));
    }
  }

  public static SpeechFormatter create(Path xmlFilePath) {
    XDocument doc;
    try {
      doc = XDocument.load(xmlFilePath.toAbsolutePath());
    } catch (EXmlException e) {
      throw new EApplicationException("Unable to load XmlFormatter from file " + xmlFilePath + ".", e);
    }

    SpeechFormatter ret = new XmlFormatterLoader().parse(doc.getRoot());
    return ret;
  }

  SpeechFormatter(IMap<Class, IList<Sentence>> sentences) {
    this.sentences = sentences;
  }

  private final IMap<Class, IList<Sentence>> sentences;
  private CommandVariableEvaluator commandVariableEvaluator = new CommandVariableEvaluator(this);

  @Override
  public String format(ISpeech speech) {
    Class cls = speech.getClass();
    IList<Sentence> sentences;
    String kind;
    try {
      sentences = this.sentences.get(cls);
    } catch (Exception ex){
      throw new EApplicationException("Failed to get sentences for " + cls.getSimpleName(), ex);
    }
    try {
      kind = _getKind(speech);
    } catch (Exception ex){
      throw new EApplicationException("Failed to get 'kind' for " + cls.getSimpleName(),  ex);
    }
    if (kind == null)
      sentences = sentences.where(q -> q.kind == null);
    else
      sentences = sentences.where(q -> q.kind == null || q.kind.equals(kind));
    assert !sentences.isEmpty() : "Sentences list is empty for speech " + cls.getSimpleName() + " and kind " + kind;

    Sentence sentence = sentences.getRandom();
    String ret = _format(speech, sentence);
    return ret;
  }

  private String _getKind(ISpeech speech) {
    if (speech instanceof ChangeAltitudeCommand) {
      ChangeAltitudeCommand tmp = (ChangeAltitudeCommand) speech;
      if (tmp.getDirection() == ChangeAltitudeCommand.eDirection.descend)
        return "descend";
      else if (tmp.getDirection() == ChangeAltitudeCommand.eDirection.climb)
        return "climb";
      else
        return null;
    } else if (speech instanceof ChangeHeadingCommand) {
      ChangeHeadingCommand tmp = (ChangeHeadingCommand) speech;
      if (tmp.isCurrentHeading())
        return "current";
      else if (tmp.getDirection() == ChangeHeadingCommand.eDirection.left)
        return "left";
      else if (tmp.getDirection() == ChangeHeadingCommand.eDirection.right)
        return "right";
      else if (tmp.getDirection() == ChangeHeadingCommand.eDirection.any)
        return "any";
      else
        return null;
    } else if (speech instanceof ChangeSpeedCommand) {
      ChangeSpeedCommand tmp = (ChangeSpeedCommand) speech;
      if (tmp.isResumeOwnSpeed())
        return "clear";
      else {
        if (tmp.getSpeedRestriction() == null)
          return null;
        else
          switch (tmp.getSpeedRestriction().direction) {
            case atMost:
              return "atMost";
            case atLeast:
              return "atLeast";
            case exactly:
              return "exactly";
            default:
              return null;
          }
      }
    } else if (speech instanceof SetAltitudeRestriction) {
      SetAltitudeRestriction tmp = (SetAltitudeRestriction) speech;
      if (tmp.getRestriction() == null)
        return "clear";
      else
        switch (tmp.getRestriction().direction) {
          case atLeast:
            return "atLeast";
          case atMost:
            return "atMost";
          case exactly:
            return "exactly";
          default:
            return null;
        }
    } else if (speech instanceof ClearedToApproachCommand) {
      ClearedToApproachCommand tmp = (ClearedToApproachCommand) speech;
      switch (tmp.getType()) {
        case ils_I:
          return "ILS_I";
        case ils_II:
          return "ILS_II";
        case ils_III:
          return "ILS_III";
        case gnss:
          return "GNSS";
        case ndb:
          return "NDB";
        case vor:
          return "VOR";
        case visual:
          return "visual";
        default:
          return null;
      }
    } else if (speech instanceof HoldCommand) {
      HoldCommand tmp = (HoldCommand) speech;
      if (tmp.isPublished())
        return "published";
      else
        return "custom";
    } else if (speech instanceof ClearedToRouteCommand) {
      ClearedToRouteCommand tmp = (ClearedToRouteCommand) speech;
      switch (tmp.getRoute().getType()) {
        case sid:
          return "sid";
        case star:
          return "star";
        case transition:
          return "transition";
        case vectoring:
          return "vectoring";
        default:
          return null;
      }
    } else if (speech instanceof GoingAroundNotification){
      GoingAroundNotification tmp = (GoingAroundNotification) speech;
      return tmp.getReason().toString();
    } else
      return null;
  }

  private String _format(ISpeech speech, Sentence sentence) {
    StringBuilder sb = new StringBuilder();
    for (Sentence.Block block : sentence.content) {
      String tmp = _evaluate(speech, block);
      sb.append(tmp);
    }
    return sb.toString();
  }

  private String _evaluate(ISpeech speech, Sentence.Block block) {
    if (block instanceof Sentence.StaticBlock)
      return _evaluate((Sentence.StaticBlock) block);
    else if (block instanceof Sentence.VariableBlock)
      return _evaluate(speech, (Sentence.VariableBlock) block);
    else if (block instanceof Sentence.ConditionalBlock)
      return _evaluate(speech, (Sentence.ConditionalBlock) block);
    else
      throw new UnsupportedOperationException();
  }

  private String _evaluate(Sentence.StaticBlock block) {
    return block.text;
  }

  private String _evaluate(ISpeech speech, Sentence.VariableBlock block) {
    String ret;
    String[] pts = block.variable.split(":");
    switch (pts[0]) {
      case "plane":
        throw new UnsupportedOperationException("Plane commands are not supported");
      case "cmd":
        ret = _evaluateCommandVariable(speech, pts[1]);
        break;
      default:
        throw new EEnumValueUnsupportedException(pts[0]);
    }
    return ret;
  }

  private String _evaluateCommandVariable(ISpeech speech, String key) {
    String ret;
    try {
      ret = commandVariableEvaluator.eval(speech, key);
    } catch (Exception ex) {
      throw new EApplicationException(sf("Variable evaluation error. Unable to find for kind '%s' key '%s'.", speech.getClass().getSimpleName(), key));
    }
    return ret;
  }

  private String _evaluate(ISpeech speech, Sentence.ConditionalBlock block) {
    boolean use = false;
    StringBuilder ret = new StringBuilder();

    for (Sentence.Block sub : block.content) {
      String tmp = _evaluate(speech, sub);
      if (!use && sub instanceof Sentence.VariableBlock && (tmp != null && tmp.length() > 0))
        use = true;
      ret.append(tmp);
    }

    if (!use)
      return "";
    else
      return ret.toString();
  }

  @Override
  public String format(Atc sender, PlaneSwitchMessage msg) {
    String ret;
    if (msg.getMessageType() == PlaneSwitchMessage.eMessageType.request) {
      ret = String.format(
          "%s {%s} via %s/%s %s",
          msg.plane.getSqwk(),
          msg.plane.getFlightModule().getCallsign().toString(),
          msg.plane.getRoutingModule().getAssignedRunwayThreshold().getName(),
          msg.plane.getRoutingModule().getAssignedRoute().getName(),
          msg.getMessageText(),
          sender.getName());
    } else {
      ret = String.format(
          "%s {%s} %s",
          msg.plane.getSqwk(),
          msg.plane.getFlightModule().getCallsign().toString(),
          msg.getMessageText(),
          sender.getName());
    }
    return ret;
  }
}

class XmlFormatterLoader {

  private final static String[] prefixes = {
      "eng.jAtcSim.lib.speaking.fromAirplane.notifications.",
      "eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.",
      "eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.rejections.",
      "eng.jAtcSim.lib.speaking.fromAtc.commands.",
      "eng.jAtcSim.lib.speaking.fromAtc.commands.afters.",
      "eng.jAtcSim.lib.speaking.fromAtc.notifications."
  };


  private static Class getTypeClass(String className) {
    Class cls = null;
    for (String prefix : prefixes) {
      String fullName = prefix + className;
      try {
        cls = Class.forName(fullName);
        break;
      } catch (ClassNotFoundException e) {
      }
    }
    if (cls == null) {
      throw new EApplicationException(sf("Unable to find class '%s' as response application.", className));
    }
    return cls;
  }

  public SpeechFormatter parse(XElement xElement) {
    IMap<Class, IList<SpeechFormatter.Sentence>> tmp = new EMap<>();

    for (XElement responseElement : xElement.getChildren("response")) {
      String type = responseElement.getAttribute("kind");
      Class cls = getTypeClass(type);
      IList<SpeechFormatter.Sentence> lst = new EList<>();
      tmp.set(cls, lst);
      for (XElement sentenceElement : responseElement.getChildren("sentence")) {
        String text = sentenceElement.getContent();
        String kind = sentenceElement.tryGetAttribute("kind");
        SpeechFormatter.Sentence sent = new SpeechFormatter.Sentence(kind, text);
        lst.add(sent);
      }
    }

    SpeechFormatter ret = new SpeechFormatter(tmp);
    return ret;
  }
}

class CommandVariableEvaluator {
  private SpeechFormatter parent;
  private IMap<Class, IMap<String, Function<? extends ISpeech, String>>> evals = new EMap<>();

  public CommandVariableEvaluator(SpeechFormatter parentFormatter) {
    this.parent = parentFormatter;

    register(AfterAltitudeCommand.class, "alt",
        q -> DataFormat.Altitude.toStandardAltitudeOrFL(q.getAltitudeInFt(), Acc.airport().getTransitionAltitude()));
    register(AfterNavaidCommand.class, "navaid",
        q -> q.getNavaid().getName());
    register(AfterHeadingCommand.class, "heading",
        q -> DataFormat.Heading.to(q.getHeading()));
    register(AfterRadialCommand.class, "radial",
        q -> DataFormat.Heading.to(q.getRadial()));
    register(AfterRadialCommand.class, "navaid",
        q -> q.getNavaid().getName());
    register(AfterDistanceCommand.class, "distance",
        q -> DataFormat.Distance.to(q.getDistanceInNm()));
    register(AfterDistanceCommand.class, "navaid",
        q -> q.getNavaid().getName());
    register(ChangeAltitudeCommand.class, "altitude",
        q -> DataFormat.Altitude.toStandardAltitudeOrFL(q.getAltitudeInFt(), Acc.airport().getTransitionAltitude()));
    register(ChangeHeadingCommand.class, "heading",
        q -> DataFormat.Heading.to(q.getHeading()));
    register(ChangeSpeedCommand.class, "speed",
        q -> DataFormat.Speed.to(q.getSpeedInKts()));
    register(SetAltitudeRestriction.class, "altitude",
        q -> DataFormat.Altitude.toStandardAltitudeOrFL(q.getRestriction().value, Acc.airport().getTransitionAltitude()));
    register(ClearedForTakeoffCommand.class, "rwy",
        q -> q.getRunwayThreshold().getName());
    register(ClearedToApproachCommand.class, "rwy",
        q -> q.getThresholdName());
    register(ClearedToApproachCommand.class, "ilsCategory",
        q -> {
          if (q.getType() == Approach.ApproachType.ils_I)
            return "I";
          else if (q.getType() == Approach.ApproachType.ils_II)
            return "II";
          else if (q.getType() == Approach.ApproachType.ils_III)
            return "III";
          else
            return "???";
        });
    register(ContactCommand.class, "atcName",
        q -> Acc.atc(q.getAtcType()).getName());
    register(ContactCommand.class, "atcFrequency",
        q -> DataFormat.Frequency.to(Acc.atc(q.getAtcType()).getFrequency()));
    register(HoldCommand.class, "navaid",
        q -> q.getNavaid().getName());
    register(HoldCommand.class, "inboundRadial",
        q -> DataFormat.Heading.to(q.getInboundRadial()));
    register(HoldCommand.class, "turnsDirection",
        q -> q.isLeftTurn() ? "left" : "right");
    register(ProceedDirectCommand.class, "navaid",
        q -> q.getNavaid().getName());
    register(ShortcutCommand.class, "navaid",
        q -> q.getNavaid().getName());
    register(ClearedToRouteCommand.class, "route",
        q -> q.getRoute().getName());
    register(UnableToEnterApproachFromDifficultPosition.class, "reason",
        q -> q.reason);
    register(HighOrderedSpeedForApproach.class, "orderedSpeed",
        q -> DataFormat.Speed.to(q.getOrderedSpeed()));
    register(HighOrderedSpeedForApproach.class, "requiredSpeed",
        q -> DataFormat.Speed.to(q.getRequiredSpeed()));
    register(GoodDayNotification.class, "emergency",
        q -> q.isEmergency() ? "may-day" : "");
    register(GoodDayNotification.class, "callsign",
        q -> q.getCallsign().toString());
    register(GoodDayNotification.class, "altitude",
        q -> DataFormat.Altitude.toStandardAltitudeOrFL(q.getAltitude(), Acc.airport().getTransitionAltitude()));
    register(GoodDayNotification.class, "targetAltitudeIfDifferent",
        q -> q.getAltitude() != q.getTargetAltitude() ?
            DataFormat.Altitude.toStandardAltitudeOrFL(q.getTargetAltitude(), Acc.airport().getTransitionAltitude()) :
            "");
    register(EstablishedOnApproachNotification.class, "rwy",
        q -> q.getThreshold().getName());
    register(DivertTimeNotification.class, "divertMinutes",
        q -> Integer.toString(q.getMinutesToDivert()));
    register(DivertingNotification.class, "navaid",
        q -> q.getExitNavaid().getName());

    register(Confirmation.class, "origin",
        q -> {
          String ret = parent.format(q.getOrigin());
          return ret;
        });
    register(Rejection.class, "origin",
        q -> {
          String ret = parent.format(q.getOrigin());
          return ret;
        });
    register(Rejection.class, "reason",
        q -> q.getReason());
  }

  private <T extends ISpeech> void register(Class<? extends T> cls, String key, Function<T, String> function) {
    if (evals.containsKey(cls) == false)
      evals.set(cls, new EMap<>());
    IMap<String, Function<? extends ISpeech, String>> tmp = evals.get(cls);
    tmp.set(key, function);
  }

  public <T extends ISpeech> String eval(T value, String key) {
    String ret;
    Class cls = value.getClass();
    Function<T, String> fun;
    try {
      IMap<String, Function<? extends ISpeech, String>> typeEvals = evals.get(cls);
      fun = (Function<T, String>) typeEvals.get(key);
    } catch (Exception ex) {
      throw new EApplicationException(
          sf("Unable to find lambda function for '%s'.'%s'.", cls.getSimpleName(), key), ex);
    }
    try {
      ret = fun.apply(value);
    } catch (Exception ex) {
      throw new EApplicationException(
          sf("Unable to evaluate '%s'.'%s' via its lambda function.", cls.getSimpleName(), key), ex);
    }
    return ret;
  }
}


