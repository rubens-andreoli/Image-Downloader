package rubensandreoli.commons.exceptions.checked;

public class BoundsException extends Exception{
    private static final long serialVersionUID = 1L;

    public BoundsException() {}

    public BoundsException(String message) {
        super(message);
    }

    public BoundsException(String message, Throwable cause) {
        super(message, cause);
    }

    public BoundsException(Throwable cause) {
        super(cause);
    }

}
