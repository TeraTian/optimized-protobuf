package cn.tera.protobuf.coder.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EncodeDefault {

    /**
     * default values for optimization
     * @return
     */
    String[] value() default {};

    /**
     * min app version
     * @return
     */
    String version() default "";

    boolean replace() default false;
}
