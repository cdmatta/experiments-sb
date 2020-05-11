package com.github.cdmatta.experiment.saf;

import com.github.cdmatta.experiment.saf.dispatch.EventDispatcher;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.jms.UncategorizedJmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.jms.TextMessage;
import java.util.Properties;
import java.util.stream.IntStream;

import static com.github.cdmatta.experiment.saf.config.SafProperties.DATA_QUEUE_NAME;
import static java.nio.file.Files.createTempDirectory;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.*;
import static org.springframework.util.SocketUtils.findAvailableTcpPort;

@ExtendWith({SpringExtension.class, MockitoExtension.class, OutputCaptureExtension.class})
@ContextConfiguration(initializers = BaseIT.DynamicBrokerInitializer.class)
@ActiveProfiles("test")
@Slf4j
public class BaseIT {

    @Autowired
    protected JmsTemplate jmsTemplate;

    @SpyBean
    protected EventDispatcher eventDispatcher;

    @Autowired
    protected MeterRegistry registry;

    public static class DynamicBrokerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            int nioPort = findAvailableTcpPort(60_000);
            String kahaDbPath = createTempDirectory("test-saf-kahadb-").toAbsolutePath().toString();

            log.info("Configuring activeMq nio port=" + nioPort);
            log.info("Configuring activeMq kahaDbPath=" + kahaDbPath);

            Properties p = new Properties();
            p.put("dynamicBrokerPort", nioPort);
            p.put("dynamicKahaDbPath", kahaDbPath);
            context.getEnvironment()
                    .getPropertySources()
                    .addFirst(new PropertiesPropertySource(this.getClass().getName(), p));
        }
    }

    protected void failAllDispatchCalls() {
        doThrow(new UncategorizedJmsException("Fail"))
                .when(eventDispatcher)
                .dispatch(eq("backend"), matches("bad Message.*"));
    }

    protected void sendMaxPoisonMessages(int maxCount) {
        IntStream.range(0, maxCount)
                .forEach(i -> {
                    sendOnePoisonMessage("bad Message " + i);
                });
    }

    protected void sendOnePoisonMessage(String s) {
        jmsTemplate.send(DATA_QUEUE_NAME, session -> {
            TextMessage message = session.createTextMessage(s);
            message.setStringProperty("ROUTING_KEY", "backend");
            return message;
        });
    }
}
