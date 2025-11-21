package MAIN;

import CONFIG.config;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class main {

    static Scanner sc = new Scanner(System.in);

    // =====================
    //   HELPER METHODS
    // =====================

    public static void printRecords(List<Map<String, Object>> result, String[] headers, String[] cols) {
        int[] colWidths = new int[headers.length];

        for (int i = 0; i < headers.length; i++) {
            colWidths[i] = headers[i].equalsIgnoreCase("Description") ? 50 : 20;
        }

        String separator = "+";
        for (int w : colWidths) separator += repeatChar('-', w + 3) + "+";

        if (result.isEmpty()) {
            System.out.println(separator);
            System.out.println("| No records found.");
            System.out.println(separator);
            return;
        }

        // Print header
        StringBuilder headerLine = new StringBuilder("| ");
        for (int i = 0; i < headers.length; i++) {
            headerLine.append(String.format("%-" + colWidths[i] + "s | ", headers[i]));
        }

        System.out.println(separator);
        System.out.println(headerLine);
        System.out.println(separator);

        // Print rows
        for (Map<String, Object> row : result) {
            String[][] lines = new String[headers.length][];
            int maxLines = 1;

            for (int i = 0; i < headers.length; i++) {
                String val = (row.get(cols[i]) != null) ? row.get(cols[i]).toString() : "";
                lines[i] = wrapText(val, colWidths[i]);
                maxLines = Math.max(maxLines, lines[i].length);
            }

            for (int lineIdx = 0; lineIdx < maxLines; lineIdx++) {
                StringBuilder rowLine = new StringBuilder("| ");
                for (int col = 0; col < headers.length; col++) {
                    String txt = lineIdx < lines[col].length ? lines[col][lineIdx] : "";
                    rowLine.append(String.format("%-" + colWidths[col] + "s | ", txt));
                }
                System.out.println(rowLine);
            }

            System.out.println(separator);
        }
    }

    private static String[] wrapText(String text, int width) {
        if (text.length() <= width) return new String[]{text};

        List<String> lines = new java.util.ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + width, text.length());
            lines.add(text.substring(start, end));
            start = end;
        }

        return lines.toArray(new String[0]);
    }

    private static String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append(c);
        return sb.toString();
    }

    // =====================
    //        USERS
    // =====================

    public static void viewUsers(config con) {
        String sql = "SELECT u_id, u_name, u_email, u_type, u_status FROM tbl_users";
        String[] headers = {"ID", "Name", "Email", "Type", "Status"};
        String[] cols = {"u_id", "u_name", "u_email", "u_type", "u_status"};
        printRecords(con.fetchRecords(sql), headers, cols);
    }

    // =====================
    //      SERVICES
    // =====================

    public static void viewServices(config con) {
        String sql =
                "SELECT s.s_id, s.s_name, s.s_desc, s.s_price, u.u_name AS provider " +
                "FROM tbl_services s LEFT JOIN tbl_users u ON s.provider_id = u.u_id";

        String[] headers = {"ID", "Name", "Description", "Price", "Provider"};
        String[] cols = {"s_id", "s_name", "s_desc", "s_price", "provider"};

        printRecords(con.fetchRecords(sql), headers, cols);
    }

    /**
     * Shows only the services that belong to the provided providerId.
     * providerId is expected to be the string representation of u_id (consistent with other code).
     */
    public static void viewServicesByProvider(config con, String providerId) {
        if (providerId == null || providerId.trim().isEmpty()) {
            System.out.println("No provider selected.");
            return;
        }

        String sql =
                "SELECT s.s_id, s.s_name, s.s_desc, s.s_price, u.u_name AS provider " +
                "FROM tbl_services s LEFT JOIN tbl_users u ON s.provider_id = u.u_id " +
                "WHERE s.provider_id = ?";

        List<Map<String, Object>> services = con.fetchRecords(sql, providerId);

        String[] headers = {"ID", "Name", "Description", "Price", "Provider"};
        String[] cols = {"s_id", "s_name", "s_desc", "s_price", "provider"};

        if (services.isEmpty()) {
            System.out.println("This provider currently offers no services.");
            return;
        }

        printRecords(services, headers, cols);
    }

    public static void addService(config con, String providerId) {
        System.out.print("Service Name: ");
        String name = sc.nextLine();

        System.out.print("Description: ");
        String desc = sc.nextLine();

        System.out.print("Price: ");
        double price = sc.nextDouble();
        sc.nextLine();

        if (providerId == null) {
            List<Map<String, Object>> providers = con.fetchRecords(
                    "SELECT u_id, u_name FROM tbl_users WHERE u_type='Provider'"
            );

            printRecords(providers, new String[]{"ID", "Name"}, new String[]{"u_id", "u_name"});

            System.out.print("Enter Provider ID for this service (or 0 for none): ");
            providerId = sc.nextLine();
            if (providerId.equals("0")) providerId = null;
        }

        if (providerId != null) {
            con.addRecord(
                    "INSERT INTO tbl_services(s_name, s_desc, s_price, provider_id) VALUES (?, ?, ?, ?)",
                    name, desc, price, providerId
            );
        } else {
            con.addRecord(
                    "INSERT INTO tbl_services(s_name, s_desc, s_price) VALUES (?, ?, ?)",
                    name, desc, price
            );
        }

        System.out.println("Service added successfully!");
    }

    public static void updateService(config con, String providerId) {
        String sqlFetch = (providerId != null)
                ? "SELECT * FROM tbl_services WHERE provider_id=?"
                : "SELECT * FROM tbl_services";

        List<Map<String, Object>> services =
                (providerId != null ? con.fetchRecords(sqlFetch, providerId) : con.fetchRecords(sqlFetch));

        String[] headers = {"ID", "Name", "Description", "Price"};
        String[] cols = {"s_id", "s_name", "s_desc", "s_price"};

        printRecords(services, headers, cols);

        if (services.isEmpty()) {
            System.out.println("No services to update.");
            return;
        }

        System.out.print("Enter Service ID to update: ");
        int sid = sc.nextInt();
        sc.nextLine();

        System.out.print("New Name: ");
        String name = sc.nextLine();

        System.out.print("New Description: ");
        String desc = sc.nextLine();

        System.out.print("New Price: ");
        double price = sc.nextDouble();
        sc.nextLine();

        String sqlUpdate = "UPDATE tbl_services SET s_name=?, s_desc=?, s_price=? WHERE s_id=?";
        if (providerId != null) {
            sqlUpdate += " AND provider_id=?";
            con.updateRecord(sqlUpdate, name, desc, price, sid, providerId);
        } else {
            con.updateRecord(sqlUpdate, name, desc, price, sid);
        }

        System.out.println("Service updated successfully!");
    }

    public static void deleteService(config con, String providerId) {
        String sqlFetch = (providerId != null)
                ? "SELECT * FROM tbl_services WHERE provider_id=?"
                : "SELECT * FROM tbl_services";

        List<Map<String, Object>> services =
                (providerId != null ? con.fetchRecords(sqlFetch, providerId) : con.fetchRecords(sqlFetch));

        String[] headers = {"ID", "Name", "Description", "Price"};
        String[] cols = {"s_id", "s_name", "s_desc", "s_price"};

        printRecords(services, headers, cols);

        if (services.isEmpty()) {
            System.out.println("No services to delete.");
            return;
        }

        System.out.print("Enter Service ID to delete: ");
        int sid = sc.nextInt();
        sc.nextLine();

        String sqlDelete = "DELETE FROM tbl_services WHERE s_id=?";
        if (providerId != null) {
            sqlDelete += " AND provider_id=?";
            con.deleteRecord(sqlDelete, sid, providerId);
        } else {
            con.deleteRecord(sqlDelete, sid);
        }

        System.out.println("Service deleted successfully!");
    }

    // =====================
    //     APPOINTMENTS
    // =====================

    private static void printAppointmentRecords(List<Map<String, Object>> result) {
        String separator = "----------------------------------------------------------------------------------------------------------------------------------------------------------------";

        if (result.isEmpty()) {
            System.out.println(separator);
            System.out.println("No records found.");
            System.out.println(separator);
            return;
        }

        String[] headers = {"ID", "Customer", "Provider", "Service", "Date", "Time", "Status"};

        System.out.println(separator);
        System.out.print("| ");
        for (String header : headers) {
            System.out.printf("%-20s | ", header);
        }
        System.out.println("\n" + separator);

        for (Map<String, Object> row : result) {

            String dateTime = row.get("app_date") != null ? row.get("app_date").toString() : "";
            String date = "", time = "N/A";

            if (dateTime.contains(" ")) {
                String[] parts = dateTime.split(" ", 2);
                date = parts[0];
                time = parts[1];
            } else {
                date = dateTime;
            }

            System.out.printf("| %-20s | %-20s | %-20s | %-20s | %-20s | %-20s | %-20s |%n",
                    row.get("a_id"),
                    row.get("customer"),
                    row.get("provider"),
                    row.get("service") != null ? row.get("service") : "",
                    date,
                    time,
                    row.get("app_status")
            );
        }

        System.out.println(separator);
    }

    public static void viewAppointments(config con) {
        String sql =
                "SELECT a.a_id, cu.u_name AS customer, pr.u_name AS provider, s.s_name AS service, a.app_date, a.app_status " +
                "FROM tbl_appointments a " +
                "JOIN tbl_users cu ON a.customer_id = cu.u_id " +
                "JOIN tbl_users pr ON a.provider_id = pr.u_id " +
                "LEFT JOIN tbl_services s ON a.service_id = s.s_id";

        printAppointmentRecords(con.fetchRecords(sql));
    }

    public static void viewMyAppointments(config con, String userId, boolean isProvider) {
        String sql =
                "SELECT a.a_id, cu.u_name AS customer, pr.u_name AS provider, s.s_name AS service, a.app_date, a.app_status " +
                "FROM tbl_appointments a " +
                "JOIN tbl_users cu ON a.customer_id = cu.u_id " +
                "JOIN tbl_users pr ON a.provider_id = pr.u_id " +
                "LEFT JOIN tbl_services s ON a.service_id = s.s_id " +
                "WHERE " + (isProvider ? "a.provider_id = ?" : "a.customer_id = ?");

        printAppointmentRecords(con.fetchRecords(sql, userId));
    }

    // =====================
    //     ADMIN PANEL
    // =====================

    public static void adminDashboard(config con) {
        char cont;

        do {
            System.out.println("\n===== ADMIN DASHBOARD =====");
            System.out.println("1. Approve Users");
            System.out.println("2. View Users");
            System.out.println("3. View Appointments");
            System.out.println("4. Manage Services");
            System.out.println("5. Logout");
            System.out.print("Enter choice: ");

            int act = sc.nextInt();
            sc.nextLine();

            switch (act) {
                case 1:
                    viewUsers(con);
                    System.out.print("Enter User ID to approve: ");
                    int id = sc.nextInt();
                    sc.nextLine();
                    con.updateRecord("UPDATE tbl_users SET u_status=? WHERE u_id=?", "Approved", id);
                    System.out.println("User approved successfully!");
                    break;

                case 2:
                    viewUsers(con);
                    break;

                case 3:
                    viewAppointments(con);
                    break;

                case 4:
                    System.out.println("1. View Services");
                    System.out.println("2. Add Service");
                    System.out.println("3. Update Service");
                    System.out.println("4. Delete Service");
                    System.out.print("Choice: ");

                    int sChoice = sc.nextInt();
                    sc.nextLine();

                    switch (sChoice) {
                        case 1: viewServices(con); break;
                        case 2: addService(con, null); break;
                        case 3: updateService(con, null); break;
                        case 4: deleteService(con, null); break;
                        default: System.out.println("Invalid choice!");
                    }
                    break;

                case 5:
                    return;

                default:
                    System.out.println("Invalid choice!");
            }

            System.out.print("\nReturn to Admin Dashboard? (Y/N): ");
            cont = sc.next().charAt(0);

        } while (cont == 'Y' || cont == 'y');
    }

    // =====================
    //   CUSTOMER PANEL
    // =====================

    public static void customerDashboard(config con, String id) {
        char cont;

        do {
            System.out.println("\n===== CUSTOMER DASHBOARD =====");
            System.out.println("1. Book Appointment");
            System.out.println("2. View My Appointments");
            System.out.println("3. View Providers");
            System.out.println("4. Logout");
            System.out.print("Enter choice: ");

            int action = sc.nextInt();
            sc.nextLine();

            String sqlProv =
                    "SELECT u_id, u_name, u_skill " +
                    "FROM tbl_users WHERE u_type='Provider' " +
                    "AND (u_status='Available' OR u_status='Approved')";

            List<Map<String, Object>> providers;

            switch (action) {
                case 1:
                    providers = con.fetchRecords(sqlProv);
                    printRecords(providers, new String[]{"ID", "Name", "Skill"}, new String[]{"u_id", "u_name", "u_skill"});

                    if (providers.isEmpty()) {
                        System.out.println("Cannot book: No providers available.");
                        break;
                    }

                    System.out.print("Enter Provider ID: ");
                    String providerId = sc.next();
                    sc.nextLine();

                    // show only this provider's services (segregated)
                    viewServicesByProvider(con, providerId);

                    System.out.print("Enter Service ID (or 0 for none): ");
                    int serviceId = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Enter Appointment Date (YYYY-MM-DD): ");
                    String date = sc.nextLine();

                    System.out.print("Enter Appointment Time (HH:MM AM/PM): ");
                    String time = sc.nextLine();

                    String dateTime = date + " " + time;

                    con.addRecord(
                            "INSERT INTO tbl_appointments(customer_id, provider_id, service_id, app_date, app_status) VALUES (?, ?, ?, ?, ?)",
                            id, providerId,
                            (serviceId == 0 ? null : serviceId),
                            dateTime, "Pending"
                    );

                    System.out.println("Appointment booked successfully!");
                    break;

                case 2:
                    viewMyAppointments(con, id, false);
                    break;

                case 3:
                    providers = con.fetchRecords(sqlProv);
                    printRecords(providers, new String[]{"ID", "Name", "Skill"}, new String[]{"u_id", "u_name", "u_skill"});
                    break;

                case 4:
                    return;

                default:
                    System.out.println("Invalid choice!");
            }

            System.out.print("\nReturn to Customer Dashboard? (Y/N): ");
            cont = sc.next().charAt(0);

        } while (cont == 'Y' || cont == 'y');
    }

    // =====================
    //    PROVIDER PANEL
    // =====================

    public static void providerDashboard(config con, String providerId) {
        char cont;

        do {
            System.out.println("\n===== PROVIDER DASHBOARD =====");
            System.out.println("1. View My Appointments");
            System.out.println("2. Update Appointment Status");
            System.out.println("3. Manage My Services");
            System.out.println("4. Logout");
            System.out.print("Enter choice: ");

            int action = sc.nextInt();
            sc.nextLine();

            switch (action) {
                case 1:
                    viewMyAppointments(con, providerId, true);
                    break;

                case 2:
                    viewMyAppointments(con, providerId, true);
                    System.out.print("Enter Appointment ID to update: ");
                    int aid = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Enter new status (Approved/Cancelled/Done): ");
                    String status = sc.nextLine();

                    con.updateRecord(
                            "UPDATE tbl_appointments SET app_status=? WHERE a_id=? AND provider_id=?",
                            status, aid, providerId
                    );

                    System.out.println("Status updated!");
                    break;

                case 3:
                    System.out.println("1. View My Services");
                    System.out.println("2. Add Service");
                    System.out.println("3. Update Service");
                    System.out.println("4. Delete Service");
                    System.out.print("Choice: ");

                    int sChoice = sc.nextInt();
                    sc.nextLine();

                    switch (sChoice) {
                        case 1:
                            printRecords(
                                    con.fetchRecords("SELECT * FROM tbl_services WHERE provider_id=?", providerId),
                                    new String[]{"ID", "Name", "Description", "Price"},
                                    new String[]{"s_id", "s_name", "s_desc", "s_price"}
                            );
                            break;

                        case 2:
                            addService(con, providerId);
                            break;

                        case 3:
                            updateService(con, providerId);
                            break;

                        case 4:
                            deleteService(con, providerId);
                            break;

                        default:
                            System.out.println("Invalid choice!");
                    }
                    break;

                case 4:
                    return;

                default:
                    System.out.println("Invalid choice!");
            }

            System.out.print("\nReturn to Provider Dashboard? (Y/N): ");
            cont = sc.next().charAt(0);

        } while (cont == 'Y' || cont == 'y');
    }

    // =====================
    //         MAIN
    // =====================

    public static void main(String[] args) {

        config con = new config();
        con.connectDB();

        char cont;

        do {
            System.out.println("\n===== HOME SERVICE APPOINTMENT LOGIN SYSTEM =====");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1: // LOGIN
                    System.out.print("Enter Email: ");
                    String em = sc.next();

                    System.out.print("Enter Password: ");
                    String pw = sc.next();

                    String hashed = config.hashPassword(pw);

                    List<Map<String, Object>> result = con.fetchRecords(
                            "SELECT * FROM tbl_users WHERE u_email = ? AND u_pass = ?",
                            em, hashed
                    );

                    if (result.isEmpty()) {
                        System.out.println("INVALID CREDENTIALS!");
                        break;
                    }

                    Map<String, Object> user = result.get(0);

                    String stat = user.get("u_status").toString();
                    String type = user.get("u_type").toString();
                    String id = user.get("u_id").toString();

                    if (stat.equalsIgnoreCase("Pending")) {
                        System.out.println("Account is Pending. Contact Admin.");
                        break;
                    }

                    System.out.println("LOGIN SUCCESS! Welcome, " + user.get("u_name"));

                    if (type.equalsIgnoreCase("Admin")) adminDashboard(con);
                    else if (type.equalsIgnoreCase("Customer")) customerDashboard(con, id);
                    else if (type.equalsIgnoreCase("Provider")) providerDashboard(con, id);

                    break;

                case 2: // REGISTER
                    System.out.print("Enter Name: ");
                    String name = sc.nextLine();

                    System.out.print("Enter Email: ");
                    String email = sc.nextLine();

                    while (!con.fetchRecords("SELECT * FROM tbl_users WHERE u_email=?", email).isEmpty()) {
                        System.out.print("Email already exists. Enter another: ");
                        email = sc.nextLine();
                    }

                    System.out.println("Select User Type:");
                    System.out.println("1 - Admin\n2 - Customer\n3 - Provider");
                    System.out.print("Choice: ");
                    int t = sc.nextInt();
                    sc.nextLine();

                    String typeStr = (t == 1) ? "Admin" : (t == 2) ? "Customer" : "Provider";

                    System.out.print("Enter Password: ");
                    String pass = sc.nextLine();

                    String hashedPw = config.hashPassword(pass);

                    con.addRecord(
                            "INSERT INTO tbl_users(u_name, u_email, u_type, u_status, u_pass) VALUES (?, ?, ?, ?, ?)",
                            name, email, typeStr, typeStr.equals("Admin") ? "Approved" : "Pending", hashedPw
                    );

                    List<Map<String, Object>> last = con.fetchRecords(
                            "SELECT u_id FROM tbl_users WHERE u_email=? ORDER BY u_id DESC LIMIT 1",
                            email
                    );

                    String uid = last.get(0).get("u_id").toString();

                    if (typeStr.equals("Customer")) {
                        String number, address;
                        do {
                            System.out.print("Enter Contact Number: ");
                            number = sc.nextLine();
                        } while (number.trim().isEmpty());

                        do {
                            System.out.print("Enter Address: ");
                            address = sc.nextLine();
                        } while (address.trim().isEmpty());

                        con.updateRecord(
                                "UPDATE tbl_users SET u_number=?, u_address=? WHERE u_id=?",
                                number, address, uid
                        );
                    }

                    if (typeStr.equals("Provider")) {
                        String contact, skill;

                        do {
                            System.out.print("Enter Contact Number: ");
                            contact = sc.nextLine();
                        } while (contact.trim().isEmpty());

                        do {
                            System.out.print("Enter Skill/Service Offered: ");
                            skill = sc.nextLine();
                        } while (skill.trim().isEmpty());

                        con.updateRecord(
                                "UPDATE tbl_users SET u_number=?, u_skill=?, u_status='Available' WHERE u_id=?",
                                contact, skill, uid
                        );
                    }

                    System.out.println("Registration successful!");
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
}
