package nl.frankkie.ouyalauncher.databaserows;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import nl.wotuu.database.DatabaseOpenHelper;
import nl.wotuu.database.DatabaseRow;
import nl.wotuu.database.annotations.DatabaseExclude;
import proguard.annotation.KeepPublicClassMemberNames;

/**
 * Created by FrankkieNL on 16-7-13.
 */
@KeepPublicClassMemberNames
public class DatabaseAppInfo extends DatabaseRow {

    public String title;
    public String packageName;
    public String componentName;
    public boolean isFavorite;
    public String folder;
    public long lastOpened;
    public int timesOpened;
    public boolean isOUYA;
    public boolean isOUYAGame;

    @DatabaseExclude
    public Drawable icon;

    @DatabaseExclude
    public Intent intent;

    public DatabaseAppInfo() {
        super(DatabaseOpenHelper.GetInstance().GetTableName(DatabaseAppInfo.class));
    }

    public DatabaseAppInfo(int id) {
        super(DatabaseOpenHelper.GetInstance().GetTableName(DatabaseAppInfo.class), id);
    }

    /**
     * Creates the application intent based on a component name and various launch flags.
     *
     * @param className   the class name of the component representing the intent
     * @param launchFlags the launch flags
     */
    public void setActivity(ComponentName className, int launchFlags) {
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DatabaseAppInfo)) {
            return false;
        }

        DatabaseAppInfo that = (DatabaseAppInfo) o;
        return title.equals(that.title) &&
                intent.getComponent().getClassName().equals(
                        that.intent.getComponent().getClassName());
    }
}
