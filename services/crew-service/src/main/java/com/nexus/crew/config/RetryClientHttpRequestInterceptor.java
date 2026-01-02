package com.nexus.crew.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.net.ConnectException;

/**
 * HTTP request interceptor that implements exponential backoff retry logic
 * for connection failures during service-to-service communication.
 */
public class RetryClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RetryClientHttpRequestInterceptor.class);

    private final int maxRetries;
    private final long initialDelayMs;
    private final double multiplier;
    private final long maxDelayMs;

    public RetryClientHttpRequestInterceptor() {
        this(5, 1000, 2.0, 16000);
    }

    public RetryClientHttpRequestInterceptor(int maxRetries, long initialDelayMs, double multiplier, long maxDelayMs) {
        this.maxRetries = maxRetries;
        this.initialDelayMs = initialDelayMs;
        this.multiplier = multiplier;
        this.maxDelayMs = maxDelayMs;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        int attempts = 0;
        long delay = initialDelayMs;
        Exception lastException = null;

        while (attempts <= maxRetries) {
            try {
                return execution.execute(request, body);
            } catch (ResourceAccessException | ConnectException e) {
                lastException = e;
                attempts++;

                if (attempts > maxRetries) {
                    log.error("Request to {} failed after {} attempts: {}",
                            request.getURI(), attempts, e.getMessage());
                    break;
                }

                log.warn("Request to {} failed (attempt {}/{}), retrying in {}ms: {}",
                        request.getURI(), attempts, maxRetries + 1, delay, e.getMessage());

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Retry interrupted", ie);
                }

                delay = Math.min((long) (delay * multiplier), maxDelayMs);
            } catch (IOException e) {
                // For other IOExceptions (not connection related), check if retryable
                if (isRetryableException(e)) {
                    lastException = e;
                    attempts++;

                    if (attempts > maxRetries) {
                        log.error("Request to {} failed after {} attempts: {}",
                                request.getURI(), attempts, e.getMessage());
                        break;
                    }

                    log.warn("Request to {} failed (attempt {}/{}), retrying in {}ms: {}",
                            request.getURI(), attempts, maxRetries + 1, delay, e.getMessage());

                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Retry interrupted", ie);
                    }

                    delay = Math.min((long) (delay * multiplier), maxDelayMs);
                } else {
                    throw e;
                }
            }
        }

        // If we've exhausted retries, throw the last exception
        if (lastException instanceof IOException) {
            throw (IOException) lastException;
        } else if (lastException != null) {
            throw new IOException("Request failed after " + attempts + " attempts", lastException);
        }

        throw new IOException("Request failed after " + attempts + " attempts");
    }

    private boolean isRetryableException(IOException e) {
        Throwable cause = e.getCause();
        return cause instanceof ConnectException ||
               e.getMessage() != null && (
                   e.getMessage().contains("Connection refused") ||
                   e.getMessage().contains("Connection reset") ||
                   e.getMessage().contains("Connection timed out")
               );
    }
}
