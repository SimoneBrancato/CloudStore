package model.dto;

public class PermissionDTO {
    private int id;
    private String category;
    
    public PermissionDTO() {}
    
    public PermissionDTO(int id, String category) {
        this.id = id;
        this.category = category;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
}
