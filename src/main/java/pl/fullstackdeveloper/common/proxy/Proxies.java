package pl.fullstackdeveloper.common.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/*
 JDK dynamic proxy - jeden generyczny "dekorator" dla dowolnego interfejsu.
 Działa tylko dla interfejsów: kolejny argument za portami wejściowymi z 02.
 Interceptory tworzą łańcuch (Chain of Responsibility) w jednym proxy, dzięki czemu każdy z nich
 widzi oryginalny obiekt i jego adnotacje (zagnieżdżanie proxy w proxy by je ukryło).
 Tak samo działa Spring AOP: jeden proxy + lista advice.
 */
public final class Proxies {

    @SuppressWarnings("unchecked")
    public static <T> T wrap(final Class<T> type, final T target, final Interceptor... interceptors) {
        var chain = List.of(interceptors);
        InvocationHandler handler = (proxy, method, arguments) ->
                new Invocation(target, method, arguments, chain, 0).proceed();
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    @FunctionalInterface
    public interface Interceptor {

        Object intercept(Invocation invocation) throws Throwable;

    }

    public record Invocation(Object target, Method method, Object[] arguments, List<Interceptor> chain, int position) {

        // Wywołuje kolejny interceptor z łańcucha, a na końcu metodę obiektu docelowego
        public Object proceed() throws Throwable {
            if (position < chain.size()) {
                return chain.get(position).intercept(new Invocation(target, method, arguments, chain, position + 1));
            }
            try {
                return method.invoke(target, arguments);
            } catch (InvocationTargetException exception) {
                throw exception.getCause();
            }
        }

        // Adnotację szukamy na metodzie i klasie implementacji (nie interfejsu)
        public boolean isAnnotatedWith(final Class<? extends Annotation> annotation) throws NoSuchMethodException {
            var targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
            return targetMethod.isAnnotationPresent(annotation) || target.getClass().isAnnotationPresent(annotation);
        }

    }

    private Proxies() {
    }

}
