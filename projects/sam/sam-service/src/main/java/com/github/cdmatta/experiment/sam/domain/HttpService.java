package com.github.cdmatta.experiment.sam.domain;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static java.lang.System.currentTimeMillis;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Optional.empty;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

@Slf4j
@Getter
@Setter
@ToString
public class HttpService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss");

    private URL url;

    private String mandatoryContent;

    private int recentFailureCount = 0;

    private Optional<LocalDateTime> downtimeStart = empty();

    private String lastFailureDetail;

    public void resetFailureCountersOnSuccess() {
        recentFailureCount = 0;
        downtimeStart = empty();
    }

    public void updateFailureCounters(final LocalDateTime monitoringStartTime) {
        ++recentFailureCount;
        if (downtimeStart.isPresent()) {
            return;
        }
        downtimeStart = Optional.of(monitoringStartTime);
    }

    @JsonView
    public String downSince() {
        return downtimeStart.map(v -> v.format(FORMATTER)).orElse("");
    }

    public boolean serviceIsUp() {
        return recentFailureCount == 0;
    }

    /**
     * Check the service availability.
     *
     * @param timeoutSeconds Max seconds the thread will wait before assuming service is down
     * @return True if service is up. False otherwise
     */
    public boolean checkStatus(final int timeoutSeconds) {
        HttpURLConnection connection = null;
        var startMillis = currentTimeMillis();
        InputStream inputStream = null;
        try {
            connection = openConnection(timeoutSeconds);
            var responseCode = connection.getResponseCode();
            if (responseCode != HTTP_OK) {
                log.error("Result up=false url={} responseCode={}", url, responseCode);
                lastFailureDetail = "Response code " + responseCode + " != HTTP OK/200";
                return false;
            }
            inputStream = connection.getInputStream();
            var contentIsValid = isInputStreamContentValid(inputStream);
            var duration = currentTimeMillis() - startMillis;
            log.info("Result url={} responseCode={} validContent={} took={} ms", url, responseCode,
                    contentIsValid, duration);
            lastFailureDetail = contentIsValid ? "" : "Page does not contain required text";
            return contentIsValid;
        } catch (Exception e) {
            log.error("Result up=false. Exception when checking url=" + url, e);
            lastFailureDetail = "Failure in http connection/reading page contents";
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception ignored) {
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return false;
    }

    private boolean isInputStreamContentValid(InputStream inputStream) {
        if (StringUtils.isBlank(mandatoryContent)) {
            return true;
        }
        try {
            var htmlPage = trimToEmpty(IOUtils.toString(inputStream, Charset.defaultCharset()));
            return contains(htmlPage, mandatoryContent);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read content", e);
        }
    }

    protected HttpURLConnection openConnection(final int timeoutSeconds) throws IOException {
        var connection = (HttpURLConnection) url.openConnection();
        var timeoutMillis = timeoutSeconds * 1000;
        connection.setConnectTimeout(timeoutMillis);
        connection.setReadTimeout(timeoutMillis);
        connection.connect();
        return connection;
    }

    @Override
    public boolean equals(final Object obj) {
        return reflectionEquals(this, obj, "recentFailureCount", "downtimeStart");
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this, "recentFailureCount", "downtimeStart");
    }
}
