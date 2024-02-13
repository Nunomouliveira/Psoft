package psoftProjectG10.deviceManagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import psoftProjectG10.deviceManagement.model.Device;
import psoftProjectG10.deviceManagement.repositories.DeviceRepository;
import psoftProjectG10.deviceManagement.services.DeviceService;
import psoftProjectG10.deviceManagement.services.FileStorageService;
import psoftProjectG10.exceptions.NotFoundException;
import psoftProjectG10.planManagement.model.Plan;
import psoftProjectG10.planManagement.repositories.PlanRepository;
import psoftProjectG10.subscriptionManagement.model.Subscription;
import psoftProjectG10.subscriptionManagement.repositories.SubscriptionRepository;
import psoftProjectG10.userManagement.model.Role;
import psoftProjectG10.userManagement.model.User;
import psoftProjectG10.userManagement.repositories.UserRepository;
import psoftProjectG10.utils.Utils;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "Devices", description = "Endpoints for managing Devices")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/device")
class DeviceController {

    private static final Logger logger = LoggerFactory.getLogger(DeviceController.class);

    private final PlanRepository planRepository;
    private final DeviceService service;

    private final FileStorageService fileStorageService;

    private final SubscriptionRepository subscriptionRepository;

    private final DeviceViewMapper deviceViewMapper;

    private final DeviceRepository deviceRepository;

    private final UserRepository userRepository;

    @Autowired
    private final Utils utils;


    @Operation(summary = "US011 - Adds a new Device")
    @RolesAllowed({Role.SUBSCRIBER, Role.ADMIN})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<DeviceView> create(HttpServletRequest request, @Valid @RequestBody final CreateDeviceRequest resource) {

        Long id = utils.getUserByToken(request);
        Long id_sub=deviceRepository.findSubscriptionIdByUserId(id);

        if(id_sub == null)
        {
            throw new NotFoundException("You are not subscribed to any plan, so you can't add a device");
        }


        resource.setSubscription(id_sub);
        final Optional<Subscription> subscriptionOptional = subscriptionRepository.findById(id_sub);
        subscriptionOptional.ifPresent(subscription -> {
            Plan planType = subscription.getPlan();

            if(planType.getMaxDevices()<=deviceRepository.countDevicesBySubscriptionId(id_sub))
                throw new IllegalArgumentException("You have reached the maximum number of devices for your plan. Please upgrade your plan.");
        });

        final var device = service.create(resource);
        final var newDeviceUri = ServletUriComponentsBuilder.fromCurrentRequestUri().pathSegment(device.getId().toString())
                .build().toUri();

        return ResponseEntity.created(newDeviceUri).eTag(Long.toString(device.getVersion()))
                .body(deviceViewMapper.toDeviceView(device));

    }

    @Operation(summary = "US012 - Deletes an existing device")
    @RolesAllowed({Role.SUBSCRIBER, Role.ADMIN})
    @DeleteMapping(value = "/{macAddress}")
    public ResponseEntity<DeviceView> delete(final WebRequest request,
                                             @PathVariable("macAddress") @Parameter(description = "The macAddress of the device to delete")
                                             final String macAddress, HttpServletRequest request1) {

        long id = utils.getUserByToken(request1);
        User user = userRepository.getById(id);

        Device device = deviceRepository.getByMacAddress(macAddress);

        if(device == null)
        {
            throw new NotFoundException("This Device doesn't exist");
        }

        Subscription subscription = device.getSubscription();
        final var User = subscription.getUser();

        if(id == User.getId() || user.getUsername().equals("admin@mail.com"))
        {
            final String ifMatchValue = request.getHeader("If-Match");
            if (ifMatchValue == null || ifMatchValue.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "You must issue a conditional DELETE using 'if-match'");
            }
            final int count = service.deleteById(macAddress, getVersionFromIfMatchHeader(ifMatchValue));

            return count == 1 ? ResponseEntity.noContent().build() : ResponseEntity.status(409).build();
        }
        else
        {
            throw new IllegalArgumentException("This Device doesn't belong to you! Choose the correct one!");
        }

    }

    @Operation(summary = "US013 - Update the details of my device")
    @RolesAllowed({Role.SUBSCRIBER, Role.ADMIN})
    @PutMapping(value = "/{macAddress}")
    public ResponseEntity<DeviceView> upsert(final WebRequest request,
                                             @PathVariable("macAddress") @Parameter(description = "The macAddress of the device to update") final String macAddress,
                                             @Valid @RequestBody final EditDeviceRequest resource, HttpServletRequest request1) {

        long id = utils.getUserByToken(request1);
        User user = userRepository.getById(id);

        Device device1 = deviceRepository.getByMacAddress(macAddress);

        if(device1 == null)
        {
            throw new NotFoundException("This Device doesn't exist");
        }

        Subscription subscription = device1.getSubscription();
        final var User = subscription.getUser();

        if(id == User.getId() || user.getUsername().equals("admin@mail.com"))
        {
            final String ifMatchValue = request.getHeader("If-Match");
            if (ifMatchValue == null || ifMatchValue.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            final var device = service.update(macAddress, resource, getVersionFromIfMatchHeader(ifMatchValue));
            return ResponseEntity.ok().eTag(Long.toString(device.getVersion())).body(deviceViewMapper.toDeviceView(device));
        }
        else
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This subscription doesn't belong to you! Choose the correct one");
        }

    }

    @Operation(summary = "US014 - Gets a list of my devices")
    @RolesAllowed({Role.SUBSCRIBER, Role.ADMIN})
    @GetMapping
    public Iterable<DeviceView> listMyDevices(HttpServletRequest request,@RequestParam("page") int page, @RequestParam("size") Integer batchSize) {

        Long id = utils.getUserByToken(request);

        User user = userRepository.getById(id);

        Long id_sub=deviceRepository.findSubscriptionIdByUserId(id);

        if((id_sub == null && !Objects.equals(user.getUsername(), "admin@mail.com")) || (id_sub != null && Objects.equals(user.getUsername(), "admin@mail.com")))
        {
            throw new NotFoundException("You are not subscribed to any plan, so you can't have any device associated");
        }

        if(user.getUsername().equals("admin@mail.com"))
        {
            return deviceViewMapper.toDeviceView(service.findAll(page,batchSize));
        }

       return deviceViewMapper.toDeviceView(service.listMyDevices(id_sub, page, batchSize));

    }

    @Operation(summary = "Gets a specific Device")
    @RolesAllowed({Role.ADMIN})
    @GetMapping(value = "/{macAddress}")
    public ResponseEntity<DeviceView> findById(
            @PathVariable("macAddress") @Parameter(description = "The mac-address of the Device to find") final String macAddress) {
        final var device = service.findOne(macAddress).orElseThrow(() -> new NotFoundException(Device.class, macAddress));

        return ResponseEntity.ok().eTag(Long.toString(device.getVersion())).body(deviceViewMapper.toDeviceView(device));
    }


    private Long getVersionFromIfMatchHeader(final String ifMatchHeader) {
        if (ifMatchHeader.startsWith("\"")) {
            return Long.parseLong(ifMatchHeader.substring(1, ifMatchHeader.length() - 1));
        }
        return Long.parseLong(ifMatchHeader);
    }


    @Operation(summary = "US016 - Uploads a photo of a device")
    @RolesAllowed({Role.SUBSCRIBER, Role.ADMIN})
    @PostMapping("/photo/{macAddress}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UploadFileResponse> uploadFile(HttpServletRequest request1,
                                                         @PathVariable("macAddress") @Parameter(description = "The macAddress of the device")
                                                         final String macAddress,
                                                         @RequestParam("file") final MultipartFile file) throws URISyntaxException {

        long id = utils.getUserByToken(request1);
        User user = userRepository.getById(id);

        Device device1 = deviceRepository.getByMacAddress(macAddress);
        Subscription subscription = device1.getSubscription();
        final var User = subscription.getUser();

        if(id == User.getId() || user.getUsername().equals("admin@mail.com"))
        {
            final UploadFileResponse up = doUploadFile(macAddress, file);

            return ResponseEntity.created(new URI(up.getFileDownloadUri())).body(up);
        }
        else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This device doesn't belong to you! Choose the correct one");
        }


    }

    public UploadFileResponse doUploadFile(final String macAddress, final MultipartFile file) {

        final String fileName = fileStorageService.storeFile(macAddress, file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentRequestUri().pathSegment(fileName)
                .toUriString();
        fileDownloadUri = fileDownloadUri.replace("/photos/", "/photo/");

        return new UploadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
    }

    @Operation(summary = "Uploads a set of photos of a device")
    @RolesAllowed({Role.SUBSCRIBER, Role.ADMIN})
    @PostMapping("/photos/{macAddress}")
    @ResponseStatus(HttpStatus.CREATED)
    public List<UploadFileResponse> uploadMultipleFiles(@PathVariable("macAddress") final String macAddress,
                                                        @RequestParam("files") final MultipartFile[] files,
                                                        HttpServletRequest request1) {

        long id = utils.getUserByToken(request1);
        User user = userRepository.getById(id);

        Device device1 = deviceRepository.getByMacAddress(macAddress);
        Subscription subscription = device1.getSubscription();
        final var User = subscription.getUser();

        if(id == User.getId() || user.getUsername().equals("admin@mail.com"))
        {
            return Arrays.asList(files).stream().map(f -> doUploadFile(macAddress, f)).collect(Collectors.toList());
        }
        else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This device doesn't belong to you! Choose the correct one");
        }
    }


    @Operation(summary = "Downloads a photo of a device")
    @RolesAllowed({Role.SUBSCRIBER, Role.ADMIN})
    @GetMapping("/photo/{macAddress}/{fileName:.+}")
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