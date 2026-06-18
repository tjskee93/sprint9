package ru.yandex.practicum.mybank.controller;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.mybank.client.FrontClient;
import ru.yandex.practicum.mybank.error.ClientException;
import ru.yandex.practicum.mybank.model.CashAction;
import ru.yandex.practicum.mybank.model.dto.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Контроллер main.html.
 *
 * Используемая модель для main.html:
 *      model.addAttribute("name", name);
 *      model.addAttribute("birthdate", birthdate.format(DateTimeFormatter.ISO_DATE));
 *      model.addAttribute("sum", sum);
 *      model.addAttribute("accounts", accounts);
 *      model.addAttribute("errors", errors);
 *      model.addAttribute("info", info);
 *
 * Поля модели:
 *      name - Фамилия Имя текущего пользователя, String (обязательное)
 *      birthdate - дата рождения текущего пользователя, String в формате 'YYYY-MM-DD' (обязательное)
 *      sum - сумма на счету текущего пользователя, Integer (обязательное)
 *      accounts - список аккаунтов, которым можно перевести деньги, List<AccountDto> (обязательное)
 *      errors - список ошибок после выполнения действий, List<String> (не обязательное)
 *      info - строка успешности после выполнения действия, String (не обязательное)
 *
 * С примерами использования можно ознакомиться в тестовом классе заглушке AccountStub
 */
@Controller
public class MainController {

    FrontClient frontClient;

    public MainController(FrontClient frontClient) {
        this.frontClient = frontClient;
    }

    /**
     * GET /.
     * Редирект на GET /account
     */
    @GetMapping
    public String index() {
        return "redirect:/account";
    }

    /**
     * GET /account.
     * Что нужно сделать:
     * 1. Сходить в сервис accounts через Gateway API для получения данных аккаунта по REST
     * 2. Заполнить модель main.html полученными из ответа данными
     * 3. Текущего пользователя можно получить из контекста Security
     */
    @GetMapping("/account")
    public String getAccount(Model model,
                             @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient) {
        // TODO: Заменить на то, что описано в комментарии к методу
        fillModel(model, token(authorizedClient), null, null);

        return "main";
    }

    /**
     * POST /account.
     * Что нужно сделать:
     * 1. Сходить в сервис accounts через Gateway API для изменения данных текущего пользователя по REST
     * 2. Заполнить модель main.html полученными из ответа данными
     * 3. Текущего пользователя можно получить из контекста Security
     *
     * Изменяемые данные:
     * 1. name - Фамилия Имя
     * 2. birthdate - дата рождения в формате YYYY-DD-MM
     */
    @PostMapping("/account")
    public String editAccount(
            Model model,
            @RequestParam("name") String name,
            @RequestParam("birthdate") LocalDate birthdate,
            @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient
    ) {
        // TODO: Заменить на то, что описано в комментарии к методу
        String token = token(authorizedClient);
        try {
            String[] parts = name.trim().split("\\s+", 2);
            frontClient.updateAccount(token, new AccountUpdateDTO(parts[1], parts[0], birthdate));
            fillModel(model, token(authorizedClient), null, "Data is saved");
        } catch(ClientException exception) {
            fillModel(model, token, exception.getErrors(), null);
        }
        return "main";
    }

    /**
     * POST /cash.
     * Что нужно сделать:
     * 1. Сходить в сервис cash через Gateway API для снятия/пополнения счета текущего аккаунта по REST
     * 2. Заполнить модель main.html полученными из ответа данными
     * 3. Текущего пользователя можно получить из контекста Security
     *
     * Параметры:
     * 1. value - сумма списания
     * 2. action - GET (снять), PUT (пополнить)
     */
    @PostMapping("/cash")
    public String editCash(
            Model model,
            @RequestParam("value") int value,
            @RequestParam("action") CashAction action,
            @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient
            ) {
        // TODO: Заменить на то, что описано в комментарии к методу
        String token = token(authorizedClient);
        try {
            String result = frontClient.cash(token, new CashDTO(value, action));
            fillModel(model, token, null, result);
        } catch (ClientException exception) {
            fillModel(model, token, exception.getErrors(), null);
        }

        return "main";
    }

    /**
     * POST /transfer.
     * Что нужно сделать:
     * 1. Сходить в сервис accounts через Gateway API для перевода со счета текущего аккаунта на счет другого аккаунта по REST
     * 2. Заполнить модель main.html полученными из ответа данными
     * 3. Текущего пользователя можно получить из контекста Security
     *
     * Параметры:
     * 1. value - сумма списания
     * 2. login - логин пользователя получателя
     */
    @PostMapping("/transfer")
    public String transfer(
            Model model,
            @RequestParam("value") int value,
            @RequestParam("login") String login,
            @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient
    ) {
        String token = token(authorizedClient);
        try {
            String result = frontClient.transfer(token, new TransferDTO(login, value));
            fillModel(model, token, null, result);
        } catch (ClientException exception) {
            fillModel(model, token, exception.getErrors(), null);
        }

        return "main";
    }

    private void fillModel(Model model, String token, List<String> errors, String info) {
        AccountPageDTO page = frontClient.loadPage(token);
        model.addAttribute("name", page.account().firstName() + " " + page.account().lastName());
        model.addAttribute("birthdate", page.account().birth_date().format(DateTimeFormatter.ISO_DATE));
        model.addAttribute("sum", page.account().balance());
        model.addAttribute("accounts", page.accounts());
        model.addAttribute("errors", errors);
        model.addAttribute("info", info);
    }

    private String token(OAuth2AuthorizedClient authorizedClient) {
        return authorizedClient.getAccessToken().getTokenValue();
    }
}
