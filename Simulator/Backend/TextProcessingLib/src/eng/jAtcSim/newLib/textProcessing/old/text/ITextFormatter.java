//package eng.jAtcSim.newLib.textProcessing.old.text;
//
//import eng.jAtcSim.newLib.speeches.base.ISpeech;
//import eng.jAtcSim.newLib.textProcessing.old.base.IFormatter;
//
//public interface ITextFormatter<TSource extends ISpeech> extends IFormatter<TSource, String> {
//
//  @Override
//  public abstract String format(TSource speech);
//
//  //  default String formatMessageParticipant(Participant participant){
////    EAssert.Argument.isNotNull(participant);
////    return participant.getId();
////  }
////
////  default String format(Participant source, Participant target, ISpeech speech) {
////    EAssert.Argument.isNotNull(source);
////    EAssert.Argument.isNotNull(target);
////    String sb = this.formatMessageParticipant(source) +
////        " => " +
////        this.formatMessageParticipant(target) +
////        ": " +
////        this.format(speech);
////    return sb;
////  }
//}
