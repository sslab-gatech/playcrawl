/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gtisc.playcrawl.device;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import org.gtisc.playcrawl.error.UnexpectedError;
import org.gtisc.playcrawl.util.Finder;

/**
 *
 * @author meng
 */
public class DeviceManager {

    private Map<String, Map<String, String>> meta;
    private Map<String, Device> devs;

    public DeviceManager() {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Map<String, String>>>() {
        }.getType();

        try (Reader reader = new FileReader(Finder.getDeviceMeta().toFile())) {
            meta = gson.fromJson(reader, type);
        } catch (FileNotFoundException ex) {
            throw new UnexpectedError(ex);
        } catch (IOException ex) {
            throw new UnexpectedError(ex);
        }

        devs = new HashMap<>();
        for (String key : meta.keySet()) {
            try (Reader reader = new FileReader(Finder.getDevice(key).toFile())) {
                Device device = gson.fromJson(reader, Device.class);
                devs.put(key, device);
            } catch (FileNotFoundException ex) {
                throw new UnexpectedError(ex);
            } catch (IOException ex) {
                throw new UnexpectedError(ex);
            }
        }
    }

    public Device getDevice(String device) {
        return devs.get(device);
    }

    public String getVersion(String device, String model) {
        return meta.get(device).get(model);
    }
}
