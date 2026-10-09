package org.example.outbound;

import org.example.domain.OrderNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Adapter for the OrderNotifier port. Only logs; no real HTTP call. */
@Component
public class LogOrderNotifier implements OrderNotifier {
    private static final Logger log = LoggerFactory.getLogger(LogOrderNotifier.class);

    @Override
    public void registered(String businessKey) {
        log.info("Order registered: {}", businessKey);
    }
}
