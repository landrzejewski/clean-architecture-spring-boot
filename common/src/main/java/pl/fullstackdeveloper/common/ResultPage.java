package pl.fullstackdeveloper.common;

import java.util.List;
import java.util.function.Function;

public record ResultPage<T>(List<T> content, PageSpec pageSpec, int totalPages) {

    public <R> ResultPage<R> map(final Function<T, R> mapper) {
        return new ResultPage<>(content.stream().map(mapper).toList(), pageSpec, totalPages);
    }

}
