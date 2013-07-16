package nl.wotuu.database.exceptions;

import nl.wotuu.database.DatabaseRow;

/**
 * Created by Wouter on 7/9/13.
 */
public class DatabaseLoadException extends RuntimeException {
    public DatabaseRow databaseRow;

    public DatabaseLoadException(DatabaseRow databaseRow) {
        this.databaseRow = databaseRow;
    }

    public DatabaseLoadException(DatabaseRow databaseRow, String detailMessage) {
        super(detailMessage);
        this.databaseRow = databaseRow;
    }

    public DatabaseLoadException(DatabaseRow databaseRow, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.databaseRow = databaseRow;
    }

    public DatabaseLoadException(DatabaseRow databaseRow, Throwable throwable) {
        super(throwable);
        this.databaseRow = databaseRow;
    }
}
