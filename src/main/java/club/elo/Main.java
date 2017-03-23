package club.elo;

import club.elo.converter.ClubEloConverter;
import club.elo.converter.ResultSetConverter;

import java.sql.*;
import java.util.Scanner;

public class Main {

    // TODO: Create object that contains methods for various DB queries
    private static final String JDB_URL = "jdbc:mysql://localhost:3306/soccerElo?rewriteBatchedStatements=true";

    public static void main(String args[]) throws Exception {
        /* Change these according to your local database connection */
        String username = "usr";
        String password = "psswrd";

        System.out.println("Connecting to database...");

        try (Connection connection = DriverManager.getConnection(JDB_URL, username, password)) {
            System.out.println("Connected!");
            Statement statement = connection.createStatement();

            CLI cli = new CLI(new ClubEloConverter(), new ResultSetConverter());

            try (Scanner input = new Scanner(System.in)) {
                cli.handle(input, statement);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
