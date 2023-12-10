import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;


public class newUi extends JFrame{
    private JPanel mainPanel;
    private JPanel loginPanel;
    private JPanel backgroundPanel;
    private int imageIndex = 0;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private static User loggedInUser;
    private JButton submitButton; // New Submit button
    private JButton switchModeButton; // New button to switch between login and create account modes
    private boolean createAccountMode = false; // Flag to indicate whether the panel is in create account mode
    private String[] imagePaths = {
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
    private JLabel[] imageLabels; // Array to hold JLabels for images


        public newUi(){

        loginPanel = new JPanel(); // Instantiate the loginPanel
        loginPanel.setLayout(new GridLayout(0, 2, 5, 5)); //Set the layout for loginPanel


        JPanel transparentPanel = new JPanel(null);
        transparentPanel.setOpaque(false); // Set the panel to be transparent
        transparentPanel.setBounds(600, 100, 300, 400); // Adjusted position and size

        // Create an aqua blue login box
        JPanel loginBox = new JPanel(new GridLayout(0, 2, 5, 5));
        loginBox.setBackground(new Color(127, 204, 255)); // Aqua blue color
        loginBox.setBorder(new LineBorder(Color.BLACK, 2)); // Adding a black border




            //Window optimization
            setTitle("Movie Watchers");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(900, 600);
            setLocationRelativeTo(null); // Center the window
            setResizable(false);
            setVisible(true);








            //background color
            mainPanel = new JPanel(null);
            mainPanel.setBackground(new Color(173, 216, 230)); // Carolina blue color
            mainPanel.setBorder(new LineBorder(Color.BLACK, 2)); // Adding a black border












        // Title label
        JLabel titleLabel = new JLabel("Welcome To Movie Watchers!");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(86, 160, 211));
        titleLabel.setBounds(0, 0, 900, 50);
        mainPanel.add(titleLabel);












        // Background panel for the image carousel
        backgroundPanel = new JPanel();
        backgroundPanel.setLayout(new GridLayout(0, 7, 10, 10));
        backgroundPanel.setBounds(0, 50, 600, 500);
        backgroundPanel.setBackground(Color.BLACK);
        mainPanel.add(backgroundPanel);






        loginPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        loginPanel.setBackground(new Color(86, 160, 211));
        loginPanel.setBounds(220, 80, 250, 220); // Adjusted position and size




         // Labels and fields for username 
         JLabel usernameLabel = new JLabel("Username:");
         usernameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
         // Assign to the class-level field instead of declaring a new local variable
         usernameField = new JTextField();
         usernameField.setFont(new Font("Arial", Font.PLAIN, 16));
         usernameField.setColumns(10);





        //Labels and fields for password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        // Assign to the class-level field instead of declaring a new local variable
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordField.setColumns(10);
        
        
         // Create a 'Submit' button
    submitButton = new JButton("Submit");

    // Add action listener to the 'Submit' button
    submitButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (createAccountMode) {
                if (createAccount()) {
                    // Account creation successful
                    switchToLoginMode();
                    clearFields(); // Clear text fields after successful account creation
                    JOptionPane.showMessageDialog(newUi.this, "Account created successfully. Proceed to login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(newUi.this, "Failed to create account. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Handle login functionality here
                if (authenticateUser()) {
                    // Successful login
                    dispose(); // Close the login window
                } else {
                    JOptionPane.showMessageDialog(newUi.this, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    });

        //Create account button
        JButton createAccountButton = new JButton("Create Account");
        createAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (createAccountMode) {
                    switchToLoginMode();
                } else {
                    switchToCreateAccountMode(createAccountButton);
                }
            }
        });




         // Add components to the login box
         loginBox.add(usernameLabel);
         loginBox.add(usernameField);
         loginBox.add(passwordLabel);
         loginBox.add(passwordField);
         loginBox.add(submitButton);
         loginBox.add(createAccountButton);
         transparentPanel.add(loginBox);


         // Set the bounds for the login box components
        Insets insets = loginBox.getInsets();
        Dimension size = loginBox.getPreferredSize();
        loginBox.setBounds(insets.left, insets.top, size.width, size.height);


        CompoundBorder compoundBorder = BorderFactory.createCompoundBorder(
                new LineBorder(Color.BLACK, 2), // Outer border
                BorderFactory.createEmptyBorder(10, 10, 10, 10) // Inner padding
        );

        loginPanel.setBorder(compoundBorder);
        mainPanel.add(loginPanel);










        mainPanel.add(transparentPanel);

        // Setting the layout for the main panel
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(loginPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);

        // Starting the image carousel
        startImageCarousel(backgroundPanel);



        
        

        }







        public User getLoggedInUser() {
        return loggedInUser;
    }







       // Method to compare provided password with stored hashed password
       private boolean verifyPassword(String providedPassword, String storedPasswordHash) {
        String hashedPassword = hashPassword(providedPassword);
        return hashedPassword != null && hashedPassword.equals(storedPasswordHash);
    }











   private String hashPassword(String password) {
    try {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashedBytes);
    } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
        return null;
    }
}














    //Authenticate user function doesn't need to be changed for any version
    private boolean authenticateUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/movies", "root", "root")) {
            String query = "SELECT * FROM Users WHERE Username = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
        
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    String storedHashedPassword = resultSet.getString("Password");
                    String storedSalt = resultSet.getString("Salt");
        
                    // Hash the provided password with the stored salt
                    String hashedPassword = hashPasswordWithSalt(password, storedSalt);
        
                    // Compare hashed passwords using a secure method
                    if (storedHashedPassword != null && storedHashedPassword.equals(hashedPassword)) {
                        loggedInUser = new User(resultSet.getInt("UserID"), username);
                        return true; // Authentication successful
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace(); // Print SQL exceptions for debugging
        }
        
        return false; // Authentication failed
    }
    
    











    //Create account function doesnt need to be changed for any version
    private boolean createAccount() {
        String newUsername = usernameField.getText();
        char[] newPasswordChars = passwordField.getPassword();
        String newPassword = new String(newPasswordChars);
        
        // Generate a random salt
        String salt = generateSalt();
        
        // Hash the password with the generated salt
        String hashedPassword = hashPasswordWithSalt(newPassword, salt);
        
        if (hashedPassword == null) {
            return false; // Unable to hash password
        }
        
        // Perform insertion into the Users table with salt and hashed password
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/movies", "root", "root")) {
            String insertQuery = "INSERT INTO Users (Username, Password, Salt) VALUES (?, ?, ?)";
            
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                preparedStatement.setString(1, newUsername);
                preparedStatement.setString(2, hashedPassword); // Store the hashed password
                preparedStatement.setString(3, salt); // Store the salt
                
                int rowsAffected = preparedStatement.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    








    




private String generateSalt() {
    // Generate a random salt using a secure random generator
    byte[] saltBytes = new byte[16]; // Salt length, you can adjust this value
    new SecureRandom().nextBytes(saltBytes);
    return Base64.getEncoder().encodeToString(saltBytes);
}











private String hashPasswordWithSalt(String password, String salt) {
    try {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        // Concatenate salt with the password before hashing
        String saltedPassword = password + salt;
        byte[] hashedBytes = md.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashedBytes);
    } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
        return null;
    }
}











    
    private void switchToCreateAccountMode(JButton button) {
        createAccountMode = true;
        submitButton.setText("Proceed To login");
        switchModeButton.setText("Create Account");
    }









    private void switchToLoginMode() {
        createAccountMode = false;
        submitButton.setText("Submit");
        switchModeButton.setText("Create Account");
    }








    public static boolean isAuthenticated() {
        return loggedInUser != null;
    }

    






    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
    }








//carosel functionality
 private void startImageCarousel(JPanel panel) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateImages(panel);
            }
        }, 0, 3000);
    }

    private void updateImages(JPanel panel) {
        int columns = 7;
        int rows = 4; // Change rows for each panel as needed
        int totalCells = columns * rows;

        if (imageLabels == null) {
            imageLabels = new JLabel[totalCells];
            for (int i = 0; i < totalCells; i++) {
                imageLabels[i] = createImageLabel(""); // Create blank labels initially
                panel.add(imageLabels[i]);
            }
        }

        for (int i = 0; i < totalCells; i++) {
            String imagePath = imagePaths[(imageIndex + i) % imagePaths.length];
            imageLabels[i].setIcon(new ImageIcon(getScaledImage(imagePath, 100, 125)));
        }

        imageIndex = (imageIndex + columns) % imagePaths.length;
    }

    private JLabel createImageLabel(String imagePath) {
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        return imageLabel;
    }

    private Image getScaledImage(String imagePath, int width, int height) {
        ImageIcon icon = new ImageIcon(imagePath);
        Image image = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(image).getImage();
    }



}








