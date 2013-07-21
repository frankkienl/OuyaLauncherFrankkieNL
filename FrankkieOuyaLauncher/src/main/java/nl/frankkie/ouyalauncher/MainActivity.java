/*
 * Copyright (c) 2013. FrankkieNL
 */

package nl.frankkie.ouyalauncher;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.flurry.android.FlurryAgent;
import com.flurry.org.codehaus.jackson.map.JsonSerializableWithType;

import nl.frankkie.ouyalauncher.databaserows.DatabaseAppInfo;
import nl.frankkie.ouyalauncher.databaserows.DatabaseFolder;
import nl.wotuu.database.DatabaseOpenHelper;
import tv.ouya.console.api.OuyaController;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    public static int numberOfItemsPerRow = 5;
    LinearLayout table;
    View selectedItem;
    //    ListView gridView;
    //MyAdapter adapter;
    private static ArrayList<IGridItem> gridItems = new ArrayList<IGridItem>();
    private static ArrayList<IGridItem> filteredGridItems = new ArrayList<IGridItem>();
    private static ArrayList<AppInfo> mApplications = new ArrayList<AppInfo>();
    private static ArrayList<AppInfo> mFilteredApplications = new ArrayList<AppInfo>();
    //FILTERS
    public static final int APP_ALL = 0;
    public static final int APP_OUYA_GAMES_ONLY = 1;
    public static final int APP_OUYA_APPS_ONLY = 2;
    public static final int APP_OUYA_ONLY = 3; //APPS && GAMES
    public static final int APP_ANDROID_APPS_ONLY = 4;
    public static final int APP_FAVORITES_ONLY = 5;
    int appType = APP_ALL;
    public String currentFolder = "";
    ////
    Handler handler = new Handler();


    public void showAppInfo() {
//        View v = getCurrentFocus();
        if (selectedItem != null) {
            selectedItem = getCurrentFocus(); //via Y, not via Menu
            if (selectedItem == null) {
                return;
            }
            TextView tv_packagename = (TextView) ((ViewGroup) selectedItem).findViewById(R.id.item_packagename);
            String packagename = tv_packagename.getText().toString();
            showInstalledAppDetails(this, packagename);
            Util.logAppInfo(MainActivity.this, packagename);
        }
    }

    public void showFilters() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filters");
        String[] items = new String[]{"All Apps", "OUYA Games", "OUYA Apps", "OUYA Apps and Games", "Android Apps and Games", "Favorites"};
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                appType = i;
                filter();
                fillTable();
                Util.logFilterChange(MainActivity.this, i);
            }
        });
        builder.create().show();
    }

    public void filter() {
        filteredGridItems.clear();

        //Sort
        Collections.sort(gridItems);

        if (currentFolder != null && currentFolder.length() > 1) {
            for (IGridItem info : gridItems) {
                if (info instanceof ExitFolderGridItem) {
                    filteredGridItems.add(info);
                    continue;
                }
                if (!info.isInFolder()) {
                    continue;
                } else if (info.getFolderName().equals(currentFolder)) {
                    filteredGridItems.add(info);
                }
            }
            return;
        }

        for (IGridItem info : gridItems) {
            if (info instanceof ExitFolderGridItem) {
                continue;
            }
            if (appType == APP_ALL) {
                filteredGridItems.add(info);
                continue;
            }
            if (info.isFolder()) { //always add folders
                filteredGridItems.add(info);
                continue;
            }
            if (info.isInFolder()) {
                //don't add
                continue;
            }
            if (appType == APP_OUYA_ONLY) {
                if (info.isOUYA()) {
                    filteredGridItems.add(info);
                }
            } else if (appType == APP_ANDROID_APPS_ONLY) {
                if (!info.isOUYA()) {
                    filteredGridItems.add(info);
                }
            } else if (appType == APP_OUYA_GAMES_ONLY) {
                if (info.isOUYA() && info.isOUYAGame()) {
                    filteredGridItems.add(info);
                }
            } else if (appType == APP_OUYA_APPS_ONLY) {
                if (info.isOUYA() && !info.isOUYAGame()) {
                    filteredGridItems.add(info);
                }
            } else if (appType == APP_FAVORITES_ONLY) {
                if (info.isFavorite()) {
                    filteredGridItems.add(info);
                }
            }
        }
    }

    private static final String SCHEME = "package";
    private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";
    private static final String APP_PKG_NAME_22 = "pkg";
    private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
    private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

    public static void showInstalledAppDetails(Context context, String packageName) {
        Intent intent = new Intent();
        final int apiLevel = Build.VERSION.SDK_INT;
        if (apiLevel >= 9) { // above 2.3
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts(SCHEME, packageName, null);
            intent.setData(uri);
        } else { // below 2.3
            final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22
                    : APP_PKG_NAME_21);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(APP_DETAILS_PACKAGE_NAME,
                    APP_DETAILS_CLASS_NAME);
            intent.putExtra(appPkgName, packageName);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OuyaController.init(this);
        initUI();
        appType = getIntent().getIntExtra("type", APP_ALL);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                LoadApplicationsTask task = new LoadApplicationsTask();
                task.execute();
            }
        }, 10);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Dont consume DPAD
        int[] ignoreList = new int[]{OuyaController.BUTTON_DPAD_DOWN,
                OuyaController.BUTTON_DPAD_UP,
                OuyaController.BUTTON_DPAD_LEFT,
                OuyaController.BUTTON_DPAD_RIGHT,
                OuyaController.BUTTON_O,
                OuyaController.BUTTON_A
        };
        for (int i : ignoreList) {
            if (event.getKeyCode() == i) {
                return super.onKeyDown(keyCode, event); //let the OUYA take care of it.
            }
        }
        //Do consume U and Y
        if (event.getKeyCode() == OuyaController.BUTTON_U) {
            showAppInfo();
            return true;
        }
        if (event.getKeyCode() == OuyaController.BUTTON_Y) {
            showFilters();
            return true;
        }
        if (event.getKeyCode() == OuyaController.BUTTON_A) {
            pressedA();
            return true;
        }
        //check menu-key
        if (event.getKeyCode() == OuyaController.BUTTON_MENU || event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            pressedMenu();
            return true;
        }
        //Let the SDK take care of the rest
        boolean handled = OuyaController.onKeyDown(keyCode, event);
        return handled || super.onKeyDown(keyCode, event);
    }

    public void makeAppMenu(final DatabaseAppInfo info) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Menu");
        String[] items = new String[]{"Filters", "App Info",
                ((info.isFavorite()) ? "Remove from" : "Add to") + " Favorites",
                ((info.isInFolder()) ? "Remove from" : "Add to") + " Folder"
        };
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0: {
                        showFilters();
                        break;
                    }
                    case 1: {
                        showAppInfo();
                        break;
                    }
                    case 2: {
                        if (info.isFavorite()) {
                            removeFromFavorites();
                        } else {
                            addToFavorites();
                        }
                        break;
                    }
                    case 3: {
                        if (info.isInFolder()) {
                            removeFromFolder(info);
                        } else {
                            addToFolder(info);
                        }
                        break;
                    }
                }
            }
        });
        builder.create().show();
    }

    public void makeFolderMenu(final String folderName) {
        if (currentFolder != null && currentFolder.length() > 1) {
            return; //no menu on the Exit Folder, folder
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Menu");
        String[] items = new String[]{
                "Remove this Folder"
        };
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    //remove folder
                    removeFolder(folderName);
                }
            }
        });
        builder.create().show();
    }

    public void removeFolder(String folderName) {
        currentFolder = "";
        IGridItem toDelete = null;
        for (IGridItem item : gridItems) {
            //remove items
            if (item.isInFolder() && item.getFolderName().equals(folderName)) {
                item.setFolderName("");
            }
            if (item.isFolder() && item.getFolderName().equals(folderName)) {
                toDelete = item;
            }
        }
        gridItems.remove(toDelete); // fix ConcurrentModificationException
        DatabaseFolder folder = getDatabaseFolder(folderName);
        folder.OnDelete();
        filter();
        fillTable();
    }

    public void pressedMenu() {
        selectedItem = getCurrentFocus();
        String pak = ((TextView) selectedItem.findViewById(R.id.item_packagename)).getText().toString();
        String title = ((TextView) selectedItem.findViewById(R.id.item_text)).getText().toString();
        if (pak.equals("folder")) {
            makeFolderMenu(title);
        } else {
            final DatabaseAppInfo info = getDatabaseAppInfo(pak);
            makeAppMenu(info);
        }
    }

    public void addToFolder(final DatabaseAppInfo info) {
        //get foldername
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add to Folder");
        DatabaseOpenHelper helper = DatabaseOpenHelper.CreateInstance(this);
        Cursor cursor = helper.WriteableDatabase.rawQuery("SELECT id,title FROM folder", null);
        //cursor.moveToFirst();
        ArrayList<String> folderNames = new ArrayList<String>();
        folderNames.add("New Folder ...");
        while (cursor.moveToNext()) {
            String title = cursor.getString(1);
            folderNames.add(title);
        }
        cursor.close();
        final String[] folders = folderNames.toArray(new String[]{});
        builder.setItems(folders, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) { //create new
                    makeNewFolder(info);
                } else {
                    addToFolder(info, folders[i]);
                }
            }
        });
        builder.create().show();
    }

    public void makeNewFolder(final  DatabaseAppInfo info){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Folder Name");
        final EditText editText = new EditText(this);
        editText.setHint("folder name here");
        builder.setView(editText);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String folderName = editText.getText().toString();
                addToFolder(info, folderName);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //nothing just remove dialog
            }
        });
        builder.create().show();
    }

    public void addToFolder(DatabaseAppInfo info, String folderName) {
        info.setFolderName(folderName);
        info.OnUpdate();
        Toast.makeText(this, "Added to Folder", Toast.LENGTH_LONG).show();
        //folder check
        if (!folderExistsInGridItemsList(folderName)) {
            gridItems.add(getDatabaseFolder(folderName));
        }
        filter();
        fillTable();
    }

    public void removeFromFolder(DatabaseAppInfo info) {
        info.setFolderName("");
        info.OnUpdate();
        Toast.makeText(this, "Removed from Folder", Toast.LENGTH_LONG).show();
        filter();
        fillTable();
    }

    public void addToFavorites() {
        if (selectedItem != null) {
            selectedItem = getCurrentFocus(); // not via Menu
            if (selectedItem == null) {
                return;
            }
            String pak = ((TextView) selectedItem.findViewById(R.id.item_packagename)).getText().toString();
            DatabaseAppInfo info = getDatabaseAppInfo(pak);
            if (info != null) {
                info.setFavorite(true);
            }

            info.OnUpdate();

            //Util.addToFavorites(this, info);
            Util.logAddFavorite(this, info);
            Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show();
        }
        //Refresh
        filter();
        fillTable();
    }

    public void removeFromFavorites() {
        if (selectedItem != null) {
            selectedItem = getCurrentFocus(); // not via Menu
            if (selectedItem == null) {
                return;
            }
            String pak = ((TextView) selectedItem.findViewById(R.id.item_packagename)).getText().toString();
            DatabaseAppInfo info = getDatabaseAppInfo(pak);
            if (info != null) {
                info.setFavorite(false);
            }

            info.OnUpdate();
            //Util.removeFromFavorites(this, ((TextView) selectedItem.findViewById(R.id.item_packagename)).getText().toString());
            Util.logRemoveFavorite(this, info);
            Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
        }
        //Refresh
        filter();
        fillTable();
    }

    public void pressedA() {
//        startDiscover();
    }

    public void startDiscover() {
        Intent i = new Intent();
//        i.setClassName("tv.ouya.console","tv.ouya.console.launcher.store.adapter.DiscoverActivity");
        i.setClassName("tv.ouya.console", "tv.ouya.console.launcher.store.OldDiscoverActivity");
        try {
            startActivity(i);
        } catch (Exception e) {
            Log.e("FrankkieLauncher", "ERROR", e);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //Dont consume DPAD, and O
        int[] ignoreList = new int[]{OuyaController.BUTTON_DPAD_DOWN,
                OuyaController.BUTTON_DPAD_UP,
                OuyaController.BUTTON_DPAD_LEFT,
                OuyaController.BUTTON_DPAD_RIGHT,
                OuyaController.BUTTON_O,
                OuyaController.BUTTON_A
        };
        for (int i : ignoreList) {
            if (event.getKeyCode() == i) {
                return super.onKeyDown(keyCode, event); //let the OUYA take care of it.
            }
        }
        //Do consume U and Y
        if (event.getKeyCode() == OuyaController.BUTTON_U) {
            return true;
        }
        if (event.getKeyCode() == OuyaController.BUTTON_Y) {
            return true;
        }
        //Let the SDK take care of the rest
        boolean handled = OuyaController.onKeyUp(keyCode, event);
        return handled || super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        //Dont consume LS, RS, L2, R2 events
        //boolean handled = OuyaController.onGenericMotionEvent(event);
        //return handled || super.onGenericMotionEvent(event);
        return super.onGenericMotionEvent(event);
    }

    protected void initUI() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);
        //Util.setBackground(this);
        Util.setLogo(this);
        table = (LinearLayout) findViewById(R.id.table);
    }


    private void loadApplications() {
        PackageManager manager = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
        //Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

        if (apps != null) {
            final int count = apps.size();

            if (gridItems == null) {
                gridItems = new ArrayList<IGridItem>(count);
            }
            gridItems.clear();
            Runtime.getRuntime().gc();

            //inject folder exit
            ExitFolderGridItem exitFolderGridItem = new ExitFolderGridItem();
            exitFolderGridItem.icon = getResources().getDrawable(R.drawable.folder);
            gridItems.add(exitFolderGridItem);

            for (int i = 0; i < count; i++) {
                ResolveInfo rinfo = apps.get(i);
                DatabaseAppInfo databaseAppInfo = getDatabaseAppInfo(rinfo);
                gridItems.add(databaseAppInfo);
                //check for folders
                if (databaseAppInfo.isInFolder()) {
                    if (!folderExistsInGridItemsList(databaseAppInfo.getFolderName())) {
                        gridItems.add(getDatabaseFolder(databaseAppInfo.getFolderName()));
                    }
                }
            }
        }
    }

    public boolean folderExistsInGridItemsList(String folderName) {
        //Check if folder already exists
        for (IGridItem item : gridItems) {
            if (item.isFolder() && (item instanceof DatabaseFolder)) { //if (item instanceof DatabaseFolder){
                DatabaseFolder folder = (DatabaseFolder) item;
                if (folder.getFolderName().equals(folderName)) {
                    //OK !
                    return true;
                }
            }
        }
        return false;
    }

    public void checkIsOUYAAppByIntent(DatabaseAppInfo info) {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.addCategory("tv.ouya.intent.category.GAME");
        List<ResolveInfo> infos = getPackageManager().queryIntentActivities(mainIntent, 0);
        for (ResolveInfo ri : infos) {
            if (ri.activityInfo.applicationInfo.packageName.equals(info.packageName)) {
                info.setOUYA(true);
                info.setOUYAGame(true);
            }
        }
        //////
        Intent mainIntent2 = new Intent(Intent.ACTION_MAIN, null);
        mainIntent2.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent2.addCategory("tv.ouya.intent.category.APP");
        List<ResolveInfo> infos2 = getPackageManager().queryIntentActivities(mainIntent2, 0);
        for (ResolveInfo ri : infos2) {
            if (ri.activityInfo.applicationInfo.packageName.equals(info.packageName)) {
                info.setOUYA(true);
                info.setOUYAGame(false);
            }
        }
    }

    public boolean checkIsOUYAAppByIntent(AppInfo info) {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.addCategory("tv.ouya.intent.category.GAME");
        List<ResolveInfo> infos = getPackageManager().queryIntentActivities(mainIntent, 0);
        for (ResolveInfo ri : infos) {
            if (ri.activityInfo.applicationInfo.packageName.equals(info.packagename)) {
                info.isOUYA = true;
                info.isOUYAGame = true;
            }
        }
        //////
        Intent mainIntent2 = new Intent(Intent.ACTION_MAIN, null);
        mainIntent2.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent2.addCategory("tv.ouya.intent.category.APP");
        List<ResolveInfo> infos2 = getPackageManager().queryIntentActivities(mainIntent2, 0);
        for (ResolveInfo ri : infos2) {
            if (ri.activityInfo.applicationInfo.packageName.equals(info.packagename)) {
                info.isOUYA = true;
                info.isOUYAGame = false;
            }
        }
        return info.isOUYA;
    }

    /**
     * to memory intensive
     *
     * @param info
     * @return boolean
     * @deprecated
     */
    @Deprecated
    public boolean checkIsOUYAAppByIcon(AppInfo info) {
        String packageName = "";
        try {
            packageName = info.intent.getComponent().getPackageName();
            android.content.pm.ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(packageName, 0);
            Resources resources = getPackageManager().getResourcesForApplication(applicationInfo);
            int identifier3 = resources.getIdentifier(packageName + ":drawable/ouya_icon", "", "");
            if (identifier3 != 0) {
                return true;
            }
//            info.icon = getPackageManager().getResourcesForApplication(applicationInfo).getDrawable(identifier3);
            //Runtime.getRuntime().gc();
            //return true;
        } catch (Exception e) {
            //  Log.e("FRANKKIE_LAUNCHER", "ERROR", e);
        }
        return false;
    }

    public void fillTable() {
        //cachedFavorites = Util.getFavorites(this);
        table.removeAllViews(); //clear
        ViewGroup row = null;
        LayoutInflater inflater = getLayoutInflater();
        for (int i = 0; i < filteredGridItems.size(); i++) {
            if (i % numberOfItemsPerRow == 0) {
                if (row != null) {
                    table.addView(row);
                }
                row = (ViewGroup) inflater.inflate(R.layout.table_row, table, false);
            }
            View v = fillTable(filteredGridItems.get(i));
            row.addView(v);
            if (i == filteredGridItems.size() - 1) {
                //last row
                //Fill up !
                for (int j = i % numberOfItemsPerRow; j < numberOfItemsPerRow - 1; j++) {
                    View w = inflater.inflate(R.layout.grid_item_empty, table, false);
                    row.addView(w);
                }
                table.addView(row);
            }
        }
    }

    private Rect mOldBounds = new Rect();

    public View fillTable(final IGridItem info) {
        ViewGroup layout;
        LayoutInflater inflater = getLayoutInflater();
        layout = (ViewGroup) inflater.inflate(R.layout.grid_item, table, false);
        ////////
        TextView tv = (TextView) layout.findViewById(R.id.item_text);
        tv.setText(info.getTitle());
        TextView tv_packagename = (TextView) layout.findViewById(R.id.item_packagename);
        if (!info.isFolder()) {
            tv_packagename.setText(((DatabaseAppInfo) info).packageName);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = ((DatabaseAppInfo) info).intent;
                    try {
                        //Analytics
                        Util.logAppLaunch(MainActivity.this, (DatabaseAppInfo) info);
                        startActivity(i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            if (info instanceof ExitFolderGridItem) {
                tv_packagename.setText("folder");
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        currentFolder = "";
                        filter();
                        fillTable();
                    }
                });
            } else {
                tv_packagename.setText("folder");
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        currentFolder = info.getFolderName();
                        filter();
                        fillTable();
                    }
                });
            }
        }
        ImageView img = (ImageView) layout.findViewById(R.id.item_image);
        img.setImageDrawable(info.getImage());
        ///
        if (info.isFavorite()) {
            layout.findViewById(R.id.item_star).setVisibility(View.VISIBLE);
        }
        return layout;
    }

    public void getIconImageAndroidApps(AppInfo info) {
        Drawable icon = info.icon;
        //final Resources resources = getContext().getResources();
        int width = 180;//(int) resources.getDimension(android.R.dimen.app_icon_size);
        int height = 180;//(int) resources.getDimension(android.R.dimen.app_icon_size);

        final int iconWidth = icon.getIntrinsicWidth();
        final int iconHeight = icon.getIntrinsicHeight();

        if (icon instanceof PaintDrawable) {
            PaintDrawable painter = (PaintDrawable) icon;
            painter.setIntrinsicWidth(width);
            painter.setIntrinsicHeight(height);
        }

        if (width > 0 && height > 0 && (width < iconWidth || height < iconHeight)) {
            final float ratio = (float) iconWidth / iconHeight;

            if (iconWidth > iconHeight) {
                height = (int) (width / ratio);
            } else if (iconHeight > iconWidth) {
                width = (int) (height * ratio);
            }

            final Bitmap.Config c =
                    icon.getOpacity() != PixelFormat.OPAQUE ?
                            Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
            final Bitmap thumb = Bitmap.createBitmap(width, height, c);
            final Canvas canvas = new Canvas(thumb);
            canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG, 0));
            // Copy the old bounds to restore them later
            // If we were to do oldBounds = icon.getBounds(),
            // the call to setBounds() that follows would
            // change the same instance and we would lose the
            // old bounds
            mOldBounds.set(icon.getBounds());
            icon.setBounds(0, 0, width, height);
            icon.draw(canvas);
            icon.setBounds(mOldBounds);
            icon = info.icon = new BitmapDrawable(thumb);
            info.filtered = true;
        }
    }

    public void getIconImageAndroidApps(DatabaseAppInfo info) {
        Drawable icon = info.icon;
        //final Resources resources = getContext().getResources();
        int width = 180;//(int) resources.getDimension(android.R.dimen.app_icon_size);
        int height = 180;//(int) resources.getDimension(android.R.dimen.app_icon_size);

        final int iconWidth = icon.getIntrinsicWidth();
        final int iconHeight = icon.getIntrinsicHeight();

        if (icon instanceof PaintDrawable) {
            PaintDrawable painter = (PaintDrawable) icon;
            painter.setIntrinsicWidth(width);
            painter.setIntrinsicHeight(height);
        }

        if (width > 0 && height > 0 && (width < iconWidth || height < iconHeight)) {
            final float ratio = (float) iconWidth / iconHeight;

            if (iconWidth > iconHeight) {
                height = (int) (width / ratio);
            } else if (iconHeight > iconWidth) {
                width = (int) (height * ratio);
            }

            final Bitmap.Config c =
                    icon.getOpacity() != PixelFormat.OPAQUE ?
                            Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
            final Bitmap thumb = Bitmap.createBitmap(width, height, c);
            final Canvas canvas = new Canvas(thumb);
            canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG, 0));
            // Copy the old bounds to restore them later
            // If we were to do oldBounds = icon.getBounds(),
            // the call to setBounds() that follows would
            // change the same instance and we would lose the
            // old bounds
            mOldBounds.set(icon.getBounds());
            icon.setBounds(0, 0, width, height);
            icon.draw(canvas);
            icon.setBounds(mOldBounds);
            icon = info.icon = new BitmapDrawable(thumb);
            //info.filtered = true;
        }
    }

    public void getIconImageOUYA(AppInfo info) {
        String packageName = info.intent.getComponent().getPackageName();
        try {
            android.content.pm.ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(packageName, 0);
            Resources resources = getPackageManager().getResourcesForApplication(applicationInfo);
            int identifier3 = resources.getIdentifier(packageName + ":drawable/ouya_icon", "", "");
            info.icon = getPackageManager().getResourcesForApplication(applicationInfo).getDrawable(identifier3);
            info.filtered = true;
            ///////////////////
            Bitmap bitmap = ((BitmapDrawable) info.icon).getBitmap();
            // Scale it //http://stackoverflow.com/questions/4609456/android-set-drawable-size-programatically
            BitmapDrawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 480, 270, true));
            //Save to file //http://stackoverflow.com/questions/649154/save-bitmap-to-location
            FileOutputStream out = new FileOutputStream("/sdcard/FrankkieOuyaLauncher/thumbnails/" + packageName + ".png");
            d.getBitmap().compress(Bitmap.CompressFormat.PNG, 90, out);
            Runtime.getRuntime().gc(); //important
        } catch (Exception e) {
            Log.e("FrankkieOuyaLauncher", "ERROR", e);
        }
    }

    public void getIconImageOUYA(DatabaseAppInfo info) {
        String packageName = info.intent.getComponent().getPackageName();
        try {
            android.content.pm.ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(packageName, 0);
            Resources resources = getPackageManager().getResourcesForApplication(applicationInfo);
            int identifier3 = resources.getIdentifier(packageName + ":drawable/ouya_icon", "", "");
            info.icon = getPackageManager().getResourcesForApplication(applicationInfo).getDrawable(identifier3);
            //info.filtered = true;
            ///////////////////
            Bitmap bitmap = ((BitmapDrawable) info.icon).getBitmap();
            // Scale it //http://stackoverflow.com/questions/4609456/android-set-drawable-size-programatically
            BitmapDrawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 480, 270, true));
            //Save to file //http://stackoverflow.com/questions/649154/save-bitmap-to-location
            FileOutputStream out = new FileOutputStream("/sdcard/FrankkieOuyaLauncher/thumbnails/" + packageName + ".png");
            d.getBitmap().compress(Bitmap.CompressFormat.PNG, 90, out);
            Runtime.getRuntime().gc(); //important
        } catch (Exception e) {
            Log.e("FrankkieOuyaLauncher", "ERROR", e);
        }
    }

    public class LoadApplicationsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            loadApplications();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            filter();
            fillTable();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //ANALYTICS
        FlurryAgent.onStartSession(this, "MDHSMF65TV4JCSW3QN63");
    }

    @Override
    protected void onStop() {
        super.onStop();
        //ANALYTICS
        FlurryAgent.onEndSession(this);
    }

    /**
     * Get DatabaseAppInfo, will be made when not in DB
     */
    public DatabaseAppInfo getDatabaseAppInfo(ResolveInfo resolveInfo) {
        DatabaseAppInfo appInfo = null;
        DatabaseOpenHelper helper = DatabaseOpenHelper.CreateInstance(this);
        Cursor cursor = helper.WriteableDatabase.rawQuery("SELECT id FROM appinfo WHERE packageName = '"
                + resolveInfo.activityInfo.applicationInfo.packageName + "'", null);
        if (cursor.getCount() == 0) {
            appInfo = new DatabaseAppInfo();
            //Fill info
            appInfo.packageName = resolveInfo.activityInfo.applicationInfo.packageName;
            appInfo.title = resolveInfo.loadLabel(getPackageManager()).toString();
        } else {
            cursor.moveToFirst();
            appInfo = new DatabaseAppInfo(cursor.getInt(0));
            appInfo.OnLoad();
        }
        cursor.close();
        //(Re)set the stuff that is not in Database
        checkIsOUYAAppByIntent(appInfo);
        //Intent
        appInfo.setActivity(new ComponentName(
                appInfo.packageName,
                resolveInfo.activityInfo.name),
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        //Image
        if (appInfo.isOUYA()) {
            File thumbFile = new File("/sdcard/FrankkieOuyaLauncher/thumbnails/" + appInfo.packageName + ".png");
            if (thumbFile.exists()) {
                appInfo.icon = new BitmapDrawable(BitmapFactory.decodeFile(thumbFile.getPath()));
            } else {
                getIconImageOUYA(appInfo);
            }
        } else {
            appInfo.icon = resolveInfo.loadIcon(getPackageManager());
            getIconImageAndroidApps(appInfo);
        }

        if (!appInfo.InDatabase()) {
            appInfo.OnInsert();
        } else {
            appInfo.OnUpdate();
        }

        return appInfo;
    }

    public DatabaseAppInfo getDatabaseAppInfo(String packageName) {
        for (IGridItem item : gridItems) {
            if (item.isFolder()) {
                continue;
            }
            if (((DatabaseAppInfo) item).packageName.equals(packageName)) {
                return (DatabaseAppInfo) item;
            }
        }
        return null;
    }

    public DatabaseFolder getDatabaseFolder(String folderName) {
        DatabaseOpenHelper helper = DatabaseOpenHelper.CreateInstance(this);
        Cursor cursor = helper.WriteableDatabase.rawQuery("SELECT id FROM folder WHERE title = '"
                + folderName + "'", null);
        DatabaseFolder folder = null;
        if (cursor.getCount() == 0) {
            //Make folder
            folder = new DatabaseFolder();
            folder.setFolderName(folderName);
            folder.OnInsert(); //add to DB
            //gridItems.add(folder);
        } else {
            cursor.moveToFirst();
            folder = new DatabaseFolder(cursor.getInt(0));
            folder.OnLoad();
        }
        folder.setIcon(getResources().getDrawable(R.drawable.folder));
        cursor.close();
        return folder;
    }


}
