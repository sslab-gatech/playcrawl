/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gtisc.playcrawl.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.gtisc.playcrawl.error.UnexpectedError;

/**
 *
 * @author xumeng
 */
public class Log {

    private static final String FORMAT
            = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %5$s%6$s%n";

    private static final String LOGNAME = "log";

    private static final Logger LOGGER = init();

    private static Logger init() {
        System.getProperties()
                .setProperty("java.util.logging.SimpleFormatter.format", FORMAT);

        Logger logger = Logger.getLogger(LOGNAME);

        String path = System.getProperty("log");
        if (path == null) {
            Logger.getGlobal().log(Level.INFO,
                    "LOG not specified, defaulting to console");

            return logger;
        }

        logger.setUseParentHandlers(false);

        String filename = LOGNAME + "." + System.currentTimeMillis();
        Path filepath = Paths.get(path, filename);

        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }

        try {
            Handler redir = new FileHandler(filepath.toString(), false);
            redir.setFormatter(new SimpleFormatter());
            logger.addHandler(redir);
        } catch (IOException | SecurityException ex) {
            throw new UnexpectedError(ex);
        }

        Path linkpath = Paths.get(path, LOGNAME + "." + "latest");
        try {
            Files.deleteIfExists(linkpath);
            Files.createSymbolicLink(linkpath, Paths.get(filename));
        } catch (IOException ex) {
            throw new UnexpectedError(ex);
        }

        return logger;
    }

    public static void info(String format, Object... objs) {
        LOGGER.log(Level.INFO, String.format(format, objs));
    }

    public static void error(Throwable ex, String format, Object... objs) {
        LOGGER.log(Level.SEVERE, String.format(format, objs), ex);
    }

    public static void error(Throwable ex) {
        LOGGER.log(Level.SEVERE, "", ex);
    }
}
