package psoftProjectG10.userManagement.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import psoftProjectG10.exceptions.ConflictException;
import psoftProjectG10.userManagement.api.*;
import psoftProjectG10.userManagement.model.User;
import psoftProjectG10.userManagement.repositories.UserRepository;

import javax.validation.ValidationException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepo;
    private final EditUserMapper userEditMapper;
    private final UserViewMapper userViewMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Iterable<User> findAll() {
        return userRepo.findAll();
    }

    @Transactional
    public UserView create(final CreateUserRequest request) {
        if (userRepo.findByUsername(request.getUsername()).isPresent()) {
            throw new ConflictException("Username already exists!");
        }
        if (!request.getPassword().equals(request.getRePassword())) {
            throw new ValidationException("Passwords don't match!");
        }

        User user = userEditMapper.create(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());

        user = userRepo.save(user);

        return userViewMapper.toUserView(user);
    }

    @Transactional
    public UserView update(final Long id, final EditUserRequest request) {
        User user = userRepo.getById(id);
        userEditMapper.update(request, user);

        user = userRepo.save(user);

        return userViewMapper.toUserView(user);
    }

    @Transactional
    public UserView upsert(final CreateUserRequest request) {
        final Optional<User> optionalUser = userRepo.findByUsername(request.getUsername());

        if (optionalUser.isEmpty()) {
            return create(request);
        }
        final EditUserRequest updateUserRequest = new EditUserRequest(request.getFullName(), request.getAuthorities());
        return update(optionalUser.get().getId(), updateUserRequest);
    }

    @Transactional
    public UserView delete(final Long id) {
        User user = userRepo.getById(id);

        user.setEnabled(false);
        user = userRepo.save(user);

        return userViewMapper.toUserView(user);
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        return userRepo.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException(String.format("User with username - %s, not found", username)));
    }

    public boolean usernameExists(final String username) {
        return userRepo.findByUsername(username).isPresent();
    }

    public UserView getUser(final Long id) {
        return userViewMapper.toUserView(userRepo.getById(id));
    }


    public UserView getUserByUsername(final String username) {
        return userViewMapper.toUserView(userRepo.getByUsername(username));
    }

    public List<UserView> searchUsers(Page page, SearchUsersQuery query) {
        if (page == null) {
            page = new Page(1, 10);
        }
        if (query == null) {
            query = new SearchUsersQuery("", "");
        }
        final List<User> users = userRepo.searchUsers(page, query);
        return userViewMapper.toUserView(users);
    }

}
