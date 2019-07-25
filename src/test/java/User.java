public class User {
    private Object email;
    private String password;
    private String address;
    private String country;
    private String isCode;

    public Object getEmail() {
        return email;
    }

    public void setEmail(Object email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public  String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public  String getIsCode() {
        return isCode;
    }

    public void setIsCode(String isCode) {
        this.isCode = isCode;
    }

    public User(Object email, String password) {
        this.email = email;
        this.password = password;
    }
}
