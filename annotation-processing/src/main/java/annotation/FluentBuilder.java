package annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created on: 7/21/20
 *
 * @author Denis Citaku
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface FluentBuilder {

  String[] excludeFields() default {};

  @interface Exclude { }

}
