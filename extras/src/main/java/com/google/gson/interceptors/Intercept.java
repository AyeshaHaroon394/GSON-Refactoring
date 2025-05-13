package com.google.gson.interceptors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for specifying post-deserialization interceptors. Applied to classes to indicate they
 * should be processed after deserialization.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Intercept {
  Class<? extends JsonPostDeserializer<?>> postDeserialize();
}
