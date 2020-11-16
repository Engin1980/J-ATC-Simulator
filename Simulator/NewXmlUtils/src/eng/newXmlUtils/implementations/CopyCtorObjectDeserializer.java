//package eng.newXmlUtils.implementations;
//
//import eng.eSystem.collections.IReadOnlyList;
//import eng.eSystem.eXml.XElement;
//import eng.eSystem.functionalInterfaces.Consumer2;
//import eng.eSystem.utilites.ReflectionUtils;
//import eng.newXmlUtils.XmlContext;
//import eng.newXmlUtils.base.Deserializer;
//import eng.newXmlUtils.utils.InternalXmlUtils;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.Modifier;
//
//public class CopyCtorObjectDeserializer implements Deserializer {
//  private Consumer2<T, XmlContext> afterLoadAction = null;
//
//  @Override
//  public Object invoke(XElement e, XmlContext c) {
//    Object ret;
//
//    Class<?> type = InternalXmlUtils.loadType(e);
//
//    ret = getInstance(type, c);
//
//    IReadOnlyList<Field> fields = ReflectionUtils.ClassUtils.getFields(type).where(q -> !Modifier.isStatic(q.getModifiers()));
//
//    for (Field field : fields) {
//      restoreField(e, ret, field, c);
//    }
//
//    if (this.afterLoadAction != null)
//      this.afterLoadAction.invoke((T) ret, c);
//
//    return ret;
//  }
//
//  public CopyCtorObjectDeserializer<T> withAfterLoadAction(Consumer2<T, XmlContext> afterLoadAction) {
//    this.afterLoadAction = afterLoadAction;
//    return this;
//  }
//}
