package ru.orangesoftware.financisto.export.qif;

import ru.orangesoftware.financisto.model.Category;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Denis Solonenko
 * Date: 2/16/11 10:08 PM
 */
public class QifCategory {

    public static final String SEPARATOR = ":";

    public String name;
    public boolean isIncome;

    public static QifCategory fromCategory(Category c) {
        QifCategory qifCategory = new QifCategory();
        qifCategory.name = buildName(c);
        qifCategory.isIncome = c.isIncome();
        return qifCategory;
    }

    private static String buildName(Category c) {
        StringBuilder sb = new StringBuilder();
        sb.append(c.title);
        for (Category p = c.parent; p != null; p = p.parent) {
            sb.insert(0, SEPARATOR);
            sb.insert(0, p.title);
        }
        return sb.toString();
    }

    public void writeTo(QifBufferedWriter qifWriter) throws IOException {
        qifWriter.write("N").write(name).newLine();
        qifWriter.write(isIncome ? "I" : "E").newLine();
        qifWriter.end();
    }

}