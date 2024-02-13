package psoftProjectG10.userManagement.api;

import org.mapstruct.Mapper;
import psoftProjectG10.userManagement.model.User;

import java.util.List;
@Mapper(componentModel = "spring")
public abstract class UserViewMapper {

	public abstract UserView toUserView(User user);

	public abstract Iterable<UserView> toUserView(Iterable<User> users);

	public abstract List<UserView> toUserView(List<User> users);
}
