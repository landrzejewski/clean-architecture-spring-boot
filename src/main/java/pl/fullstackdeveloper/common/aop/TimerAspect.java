package pl.fullstackdeveloper.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(AspectOrder.TIMER)
public class TimerAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimerAspect.class);

    @Around("@annotation(pl.fullstackdeveloper.common.annotations.Timer) || @within(pl.fullstackdeveloper.common.annotations.Timer)")
    public Object measure(final ProceedingJoinPoint joinPoint) throws Throwable {
        var start = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            LOGGER.info("{} took {} µs", joinPoint.getSignature().toShortString(), (System.nanoTime() - start) / 1_000);
        }
    }

}
