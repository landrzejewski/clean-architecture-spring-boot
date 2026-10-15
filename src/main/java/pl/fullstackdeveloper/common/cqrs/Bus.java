package pl.fullstackdeveloper.common.cqrs;

/*
 Szyna komend i zapytań: nadawca nie zna obiektu, który obsłuży komunikat.
 Dzięki temu strona zapisu i strona odczytu mogą ewoluować niezależnie.
 */
public interface Bus {

    <R> R send(Command<R> command);

    <R> R ask(Query<R> query);

}
