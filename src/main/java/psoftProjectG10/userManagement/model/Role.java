package psoftProjectG10.userManagement.model;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.security.core.GrantedAuthority;

@Value
@AllArgsConstructor
public class Role implements GrantedAuthority {

    private static final long serialVersionUID = 1L;

    public static final String ADMIN = "ADMIN";
    public static final String MARKETING_DIRECTOR = "MARKETING_DIRECTOR";
    public static final String FINANCIAL_DIRECTOR = "FINANCIAL_DIRECTOR";
    public static final String SUBSCRIBER = "SUBSCRIBER";
    public static final String PRODUCT_MANAGER = "PRODUCT_MANAGER";
    public static final String CUSTOMER = "CUSTOMER";

    private String authority;
}

