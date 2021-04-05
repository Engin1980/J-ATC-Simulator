package eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.exceptions.EXmlException;
import eng.jAtcSim.newLib.shared.enums.LeftRightAny;
import eng.jAtcSim.newLib.speeches.airplane.IPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.GoingAroundNotification;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.*;
import eng.jAtcSim.newLib.textProcessing.formatting.IPlaneFormatter;
import eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter.types.CommandVariableEvaluator;
import eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter.types.Sentence;

import java.nio.file.Path;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class DynamicPlaneFormatter implements IPlaneFormatter<String> {

  private static class DynamicPlaneFormatterLoader {

    private final static String[] prefixes = {
          "eng.jAtcSim.newLib.speeches.airplane.atc2airplane.",
          "eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.",
          "eng.jAtcSim.newLib.speeches.airplane.airplane2atc.",
          "eng.jAtcSim.newLib.speeches.base.",
          "eng.jAtcSim.newLib.speeches.airplane.airplane2atc.responses."
    };

    private static Class<?> getTypeClass(String className) {
      Class<?> cls = null;
      for (String prefix : prefixes) {
        String fullName = prefix + className;
        try {
          cls = Class.forName(fullName);
          break;
        } catch (ClassNotFoundException e) {
          // intentionally blank
        }
      }
      if (cls == null) {
        throw new EApplicationException(sf("Unable to find class '%s' as response application.", className));
      }
      return cls;
    }

    private DynamicPlaneFormatter load(XElement root) {
      DynamicPlaneFormatter ret = new DynamicPlaneFormatter();

      IMap<Class<?>, IList<Sentence>> tmp = ret.sentences;
      for (XElement responseElement : root.getChildren("response")) {
        String type = responseElement.getAttribute("type");
        Class<?> cls = getTypeClass(type);
        IList<Sentence> lst = new EList<>();
        tmp.set(cls, lst);
        for (XElement sentenceElement : responseElement.getChildren("sentence")) {
          String text = sentenceElement.getContent();
          String kind = sentenceElement.tryGetAttribute("kind").orElse(null);
          Sentence sent = new Sentence(kind, text);
          lst.add(sent);
        }
      }

      return ret;
    }
  }

  public static DynamicPlaneFormatter load(Path file) {
    DynamicPlaneFormatter ret;
    try {
      XElement root = XDocument.load(file).getRoot();
      ret = new DynamicPlaneFormatterLoader().load(root);
    } catch (EXmlException e) {
      throw new EApplicationException("Unable to load."); //TODO improve
    }

    return ret;
  }

  private final IMap<Class<?>, IList<Sentence>> sentences;
  private final CommandVariableEvaluator commandVariableEvaluator = new CommandVariableEvaluator(this);

  private DynamicPlaneFormatter() {
    this.sentences = new EMap<>();
  }

  @Override
  public String format(IPlaneSpeech speech) {
    Class<?> cls = speech.getClass();
    IList<Sentence> sentences;
    String kind;
    try {
      sentences = this.sentences.get(cls);
    } catch (Exception ex) {
      throw new EApplicationException("Failed to get sentences for " + cls.getSimpleName(), ex);
    }
    try {
      kind = _getKind(speech);
    } catch (Exception ex) {
      throw new EApplicationException("Failed to get 'kind' for " + cls.getSimpleName(), ex);
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

  private String _evaluate(IPlaneSpeech speech, Sentence.Block block) {
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

  private String _evaluate(IPlaneSpeech speech, Sentence.VariableBlock block) {
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

  private String _evaluate(IPlaneSpeech speech, Sentence.ConditionalBlock block) {
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

  private String _evaluateCommandVariable(IPlaneSpeech speech, String key) {
    String ret;
    try {
      ret = commandVariableEvaluator.eval(speech, key);
    } catch (Exception ex) {
      throw new EApplicationException(sf("Variable evaluation error. Unable to find for kind '%s' key '%s'.", speech.getClass().getSimpleName(), key));
    }
    return ret;
  }

  private String _format(IPlaneSpeech speech, Sentence sentence) {
    StringBuilder sb = new StringBuilder();
    for (Sentence.Block block : sentence.content) {
      String tmp = _evaluate(speech, block);
      sb.append(tmp);
    }
    return sb.toString();
  }

  private String _getKind(IPlaneSpeech speech) {
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
      else if (tmp.getDirection() == LeftRightAny.left)
        return "left";
      else if (tmp.getDirection() == LeftRightAny.right)
        return "right";
      else if (tmp.getDirection() == LeftRightAny.any)
        return "any";
      else
        return null;
    } else if (speech instanceof ChangeSpeedCommand) {
      ChangeSpeedCommand tmp = (ChangeSpeedCommand) speech;
      if (tmp.isResumeOwnSpeed())
        return "clear";
      else {
        if (tmp.getRestriction() == null)
          return null;
        else
          switch (tmp.getRestriction().direction) {
            case below:
              //TODO sjednotit
              return "atMost";
            case above:
              return "atLeast";
            case exactly:
              return "exactly";
            default:
              return null;
          }
      }
    } else if (speech instanceof AltitudeRestrictionCommand) {
      AltitudeRestrictionCommand tmp = (AltitudeRestrictionCommand) speech;
      if (tmp.getRestriction() == null)
        return "clear";
      else
        switch (tmp.getRestriction().direction) {
          case above:
            //TODO sjednotit
            return "atLeast";
          case below:
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
      switch (tmp.getRouteType()) {
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
    } else if (speech instanceof GoingAroundNotification) {
      GoingAroundNotification tmp = (GoingAroundNotification) speech;
      return tmp.getReason().toString();
    } else
      return null;
  }

}
