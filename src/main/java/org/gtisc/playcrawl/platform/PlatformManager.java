/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gtisc.playcrawl.platform;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.gtisc.playcrawl.error.UnexpectedError;
import org.gtisc.playcrawl.util.Finder;

/**
 *
 * @author meng
 */
public class PlatformManager {

    private static final int MAJOR_TICK = 10000;
    private static final int MINOR_TICK = 100;

    private List<Platform> meta;

    public PlatformManager() {
        meta = new LinkedList<>();

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Map<String, String>>>() {
        }.getType();

        try (Reader reader = new FileReader(Finder.getPlatformMeta().toFile())) {
            Map<String, Map<String, String>> data = gson.fromJson(reader, type);
            for (String key : data.keySet()) {
                Map<String, String> dict = data.get(key);

                int min = v2i(dict.get("min"));
                int max = v2i(dict.get("max"));

                Platform plat = new Platform();
                plat.setMax(max);
                plat.setMin(min);
                plat.setName(dict.get("name"));
                plat.setSdk(Integer.parseInt(key));

                meta.add(plat);
            }
        } catch (FileNotFoundException ex) {
            throw new UnexpectedError(ex);
        } catch (IOException ex) {
            throw new UnexpectedError(ex);
        }
    }

    private static int v2i(String version) {
        String[] token = version.split("\\.");

        return Integer.parseInt(token[0]) * MAJOR_TICK
                + Integer.parseInt(token[1]) * MINOR_TICK
                + (token[2].equals("x") ? (MINOR_TICK - 1) : Integer.parseInt(token[2]));
    }

    public Platform query(String version) {
        int num = v2i(version);

        for (Platform plat : meta) {
            if (num >= plat.getMin() && num <= plat.getMax()) {
                return plat;
            }
        }

        return null;
    }
}
