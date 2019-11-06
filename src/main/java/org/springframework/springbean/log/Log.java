package org.springframework.springbean.log;

public interface Log {
  boolean isFatalEnabled();

  boolean isErrorEnabled();

  boolean isWarnEnabled();

  boolean isInfoEnabled();

  boolean isDebugEnabled();

  boolean isTraceEnabled();

  void fatal(Object var1);

  void fatal(Object var1, Throwable var2);

  void error(Object var1);

  void error(Object var1, Throwable var2);

  void warn(Object var1);

  void warn(Object var1, Throwable var2);

  void info(Object var1);

  void info(Object var1, Throwable var2);

  void debug(Object var1);

  void debug(Object var1, Throwable var2);

  void trace(Object var1);

  void trace(Object var1, Throwable var2);
}
