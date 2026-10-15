package pl.fullstackdeveloper.common.proxy;

public interface TransactionManager {

    void begin();

    void commit();

    void rollback();

}
