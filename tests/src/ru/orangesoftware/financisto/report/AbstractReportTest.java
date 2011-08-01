package ru.orangesoftware.financisto.report;

import ru.orangesoftware.financisto.blotter.WhereFilter;
import ru.orangesoftware.financisto.db.AbstractDbTest;
import ru.orangesoftware.financisto.graph.GraphUnit;
import ru.orangesoftware.financisto.model.Account;
import ru.orangesoftware.financisto.model.Category;
import ru.orangesoftware.financisto.model.Currency;
import ru.orangesoftware.financisto.test.AccountBuilder;
import ru.orangesoftware.financisto.test.CategoryBuilder;
import ru.orangesoftware.financisto.utils.CurrencyCache;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Denis Solonenko
 * Date: 7/8/11 1:26 AM
 */
public abstract class AbstractReportTest extends AbstractDbTest {

    Account a1;
    Account a2;
    Report report;
    Map<String, Category> categories;
    WhereFilter filter = WhereFilter.empty();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        a1 = AccountBuilder.createDefault(db);
        a2 = AccountBuilder.createDefault(db);
        categories = CategoryBuilder.createDefaultHierarchy(db);
        report = createReport();
        CurrencyCache.initialize(em);
    }

    protected abstract Report createReport();

    List<GraphUnit> assertReportReturnsData() {
        ReportData data = report.getReport(db, filter);
        assertNotNull(data);
        List<GraphUnit> units = data.units;
        assertNotNull(units);
        return units;
    }

    void assertName(GraphUnit unit, String name) {
        assertEquals(name, unit.name);
    }

    void assertIncome(GraphUnit u, long amount) {
        assertEquals(amount, u.getIncomeExpense(a1.currency).income);
    }

    void assertExpense(GraphUnit u, long amount) {
        assertEquals(amount, u.getIncomeExpense(a1.currency).expense);
    }

    void assertIncome(GraphUnit u, Currency c, long amount) {
        assertEquals(amount, u.getIncomeExpense(c).income);
    }

    void assertExpense(GraphUnit u, Currency c, long amount) {
        assertEquals(amount, u.getIncomeExpense(c).expense);
    }


}