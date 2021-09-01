package in.apssdc.engineering.itirequirements.model;

/**
 * Created by Sandhya on 03-11-2018.
 */

public class Images {
    private String imagePath;
   // private String timeStamp;
    private String imageName;

    public Images(String imagePath, /*String timeStamp,*/ String imageName) {
        this.imagePath = imagePath;
     //   this.timeStamp = timeStamp;
        this.imageName = imageName;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

  /*  public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }*/

    public String getimageName() {
        return imageName;
    }

    public void setimageName(String imageName) {
        this.imageName = imageName;
    }
}
