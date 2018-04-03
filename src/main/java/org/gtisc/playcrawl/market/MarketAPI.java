/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gtisc.playcrawl.market;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.gtisc.playcrawl.Config;
import org.gtisc.playcrawl.account.Account;
import org.gtisc.playcrawl.device.Device;
import org.gtisc.playcrawl.device.DeviceManager;
import org.gtisc.playcrawl.error.HttpException;
import org.gtisc.playcrawl.platform.Platform;
import org.gtisc.playcrawl.platform.PlatformManager;
import org.gtisc.playcrawl.proto.Googleplay.AndroidAppDeliveryData;
import org.gtisc.playcrawl.proto.Googleplay.BrowseResponse;
import org.gtisc.playcrawl.proto.Googleplay.BulkDetailsRequest;
import org.gtisc.playcrawl.proto.Googleplay.BulkDetailsResponse;
import org.gtisc.playcrawl.proto.Googleplay.BuyResponse;
import org.gtisc.playcrawl.proto.Googleplay.DetailsResponse;
import org.gtisc.playcrawl.proto.Googleplay.ListResponse;
import org.gtisc.playcrawl.proto.Googleplay.SearchResponse;
import org.gtisc.playcrawl.util.Http;
import org.gtisc.playcrawl.util.Proto;

/**
 *
 * @author meng
 */
public class MarketAPI {

    private final Account account;

    private final Device device;
    private final String version;
    private final Platform platform;

    private String auth;
    private long timestamp;

    private final Http http;

    public MarketAPI(Account account) {
        this.account = account;

        DeviceManager devManager = new DeviceManager();
        PlatformManager platManager = new PlatformManager();

        device = devManager.getDevice(account.getDevice());
        version = devManager.getVersion(account.getDevice(), account.getModel());
        platform = platManager.query(version);

        auth = null;
        timestamp = 0;

        http = new Http(Config.RPS_CHECKIN);
    }

    public void login() throws HttpException {
        Request request = new Request.Builder()
                .url(Config.URL_AUTH)
                .addHeader("User-Agent", "GoogleLoginService/"
                        + Config.GOOGLE_LOGIN_VERSION
                        + " (desktop BUILDN")
                .post(new FormBody.Builder()
                        .addEncoded("accountType", "HOSTED_OR_GOOGLE")
                        .addEncoded("Email", account.getUsername())
                        .addEncoded("Passwd", account.getPassword())
                        .addEncoded("androidId", Long.toHexString(account.getAid()))
                        .addEncoded("service", "androidmarket")
                        .addEncoded("has_permission", "1")
                        .addEncoded("source", "android")
                        .addEncoded("app", "com.android.vending")
                        .addEncoded("lang", Config.LANGUAGE)
                        .addEncoded("device_country", Config.COUNTRY)
                        .addEncoded("operatorCountry", Config.COUNTRY)
                        .addEncoded("sdk_version", Integer.toString(platform.getSdk()))
                        .build())
                .build();

        Map<String, String> result = http.executeForMap(request);
        auth = result.get("Auth");
        if (auth == null) {
            throw new HttpException("Cannot find 'auth' in response");
        }
        timestamp = System.currentTimeMillis();
    }

    public BrowseResponse browse(String cat, String ctr) throws HttpException {
        Map<String, String> params = new HashMap<>();
        params.put("c", "3");
        if (cat != null) {
            params.put("cat", cat);
        }
        if (ctr != null) {
            params.put("ctr", ctr);
        }

        Request request = prepareRequest()
                .url(Http.buildUrl(Config.URL_BROWSE, params))
                .get()
                .build();

        byte[] response = http.executeForBytes(request);
        return Proto.unwrap(response, BrowseResponse.class);
    }

    public ListResponse list(String cat, String ctr, Long num, Long offset, String ctntkn)
            throws HttpException {

        Map<String, String> params = new HashMap<>();
        params.put("c", "3");
        if (cat != null) {
            params.put("cat", cat);
        }
        if (ctr != null) {
            params.put("ctr", ctr);
        }
        if (num != null) {
            params.put("n", num.toString());
        }
        if (offset != null) {
            params.put("o", offset.toString());
        }
        if (ctntkn != null) {
            params.put("ctntkn", ctntkn);
        }

        Request request = prepareRequest()
                .url(Http.buildUrl(Config.URL_LIST, params))
                .get()
                .build();

        byte[] response = http.executeForBytes(request);
        return Proto.unwrap(response, ListResponse.class);
    }
    
    public SearchResponse search(String keyword, Long num, Long offset) throws HttpException {
        Map<String, String> params = new HashMap<>();
        params.put("c", "3");
        params.put("q", keyword);
        if (num != null) {
            params.put("n", num.toString());
        }
        if (offset != null) {
            params.put("o", offset.toString());
        }
        
        Request request = prepareRequest()
                .url(Http.buildUrl(Config.URL_SEARCH, params))
                .get()
                .build();

        byte[] response = http.executeForBytes(request);
        return Proto.unwrap(response, SearchResponse.class);
    }

    public DetailsResponse details(String pkgName) throws HttpException {
        Map<String, String> params = new HashMap<>();
        params.put("doc", pkgName);

        Request request = prepareRequest()
                .url(Http.buildUrl(Config.URL_DETAILS, params))
                .get()
                .build();

        byte[] response = http.executeForBytes(request);
        return Proto.unwrap(response, DetailsResponse.class);
    }

    public BulkDetailsResponse bulkDetails(List<String> pkgNames) throws HttpException {
        BulkDetailsRequest protoRequest = BulkDetailsRequest.newBuilder()
                .addAllDocid(pkgNames)
                .build();

        Request request = prepareRequest()
                .url(Config.URL_BULK_DETAILS)
                .addHeader("Content-Type", "application/x-protobuf")
                .post(RequestBody.create(Http.CONTENT_TYPE_PROTOBUF, protoRequest.toByteArray()))
                .build();

        byte[] response = http.executeForBytes(request);
        return Proto.unwrap(response, BulkDetailsResponse.class);
    }

    public void download(String pkgName, int verCode, Path dest) throws HttpException {
        Request request = prepareRequest()
                .url(Config.URL_PURCHASE)
                .post(new FormBody.Builder()
                        .addEncoded("ot", "1")
                        .addEncoded("doc", pkgName)
                        .addEncoded("vc", Integer.toString(verCode))
                        .build())
                .build();

        byte[] response = http.executeForBytes(request);
        BuyResponse buy = Proto.unwrap(response, BuyResponse.class);
        AndroidAppDeliveryData delivery = buy.getPurchaseStatusResponse().getAppDeliveryData();

        String url = delivery.getDownloadUrl();
        String cookie = delivery.getDownloadAuthCookie(0).getName()
                + "=" + delivery.getDownloadAuthCookie(0).getValue();

        String agent = "AndroidDownloadManager" + "/"
                + Config.GOOGLE_DOWNLOAD_VERSION + "("
                + "Linux" + "; "
                + "U" + "; "
                + "Android " + Config.GOOGLE_DOWNLOAD_VERSION + "; "
                + account.getDevice() + "Build" + "/" + account.getModel()
                + ")";

        request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", agent)
                .addHeader("Cookie", cookie)
                .get()
                .build();

        http.executeForDownload(request, dest);
    }

    private Request.Builder prepareRequest() throws HttpException {
        if (System.currentTimeMillis() - timestamp >= Config.AUTH_TIMEOUT) {
            login();
        }

        timestamp = System.currentTimeMillis();

        String agent = "Android-Finsky" + "/"
                + Config.GOOGLE_AGENT_NAME + " ("
                + "api=3" + ","
                + "versionCode=" + Config.GOOGLE_AGENT_CODE + ","
                + "sdk=" + platform.getSdk() + ","
                + "device=" + account.getDevice() + ","
                + "hardware=" + account.getDevice() + ","
                + "product=" + account.getDevice() + ","
                + "build=" + account.getModel() + ":user"
                + ")";

        return new Request.Builder()
                .addHeader("Accept-Language", Config.LANGUAGE)
                .addHeader("Authorization", "GoogleLogin auth=" + auth)
                .addHeader("X-DFE-Enabled-Experiments", "cl:billing.select_add_instrument_by_default")
                .addHeader("X-DFE-Unsupported-Experiments", "nocache:billing.use_charging_poller,market_emails,buyer_currency,prod_baseline,checkin.set_asset_paid_app_field,shekel_test,content_ratings,buyer_currency_in_app,nocache:encrypted_apk,recent_changes")
                .addHeader("X-DFE-Device-Id", Long.toHexString(account.getAid()))
                .addHeader("X-DFE-Client-Id", "am-android-google")
                .addHeader("X-DFE-Device-Config-Token", "1")
                .addHeader("X-DFE-SmallestScreenWidthDp", "320")
                .addHeader("X-DFE-Filter-Level", "3")
                .addHeader("User-Agent", agent)
                .addHeader("Host", "android.clients.google.com");
    }
}
