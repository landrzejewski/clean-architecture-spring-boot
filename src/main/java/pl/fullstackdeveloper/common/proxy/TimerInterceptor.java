package pl.fullstackdeveloper.common.proxy;

import pl.fullstackdeveloper.common.annotations.Timer;

import java.util.logging.Logger;

public final class TimerInterceptor implements Proxies.Interceptor {

    private static final Logger LOGGER = Logger.getLogger(TimerInterceptor.class.getName());

    @Override
    public Object intercept(final Proxies.Invocation invocation) throws Throwable {
        if (!invocation.isAnnotatedWith(Timer.class)) {
            return invocation.proceed();
        }
        var start = System.nanoTime();
        try {
            return invocation.proceed();
        } finally {
            LOGGER.info("%s took %d µs".formatted(invocation.method().getName(), (System.nanoTime() - start) / 1_000));
        }
    }

}
