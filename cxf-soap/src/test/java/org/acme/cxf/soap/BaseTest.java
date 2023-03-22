package org.acme.cxf.soap;

import io.quarkus.runtime.LaunchMode;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

public class BaseTest {
    protected String getServerUrl() {
        Config config = ConfigProvider.getConfig();
        final int port = LaunchMode.current().equals(LaunchMode.TEST) ? config.getValue("quarkus.http.test-port", Integer.class)
                : config.getValue("quarkus.http.port", Integer.class);
        return String.format("http://localhost:%d", port);
    }
}
