package eng.jAtcSim.newLib.textProcessing.implemented.atcFormatter.formatters;

import eng.eSystem.EStringBuilder;
import eng.eSystem.Tuple;
import eng.jAtcSim.newLib.speeches.atc.atc2user.RunwayInUseNotification;
import eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers.SmartTextSpeechFormatter;

public class RunwayInUseNotificationFormatter extends SmartTextSpeechFormatter<RunwayInUseNotification> {
  @Override
  public String _format(RunwayInUseNotification input) {
    EStringBuilder sb = new EStringBuilder();
    if (input.getExpectedTime() != null)
      sb.appendFormatLine("Expected used runway configuration at %s",input.getExpectedTime().toHourMinuteString());

    sb.append("Departures - ");
    sb.appendItems(
        input.getDeparturesThresholds().select(q -> new Tuple<>(q.getThresholdName(), q.getCategories().toString())),
        q -> q.getA() + " for " + q.getB(),
        ", ");
    sb.appendLine("\n");
    sb.append("Arrivals - ");
    sb.appendItems(
        input.getArrivalsThresholds().select(q -> new Tuple<>(q.getThresholdName(), q.getCategories().toString())),
        q -> q.getA() + " for " + q.getB(),
        ", ");
    return sb.toString();
  }
}
