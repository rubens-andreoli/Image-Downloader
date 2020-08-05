package rubensandreoli.commons.exceptions.checked;

public class CastException extends Exception{
    private static final long serialVersionUID = 1L;

    public CastException() {}

    public CastException(String message) {
        super(message);
    }

    public CastException(String message, Throwable cause) {
        super(message, cause);
    }

    public CastException(Throwable cause) {
        super(cause);
    }

}
