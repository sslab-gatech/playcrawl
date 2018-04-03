/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gtisc.playcrawl.account;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import org.gtisc.playcrawl.error.UnexpectedError;

/**
 *
 * @author meng
 */
public class AccountManager {

    private String filepath;
    private Account account;

    public AccountManager(String filepath) {
        this.filepath = filepath;

        Gson gson = new Gson();
        try (Reader reader = new FileReader(this.filepath)) {
            account = gson.fromJson(reader, Account.class);
        } catch (FileNotFoundException ex) {
            throw new UnexpectedError(ex);
        } catch (IOException ex) {
            throw new UnexpectedError(ex);
        }
    }

    public void save() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (Writer writer = new FileWriter(this.filepath)) {
            gson.toJson(account, writer);
        } catch (FileNotFoundException ex) {
            throw new UnexpectedError(ex);
        } catch (IOException ex) {
            throw new UnexpectedError(ex);
        }
    }

    public Account getAccount() {
        return account;
    }
}
