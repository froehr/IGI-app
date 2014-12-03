package de.ifgi.igiapp.igi_app.Bus;

/**
 * Created by helo on 03.12.14.
 */
public class AnswerAvailableEvent {

    public final String event;

    public AnswerAvailableEvent(String event) {
        this.event = event;
    }

    public String getEvent() {
        return event;
    }
}
