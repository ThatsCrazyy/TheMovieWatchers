import java.util.Objects; // Importing the Objects class from java.util package

// Class representing a Movie
public class Movie {
    private int id; // Private integer variable to store the movie's ID
    private String title; // Private String variable to store the movie's title
    private double rating; // Private double variable to store the movie's rating
    private String director; // Private String variable to store the movie's directorassum
    private String releaseDate; // Private String variable to store the movie's release date

    // Constructor with parameters to initialize a Movie object
    public Movie(int id, String title, double rating, String director, String releaseDate) {
        this.id = id; // Assigning the provided id to the movie's id
        this.title = title; // Assigning the provided title to the movie's title
        this.rating = rating; // Assigning the provided rating to the movie's rating
        this.director = director; // Assigning the provided director to the movie's director
        this.releaseDate = releaseDate; // Assigning the provided release date to the movie's release date
    }

    // Getter method to retrieve the movie's ID
    public int getId() {
        return id; // Returning the movie's id
    }

    // Getter method to retrieve the movie's title
    public String getTitle() {
        return title; // Returning the movie's title
    }

    // Getter method to retrieve the movie's rating
    public double getRating() {
        return rating; // Returning the movie's rating
    }

    // Getter method to retrieve the movie's director
    public String getDirector() {
        return director; // Returning the movie's director
    }

    // Getter method to retrieve the movie's release date
    public String getReleaseDate() {
        return releaseDate; // Returning the movie's release date
    }
}
