package party.liyin.socketchannel.exception;

public class DependencyMissingException extends RuntimeException {
    public DependencyMissingException(String message) {
        super(message);
    }
}