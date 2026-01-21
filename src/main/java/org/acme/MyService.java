package org.acme;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.hostbased.AcceptAllHostBasedAuthenticator;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

@Singleton
@Startup
public class MyService {
    private static final Logger log = LoggerFactory.getLogger(MyService.class);
    private final SshServer sshServer;

    public MyService( ) {
        this.sshServer = SshServer.setUpDefaultServer();
    }

    @PostConstruct
    public void start() throws IOException {
        configureServer(this.sshServer);
        sshServer.start();
        log.info("SSH Server running on port {}", sshServer.getPort());
    }

    private void configureServer(SshServer server) {
        server.setPort(2222);
        server.setKeyPairProvider(
                new SimpleGeneratorHostKeyProvider(Path.of("/tmp/key")));
        server.setPublickeyAuthenticator(null);
        server.setPasswordAuthenticator(new PasswordAuthenticator() {
            @Override
            public boolean authenticate(String username, String password, ServerSession session) throws PasswordChangeRequiredException, AsyncAuthException {
                try {
                    String configValue = ConfigProvider.getConfig().getValue("test.uri", String.class);
                    log.info("My config is {}", configValue);
                } catch (Exception e) {
                    log.error("Error", e);
                }
                return true;
            }
        });
        server.setKeyboardInteractiveAuthenticator(null);
        server.setHostBasedAuthenticator(AcceptAllHostBasedAuthenticator.INSTANCE);
    }

    @PreDestroy
    public void stop() {
        try {
            sshServer.stop();
            log.info("SSH Server stopped");
        } catch (IOException e) {
            log.error("Error stopping servers", e);
        }
    }
}
