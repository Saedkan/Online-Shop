package org.example.domain;

/**
 * Unchecked on purpose: Spring rolls back a transaction when an unchecked
 * exception leaves the transactional method. Plain Java, no framework imports.
 */
public class DuplicateOrder extends RuntimeException {
    public DuplicateOrder(String businessKey) {
        super("duplicate order: " + businessKey);
    }
}
