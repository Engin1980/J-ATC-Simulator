package eng.newXmlUtils.annotations;

import java.lang.annotation.*;

@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(XmlConstructorParameters.class)
public @interface XmlConstructorParameter {
  int index();
  String valueString();
}
