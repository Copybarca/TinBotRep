package com.javarush.telegram;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Tokens {
    public static final String TELEGRAM_BOT_NAME;
    public static final String TELEGRAM_BOT_TOKEN;
    public static final String OPEN_AI_TOKEN;
    static{
        Properties prop = new Properties();
        String token = "src/main/resources/config/tgBot2.config";
        try (FileInputStream fis = new FileInputStream(token)) {
            prop.load(fis);
        } catch (FileNotFoundException ex) {
            System.out.println("Tokens file missing");
        } catch (IOException ex) {
            System.out.println("Tokens missing");
        }
        TELEGRAM_BOT_NAME = prop.getProperty("tgBot2.name");
        TELEGRAM_BOT_TOKEN =prop.getProperty("tgBot2.token");


        token = "src/main/resources/config/gpt2.config";
        try (FileInputStream fis = new FileInputStream(token)) {
            prop.load(fis);
        } catch (FileNotFoundException ex) {
            System.out.println("Tokens file missing");
        } catch (IOException ex) {
            System.out.println("Tokens missing");
        }
        OPEN_AI_TOKEN=prop.getProperty("gpt2.token");
    }
}
