package pl.fullstackdeveloper.common.proxy;

import java.util.Arrays;
import java.util.logging.Logger;

public final class LoggingInterceptor implements Proxies.Interceptor {

    private static final Logger LOGGER = Logger.getLogger(LoggingInterceptor.class.getName());

    @Override
    public Object intercept(final Proxies.Invocation invocation) throws Throwable {
        var name = invocation.target().getClass().getSimpleName() + "." + invocation.method().getName();
        LOGGER.info("-> %s %s".formatted(name, Arrays.toString(invocation.arguments())));
        try {
            var result = invocation.proceed();
            LOGGER.info("<- %s returned %s".formatted(name, result));
            return result;
        } catch (Throwable throwable) {
            LOGGER.info("<- %s threw %s".formatted(name, throwable.getClass().getSimpleName()));
            throw throwable;
        }
    }

}
