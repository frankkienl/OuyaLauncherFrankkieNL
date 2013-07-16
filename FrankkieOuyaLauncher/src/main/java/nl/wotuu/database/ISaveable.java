package nl.wotuu.database;

import java.util.List;

/**
 * Created by Wouter on 15/06/13.
 */
public interface ISaveable {

    /**
     * Insert this class into the database.
     */
    void OnInsert();

    /**
     * Update this class in the database.
     */
    void OnUpdate();

    /**
     * Return a value representing if this instance is in the database or not.
     * @return Yes if this instance exists in the database, no if not.
     */
    Boolean InDatabase();

    /**
     * Return a value representing if this instance is in the database or not.
     * @param compareColumns The columns to use to compare with other rows in the database.
     * @return Yes if this instance exists in the database, no if not.
     */
    Boolean InDatabase(List<String> compareColumns);

    /**
     * Delete this class from the database.
     */
    void OnDelete();

    /**
     * Load this class from the database.
     */
    void OnLoad();
}
