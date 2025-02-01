package hotelmanagementSystem;
import java.sql.*;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
// creating an interface class with its method
public interface Reservation {
    void bookARoom();
    void checkout();
}
//creating a class that implements the interface
class Booking implements Reservation {
    private static Connection conn;// connecting to the databse
    private Scanner scanner;
    private LoginSystem loginSystem;
// constructor
    public Booking(Connection conn, Scanner scanner, LoginSystem loginSystem) {
        this.conn = conn;
        this.scanner = scanner;
        this.loginSystem = loginSystem;
    }

   //initializing of isroomavailable method to check the availability of the room number and type from the database
    //ignoring the case sensitivity
    private boolean isRoomAvailable(int roomNumber, String roomType) {
        String query = "SELECT isAvailable FROM Rooms WHERE roomNumber = ? AND roomType = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, roomNumber);
            stmt.setString(2, roomType);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return "available".equalsIgnoreCase(rs.getString("isAvailable"));
                }
                System.out.println("Room number " + roomNumber + " for " + roomType + " does not exist.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error checking room availability: " + e.getMessage());
        }
        return false;
    }
  // initialization of the interface method that can be override
    @Override
    public void bookARoom() {
        if (loginSystem.getLoggedInUser() == null) {// checking if there's a logged in user
            System.out.println("You must log in first.");
            return;
        }

        try {
        	//checking of discount eligibility based on the logged in user customertype from the database
            System.out.println("Checking for discount eligibility...");
            System.out.println();
            
            String customerType = getCustomerType();
            if (customerType == null) return;
            
            double discountRate = getDiscountRate(customerType);
            displayDiscountEligibility(customerType, discountRate);
            
            double promoDiscountPercentage = processPromo();

            System.out.print("Enter the number of days you plan to stay: ");
            int numberOfDays = scanner.nextInt();
            scanner.nextLine();
            
            Map<String, Double> roomBookings = processRoomBookings(numberOfDays);
            
            double totalPrice = calculateTotalPrice(roomBookings, numberOfDays);
            double totalDiscount = totalPrice * promoDiscountPercentage / 100.0;
            double promoDiscount = totalPrice * discountRate;
            double finalPrice = totalPrice - promoDiscount - totalDiscount;
            
            printBookingSummary(roomBookings, numberOfDays, totalPrice, totalDiscount, promoDiscount, finalPrice);
            
            if (confirmBooking()) {
                java.sql.Date checkInDate = getCheckInDate();
                if (checkInDate == null) return;

                if (saveBooking(roomBookings, totalDiscount, numberOfDays, finalPrice, checkInDate)) {
                    updateRoomAvailability(roomBookings);
                    System.out.println("Booking confirmed and room availability updated.\n");
                    handlePayment(finalPrice);
                }
            } else {
                System.out.println("Booking cancelled.\n");
            }

        } catch (SQLException e) {
            System.out.println("Error during booking process: " + e.getMessage());
        }
    }
    
    private boolean isUserLoggedIn() {
        return loginSystem.getLoggedInUser() != null;
    }

    private String getCustomerType() throws SQLException {
        String query = "SELECT CustomerType FROM Guest WHERE Username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, loginSystem.getLoggedInUser());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("CustomerType");
                } else {
                    System.out.println("Error: Customer details not found.");
                    return null;
                }
            }
        }
    }
    
    public double calculateTotalPrice(Map<String, Double> roomBookings, int numberOfDays) {
        double totalPrice = 0.0;

        for (double roomPrice : roomBookings.values()) {
            totalPrice += roomPrice; // Sum up room prices
        }

        return totalPrice * numberOfDays; // Multiply by the number of days
    }

    
    private void displayDiscountEligibility(String customerType, double discountRate) {
        if (discountRate > 0) {
            System.out.println("You are eligible for a discount as a " + customerType);
            System.out.println("Discount rate: " + (discountRate * 100) + "%.\n");
        } else {
            System.out.println("You are not eligible for any discounts.\n");
        }
    }
    
    private double processPromo() {
        System.out.print("Do you want to avail a promo? (yes/no): ");
        String response = scanner.nextLine().trim().toLowerCase();

        if (!response.equals("yes")) return 0.0;

        viewPromos();
        System.out.print("Enter the promo name you want to avail: ");
        String promoName = scanner.nextLine().trim();

        Promo promo = getPromoDetails(promoName);
        if (promo == null) {
            System.out.println("Invalid promo name or no such promo available.");
            return 0.0;
        }

        java.sql.Date checkInDate = getCheckInDate();
        if (checkInDate == null) return 0.0;

        if (isPromoValid(promo, checkInDate)) {
            System.out.println("Promo applied: " + promoName + " with " + promo.getDiscountPercentage() + "% discount.");
            return promo.getDiscountPercentage() * 100.0;
        } else {
            System.out.println("The promo is not valid for your selected check-in date.");
            return 0.0;
        }
    }
    
    private java.sql.Date getCheckInDate() {
        System.out.print("Enter check-in date (YYYY-MM-DD): ");
        String input = scanner.nextLine();

        if (input.isEmpty()) {
            System.out.println("Check-in date cannot be empty.");
            return null;
        }

        try {
            return java.sql.Date.valueOf(input);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid date format. Please try again.");
            return null;
        }
    }
    
    private boolean isPromoValid(Promo promo, java.sql.Date checkInDate) {
        return checkInDate.after(promo.getValidFrom()) && checkInDate.before(promo.getValidUntil());
    }

    private Map<String, Double> processRoomBookings(int numberOfDays) throws SQLException {
        Map<String, Double> roomBookings = new HashMap<>();

        while (true) {
            System.out.print("Enter room type or 'done' to finish: ");
            String roomType = scanner.nextLine();

            if (roomType.equalsIgnoreCase("done")) break;

            if (!isValidRoomType(roomType)) {
                System.out.println("Invalid room type. Please try again.");
                continue;
            }

            System.out.print("Enter the room number you want to book for " + roomType + ": ");
            int roomNumber = scanner.nextInt();
            scanner.nextLine();

            if (!isRoomAvailable(roomNumber, roomType)) {
                System.out.println("Room number " + roomNumber + " is not available.");
                continue;
            }

            double roomPrice = getRoomPrice(roomType);
            double calculatedPrice = roomPrice * numberOfDays;
            roomBookings.put(roomType + " (Room# " + roomNumber + ")", roomPrice);

            System.out.println("Room number " + roomNumber + " for " + roomType + " booked successfully.\n");
        }

        return roomBookings;
    }
    
    private boolean confirmBooking() {
        System.out.print("Do you want to confirm the booking? (yes/no): ");
        String response = scanner.nextLine();
        return response.equalsIgnoreCase("yes");
    }

 
    public class HotelManagementSystem {

        // Existing fields
        private static Connection conn;
        private static Scanner scanner;

        // Other methods related to booking, saving, etc.

        // Place this method in the same class
        private Promo getPromoDetails(String promoName) {
            Promo promo = null;
            String query = "SELECT promo_name, discount_percentage, valid_from, valid_until FROM Promos WHERE promo_name = ?";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, promoName);  // Set the promo name entered by the user

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // Extract the promo details from the result set
                        String name = rs.getString("promo_name");
                        double discount = rs.getDouble("discount_percentage");
                        java.sql.Date validFrom = rs.getDate("valid_from");
                        java.sql.Date validUntil = rs.getDate("valid_until");

                        // Create a new Promo object with the retrieved data
                        promo = new Promo(name, discount, validFrom, validUntil);
                    }
                }
            } catch (SQLException e) {
                System.out.println("Error retrieving promo details: " + e.getMessage());
            }

            return promo;  
        }

       
    }

    
    //initializing viewpromos method
    private static void viewPromos() {
    	// checking database promo table
        String query = "SELECT promo_name, discount_percentage, description, valid_from, valid_until FROM Promos";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("+-------------------------------------------------------------------------------------------------------------------------------------------------------+");
            System.out.println("| Promo Name          | Discount (%) |                                   Description                                 |   Valid From   |  Valid Until   |");
            System.out.println("+-------------------------------------------------------------------------------------------------------------------------------------------------------+");
            
            
            while (rs.next()) {
            	//getting the data on the promo table
                String promoName = rs.getString("promo_name");
                Float discount = rs.getFloat("discount_percentage");
                String description = rs.getString("description");
                Date valid_from = rs.getDate("valid_from");
                Date valid_until = rs.getDate("valid_until");
                //displaying the get data from the database with design format
                System.out.printf("| %-19s | %-12.2f | %-77s | %-14s |  %-14s |%n",
                                  promoName, discount, description, valid_from, valid_until);
            }
            System.out.println("+-------------------------------------------------------------------------------------------------------------------------------------------------------+");
        } catch (SQLException e) {
            System.out.println("Error retrieving promos: " + e.getMessage());
        }
    }

    
    private Promo getPromoDetails(String promoName) {
        // Logic to fetch promo details from database
        Promo promo = null;

        String promoQuery = "SELECT promo_name, discount_percentage, valid_from, valid_until FROM Promos WHERE promo_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(promoQuery)) {
            stmt.setString(1, promoName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    promo = new Promo(
                        rs.getString("promo_name"),
                        rs.getDouble("discount_percentage") / 100, // Convert to decimal
                        rs.getDate("valid_from"),
                        rs.getDate("valid_until")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching promo details: " + e.getMessage());
        }

        return promo;
    }

    
    private void handlePayment(double finalPrice) {
        System.out.println("+--------------------------------+");
        System.out.println("|        Payment Options         |");
        System.out.println("+--------------------------------+");
        System.out.println("| 1. Bank Card                   |");
        System.out.println("| 2. Online Wallet               |");
        System.out.println("| 3. Cash on Check-in Date       |");
        System.out.println("+--------------------------------+");
        System.out.print("Choose your payment method (1-3): ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        switch (choice) {
            case 1:
                handleBankCardPayment(finalPrice);
                break;
            case 2:
                handleOnlineWalletPayment(finalPrice);
                break;
            case 3:
                System.out.println("You have chosen to pay with cash on the check-in date.");
                System.out.println("Please make sure to bring the exact amount: $" + finalPrice);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
                handlePayment(finalPrice);
                break;
        }
    }
    
    private void handleBankCardPayment(double finalPrice) {
        System.out.println("Please enter your bank card details:");
        System.out.print("Card Number: ");
        String cardNumber = scanner.nextLine();
        System.out.print("Expiration Date (MM/YY): ");
        String expirationDate = scanner.nextLine();
        System.out.print("CVV: ");
        String cvv = scanner.nextLine();

        // Simulate payment processing
        System.out.println("Processing payment...");
        System.out.println("Payment of $" + finalPrice + " completed successfully using Bank Card.");
    }

    private void handleOnlineWalletPayment(double finalPrice) {
        System.out.println("Please choose your online wallet:");
        System.out.println("1. Gcash");
        System.out.println("2. Maya");
        System.out.println("3. Paypal");
        System.out.print("Enter your choice: ");

        int walletChoice = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        System.out.print("Enter your wallet account number: ");
        String walletID = scanner.nextLine();

        // Simulate payment processing
        System.out.println("Processing payment...");
        System.out.println("Payment of $" + finalPrice + " completed successfully using Online Wallet.");
    }


    //displaying the booking summary with its calculation with a design format
    private void printBookingSummary(Map<String, Double> roomBookings, double numberOfDays, double totalPrice, double promoDiscount, double totalDiscount, double finalPrice) {
        System.out.println("+--------------------------------------------------+");
        System.out.printf("| %-30s | %-15s |\n", "Description", "Amount ($)");
        System.out.println("+--------------------------------------------------+");
        
        // getting the details store on map
        for (Map.Entry<String, Double> entry : roomBookings.entrySet()) {
            String roomDescription = entry.getKey();
            double roomPrice = entry.getValue();
            System.out.printf("| %-30s | $%-14.2f |\n", roomDescription, roomPrice);
        }
        
        System.out.println("+--------------------------------------------------+");
        System.out.printf("| %-30s | %-14d |\n", "Days of staying", (int)numberOfDays);
        System.out.println("+--------------------------------------------------+");
        
        System.out.printf("| %-30s | $%-14.2f |\n", "Total Price", totalPrice);
        System.out.printf("| %-30s | $%-14.2f |\n", "Promo Discount ", promoDiscount);
        System.out.printf("| %-30s | $%-14.2f |\n", "Total Discount ", totalDiscount);
        System.out.printf("| %-30s | $%-14.2f |\n", "Final Price to Pay", finalPrice);
        System.out.println("+--------------------------------------------------+");
    }
    
   
    //creation getdiscount rate method for the specific rate per each customer type
    private double getDiscountRate(String customerType) {
        switch (customerType.toLowerCase()) {
            case "student": return 0.15; // 15% discount
            case "senior": return 0.20; // 20% discount
            case "pwd": return 0.15; // 15% discount
            default: return 0.0; // No discount for other types
        }
    }
    //creation of isvalidroomtype method for the roomtype entered existing on database
    private boolean isValidRoomType(String roomType) {
        roomType = roomType.trim().toLowerCase();
        String query = "SELECT roomNumber, isAvailable FROM Rooms WHERE LOWER(roomType) = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, roomType);

          //displaying the result get from the database with a design format
            try (ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                
                System.out.println("+----------------------------------------------+");
                System.out.printf("|%-10s| %-15s | %-10s |\n", "Room Number", "Room Type", " Availability ");
                System.out.println("+----------------------------------------------+");

                while (rs.next()) {
                    count++;
                    int roomNumber = rs.getInt("roomNumber");
                    String isAvailable = rs.getString("isAvailable");
                    System.out.printf("|%-10d |%-17s|%-16s|\n", roomNumber, roomType, isAvailable);
                }

                System.out.println("+----------------------------------------------+");
                System.out.println("Total rooms of type '" + roomType + "': " + count);
                return count > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error validating room type: " + e.getMessage());
        }
        return false;
    }

//getter of each roomtype price
    private double getRoomPrice(String roomType) {
        String query = "SELECT price FROM Rooms WHERE LOWER(roomType) = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, roomType.toLowerCase());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("price");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching room price: " + e.getMessage());
        }
        return 0.0;
    }
//creation of method on calculation of the discount per customer type that is store on the map
    private double calculateDiscount(String customerType, Map<String, Double> roomBookings, double totalPrice) {
        double discount = 0.0;
        switch (customerType.toLowerCase()) {
            case "senior": 
                discount += totalPrice * 0.20; 
                break;
            case "student": 
                discount += totalPrice * 0.15; 
                break;
            case "pwd": 
                discount += totalPrice * 0.15; 
                break;
        }

      

        return discount;
    }

//creation of the save booking method for the insertion and saving booking details on the database together with other details use map storage
    private boolean saveBooking(Map<String, Double> roomBookings, double discount, int numberOfDays, double totalPrice, java.sql.Date checkInDateInput) {
        String insertQuery = "INSERT INTO Bookings (date, username, roomType, room_number, check_in_date, day_of_staying, total_price) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            for (Map.Entry<String, Double> entry : roomBookings.entrySet()) {
                String roomDescription = entry.getKey();
                int roomNumber = extractRoomNumber(roomDescription);
                String roomType = extractRoomType(roomDescription);

               
             // Insert current date, check-in date, and other booking details into the database
                java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());  
                
                
                stmt.setDate(1, currentDate);  
                stmt.setString(2, loginSystem.getLoggedInUser()); 
                stmt.setString(3, roomType);  
                stmt.setInt(4, roomNumber);  
                stmt.setDate(5, checkInDateInput);  
                stmt.setInt(6, numberOfDays);  
                stmt.setDouble(7, totalPrice); 

                
                stmt.addBatch();
            }

            
            stmt.executeBatch();
            return true;  
        } catch (SQLException e) {
            System.out.println("Error saving booking: " + e.getMessage());
            return false;  
        }
    }

    //creation of checkout interface method that can be override
    @Override
    public void checkout() {
        System.out.print("Enter Room Number to check out: ");
        int roomNumber = scanner.nextInt();
        scanner.nextLine(); // consume the newline character

        // Prompt for confirmation before proceeding with the checkout
        System.out.print("Are you sure you want to check out? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (!confirmation.equals("yes")) {
            System.out.println("Checkout cancelled.");
            return; // Exit the method if the user doesn't confirm
        }

        // Checking if the room number entered by the user exists in the database
        String checkQuery = "SELECT isAvailable FROM Rooms WHERE roomNumber = ?";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, roomNumber);
            try (ResultSet rs = checkStmt.executeQuery()) {
                // If the room number is found and set to "Not Available", it's confirmed as booked
                if (rs.next() && "Not Available".equalsIgnoreCase(rs.getString("isAvailable"))) {
                    // Get the current date for checkout
                    Date checkOutDate = new Date(System.currentTimeMillis());

                    // Update the room availability to "Available" and set the checkout date in the bookings table
                    String updateRoomQuery = "UPDATE Rooms SET isAvailable = ? WHERE roomNumber = ?";
                    try (PreparedStatement updateRoomStmt = conn.prepareStatement(updateRoomQuery)) {
                        updateRoomStmt.setString(1, "Available");
                        updateRoomStmt.setInt(2, roomNumber);
                        updateRoomStmt.executeUpdate();
                    }

                    String updateBookingQuery = "UPDATE Bookings SET check_out_date = ? WHERE room_number = ?";
                    try (PreparedStatement updateBookingStmt = conn.prepareStatement(updateBookingQuery)) {
                        updateBookingStmt.setDate(1, checkOutDate);
                        updateBookingStmt.setInt(2, roomNumber);
                        updateBookingStmt.executeUpdate();
                    }

                    System.out.println("Room " + roomNumber + " has been successfully checked out.");
                } else {
                    System.out.println("Room " + roomNumber + " is not currently booked.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error during checkout: " + e.getMessage());
        }
    }


    			
    		
    	
    

//creation of method for updating of room availability
    private void updateRoomAvailability(Map<String, Double> roomBookings) {
        String updateQuery = "UPDATE Rooms SET isAvailable = ? WHERE roomNumber = ? AND roomType = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            for (String roomDescription : roomBookings.keySet()) {
                int roomNumber = extractRoomNumber(roomDescription);
                String roomType = extractRoomType(roomDescription);

                
                stmt.setString(1, "Not Available"); 
                stmt.setInt(2, roomNumber);        
                stmt.setString(3, roomType);      
                stmt.executeUpdate();

               
            }
           
        } catch (SQLException e) {
            System.out.println("Error updating room availability: " + e.getMessage());
        }
    }


    private int extractRoomNumber(String roomDescription) {
        String[] parts = roomDescription.split("#");
        return Integer.parseInt(parts[1].trim().replace(")", ""));
    }

    private String extractRoomType(String roomDescription) {
        return roomDescription.split(" ")[0];
    }
}
