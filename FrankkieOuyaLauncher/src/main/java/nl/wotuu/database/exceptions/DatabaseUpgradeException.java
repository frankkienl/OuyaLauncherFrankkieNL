package nl.wotuu.database.exceptions;

import nl.wotuu.database.DatabaseRow;

/**
 * Created by Wouter on 7/9/13.
 */
public class DatabaseUpgradeException extends RuntimeException {
    public int oldversion;
    public int newVersion;

    public DatabaseUpgradeException(int oldversion, int newVersion) {
        this.oldversion = oldversion;
        this.newVersion = newVersion;
    }

    public DatabaseUpgradeException(int oldversion, int newVersion, String detailMessage) {
        super(FormatDetailMessage(oldversion, newVersion, detailMessage));
        this.oldversion = oldversion;
        this.newVersion = newVersion;
    }

    public DatabaseUpgradeException(int oldversion, int newVersion, String detailMessage, Throwable throwable) {
        super(FormatDetailMessage(oldversion, newVersion, detailMessage), throwable);
        this.oldversion = oldversion;
        this.newVersion = newVersion;
    }

    public DatabaseUpgradeException(int oldversion, int newVersion, Throwable throwable) {
        super(throwable);
        this.oldversion = oldversion;
        this.newVersion = newVersion;
    }

    private static String FormatDetailMessage(int oldversion, int newVersion, String detailMessage){
        return "Upgrading from version " + oldversion + " to " + newVersion + " failed: " + detailMessage;
    }
}
