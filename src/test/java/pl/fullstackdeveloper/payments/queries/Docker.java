package pl.fullstackdeveloper.payments.queries;

import org.testcontainers.DockerClientFactory;

// Testy wymagające kontenerów są pomijane, gdy Docker nie działa
public final class Docker {

    public static boolean isAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private Docker() {
    }

}
