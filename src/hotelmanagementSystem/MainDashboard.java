package hotelmanagementSystem;

import java.util.Scanner;
import java.sql.*;

public class MainDashboard {
    private static Connection conn; // use to connect on database
    private static LoginSystem loginSystem;

    public static void main(String[] args) {
        loginSystem = new LoginSystem(); // Initialize LoginSystem
        Scanner scanner = new Scanner(System.in);

        try {
            conn = DriverManager.getConnection(
            		 "jdbc:mysql://sql12.freesqldatabase.com:3306/sql12756466",
                     "sql12756466", "5YEGpKtSs1");
            System.out.println("Database connection successful.");
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            return;
        }

        if (loginMenu(scanner)) { // Prompt login menu
            displayDashboard(scanner); //call the dashboard method.
        }
    }
    // asking the user to choose an option 
    private static boolean loginMenu(Scanner scanner) {
        while (true) {
            System.out.println("1. Login");
            System.out.println("2. Create Account");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            int option = Integer.parseInt(scanner.nextLine());

            switch (option) {
            case 1:
                System.out.print("Enter username: ");
                String username = scanner.nextLine().trim(); // Trim to avoid extra spaces
                System.out.print("Enter password: ");
                String password = scanner.nextLine();

                if (loginSystem.login(username, password)) {
                    return true; // Login was successful, exit the loop
                }
                // If login fails, no need for additional messages as they're already handled in the LoginSystem
                break;


                case 2:
                    createGuestAccount(scanner); // initializing createguestaccount method
                    break;

                case 3:
                    System.out.println("Exiting...");
                    System.exit(0);
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
    	//initializing create account method for easy account creation
    private static void createGuestAccount(Scanner scanner) {
        // Get the username and password
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        
        System.out.print("Enter your Age: ");
        int age = scanner.nextInt();
        scanner.nextLine(); 

     // Validate the customer type
        System.out.println("Identify if you're a Senior, Student, PWD: ");
        System.out.print("Type 'none' if not in the choices.");
        
        
        
        String customerType = scanner.nextLine().trim(); // Trim to remove extra spaces

     // Validate the CustomerType (case-insensitive)
     if (!customerType.equalsIgnoreCase("Senior") && 
         !customerType.equalsIgnoreCase("Student") && 
         !customerType.equalsIgnoreCase("PWD") && 
         !customerType.equalsIgnoreCase("none")) 
     { 
         System.out.println("Invalid CustomerType! Only 'Senior', 'Student', 'PWD', or 'none' is allowed."); 
         return; 
     }


        
        System.out.print("Enter Contact Number: ");
        String contactNumber = scanner.nextLine();

        
        System.out.print("Enter Email Address: ");
        String emailAddress = scanner.nextLine();

       
        System.out.print("Enter Role (admin/guest): ");
        String role = scanner.nextLine().toLowerCase();

        // Validate the role
        if (!role.equals("admin") && !role.equals("guest")) {
            System.out.println("Invalid role! Only 'admin' or 'guest' is allowed.");
            return;
        }

        // Call the method to create the account
        loginSystem.createAccount(username, password, age, customerType, contactNumber, emailAddress, role);
    }

    //initializing the dashboard
    private static void displayDashboard(Scanner scanner) {
    	System.out.println("+----------------------------------------------------------------+");
    	System.out.println("|                         Welcome                                |");
    	System.out.println("|                            to                                  |");
    	System.out.println("|                        AMRADIAMA                               |");
    	System.out.println("|                          HOTEL                                 |");
    	System.out.println("+----------------------------------------------------------------+");
    	 
    	
    	// initializing the dashboard menu
        while (true) {
        	System.out.println("+-----------------------------------------------------------------+");
            System.out.println("|                     Management Menu                             |");
            System.out.println("+-----------------------------------------------------------------+");
            System.out.println("|                 1. View Rooms Choices                           |");
            System.out.println("|                 2. Manage Guests (Your Details)                 |");
            System.out.println("|                 3. Reservation                                  |");
            System.out.println("|                 4. View Promos and Discounts                    |");
            System.out.println("|                 5. Exit                                         |");
            System.out.println("+-----------------------------------------------------------------+");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    viewRooms();// calling the viewrooms method
                    break;
                case 2:
                	//calling the manageguest method with getter of the logged in user
                    manageGuests(scanner, loginSystem.getLoggedInUser()); 
                    break;
                case 3:
                    handleReservation(scanner);//calling handleReservation
                    break;
                case 4:
                    PromoDiscount(scanner); //calling the promodiscount method
                    break;
                case 5:
                    System.out.println("Exiting... Goodbye!");// exiting
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    // creation of promodiscount methood with its menu
    private static void PromoDiscount(Scanner scanner) {
        System.out.println("+-----------------------------------------+");
        System.out.println("|          Promos and Discounts           |");
        System.out.println("+-----------------------------------------+");
        System.out.println("| 1. View Promos                          |");
        System.out.println("| 2. Manage Promos (Admins only)          |");
        System.out.println("| 3. Generate Booking Report (Admins only)|");
        System.out.println("| 4. Back to Main Menu                    |");
        System.out.println("+-----------------------------------------+");
        System.out.print("Enter your choice: ");
        int promoChoice = scanner.nextInt();
        scanner.nextLine();

        switch (promoChoice) {
            case 1:
                viewPromos(); // calling viewpromos method
                break;

            case 2:
                if (isAdminUser()) {// calling the isadminuser method
                    managePromos(scanner); // calling the managepromos method
                } else {
                    System.out.println("Access denied. Only admins can manage promos.");
                }
                break;

            case 3:
                if (isAdminUser()) {// calling isadminuser method
                    generateBookingReport();//calling generatebookingreport method
                } else {
                    System.out.println("Access denied. Only admins can generate booking reports.");
                }
                break;

            case 4:
                System.out.println("Returning to Main Menu...");//returning to main menu
                return;

            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    //initializing generatebookingreport method
    private static void generateBookingReport() {
    	// SQL query to fetch the summarized booking report on database tables
        String query = "SELECT " +
                       "COUNT(b.booking_id) AS total_booked_rooms, " +
                       "SUM(r.price) AS total_price, " +
                       "GROUP_CONCAT(b.room_number ORDER BY b.room_number) AS booked_room_numbers, " +
                       "GROUP_CONCAT(r.roomType ORDER BY b.room_number) AS booked_room_types, " +
                       "GROUP_CONCAT(g.Username ORDER BY b.room_number) AS guest_names " +
                       "FROM Bookings b " +
                       "JOIN Guest g ON g.Username = b.username " +
                       "JOIN Rooms r ON b.room_number = r.roomNumber";
       
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                // Get the summarized data
                int totalBookedRooms = rs.getInt("total_booked_rooms");
                double totalPrice = rs.getDouble("total_price");
                String bookedRoomNumbers = rs.getString("booked_room_numbers");
                String bookedRoomTypes = rs.getString("booked_room_types");
                String guestNames = rs.getString("guest_names");

                // displaying the Output summary report
                System.out.println("+----------------------------------------------------------------------------------------------------------------------+");
                System.out.println("| Booking Summary Report                                                                                               |");
                System.out.println("+----------------------------------------------------------------------------------------------------------------------+");
                System.out.printf("| Total Booked Rooms       | %-98d |\n", totalBookedRooms);
                System.out.printf("| Total Price              | $%-98.2f |\n", totalPrice);
                System.out.println("+----------------------------------------------------------------------------------------------------------------------+");
                System.out.printf("| Booked Room Numbers      | %-98s |\n", bookedRoomNumbers);
                System.out.printf("| Booked Room Types        | %-98s |\n", bookedRoomTypes);
                System.out.printf("| Guest Names              | %-98s |\n", guestNames);
                System.out.println("+----------------------------------------------------------------------------------------------------------------------+");

            } else {
                System.out.println("No bookings found.");
            }
        } catch (SQLException e) {
            System.out.println("Error generating booking summary report: " + e.getMessage());
        }
    }
    //initializing isadminuser
    private static boolean isAdminUser() {
    	//checking if theres a logged in user
        if (loginSystem.getLoggedInUser() == null) {
        	//displaying if there no logged in user
            System.out.println("You must log in first.");
            return false;
        }
        // getting the logged in user information
        String loggedInUser = loginSystem.getLoggedInUser();
        System.out.println("Logged in user: " + loggedInUser);  
        String userType = "Guest"; 

        try {
        	//checking and getting  user role on guest table where logged in username is in
            String userQuery = "SELECT role FROM Guest WHERE Username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(userQuery)) {
                stmt.setString(1, loggedInUser);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        userType = rs.getString("role").trim();  // Trim any spaces
                       
                    } else {
                    	//displaying error if no user found on database
                        System.out.println("Error: User not found in the database.");
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking authentication: " + e.getMessage());
            return false;
        }
        //returning username role ignoring case sensitivity
        return userType.equalsIgnoreCase("Admin");
    }

    //initializing viewpromos method
    private static void viewPromos() {
    	// checking database promo table
        String query = "SELECT promo_name, discount_percentage, description, valid_from, valid_until FROM Promos";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("+--------------------------------------------------------------------------------------------------------------------------+");
            System.out.println("| Promo Name          | Discount (%) |                     Description                   |   Valid From   |  Valid Until   |");
            System.out.println("+--------------------------------------------------------------------------------------------------------------------------+");
            
            
            while (rs.next()) {
            	//getting the data on the promo table
                String promoName = rs.getString("promo_name");
                Float discount = rs.getFloat("discount_percentage");
                String description = rs.getString("description");
                Date valid_from = rs.getDate("valid_from");
                Date valid_until = rs.getDate("valid_until");
                //displaying the get data from the database with design format
                System.out.printf("| %-19s | %-12.2f | %-50s | %-12s |  %-12s |%n",
                                  promoName, discount, description, valid_from, valid_until);
            }
            System.out.println("+---------------------------------------------------------------------------------------------------------+");
        } catch (SQLException e) {
            System.out.println("Error retrieving promos: " + e.getMessage());
        }
    }

    // initializing authenticateadmin method checking if username, password and role from the database was an admin
    private static boolean authenticateAdmin(String username, String password, admins admin) {
        String query = "SELECT * FROM admins WHERE username = ? AND password = ? AND role = 'admin'";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                admin.setId(rs.getInt("id"));
                admin.setUsername(rs.getString("username"));
                admin.setPassword(rs.getString("password"));
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error authenticating admin: " + e.getMessage());
        }
        return false;
    }
 // initializing authenticateguest method checking if username, password and role from the database was an guest
    private static boolean authenticateGuest(String username, String password) {
        String query = "SELECT * FROM admins WHERE username = ? AND password = ? AND role = 'guest'";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            return rs.next(); // If a result is found, the user is authenticated as a guest
        } catch (SQLException e) {
            System.out.println("Error authenticating guest: " + e.getMessage());
        }
        return false;
    }


//initializing managepromos method and displaying its menu
    private static void managePromos(Scanner scanner) {
        while (true) {
            System.out.println("+-----------------------------------------+");
            System.out.println("|            Manage Promos                |");
            System.out.println("+-----------------------------------------+");
            System.out.println("| 1. Add Promo                            |");
            System.out.println("| 2. Update Promo                         |");
            System.out.println("| 3. Delete Promo                         |");
            System.out.println("| 4. Back to Promo Menu                   |");
            System.out.println("+-----------------------------------------+");
            System.out.print("Enter your choice: ");
            int adminChoice = scanner.nextInt();
            scanner.nextLine(); 

            switch (adminChoice) {
                case 1:
                    addPromo(scanner);// calling the addpromo method
                    break;
                case 2:
                    updatePromo(scanner); // calling the update promo method
                    break;
                case 3:
                    deletePromo(scanner); // calling the deletepromo method
                    break;
                case 4:
                    System.out.println("Returning to Promo Menu...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    //initializing the addpromo method that create new promo
    private static void addPromo(Scanner scanner) {
        System.out.print("Enter promo name: ");
        String promoName = scanner.nextLine();
        System.out.print("Enter discount percentage: ");
        double discount = scanner.nextDouble();
        scanner.nextLine(); // Consume newline
        System.out.print("Enter promo description: ");
        String description = scanner.nextLine();
        System.out.print("Enter validity date (YYYY-MM-DD): ");
        String validityDate = scanner.nextLine();
        //saving the new promo on database promos table
        String query = "INSERT INTO Promos (promo_name, discount_percentage, description, validity_date) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, promoName);
            stmt.setDouble(2, discount);
            stmt.setString(3, description);
            stmt.setDate(4, Date.valueOf(validityDate));
            stmt.executeUpdate();
            System.out.println("Promo added successfully!");
        } catch (SQLException e) {
            System.out.println("Error adding promo: " + e.getMessage());
        }
    }
    //initializing update promo method to edit an existing promo
    private static void updatePromo(Scanner scanner) {
        System.out.print("Enter promo ID to update: ");
        int promoId = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        System.out.print("Enter new promo name: ");
        String promoName = scanner.nextLine();
        System.out.print("Enter new discount percentage: ");
        double discount = scanner.nextDouble();
        scanner.nextLine(); // Consume newline
        System.out.print("Enter new promo description: ");
        String description = scanner.nextLine();
        System.out.print("Enter new validity date (YYYY-MM-DD): ");
        String validityDate = scanner.nextLine();
        //updating the promo on database
        String query = "UPDATE Promos SET promo_name = ?, discount_percentage = ?, description = ?, validity_date = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, promoName);
            stmt.setDouble(2, discount);
            stmt.setString(3, description);
            stmt.setDate(4, Date.valueOf(validityDate));
            stmt.setInt(5, promoId);
            stmt.executeUpdate();
            System.out.println("Promo updated successfully!");
        } catch (SQLException e) {
            System.out.println("Error updating promo: " + e.getMessage());
        }
    }
//initializing delete promo method and remove a promo on the database
    private static void deletePromo(Scanner scanner) {
        System.out.print("Enter promo ID to delete: ");
        int promoId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        System.out.print("Are you sure you want to delete this promo? (yes/no): ");
        String ans = scanner.nextLine().trim().toLowerCase(); // Read and normalize the response

        if ("yes".equals(ans)) {
            String query = "DELETE FROM Promos WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, promoId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Promo deleted successfully!");
                } else {
                    System.out.println("Promo not found.");
                }
            } catch (SQLException e) {
                System.out.println("Error deleting promo: " + e.getMessage());
            }
        } else if ("no".equals(ans)) {
            System.out.println("Deletion canceled. Returning to promo menu...");
            // Call method to return to the promo menu (implement this method if necessary)
            return;
        } else {
            System.out.println("Invalid response. Please enter 'yes' or 'no'.");
        }
    }



//initializing handel reservation method and displaying its menu
    private static void handleReservation(Scanner scanner) {
        // Create an instance of the Booking class to handle booking and checkout method
        Booking booking = new Booking(conn, scanner, loginSystem );
        while (true) {
            System.out.println("+-----------------------------------------+");
            System.out.println("|               Reservation Menu          |");
            System.out.println("+-----------------------------------------+");
            System.out.println("| 1. View Reservation List                |"); 
            System.out.println("| 2. Book a Room                          |");
            System.out.println("| 3. Checkout                             |");
            System.out.println("| 4. Back to Main Menu                    |");
            System.out.println("+-----------------------------------------+");
            System.out.print("Enter your choice: ");
         
            int reservationChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            System.out.println();

            switch (reservationChoice) {
            	case 1:
            		 if (isAdminUser()) {// calling the isadminuser method
            			 viewReservation(); // calling the managepromos method
                     } else {
                         System.out.println("Access denied. Only admins can manage promos.");
                     }
                     break;
            		
            	
                case 2:
                    booking.bookARoom();// calling the instance of the booking class, with booking method
                    break;
                case 3:
                    booking.checkout(); //calling the instance of the booking class, with checkout method
                    break;
                case 4:
                    System.out.println("Returning to Main Menu...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        
    }
    //initializing vieew reservation method and getting the data on the database
    private static void viewReservation() {
        // Correct query with appropriate column names
        String query = "SELECT booking_id, date, username, roomType, room_number, check_in_date FROM Bookings";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            // Print table header
            System.out.println("+---------------------------------------------------------------------------------------------------------------+");
            System.out.println("| Booking ID |             Date            |    Username   |     Room Type      | Room Number | Check-in Date   |");
            System.out.println("+---------------------------------------------------------------------------------------------------------------+");

            while (rs.next()) {
                // Extract data from ResultSet
                int bookingID = rs.getInt("booking_id");
                Date date = rs.getDate("date");
                String username = rs.getString("username");
                String roomType = rs.getString("roomType");
                int roomNumber = rs.getInt("room_number");
                Date checkInDate = rs.getDate("check_in_date");
               
              //displaying of the reservation data from the database with a design format
                System.out.printf("| %10d | %-27s | %-13s | %-18s | %11d | %-15s |%n",
                        bookingID,
                        date != null ? date.toString() : "N/A",
                        username,
                        roomType,
                        roomNumber,
                        checkInDate != null ? checkInDate.toString() : "N/A");
               
            }

            
            System.out.println("+---------------------------------------------------------------------------------------------------------------+");
        } catch (SQLException e) {
            System.out.println("Error retrieving bookings: " + e.getMessage());
        }
    }


//initializing manageguest method and displaying its menu
    private static void manageGuests(Scanner scanner, String loggedInUser) {
        System.out.println("+-----------------------------------------+");
        System.out.println("|               Manage Guests             |");
        System.out.println("+-----------------------------------------+");
        System.out.println("| 1. View Your Details                    |");
        System.out.println("| 2. Delete Your Account                  |");
        System.out.println("| 3. Back to Main Menu                    |");
        System.out.println("+-----------------------------------------+");
        System.out.print("Enter your choice: ");
        int guestChoice = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        try {
            switch (guestChoice) {
                case 1: // get details of logged-in user from the database
                    String query = "SELECT * FROM Guest WHERE Username = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setString(1, loggedInUser);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                        	//displaying the get details from the database with a design format
                            System.out.println("+--------------------------------------------------------------------------------+");
                            System.out.println("| Username   | Age   | Customer Type |   Contact   |             Email           |");
                            System.out.println("+--------------------------------------------------------------------------------+");
                            System.out.printf("| %-10s | %-5d | %-13s | %-11s | %-27s |%n",
                                  rs.getString("Username"), rs.getInt("Age"),
                                    rs.getString("CustomerType"), rs.getString("Contactnumber"),
                                    rs.getString("EmailAddress"));
                            System.out.println("+--------------------------------------------------------------------------------+");
                        } else {
                            System.out.println("No details found for your account.");
                        }
                    }
                    break;

                case 2:
                    // Ask the user for confirmation before deleting the account
                    System.out.print("Are you sure you want to delete your account? (yes/no): ");
                    String confirmation = scanner.nextLine().trim().toLowerCase();

                    if ("yes".equals(confirmation)) {
                        // Deleting data from the database where the logged in user was in
                        String deleteQuery = "DELETE FROM Guest WHERE Username = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                            stmt.setString(1, loggedInUser);
                            int rows = stmt.executeUpdate();
                            if (rows > 0) {
                                System.out.println("Your account has been deleted successfully.");
                                System.exit(0); // Exit the program 
                            } else {
                                System.out.println("Error deleting your account.");
                            }
                        } catch (SQLException e) {
                            System.out.println("Error during account deletion: " + e.getMessage());
                        }
                    } else if ("no".equals(confirmation)) {
                        System.out.println("Account deletion canceled.");
                    } else {
                        System.out.println("Invalid response. Please enter 'yes' or 'no'.");
                    }
                    break;


                case 3:
                    System.out.println("Returning to Main Menu...");
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } catch (SQLException e) {
            System.out.println("Error managing guests: " + e.getMessage());
        }
    }
//initializing viewrooms method and getting needed data form the database
    private static void viewRooms() {
        String query = "SELECT roomNumber, roomType, price, isAvailable FROM Rooms";
//displaying the get data from the database with a design format
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("+----------------------------------------------------------+");
            System.out.println("| Room Number | Room Type    | Price   | Availability      |");
            System.out.println("+----------------------------------------------------------+");
            
            while (rs.next()) {
                int roomNumber = rs.getInt("roomNumber");
                String roomType = rs.getString("roomType");
                
                
                double price = rs.getDouble("price");

                
                String availability = rs.getString("isAvailable");
                
                
                System.out.printf("| %11d | %-12s | %7.2f | %-15s |%n", roomNumber, roomType, price, availability);
            }

            System.out.println("+----------------------------------------------------------+");
        } catch (SQLException e) {
            System.out.println("Error retrieving rooms: " + e.getMessage());
        }
    }
}

