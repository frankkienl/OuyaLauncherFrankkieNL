package nl.frankkie.ouyalauncher.databaserows;

import android.graphics.drawable.Drawable;

import nl.frankkie.ouyalauncher.IGridItem;
import nl.wotuu.database.DatabaseOpenHelper;
import nl.wotuu.database.DatabaseRow;
import nl.wotuu.database.annotations.DatabaseExclude;

/**
 * Created by FrankkieNL on 17-7-13.
 */
public class DatabaseFolder extends DatabaseRow implements IGridItem {

    public DatabaseFolder() {
        super(DatabaseOpenHelper.GetInstance().GetTableName(DatabaseFolder.class));
    }

    public DatabaseFolder(int id) {
        super(DatabaseOpenHelper.GetInstance().GetTableName(DatabaseFolder.class), id);
    }

    public String title;
    @DatabaseExclude
    public Drawable icon;

    @Override
    public String getTitle(){
        return title;
    }

    @Override
    public Drawable getImage(){
        return icon;
    }

    @Override
    public boolean isOUYA() {
        return true;
    }

    @Override
    public boolean isOUYAGame() {
        return true;
    }

    @Override
    public boolean isFavorite() {
        return true;
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    @Override
    public boolean isInFolder() {
        return false;
    }

    @Override
    public String folderName() {
        return title;
    }
}
