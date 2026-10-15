package pl.fullstackdeveloper.common.proxy;

import pl.fullstackdeveloper.common.annotations.Atomic;

public final class AtomicInterceptor implements Proxies.Interceptor {

    private final TransactionManager transactionManager;

    public AtomicInterceptor(final TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object intercept(final Proxies.Invocation invocation) throws Throwable {
        if (!invocation.isAnnotatedWith(Atomic.class)) {
            return invocation.proceed();
        }
        transactionManager.begin();
        try {
            var result = invocation.proceed();
            transactionManager.commit();
            return result;
        } catch (Throwable throwable) {
            transactionManager.rollback();
            throw throwable;
        }
    }

}
