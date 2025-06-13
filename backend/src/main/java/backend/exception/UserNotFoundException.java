package backend.exception;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(Long id) {
        super("could not find user with id " + id);
    }
    public UserNotFoundException(String message) {
        super(message);
    }
}
