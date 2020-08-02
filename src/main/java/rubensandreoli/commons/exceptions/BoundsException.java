package rubensandreoli.commons.exceptions;

public class BoundsException extends Exception{
    private static final long serialVersionUID = 1L;

    public BoundsException(String msg) {
        super(msg);
    }

    public BoundsException() {
        super();
    }

}
