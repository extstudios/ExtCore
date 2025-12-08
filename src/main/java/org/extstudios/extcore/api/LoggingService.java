package org.extstudios.extcore.api;

public interface LoggingService {

    void info(Object... parts);

    void warn(Object... parts);

    void warn(Throwable throwable, Object... message);

    void error(Object... parts);

    void error(Throwable throwable, Object... message);

    void debug(Object... parts);

    boolean isDebugEnabled();

    void setDebugEnabled(boolean enabled);

    void setPrefix(String prefix);

    LoggingService withPrefix(String prefix);

    void seperator();

    void header(String title);
}