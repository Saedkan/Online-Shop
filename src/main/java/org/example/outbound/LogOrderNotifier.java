package org.example.outbound;

import org.example.domain.OrderNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adapter for the OrderNotifier port. Only logs; no real HTTP call (that is Lab 6).
 * It already receives the downstream base URL and timeout from yaml.
 */
@Component
public class LogOrderNotifier implements OrderNotifier {
    private static final Logger log = LoggerFactory.getLogger(LogOrderNotifier.class);

    private final DownstreamProperties downstream;

    public LogOrderNotifier(DownstreamProperties downstream) {
        this.downstream = downstream;
    }

    @Override
    public void registered(String businessKey) {
        log.info("Order registered: {} (downstream {}, timeout {})",
                businessKey, downstream.baseUrl(), downstream.timeout());
    }
}
