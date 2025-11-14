package io.hhplus.ecommerce.queue;


import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QueueAnnotation {
    String key();
    String topic() default "";
}
