/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gtisc.playcrawl.error;

/**
 *
 * @author xumeng
 */
public class UnexpectedError extends Error {

    public UnexpectedError() {
    }

    public UnexpectedError(String message) {
        super(message);
    }

    public UnexpectedError(String message, Throwable cause) {
        super(message, cause);
    }

    public UnexpectedError(Throwable cause) {
        super(cause);
    }

}
