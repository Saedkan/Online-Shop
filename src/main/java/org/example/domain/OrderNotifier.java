package org.example.domain;

/** Outbound port: tells the outside world that an order was registered. */
public interface OrderNotifier {
    void registered(String businessKey);
}
