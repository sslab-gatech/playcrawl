/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gtisc.playcrawl;

/**
 *
 * @author meng
 */
public class Config {

    /* urls */
    public static final String URL_ENDPOINT = "https://android.clients.google.com/";
    public static final String URL_AUTH = URL_ENDPOINT + "auth";
    public static final String URL_CHECKIN = URL_ENDPOINT + "checkin";
    public static final String URL_BROWSE = URL_ENDPOINT + "fdfe/browse";
    public static final String URL_LIST = URL_ENDPOINT + "fdfe/list";
    public static final String URL_SEARCH = URL_ENDPOINT + "fdfe/search";
    public static final String URL_DETAILS = URL_ENDPOINT + "fdfe/details";
    public static final String URL_BULK_DETAILS = URL_ENDPOINT + "fdfe/bulkDetails";
    public static final String URL_PURCHASE = URL_ENDPOINT + "fdfe/purchase";

    /* timeouts */
    public static final int HTTP_TIMEOUT = 60;
    public static final long AUTH_TIMEOUT = 5 * 60 * 1000;

    /* rps */
    public static final double RPS_CHECKIN = 1.0 / 3.0;
    public static final double RPS_MARKET = 1.0 / 3.0;

    /* limits */
    public static final int LIMIT_DETAILS = 20;
    public static final int LIMIT_VERSION = 500;
    public static final int LIMIT_FAILURE = 5;

    /* retry */
    public static final int RETRY_COOLDOWN = 10 * 1000;
    public static final int RETRY_TIME = 3;

    /* google */
    public static final String GOOGLE_LOGIN_VERSION = "1.3";
    public static final String GOOGLE_DOWNLOAD_VERSION = "7.1";
    public static final String GOOGLE_AGENT_NAME = "7.0.12.H-all [0]";
    public static final String GOOGLE_AGENT_CODE = "80701200";
    public static final int GOOGLE_SERVICE_VERSION = 16;

    /* cats */
    public static final String CTR_TOP_FREE = "apps_topselling_free";
    public static final String CTR_TOP_NEW = "apps_topselling_new_free";

    /* misc */
    public static final String LANGUAGE = "en";
    public static final String COUNTRY = "us";
    public static final String LOCALE = "en_US";
    public static final String TIMEZONE = "America/New_York";
    public static final String SIGNATURE = "61ed377e85d386a8dfee6b864bd85b0bfaa5af81";
}
