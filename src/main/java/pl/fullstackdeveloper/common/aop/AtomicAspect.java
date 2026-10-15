package pl.fullstackdeveloper.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

// Odpowiednik AtomicInterceptor z 04 - tym razem transakcje dostarcza Spring
@Aspect
@Component
@Order(AspectOrder.ATOMIC)
public class AtomicAspect {

    private final TransactionTemplate transactionTemplate;

    public AtomicAspect(final PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Around("@annotation(pl.fullstackdeveloper.common.annotations.Atomic) || @within(pl.fullstackdeveloper.common.annotations.Atomic)")
    public Object runInTransaction(final ProceedingJoinPoint joinPoint) throws Throwable {
        var result = new Object[1];
        var failure = new Throwable[1];
        transactionTemplate.executeWithoutResult(status -> {
            try {
                result[0] = joinPoint.proceed();
            } catch (Throwable throwable) {
                failure[0] = throwable;
                status.setRollbackOnly();
            }
        });
        if (failure[0] != null) {
            throw failure[0];
        }
        return result[0];
    }

}
