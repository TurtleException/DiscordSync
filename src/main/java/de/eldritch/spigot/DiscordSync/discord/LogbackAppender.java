package de.eldritch.spigot.DiscordSync.discord;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import de.eldritch.spigot.DiscordSync.DiscordSync;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LogbackAppender extends AppenderBase<ILoggingEvent> {
    @Override
    protected void append(ILoggingEvent event) {
        LogRecord record = new LogRecord(Level.INFO, "[JDA] " + event.getMessage());

        // set level
        if      (event.getLevel().equals(ch.qos.logback.classic.Level.ALL))     record.setLevel(Level.ALL);
        else if (event.getLevel().equals(ch.qos.logback.classic.Level.DEBUG))   record.setLevel(Level.FINER);
        else if (event.getLevel().equals(ch.qos.logback.classic.Level.ERROR))   record.setLevel(Level.SEVERE);
        else if (event.getLevel().equals(ch.qos.logback.classic.Level.INFO))    record.setLevel(Level.INFO);
        else if (event.getLevel().equals(ch.qos.logback.classic.Level.OFF))     record.setLevel(Level.OFF);
        else if (event.getLevel().equals(ch.qos.logback.classic.Level.TRACE))   record.setLevel(Level.FINE);
        else if (event.getLevel().equals(ch.qos.logback.classic.Level.WARN))    record.setLevel(Level.WARNING);

        record.setParameters(event.getArgumentArray());

        record.setInstant(Instant.ofEpochMilli(event.getTimeStamp()));

        record.setLoggerName(event.getLoggerName());


        // log
        DiscordSync.singleton.getLogger().log(record);
    }
}