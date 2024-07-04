import java.sql.*;
import java.util.Scanner;
public class Main {

    private static final String url = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String userName = "root";
    private static final String password = "tiger";

    public static void main(String[] args) throws ClassNotFoundException, SQLException {

        //load drivers
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        }catch (ClassNotFoundException e){
            System.out.println(e.getMessage());
        }

        //connection
        try{
            Connection connection = DriverManager.getConnection(url, userName, password);
            while (true){
                System.out.println();
                System.out.println("Hotel management system");
                Scanner scanner = new Scanner(System.in);
                System.out.println("1. Reserve a room");
                System.out.println("2. View Reservations");
                System.out.println("3. get room number");
                System.out.println("4. Update reservations");
                System.out.println("5. Delete reservations");
                System.out.println("0. Exit");
                System.out.println("Choose an option: ");
                int choice = scanner.nextInt();
                switch (choice){
                    case 1:
                        reserveRoom(connection, scanner);
                        break;
                    case 2:
                        viewReservations(connection);
                        break;
                    case 3:
                        getRoomNumber(connection, scanner);
                        break;
                    case 4:
                        updateReservation(connection, scanner);
                        break;
                    case 5:
                        deleteReservation(connection, scanner);
                        break;
                    case 0:
                        exit();
                        scanner.close();
                        return;
                    default:
                        System.out.println("Invalid choice. try again");
                }
            }
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }catch(InterruptedException e){
            throw new RuntimeException(e);
        }
    }
    private static void reserveRoom(Connection connection, Scanner scanner){
        try {
            System.out.println("enter guest name: ");
            String guestName = scanner.next();
            scanner.nextLine();
            System.out.println("Enter room Number: ");
            int roomNumber = scanner.nextInt();
            System.out.println("enter contact number");
            String contactNumber = scanner.next();

            String sql = "insert into reservations (guest_name, room_number, contact_number)" +
                    "values ('" + guestName + "', "+ roomNumber + ", '"+ contactNumber + "')";

            try (Statement statement = connection.createStatement()){
                int affectedRows = statement.executeUpdate((sql));

                if(affectedRows>0){
                    System.out.println("Reservation successful!");
                }else {
                    System.out.println("Reservation failed!");
                }
            }

        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    private static void viewReservations(Connection connection) throws SQLException{
        String sql = "select reservation_id, guest_name, room_number, contact_number, reservation_date from reservations";

        try( Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql)){
            System.out.println("current reservations: ");
            System.out.println("+----------------+-----------------+---------------+----------------------+------------------------+");
            System.out.println("| Reservation ID | Guest           | Room Number   | Contact Number       | Reservation Date       |");
            System.out.println("+----------------+-----------------+---------------+----------------------+------------------------+");

            while (resultSet.next()){
                int reservationId = resultSet.getInt("reservation_id");
                String guestName = resultSet.getString("guest_name");
                int roomNumber = resultSet.getInt("room_number");
                String contactNumber = resultSet.getString("contact_number");
                String reservationDate = resultSet.getTimestamp("reservation_date").toString();

                //format and display the reservation date in a table- like format
                System.out.printf("| %-14d | %-15s | %-13d | %-20s | %-19s  |\n",
                        reservationId, guestName, roomNumber, contactNumber, reservationDate);
            }
            System.out.println("+----------------+-----------------+---------------+----------------------+------------------------+");
        }
    }

    private static  void getRoomNumber (Connection connection, Scanner scanner){
        try {
            System.out.print("enter reservation ID: ");
            int reservationId = scanner.nextInt();
            System.out.print("Enter guest name: ");
            String guestName = scanner.next();

            String sql = "select room_number from reservations " +
                    "where reservation_id = " + reservationId +
                    "and guest_name = '" + guestName + "'";

            try(Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)){

                if(resultSet.next()){
                    int roomNumber = resultSet.getInt("room_number");
                    System.out.println("room number for reservation ID " + reservationId +
                            "and guest " + guestName + " is: " + roomNumber);
                }else {
                    System.out.println("reservation not found for the given id and name.");
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    private static void updateReservation(Connection connection, Scanner scanner){
    try {
        System.out.println("enter reservation id to update: ");
        int reservationId = scanner.nextInt();
        scanner.nextLine();//consume the newline character

        if(!reservationExists(connection, reservationId)){
            System.out.println("reservation not found for given id.");
            return;
        }
        System.out.print("enter new guest name: ");
        String newGuestName = scanner.nextLine();
        System.out.print("enter new room number: ");
        int newRoomNumber = scanner.nextInt();
        System.out.print("enter new contact number: ");
        String newContactNumber = scanner.next();

        String sql = "update reservations set guest_name = '" + newGuestName + "', " +
                "room_number = " + newRoomNumber + ", " +
                "contact_number = '" + newContactNumber + "' " +
                "where reservation_id = " + reservationId;

        try(Statement statement = connection.createStatement()){
            int affectedRows = statement.executeUpdate(sql);

            if(affectedRows>0){
                System.out.println("reservation updated successfully!");
            }else {
                System.out.println("reservation update failed.");
            }
        }
    }catch (SQLException e){
        e.printStackTrace();
    }
    }

    private static void deleteReservation (Connection connection, Scanner scanner){
    try {
        System.out.println("enter reservation id to delete: ");
        int reservationId = scanner.nextInt();

        if(!reservationExists(connection, reservationId)){
            System.out.println("reservation not found for the given id.");
            return;
        }

        String sql = "delete from reservations where reservation_id = " + reservationId;

        try(Statement statement = connection.createStatement()){
            int affectedRows = statement.executeUpdate(sql);

            if (affectedRows > 0){
                System.out.println("reservation deleted successfully!");
            }else{
                System.out.println("reservation deletion failed.");
            }
        }

    }catch (SQLException e){
        e.printStackTrace();
    }
    }

    private static boolean reservationExists(Connection connection, int reservationId){
        try {
            String sql = "select reservation_id from reservations where reservation_id = " +reservationId;

            try(Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)){

                return resultSet.next(); // if there's a result, the reservation exist
            }
        }catch (SQLException e){
            e.printStackTrace();
            return false; // handle database errors as needed
        }
    }
    private static void exit() throws  InterruptedException {
    System.out.print("exiting System");
    int i = 5;
    while (i!=0){
        System.out.print(".");
        Thread.sleep(450);
        i--;
    }
    System.out.println();
    System.out.println("thank you for using hotel reservation system!!");
    }
}