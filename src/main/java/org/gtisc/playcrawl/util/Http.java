/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gtisc.playcrawl.util;

import com.google.common.util.concurrent.RateLimiter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.gtisc.playcrawl.Config;
import org.gtisc.playcrawl.error.HttpException;

/**
 *
 * @author meng
 */
public class Http {

    public static MediaType CONTENT_TYPE_PROTOBUF
            = MediaType.parse("application/x-protobuffer");

    private final OkHttpClient client;
    private final RateLimiter limiter;

    public Http(double rps) {
        limiter = RateLimiter.create(rps);

        client = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .readTimeout(Config.HTTP_TIMEOUT, TimeUnit.SECONDS)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String string, SSLSession ssls) {
                        return true;
                    }
                })
                .build();
    }

    public byte[] executeForBytes(Request request) throws HttpException {
        limiter.acquire();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new HttpException(response.code() + " " + response.message());
            }

            return response.body().bytes();
        } catch (IOException ex) {
            throw new HttpException(ex);
        }
    }

    public String executeForString(Request request) throws HttpException {
        limiter.acquire();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new HttpException(response.code() + " " + response.message());
            }

            return response.body().string();
        } catch (IOException ex) {
            throw new HttpException(ex);
        }
    }

    public Map<String, String> executeForMap(Request request) throws HttpException {
        limiter.acquire();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new HttpException(response.code() + " " + response.message());
            }

            Map<String, String> result = new HashMap<>();

            String string = response.body().string();
            String[] lines = string.split("\n");
            Pattern pattern = Pattern.compile("^([^=]+)=(.+)$");
            for (String line : lines) {
                Matcher m = pattern.matcher(line);
                if (m.matches()) {
                    result.put(m.group(1), m.group(2));
                }
            }

            return result;
        } catch (IOException ex) {
            throw new HttpException(ex);
        }
    }

    public void executeForDownload(Request request, Path dest) throws HttpException {
        limiter.acquire();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new HttpException(response.code() + " " + response.message());
            }

            Files.copy(response.body().byteStream(), dest);
        } catch (IOException ex) {
            throw new HttpException(ex);
        }
    }

    public static HttpUrl buildUrl(String base, Map<String, String> params) {
        HttpUrl url = HttpUrl.parse(base);

        HttpUrl.Builder builder = url.newBuilder();
        for (Map.Entry<String, String> param : params.entrySet()) {
            builder.addEncodedQueryParameter(param.getKey(), param.getValue());
        }

        return builder.build();
    }

    public static Map<String, String> parseUrl(String link) {
        HttpUrl url = HttpUrl.parse(link);

        Map<String, String> result = new HashMap<>();
        for (int i = 0, size = url.querySize(); i < size; i++) {
            result.put(url.queryParameterName(i), url.queryParameterValue(i));
        }

        return result;
    }
}
