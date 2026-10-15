package pl.fullstackdeveloper.common.aop;

/*
 Kolejność aspektów (mniejsza wartość = bardziej zewnętrzny):
 retry -> timer -> atomic -> metoda
 Retry musi być na zewnątrz transakcji - każda próba w nowej transakcji.
 */
final class AspectOrder {

    static final int RETRY = 10;
    static final int TIMER = 20;
    static final int ATOMIC = 30;

    private AspectOrder() {
    }

}
