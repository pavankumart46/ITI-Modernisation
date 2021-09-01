package in.apssdc.engineering.itirequirements.model;

/**
 * Created by Sandhya on 08-11-2018.
 */

public class Recordings {
    private String recordingPath;
    private String recordingName;
    //private String timeStamp;

    public Recordings(String recordingPath, String recordingName /*String timeStamp*/) {
        this.recordingPath = recordingPath;
        this.recordingName = recordingName;
        //this.timeStamp = timeStamp;
    }

    public String getRecordingPath() {
        return recordingPath;
    }

    public void setRecordingPath(String recordingPath) {
        this.recordingPath = recordingPath;
    }

    public String getRecordingName() {
        return recordingName;
    }

    public void setRecordingName(String recordingName) {
        this.recordingName = recordingName;
    }

    /*public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }*/
}
