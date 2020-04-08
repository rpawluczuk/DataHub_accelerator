package springapp.datahubaccelerator;

public enum DontAllowedSheets {

    SHEET_ONE("Sheet1"),
    DOMAINS("Domains"),
    TABLE_OF_CONTENTS("Table Of Contents"),
    INSTRUCTIONS("Instructions"),
    VERSIONS("Versions"),
    BACKWARD_COMPABILITY("Backward Compatibility"),
    ISSUES_LIST("Issues List"),
    PROGRESS_REPORT("Progress Report"),
    TEMPLATE("Template"),
    TEMPLATE_BDE("Template_Bde"),
    SAMPLE("Sample"),
    BUSINESS_RULES("Business Rules"),
    CDC("CDC"),
    CONFORMED("Conformed"),
    REFERENCE("Reference"),
    AUDIT("audit"),
    COUNT("count")
    ;

    private String nameOfSheet;

    DontAllowedSheets(String nameOfSheet) {
        this.nameOfSheet = nameOfSheet;
    }

    public String getNameOfSheet() {
        return nameOfSheet;
    }

    public void setNameOfSheet(String nameOfSheet) {
        this.nameOfSheet = nameOfSheet;
    }
}
