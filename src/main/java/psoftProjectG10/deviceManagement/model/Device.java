package psoftProjectG10.deviceManagement.model;

import org.hibernate.StaleObjectStateException;
import psoftProjectG10.subscriptionManagement.model.Subscription;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long idDevice;

    @Version
    private long version;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "idSub")
    @NotNull
    private Subscription subscription;

    @Column(nullable = false)
    @NotNull
    @NotBlank
    @Size(min=1, max=50)
    private String name;

    @Column(nullable = false)
    @NotNull
    @NotBlank
    private String description;

    @Column(nullable = false, unique = true, updatable = false)
    @NotNull
    @NotBlank
    @Size(min=17, max=17)
    private String macAddress;

    protected Device() {
    }

    public Device(final Subscription subscription,final String macAddress ,final String name, final String description) {
        setSubscription(subscription);
        setMacAddress(macAddress);
        setName(name);
        setDescription(description);
    }

    public Long getId() {
        return idDevice;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(final Subscription subscription) {
        if (subscription == null) {
            throw new IllegalArgumentException("Subscription must not be null");
        }
        this.subscription = subscription;
    }

    public void setMacAddress(final String macAddress){
        String pattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
        if (macAddress == null || macAddress.isBlank() || macAddress.length() != 17) {
            throw new IllegalArgumentException("The Mac address should have 17 characters");
        }
        else {
            boolean isValid = macAddress.matches(pattern);
            if (isValid) {
                this.macAddress = macAddress;
            } else {
                throw new IllegalArgumentException("The Mac address should be valid and have this format: (AA-BB-CC-DD-EE-FF) or (AA:BB:CC:DD:EE:FF)");
            }
        }
    }

    public String getMacAddress(){return macAddress;}


    public String getName() {
        return name;
    }

    public void setName(final String name) {
        if (name == null || name.isBlank() || name.length() >= 50) {
            throw new IllegalArgumentException("Name must not be null, nor blank, nor longer than 50 characters");
        }
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        if (description == null || description.isBlank()){
            throw new IllegalArgumentException("Description must not be null or blank");
        }
        this.description = description;
    }

    public Long getVersion() {
        return version;
    }

    public void applyPatch(final long desiredVersion, final String name, final String description) {
        if (this.version != desiredVersion) {
            throw new StaleObjectStateException("Object was already modified by another user", this.idDevice);
        }
        if (name != null) {
            setName(name);
        }
        if (description != null) {
            setDescription(description);
        }
    }

    public void updateData(final long desiredVersion, final String name, final String description) {
        if (this.version != desiredVersion) {
            throw new StaleObjectStateException("Object was already modified by another user", this.idDevice);
        }
        setName(name);
        setDescription(description);
        this.version++;
    }
}