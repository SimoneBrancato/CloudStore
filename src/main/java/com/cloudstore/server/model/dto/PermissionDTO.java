package model.dto;

public class PermissionDTO {
    private int id; // The unique identifier for the permission
    private String category; // The category or type of the permission
    public PermissionDTO() {} // Default constructor for frameworks that require it
    
    /**
        * Constructor to initialize all fields.
        * @param id The unique identifier for the permission.
        * @param category The category or type of the permission
    **/
    public PermissionDTO(int id, String category) {
        this.id = id;
        this.category = category;
    }
    
    /**
        * Gets the unique identifier for the permission.
        * @return The permission ID.
    **/
    public int getId() {
        return id;
    }
    
    /**
        * Sets the unique identifier for the permission.
        * @param id The permission ID.
    **/
    public void setId(int id) {
        this.id = id;
    }
    
    /**
        * Gets the category or type of the permission.
        * @return The category.
    **/
    public String getCategory() {
        return category;
    }
    
    /**
        * Sets the category or type of the permission.
        * @param category The category.
    **/
    public void setCategory(String category) {
        this.category = category;
    }
}
