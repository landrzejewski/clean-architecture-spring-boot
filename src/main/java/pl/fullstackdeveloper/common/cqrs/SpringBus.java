package pl.fullstackdeveloper.common.cqrs;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 Implementacja szyny oparta o kontener Springa: handler wyszukiwany jest po pierwszym typie generycznym,
 np. AddTransactionCommand -> bean implementujący CommandHandler<AddTransactionCommand, ?>.
 */
@Component
class SpringBus implements Bus {

    private final ListableBeanFactory beanFactory;
    private final Map<Class<?>, Object> handlers = new ConcurrentHashMap<>();

    SpringBus(final ListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R send(final Command<R> command) {
        var handler = (CommandHandler<Command<R>, R>) handlerFor(command.getClass(), CommandHandler.class);
        return handler.handle(command);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R ask(final Query<R> query) {
        var handler = (QueryHandler<Query<R>, R>) handlerFor(query.getClass(), QueryHandler.class);
        return handler.handle(query);
    }

    private Object handlerFor(final Class<?> messageType, final Class<?> handlerType) {
        return handlers.computeIfAbsent(messageType, type -> {
            var names = Arrays.stream(beanFactory.getBeanNamesForType(handlerType))
                    .filter(name -> handles(name, handlerType, type))
                    .toList();
            if (names.size() != 1) {
                throw new IllegalStateException("Expected exactly one handler for %s, found %s".formatted(type.getSimpleName(), names));
            }
            return beanFactory.getBean(names.getFirst());
        });
    }

    private boolean handles(final String beanName, final Class<?> handlerType, final Class<?> messageType) {
        var beanType = beanFactory.getType(beanName);
        return beanType != null && messageType.equals(ResolvableType.forClass(beanType).as(handlerType).getGeneric(0).resolve());
    }

}
