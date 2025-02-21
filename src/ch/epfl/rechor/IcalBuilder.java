package ch.epfl.rechor;

public final class IcalBuilder {
    public enum Component {
        VCALENDAR,
        VEVENT
    }

    public enum Name {
        BEGIN,
        END,
        PRODID,
        VERSION,
        UID,
        DTSTAMP,
        DTSTART,
        DTEND,
        SUMMARY,
        DESCRIPTION
    }
}
