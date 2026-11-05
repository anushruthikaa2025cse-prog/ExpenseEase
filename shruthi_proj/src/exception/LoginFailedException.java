package exception;

// OOP: User-defined exception for a failed login or password reset.
public class LoginFailedException extends Exception {

    // Empty constructor.
    public LoginFailedException() {
    }

    // Constructor with a message.
    public LoginFailedException(String message) {
        super(message);
    }
}
