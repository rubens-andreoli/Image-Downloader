package rubensandreoli.commons.exceptions;

public class CastException extends Exception{
    private static final long serialVersionUID = 1L;

    public CastException(String message) {
        super(message);
    }

    public CastException() {
        super();
    }

}
