package exception;

// OOP: User-defined exception for bad input.
public class InvalidInputException extends Exception {

    // Empty constructor.
    public InvalidInputException() {
    }

    // Constructor with a message.
    public InvalidInputException(String message) {
        super(message);
    }
}
