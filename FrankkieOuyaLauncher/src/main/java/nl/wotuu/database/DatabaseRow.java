package nl.wotuu.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteMisuseException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import nl.wotuu.database.annotations.DatabaseExclude;
import nl.wotuu.database.annotations.DatabasePrimaryKey;
import nl.wotuu.database.exceptions.DatabaseLoadException;
import proguard.annotation.KeepPublicClassMemberNames;

/**
 * Created by Wouter on 6/16/13.
 */
@KeepPublicClassMemberNames
public abstract class DatabaseRow implements ISaveable {

    @DatabaseExclude
    public static Boolean NO_QUERIES = false;

    @DatabaseExclude
    private String tableName;

    @DatabasePrimaryKey
    public int id;

    public DatabaseRow(String tableName) {
        if (tableName == null)
            throw new NullPointerException("Table name cannot be null!");
        this.tableName = tableName;
    }

    public DatabaseRow(String tableName, int id) {
        this.id = id;
        if (tableName == null)
            throw new NullPointerException("Table name cannot be null!");
        this.tableName = tableName;
    }

    /**
     * @inheritDoc
     */
    public void OnInsert(Boolean mayQueue) {
        DatabaseOpenHelper helper = DatabaseOpenHelper.GetInstance();

        List<String> fieldNames = this.GetDatabaseFieldNames();
        List<String> fieldValues = this.GetDatabaseFieldValues();

        if (fieldNames.size() != fieldValues.size()) {
            throw new ArrayIndexOutOfBoundsException("Field names count does not equal field values count!");
        }

        if (fieldNames.size() == 0 || fieldValues.size() == 0)
            throw new SQLiteException("Cannot insert into database if implementing class doesn't have any fields!");

        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < fieldNames.size(); i++) {
            contentValues.put(fieldNames.get(i), fieldValues.get(i));
        }

        if (!DatabaseRow.NO_QUERIES) {
            helper.SynchronizedInsert(this.tableName, contentValues, this, mayQueue);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void OnInsert() {
        this.OnInsert(false);
    }

    /**
     * @inheritDoc
     */
    public void OnUpdate(Boolean mayQueue) {
        if (this.id <= 0)
            throw new SQLiteException("Cannot update user whose ID is not set!");

        DatabaseOpenHelper helper = DatabaseOpenHelper.GetInstance();

        List<String> fieldNames = this.GetDatabaseFieldNames();
        List<String> fieldValues = this.GetDatabaseFieldValues();

        if (fieldNames.size() == 0 || fieldValues.size() == 0)
            throw new SQLiteException("Cannot update database if implementing class doesn't have any fields!");

        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < fieldNames.size(); i++) {
            contentValues.put(fieldNames.get(i), fieldValues.get(i));
        }

        String whereClause = "`id` = '" + this.id + "';";

        if (!DatabaseRow.NO_QUERIES) {
            helper.SynchronizedUpdate(this.tableName, contentValues, whereClause, mayQueue);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void OnUpdate() {
        this.OnUpdate(false);
    }

    /**
     * @inheritDoc
     */
    public void OnDelete(Boolean mayQueue) {
        if (this.id <= 0)
            throw new SQLiteMisuseException("Cannot delete user whose ID is not set!");

        DatabaseOpenHelper helper = DatabaseOpenHelper.GetInstance();
        String query = "DELETE FROM `" + this.tableName + "`" +
                "WHERE `id` = '" + this.id + "'";

        if (!DatabaseRow.NO_QUERIES) {
            helper.SynchronizedDelete(query, mayQueue);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void OnDelete() {
        this.OnDelete(false);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Boolean InDatabase() {
        return this.InDatabase(this.GetDatabaseFieldNames());
    }

    /**
     * @inheritDoc
     */
    public Boolean InDatabase(List<String> compareColumns) {
        DatabaseOpenHelper helper = DatabaseOpenHelper.GetInstance();
        SQLiteDatabase readableDatabase = helper.ReadableDatabase;
        String query = "SELECT `id` FROM `" + this.tableName + "` WHERE ";

        if (this.id < 1) {
            List<String> fieldValues = this.GetDatabaseFieldValues(compareColumns);

            List<String> queryAddition = new ArrayList<String>();
            for (int i = 0; i < compareColumns.size(); i++) {
                queryAddition.add("`" + compareColumns.get(i) + "` = " + DatabaseUtils.sqlEscapeString(fieldValues.get(i)));
            }

            query += Utils.Join(queryAddition, " AND ");
        } else
            query += "`id` = '" + this.id + "'";

        if (!DatabaseRow.NO_QUERIES) {
            // synchronized (readableDatabase){
            Cursor cursor = readableDatabase.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                this.id = cursor.getInt(0);
                return true;
            }
            // }
        }

        return false;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void OnLoad() {
        if (this.id <= 0) {
            throw new IllegalArgumentException("Cannot load object from database whose ID is not set.");
        }

        DatabaseOpenHelper helper = DatabaseOpenHelper.GetInstance();
        SQLiteDatabase readableDatabase = helper.ReadableDatabase;
        String query = "SELECT * FROM `" + this.tableName + "` WHERE `id` = '" + this.id + "'";

        if (!DatabaseRow.NO_QUERIES) {
            Cursor cursor = readableDatabase.rawQuery(query, null);
            Field[] fields = this.GetDatabaseFields();
            if (cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    String columnName = cursor.getColumnName(i);
                    for (Field field : fields) {
                        if (field.getName().equals(columnName)) {
                            try {
                                field.set(this, this.GetValueOfCursor(cursor, field.getType(), i));
                                break;
                            } catch (IllegalAccessException e) {
                                throw new NullPointerException("Cannot fetch value from database! Type of column name " +
                                        columnName + " is not supported.");
                            }
                        }
                    }
                }
            } else Logger.e("Cannot find database row with id = '" + this.id + "'!");
        }
    }

    /**
     * Attempts to load this database row based on the passed column names.
     *
     * @param columnNames The column names to match this row with.
     */
    public void OnLoad(List<String> columnNames) {

        if (columnNames.size() <= 0) {
            throw new IllegalArgumentException("Unable to load data from class '" + this.getClass().getName() +
                    "' when 0 column names are given");
        }

        // Check if this user's ID is set
        SQLiteDatabase readableDatabase = DatabaseOpenHelper.GetInstance().ReadableDatabase;
        String query = "SELECT `id` " +
                "FROM `" + DatabaseOpenHelper.GetInstance().GetTableName(this.getClass()) + "`";

        List<String> values = this.GetDatabaseFieldValues(columnNames);

        List<String> queryAddition = new ArrayList<String>();
        int count = 0;
        for (String value : values) {
            queryAddition.add(" `" + columnNames.get(count) + "` = " + DatabaseUtils.sqlEscapeString(value));
            count++;
        }

        if (queryAddition.size() == 0) {
            throw new DatabaseLoadException(this, "Unable to load data from class '" + this.getClass().getName() +
                    "' when query would not contain WHERE clause.");
        } else {
            query += " WHERE " + Utils.Join(queryAddition, " AND ");
        }

        Cursor cursor = readableDatabase.rawQuery(query, null);

        if (cursor.getCount() == 0)
            throw new DatabaseLoadException(this, "Didn't find a matching row to load this DatabaseRow from (query: '" + query + "')");
        else if (cursor.getCount() > 1)
            throw new DatabaseLoadException(this, "Found multiple rows matching the query ('" + query + "') (" + cursor.getCount() + ")");
        else if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            // Fetch the ID
            this.id = cursor.getInt(0);
            // Load by id now
            this.OnLoad();
        }
    }

    /**
     * Get the data of the cursor on a certain index, based on the type.
     *
     * @param cursor The cursor that contains the data.
     * @param type   The type your field is in.
     * @param index  The index of the location of the data in the cursor.
     * @return The resulting object matching to the type.
     */
    private Object GetValueOfCursor(Cursor cursor, Class type, int index) {
        if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class)) {
            return cursor.getInt(index);
        } else if (type.isAssignableFrom(String.class)) {
            return cursor.getString(index);
        } else if (type.isAssignableFrom(Float.class) || type.isAssignableFrom(float.class)) {
            return cursor.getDouble(index);
        } else if (type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class)) {
            return cursor.getFloat(index);
        } else {
            throw new IllegalArgumentException("Unable to assign value from cursor to class " + type.getName() +
                    " with index " + index + ". This type is not supported.");
        }
    }

    /**
     * Get all database fields
     *
     * @return
     */
    private Field[] GetDatabaseFields() {
        List<Field> databaseFields = new ArrayList<Field>();

        Field[] fields = this.getClass().getDeclaredFields();
        for (Field f : fields) {
            if (!f.isAnnotationPresent(DatabaseExclude.class))
                databaseFields.add(f);
        }

        return databaseFields.toArray(new Field[0]);
    }

    /**
     * Get all field names of the implementing class, that are not decorated with the
     * DatabaseExclude annotation.
     *
     * @return The list containing the field names.
     */
    private List<String> GetDatabaseFieldNames() {
        List<String> fieldNames = new ArrayList<String>();

        Field[] fields = this.GetDatabaseFields();
        for (Field f : fields) {
            fieldNames.add(f.getName());
        }

        return fieldNames;
    }


    /**
     * Get all field values of the implementing class.
     *
     * @return The list containing the field values.
     */
    private List<String> GetDatabaseFieldValues() {
        return this.GetDatabaseFieldValues(this.GetDatabaseFieldNames());
    }


    /**
     * Get all field values of the implementing class.
     *
     * @param fieldNames The list containing the field names of the values you'd like to get.
     * @return The list containing the field values.
     */
    private List<String> GetDatabaseFieldValues(List<String> fieldNames) {
        if (fieldNames.size() == 0)
            throw new IllegalArgumentException("Cannot get field values of 0 fields!");

        List<String> fieldValues = new ArrayList<String>();

        Field[] fields = this.GetDatabaseFields();
        // For every value we want
        for (String s : fieldNames) {
            // For every value we have
            for (Field f : fields) {
                // If there is no annotation present, and if the value we have is a value we want
                if (!f.isAnnotationPresent(DatabaseExclude.class) && fieldNames.contains(f.getName())) {
                    // If the value we want is the value we have
                    if (s.equals(f.getName())) {
                        try {
                            // Add it to the list!
                            fieldValues.add(String.valueOf(f.get(this)));
                        } catch (IllegalAccessException e) {

                        }
                    }
                }
            }
        }
        return fieldValues;
    }
}
