/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gtisc.playcrawl.market;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gtisc.playcrawl.Config;
import org.gtisc.playcrawl.account.Account;
import org.gtisc.playcrawl.app.AppInfo;
import org.gtisc.playcrawl.error.HttpException;
import org.gtisc.playcrawl.error.UnexpectedError;
import org.gtisc.playcrawl.proto.Googleplay.AppDetails;
import org.gtisc.playcrawl.proto.Googleplay.BrowseLink;
import org.gtisc.playcrawl.proto.Googleplay.BrowseResponse;
import org.gtisc.playcrawl.proto.Googleplay.BulkDetailsEntry;
import org.gtisc.playcrawl.proto.Googleplay.BulkDetailsResponse;
import org.gtisc.playcrawl.proto.Googleplay.DocV2;
import org.gtisc.playcrawl.proto.Googleplay.ListResponse;
import org.gtisc.playcrawl.util.Http;
import org.gtisc.playcrawl.util.Log;
import org.gtisc.playcrawl.util.Split;

/**
 *
 * @author meng
 */
public class MarketManager {

    private final MarketAPI api;

    public MarketManager(Account account) {
        api = new MarketAPI(account);
    }

    public void login() throws HttpException {
        api.login();
        Log.info("Authenticated to market");
    }

    public Map<String, List<String>> browse() throws HttpException {
        Map<String, List<String>> result = new HashMap<>();
        List<String> cats = new LinkedList<>();

        BrowseResponse browseResponse = api.browse(null, null);
        for (BrowseLink catLink : browseResponse.getCategoryList()) {
            Map<String, String> params
                    = Http.parseUrl(Config.URL_ENDPOINT + catLink.getDataUrl());

            String cat = params.get("cat");
            if (cat == null) {
                continue;
            }
            cats.add(cat);
            System.out.println(cat);

            BrowseResponse subBrowseResponse = api.browse(cat, null);
            for (BrowseLink subCatLink : subBrowseResponse.getCategoryList()) {
                Map<String, String> subParams
                        = Http.parseUrl(Config.URL_ENDPOINT + subCatLink.getDataUrl());

                String subCat = subParams.get("cat");
                if (subCat == null) {
                    continue;
                }

                cats.add(subCat);
                System.out.println(subCat);
            }
        }

        for (String cat : cats) {
            List<String> ctrs = new LinkedList<>();

            ListResponse listResponse = api.list(cat, null, null, null, null);
            for (DocV2 doc : listResponse.getDocList()) {
                ctrs.add(doc.getDocid());
            }

            result.put(cat, ctrs);
        }

        return result;
    }

    public List<AppInfo> details(List<String> pkgNames, List<AppInfo> apps) throws HttpException {
        Set<String> existing = new HashSet<>();
        for (AppInfo app : apps) {
            existing.add(app.getPkgName());
        }

        List<String> newPkgs = new LinkedList<>();
        for (String name : pkgNames) {
            if (!existing.contains(name)) {
                newPkgs.add(name);
            }
        }

        Log.info("%d new packages to query", newPkgs.size());

        int failure = 0;

        List<String>[] splits = Split.splitByBinsize(newPkgs, Config.LIMIT_DETAILS);
        for (List<String> split : splits) {
            BulkDetailsResponse response = null;

            int retry = 0;
            boolean success = false;

            while (!success) {
                try {
                    response = api.bulkDetails(split);
                    success = true;
                    failure = 0;
                } catch (HttpException ex) {
                    if (++retry >= Config.RETRY_TIME) {
                        if (++failure >= Config.LIMIT_FAILURE) {
                            Log.info("Too many failures, exit details");
                            return apps;
                        }
                        Log.info("Retried too many times, give up: %s", ex.getMessage());
                        success = true;
                    } else {
                        try {
                            Thread.sleep(Config.RETRY_COOLDOWN);
                        } catch (InterruptedException ex1) {
                            throw new UnexpectedError(ex1);
                        }
                    }
                }
            }

            if (response == null) {
                continue;
            }

            for (BulkDetailsEntry entry : response.getEntryList()) {
                if (!entry.hasDoc()) {
                    continue;
                }

                DocV2 doc = entry.getDoc();
                AppDetails details = doc.getDetails().getAppDetails();

                AppInfo info = new AppInfo();
                info.setPkgName(details.getPackageName());
                info.setVerCode(details.getVersionCode());
                info.setSize(details.getInstallationSize());
                info.setUpdate(details.getUploadDate());
                info.setDownloads(details.getNumDownloads());
                info.setTitle(doc.getTitle());
                info.setDeveloper(doc.getCreator());
                info.setDescriptionHtml(doc.getDescriptionHtml());

                apps.add(info);
            }

            Log.info("%d details retrieved", split.size());
        }

        return apps;
    }

    public boolean download(String pkgName, int verCode, Path folder) {
        String filename = pkgName + "-" + verCode + ".apk";
        Path dest = folder.resolve(filename);

        if (Files.exists(dest)) {
            Log.info("%s-%d ignored", pkgName, verCode);
            return true;
        }

        int retry = 0;

        while (true) {
            try {
                api.download(pkgName, verCode, dest);
                Log.info("%s-%d succeed", pkgName, verCode);
                return true;
            } catch (HttpException ex) {
                if (++retry >= Config.RETRY_TIME) {
                    Log.info("%s-%d failure", pkgName, verCode);
                    Log.info("Retried too many times, give up: %s", ex.getMessage());
                    return false;
                } else {
                    try {
                        Thread.sleep(Config.RETRY_COOLDOWN);
                    } catch (InterruptedException ex1) {
                        throw new UnexpectedError(ex1);
                    }
                }
            }
        }
    }

    public void probe(String pkgName, int verCode, Path folder) {
        if (verCode > Config.LIMIT_VERSION) {
            Log.info("%s-%d ignored", pkgName, verCode);
            return;
        }

        for (int i = verCode; i > 0; i--) {
            if (download(pkgName, i, folder)) {
                Log.info("%s-%d found", pkgName, i);
            } else {
                Log.info("%s-%d not found", pkgName, i);
            }
        }
    }

    public void downloads(List<AppInfo> apps, Path folder) {
        int failure = 0;

        for (AppInfo app : apps) {
            if (download(app.getPkgName(), app.getVerCode(), folder)) {
                failure = 0;
            } else {
                if (++failure >= Config.LIMIT_FAILURE) {
                    Log.info("Too many failures, exit download");
                    return;
                }
            }
        }
    }

    public void probes(List<AppInfo> apps, Path folder) {
        for (AppInfo app : apps) {
            probe(app.getPkgName(), app.getVerCode(), folder);
        }
    }
}
