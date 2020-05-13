package springapp.datahubaccelerator.generators;

import springapp.datahubaccelerator.domain.Field;

import java.util.List;
import java.util.stream.Collectors;

public class ScriptGeneratorForCreatingEntitiesBC extends ScriptGenerator {

    private List<Field> allFields;
    private String targetExtract;
    private String userStoryNumber;
    private String primaryKeyColumnName;

    public ScriptGeneratorForCreatingEntitiesBC(List<Field> allFields) {
        this.allFields = allFields;
        this.targetExtract = allFields.get(0).getTargetExtract().toUpperCase();
        this.userStoryNumber = allFields.get(0).getReasonAdded();
        this.primaryKeyColumnName = allFields.get(0).getColumnName();
    }

    public String generateDDLScript() {
        return "/* User Story: " + userStoryNumber + "_Bde_" + generateEntityName(targetExtract) + " */\n\n" +
                generateSBCPart() +
                generateODSBasePart() +
                generateODSDeltaPart() +
                generateKBCPart() +
                generateTRFPart();
//                generateConstraintsPart() +
//                generateIndexesPart();
    }

    public String generateDMLScript() {
        return "/* User Story: " + userStoryNumber + "_Bde_" + generateEntityName(targetExtract) + " */\n" +
                "\n" +
                "/* MD_FK_REF Inserts */";
    }

    private String generateSBCPart() {
        return "/* SBC Changes */\n" +
                "IF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.TABLES\n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'Z_SBC_" + targetExtract + "'))\n" +
                "BEGIN\n" +
                "CREATE TABLE [Z_SBC_" + targetExtract + "] (\n" +
                "\t PART_NO                 [int] NOT NULL\n" +
                "\t,SOURCE_SYSTEM           [varchar](10) NOT NULL\n" +
                "\t,PUBLICID                [varchar](20) NOT NULL\n" +
                "\t,CASE_NO                 [varchar](255) NULL\n" +
                "\t,CREATETIME              [datetime2](7) NOT NULL\n" +
                "\t,UPDATETIME              [datetime2](7) NOT NULL\n" +
                "\t,ID                      [bigint] NOT NULL" +
                generateRowsForSBC(allFields
                    .stream()
                    .filter(x -> !x.getColumnName().contains("ROW_PROC_DTS"))
                    .filter(x -> !x.getColumnName().contains("SOURCE_SYSTEM"))
                    .collect(Collectors.toList()))+"\n" +
                ");\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][SBC] " + userStoryNumber + ": Bde_DelnqntProc'\n" +
                "\n" +
                "GO\n";
    }

    private String generateODSBasePart() {
        return "\n/* ODS Changes */" +
                "\n" +
                "IF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.TABLES \n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'Z_CS_" + targetExtract + "_BASE'))\n" +
                "BEGIN\n" +
                "\tCREATE TABLE [dbo].[Z_CS_" + targetExtract + "_BASE](\n" +
                "\t [" + primaryKeyColumnName.replace("KEY", "BID") + "]\tint IDENTITY(1,1) NOT NULL\n" +
                "\t,[" + primaryKeyColumnName + "]\tvarchar(100)\tNOT NULL\n" +
                "\t,[SOURCE_SYSTEM]\tvarchar(10)\tNOT NULL\n\t," +
                generateRowsForScript(allFields
                        .stream()
                        .filter(x -> !x.getScdType().equals("2"))
                        .filter(x -> !x.getColumnName().contains(primaryKeyColumnName))
                        .filter(x -> !x.getColumnName().contains("ROW_PROC_DTS"))
                        .filter(x -> !x.getColumnName().contains("X_SEQ_NO"))
                        .filter(x -> !x.getColumnName().contains("SOURCE_SYSTEM"))
                        .collect(Collectors.toList()), 0) +
                "\n\t,[ETL_LATE_ARRIVING_SCD]\tvarchar(1)\tNOT NULL\n" +
                "\t,[ETL_ACTIVE_FL]\tvarchar(1)\tNOT NULL\n" +
                "\t,[ETL_ADD_DTS]\tdatetime2(7)\tNULL\n" +
                "\t,[ETL_LAST_UPDATE_DTS]\tdatetime2(7)\tNOT NULL\n" +
                "CONSTRAINT [Z_" + generateShortcut(targetExtract, "BASE") + "_PK] PRIMARY KEY NONCLUSTERED \n" +
                "(\n" +
                "\t[" + primaryKeyColumnName + "] ASC\n" +
                "),\n" +
                "CONSTRAINT [Z_" + generateShortcut(targetExtract, "BASE") + "_AK1] UNIQUE NONCLUSTERED \n" +
                "(\n" +
                "\t[" + primaryKeyColumnName.replace("KEY", "BID") + "] ASC\n" +
                ")\n" +
                ")\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][ODS] " +
                userStoryNumber + ": Z_CS_" + targetExtract + "_BASE'\n" +
                "\nGO\n";
    }

    private String generateODSDeltaPart() {
        String primaryKeyColumnName = allFields.get(0).getColumnName().replace("(PK)", "").trim();
        return "\nIF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.TABLES \n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'Z_CS_" + targetExtract + "_DELTA'))\n" +
                "BEGIN\n" +
                "\tCREATE TABLE [dbo].[Z_CS_" + targetExtract + "_DELTA](\n" +
                "\t [" + primaryKeyColumnName.replace("KEY", "DID") + "]\tint IDENTITY(1,1) NOT NULL\n" +
                "\t,[" + primaryKeyColumnName + "]\tvarchar(100)\tNOT NULL\n" +
                "\t,[ETL_ROW_EFF_DTS]\tdatetime2(7)\tNOT NULL\n" +
                "\t,[ETL_ROW_EXP_DTS]\tdatetime2(7)\tNOT NULL\n" +
                "\t,[ROW_PROC_DTS]\tdatetime2(7)\tNOT NULL\n" +
                "\t,[SOURCE_SYSTEM]\tvarchar(10)\tNOT NULL" +
                generateRowsForScript(allFields.stream()
                        .filter(x -> !x.getScdType().equals("1"))
                        .filter(x -> !x.getColumnName().contains("X_SEQ_NO"))
                        .filter(x -> !x.getColumnName().contains("ROW_PROC_DTS"))
                        .filter(x -> !x.getColumnName().contains("SOURCE_SYSTEM"))
                        .collect(Collectors.toList()), 1) +
                "\n\t,[ETL_CURR_ROW_FL]\tvarchar(1)\tNOT NULL\n" +
                "\t,[ETL_LATE_ARRIVING_FL] varchar(1)\tNOT NULL\n" +
                "\t,[ETL_ACTIVE_FL]\tvarchar(1)\tNOT NULL\n" +
                "\t,[ETL_ADD_DTS]\tdatetime2(7)\tNULL\n" +
                "\t,[ETL_LAST_UPDATE_DTS]\tdatetime2(7)\tNOT NULL\n" +
                " CONSTRAINT [Z_" + generateShortcut(targetExtract, "DELTA") + "_PK] PRIMARY KEY NONCLUSTERED \n" +
                "(\n" +
                "\t[" + primaryKeyColumnName + "] ASC,\n" +
                "\t[ETL_ROW_EFF_DTS] ASC\n" +
                "),\n" +
                " CONSTRAINT [Z_" + generateShortcut(targetExtract, "DELTA") + "_AK1] UNIQUE NONCLUSTERED \n" +
                "(\n" +
                "\t[" + primaryKeyColumnName.replace("KEY", "DID") + "] ASC\n" +
                ")\n" +
                ")\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][ODS] " +
                userStoryNumber + ": Z_CS_" + targetExtract + "_DELTA'\n" +
                "\nGO\n";
    }

    private String generateKBCPart() {
        return "IF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.TABLES\n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'Z_KBC_" + targetExtract + "'))\n" +
                "BEGIN\n" +
                "\tCREATE TABLE [dbo].[Z_KBC_" + targetExtract + "](\n" +
                "\t\t[KEYTAB_ID] [bigint] IDENTITY(1,1) NOT NULL,\n" +
                "\t\t[BUS_KEY] [varchar](100) NOT NULL,\n" +
                "\t\t[SOURCE_SYSTEM] [varchar](10) NOT NULL,\n" +
                "\t\t[SRC_ID] [bigint] NOT NULL,\n" +
                "\t\t[ETL_ACTIVE_FL] [varchar](1) NOT NULL,\n" +
                "\t\t[ETL_ADD_DTS] [datetime2](7) NULL,\n" +
                "\t\t[ETL_LAST_UPDATE_DTS] [datetime2](7) NOT NULL,\n" +
                "\t CONSTRAINT [Z_PK_" + targetExtract + "] PRIMARY KEY NONCLUSTERED \n" +
                "\t(\n" +
                "\t\t[SRC_ID] ASC\n" +
                "\t));\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][KBC] " +
                userStoryNumber + ": Z_KBC_" + targetExtract + "'\n" +
                "\n" +
                "GO";
    }

    private String generateTRFPart() {
        return "/* TRF Changes */\n" +
                "IF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.TABLES\n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'Z_V_TRF_" + targetExtract + "'))\n" +
                "BEGIN\n" +
                "EXEC('CREATE VIEW [dbo].[Z_V_TRF_" + targetExtract + "]\n" +
                "\tAS\n" +
                "\tSELECT  " + BCgenerateRowsForTRF(allFields) +
                "        ,CAST(DP.ID AS VARCHAR(100)) AS SRC_ID\n" +
                "\t\t,UPPER(ACCT.SOURCE_SYSTEM + ''-'' + ACCT.BUS_KEY) AS BILL_ACCT_KEY\n" +
                "\t\t,CASE WHEN DP.POLICYPERIODID IS NULL THEN ''NOKEY''\n" +
                "            ELSE CASE WHEN PP.BUS_KEY IS NULL THEN CAST(DP.POLICYPERIODID AS VARCHAR(100)) ELSE UPPER(PP.SOURCE_SYSTEM + ''-'' + PP.BUS_KEY) END\n" +
                "            END AS BILL_POL_PRD_KEY\n" +
                "\t\t,UPPER(DEPLAN.SOURCE_SYSTEM + ''-'' + DEPLAN.BUS_KEY) AS DELNQNT_PLAN_KEY\n" +
                "\t\t,CASE WHEN DP.INVOICE_BDE IS NULL THEN ''NOKEY''\n" +
                "            ELSE CASE WHEN INV.BUS_KEY IS NULL THEN CAST(DP.INVOICE_BDE AS VARCHAR(100)) ELSE UPPER(INV.SOURCE_SYSTEM + ''-'' + INV.BUS_KEY) END\n" +
                "            END AS BILL_INV_KEY\n" +
                "\t\t,DP.SOURCE_SYSTEM AS SOURCE_SYSTEM\n" +
                "\t\t,DP.UpdateTime AS ROW_PROC_DTS\n" +
                "\t\t,DP.START_DTS AS START_DTS\n" +
                "\t\t,DP.EXIT_DTS AS EXIT_DTS\n" +
                "\t\t,COALESCE(DP.DELNQNT_PROC_SUBTYPE_CD, '' '') AS DELNQNT_PROC_SUBTYPE_CD\n" +
                "\t\t,COALESCE(DP.DELNQNT_PROC_STAT_CD, '' '') AS DELNQNT_PROC_STAT_CD\n" +
                "\t\t,COALESCE(DP.DELNQNT_AMT, 0) AS DELNQNT_AMT\n" +
                "\t\t,COALESCE(CURR_CD, '' '') AS CURR_CD\n" +
                "\t\t,COALESCE(REASON_CD, '' '') AS REASON_CD\n" +
                "\t\t,CONVERT(DATE, DP.INCEPTION_DTS) AS INCEPTION_DT\n" +
                "\t\t,DP.HELD_DTS AS HELD_DTS\n" +
                "\t\t,COALESCE(PHASE_CD, '' '') AS PHASE_CD\n" +
                "\t\t,COALESCE(DP.CASE_NO, '' '') AS CASE_NO\n" +
                "\t\t,COALESCE(DP.RA_BACHEM_STAT, '' '') AS RA_BACHEM_STAT\n" +
                "\t\t,COALESCE(SEPA_ERROR_REASON_CD, '' '') AS SEPA_ERROR_REASON_CD\n" +
                "\t\t,DP.ID AS X_SEQ_NO\n" +
                "\t\t,DP.UPDATETIME AS ETL_ROW_EFF_DTS\n" +
                "\t\t\n" +
                "\tFROM Z_SBC_DELNQNT_PROC DP\n" +
                "\t  LEFT JOIN KBC_ACCOUNT ACCT ON ACCT.SRC_ID = DP.ACCOUNTID\n" +
                "\t  LEFT JOIN KBC_POLICYPERIOD PP ON PP.SRC_ID = DP.POLICYPERIODID\n" +
                "\t  LEFT JOIN KBC_PLAN DEPLAN ON DEPLAN.SRC_ID = DP.DELINQUENCYPLANID\n" +
                "\t  LEFT JOIN KBC_INVOICE INV ON INV.SRC_ID = DP.INVOICE_BDE')\n" +
                "\t;\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][TRF] P17152-29242: Z_V_TRF_DELNQNT_PROC'\n" +
                "\n" +
                "GO";
    }

    private String generateRowsForSBC(List<Field> allFields) {
        String rowsForSBC = "";
        for (int i = 1; i < allFields.size() - 1; i++) {
                rowsForSBC = rowsForSBC + "\n\t,[" +
                        handleColumnNameForSBC(allFields.get(i)) + "]\t" +
                        handleDataType(allFields.get(i).getDatatype()) + "\t" +
                        "NULL";
        }
        return rowsForSBC;
    }

    private String handleColumnNameForSBC(Field field) {
        if (field.getColumnName().toUpperCase().contains("KEY")) {
            return field.getColumnName().replace("KEY", "ID");
        }
        if (field.getColumnMapping().toUpperCase().contains("TYPECODE")) {
            return field.getColumnName();
        }
        int positionOfFirstChar = field.getColumnMapping().indexOf(".") + 1;
        int positionOfLastChar = field.getColumnMapping().indexOf(" ", positionOfFirstChar);
        return field.getColumnMapping().substring(positionOfFirstChar, positionOfLastChar);
    }

    private String BCgenerateRowsForTRF(List<Field> allFields) {
        String rowsForTRF = "";
        String columnMapping;
        for (int i = 0; i < allFields.size() - 1; i++) {
            columnMapping = allFields.get(i).getColumnMapping().toUpperCase();
            if (columnMapping.startsWith("UPPER")){
                int locationOfLastCharOfKeyShortcatInTheMapping = columnMapping.indexOf(".");
                String keyShortcut = columnMapping.substring(0, locationOfLastCharOfKeyShortcatInTheMapping);
                int locationOfFirstCharOfKeyShortcatInTheMapping = keyShortcut.lastIndexOf(" ");
                keyShortcut = keyShortcut.substring(locationOfFirstCharOfKeyShortcatInTheMapping);
                if (i == 0){
                    rowsForTRF += columnMapping
                            .replace("@PV_SOURCESYSTEM", keyShortcut + ".SOURCE_SYSTEM")
                            .replace("\'", "\'\'") +
                            "\n\t,CAST(" + keyShortcut + ".ID AS VARCHAR(100)) AS SRC_ID\n\t";
                } else {
                    rowsForTRF += "," + columnMapping
                            .replace("@PV_SOURCESYSTEM", keyShortcut + ".SOURCE_SYSTEM")
                            .replace("\'", "\'\'")
                            .replace("PUBLICID", "BUS_KEY") + "\n\t";
                }
            }
        }
        return rowsForTRF;
    }
}
