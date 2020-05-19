package springapp.datahubaccelerator.generators;

import springapp.datahubaccelerator.domain.Field;

import java.util.List;
import java.util.stream.Collectors;

public class ScriptGeneratorForCreatingEntitiesBC extends ScriptGenerator {

    private List<Field> allFields;
    private String targetExtract;
    private String userStoryNumber;
    private String primaryKeyColumnName;
    private String primaryKeyShortcut;

    public ScriptGeneratorForCreatingEntitiesBC(List<Field> allFields) {
        this.allFields = allFields;
        this.targetExtract = allFields.get(0).getTargetExtract().toUpperCase();
        this.userStoryNumber = allFields.get(0).getReasonAdded();
        this.primaryKeyColumnName = allFields.get(0).getColumnName();
        this.primaryKeyShortcut  = getKeyShortcut(allFields.get(0).getColumnMapping());
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
                "\t [PART_NO]           [int] NOT NULL\n" +
                "\t,[SOURCE_SYSTEM]     [varchar](10) NOT NULL\n" +
                "\t,[PUBLICID]          [varchar](20) NOT NULL\n" +
                "\t,[CASE_NO]           [varchar](255) NULL\n" +
                "\t,[CREATETIME]        [datetime2](7) NOT NULL\n" +
                "\t,[UPDATETIME]        [datetime2](7) NOT NULL\n" +
                "\t,[ID]                [bigint] NOT NULL" +
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
                "\tSELECT  " + BCgenerateKeyRowsForTRF(allFields.stream()
                        .filter(x -> x.getColumnName().contains("KEY"))
                        .collect(Collectors.toList())) +
                "\t\t," + primaryKeyShortcut + ".SOURCE_SYSTEM AS SOURCE_SYSTEM\n" +
                "\t\t," + primaryKeyShortcut + ".UpdateTime AS ROW_PROC_DTS\n" +
                BCgenerateNoKeyRowsForTRF(allFields.stream()
                        .filter(x -> !x.getColumnName().contains("KEY"))
                        .filter(x -> !x.getColumnName().equalsIgnoreCase("SOURCE_SYSTEM"))
                        .filter(x -> !x.getColumnName().contains("ROW_PROC_DTS"))
                        .collect(Collectors.toList())) +
                "\t," + primaryKeyShortcut + ".ID AS X_SEQ_NO\n" +
                "\t," + primaryKeyShortcut +".UPDATETIME AS ETL_ROW_EFF_DTS\n" +
                "\t\t\n" +
                "\tFROM Z_SBC_" + targetExtract + " " + primaryKeyShortcut + "\n" +
                generateJoinsForTRF(Field.getFromJoinWhere()) +
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
                        handleGeneralRuleAppliedForSBC(allFields.get(i).getGeneralRuleApplied());
        }
        return rowsForSBC;
    }

    private String handleColumnNameForSBC(Field field) {
        if (field.getColumnName().toUpperCase().contains("KEY")) {
            return field.getSourceColumnName();
        }
        if (field.getColumnMapping().contains("TYPECODE")) {
            return field.getColumnName();
        }
        return field.getSourceColumnName();
    }

    private String handleGeneralRuleAppliedForSBC(String generalRuleApplied) {
        if (generalRuleApplied.equals("")){
            return "NULL";
        }
        return  "NOT NULL";
    }

    private String BCgenerateNoKeyRowsForTRF(List<Field> allFields) {
        String rowsForTRF = "";
        for (int i = 0; i < allFields.size() - 1; i++) {
            Field field = allFields.get(i);
            if (field.getGeneralRuleApplied().equalsIgnoreCase("General Rule 1")) {
                rowsForTRF += "\t,COALESCE(" + primaryKeyShortcut + "." + field.getSourceColumnName() + " '' '') " +
                        "AS " + field.getColumnName() + "\n";
            } else if (field.getGeneralRuleApplied().equalsIgnoreCase("General Number Rule 1")){
                rowsForTRF += "\t,COALESCE(" + primaryKeyShortcut + "." + field.getSourceColumnName() + " 0) " +
                        "AS " + field.getColumnName() + "\n";
            } else if (field.getColumnMapping().contains("CAST")) {
                rowsForTRF += "\t,CONVERT(" + field.getDatatype() + ", " + primaryKeyShortcut + "." +
                        field.getSourceColumnName() + ") AS " + field.getColumnName() + "\n";
            } else {
                rowsForTRF += field.getColumnMapping() + "\n";
            }
        }
        return rowsForTRF;
    }

    private String BCgenerateKeyRowsForTRF(List<Field> allFields) {
        String keyRowsForTRF = "";
        for (int i = 0; i < allFields.size(); i++) {
            Field field = allFields.get(i);
            String keyShortcut = getKeyShortcut(field.getColumnMapping());
            if (field.getColumnMapping().startsWith("UPPER") || field.getColumnMapping().startsWith(",UPPER")) {
                if (i == 0) {
                    keyRowsForTRF += field.getColumnMapping()
                            .replace("@PV_SOURCESYSTEM", keyShortcut + ".SOURCE_SYSTEM")
                            .replace("\'", "\'\'") +
                            "\n\t,CAST(" + keyShortcut + ".ID AS VARCHAR(100)) AS SRC_ID\n\t";
                } else {
                    keyRowsForTRF += "," + field.getColumnMapping()
                            .replace("@PV_SOURCESYSTEM", keyShortcut + ".SOURCE_SYSTEM")
                            .replace("\'", "\'\'")
                            .replace("PUBLICID", "BUS_KEY") + "\n\t";
                }
            } else if (field.getColumnMapping().startsWith("CASE") || field.getColumnMapping().startsWith(",CASE")) {
                keyRowsForTRF += "," + field.getColumnMapping()
                        .replace("\'", "\'\'")
                        .replace("PUBLICID", "BUS_KEY")
                        .replace("@PV_SOURCESYSTEM", keyShortcut + ".SOURCE_SYSTEM")
                        .replace("END AS", "\tEND AS") + "\n\t";
            }
        }
        return keyRowsForTRF;
    }

    private String getKeyShortcut(String columnMapping) {
        int locationOfLastCharOfKeyShortcatInTheMapping = columnMapping.indexOf(".");
        String keyShortcut = columnMapping.substring(0, locationOfLastCharOfKeyShortcatInTheMapping);
        int locationOfFirstCharOfKeyShortcatInTheMapping;
        if (keyShortcut.contains(" ")){
            locationOfFirstCharOfKeyShortcatInTheMapping = keyShortcut.lastIndexOf(" ");
        } else {
            locationOfFirstCharOfKeyShortcatInTheMapping = keyShortcut.lastIndexOf(",");
        }
        keyShortcut = keyShortcut.substring(locationOfFirstCharOfKeyShortcatInTheMapping);
        return keyShortcut;
    }

    private String generateJoinsForTRF(String fromJoinWhere){
        String joinsForTRF = "";
        String singleJoin;
        int index;
        index = fromJoinWhere.indexOf("FROM");
        index = fromJoinWhere.indexOf("\n", index);
        fromJoinWhere = fromJoinWhere.substring(index + 1);
        while (!fromJoinWhere.startsWith("\n")){
                index = fromJoinWhere.indexOf("\n");
                singleJoin = fromJoinWhere.substring(0, index);
                joinsForTRF += singleJoin.replace("BC_", "KBC_")
                        .replace("ID", "SRC_ID") + "\n";
                fromJoinWhere = fromJoinWhere.substring(index + 1);
        }
        return joinsForTRF;
    }

}
