package com.cloudstore.server.model.dto;

public class UserDTO {
    private String nickname; // The unique nickname of the user
    private String name; // The first name of the user
    private String surname; // The last name of the user
    private String email; // The email address of the user
    private String password; // The password of the user
    private PermissionDTO permission; // The permission level of the user
    
    public UserDTO() {} // Default constructor
    
    /**
        * Constructor to initialize all fields.
        * @param nickname The unique nickname of the user.
        * @param name The first name of the user.
        * @param surname The last name of the user.
        * @param email The email address of the user.
        * @param password The password of the user.
        * @param permission The permission level of the user.
    **/
    public UserDTO(String nickname, String name, String surname, String email, 
                   String password, PermissionDTO permission) {
        this.nickname = nickname;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.permission = permission;
    }
    
    /**
        * Gets the unique nickname of the user.
        * @return The user's nickname.
    **/
    public String getNickname() {
        return nickname;
    }
    
    /**
        * Sets the unique nickname of the user.
        * @param nickname The user's nickname.
    **/
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    /**
        * Gets the first name of the user.
        * @return The user's first name.
    **/
    public String getName() {
        return name;
    }
    
    /**
        * Sets the first name of the user.
        * @param name The user's first name.
    **/
    public void setName(String name) {
        this.name = name;
    }
    
    /**
        * Gets the last name of the user.
        * @return The user's last name.
    **/
    public String getSurname() {
        return surname;
    }
    
    /**
        * Sets the last name of the user.
        * @param surname The user's last name.
    **/
    public void setSurname(String surname) {
        this.surname = surname;
    }
    
    /**
        * Gets the email address of the user.
        * @return The user's email address.
    **/
    public String getEmail() {
        return email;
    }

    /**
        * Sets the email address of the user.
        * @param email The user's email address.
    **/
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
        * Gets the password of the user.
        * @return The user's password.
    **/
    public String getPassword() {
        return password;
    }
    
    /**
        * Sets the password of the user.
        * @param password The user's password.
    **/
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
        * Gets the permission level of the user.
        * @return The user's permission.
    **/
    public PermissionDTO getPermission() {
        return permission;
    }
    
    /**
        * Sets the permission level of the user.
        * @param permission The user's permission.
    **/
    public void setPermission(PermissionDTO permission) {
        this.permission = permission;
    }
}
