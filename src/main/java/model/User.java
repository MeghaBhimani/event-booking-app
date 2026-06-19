package model;

public class User {

    private String username;
    private String password;      // always stored encrypted (Caesar cipher)
    private String preferredName;

    public User() {}

    public User(String username, String password, String preferredName) {
        this.username      = username;
        this.password      = password;
        this.preferredName = preferredName;
    }

    public String getUsername()      { return username; }
    public String getPassword()      { return password; }
    public String getPreferredName() { return preferredName; }

    public void setUsername(String username)           { this.username      = username; }
    public void setPassword(String password)           { this.password      = password; }
    public void setPreferredName(String preferredName) { this.preferredName = preferredName; }
}
