package psoftProjectG10.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import java.util.*;
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private final Logger logger = LogManager.getLogger();

	@ExceptionHandler(value = { org.hibernate.StaleObjectStateException.class, ConflictException.class })
	@ResponseStatus(HttpStatus.CONFLICT)
	protected ResponseEntity<Object> handleConflict(final HttpServletRequest request, final Exception ex) {
		logger.error("ConflictException {}\n", request.getRequestURI(), ex);

		final Map<String, String> details = new HashMap<>();
		details.put("message", "Object was updated by another user");
		details.put("error", ex.getMessage());

		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiCallError<>("Conflict", details.entrySet()));
	}

	@ExceptionHandler({ ConstraintViolationException.class })
	@ResponseStatus(HttpStatus.CONFLICT)
	protected ResponseEntity<Object> handleConstraintViolation(final HttpServletRequest request,
			final ConstraintViolationException ex) {
		logger.error("ConstraintViolationException {}\n", request.getRequestURI(), ex);

		final Map<String, String> details = new HashMap<>();
		details.put("message", "The identity of the object you tried to create is already in use");
		details.put("error", ex.getMessage());
		details.put("constraint", ex.getConstraintName());
		details.put("state", ex.getSQLState());

		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiCallError<>("Conflict", details.entrySet()));
	}

	@ExceptionHandler({ DataIntegrityViolationException.class })
	@ResponseStatus(HttpStatus.CONFLICT)
	protected ResponseEntity<Object> handleDataIntegrityViolation(final HttpServletRequest request,
			final DataIntegrityViolationException ex) {
		logger.error("DataIntegrityViolationException {}\n", request.getRequestURI(), ex);

		final Map<String, String> details = new HashMap<>();
		details.put("message", "The identity of the object you tried to create is already in use");
		details.put("error", ex.getMessage());

		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiCallError<>("Conflict", details.entrySet()));
	}

	@ExceptionHandler({ IllegalArgumentException.class, NumberFormatException.class })
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	protected ResponseEntity<Object> handleIllegalArgument(final HttpServletRequest request,
			final IllegalArgumentException ex) {
		logger.error("BadRequestException {}\n", request.getRequestURI(), ex);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ApiCallError<>("Bad Request", List.of(ex.getMessage())));
	}

	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<ApiCallError<String>> handleNotFoundException(final HttpServletRequest request,
			final NotFoundException ex) {
		logger.error("NotFoundException {}\n", request.getRequestURI(), ex);

		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiCallError<>("Not found", List.of(ex.getMessage())));
	}

	@ExceptionHandler(ValidationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<ApiCallError<String>> handleValidationException(final HttpServletRequest request,
			final ValidationException ex) {
		logger.error("ValidationException {}\n", request.getRequestURI(), ex);

		return ResponseEntity.badRequest()
				.body(new ApiCallError<>("Bad Request: Validation Failed", List.of(ex.getMessage())));
	}

	/*
	 * @ExceptionHandler(MissingServletRequestParameterException.class)
	 *
	 * @ResponseStatus(HttpStatus.BAD_REQUEST) public
	 * ResponseEntity<ApiCallError<String>>
	 * handleMissingServletRequestParameterException( final HttpServletRequest
	 * request, final MissingServletRequestParameterException ex) {
	 * logger.error("handleMissingServletRequestParameterException {}\n",
	 * request.getRequestURI(), ex);
	 *
	 * return ResponseEntity.badRequest() .body(new
	 * ApiCallError<>("Missing request parameter", List.of(ex.getMessage()))); }
	 */

	/*
	 * @ExceptionHandler(MethodArgumentTypeMismatchException.class)
	 *
	 * @ResponseStatus(HttpStatus.BAD_REQUEST) public
	 * ResponseEntity<ApiCallError<Map.Entry<String, String>>>
	 * handleMethodArgumentTypeMismatchException( final HttpServletRequest request,
	 * final MethodArgumentTypeMismatchException ex) {
	 * logger.error("handleMethodArgumentTypeMismatchException {}\n",
	 * request.getRequestURI(), ex);
	 *
	 * final Map<String, String> details = new HashMap<>(); details.put("paramName",
	 * ex.getName()); details.put("paramValue",
	 * ofNullable(ex.getValue()).map(Object::toString).orElse(""));
	 * details.put("errorMessage", ex.getMessage());
	 *
	 * return ResponseEntity.badRequest() .body(new
	 * ApiCallError<>("Method argument type mismatch", details.entrySet())); }
	 */

	/**
	 * handle validation errors generated by @Valid
	 */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex,
			final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
		logger.error("handleMethodArgumentNotValidException {}\n", request.getDescription(false), ex);
		final List<Map<String, String>> details = new ArrayList<>();
		ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
			final Map<String, String> detail = new HashMap<>();
			detail.put("objectName", fieldError.getObjectName());
			detail.put("field", fieldError.getField());
			detail.put("rejectedValue", "" + fieldError.getRejectedValue());
			detail.put("errorMessage", fieldError.getDefaultMessage());
			details.add(detail);
		});

		return ResponseEntity.badRequest().body(new ApiCallError<>("Method argument validation failed", details));
	}

	/**
	 * handle validation errors generated by @Valid
	 */
	/*
	 * @ExceptionHandler(MethodArgumentNotValidException.class)
	 *
	 * @ResponseStatus(HttpStatus.BAD_REQUEST) public
	 * ResponseEntity<ApiCallError<Map<String, String>>>
	 * handleMethodArgumentNotValidException( final HttpServletRequest request,
	 * final MethodArgumentNotValidException ex) {
	 * logger.error("handleMethodArgumentNotValidException {}\n",
	 * request.getRequestURI(), ex);
	 *
	 * final List<Map<String, String>> details = new ArrayList<>();
	 * ex.getBindingResult().getFieldErrors().forEach(fieldError -> { final
	 * Map<String, String> detail = new HashMap<>(); detail.put("objectName",
	 * fieldError.getObjectName()); detail.put("field", fieldError.getField());
	 * detail.put("rejectedValue", "" + fieldError.getRejectedValue());
	 * detail.put("errorMessage", fieldError.getDefaultMessage());
	 * details.add(detail); });
	 *
	 * return ResponseEntity.badRequest().body(new
	 * ApiCallError<>("Method argument validation failed", details)); }
	 */
	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ResponseEntity<ApiCallError<String>> handleAccessDeniedException(final HttpServletRequest request,
			final AccessDeniedException ex) {
		logger.error("handleAccessDeniedException {}\n", request.getRequestURI(), ex);

		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(new ApiCallError<>("Access denied!", List.of(ex.getMessage())));
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<ApiCallError<String>> handleInternalServerError(final HttpServletRequest request,
			final Exception ex) {
		logger.error("handleInternalServerError {}\n", request.getRequestURI(), ex);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ApiCallError<>("Internal server error", List.of(ex.getMessage())));
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ApiCallError<T> {

		private String message;
		private Collection<T> details;
	}
}
