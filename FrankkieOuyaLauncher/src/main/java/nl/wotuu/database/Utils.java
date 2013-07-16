package nl.wotuu.database;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Wouter on 6/18/13.
 */
public class Utils {
    /**
     * Join a string array or list with a delimiter.
     *
     * @param s         The array or list to join.
     * @param delimiter The delimiter to glue the pieces together with.
     * @return The joined string.
     */
    public static String Join(Collection<?> s, String delimiter) {
        StringBuilder builder = new StringBuilder();
        Iterator<?> iter = s.iterator();
        while (iter.hasNext()) {
            builder.append(iter.next());
            if (!iter.hasNext()) {
                break;
            }
            builder.append(delimiter);
        }
        return builder.toString();
    }
}
