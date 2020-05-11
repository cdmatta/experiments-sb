package com.github.cdmatta.experiment.saf.health;

import com.github.cdmatta.experiment.saf.BaseIT;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = {
                "saf.redelivery-policy.maximum-redeliveries=0",
                "saf.redelivery-policy.use-exponential-backoff=false",
                "saf.dlq-max-message-count=" + DlqHealthIndicatorIT.DLQ_MAX_MESSAGE_COUNT
        }
)
public class DlqHealthIndicatorIT extends BaseIT {

    final static int DLQ_MAX_MESSAGE_COUNT = 5;

    @LocalServerPort
    private int localServerPort;

    @BeforeEach
    public void beforeEach() {
        RestAssured.port = localServerPort;
    }

    @Test
    public void whenDlqMaxMessageCountIsExceeded_thenHealthIndicatorResultsInOutOfService() {
        failAllDispatchCalls();
        sendMaxPoisonMessages(DLQ_MAX_MESSAGE_COUNT);
        verifyStatusIsUp();
        sendOnePoisonMessage("bad Message final");
        verifyStatusIsOutOfService();
    }

    private void verifyStatusIsUp() {
        await().atMost(ofSeconds(5)).untilAsserted(() -> assertThat(registry.get("saf.dataq.current.message.count").gauge().value()).isEqualTo(0));
        verify(eventDispatcher, timeout(2_000).times(DLQ_MAX_MESSAGE_COUNT)).dispatch(eq("backend"), matches("bad Message.*"));

        assertThat(registry.get("saf.dlq.current.message.count").gauge().value()).isEqualTo(DLQ_MAX_MESSAGE_COUNT);
        given()
                .accept(JSON)
                .when()
                .get("/actuator/health")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HTTP_OK)
                .body("status", is("UP"));
    }

    private void verifyStatusIsOutOfService() {
        await().atMost(ofSeconds(5)).untilAsserted(() -> assertThat(registry.get("saf.dataq.current.message.count").gauge().value()).isEqualTo(0));
        verify(eventDispatcher, timeout(2_000).times(1)).dispatch(eq("backend"), matches("bad Message final"));

        assertThat(registry.get("saf.dlq.current.message.count").gauge().value()).isEqualTo(DLQ_MAX_MESSAGE_COUNT + 1);
        given()
                .accept(JSON)
                .when()
                .get("/actuator/health")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HTTP_UNAVAILABLE)
                .body("status", is("OUT_OF_SERVICE"))
                .body("components.dlq.status", is("OUT_OF_SERVICE"))
                .body("components.dlq.details.maximum_message_count", is(DLQ_MAX_MESSAGE_COUNT))
                .body("components.dlq.details.current_message_count", is(DLQ_MAX_MESSAGE_COUNT + 1));
    }
}
