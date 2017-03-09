package club.elo;

import club.elo.converter.ClubEloConverter;
import club.elo.converter.ResultSetConverter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {

    private static final boolean UPDATE = true;
    private static final String JDB_URL = "jdbc:mysql://localhost:3306/soccerElo?rewriteBatchedStatements=true";

    public static void main(String args[]) throws Exception {
        /* Change these according to your local database connection */
        String username = "usr";
        String password = "psswrd";

        System.out.println("Connecting to database...");

        try (Connection connection = DriverManager.getConnection(JDB_URL, username, password)) {
            System.out.println("Connected!");
            Statement statement = connection.createStatement();
            DBUpdater dbUpdater = new DBUpdater(new ClubEloConverter(), new ResultSetConverter());

            if (UPDATE) {
                System.out.println("Updating local database (this could be a while!)");
                dbUpdater.update(statement);
            }

            // TODO: Command line interface for user interaction and queries to the DB.
            // TODO: Move the DB update to a command you run through the above interface.
            // TODO: Have the db update run in the background instead of having the user wait for a few hours.

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
