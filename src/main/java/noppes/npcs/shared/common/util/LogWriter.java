package noppes.npcs.shared.common.util;

import noppes.npcs.shared.SharedReferences;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class LogWriter {
	private final static String name = SharedReferences.modid();
	private final static Logger logger = Logger.getLogger(name);
	private final static SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm:ss");
	private static Handler handler;
	static {
		try {
			File dir = new File("logs");
			if(!dir.exists())
				dir.mkdir();
			File file = new File(dir, name + "-latest.log");
			File lock = new File(dir, name + "-latest.log.lck");
			File file1 = new File(dir, name + "-1.log");
			File file2 = new File(dir, name + "-2.log");
			File file3 = new File(dir, name + "-3.log");
			
			if(lock.exists())
				lock.delete();
			
			if(file3.exists())
				file3.delete();
			if(file2.exists())
				file2.renameTo(file3);
			if(file1.exists())
				file1.renameTo(file2);
			if(file.exists())
				file.renameTo(file1);
			
			handler = new StreamHandler(new FileOutputStream(file), new Formatter() {
				@Override
				public String format(LogRecord record) {
					StackTraceElement element = Thread.currentThread().getStackTrace()[8];
					String line = "[" + element.getClassName() + ":" + element.getLineNumber() + "] ";
					String time = "[" + dateformat.format(new Date(record.getMillis())) + "][" + record.getLevel() + "/" + name + "]" + line;
					if(record.getThrown() != null){
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						record.getThrown().printStackTrace(pw);
						return time + sw.toString();
					}
					return time + record.getMessage() + System.getProperty("line.separator");
				}
			});
			handler.setLevel(Level.ALL);
			logger.addHandler(handler);
			logger.setUseParentHandlers(false);
			Handler consoleHandler = new ConsoleHandler();
			consoleHandler.setFormatter(handler.getFormatter());
			consoleHandler.setLevel(Level.ALL);
			logger.addHandler(consoleHandler);
			
			logger.setLevel(Level.ALL);
			info(new Date().toString());
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void info(Object msg) {
		logger.log(Level.FINE, String.valueOf(msg));
		handler.flush();
	}

	public static void warn(Object msg) {
		logger.log(Level.WARNING, String.valueOf(msg));
		handler.flush();
	}

	public static void error(Object msg) {
		logger.log(Level.SEVERE, String.valueOf(msg));
		handler.flush();
	}

	public static void error(Object msg, Throwable e) {
		logger.log(Level.SEVERE, String.valueOf(msg));
		logger.log(Level.SEVERE, e.getMessage(), e);
		handler.flush();
	}

	public static void except(Throwable e) {
		logger.log(Level.SEVERE, e.getMessage(), e);
		handler.flush();
	}

	public static void debug(Object msg) {
		if(!SharedReferences.VerboseDebug())
			return;
		logger.log(Level.INFO, String.valueOf(msg));
		handler.flush();
	}
}
