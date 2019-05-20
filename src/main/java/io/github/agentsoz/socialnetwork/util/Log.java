package io.github.agentsoz.socialnetwork.util;

import io.github.agentsoz.socialnetwork.SNConfig;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;

public class Log {
/* 
 *  Logger class used for  test clases
 */
	private static Logger logger = null;
	private static Level logLevel;
	
	
	public static Logger createLogger(String string, String file) {
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		PatternLayoutEncoder ple = new PatternLayoutEncoder();

		ple.setPattern("%date %level [%thread] %logger{10} [%file:%line]%n%msg%n%n"); // layout pattern for the appender
		ple.setContext(lc);
		ple.start();
		FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
		fileAppender.setFile(file);
		fileAppender.setEncoder(ple);
		fileAppender.setAppend(false);
		fileAppender.setContext(lc);
		fileAppender.start();
		Logger logger = (Logger) LoggerFactory.getLogger(string);
		logger.detachAndStopAllAppenders(); // detach console (doesn't seem to work)
		logger.addAppender(fileAppender); // attach file appender
		assignLogLevel(logger); // set log level as specified in the config
		logger.setAdditive(true); /* set to true if root should log too */

		return logger;
	}

	public static void assignLogLevel(Logger log) {
		Level l=null;
		String configLevel = SNConfig.getLogLevel(); // get level as specified in the SN config
		if(configLevel.equals("d")){
			l=Level.DEBUG;
		}
		else if(configLevel.equals("i")){
			l=Level.INFO;
		}
		else if(configLevel.equals("w")){
			l=Level.WARN;
		}
		else if(configLevel.equals("t")){
			l=Level.TRACE;
		}
		else if(configLevel.equals("e")){
			l=Level.ERROR;
		}

		log.setLevel(l);

	}

	public static void setLogLevel(Level level) {
		logger.setLevel(level);

	}

	// create the logger only onetime
	public static Logger getOrCreateLogger(String string, String file){
		if(logger == null) { // first time, create it
			logger = createLogger(string,file);
			logger.info("logger created: {}", file);
		}

		return logger;
	}
}
