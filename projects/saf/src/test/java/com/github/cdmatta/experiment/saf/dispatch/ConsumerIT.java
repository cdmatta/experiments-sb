package com.github.cdmatta.experiment.saf.dispatch;

import com.github.cdmatta.experiment.saf.BaseIT;
import com.github.cdmatta.experiment.saf.config.SafProperties;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.jms.UncategorizedJmsException;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@SpringBootTest(
        properties = {
                "saf.redelivery-policy.maximum-redeliveries=1",
                "saf.redelivery-policy.back-off-multiplier=1",
                "saf.redelivery-policy.use-exponential-backoff=false"
        }
)
public class ConsumerIT extends BaseIT {

    @Test
    public void receivedMessagesOnDataQueueCanBeDispatched(CapturedOutput output) {
        jmsTemplate.send(SafProperties.DATA_QUEUE_NAME, session -> {
            TextMessage message = session.createTextMessage("Test data");
            message.setStringProperty("ROUTING_KEY", "backend");
            return message;
        });

        await().atMost(ofSeconds(1))
                .untilAsserted(() -> assertThat(output).contains("Dispatched routingKey=backend event=Test data"));
    }

    @Test
    public void whenMessageSendingFailsExceedingRedeliveryPolicyTheyEndUpInDlq() throws JMSException {
        String badMessage = "bad Message";
        String routingKey = "backend";
        doThrow(new UncategorizedJmsException("Fail"))
                .when(eventDispatcher)
                .dispatch(routingKey, badMessage);

        jmsTemplate.send(SafProperties.DATA_QUEUE_NAME, session -> {
            TextMessage message = session.createTextMessage(badMessage);
            message.setStringProperty("ROUTING_KEY", routingKey);
            return message;
        });

        verify(eventDispatcher, timeout(2_000).times(2)).dispatch(eq(routingKey), eq(badMessage));

        TextMessage poisonMessage = (TextMessage) jmsTemplate.receive("ActiveMQ.DLQ");
        assertThat(poisonMessage.getText()).isEqualTo(badMessage);
    }
}
