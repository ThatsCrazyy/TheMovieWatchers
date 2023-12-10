// This class represents a User entity with userId and username attributes
public class User {
    private int userId;     // Unique identifier for a user
    private String username; // User's username

    // Constructor to initialize a User object with userId and username
    public User(int userId, String username) {
        this.userId = userId;       // Set the userId attribute
        this.username = username;   // Set the username attribute
    }

    // Getter method to retrieve the userId
    public int getUserId() {
        return userId;  // Return the userId attribute
    }

    // Getter method to retrieve the username
    public String getUsername() {
        return username;  // Return the username attribute
    }
}
