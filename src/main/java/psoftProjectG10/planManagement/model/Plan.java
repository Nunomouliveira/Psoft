package psoftProjectG10.planManagement.model;

import org.hibernate.StaleObjectStateException;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Entity
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long idPlan;

    @Version
    private long version;

    @Column(nullable = false, unique = true, updatable = false)
    @NotBlank
    @NotNull
    private String planType;

    @Column(nullable = false)
    @Min(0)
    private Integer numMinutes;

    @Column(nullable = false)
    @Min(0)
    private Integer maxDevices;

    @Column(nullable = false)
    @Min(0)
    private Integer musicCollections;

    @Column(nullable = false)
    @NotBlank
    private String musicSuggestions;

    @Column(nullable = false)
    @Min(0)
    private Double monthlyFee;

    @Column(nullable = false)
    @Min(0)
    private Double annualFee;

    @Column(nullable = false)
    private String planState = "Active";

    @Column(nullable = false)
    private String planPromote = "Not Promoted";

    @Column(nullable = false)
    @NotBlank
    private String htmlDescription;


    protected Plan() {
    }

    public Plan(final String planType, final Integer numMinutes, final Integer maxDevices, final Integer musicCollections,
                final String musicSuggestions, final Double monthlyFee, final Double annualFee,
                final String htmlDescription) {
        setPlanType(planType);
        setNumMinutes(numMinutes);
        setMaxDevices(maxDevices);
        setMusicCollections(musicCollections);
        setMusicSuggestions(musicSuggestions);
        setMonthlyFee(monthlyFee);
        setAnnualFee(annualFee);
        setHtmlDescription(htmlDescription);
    }

    public Long getId() {
        return idPlan;
    }

    public void setId(Long idPlan) {
        this.idPlan = idPlan;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(final String planType) {
        if (planType == null || planType.isBlank()) {
            throw new IllegalArgumentException("PlanType must not be null, nor blank");
        }
        this.planType = planType;
    }

    public String getPlanPromote() {
        return planPromote;
    }

    public void setPlanPromote(String planPromote) {
        if(!planPromote.equals("Promoted") && !planPromote.equals("Not Promoted"))
        {
            throw new IllegalArgumentException("You must type 'Promoted', just like that");
        }
        else{
            this.planPromote = planPromote;
        }
    }

    public Integer getNumMinutes() {
        return numMinutes;
    }

    public void setNumMinutes(final Integer numMinutes) {
        if (numMinutes == null) {
            throw new IllegalArgumentException("numMinutes must not be null");
        }
        this.numMinutes = numMinutes;
    }

    public Integer getMaxDevices() {
        return maxDevices;
    }

    public void setMaxDevices(final Integer maxDevices) {
        if (maxDevices == null) {
            throw new IllegalArgumentException("MaxDevices must not be null");
        }
        this.maxDevices = maxDevices;
    }

    public Integer getMusicCollections() {
        return musicCollections;
    }

    public void setMusicCollections(final Integer musicCollections) {
        if (musicCollections == null) {
            throw new IllegalArgumentException("MusicCollections must not be null");
        }
        this.musicCollections = musicCollections;
    }

    public String getMusicSuggestions() {
        return musicSuggestions;
    }

    public void setMusicSuggestions(final String musicSuggestions) {
        if (musicSuggestions == null || musicSuggestions.isBlank()) {
            throw new IllegalArgumentException("MusicSuggestions must not be null, nor blank");
        }
        this.musicSuggestions = musicSuggestions;
    }

    public Double getMonthlyFee() {
        return monthlyFee;
    }

    public void setMonthlyFee(final Double monthlyFee) {
        if (monthlyFee == null) {
            throw new IllegalArgumentException("MonthlyFee must not be null, nor blank");
        }
        this.monthlyFee = monthlyFee;
    }

    public Double getAnnualFee() {
        return annualFee;
    }

    public void setAnnualFee(final Double annualFee) {
        if (annualFee == null) {
            throw new IllegalArgumentException("AnnualFee must not be null, nor blank");
        }
        this.annualFee = annualFee;
    }

    public String getPlanState() {
        return planState;
    }

    public void setPlanState(final String planState) {
        if (!planState.equals("Deactivated") && !planState.equals("Ceased")) {
            throw new IllegalArgumentException("If you want to deactivate a plan, you must type -Deactivated-");
        }
        this.planState = planState;
    }
    public String getHtmlDescription() {
        return htmlDescription;
    }
    public void setHtmlDescription(final String htmlDescription) {
        if (htmlDescription == null || htmlDescription.isBlank()) {
            throw new IllegalArgumentException("HTMLDescription must not be null, nor blank");
        }
        this.htmlDescription = htmlDescription;
    }

    public Long getVersion() {
        return version;
    }

    public void applyPatch(final long desiredVersion, final Integer numMinutes, final Integer maxDevices,
                           final Integer musicCollections,
                           final String musicSuggestions,
                           final String planState,
                           final String planPromote,
                           final Double monthlyFee,
                           final Double annualFee,
                           final String htmlDescription) {
        if (this.version != desiredVersion) {
            throw new StaleObjectStateException("Object was already modified by another user", this.idPlan);
        }
        if (numMinutes != null) {
            setNumMinutes(numMinutes);
        }
        if (maxDevices != null) {
            setMaxDevices(maxDevices);
        }
        if (musicCollections != null) {
            setMusicCollections(musicCollections);
        }
        if (musicSuggestions != null) {
            setMusicSuggestions(musicSuggestions);
        }
        if (planState != null) {
            setPlanState(planState);
        }
        if (planPromote != null) {
            setPlanPromote(planPromote);
        }
        if (monthlyFee != null) {
            setMonthlyFee(monthlyFee);
        }
        if (annualFee != null) {
            setAnnualFee(annualFee);
        }
        if (htmlDescription != null) {
            setHtmlDescription(htmlDescription);
        }
    }

}