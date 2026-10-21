package it.unifi.ing.drivehub.domain;

/** Base for entities whose identifier is assigned by a persistence adapter. */
public abstract class BaseEntity {
    private Long id;

    protected BaseEntity(Long id) {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        this.id = id;
    }

    public final Long id() {
        return id;
    }

    public final void assignId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        if (this.id != null && this.id != id) {
            throw new DomainRuleViolationException("entity id is already assigned");
        }
        this.id = id;
    }

    public final long requireId() {
        if (id == null) {
            throw new DomainRuleViolationException("entity has not been persisted yet");
        }
        return id;
    }
}
