package psoftProjectG10.subscriptionManagement.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hibernate.StaleObjectStateException;
import psoftProjectG10.planManagement.model.Plan;
import psoftProjectG10.userManagement.model.User;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long idSub;

    @Version
    private long version;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "idPlan")
    private Plan plan;

    @OneToOne
    @JoinColumn(name = "id", nullable = true, unique = true, updatable = true)
    private User user;

    @Column(nullable = false)
    private LocalDate cancelDate =  LocalDate.of(9999, 12, 31);

    @Column(nullable = false)
    private LocalDate renewDate;
    @Column(nullable = false)
    private LocalDate startDate =  LocalDate.now();

    @Column(nullable = false)
    private SubscriptionStatus subState = SubscriptionStatus.Active;

    @Column(nullable = false)
    @NotBlank
    private String feeType;

    @Transient
    private Integer cancellations=0;

    @Transient
    private Integer subscriptions=0;

    @Column(length = 600, nullable = false)
    @NotBlank
    private String funnyQuote=retrieveDataFromApi();
    @Column(nullable = false)
    private LocalDate lastUpdateDate = LocalDate.now();

    @Column(nullable = false)
    private transient String[] weather;

    protected Subscription() {
    }

    public Subscription(final Plan plan, final User user, final String feeType) {
        setPlan(plan);
        setUser(user);
        setFeeType(feeType);
        this.weather = getWeatherInfo();
    }

    public String[] getWeather() {
        return weather;
    }

    public void setWeather(String[] weather) {
        this.weather = weather;
    }

    public Long getId() {
        return idSub;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(final Plan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("Plan must not be null");
        }
        this.plan = plan;
    }
    public String getFunnyQuote() {
        return funnyQuote;
    }

    public void setFunnyQuote(String funnyQuote) {
        this.funnyQuote = funnyQuote;
    }

    public SubscriptionStatus getSubState() {
        return subState;
    }

    public void setSubState(final SubscriptionStatus subState) {
        this.subState = subState;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getRenewDate() {
        return renewDate;
    }

    public void setRenewDate(LocalDate renewDate) {
        if(renewDate.isBefore(LocalDate.now()))
        {
            setSubState(SubscriptionStatus.Expired);
        }
        this.renewDate = renewDate;
    }

    public LocalDate getCancelDate() {
        return cancelDate;
    }

    public void setCancelDate(LocalDate cancelDate) {
        this.cancelDate = cancelDate;
    }

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(final String feeType) {
        if (feeType.isBlank() || (!feeType.equals("Monthly") && !feeType.equals("Annual"))) {
            throw new IllegalArgumentException("FeeType must not be blank and must be -Monthly- or -Annual-");
        }
        this.feeType = feeType;
    }

    public Integer getCancellations() {
        return cancellations;
    }

    public void setCancellations(Integer cancellations) {
        this.cancellations = cancellations;
    }

    public Integer getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Integer subscriptions) {
        this.subscriptions = subscriptions;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(final User user)
    {
        this.user=user;
    }


    public Long getVersion() {
        return version;
    }

    public void applyPatch(final long desiredVersion,  final String feeType) {
        if (this.version != desiredVersion) {
            throw new StaleObjectStateException("Object was already modified by another user", this.idSub);
        }

        if (feeType != null) {
            setFeeType(feeType);
        }
    }

    public void updateData(final long desiredVersion,  final String feeType) {
        if (this.version != desiredVersion) {
            throw new StaleObjectStateException("Object was already modified by another user", this.idSub);
        }

        setFeeType(feeType);
    }
    public String retrieveDataFromApi() {
        String baseUrl = "https://api.api-ninjas.com/v1/quotes?category=funny";

        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpGet request = new HttpGet(baseUrl);
            request.setHeader("X-Api-Key", "uoRAzOXc9UTovV0+dm8ZxA==tfRtWOhzVmr0nRlY");

            CloseableHttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(result);
            String content = jsonNode.get(0).get("quote").asText();

            if (content.length() >= 600) {
                content = content.substring(0, 600);
            }

            return content;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void updateFunnyQuote() {
        LocalDate currentDate = LocalDate.now();
        long daysPassed = ChronoUnit.DAYS.between(lastUpdateDate, currentDate);

        if (daysPassed >= 1) {
            this.funnyQuote = retrieveDataFromApi();
            this.lastUpdateDate = currentDate;
        }
    }

    public String[] getWeatherInfo() {
        String apiKey = "18e350e9b69a408a8d6215322231806";

        try {

            String apiKey1 = "CC3E12277523526B1CF15F01570C7F04";

            URL url3 = new URL("https://icanhazip.com");
            HttpURLConnection connection2 = (HttpURLConnection) url3.openConnection();
            connection2.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection2.getInputStream()));
            String ipAddress = reader.readLine().trim();

            String apiUrl = "http://api.ip2location.io/?key=" + apiKey1 + "&ip=" + ipAddress;

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader1 = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader1.readLine()) != null) {
                    response.append(line);
                }
                reader1.close();

                String[] responseData = response.toString().split(",");
                String latitude2 = responseData[5];
                String longitude2 = responseData[6];

                int startIndex = latitude2.indexOf(":") + 1;
                int startIndex1 = longitude2.indexOf(":") + 1;

                String numberStr = latitude2.substring(startIndex).trim();
                String numberStr1 = longitude2.substring(startIndex1).trim();


                double latitudeNumber = Double.parseDouble(numberStr);
                double longitudeNumber = Double.parseDouble(numberStr1);

                String urlString = "https://api.weatherapi.com/v1/current.json?key=" + apiKey + "&q=" + latitudeNumber + "," + longitudeNumber;

                URL url2 = new URL(urlString);
                HttpURLConnection connection1 = (HttpURLConnection) url2.openConnection();
                connection1.setRequestMethod("GET");

                BufferedReader reader2 = new BufferedReader(new InputStreamReader(connection1.getInputStream()));
                StringBuilder response1 = new StringBuilder();
                String line1;
                while ((line1 = reader2.readLine()) != null) {
                    response1.append(line1);
                }
                reader2.close();

                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(response1.toString(), JsonObject.class);

                JsonObject locationObject = jsonObject.getAsJsonObject("location");
                String locationName = locationObject.get("name").getAsString();
                String region = locationObject.get("region").getAsString();
                String country = locationObject.get("country").getAsString();
                double latitude1 = locationObject.get("lat").getAsDouble();
                double longitude1 = locationObject.get("lon").getAsDouble();
                String localTime = locationObject.get("localtime").getAsString();

                JsonObject currentObject = jsonObject.getAsJsonObject("current");
                double temperature = currentObject.get("temp_c").getAsDouble();
                String conditionText = currentObject.getAsJsonObject("condition").get("text").getAsString();
                int humidity = currentObject.get("humidity").getAsInt();
                double windSpeed = currentObject.get("wind_kph").getAsDouble();

                String[] weatherData = new String[8];
                weatherData[0] = "Local time: " + localTime;
                weatherData[1] = "Location: " + locationName + ", " + region + ", " + country;
                weatherData[2] = "Latitude :" + String.valueOf(latitude1);
                weatherData[3] = "Longitude: " + String.valueOf(longitude1);
                weatherData[4] = "Temperature (Cº): " + String.valueOf(temperature);
                weatherData[5] = "Humidity (%): " + String.valueOf(humidity);
                weatherData[6] = "Wind speed (Km/h): " + windSpeed;
                weatherData[7] = "Conditions: " + conditionText;

                return weatherData;

            } else {
                System.out.println("Error: " + responseCode);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return new String[0];
    }
}
