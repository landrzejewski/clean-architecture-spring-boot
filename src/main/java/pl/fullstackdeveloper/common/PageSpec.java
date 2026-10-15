package pl.fullstackdeveloper.common;

public record PageSpec(int index, int size) {

    public PageSpec {
        if (index < 0 || size < 1) {
            throw new IllegalArgumentException("Invalid page specification");
        }
    }

}
