package pl.fullstackdeveloper.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pl.fullstackdeveloper.common.annotations.Retry;

import java.util.Arrays;

import static pl.fullstackdeveloper.common.aop.Aop.findAnnotation;

@Aspect
@Component
@Order(AspectOrder.RETRY)
public class RetryAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryAspect.class);

    @Around("@annotation(pl.fullstackdeveloper.common.annotations.Retry)")
    public Object retry(final ProceedingJoinPoint joinPoint) throws Throwable {
        var retry = findAnnotation(joinPoint, Retry.class);
        var attempt = 0;
        while (true) {
            attempt++;
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                if (attempt >= retry.attempts() || !isRetryable(throwable, retry)) {
                    throw throwable;
                }
                LOGGER.info("{} failed with {} (attempt {}), retrying", joinPoint.getSignature().toShortString(),
                        throwable.getClass().getSimpleName(), attempt);
            }
        }
    }

    private boolean isRetryable(final Throwable throwable, final Retry retry) {
        return Arrays.stream(retry.on()).anyMatch(type -> type.isInstance(throwable));
    }

}
