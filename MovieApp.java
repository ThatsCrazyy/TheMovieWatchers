import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.*;

// Class managing movie-related functionalities
public class MovieApp {
    // Constants for API and database details
    private static final String API_KEY = "8eef2d14581fa54099cf31c74a003212";
    private static final String API_ENDPOINT = "https://api.themoviedb.org/3/search/movie";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/movies";
    private static final String USER = "root";
    private static final String PASSWORD = "root";
    // Static variable tracking the logged-in user
    public static User loggedInUser;

    // Main method of the application
    public static void main(String[] args) {
        // Display the login page
       newUi loginPanel = showLoginPage();

        // Check if the login was successful
        if (loginPanel.isAuthenticated()) {
            // If login is successful, create and show the main GUI
            loggedInUser = loginPanel.getLoggedInUser();
            MovieSearch.createAndShowGUI(loggedInUser);
        }
    }




    

    // Method displaying the login page and authenticating the user
    private static newUi showLoginPage() {
        // Create an instance of the LoginPanel
        newUi loginPanel = new newUi();

        // Wait for the user to log in
        while (!loginPanel.isAuthenticated()) {
            try {
                Thread.sleep(1000); // Sleep for 1 second before checking again
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Return the LoginPanel instance
        return loginPanel;
    }















    // Method to display the list of user's favorite movies
    public static void showUserList() {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            // SQL query to fetch user's favorite movies
            String query = "SELECT m.* FROM Movies m JOIN UserMovies um ON m.MovieID = um.MovieID WHERE um.UserID = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, loggedInUser.getUserId());

                ResultSet resultSet = preparedStatement.executeQuery();
                displayUserList(resultSet); // Display the list of favorite movies
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }












    private static void displayUserList(ResultSet resultSet) throws SQLException {
        JFrame favoritesFrame = new JFrame("Your Favorite Movies");
        favoritesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        favoritesFrame.setLayout(new BorderLayout());
        favoritesFrame.getContentPane().setBackground(new Color(236, 240, 241));
    
        JPanel moviesPanel = new JPanel();
        moviesPanel.setLayout(new BoxLayout(moviesPanel, BoxLayout.Y_AXIS));
    
        JLabel titleLabel = new JLabel("Your Favorite Movies");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(52, 152, 219));
    
        favoritesFrame.add(titleLabel, BorderLayout.NORTH);
        favoritesFrame.add(new JScrollPane(moviesPanel), BorderLayout.CENTER);
    
        while (resultSet.next()) {
            int userMovieID = resultSet.getInt("UserMovieID");
            String title = resultSet.getString("Title");
            double userRating = resultSet.getDouble("UserRating");
    
            JPanel moviePanel = new JPanel(new BorderLayout());
            JLabel movieLabel = new JLabel(title + " - User Rating: " + userRating);
            JButton editRatingButton = new JButton("Edit Rating");
    
            editRatingButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String newRatingString = JOptionPane.showInputDialog(
                            favoritesFrame,
                            "Enter new rating for " + title + ":",
                            "Edit Rating",
                            JOptionPane.QUESTION_MESSAGE
                    );
    
                    if (newRatingString != null && !newRatingString.isEmpty()) {
                        try {
                            double newRating = Double.parseDouble(newRatingString);
                            updateRatingForSelectedMovie(userMovieID, newRating);
                            movieLabel.setText(title + " - User Rating: " + newRating); // Update displayed rating
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(
                                    favoritesFrame,
                                    "Invalid input. Please enter a valid number.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                }
            });
    
            moviePanel.add(movieLabel, BorderLayout.CENTER);
            moviePanel.add(editRatingButton, BorderLayout.EAST);
            moviesPanel.add(moviePanel);
        }
    
        favoritesFrame.setSize(400, 300);
        favoritesFrame.setLocationRelativeTo(null);
        favoritesFrame.setVisible(true);
    }
    
    















    // Method to fetch movie data from TMDb API and insert it into the database
    public static void fetchAndInsertData(String searchTerm) {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Fetch data from the TMDb API with the provided search term
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_ENDPOINT + "?api_key=" + API_KEY + "&query=" + searchTerm))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                insertDataIntoDatabase(responseBody); // Insert fetched data into the database
            } else {
                System.out.println("Error fetching data from TMDb API. Status code: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






    







    // Method to parse API response and insert movie data into the database
    private static void insertDataIntoDatabase(String apiResponse) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String insertMovieQuery = "INSERT INTO Movies (MovieID, Title, Director, ReleaseDate) VALUES (?, ?, ?, ?)";

            JSONArray moviesArray = new JSONObject(apiResponse).getJSONArray("results");

            // Iterate through movie data obtained from the API
            for (int i = 0; i < moviesArray.length(); i++) {
                JSONObject movieJson = moviesArray.getJSONObject(i);
                Movie movie = parseTmdbApiResponse(movieJson); // Parse API response into a Movie object

                // Check if the movie already exists in the Movies table
                if (!movieExists(connection, movie.getId())) {
                    // Movie doesn't exist, insert it into the Movies table
                    try (PreparedStatement insertStatement = connection.prepareStatement(insertMovieQuery)) {
                        // Set values for insertion
                        insertStatement.setInt(1, movie.getId());
                        insertStatement.setString(2, movie.getTitle());
                        insertStatement.setString(3, movie.getDirector());
                        insertStatement.setString(4, movie.getReleaseDate());

                        int rowsAffected = insertStatement.executeUpdate();

                        if (rowsAffected > 0) {
                            System.out.println("Movie added to Movies table successfully.");
                        } else {
                            System.out.println("Failed to add movie to Movies table.");
                        }
                    }
                } else {
                    System.out.println("Movie already exists in Movies table.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }










    // Method to check if a movie already exists in the database
    private static boolean movieExists(Connection connection, int movieId) throws SQLException {
        String checkMovieQuery = "SELECT COUNT(*) FROM Movies WHERE MovieID = ?";
        try (PreparedStatement checkStatement = connection.prepareStatement(checkMovieQuery)) {
            checkStatement.setInt(1, movieId);
            ResultSet resultSet = checkStatement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        }
        return false;
    }








    // Method to parse TMDb API response and create a Movie object
    private static Movie parseTmdbApiResponse(JSONObject movieJson) {
        int id = movieJson.getInt("id");
        String title = movieJson.getString("title");
        double rating = movieJson.getDouble("vote_average");

        // Fetch director information
        String director = getDirector(id);

        // Fetch release date information
        String releaseDate = getReleaseDate(id);

        return new Movie(id, title, rating, director, releaseDate); // Create a Movie object
    }













    // Method to fetch director information from TMDb API
    private static String getDirector(int movieId) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.themoviedb.org/3/movie/" + movieId + "/credits?api_key=" + API_KEY))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                System.out.println("Director API Response: " + responseBody); // Print the response to the console

                JSONObject creditsJson = new JSONObject(responseBody);
                JSONArray crewArray = creditsJson.getJSONArray("crew");

                // Iterate through crew members to find the director
                for (int i = 0; i < crewArray.length(); i++) {
                    JSONObject crewMember = crewArray.getJSONObject(i);
                    if (crewMember.getString("job").equals("Director")) {
                        return crewMember.getString("name"); // Return director's name
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










    // Method to fetch release date information from TMDb API
    private static String getReleaseDate(int movieId) {
        // This method is incomplete and returns a placeholder value
        return "Release date not available";
    }



    private static void updateRatingForSelectedMovie(int movieIndex, double newRating) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            // Assuming UserMovies table has a column named UserMovieID for uniquely identifying the record
            String updateQuery = "UPDATE UserMovies SET UserRating = ? WHERE UserMovieID = ?";
    
            // Assuming movieIndex is the UserMovieID of the selected movie in the UserMovies table
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                preparedStatement.setDouble(1, newRating);
                preparedStatement.setInt(2, movieIndex);
    
                int rowsAffected = preparedStatement.executeUpdate();
    
                if (rowsAffected > 0) {
                    System.out.println("User rating updated successfully.");
                } else {
                    System.out.println("Failed to update user rating.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    





}
