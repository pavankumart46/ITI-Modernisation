package in.apssdc.engineering.itirequirements.model;

/**
 * Created by Sandhya on 08-11-2018.
 */

public class ManagementDetailsModel {

    private String yearOfEstablishment, capitalExpenditure, unutilizedFund, challengesInManagement, isManagementCommittee, presentCapitalExpenditure, presentOperatingExpenditure,timeStamp;

    public ManagementDetailsModel(String yearOfEstablishment, String capitalExpenditure, String unutilizedFund, String challengesInManagement, String isManagementCommittee, String presentCapitalExpenditure, String presentOperatingExpenditure, String timeStamp) {
        this.yearOfEstablishment = yearOfEstablishment;
        this.capitalExpenditure = capitalExpenditure;
        this.unutilizedFund = unutilizedFund;
        this.challengesInManagement = challengesInManagement;
        this.isManagementCommittee = isManagementCommittee;
        this.presentCapitalExpenditure = presentCapitalExpenditure;
        this.presentOperatingExpenditure = presentOperatingExpenditure;
        this.timeStamp = timeStamp;
    }

    public String getYearOfEstablishment() {
        return yearOfEstablishment;
    }

    public void setYearOfEstablishment(String yearOfEstablishment) {
        this.yearOfEstablishment = yearOfEstablishment;
    }

    public String getCapitalExpenditure() {
        return capitalExpenditure;
    }

    public void setCapitalExpenditure(String capitalExpenditure) {
        this.capitalExpenditure = capitalExpenditure;
    }

    public String getUnutilizedFund() {
        return unutilizedFund;
    }

    public void setUnutilizedFund(String unutilizedFund) {
        this.unutilizedFund = unutilizedFund;
    }

    public String getChallengesInManagement() {
        return challengesInManagement;
    }

    public void setChallengesInManagement(String challengesInManagement) {
        this.challengesInManagement = challengesInManagement;
    }

    public String getIsManagementCommittee() {
        return isManagementCommittee;
    }

    public void setIsManagementCommittee(String isManagementCommittee) {
        this.isManagementCommittee = isManagementCommittee;
    }

    public String getPresentCapitalExpenditure() {
        return presentCapitalExpenditure;
    }

    public void setPresentCapitalExpenditure(String presentCapitalExpenditure) {
        this.presentCapitalExpenditure = presentCapitalExpenditure;
    }

    public String getPresentOperatingExpenditure() {
        return presentOperatingExpenditure;
    }

    public void setPresentOperatingExpenditure(String presentOperatingExpenditure) {
        this.presentOperatingExpenditure = presentOperatingExpenditure;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
