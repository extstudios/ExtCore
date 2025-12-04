package org.extstudios.extCore.Internal;

import org.extstudios.extCore.API.LoggingService;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingServiceImpl implements LoggingService {

    private final Logger logger;
    private volatile boolean debugEnabled;
    private String prefix;

    public LoggingServiceImpl(Logger logger) {
        this(logger, null);
    }

    public LoggingServiceImpl(Logger logger, String prefix) {
        this.logger = logger;
        this.prefix = prefix;
        this.debugEnabled = false;
    }

    @Override
    public void info(Object... parts) {
        logger.info(formatMessage(parts));
    }

    @Override
    public void warn(Object... parts) {
        logger.warning(formatMessage(parts));
    }

    @Override
    public void warn(Throwable throwable, Object... message) {
        logger.log(Level.WARNING, formatMessage(message), throwable);
    }

    @Override
    public void error(Object... parts) {
        logger.severe(formatMessage(parts));
    }

    @Override
    public void error(Throwable throwable, Object... message) {
        logger.log(Level.SEVERE, formatMessage(message), throwable);
    }

    public void debug(Object... parts) {
        if (debugEnabled) {
            logger.info(formatMessage("[DEBUG]", parts));
        }
    }

    @Override
    public boolean isDebugEnabled() {
    return debugEnabled;
    }

    @Override
    public void setDebugEnabled(boolean enabled) {
        this.debugEnabled = enabled;
        if (enabled) {
            logger.info(formatMessage("Debug logging enabled"));
        } else {
            logger.info(formatMessage("Debug logging disabled"));
        }
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public LoggingService withPrefix(String prefix) {
        LoggingServiceImpl newLogger = new LoggingServiceImpl(logger, prefix);
        newLogger.debugEnabled = this.debugEnabled;
        return newLogger;
    }

    @Override
    public void seperator() {
        logger.info("================================================");
    }

    @Override
    public void header(String title) {
        seperator();
        logger.info(formatMessage(title));
        seperator();
    }

    private String formatMessage(Object... parts) {
        if (parts == null || parts.length == 0) {
            return applyPrefix("");
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(formatObject(parts[i]));
        }
        return applyPrefix(sb.toString());
    }

    private String formatObject(Object obj) {
        if (obj == null) {
            return "null";
        }

        if (obj instanceof Throwable) {
            return ((Throwable) obj).getMessage();
        }

        return obj.toString();
    }

    private String applyPrefix(String message) {
        if (prefix != null && !prefix.isEmpty()) {
            return prefix + " " + message;
        }
        return message;
    }

}
