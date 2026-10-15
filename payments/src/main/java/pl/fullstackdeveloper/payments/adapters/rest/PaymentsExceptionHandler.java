package pl.fullstackdeveloper.payments.adapters.rest;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.fullstackdeveloper.payments.application.CardNotFoundException;
import pl.fullstackdeveloper.payments.domain.DomainException;

import java.net.URI;
import java.util.Locale;

/*
 Tłumaczenie wyjątków aplikacji i domeny na odpowiedzi HTTP (RFC 9457 - Problem Details).
 Domena nie wie nic o kodach HTTP - ta wiedza należy do adaptera.
 */
@RestControllerAdvice(basePackageClasses = PaymentsExceptionHandler.class)
class PaymentsExceptionHandler {

    private final MessageSource messageSource;

    PaymentsExceptionHandler(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(CardNotFoundException.class)
    ProblemDetail onCardNotFound(final CardNotFoundException exception, final Locale locale) {
        return problem(HttpStatus.NOT_FOUND, exception, locale);
    }

    @ExceptionHandler(DomainException.class)
    ProblemDetail onDomainException(final DomainException exception, final Locale locale) {
        return problem(HttpStatus.UNPROCESSABLE_CONTENT, exception, locale);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail onIllegalArgument(final IllegalArgumentException exception, final Locale locale) {
        return problem(HttpStatus.BAD_REQUEST, exception, locale);
    }

    private ProblemDetail problem(final HttpStatus status, final Exception exception, final Locale locale) {
        var name = exception.getClass().getSimpleName();
        var detail = messageSource.getMessage(name, null, exception.getMessage(), locale);
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("urn:problem:payments:" + name));
        return problem;
    }

}
