package pl.fullstackdeveloper.common.cqrs;

public interface QueryHandler<Q extends Query<R>, R> {

    R handle(Q query);

}
