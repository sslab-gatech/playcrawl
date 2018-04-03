/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gtisc.playcrawl.checkin;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Map;
import java.util.Random;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.gtisc.playcrawl.Config;
import org.gtisc.playcrawl.account.Account;
import org.gtisc.playcrawl.device.Device;
import org.gtisc.playcrawl.device.DeviceManager;
import org.gtisc.playcrawl.error.DeviceException;
import org.gtisc.playcrawl.error.HttpException;
import org.gtisc.playcrawl.platform.Platform;
import org.gtisc.playcrawl.platform.PlatformManager;
import org.gtisc.playcrawl.proto.Googleplay.AndroidBuildProto;
import org.gtisc.playcrawl.proto.Googleplay.AndroidCheckinProto;
import org.gtisc.playcrawl.proto.Googleplay.AndroidCheckinRequest;
import org.gtisc.playcrawl.proto.Googleplay.AndroidCheckinResponse;
import org.gtisc.playcrawl.proto.Googleplay.DeviceConfigurationProto;
import org.gtisc.playcrawl.util.Http;
import org.gtisc.playcrawl.util.Log;

/**
 *
 * @author meng
 */
public class CheckinManager {

    private final Account account;
    private final Http http;

    public CheckinManager(Account account) {
        this.account = account;
        http = new Http(Config.RPS_CHECKIN);
    }

    public void checkin(String devName, String modName) throws DeviceException, HttpException {
        DeviceManager devManager = new DeviceManager();
        PlatformManager platManager = new PlatformManager();

        Device device = devManager.getDevice(devName);
        if (device == null) {
            throw new DeviceException("Device '" + devName + "' not found");
        }

        String version = devManager.getVersion(devName, modName);
        if (version == null) {
            throw new DeviceException("Model '" + modName + "' not found");
        }

        Platform platform = platManager.query(version);
        if (platform == null) {
            throw new DeviceException("Platform '" + version + "' not found");
        }

        /* step 1: authenticate google account */
        Request request = new Request.Builder()
                .url(Config.URL_AUTH)
                .addHeader("User-Agent", "GoogleLoginService/"
                        + Config.GOOGLE_LOGIN_VERSION
                        + " (desktop BUILDN")
                .post(new FormBody.Builder()
                        .addEncoded("accountType", "HOSTED_OR_GOOGLE")
                        .addEncoded("Email", account.getUsername())
                        .addEncoded("Passwd", account.getPassword())
                        .addEncoded("service", "ac2dm")
                        .addEncoded("has_permission", "1")
                        .addEncoded("source", "android")
                        .addEncoded("app", "com.google.android.gsf")
                        .addEncoded("lang", Config.LANGUAGE)
                        .addEncoded("sdk_version", Integer.toString(platform.getSdk()))
                        .build())
                .build();

        Map<String, String> result = http.executeForMap(request);
        String auth = result.get("Auth");
        if (auth == null) {
            throw new HttpException("Cannot find 'auth' in response");
        }

        Log.info("Authenticated to GSF: auth=%s", auth);

        /* step 2: get aid and token */
        AndroidCheckinRequest protoRequest = generateCheckingRequest(device,
                modName, version, platform);

        request = new Request.Builder()
                .url(Config.URL_CHECKIN)
                .addHeader("User-Agent", "Android-Checkin/2.0 (generic " + modName + "); gzip")
                .addHeader("Host", "android.clients.google.com")
                .addHeader("Content-Type", "application/x-protobuffer")
                .post(RequestBody.create(Http.CONTENT_TYPE_PROTOBUF, protoRequest.toByteArray()))
                .build();

        byte[] response = http.executeForBytes(request);

        AndroidCheckinResponse protoResponse;
        try {
            protoResponse = AndroidCheckinResponse.parseFrom(response);
        } catch (InvalidProtocolBufferException ex) {
            throw new HttpException(ex);
        }

        if (!protoResponse.hasAndroidId() || !protoResponse.hasSecurityToken()) {
            throw new HttpException("Cannot find 'aid' or 'token' in response");
        }

        long aid = protoResponse.getAndroidId();
        long token = protoResponse.getSecurityToken();

        Log.info("Device profile uploaded: aid=%d, token=%d", aid, token);

        /* step 3: associate aid with google account */
        AndroidCheckinRequest newProtoRequest = AndroidCheckinRequest.newBuilder(protoRequest)
                .setId(aid)
                .setSecurityToken(token)
                .addAccountCookie("[" + account.getUsername() + "]")
                .addAccountCookie(auth)
                .build();

        request = new Request.Builder()
                .url(Config.URL_CHECKIN)
                .addHeader("User-Agent", "Android-Checkin/2.0 (generic " + modName + "); gzip")
                .addHeader("Host", "android.clients.google.com")
                .addHeader("Content-Type", "application/x-protobuffer")
                .post(RequestBody.create(Http.CONTENT_TYPE_PROTOBUF, newProtoRequest.toByteArray()))
                .build();

        response = http.executeForBytes(request);

        try {
            protoResponse = AndroidCheckinResponse.parseFrom(response);
        } catch (InvalidProtocolBufferException ex) {
            throw new HttpException(ex);
        }

        if (!protoResponse.hasAndroidId() || !protoResponse.hasSecurityToken()) {
            throw new HttpException("Cannot find 'aid' or 'token' in response");
        }

        if (protoResponse.getAndroidId() != aid
                || protoResponse.getSecurityToken() != token) {

            throw new HttpException("'aid' and 'token' mismatch");
        }

        account.setAid(aid);
        account.setToken(token);
        account.setDevice(devName);
        account.setModel(modName);

        Log.info("Checkin successful");
    }

    private AndroidCheckinRequest generateCheckingRequest(Device device,
            String modName, String version, Platform platform) {

        AndroidBuildProto build = AndroidBuildProto.newBuilder()
                .setId(generateBuildId(device.getBuild(), modName, version))
                .setProduct(device.getBuild().getProduct())
                .setCarrier(device.getBuild().getCarrier())
                .setRadio(device.getBuild().getRadio())
                .setBootloader(device.getBuild().getBootloader())
                .setClient(device.getBuild().getClient())
                .setTimestamp(System.currentTimeMillis() / 1000)
                .setGoogleServices(Config.GOOGLE_SERVICE_VERSION)
                .setDevice(device.getBuild().getDevice())
                .setSdkVersion(platform.getSdk())
                .setModel(device.getBuild().getModel())
                .setManufacturer(device.getBuild().getManufacturer())
                .setBuildProduct(device.getBuild().getBuildProduct())
                .setOtaInstalled(device.getBuild().isOtaInstalled())
                .build();

        AndroidCheckinProto operator = AndroidCheckinProto.newBuilder()
                .setBuild(build)
                .setLastCheckinMsec(0)
                .setCellOperator(device.getOperator().getCellOperator())
                .setSimOperator(device.getOperator().getSimOperator())
                .setRoaming(device.getOperator().getRoaming())
                .setUserNumber(device.getOperator().getUserNumber())
                .build();

        DeviceConfigurationProto config = DeviceConfigurationProto.newBuilder()
                .setTouchScreen(device.getConfig().getTouchScreen())
                .setKeyboard(device.getConfig().getKeyboard())
                .setHasHardKeyboard(device.getConfig().isHasHardKeyboard())
                .setNavigation(device.getConfig().getNavigation())
                .setHasFiveWayNavigation(device.getConfig().isHasFiveWayNavigation())
                .setScreenLayout(device.getConfig().getScreenLayout())
                .setScreenDensity(device.getConfig().getScreenDensity())
                .setScreenHeight(device.getConfig().getScreenHeight())
                .setScreenWidth(device.getConfig().getScreenWidth())
                .addAllSystemSharedLibrary(device.getConfig().getSystemSharedLibrary())
                .addAllSystemAvailableFeature(device.getConfig().getSystemAvailableFeature())
                .addAllNativePlatform(device.getConfig().getNativePlatform())
                .addAllSystemSupportedLocale(device.getConfig().getSystemSupportedLocale())
                .setGlEsVersion(device.getConfig().getGlEsVersion())
                .addAllGlExtension(device.getConfig().getGlExtension())
                .build();

        return AndroidCheckinRequest.newBuilder()
                .setId(0)
                .setCheckin(operator)
                .addMacAddr(generateMacAddr())
                .addMacAddrType("wifi")
                .setDigest("1-da39a3ee5e6b4b0d3255bfef95601890afd80709")
                .setLocale(Config.LOCALE)
                .setTimeZone(Config.TIMEZONE)
                .setVersion(3)
                .setDeviceConfiguration(config)
                .setFragment(0)
                .build();
    }

    private String generateBuildId(Device.DeviceBuild build,
            String model, String version) {

        return build.getCarrier().toLowerCase() + "/"
                + build.getProduct() + "/"
                + build.getBuildProduct() + ":" + version + "/"
                + model + "/"
                + build.getSeq() + ":" + "user" + "/"
                + "release-keys";
    }

    private String generateMacAddr() {
        Random rand = new Random();

        String mac = "b407f9";
        for (int i = 0; i < 6; i++) {
            mac += Integer.toString(rand.nextInt(16), 16);
        }

        return mac;
    }
}
