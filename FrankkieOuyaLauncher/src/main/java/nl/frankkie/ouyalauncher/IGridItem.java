package nl.frankkie.ouyalauncher;

import android.graphics.drawable.Drawable;

/**
 * Created by FrankkieNL on 17-7-13.
 */
public interface IGridItem extends Comparable<IGridItem> {
    public String getTitle();
    public Drawable getImage();
    public boolean isOUYA();
    public boolean isOUYAGame();
    public boolean isFavorite();
    public boolean isFolder();
    public boolean isInFolder();
    public String getFolderName();
    public void setFolderName(String name);
    public int getGridWidth();
    public int getGridHeight();
    public int getGridPosX();
    public int getGridPosY();
}
