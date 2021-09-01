package in.apssdc.engineering.itirequirements.model;

/**
 * Created by Sandhya on 09-11-2018.
 */

public class CourseDetailsModel {

    private String noonIndustryPartners,placedStudentLocations,remedialStepsforPlacement,noofLoans,enterpreneurshipPromotingApproach,dedicatedPlacementOfficer,timeStamp;

    public CourseDetailsModel(String noonIndustryPartners, String placedStudentLocations, String remedialStepsforPlacement, String noofLoans, String enterpreneurshipPromotingApproach, String dedicatedPlacementOfficer, String timeStamp) {
        this.noonIndustryPartners = noonIndustryPartners;
        this.placedStudentLocations = placedStudentLocations;
        this.remedialStepsforPlacement = remedialStepsforPlacement;
        this.noofLoans = noofLoans;
        this.enterpreneurshipPromotingApproach = enterpreneurshipPromotingApproach;
        this.dedicatedPlacementOfficer = dedicatedPlacementOfficer;
        this.timeStamp = timeStamp;
    }

    public String getNoonIndustryPartners() {
        return noonIndustryPartners;
    }

    public void setNoonIndustryPartners(String noonIndustryPartners) {
        this.noonIndustryPartners = noonIndustryPartners;
    }

    public String getPlacedStudentLocations() {
        return placedStudentLocations;
    }

    public void setPlacedStudentLocations(String placedStudentLocations) {
        this.placedStudentLocations = placedStudentLocations;
    }

    public String getRemedialStepsforPlacement() {
        return remedialStepsforPlacement;
    }

    public void setRemedialStepsforPlacement(String remedialStepsforPlacement) {
        this.remedialStepsforPlacement = remedialStepsforPlacement;
    }

    public String getNoofLoans() {
        return noofLoans;
    }

    public void setNoofLoans(String noofLoans) {
        this.noofLoans = noofLoans;
    }

    public String getEnterpreneurshipPromotingApproach() {
        return enterpreneurshipPromotingApproach;
    }

    public void setEnterpreneurshipPromotingApproach(String enterpreneurshipPromotingApproach) {
        this.enterpreneurshipPromotingApproach = enterpreneurshipPromotingApproach;
    }

    public String getDedicatedPlacementOfficer() {
        return dedicatedPlacementOfficer;
    }

    public void setDedicatedPlacementOfficer(String dedicatedPlacementOfficer) {
        this.dedicatedPlacementOfficer = dedicatedPlacementOfficer;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
