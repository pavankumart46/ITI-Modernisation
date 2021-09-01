package in.apssdc.engineering.itirequirements.model;

public class MAPSFILE {
    private String maps_file_path;
    private String area;
    private String description;

    public MAPSFILE(String maps_file_path, String area, String description) {
        this.maps_file_path = maps_file_path;
        this.area = area;
        this.description = description;
    }

    public String getMaps_file_path() {
        return maps_file_path;
    }

    public void setMaps_file_path(String maps_file_path) {
        this.maps_file_path = maps_file_path;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
