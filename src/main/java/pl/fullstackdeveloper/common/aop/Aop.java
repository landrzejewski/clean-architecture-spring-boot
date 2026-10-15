package pl.fullstackdeveloper.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.annotation.Annotation;

final class Aop {

    // Adnotacja z metody implementacji, a jeśli jej brak - z klasy
    static <A extends Annotation> A findAnnotation(final ProceedingJoinPoint joinPoint, final Class<A> type) {
        var method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        var targetClass = joinPoint.getTarget().getClass();
        try {
            var targetMethod = targetClass.getMethod(method.getName(), method.getParameterTypes());
            var annotation = AnnotatedElementUtils.findMergedAnnotation(targetMethod, type);
            if (annotation != null) {
                return annotation;
            }
        } catch (NoSuchMethodException exception) {
            // metoda niepubliczna - przechodzimy do adnotacji klasy
        }
        return AnnotatedElementUtils.findMergedAnnotation(targetClass, type);
    }

    private Aop() {
    }

}
