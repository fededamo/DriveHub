package it.unifi.ing.drivehub.domain;

/** Raised when an operation would violate an aggregate invariant. */
public final class DomainRuleViolationException extends IllegalStateException {
    public DomainRuleViolationException(String message) {
        super(message);
    }
}
