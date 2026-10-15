package pl.fullstackdeveloper.common.web;

import java.net.URI;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri;

public final class LocationUri {

    public static URI fromRequest(final String pathSegment) {
        return fromCurrentRequestUri().pathSegment(pathSegment).build().toUri();
    }

    private LocationUri() {
    }

}
