package de.turtle_exception.discordsync.util;

import de.turtle_exception.discordsync.DiscordSync;
import net.dv8tion.jda.api.JDA;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.LogRecord;

// this works with Log4J
public class JDALogFilter extends AbstractFilter {
    private final DiscordSync plugin;
    private final Logger logger = (Logger) LogManager.getLogger(JDA.class);

    private boolean enabled = true;
    private Level level;

    public JDALogFilter(@NotNull DiscordSync plugin) {
        this.plugin = plugin;
        this.level = plugin.getLogger().getLevel();
        this.start();
        this.logger.addFilter(this);
    }

    @Override
    public void stop() {
        this.enabled = false;
        super.stop();
    }

    @Override
    public Result filter(LogEvent event) {
        if (!enabled) return Result.NEUTRAL;
        this.handle(event);
        return Result.DENY;
    }

    public void setLevel(@NotNull Level level) {
        this.level = level;
    }

    public @NotNull Level getLevel() {
        return this.level;
    }

    private void handle(LogEvent event) {
        if (event == null) return;

        String msg   = event.getMessage().getFormattedMessage();
        Level  level = getLevel(event);

        if (level.intValue() < this.level.intValue() || this.level.equals(Level.OFF))
            return;

        LogRecord record = new LogRecord(level, "[JDA] " + msg);

        record.setInstant(Instant.ofEpochMilli(event.getTimeMillis()));

        if (event.getThrown() != null)
            record.setThrown(event.getThrown());

        plugin.getLogger().log(record);
    }

    private @NotNull Level getLevel(@NotNull LogEvent event) {
        org.apache.logging.log4j.Level level = event.getLevel();
        if (level.equals(org.apache.logging.log4j.Level.OFF))   return Level.OFF;
        if (level.equals(org.apache.logging.log4j.Level.ERROR)) return Level.SEVERE;
        if (level.equals(org.apache.logging.log4j.Level.WARN))  return Level.WARNING;
        if (level.equals(org.apache.logging.log4j.Level.INFO))  return Level.INFO;
        if (level.equals(org.apache.logging.log4j.Level.DEBUG)) return Level.FINE;
        if (level.equals(org.apache.logging.log4j.Level.TRACE)) return Level.FINEST;
        if (level.equals(org.apache.logging.log4j.Level.ALL))   return Level.ALL;
        return Level.INFO;
    }
}
