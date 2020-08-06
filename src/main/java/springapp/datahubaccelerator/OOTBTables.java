package springapp.datahubaccelerator;

public enum OOTBTables {

    PCX_PFLINECOV_EXT("PCX_PFLINECOV_EXT", "CS_COVG_BASE", "COVG_KEY"),
    PC_POLICYPERIOD("PC_POLICYPERIOD", "CS_POLICY_BASE", "POL_KEY")
    ;

    private String ootbSourceTable;
    private String ootbTable;
    private String ootbPrimaryKey;


    OOTBTables(String ootbSourceTable, String ootbTable, String ootbPrimaryKey) {
        this.ootbSourceTable = ootbSourceTable;
        this.ootbTable = ootbTable;
        this.ootbPrimaryKey = ootbPrimaryKey;
    }

    public String getOotbTable() {
        return ootbTable;
    }

    public void setOotbTable(String ootbTable) {
        this.ootbTable = ootbTable;
    }

    public String getOotbSourceTable() {
        return ootbSourceTable;
    }

    public void setOotbSourceTable(String ootbSourceTable) {
        this.ootbSourceTable = ootbSourceTable;
    }

    public String getOotbPrimaryKey() {
        return ootbPrimaryKey;
    }

    public void setOotbPrimaryKey(String ootbPrimaryKey) {
        this.ootbPrimaryKey = ootbPrimaryKey;
    }
}
