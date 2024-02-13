package psoftProjectG10.userManagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import psoftProjectG10.deviceManagement.services.FileStorageService;
import psoftProjectG10.userManagement.model.Role;
import psoftProjectG10.userManagement.model.User;
import psoftProjectG10.userManagement.repositories.UserRepository;
import psoftProjectG10.userManagement.services.UserService;
import psoftProjectG10.utils.Utils;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "User")
@RestController
@RequestMapping(path = "api/user")
@RequiredArgsConstructor
public class UserAdminApi {

	private static final Logger logger = LoggerFactory.getLogger(UserAdminApi.class);

	private final UserService userService;

	private final UserViewMapper userViewMapper;

	private final FileStorageService fileStorageService;

	private final UserRepository userRepository;


	@Autowired
	private final Utils utils;

	@Operation(summary = "Get all users")
	@RolesAllowed({Role.ADMIN})
	@GetMapping(value = "/all")
	public Iterable<UserView> findAll() {
		return userViewMapper.toUserView(userService.findAll());
	}

	@Operation(summary = "create a user")
	@RolesAllowed({Role.ADMIN})
	@PostMapping(value = "/create")
	public UserView create(@RequestBody @Valid final CreateUserRequest request) {
		return userService.create(request);
	}

	@Operation(summary = "Update a user")
	@RolesAllowed({Role.ADMIN})
	@PutMapping("/update/{id}")
	public UserView update(@PathVariable final Long id, @RequestBody @Valid final EditUserRequest request) {
		return userService.update(id, request);
	}

	@Operation(summary = "Delete a user")
	@RolesAllowed({Role.ADMIN})
	@DeleteMapping("{id}")
	public UserView delete(@PathVariable final Long id) {
		return userService.delete(id);
	}

	@Operation(summary = "Get a specific user")
	@RolesAllowed({Role.ADMIN})
	@GetMapping("{id}")
	public UserView get(@PathVariable final Long id) {
		return userService.getUser(id);
	}

	@Operation(summary = "Search user by his name")
	@RolesAllowed({Role.ADMIN})
	@GetMapping("/search/{username}")
	public UserView get(@PathVariable final String username) {
		return userService.getUserByUsername(username);
	}

	@Operation(summary = "Search user by his name")
	@RolesAllowed({Role.ADMIN})
	@PostMapping("/search")
	public ListResponse<UserView> search(@RequestBody final SearchRequest<SearchUsersQuery> request) {
		return new ListResponse<>(userService.searchUsers(request.getPage(), request.getQuery()));
	}

	@Operation(summary = "Uploads a photo of a user - US015")
	@RolesAllowed({Role.SUBSCRIBER, Role.ADMIN, Role.CUSTOMER, Role.PRODUCT_MANAGER, Role.MARKETING_DIRECTOR})
	@PostMapping("/photo")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<UploadFileResponse> uploadFile(@RequestParam("file") final MultipartFile file,
														 HttpServletRequest request1) throws URISyntaxException {

		long id = utils.getUserByToken(request1);
		User user = userRepository.getById(id);

		final UploadFileResponse up = doUploadFile(user.getUsername(), file);

		return ResponseEntity.created(new URI(up.getFileDownloadUri())).body(up);

	}

	public UploadFileResponse doUploadFile(final String username, final MultipartFile file) {

		final String fileName = fileStorageService.storeFile(username, file);

		String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentRequestUri().pathSegment(fileName)
				.toUriString();
		fileDownloadUri = fileDownloadUri.replace("/photos/", "/photo/");

		return new UploadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
	}

	@Operation(summary = "Uploads a set of photos of a user")
	@RolesAllowed({Role.SUBSCRIBER, Role.ADMIN, Role.CUSTOMER, Role.PRODUCT_MANAGER, Role.MARKETING_DIRECTOR})
	@PostMapping("/photos")
	@ResponseStatus(HttpStatus.CREATED)
	public List<UploadFileResponse> uploadMultipleFiles(@PathVariable("username") final String username,
														@RequestParam("files") final MultipartFile[] files,
														HttpServletRequest request1) {

		long id = utils.getUserByToken(request1);
		User user = userRepository.getByUsername(username);

		if(id == user.getId() || user.getUsername().equals("admin@mail.com"))
		{
			return Arrays.asList(files).stream().map(f -> doUploadFile(username, f)).collect(Collectors.toList());
		}
		else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"This username isn't yours. Choose the correct one");
		}
	}


	@Operation(summary = "Downloads a photo of a user")
	@RolesAllowed({Role.SUBSCRIBER, Role.ADMIN, Role.CUSTOMER, Role.PRODUCT_MANAGER, Role.MARKETING_DIRECTOR})
	@GetMapping("/photo/{fileName:.+}")
	public ResponseEntity<Resource> downloadFile(@PathVariable final String fileName,
												 final HttpServletRequest request) {
		final Resource resource = fileStorageService.loadFileAsResource(fileName);

		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (final IOException ex) {
			logger.info("Could not determine file type.");
		}

		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}
}
