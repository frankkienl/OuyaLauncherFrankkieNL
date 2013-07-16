package nl.wotuu.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.wotuu.database.annotations.DatabaseExclude;
import nl.wotuu.database.annotations.DatabasePrimaryKey;
import nl.frankkie.ouyalauncher.databaserows.DatabaseAppInfo;
import nl.wotuu.database.exceptions.DatabaseUpgradeException;

/**
 * Created by Wouter on 6/11/13.
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static DatabaseOpenHelper instance;


    private Object databaseSyncLock = new Object();

    public SQLiteDatabase WriteableDatabase;
    public SQLiteDatabase ReadableDatabase;

    private HashMap<String, TableNameMap> TableNames;
    private HashMap<String, DatabaseUtils.InsertHelper> InsertHelpers;

    private DatabaseBatchManager BatchManager;

    /**
     * Creates a new database helper. Can only be called once for effect. Multiple times will just return
     * the first instance.
     *
     * @param context The activity to create the instance from.
     * @return The DatabaseOpenHelper
     */
    public static DatabaseOpenHelper CreateInstance(Context context) {
        if (instance == null)
            instance = new DatabaseOpenHelper(context);
        return instance;
    }

    /**
     * Gets the instance of the DatabaseOpenHelper.
     *
     * @return The DatabaseOpenHelper instance.
     */
    public static DatabaseOpenHelper GetInstance() {
        if (instance == null)
            throw new NullPointerException("Call CreateInstance first!");
        return instance;
    }


    DatabaseOpenHelper(Context context) {
        super(context, "galaxy_empire_trainer", null, DATABASE_VERSION);

        this.TableNames = new HashMap<String, TableNameMap>();
        this.TableNames.put("appinfo", new TableNameMap("appinfo", DatabaseAppInfo.class));

        this.WriteableDatabase = this.getWritableDatabase();
        this.ReadableDatabase = this.getReadableDatabase();

        this.BatchManager = new DatabaseBatchManager(this);
    }

    /**
     * Get the insert helper for a certain table.
     *
     * @param tableName The name of the table you'd like to get the insert helper from.
     * @return The inserthelper, or null if none exists.
     */
    public DatabaseUtils.InsertHelper GetInsertHelper(String tableName) {
        for (Map.Entry<String, DatabaseUtils.InsertHelper> entry : this.InsertHelpers.entrySet()) {
            if (entry.getKey().equals(tableName))
                return entry.getValue();
        }
        return null;
    }

    /**
     * Get the table name corresponding to a certain class.
     *
     * @param c The Class definition you'd like to get the table name for.
     * @return The tablename, or null otherwise!
     */
    public String GetTableName(Class c) {
        for (Map.Entry<String, TableNameMap> entry : this.TableNames.entrySet()) {
            if (entry.getValue().TableClass == c)
                return entry.getValue().TableName;
        }
        return null;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        this.InsertHelpers = new HashMap<String, DatabaseUtils.InsertHelper>();
        for (TableNameMap tableNameMap : this.TableNames.values()) {
            this.InsertHelpers.put(tableNameMap.TableName, new DatabaseUtils.InsertHelper(db, tableNameMap.TableName));
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (TableNameMap tableNameMap : this.TableNames.values()) {
            db.execSQL(this.CreateTable(tableNameMap.TableClass, tableNameMap.TableName));
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        //
    }

    /**
     * Performs a synchronized insert of an insert query.
     *
     * @param tableName     The table name that is inserted in.
     * @param contentValues The inserted values.
     * @param databaseRow   The database row that will receive the inserted ID.
     * @param mayQueue      If the insert is allowed to be queued (the DatabaseRow will receive the ID of the inserted row)
     */
    public void SynchronizedInsert(String tableName, ContentValues contentValues, DatabaseRow databaseRow, Boolean mayQueue) {
        if (mayQueue) {
            this.BatchManager.QueueInsert(tableName, contentValues, databaseRow);
        } else {
            synchronized (databaseSyncLock) {
                databaseRow.id = (int) this.WriteableDatabase.insert(tableName, null, contentValues);
            }
        }
    }

    /**
     * Performs a synchronized insert of an insert query.
     *
     * @param rawQuery The raw insert query.
     * @param mayQueue If the query may be queued or not (does not set the received id!).
     */
    public void SynchronizedInsert(String rawQuery, Boolean mayQueue) {
        if (mayQueue) {
            this.BatchManager.QueueInsert(rawQuery);
        } else {
            synchronized (databaseSyncLock) {
                this.WriteableDatabase.execSQL(rawQuery);
            }
        }
    }

    /**
     * Performs a synchronized update of an update query.
     *
     * @param tableName     The table name that is updateed in.
     * @param contentValues The updateed values.
     * @param whereClause   The where clause which matches all rows that must be updated.
     * @param mayQueue      If the update is allowed to be queued.
     */
    public void SynchronizedUpdate(String tableName, ContentValues contentValues, String whereClause, Boolean mayQueue) {
        if (mayQueue) {
            this.BatchManager.QueueUpdate(tableName, contentValues, whereClause);
        } else {
            synchronized (databaseSyncLock) {
                this.WriteableDatabase.update(tableName, contentValues, whereClause, null);
            }
        }
    }

    /**
     * Performs a synchronized update of an update query.
     *
     * @param rawQuery The raw update query.
     * @param mayQueue If the query may be queued or not.
     */
    public void SynchronizedUpdate(String rawQuery, Boolean mayQueue) {
        if (mayQueue) {
            this.BatchManager.QueueUpdate(rawQuery);
        } else {
            synchronized (databaseSyncLock) {
                this.WriteableDatabase.execSQL(rawQuery);
            }
        }
    }

    /**
     * Performs a synchronized delete of an delete query.
     *
     * @param rawQuery The raw delete query.
     * @param mayQueue If the query may be queued or not.
     */
    public void SynchronizedDelete(String rawQuery, Boolean mayQueue) {
        if (mayQueue) {
            this.BatchManager.QueueDelete(rawQuery);
        } else {
            synchronized (databaseSyncLock) {
                this.WriteableDatabase.execSQL(rawQuery);
            }
        }
    }

    /**
     * Makes a create table query from a class definition.
     *
     * @param c         The class definition you'd like to create a CREATE TABLE from.
     * @param tableName The name of the table you'd like to have created.
     * @return The string containing the CREATE TABLE query;
     */
    public String CreateTable(Class c, String tableName) {
        Field[] fields = c.getFields();
        if (fields.length == 0) {
            throw new IllegalArgumentException("Cannot create Create Table SQL string from class without fields.");
        }

        String result = "CREATE TABLE IF NOT EXISTS `" + tableName + "` ";

        Field primaryKeyField = null;

        List<String> fieldStrings = new ArrayList<String>();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(DatabaseExclude.class)) {
                String fieldString = "`" + field.getName() + "` ";


                if (field.isAnnotationPresent(DatabasePrimaryKey.class)) {
                    if (primaryKeyField != null) {
                        throw new UnsupportedOperationException("Cannot have database table with two primary keys.");
                    }
                    if (!field.getType().isAssignableFrom(Integer.class) && !field.getType().isAssignableFrom(int.class)) {
                        throw new UnsupportedOperationException("Cannot have primary key that is not of Integer or int type!.");
                    }
                    primaryKeyField = field;
                    fieldString += " INTEGER PRIMARY KEY AUTOINCREMENT";
                } else {

                    if (field.getType().isAssignableFrom(Integer.class) || field.getType().isAssignableFrom(int.class)) {
                        fieldString += " INT ";
                    } else if (field.getType().isAssignableFrom(String.class)) {
                        fieldString += " TEXT ";
                    } else if (field.getType().isAssignableFrom(Float.class) || field.getType().isAssignableFrom(float.class) ||
                            field.getType().isAssignableFrom(Double.class) || field.getType().isAssignableFrom(double.class)) {
                        fieldString += " REAL ";
                    } else fieldString += " NOT NULL";
                }

                fieldStrings.add(fieldString);
            }
        }

        if (primaryKeyField == null) {
            throw new UnsupportedOperationException("Cannot have database table without a primary key! (For your own good.)");
        }

        result += " (" + Utils.Join(fieldStrings, ", ") + ") ";

        return result;
    }

    private class TableNameMap {
        public String TableName;
        public Class TableClass;

        private TableNameMap(String tableName, Class tableClass) {
            TableName = tableName;
            TableClass = tableClass;
        }
    }
}
