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

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "Tinder_java_ai_bot"; //TODO: добавь имя бота в кавычках
    public static final String TELEGRAM_BOT_TOKEN = "7614138998:AAFbSqOAPhs2ZbShAIJ-m4w9uEfT066kzhc"; //TODO: добавь токен бота в кавычках
    public static final String OPEN_AI_TOKEN = "gpt:4dws6NYyD0BDK2ufp71ZJFkblB3TCC3tppbmX6OYmhSFydbM"; //TODO: добавь токен ChatGPT в кавычках
    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode dialogMode = null;
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
                        "Переписка от вашего имени \uD83D\uDE08","/message",//TODO: продумать логику для этого пункта
                        "Общение со знаменитостями \uD83D\uDD25","/date",//TODO: продумать логику для этого пункта
                        "Вопрос чату \uD83E\uDDE0","/gpt"
                );
                return;
            case "/gpt":
                dialogMode = DialogMode.GPT;
                sendPhotoMessage("gpt");
                sendTextMessage("Write your message to GPT");
                message = getMessageText();
                return;
            case "/profile":
                dialogMode = DialogMode.PROFILE;
                sendPhotoMessage("profile");
                sendTextMessage("Расскажи о себе для генерации профиля");
                message = getMessageText();
                return;
            case "/opener":
                dialogMode = DialogMode.OPENER;
                sendPhotoMessage("opener");
                sendTextMessage("Расскажи о девушке, чтобы генеарция была более подходящей");
                message = getMessageText();
                return;
            case "/message"://TODO: проверить логику для этого пункта
                dialogMode = DialogMode.MESSAGE;
                sendPhotoMessage("message");
                sendTextButtonsMessage("Пришлите в чат переписку",
                        "Следующее сообщение ","message_next",
                        "Пригласить на свидание","message_date");
                sendTextMessage("Пришли в чат свою переписку");
                message = getMessageText();
                return;
            case "/date"://TODO: проверить логику для этого пункта
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
            case PROFILE:
                prompt = loadPrompt("profile");
                answer =chatGPT.sendMessage(prompt, message);
                sendTextMessage(answer);
                break;
            case OPENER:
                prompt = loadPrompt("opener");
                answer =chatGPT.sendMessage(prompt, message);
                sendTextMessage(answer);
                break;
            case MESSAGE://TODO: продумать логику для этого пункта
                String query_message = getCallbackQueryButtonKey();
                if(query_message.startsWith("message_")){
                    sendPhotoMessage(query_message);
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
            case DATE://TODO: продумать логику для этого пункта
                String query = getCallbackQueryButtonKey();
                if(query.startsWith("date_")){
                    sendPhotoMessage(query);
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
