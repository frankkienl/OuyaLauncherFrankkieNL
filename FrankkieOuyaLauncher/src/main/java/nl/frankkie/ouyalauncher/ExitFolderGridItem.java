package nl.frankkie.ouyalauncher;

import android.graphics.drawable.Drawable;

/**
 * Created by FrankkieNL on 19-7-13.
 */
public class ExitFolderGridItem implements IGridItem {

    public Drawable icon;

    @Override
    public String getTitle() {
        return "Exit Folder";
    }

    @Override
    public Drawable getImage() {
        return icon;
    }

    @Override
    public boolean isOUYA() {
        return false;
    }

    @Override
    public boolean isOUYAGame() {
        return false;
    }

    @Override
    public boolean isFavorite() {
        return false;
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
        return getTitle();
    }

    @Override
    public void setFolderName(String name) {
        //not implemented
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
        return -1; //always first
    }
}
