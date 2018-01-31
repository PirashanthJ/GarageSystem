package vehicles.logic;


import common.Database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * @author Ilias.
 *
 * Various methods that need to be used by multiple classes.
 */

public class Utility {

    // Credit to code from parts.logic.Utility.closeStatementAndConnection(Statement s, Connection c)
    static void closeDatabaseFeeds(Statement s, Connection c){
        try {
            if(s != null && !s.isClosed()){
                s.close();
            }else if(c != null && !c.isClosed()){
                c.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Select the latest (Largest Increment) ID in a table
    static int getLatestID(String table) {
        Database db = Database.getInstance();
        int ID = 0;
        Connection connection = db.getConnection();
        Statement statement = null;

        String query =  "SELECT "+table+"ID " +
                        "FROM "+table+" " +
                        "ORDER BY "+table+"ID DESC LIMIT 1";
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            ID = rs.getInt(table+"ID");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Utility.closeDatabaseFeeds(statement, connection);
        }
        return ID;
    }
}
