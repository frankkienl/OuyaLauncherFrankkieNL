package nl.frankkie.ouyalauncher.databaserows;

import android.appwidget.AppWidgetProviderInfo;

import nl.wotuu.database.DatabaseOpenHelper;
import nl.wotuu.database.DatabaseRow;
import nl.wotuu.database.annotations.DatabaseExclude;

/**
 * Created by FrankkieNL on 22-7-13.
 */
public class DatabaseAppWidget extends DatabaseRow {
    public int appWidgetId;
    public int posX = 0;
    public int posY = 0;
    public int width = 0;
    public int height = 0;

    @DatabaseExclude
    public AppWidgetProviderInfo info = null;

    public DatabaseAppWidget() {
        super(DatabaseOpenHelper.GetInstance().GetTableName(DatabaseAppWidget.class));
    }

    public DatabaseAppWidget(int id) {
        super(DatabaseOpenHelper.GetInstance().GetTableName(DatabaseAppWidget.class), id);
    }

}
