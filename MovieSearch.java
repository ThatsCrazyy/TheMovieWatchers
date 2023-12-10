import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MovieSearch {
    private static final String API_KEY = "8eef2d14581fa54099cf31c74a003212";
    private static final String API_ENDPOINT = "https://api.themoviedb.org/3/search/movie";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/movies";
    private static final String USER = "root";
    private static final String PASSWORD = "root";
    private static User loggedInUser;
    private static FriendManager friendManager;

    public static void createAndShowGUI(User user) {
        loggedInUser = user;

        if (user != null) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            friendManager = new FriendManager(loggedInUser); // Initialize FriendManager instance

            JFrame frame = new JFrame("Movie Watchers");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JLabel titleLabel = new JLabel("Welcome To Movie Watchers!");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel searchLabel = new JLabel("Enter Movie Title:");
            searchLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            JTextField searchField = new JTextField(30);
            searchField.setPreferredSize(new Dimension(300, 30)); // Increase the size of the search bar
            JButton searchButton = new JButton("Search");
            searchButton.setBackground(new Color(255, 128, 0));
            searchButton.setForeground(Color.WHITE);

            searchButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String searchTerm = searchField.getText();
                    if (!searchTerm.isEmpty()) {
                        System.out.println("Search Term: " + searchTerm);
                        displayTopMovies(searchTerm);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Please enter a movie title.");
                    }
                }
            });

            JButton viewFriendsButton = new JButton("View Friends");
            JButton addFriendButton = new JButton("Add Friend");

            viewFriendsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    viewFriends(frame);
                }
            });

            JButton viewMyMoviesButton = new JButton("View My Movies");
            viewMyMoviesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    viewFavorites(loggedInUser.getUsername()); // Assuming this method displays user's movie list
                }
            });

            addFriendButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String inputFriendUsername = JOptionPane.showInputDialog(frame,
                            "Enter the username of the friend to add:");
                    if (inputFriendUsername != null && !inputFriendUsername.isEmpty()) {
                        friendManager.addFriend(loggedInUser.getUsername(), inputFriendUsername);
                        
                    }
                }
            });

            // Create a panel for buttons
            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(new Color(0, 153, 153));
            buttonPanel.add(viewFriendsButton);
            buttonPanel.add(addFriendButton);
            buttonPanel.add(viewMyMoviesButton);
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 10));

            // Create panel for the search components
            JPanel searchPanel = new JPanel();
            searchPanel.setLayout(new FlowLayout());
            searchPanel.setBackground(new Color(0, 153, 153));
            searchPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0)); // Move down the search panel
            searchPanel.add(searchLabel);
            searchPanel.add(searchField);
            searchPanel.add(searchButton);

            // Create a panel for the carousel
            JPanel carouselPanel = new JPanel();
            carouselPanel.setLayout(new BorderLayout());
            carouselPanel.setBackground(new Color(255, 204, 0));

            JLabel carouselLabel = new JLabel();
            carouselLabel.setHorizontalAlignment(SwingConstants.CENTER);

            String[] movieImages = {
                    // Movie image paths...
                    "12 angry men.jpg",
                    "casino royale.jpg",
                    "catch me if you can.jpg",
                    "django unchained.jpg",
                    "Everything_Everywhere_All_at_Once.jpg",
                    "fight club.jpg",
                    "forrest gump.jpg",
                    "get out.jpg",
                    "gone girl.jpg",
                    "good will hunting.jpg",
                    "inception.jpg",
                    "infinity war.jpg",
                    "interstellar.jpg",
                    "judas and the black messiah.jpg",
                    "knives out.jpg",
                    "now you see me.jpg",
                    "saving private ryan.jpg",
                    "seven.jpg",
                    "shawshank redemption.jpg",
                    "shutter island.jpg",
                    "the Godfather.jpg",
                    "the killer.jpg",
                    "The_Batman_(film)_poster.jpg",
                    "TheDarkKnight.jpg",
                    "Whiplash.jpg",
                    "Wolf Of Wall street.jpg",
            };

            int[] currentIndex = { 0 };

            displayImage(carouselLabel, movieImages[currentIndex[0]]);

            carouselPanel.add(carouselLabel, BorderLayout.CENTER);

            // Add components to the frame
            frame.setLayout(new BorderLayout());
            frame.getContentPane().setBackground(new Color(255, 255, 255));
            frame.add(titleLabel, BorderLayout.NORTH);
            frame.add(searchPanel, BorderLayout.CENTER); // Move the search panel to center
            frame.add(buttonPanel, BorderLayout.SOUTH);
            frame.add(carouselPanel, BorderLayout.WEST); // Assuming carousel on the left side
            frame.add(buttonPanel, BorderLayout.SOUTH);

            // Set frame properties
            frame.setSize(1000, 600); // Adjust size as needed
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // Automatic image transition
            int delay = 3000; // Time in milliseconds (e.g., 3000ms = 3 seconds)
            Timer timer = new Timer(delay, e -> {
                currentIndex[0] = (currentIndex[0] + 1) % movieImages.length;
                displayImage(carouselLabel, movieImages[currentIndex[0]]);
            });
            timer.start();
        }
    }

    private static void displayImage(JLabel label, String imagePath) {
        try {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                Image img = new ImageIcon(imagePath).getImage().getScaledInstance(400, 300, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(img));
            } else {
                label.setIcon(null);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }






















    
    private static void viewFriends(JFrame frame) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            // Query for pending requests
            String pendingQuery = "SELECT Users.Username FROM Users " +
                    "INNER JOIN PendingRequests ON Users.UserID = PendingRequests.SenderID " +
                    "WHERE PendingRequests.ReceiverID = ?";

            // Query for current friends
            String friendsQuery = "SELECT Users.Username FROM Users " +
                    "INNER JOIN AcceptedFriends ON Users.UserID = AcceptedFriends.UserID2 " +
                    "WHERE AcceptedFriends.UserID1 = ?";

            try (PreparedStatement pendingStatement = connection.prepareStatement(pendingQuery);
                    PreparedStatement friendsStatement = connection.prepareStatement(friendsQuery)) {

                pendingStatement.setInt(1, loggedInUser.getUserId());
                ResultSet pendingResultSet = pendingStatement.executeQuery();

                friendsStatement.setInt(1, loggedInUser.getUserId());
                ResultSet friendsResultSet = friendsStatement.executeQuery();

                JPanel currentFriendsPanel = new JPanel();
                currentFriendsPanel.setLayout(new GridLayout(0, 1)); // One column for friend names

                JPanel pendingRequestsPanel = new JPanel();
                pendingRequestsPanel.setLayout(new GridLayout(0, 1)); // One column for friend names

                // Display current friends
                while (friendsResultSet.next()) {
                    String friendUsername = friendsResultSet.getString("Username");
                    JLabel friendLabel = new JLabel("Friend: " + friendUsername);

                    JButton removeButton = new JButton("Remove");
                    removeButton.addActionListener(e -> {
                        removeFriend(friendUsername);
                        currentFriendsPanel.remove(friendLabel); // Remove the friend from the UI
                        JOptionPane.showMessageDialog(frame, "Friend Removed");
                        frame.revalidate();
                        frame.repaint();
                    });

                    JButton viewButton = new JButton("View List");
                    viewButton.addActionListener(e -> {
                        displayFriendMovieList(friendUsername);
                        frame.revalidate();
                        frame.repaint();
                    });

                    JPanel friendEntry = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    friendEntry.add(friendLabel);
                    friendEntry.add(removeButton);
                    friendEntry.add(viewButton);
                    currentFriendsPanel.add(friendEntry);
                }

                // Display pending friend requests
                while (pendingResultSet.next()) {
                    String pendingUsername = pendingResultSet.getString("Username");
                    JLabel pendingLabel = new JLabel("Pending: " + pendingUsername);

                    JButton acceptButton = new JButton("Accept");
                    acceptButton.addActionListener(e -> {
                        acceptFriendRequest(pendingUsername);
                        pendingRequestsPanel.remove(pendingLabel); // Remove the accepted request from the UI
                        JOptionPane.showMessageDialog(frame, "Friend Request Accepted");
                        frame.revalidate();
                        frame.repaint();
                    });

                    JButton declineButton = new JButton("Decline");
                    declineButton.addActionListener(e -> {
                        declineFriendRequest(pendingUsername);
                        pendingRequestsPanel.remove(pendingLabel); // Remove the accepted request from the UI
                        JOptionPane.showMessageDialog(frame, "Friend Request Declined");
                        frame.revalidate();
                        frame.repaint();
                    });

                    JPanel pendingEntry = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    pendingEntry.add(pendingLabel);
                    pendingEntry.add(acceptButton);
                    pendingEntry.add(declineButton);

                    pendingRequestsPanel.add(pendingEntry);
                }

                // Create titles for both sections
                JLabel currentFriendsTitle = new JLabel("Current Friends");
                JLabel pendingRequestsTitle = new JLabel("Pending Friend Requests");

                // Set layout for the frame
                frame.getContentPane().removeAll();
                frame.setLayout(new BorderLayout());

                // Create split pane to separate the two sections
                JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
                splitPane.setLeftComponent(new JScrollPane(currentFriendsPanel));
                splitPane.setRightComponent(new JScrollPane(pendingRequestsPanel));
                splitPane.setDividerLocation(0.5); // Adjust divider position

                // Add titles above the split pane
                JPanel titlesPanel = new JPanel(new GridLayout(1, 2));
                titlesPanel.add(currentFriendsTitle);
                titlesPanel.add(pendingRequestsTitle);

                // Add components to the frame
                frame.add(titlesPanel, BorderLayout.NORTH);
                frame.add(splitPane, BorderLayout.CENTER);

                JButton backButton = new JButton("Back to Search");
                backButton.addActionListener(e -> {
                    frame.getContentPane().removeAll();
                    createAndShowGUI(loggedInUser); // Return to the main search screen
                });

                frame.add(backButton, BorderLayout.SOUTH);

                // Refresh the frame
                frame.revalidate();
                frame.repaint();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

















    
    private static void removeFriend(String friendUsername) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String getLoggedInUserIdQuery = "SELECT UserID FROM Users WHERE Username = ?";
            String getFriendIdQuery = "SELECT UserID FROM Users WHERE Username = ?";
            String removeFriendQuery = "DELETE FROM AcceptedFriends " +
                    "WHERE (UserID1 = ? AND UserID2 = ?) OR (UserID1 = ? AND UserID2 = ?)";
    
            try (PreparedStatement getLoggedInUserIdStatement = connection.prepareStatement(getLoggedInUserIdQuery);
                 PreparedStatement getFriendIdStatement = connection.prepareStatement(getFriendIdQuery);
                 PreparedStatement removeFriendStatement = connection.prepareStatement(removeFriendQuery)) {
    
                getLoggedInUserIdStatement.setString(1, loggedInUser.getUsername());
                ResultSet loggedInUserResultSet = getLoggedInUserIdStatement.executeQuery();
                if (loggedInUserResultSet.next()) {
                    int loggedInUserId = loggedInUserResultSet.getInt("UserID");
    
                    getFriendIdStatement.setString(1, friendUsername);
                    ResultSet friendResultSet = getFriendIdStatement.executeQuery();
                    if (friendResultSet.next()) {
                        int friendId = friendResultSet.getInt("UserID");
    
                        // Logging to check fetched IDs
                        System.out.println("Logged-in User ID: " + loggedInUserId);
                        System.out.println("Friend ID to remove: " + friendId);
    
                        removeFriendStatement.setInt(1, loggedInUserId);
                        removeFriendStatement.setInt(2, friendId);
                        removeFriendStatement.setInt(3, friendId);
                        removeFriendStatement.setInt(4, loggedInUserId);
    
                        int rowsAffected = removeFriendStatement.executeUpdate();
    
                        // Logging to check the number of rows affected by the delete statement
                        System.out.println("Rows affected: " + rowsAffected);
    
                        if (rowsAffected > 0) {
                            System.out.println("Friend removed successfully from the database.");
                        } else {
                            System.out.println("Failed to remove friend from the database.");
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    
    
    









    private static void acceptFriendRequest(String friendUsername) {
    try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
        String getFriendIDQuery = "SELECT UserID FROM Users WHERE Username = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(getFriendIDQuery)) {
            preparedStatement.setString(1, friendUsername);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int friendID = resultSet.getInt("UserID");

                // Check if the user is not already a friend
                if (!isAlreadyFriend(friendID)) {
                    String addToFriendsQuery = "INSERT INTO AcceptedFriends (UserID1, UserID2) VALUES (?, ?)";
                    try (PreparedStatement addToFriendsStatement = connection.prepareStatement(addToFriendsQuery)) {
                        addToFriendsStatement.setInt(1, loggedInUser.getUserId());
                        addToFriendsStatement.setInt(2, friendID);
                        int rowsAffected = addToFriendsStatement.executeUpdate();

                        if (rowsAffected > 0) {
                            System.out.println("Friend successfully added!");
                            JOptionPane.showMessageDialog(null, "Friend successfully added!", "Success",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            System.out.println("Failed to add friend.");
                        }
                    }

                    // Add reciprocal entry for the friend who sent the request
                    String addReciprocalQuery = "INSERT INTO AcceptedFriends (UserID1, UserID2) VALUES (?, ?)";
                    try (PreparedStatement addReciprocalStatement = connection.prepareStatement(addReciprocalQuery)) {
                        addReciprocalStatement.setInt(1, friendID);
                        addReciprocalStatement.setInt(2, loggedInUser.getUserId());
                        int rowsAffectedReciprocal = addReciprocalStatement.executeUpdate();

                        if (rowsAffectedReciprocal > 0) {
                            System.out.println("Reciprocal friend entry added!");
                        } else {
                            System.out.println("Failed to add reciprocal friend entry.");
                        }
                    }

                    String removePendingQuery = "DELETE FROM PendingRequests WHERE SenderID = ? AND ReceiverID = ?";
                    try (PreparedStatement removePendingStatement = connection.prepareStatement(removePendingQuery)) {
                        removePendingStatement.setInt(1, friendID);
                        removePendingStatement.setInt(2, loggedInUser.getUserId());
                        removePendingStatement.executeUpdate();
                    }
                } else {
                    System.out.println("User is already a friend.");
                }
            }
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}

    




















    private static boolean isAlreadyFriend(int friendID) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String query = "SELECT * FROM AcceptedFriends WHERE (UserID1 = ? AND UserID2 = ?) OR (UserID1 = ? AND UserID2 = ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, loggedInUser.getUserId());
                preparedStatement.setInt(2, friendID);
                preparedStatement.setInt(3, friendID);
                preparedStatement.setInt(4, loggedInUser.getUserId());

                ResultSet resultSet = preparedStatement.executeQuery();
                return resultSet.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return true; // Consider it as already a friend in case of an error
        }
    }








    private static void declineFriendRequest(String friendUsername) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String getSenderIDQuery = "SELECT UserID FROM Users WHERE Username = ?";
            try (PreparedStatement senderIDStatement = connection.prepareStatement(getSenderIDQuery)) {
                senderIDStatement.setString(1, friendUsername);
                ResultSet senderIDResultSet = senderIDStatement.executeQuery();

                if (senderIDResultSet.next()) {
                    int senderID = senderIDResultSet.getInt("UserID");

                    String checkPendingQuery = "SELECT * FROM PendingRequests WHERE SenderID = ? AND ReceiverID = ?";
                    try (PreparedStatement checkPendingStatement = connection.prepareStatement(checkPendingQuery)) {
                        checkPendingStatement.setInt(1, senderID);
                        checkPendingStatement.setInt(2, loggedInUser.getUserId());
                        ResultSet pendingResultSet = checkPendingStatement.executeQuery();

                        if (pendingResultSet.next()) {
                            String deletePendingQuery = "DELETE FROM PendingRequests WHERE SenderID = ? AND ReceiverID = ?";
                            try (PreparedStatement deletePendingStatement = connection
                                    .prepareStatement(deletePendingQuery)) {
                                deletePendingStatement.setInt(1, senderID);
                                deletePendingStatement.setInt(2, loggedInUser.getUserId());
                                int rowsAffected = deletePendingStatement.executeUpdate();

                                if (rowsAffected > 0) {
                                    System.out.println("Friend request declined successfully.");
                                } else {
                                    System.out.println("Failed to decline friend request.");
                                }
                            }
                        } else {
                            System.out.println("No pending friend request found from this user.");
                        }
                    }
                } else {
                    System.out.println("Sender user not found.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }














    private static void displayFriendMovieList(String friendUsername) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String query = "SELECT Movies.Title, UserMovies.UserRating " +
                    "FROM UserMovies " +
                    "INNER JOIN Movies ON UserMovies.MovieID = Movies.MovieID " +
                    "INNER JOIN Users ON UserMovies.UserID = Users.UserID " +
                    "WHERE Users.Username = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, friendUsername);
                ResultSet resultSet = preparedStatement.executeQuery();

                // Create a JTable to display the friend's movie list
                DefaultTableModel tableModel = new DefaultTableModel();
                tableModel.addColumn("Title");
                tableModel.addColumn("User Rating");

                while (resultSet.next()) {
                    String movieTitle = resultSet.getString("Title");
                    double userRating = resultSet.getDouble("UserRating");

                    Object[] rowData = { movieTitle, userRating };
                    tableModel.addRow(rowData);
                }

                JTable movieTable = new JTable(tableModel);
                movieTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                JScrollPane scrollPane = new JScrollPane(movieTable);

                // Create a JFrame to display the friend's movie list
                JFrame friendMovieFrame = new JFrame("Friend's Movie List: " + friendUsername);
                friendMovieFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                friendMovieFrame.add(scrollPane);
                friendMovieFrame.pack();
                friendMovieFrame.setLocationRelativeTo(null);
                friendMovieFrame.setVisible(true);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }



















    private static void displayTopMovies(String searchTerm) {
        try {
            // Encode the search term to handle spaces and special characters
            String encodedSearchTerm = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);

            // Fetch data from the TMDb API with the provided search term
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_ENDPOINT + "?api_key=" + API_KEY + "&query=" + encodedSearchTerm))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                showMovieOptions(responseBody);
            } else {
                System.out.println("Error fetching data from TMDb API. Status code: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }








    private static String getDirector(int movieId) {
        // Fetch the director information from the TMDb API
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.themoviedb.org/3/movie/" + movieId + "/credits?api_key=" + API_KEY))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject creditsJson = new JSONObject(response.body());
                JSONArray crewArray = creditsJson.getJSONArray("crew");

                for (int i = 0; i < crewArray.length(); i++) {
                    JSONObject crewMember = crewArray.getJSONObject(i);
                    if (crewMember.getString("job").equals("Director")) {
                        return crewMember.getString("name");
                    }
                }
            } else {
                System.out.println("Error fetching director information. Status code: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Director not available";
    }












    private static String getReleaseDate(int movieId) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + API_KEY))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject movieDetailsJson = new JSONObject(response.body());

                if (movieDetailsJson.has("release_date")) {
                    String dateString = movieDetailsJson.getString("release_date");

                    // Parse the date string and format it to 'YYYY-MM-DD' using DateTimeFormatter
                    LocalDate date = LocalDate.parse(dateString);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    return date.format(formatter);
                }
            } else {
                System.out.println("Error fetching movie details. Status code: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ""; // Return an empty string if date retrieval fails
    }









    private static void showMovieOptions(String apiResponse) {
        JFrame movieOptionsFrame = new JFrame("Top Movie Options");
        movieOptionsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create components
        JLabel titleLabel = new JLabel("Top Movie Options");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(52, 152, 219));

        DefaultListModel<String> movieListModel = new DefaultListModel<>();
        JList<String> movieList = new JList<>(movieListModel);
        JPanel moviePanel = new JPanel(); // New panel for movie information and buttons
        JButton addButton = new JButton("Add to Favorites");
        addButton.setBackground(new Color(46, 204, 113));
        addButton.setForeground(Color.WHITE);

        // Parse the API response and add top 3 movie options to the list
        JSONArray moviesArray = new JSONObject(apiResponse).getJSONArray("results");
        for (int i = 0; i < Math.min(moviesArray.length(), 3); i++) {
            JSONObject movieJson = moviesArray.getJSONObject(i);
            String movieTitle = movieJson.getString("title");

            // Fetch and display additional information (director and release date)
            String director = getDirector(movieJson.getInt("id"));
            String releaseDate = getReleaseDate(movieJson.getInt("id"));

            // Display movie information in the list
            String movieInfo = movieTitle + " (Director: " + director + ", Release Date: " + releaseDate + ")";
            movieListModel.addElement(movieInfo);

            // Add buttons for adding to favorites and viewing favorites
            JButton addToFavoritesButton = new JButton("Add to Favorites");
            JButton viewFavoritesButton = new JButton("View Favorites");

            // Add action listeners to the buttons
            addToFavoritesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Call addToFavorites method with movieId and movieTitle
                    addToFavorites(movieJson.getInt("id"), movieTitle);
                }
            });

            viewFavoritesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    viewFavorites();
                }
            });

            // Add buttons to the GUI
            moviePanel.add(new JLabel(movieInfo));
            moviePanel.add(addToFavoritesButton);
            moviePanel.add(viewFavoritesButton);
        }

        // Add action listener to the add button
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // For testing, you can print the selected movie to the console
                String selectedMovie = movieList.getSelectedValue();
                System.out.println("Selected Movie: " + selectedMovie);
                // In a real application, you would add the selected movie to the database
                // Call the method to insert the selected movie into the database here
            }
        });

        // Set layout for the movie panel
        moviePanel.setLayout(new BoxLayout(moviePanel, BoxLayout.Y_AXIS));

        // Set layout for the frame
        movieOptionsFrame.setLayout(new BorderLayout());
        movieOptionsFrame.getContentPane().setBackground(new Color(236, 240, 241));

        // Add components to the frame
        movieOptionsFrame.add(titleLabel, BorderLayout.NORTH);
        movieOptionsFrame.add(new JScrollPane(movieList), BorderLayout.CENTER);
        movieOptionsFrame.add(moviePanel, BorderLayout.WEST); // Add movie panel to the left

        // Set frame properties
        movieOptionsFrame.setSize(600, 300); // Adjusted size
        movieOptionsFrame.setLocationRelativeTo(null); // Center the frame on the screen
        movieOptionsFrame.setVisible(true);
    }












    private static void updateRatingForSelectedMovie(int movieIndex, double newRating) {
        // Check if the new rating is within the range of 0.0 to 10.0
        if (newRating < 0.0 || newRating > 10.0) {
            // Show an error message in a dialog window
            JOptionPane.showMessageDialog(null,
                    "Invalid rating. Please enter a rating between 0.0 and 10.0.",
                    "Invalid Rating", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            // Assuming UserMovies table has a column named UserMovieID for uniquely identifying the record
            String updateQuery = "UPDATE UserMovies SET UserRating = ? WHERE UserMovieID = ?";
    
            // Assuming movieIndex is the UserMovieID of the selected movie in the UserMovies table
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                preparedStatement.setDouble(1, newRating);
                preparedStatement.setInt(2, movieIndex);
    
                int rowsAffected = preparedStatement.executeUpdate();
    
                if (rowsAffected > 0) {
                    // Update the UI or perform any necessary actions on successful update
                    // For UI update, include the code here to reflect the changes
                    System.out.println("User rating updated successfully.");
    
                    // Example code for UI update:
                    // Assuming there's a method called updateUIRating() to update the displayed rating
                    // updateUIRating(movieIndex, newRating);
                } else {
                    System.out.println("Failed to update user rating.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    










    private static void addToFavorites(int movieId, String movieTitle) {
        try {
            if (!doesMovieExist(movieId)) {
                // If the movie doesn't exist, it needs to be added to the Movies table first
                String director = getDirector(movieId);
                String releaseDate = getReleaseDate(movieId);
                addMovieToDatabase(movieId, movieTitle, director, releaseDate);
            }

            double userRating = 0.0; // Set the initial user rating to 0.0

            if (!doesUserMovieExist(loggedInUser.getUserId(), movieTitle)) {
                // Check if the movie title doesn't exist in the user's list
                try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
                    // Check again if the movie exists in Movies table (in case it was just added)
                    if (doesMovieExist(movieId)) {
                        String insertUserMovieQuery = "INSERT INTO UserMovies (UserID, MovieID, Title, UserRating) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement preparedStatement = connection.prepareStatement(insertUserMovieQuery)) {
                            preparedStatement.setInt(1, loggedInUser.getUserId());
                            preparedStatement.setInt(2, movieId);
                            preparedStatement.setString(3, movieTitle);
                            preparedStatement.setDouble(4, userRating);

                            int rowsAffected = preparedStatement.executeUpdate();

                            if (rowsAffected > 0) {
                                System.out.println("Movie added to favorites successfully.");
                                JOptionPane.showMessageDialog(null, "Movie added to favorites successfully.", "Success",
                                        JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                System.out.println("Failed to add movie to favorites.");
                                JOptionPane.showMessageDialog(null, "Failed to add movie to favorites.", "Error",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } else {
                        System.out.println("Movie does not exist in the Movies table.");
                        JOptionPane.showMessageDialog(null, "Movie does not exist in the Movies table.", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                System.out.println("Movie already exists in your favorites.");
                JOptionPane.showMessageDialog(null, "Movie already exists in your favorites.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }






    private static boolean doesUserMovieExist(int userId, String movieTitle) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String query = "SELECT COUNT(*) AS count FROM UserMovies WHERE UserID = ? AND Title = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, userId);
                preparedStatement.setString(2, movieTitle);

                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }










    // Method to check if the movie exists in the Movies table
    private static boolean doesMovieExist(int movieId) {
        String checkMovieQuery = "SELECT COUNT(*) FROM Movies WHERE MovieID = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                PreparedStatement checkStatement = connection.prepareStatement(checkMovieQuery)) {
            checkStatement.setInt(1, movieId);
            ResultSet resultSet = checkStatement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }












    // Method to add a movie to the Movies table
    private static void addMovieToDatabase(int movieId, String movieTitle, String director, String releaseDate) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String insertMovieQuery = "INSERT INTO Movies (MovieID, Title, Director, ReleaseDate) VALUES (?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertMovieQuery)) {
                preparedStatement.setInt(1, movieId);
                preparedStatement.setString(2, movieTitle);
                preparedStatement.setString(3, director);
                preparedStatement.setString(4, releaseDate);
                preparedStatement.executeUpdate();
                System.out.println("Movie added to Movies table.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }











    private static void viewFavorites(String username) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String query = "SELECT UserMovieID, Title, UserRating FROM UserMovies WHERE UserID = " +
                    "(SELECT UserID FROM Users WHERE Username = ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                ResultSet resultSet = preparedStatement.executeQuery();

                JPanel moviesPanel = new JPanel();
                moviesPanel.setLayout(new BoxLayout(moviesPanel, BoxLayout.Y_AXIS));

                while (resultSet.next()) {
                    int userMovieID = resultSet.getInt("UserMovieID");
                    String title = resultSet.getString("Title");
                    double userRating = resultSet.getDouble("UserRating");

                    JPanel moviePanel = new JPanel(new BorderLayout());
                    moviePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

                    JLabel titleLabel = new JLabel(title);
                    titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
                    moviePanel.add(titleLabel, BorderLayout.NORTH);

                    JLabel ratingLabel = new JLabel("User Rating: " + userRating);
                    moviePanel.add(ratingLabel, BorderLayout.CENTER);

                    JButton editButton = new JButton("Edit");
                    editButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            double newRating = getUserRatingFromInput();
                            updateRatingForSelectedMovie(userMovieID, newRating);
                            ratingLabel.setText("User Rating: " + newRating);
                        }
                    });

                    JButton removeButton = new JButton("Remove");
                    removeButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            removeMovie(userMovieID);
                            moviesPanel.remove(moviePanel);
                            moviesPanel.revalidate();
                            moviesPanel.repaint();
                        }
                    });

                    JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
                    buttonPanel.add(editButton);
                    buttonPanel.add(removeButton);

                    moviePanel.add(buttonPanel, BorderLayout.SOUTH);

                    moviesPanel.add(moviePanel);
                }

                JScrollPane scrollPane = new JScrollPane(moviesPanel);
                scrollPane.setPreferredSize(new Dimension(400, 400)); // Set a preferred size

                JFrame favoritesFrame = new JFrame(username + "'s Favorite Movies");
                favoritesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                favoritesFrame.add(scrollPane);

                favoritesFrame.pack();
                favoritesFrame.setLocationRelativeTo(null);
                favoritesFrame.setVisible(true);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }













    private static void viewFavorites() {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String query = "SELECT UserMovieID, Title, UserRating FROM UserMovies WHERE UserID = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, loggedInUser.getUserId());
                ResultSet resultSet = preparedStatement.executeQuery();

                JPanel moviesPanel = new JPanel();
                moviesPanel.setLayout(new BoxLayout(moviesPanel, BoxLayout.Y_AXIS));

                while (resultSet.next()) {
                    int userMovieID = resultSet.getInt("UserMovieID");
                    String title = resultSet.getString("Title");
                    double userRating = resultSet.getDouble("UserRating");

                    JPanel moviePanel = new JPanel(new BorderLayout());
                    moviePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

                    JLabel titleLabel = new JLabel(title);
                    titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
                    moviePanel.add(titleLabel, BorderLayout.NORTH);

                    JLabel ratingLabel = new JLabel("User Rating: " + userRating);
                    moviePanel.add(ratingLabel, BorderLayout.CENTER);

                    JButton editButton = new JButton("Edit");
                    editButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            double newRating = getUserRatingFromInput();
                            updateRatingForSelectedMovie(userMovieID, newRating);
                            ratingLabel.setText("User Rating: " + newRating);
                        }
                    });

                    JButton removeButton = new JButton("Remove");
                    removeButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            removeMovie(userMovieID);
                            moviesPanel.remove(moviePanel);
                            moviesPanel.revalidate();
                            moviesPanel.repaint();
                        }
                    });

                    JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
                    buttonPanel.add(editButton);
                    buttonPanel.add(removeButton);

                    moviePanel.add(buttonPanel, BorderLayout.SOUTH);

                    moviesPanel.add(moviePanel);
                }

                JScrollPane scrollPane = new JScrollPane(moviesPanel);
                scrollPane.setPreferredSize(new Dimension(400, 400)); // Set a preferred size

                JFrame favoritesFrame = new JFrame("Your Favorite Movies");
                favoritesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                favoritesFrame.add(scrollPane);

                favoritesFrame.pack();
                favoritesFrame.setLocationRelativeTo(null);
                favoritesFrame.setVisible(true);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
















    private static double getUserRatingFromInput() {
        double newRating = 0.0;
        try {
            String userInput = JOptionPane.showInputDialog(null, "Enter new rating:");
            if (userInput != null && !userInput.isEmpty()) {
                newRating = Double.parseDouble(userInput);
            } else {
                JOptionPane.showMessageDialog(null, "Please enter a valid rating.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid number.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return newRating;
    }














    

    private static void removeMovie(int userMovieID) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String deleteQuery = "DELETE FROM UserMovies WHERE UserMovieID = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
                preparedStatement.setInt(1, userMovieID);

                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Movie removed from favorites successfully.");
                } else {
                    System.out.println("Failed to remove movie from favorites.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}