package MAIN;


import CONFIG.config;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class main {

    // ==== HELPER METHOD FOR DISPLAYING FILTERED DATA ====
    /**
     * Prints records from a List<Map<String, Object>> result set 
     * in a table format, mimicking config.viewRecords().
     * This method is necessary because config.viewRecords cannot accept parameters.
     */
    public static void printRecords(List<Map<String, Object>> result, String[] headers, String[] cols) {
        if (result.isEmpty()) {
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("No records found.");
            System.out.println("--------------------------------------------------------------------------------");
            return;
        }

        // The formatting logic is copied from your config.viewRecords for consistency.
        
        // Print the headers dynamically
        StringBuilder headerLine = new StringBuilder();
        headerLine.append("--------------------------------------------------------------------------------\n| ");
        for (String header : headers) {
            headerLine.append(String.format("%-20s | ", header)); // Adjust formatting as needed
        }
        headerLine.append("\n--------------------------------------------------------------------------------");

        System.out.println(headerLine.toString());

        // Print the rows dynamically based on the provided column names
        for (Map<String, Object> row : result) {
            StringBuilder dataRow = new StringBuilder("| ");
            for (String colName : cols) {
                // Fetch the column value by name from the Map
                String value = row.get(colName) != null ? row.get(colName).toString() : "";
                dataRow.append(String.format("%-20s | ", value)); // Adjust formatting
            }
            System.out.println(dataRow.toString());
        }
        System.out.println("--------------------------------------------------------------------------------");
    }
    // ====================================================

    // ==== VIEW METHODS (UPDATED for filtered view) ====

    public static void viewCustomers() {
        String sql = "SELECT * FROM tbl_users WHERE u_type = 'Customer'";
        String[] headers = {"ID", "Name", "Email", "Role", "Status"};
        String[] cols = {"u_id", "u_name", "u_email", "u_type", "u_status"};
        config con = new config();
        // Uses the original config.viewRecords since no filtering is needed
        con.viewRecords(sql, headers, cols);
    }


    public static void viewProviders() {
        String sql = "SELECT u_id, u_name, u_number, u_skill, u_status FROM tbl_users WHERE u_type = 'Provider' AND u_status = 'Approved'";
        String[] headers = {"ID", "Name", "Number", "Skills", "Status"};
        String[] cols = {"u_id", "u_name", "u_number", "u_skill", "u_status"};
        config con = new config();
        // Uses the original config.viewRecords since no filtering is needed
        con.viewRecords(sql, headers, cols);
    }

    // Global view of all appointments (Used by Admin)
    public static void viewAppointments() {
        String sql = "SELECT a.a_id, c.u_name AS customer_name, p.u_name AS provider_name, a.app_date, a.app_status " +
                     "FROM tbl_appointments a " +
                     "JOIN tbl_users c ON a.customer_id = c.u_id " +
                     "JOIN tbl_users p ON a.provider_id = p.u_id";

        String[] Appointmentsheaders = {"Appointment ID", "Customer name", "Provider name", "Date", "Status"};
        String[] Appointmentscols = {"a_id", "customer_name", "provider_name", "app_date", "app_status"};

        config con = new config();
        // Uses the original config.viewRecords
        con.viewRecords(sql, Appointmentsheaders, Appointmentscols);
    }

    /**
     * NEW METHOD: Displays appointments filtered by the logged-in user.
     * Uses con.fetchRecords() (which handles parameters) and the printRecords helper.
     */
    public static void viewMyAppointments(config con, String userId, boolean isProvider) {
        String sql = "SELECT a.a_id, c.u_name AS customer_name, p.u_name AS provider_name, a.app_date, a.app_status " +
                     "FROM tbl_appointments a " +
                     "JOIN tbl_users c ON a.customer_id = c.u_id " +
                     "JOIN tbl_users p ON a.provider_id = p.u_id " +
                     "WHERE " + (isProvider ? "a.provider_id = ?" : "a.customer_id = ?");

        String[] Appointmentsheaders = {"Appointment ID", "Customer name", "Provider name", "Date", "Status"};
        String[] Appointmentscols = {"a_id", "customer_name", "provider_name", "app_date", "app_status"};
        
        // Use fetchRecords to securely execute the parameterized query
        List<Map<String, Object>> filteredResult = con.fetchRecords(sql, userId); 
        
        // Print the result using the custom helper method
        printRecords(filteredResult, Appointmentsheaders, Appointmentscols);
    }

    // ==== MAIN METHOD (Provider Dashboard call is updated) ====
    public static void main(String[] args) {
        config con = new config();
        con.connectDB();
        Scanner sc = new Scanner(System.in);
        int choice;
        char cont;

        do {
            System.out.println("===== HOME SERVICE APPOINTMENT LOGIN SYSTEM =====");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");
            choice = sc.nextInt();

            switch (choice) {
                case 1:
                    System.out.print("Enter Email: ");
                    String em = sc.next();
                    System.out.print("Enter Password: ");
                    String pas = sc.next();
                    String hashedPass = config.hashPassword(pas);

                    String qry = "SELECT * FROM tbl_users WHERE u_email = ? AND u_pass = ?";
                    List<Map<String, Object>> result = con.fetchRecords(qry, em, hashedPass);

                    if (result.isEmpty()) {
                        System.out.println("INVALID CREDENTIALS!");
                    } else {
                        Map<String, Object> user = result.get(0);
                        String stat = user.get("u_status").toString();
                        String type = user.get("u_type").toString();
                        String id = user.get("u_id").toString();

                        if (stat.equalsIgnoreCase("Pending")) {
                            System.out.println("Account is Pending. Contact the Admin!");
                        } else {
                            System.out.println("\n LOGIN SUCCESS! Welcome, " + user.get("u_name"));
                            if (type.equalsIgnoreCase("Admin")) {
                                adminDashboard(con, sc);
                            } else if (type.equalsIgnoreCase("Customer")) {
                                customerDashboard(con, sc, id);
                            } else if (type.equalsIgnoreCase("Provider")) {
                                // PASS THE PROVIDER ID
                                providerDashboard(con, sc, id); 
                            }
                        }
                    }
                    break;

                case 2:
                    System.out.print("Enter Name: ");
                    String name = sc.next();
                    System.out.print("Enter Email: ");
                    String email = sc.next();

                    while (true) {
                        String checkEmail = "SELECT * FROM tbl_users WHERE u_email = ?";
                        List<Map<String, Object>> exists = con.fetchRecords(checkEmail, email);
                        if (exists.isEmpty()) break;
                        System.out.print("Email already exists, enter another: ");
                        email = sc.next();
                    }

                    System.out.print("Enter User Type (1 - Admin / 2 - Customer / 3 - Provider): ");
                    int t = sc.nextInt();
                    String tp = (t == 1) ? "Admin" : (t == 2) ? "Customer" : "Provider";

                    System.out.print("Enter Password: ");
                    String pass = sc.next();
                    String hashed = config.hashPassword(pass);

                    // --- REFACTORED REGISTRATION (UNCHANGED) ---
                    String insertUser = "INSERT INTO tbl_users(u_name, u_email, u_type, u_status, u_pass) VALUES (?, ?, ?, ?, ?)";
                    con.addRecord(insertUser, name, email, tp, "Pending", hashed);

                    String getUserIdQry = "SELECT u_id FROM tbl_users WHERE u_email = ? ORDER BY u_id DESC LIMIT 1";
                    List<Map<String, Object>> userResult = con.fetchRecords(getUserIdQry, email);
                    String newUserId = userResult.get(0).get("u_id").toString();

                    sc.nextLine(); // consume leftover newline

                    if (tp.equalsIgnoreCase("Customer")) {
                        System.out.print("Enter Contact Number: ");
                        String number = sc.nextLine();
                        System.out.print("Enter Address: ");
                        String address = sc.nextLine();

                        String updateCustomer = "UPDATE tbl_users SET u_number = ?, u_address = ? WHERE u_id = ?";
                        con.updateRecord(updateCustomer, number, address, newUserId);
                        
                        System.out.println("Customer profile added successfully!");
                    }

                    if (tp.equalsIgnoreCase("Provider")) {
                        System.out.print("Enter Contact Number: ");
                        String contact = sc.nextLine();
                        System.out.print("Enter Skill/Service Offered: ");
                        String skill = sc.nextLine();

                        String updateProvider = "UPDATE tbl_users SET u_number = ?, u_skill = ?, u_status = 'Available' WHERE u_id = ?";
                        con.updateRecord(updateProvider, contact, skill, newUserId);

                        System.out.println("Provider profile added successfully!");
                    }
                    // --- END REFACTORED REGISTRATION ---

                    System.out.println("Registration successful! Wait for Admin approval.");
                    break;

                case 3:
                    System.out.println("Exiting system...");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid choice!");
            }

            System.out.print("\nReturn to login menu? (Y/N): ");
            cont = sc.next().charAt(0);

        } while (cont == 'Y' || cont == 'y');

        sc.close();
    }

    // ==== DASHBOARDS (Updated Appointment View Calls) ====
    
    // Admin Dashboard (UNCHANGED)
    public static void adminDashboard(config con, Scanner inp) {
        char cont;
        do {
            System.out.println("\n===== ADMIN DASHBOARD =====");
            System.out.println("1. Approve Users");
            System.out.println("2. View Users");
            System.out.println("3. Appointments");
            System.out.println("4. Logout");
            System.out.print("Enter choice: ");
            int act = inp.nextInt();

            switch (act) {
                case 1:
                    String query = "SELECT * FROM tbl_users";
                    String[] h = {"ID", "Name", "Email", "Role", "Status"};
                    String[] c = {"u_id", "u_name", "u_email", "u_type", "u_status"};
                    con.viewRecords(query, h, c);
                    System.out.print("Enter user ID to approve: ");
                    int id = inp.nextInt();
                    String sql = "UPDATE tbl_users SET u_status=? WHERE u_id=?";
                    con.updateRecord(sql, "Approved", id);
                    System.out.println("User approved successfully!");
                    break;

                case 2:
                    String vquery = "SELECT * FROM tbl_users";
                    String[] vh = {"ID", "Name", "Email", "Role", "Status"};
                    String[] vc = {"u_id", "u_name", "u_email", "u_type", "u_status"};
                    con.viewRecords(vquery, vh, vc);
                    break;

                case 3:
                    viewAppointments(); // Admin uses the global view 
                    break;

                case 4:
                    System.out.println("Logging out...");
                    return;

                default:
                    System.out.println("Invalid choice!");
            }

            System.out.print("\nReturn to Admin Dashboard? (Y/N): ");
            cont = inp.next().charAt(0);
        } while (cont == 'Y' || cont == 'y');
    }

    // Customer Dashboard
    public static void customerDashboard(config con, Scanner inp, String id) {
        char cont;
        do {
            System.out.println("\n===== CUSTOMER DASHBOARD =====");
            System.out.println("1. Book Appointment");
            System.out.println("2. View My Appointments");
            System.out.println("3. View Providers");
            System.out.println("4. Logout");
            System.out.print("Enter choice: ");
            int action = inp.nextInt();

            switch (action) {
                case 1:
                    viewProviders();
                    System.out.print("Enter Provider ID: ");
                    int providerId = inp.nextInt();
                    inp.nextLine();
                    System.out.print("Enter Appointment Date (YYYY-MM-DD): ");
                    String date = inp.nextLine();
                    
                    String sql = "INSERT INTO tbl_appointments(customer_id, provider_id, app_date, app_status) VALUES (?, ?, ?, ?)";
                    con.addRecord(sql,id, providerId, date, "Pending");
                    System.out.println("Appointment booked successfully!");
                    break;

                case 2:
                    // Use the filtered method for the customer
                    viewMyAppointments(con, id, false); 
                    break;

                case 3:
                    viewProviders();
                    break;

                case 4:
                    System.out.println("Logging out...");
                    return;

                default:
                    System.out.println("Invalid choice!");
            }

            System.out.print("\nReturn to Customer Dashboard? (Y/N): ");
            cont = inp.next().charAt(0);
        } while (cont == 'Y' || cont == 'y');
    }

    // Provider Dashboard
    public static void providerDashboard(config con, Scanner inp, String providerId) {
        char cont;
        do {
            System.out.println("\n===== PROVIDER DASHBOARD =====");
            System.out.println("1. View Appointments");
            System.out.println("2. Update Appointment Status");
            System.out.println("3. Logout");
            System.out.print("Enter choice: ");
            int action = inp.nextInt();

            switch (action) {
                case 1:
                    // Use the filtered method for the provider
                    viewMyAppointments(con, providerId, true); 
                    break;

                case 2:
                    // Show filtered view before update
                    viewMyAppointments(con, providerId, true); 
                    System.out.print("Enter Appointment ID to update: ");
                    int appId = inp.nextInt();
                    inp.nextLine();
                    System.out.print("Enter new status (Approved/Cancelled/Done): ");
                    String stat = inp.nextLine();
                    
                    // Secure update: ensure only this provider's appointments are updated
                    String updateSql = "UPDATE tbl_appointments SET app_status=? WHERE a_id=? AND provider_id=?";
                    con.updateRecord(updateSql, stat, appId, providerId);
                    System.out.println("Status updated!");
                    break;

                case 3:
                    System.out.println("Logging out...");
                    return;

                default:
                    System.out.println("Invalid choice!");
            }

            System.out.print("\nReturn to Provider Dashboard? (Y/N): ");
            cont = inp.next().charAt(0);
        } while (cont == 'Y' || cont == 'y');
    }
}