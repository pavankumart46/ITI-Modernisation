package in.apssdc.engineering.itirequirements.model;

/**
 * Created by Sandhya on 03-11-2018.
 */

public class Videos {
    private String videoPath;
    private String videoName;
    //private String timeStamp;

    public Videos(String videoPath, String videoName/*, String timeStamp*/) {
        this.videoPath = videoPath;
        this.videoName = videoName;
        //this.timeStamp = timeStamp;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    /*public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }*/
}
