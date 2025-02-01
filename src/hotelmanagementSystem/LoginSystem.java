package hotelmanagementSystem;
import java.sql.*;
import java.util.Scanner;

public class LoginSystem {
    private Connection conn; // Use to connect to the database.
    private String loggedInUser; 
    
    // Getter for logged-in user
    public String getLoggedInUser() {
        return loggedInUser;
    }

    public LoginSystem() {
        try {
            conn = DriverManager.getConnection(
                "jdbc:mysql://sql12.freesqldatabase.com:3306/sql12756466",
                "sql12756466", "5YEGpKtSs1");
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }

    public boolean login(String username, String password) {
        try {
            PreparedStatement stmt = conn.prepareStatement(
                // Checking if the inserted username and password are in the database
                "SELECT * FROM Guest WHERE Username = ? AND Password = ?"
            );
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet results = stmt.executeQuery();

            if (results.next()) {
                loggedInUser = username; 
                System.out.println("Login successful. Welcome, " + username + "!");
                return true;
            } else {
                System.out.println("Login failed: Invalid username or password.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Login failed: " + e.getMessage());
            return false;
        }
    }

    // Creation of account
    public void createAccount(String username, String password, int age, String customerType, 
            String contactNumber, String emailAddress, String role) {
        try {
            // Checking if the username inputted is already in the database, to display an error
            String checkUserQuery = "SELECT * FROM Guest WHERE Username = ?";
            PreparedStatement checkUserStmt = conn.prepareStatement(checkUserQuery);
            checkUserStmt.setString(1, username);
            ResultSet resultSet = checkUserStmt.executeQuery();

            if (resultSet.next()) {
                System.out.println("Error: Username already exists. Please choose a different username.");
                return;
            }

            // Saving the inputted data to the Guest table in the database
            String query = "INSERT INTO Guest (Username, Password, Age, CustomerType, Contactnumber, EmailAddress, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.setInt(3, age);
                stmt.setString(4, customerType);
                stmt.setString(5, contactNumber);
                stmt.setString(6, emailAddress);
                stmt.setString(7, role);

                stmt.executeUpdate();
                System.out.println(role.substring(0, 1).toUpperCase() + role.substring(1) + " account created successfully!");
            }
        } catch (SQLException e) {
            System.out.println("Error creating account: " + e.getMessage());
        }
    }
}