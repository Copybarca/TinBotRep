package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
import com.plexpt.chatgpt.ChatGPT;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static com.javarush.telegram.Tokens.*;

public class TinderBoltApp extends MultiSessionTelegramBot {

    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode dialogMode = null;
    private UserInfo me;
    private int questionCount;
    private ArrayList<String> messageList = new ArrayList<>();
    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }








    @Override
    public void onUpdateEventReceived(Update update) {
        //TODO: основной функционал бота будем писать здесь
        String message = getMessageText();
        switch(message){
            case "/start":
                dialogMode = DialogMode.MAIN;
                sendPhotoMessage("main");
                sendTextMessage(loadMessage("main"));
                showMainMenu(
                        "Начало","/start",
                        "Генерация профиля \uD83D\uDE0E","/profile",
                        "Придумать открывашку \uD83E\uDD70","/opener",
                        "Переписка от вашего имени \uD83D\uDE08","/message",
                        "Общение со знаменитостями \uD83D\uDD25","/date",
                        "Вопрос чату \uD83E\uDDE0","/gpt"
                );
                return;
            case "/gpt":
                dialogMode = DialogMode.GPT;
                sendPhotoMessage("gpt");
                sendTextMessage("Write your message to GPT");
                message = getMessageText();
                return;
            case "/profile"://TODO: доработать ветку алгоритма
                dialogMode = DialogMode.PROFILE;
                sendPhotoMessage("profile");
                me = new UserInfo();
                questionCount = 0;
                sendTextMessage("Расскажи о себе для генерации профиля");
                sendTextMessage("Сколько тебе лет?");
                if(questionCount==0){
                    me.age = message;
                    questionCount = 1;
                }
                return;
            case "/opener"://TODO: доработать ветку алгоритма
                dialogMode = DialogMode.OPENER;
                sendPhotoMessage("opener");
                sendTextMessage("Расскажи о девушке, чтобы генеарция была более подходящей");
                message = getMessageText();
                return;
            case "/message":
                dialogMode = DialogMode.MESSAGE;
                sendPhotoMessage("message");
                sendTextButtonsMessage("Пришлите в чат переписку",
                        "Следующее сообщение ","message_next",
                        "Пригласить на свидание","message_date");
                sendTextMessage("Пришли в чат свою переписку");
                message = getMessageText();
                return;
            case "/date":
                dialogMode = DialogMode.DATE;
                sendPhotoMessage("date");
                sendTextButtonsMessage(loadMessage("date"),
                        "Ариана Гранде","date_grande",
                        "Райна Гослинг","date_gosling",
                        "Марго Робби","date_robbie",
                        "Зендая","date_zendaya",
                        "Мистер Хардли","date_hardly");
                return;
            default:
                break;
        }
        String prompt;
        String answer;

        switch(dialogMode){
            case GPT:
                prompt = loadPrompt("gpt");
                answer =chatGPT.sendMessage(prompt, message);
                sendTextMessage(answer);
                break;
            case PROFILE://TODO: доработать ветку алгоритма
                prompt = loadPrompt("profile");
                if(questionCount==1){
                    sendTextMessage("Ваше хобби?");
                    me.hobby = message;
                    questionCount = 2;
                    return;
                }if(questionCount==2){
                    sendTextMessage("Ваша профессия?");
                    me.occupation = message;
                    questionCount = 3;
                    return;

                }
                StringBuilder sb = new StringBuilder();
                sb.append(me.age).append(" ");
                sb.append(me.hobby).append(" ");
                sb.append(me.occupation);
                Message profMes = sendTextMessage("Генерирую профиль");
                answer =chatGPT.sendMessage(prompt, sb.toString());
                updateTextMessage(profMes,answer);
                break;
            case OPENER://TODO: доработать ветку алгоритма
                prompt = loadPrompt("opener");
                answer =chatGPT.sendMessage(prompt, message);
                sendTextMessage(answer);
                break;
            case MESSAGE:
                String query_message = getCallbackQueryButtonKey();
                if(query_message.startsWith("message_")){
                    prompt = loadPrompt(query_message);
                    chatGPT.setPrompt(prompt);
                    String userChatHistory = String.join("\n\n", messageList);
                    Message msg = sendTextMessage("Обработка данных");
                    answer = chatGPT.sendMessage(prompt,userChatHistory);
                    updateTextMessage(msg,answer);
                    return;
                }
                messageList.add(message);
                return;
            case DATE:
                String query = getCallbackQueryButtonKey();
                if(query.startsWith("date_")){
                    sendTextMessage(" Хороший выбор! \nТвоя задача пригласить партнёра на свидание за 5 сообщений");
                    prompt = loadPrompt(query);
                    chatGPT.setPrompt(prompt);
                    return;
                }
                answer =chatGPT.addMessage(message);
                sendTextMessage(answer);
                break;
            default:
                break;
        }

    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
