/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gtisc.playcrawl.util;

import com.google.protobuf.InvalidProtocolBufferException;
import java.lang.reflect.InvocationTargetException;
import org.gtisc.playcrawl.error.HttpException;
import org.gtisc.playcrawl.error.UnexpectedError;
import org.gtisc.playcrawl.proto.Googleplay;

/**
 *
 * @author meng
 */
public class Proto {

    public static <T> T unwrap(byte[] bytes, Class<T> type) throws HttpException {
        try {
            Googleplay.ResponseWrapper wrapper;
            try {
                wrapper = Googleplay.ResponseWrapper.parseFrom(bytes);
            } catch (InvalidProtocolBufferException ex) {
                throw new HttpException(ex);
            }

            Googleplay.Payload payload = wrapper.getPayload();
            return (T) Googleplay.Payload.class
                    .getMethod("get" + type.getSimpleName())
                    .invoke(payload);
        } catch (HttpException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new UnexpectedError(ex);
        }
    }
}
