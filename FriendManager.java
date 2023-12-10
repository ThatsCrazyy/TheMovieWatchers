import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FriendManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/movies";
    private static final String USER = "root";
    private static final String PASSWORD = "root";
    private static User loggedInUser;
    private JFrame friendManagerFrame;
    private JPanel friendPanel;
    

    public FriendManager(User user) {
        loggedInUser = user;
        
    }

    


    public void launchFriendManager() {
        // Create the Friend Manager frame
        friendManagerFrame = new JFrame("Friend Manager");
        friendManagerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        friendManagerFrame.setSize(400, 300);
        friendManagerFrame.setLocationRelativeTo(null);

        // Create a panel to display friends
        friendPanel = new JPanel();
        friendPanel.setLayout(new BoxLayout(friendPanel, BoxLayout.Y_AXIS));

        // Retrieve and display friends for the logged-in user
        displayFriends();

        // Add a scroll pane for the friend list panel
        JScrollPane scrollPane = new JScrollPane(friendPanel);

        // Add components to the frame
        friendManagerFrame.add(scrollPane);
        friendManagerFrame.setVisible(true);
    }






    
    private void displayFriends() {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String query = "SELECT Users.Username FROM Users " +
                    "INNER JOIN UserFriends ON Users.UserID = UserFriends.FriendID " +
                    "WHERE UserFriends.UserID = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, loggedInUser.getUserId());
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String friendUsername = resultSet.getString("Username");
                    JLabel friendLabel = new JLabel(friendUsername);
                    friendLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                    friendPanel.add(friendLabel);
                }

                // Refresh the friend panel
                friendPanel.revalidate();
                friendPanel.repaint();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }







    


    // Method to view friends of the logged-in user
    public static List<String> getFriends() {
        List<String> friendList = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String query = "SELECT Users.Username FROM Users " +
                    "INNER JOIN UserFriends ON Users.UserID = UserFriends.FriendID " +
                    "WHERE UserFriends.UserID = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, loggedInUser.getUserId());
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String friendUsername = resultSet.getString("Username");
                    friendList.add(friendUsername);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return friendList;
    }









    


 // Method to send a friend request to the database
 private static void sendFriendRequestToDatabase(int senderID, int receiverID) {
    try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
        String insertRequestQuery = "INSERT INTO PendingRequests (SenderID, ReceiverID, RequestDate) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertRequestQuery)) {
            preparedStatement.setInt(1, senderID);
            preparedStatement.setInt(2, receiverID);
            preparedStatement.setDate(3, java.sql.Date.valueOf(LocalDate.now())); // Assuming RequestDate is a date field
            preparedStatement.executeUpdate();
            System.out.println("Friend request sent successfully.");
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}













    public static void addFriend(String currentUserUsername, String friendUsername) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            // Check if the current user exists
            String checkCurrentUserQuery = "SELECT * FROM Users WHERE Username = ?";
            try (PreparedStatement checkCurrentUserStatement = connection.prepareStatement(checkCurrentUserQuery)) {
                checkCurrentUserStatement.setString(1, currentUserUsername);
                ResultSet currentUserResult = checkCurrentUserStatement.executeQuery();
    
                if (currentUserResult.next()) {
                    int currentUserID = currentUserResult.getInt("UserID");
    
                    // Check if the friend user exists
                    String checkFriendQuery = "SELECT * FROM Users WHERE Username = ?";
                    try (PreparedStatement checkFriendStatement = connection.prepareStatement(checkFriendQuery)) {
                        checkFriendStatement.setString(1, friendUsername);
                        ResultSet friendResult = checkFriendStatement.executeQuery();
    
                        if (friendResult.next()) {
                            int friendID = friendResult.getInt("UserID");
    
                            // Add the user as a friend
                            addFriendToDatabase(currentUserID, friendID);
                        } else {
                            System.out.println("Friend user does not exist.");
                            JOptionPane.showMessageDialog(null, "Friend user does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    System.out.println("Current user does not exist.");
                    JOptionPane.showMessageDialog(null, "Current user does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    






    
    













    private static void addFriendToDatabase(int userID, int friendID) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String addFriendRequestQuery = "INSERT INTO PendingRequests (SenderID, ReceiverID, RequestDate) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(addFriendRequestQuery)) {
                preparedStatement.setInt(1, userID);
                preparedStatement.setInt(2, friendID);
                preparedStatement.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
    
                int rowsAffected = preparedStatement.executeUpdate();
    
                if (rowsAffected > 0) {
                    System.out.println("Friend request sent successfully.");
                    JOptionPane.showMessageDialog(null, "Friend request sent successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    System.out.println("Failed to send friend request.");
                    JOptionPane.showMessageDialog(null, "Failed to send friend request.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    












// Method to send a friend request
public static void sendFriendRequest(String currentUserUsername, String friendUsername) {
    try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
        // Check if the current user exists
        String checkCurrentUserQuery = "SELECT * FROM Users WHERE Username = ?";
        try (PreparedStatement checkCurrentUserStatement = connection.prepareStatement(checkCurrentUserQuery)) {
            checkCurrentUserStatement.setString(1, currentUserUsername);
            ResultSet currentUserResult = checkCurrentUserStatement.executeQuery();

            if (currentUserResult.next()) {
                int currentUserID = currentUserResult.getInt("UserID");

                // Check if the friend user exists
                String checkFriendQuery = "SELECT * FROM Users WHERE Username = ?";
                try (PreparedStatement checkFriendStatement = connection.prepareStatement(checkFriendQuery)) {
                    checkFriendStatement.setString(1, friendUsername);
                    ResultSet friendResult = checkFriendStatement.executeQuery();

                    if (friendResult.next()) {
                        int friendID = friendResult.getInt("UserID");

                        // Send a friend request by inserting into PendingRequests table
                        sendFriendRequestToDatabase(currentUserID, friendID);
                    } else {
                        System.out.println("Friend user does not exist.");
                        JOptionPane.showMessageDialog(null, "Friend user does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                System.out.println("Current user does not exist.");
                JOptionPane.showMessageDialog(null, "Current user does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}


}