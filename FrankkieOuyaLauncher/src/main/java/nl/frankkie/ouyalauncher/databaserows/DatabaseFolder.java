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

    public String title;
    @DatabaseExclude
    public Drawable icon;

    public DatabaseFolder() {
        super(DatabaseOpenHelper.GetInstance().GetTableName(DatabaseFolder.class));
    }

    public DatabaseFolder(int id) {
        super(DatabaseOpenHelper.GetInstance().GetTableName(DatabaseFolder.class), id);
    }

    public void setIcon(Drawable drawable){
        this.icon = drawable;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Drawable getImage() {
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
        return false; //folder kan geen favorite zijn
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
    public String getFolderName() {
        return title;
    }

    @Override
    public void setFolderName(String name){
        this.title = name;
    }

    @Override
    public int getGridWidth() {
        return 1;
    }

    @Override
    public int getGridHeight() {
        return 1;
    }

    @Override
    public int getGridPosX() {
        return 0;
    }

    @Override
    public int getGridPosY() {
        return 0;
    }

    @Override
    public int compareTo(IGridItem iGridItem) {
        if (iGridItem.isFolder()){
            return getFolderName().compareTo(iGridItem.getFolderName()); //compare by name
        } else {
            return -1; //always before non foldes
        }
    }
}
