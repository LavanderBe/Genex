package Genex.entities;

public class Center {

    private String centerId;
    private String name;
    private String address;
    private String city;
    private String contactEmail;
    private String mapUrl;

    // Default constructor
    public Center() {}

    // Full constructor
    public Center(String name, String address, String city, String contactEmail, String mapUrl) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.contactEmail = contactEmail;
        this.mapUrl = mapUrl;
    }

    // Getters & Setters
    public String getCenterId() { return centerId; }
    public void setCenterId(String centerId) { this.centerId = centerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getMapUrl() { return mapUrl; }
    public void setMapUrl(String mapUrl) { this.mapUrl = mapUrl; }

    @Override
    public String toString() {
        return "Center{" +
                "centerId='" + centerId + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                ", mapUrl='" + mapUrl + '\'' +
                '}';
    }
}