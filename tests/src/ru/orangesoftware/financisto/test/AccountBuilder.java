package ru.orangesoftware.financisto.test;

import ru.orangesoftware.financisto.db.DatabaseAdapter;
import ru.orangesoftware.financisto.model.Account;
import ru.orangesoftware.financisto.model.Currency;

import static junit.framework.Assert.assertNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Denis Solonenko
 * Date: 3/2/11 9:07 PM
 */
public class AccountBuilder {

    private final DatabaseAdapter db;
    private final Account a = new Account();

    public static Account createDefault(DatabaseAdapter db) {
        Currency c = CurrencyBuilder.createDefault(db);
        return withDb(db).title("Cash").currency(c).create();
    }

    public static AccountBuilder withDb(DatabaseAdapter db) {
        return new AccountBuilder(db);
    }

    private AccountBuilder(DatabaseAdapter db) {
        this.db = db;
    }

    public AccountBuilder title(String title) {
        a.title = title;
        return this;
    }

    public AccountBuilder currency(Currency c) {
        a.currency = c;
        return this;
    }

    public Account create() {
        db.em().saveAccount(a);
        return a;
    }

}
