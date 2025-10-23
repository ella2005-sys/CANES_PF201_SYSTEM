
package MAIN;


import CONFIG.config;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class main {
    // ==== VIEW METHODS ====
    public static void viewCustomers() {
        String sql = "SELECT * FROM tbl_customers";
        String[] headers = {"ID", "Name", "Number", "Address"};
        String[] cols = {"c_id", "c_name", "c_num", "c_address"};
        config con = new config();
        con.viewRecords(sql, headers, cols);
    }

    public static void viewProviders() {
        String sql = "SELECT * FROM tbl_providers";
        String[] headers = {"ID", "Name", "Number", "Skills", "Status"};
        String[] cols = {"p_id", "p_name", "p_cnum", "p_skill", "p_status"};
        config con = new config();
        con.viewRecords(sql, headers, cols);
    }

     public static void viewAppointments() {
        String sql = "SELECT * FROM tbl_appointments";
        String[] headers = {"ID", "Customer ID", "Provider ID", "Date", "Status"};
        String[] cols = {"a_id", "customer_id", "provider_id", "app_date", "app_status"};
        config con = new config();
        con.viewRecords(sql, headers, cols);
    }



    // ==== MAIN METHOD ====
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

                        if (stat.equalsIgnoreCase("Pending")) {
                            System.out.println("Account is Pending. Contact the Admin!");
                        } else {
                            System.out.println("\n LOGIN SUCCESS! Welcome, " + user.get("u_name"));
                            if (type.equalsIgnoreCase("Admin")) {
                                adminDashboard(con, sc);
                            } else if (type.equalsIgnoreCase("Customer")) {
                                customerDashboard(con, sc);
                            } else if (type.equalsIgnoreCase("Provider")) {
                                providerDashboard(con, sc);
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

    // Insert into tbl_users
    String insert = "INSERT INTO tbl_users(u_name, u_email, u_type, u_status, u_pass) VALUES (?, ?, ?, ?, ?)";
    con.addRecord(insert, name, email, tp, "Pending", hashed);

    if (tp.equalsIgnoreCase("Customer")) {
        // Insert into tbl_customers
        sc.nextLine(); // consume leftover newline
        System.out.print("Enter Contact Number: ");
        String number = sc.nextLine();
        System.out.print("Enter Address: ");
        String address = sc.nextLine();

        String addCustomer = "INSERT INTO tbl_customers(c_name, c_num, c_address) VALUES (?, ?, ?)";
        con.addRecord(addCustomer, name, number, address);

        System.out.println("Customer profile added successfully!");
    }

    if (tp.equalsIgnoreCase("Provider")) {
        // Insert into tbl_providers
        sc.nextLine(); // consume leftover newline
        System.out.print("Enter Contact Number: ");
        String contact = sc.nextLine();
        System.out.print("Enter Skill/Service Offered: ");
        String skill = sc.nextLine();

        String addProvider = "INSERT INTO tbl_providers(p_name, p_cnum, p_skill, p_status) VALUES (?, ?, ?, ?)";
        con.addRecord(addProvider, name, contact, skill, "Available");

        System.out.println("Provider profile added successfully!");
    }

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

    // ==== ADMIN DASHBOARD ====
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
                    viewAppointments();
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

    // ==== CUSTOMER DASHBOARD ====
    public static void customerDashboard(config con, Scanner inp) {
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
                    String sql = "INSERT INTO tbl_appointments(provider_id, app_date, app_status) VALUES (?, ?, ?)";
                    con.addRecord(sql, providerId, date, "Pending");
                    System.out.println("Appointment booked successfully!");
                    break;

                case 2:
                    viewAppointments();
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

    // ==== PROVIDER DASHBOARD ====
    public static void providerDashboard(config con, Scanner inp) {
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
                    viewAppointments();
                    break;

                case 2:
                    viewAppointments();
                    System.out.print("Enter Appointment ID to update: ");
                    int appId = inp.nextInt();
                    inp.nextLine();
                    System.out.print("Enter new status (Approved/Cancelled/Done): ");
                    String stat = inp.nextLine();
                    String sql = "UPDATE tbl_appointments SET app_status=? WHERE a_id=?";
                    con.updateRecord(sql, stat, appId);
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