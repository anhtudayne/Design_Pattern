package com.cinema.booking.patterns.prototype;

import org.springframework.mail.SimpleMailMessage;

/**
 * Prototype pattern — defines the contract for clonable email templates.
 * Each template holds subject/body templates and can produce a ready-to-send SimpleMailMessage.
 */
public interface EmailTemplate {
    /** Return a fresh copy of this prototype (fields populated before sending). */
    EmailTemplate copy();

    /** Build a SimpleMailMessage with recipient and any variable substitutions applied. */
    SimpleMailMessage toMessage();
}
