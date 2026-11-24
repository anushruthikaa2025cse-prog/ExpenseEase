package model;

// A registered user of the expense tracker.
public class User {

    // OOP: Encapsulation - fields are private.
    private int userId;
    private String username;
    private String password;
    private String email;
    private String securityAnswer;

    // Empty constructor.
    public User() {
    }

    // Constructor used when reading a user from the database.
    public User(int userId, String username, String password,
                String email, String securityAnswer) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.securityAnswer = securityAnswer;
    }

    // Returns the user id.
    public int getUserId() {
        return userId;
    }

    // Sets the user id.
    public void setUserId(int userId) {
        this.userId = userId;
    }

    // Returns the username.
    public String getUsername() {
        return username;
    }

    // Sets the username.
    public void setUsername(String username) {
        this.username = username;
    }

    // Returns the password.
    public String getPassword() {
        return password;
    }

    // Sets the password.
    public void setPassword(String password) {
        this.password = password;
    }

    // Returns the email.
    public String getEmail() {
        return email;
    }

    // Sets the email.
    public void setEmail(String email) {
        this.email = email;
    }

    // Returns the security answer.
    public String getSecurityAnswer() {
        return securityAnswer;
    }

    // Sets the security answer.
    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }

    // OOP: toString() from the Object class is overridden.
    @Override
    public String toString() {
        return username + " (" + email + ")";
    }
}
