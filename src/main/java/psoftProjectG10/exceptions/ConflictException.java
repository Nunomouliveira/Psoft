package psoftProjectG10.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.net.MalformedURLException;


@ResponseStatus(code = HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ConflictException(final String string) {
        super(string);
    }

    public ConflictException(final String string, final MalformedURLException ex) {
        super(string, ex);
    }

    public ConflictException(final Class<?> clazz, final long id) {
        super(String.format("Entity %s with id %d not found", clazz.getSimpleName(), id));
    }

    public ConflictException(final Class<?> clazz, final String id) {
        super(String.format("Entity %s with id %s not found", clazz.getSimpleName(), id));
    }
}
