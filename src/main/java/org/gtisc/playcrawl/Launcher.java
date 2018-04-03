/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gtisc.playcrawl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.gtisc.playcrawl.account.Account;
import org.gtisc.playcrawl.account.AccountManager;
import org.gtisc.playcrawl.app.AppInfo;
import org.gtisc.playcrawl.checkin.CheckinManager;
import org.gtisc.playcrawl.error.DeviceException;
import org.gtisc.playcrawl.error.HttpException;
import org.gtisc.playcrawl.error.UnexpectedError;
import org.gtisc.playcrawl.market.MarketManager;
import org.gtisc.playcrawl.util.Log;

/**
 *
 * @author meng
 */
public class Launcher {

    public static void checkin(Account account, String devName, String modName) {
        CheckinManager manager = new CheckinManager(account);
        try {
            manager.checkin(devName, modName);
        } catch (DeviceException | HttpException ex) {
            Log.error(ex, "Device '%s-%s' checkin failed", devName, modName);
        }
    }

    public static void browse(Account account, String output) {
        MarketManager manager = new MarketManager(account);
        try {
            manager.login();
            Map<String, List<String>> result = manager.browse();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (Writer writer = new FileWriter(output)) {
                gson.toJson(result, writer);
            } catch (IOException ex) {
                throw new UnexpectedError(ex);
            }
        } catch (HttpException ex) {
            Log.error(ex, "Account '%s-%s' browse failed",
                    account.getUsername(), account.getAid());
        }
    }

    public static void details(Account account, String input, String output) {
        List<String> pkgNames = new LinkedList<>();
        try (Scanner reader = new Scanner(new File(input))) {
            while (reader.hasNext()) {
                pkgNames.add(reader.next());
            }
        } catch (FileNotFoundException ex) {
            throw new UnexpectedError(ex);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        List<AppInfo> apps;
        if (Files.exists(Paths.get(output))) {
            Type type = new TypeToken<List<AppInfo>>() {
            }.getType();

            try (Reader reader = new FileReader(output)) {
                apps = gson.fromJson(reader, type);
            } catch (FileNotFoundException ex) {
                throw new UnexpectedError(ex);
            } catch (IOException ex) {
                throw new UnexpectedError(ex);
            }
        } else {
            apps = new LinkedList<>();
        }

        MarketManager manager = new MarketManager(account);
        try {
            manager.login();
            List<AppInfo> result = manager.details(pkgNames, apps);

            try (Writer writer = new FileWriter(output)) {
                gson.toJson(result, writer);
            } catch (IOException ex) {
                throw new UnexpectedError(ex);
            }
        } catch (HttpException ex) {
            Log.error(ex, "Account '%s-%s' details failed",
                    account.getUsername(), account.getAid());
        }
    }

    public static void download(Account account, String input, String output) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<AppInfo>>() {
        }.getType();

        List<AppInfo> apps;
        try (Reader reader = new FileReader(input)) {
            apps = gson.fromJson(reader, type);
        } catch (FileNotFoundException ex) {
            throw new UnexpectedError(ex);
        } catch (IOException ex) {
            throw new UnexpectedError(ex);
        }

        MarketManager manager = new MarketManager(account);
        try {
            manager.login();
            manager.downloads(apps, Paths.get(output));
        } catch (HttpException ex) {
            Log.error(ex, "Account '%s-%s' download failed",
                    account.getUsername(), account.getAid());
        }
    }

    public static void probe(Account account, String input, String output) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<AppInfo>>() {
        }.getType();

        List<AppInfo> apps;
        try (Reader reader = new FileReader(input)) {
            apps = gson.fromJson(reader, type);
        } catch (FileNotFoundException ex) {
            throw new UnexpectedError(ex);
        } catch (IOException ex) {
            throw new UnexpectedError(ex);
        }

        MarketManager manager = new MarketManager(account);
        try {
            manager.login();
            manager.probes(apps, Paths.get(output));
        } catch (HttpException ex) {
            Log.error(ex, "Account '%s-%s' probe failed",
                    account.getUsername(), account.getAid());
        }
    }

    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("playcrawl");
        parser.addArgument("-c", "--config").required(true);

        Subparsers subs = parser.addSubparsers().dest("COMMAND");

        Subparser checkin = subs.addParser("checkin");
        checkin.addArgument("device");
        checkin.addArgument("model");

        Subparser browse = subs.addParser("browse");
        browse.addArgument("output");

        Subparser details = subs.addParser("details");
        details.addArgument("input");
        details.addArgument("output");

        Subparser download = subs.addParser("download");
        download.addArgument("input");
        download.addArgument("output");

        Subparser probe = subs.addParser("probe");
        probe.addArgument("input");
        probe.addArgument("output");

        try {
            Namespace result = parser.parseArgs(args);
            AccountManager manager = new AccountManager(result.getString("config"));
            Account account = manager.getAccount();

            switch (result.getString("COMMAND")) {
                case "checkin":
                    checkin(account,
                            result.getString("device"), result.getString("model"));

                    manager.save();
                    break;
                case "browse":
                    browse(account, result.getString("output"));
                    break;
                case "details":
                    details(account, result.getString("input"), result.getString("output"));
                    break;
                case "download":
                    download(account, result.getString("input"), result.getString("output"));
                    break;
                case "probe":
                    probe(account, result.getString("input"), result.getString("output"));
                    break;
                default:
                    parser.printUsage();
                    break;
            }
        } catch (ArgumentParserException ex) {
            parser.handleError(ex);
        }
    }
}
