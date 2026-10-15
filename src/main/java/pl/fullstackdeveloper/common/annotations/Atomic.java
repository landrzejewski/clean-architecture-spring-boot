package pl.fullstackdeveloper.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Deklaracja intencji ("wykonaj atomowo") - bez zależności od konkretnej technologii transakcji
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Atomic {
}
