package springapp.datahubaccelerator.generators;

import springapp.datahubaccelerator.domain.Field;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ScriptGenerator {

    public ScriptGenerator() {
    }

    public String generateTRFPart(List<Field> allFields) {
        String targetExtract = allFields.get(0).getTargetExtract();
        return  "/* User Story: P17152-" + Field.getUserStoryNumber() + "_Bde_" + Field.getEntityName() + " */\n" +
                "\n" +
                "/* TRF Changes */\n" +
                "IF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.TABLES \n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'Z_TRF_" + targetExtract +"'))\n" +
                "BEGIN\n" +
                "CREATE TABLE [dbo].[Z_TRF_" + targetExtract + "](\n" +
                "\t" + generateRowsForScript(allFields, 0) +
                "[ETL_ROW_EFF_DTS]      [datetime2](7)\tNOT NULL\n" +
                ")\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][TRF] P17152-" +
                Field.getUserStoryNumber() + ": Z_TRF_" + targetExtract + "'";
    }

    public String generateODSBasePart(List<Field> allFields) {
        String targetExtract = allFields.get(0).getTargetExtract();
        String primaryKeyColumnName = allFields.get(0).getColumnName().replace("(PK)", "").trim();
        return "/* ODS Changes */\n" +
                "\n" +
                "IF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.TABLES \n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'Z_CS_" + targetExtract + "_BASE'))\n" +
                "BEGIN\n" +
                "\tCREATE TABLE [dbo].[Z_CS_" + targetExtract + "_BASE](\n" +
                "\t [" + primaryKeyColumnName.replace("KEY", "BID") + "]\t\t\t[int] IDENTITY(1,1) NOT NULL\n" +
                "\t," + generateRowsForScript(allFields
                .stream()
                .filter(x -> !x.getScdType().equals("2"))
                .collect(Collectors.toList()), 0) +
                "[ETL_LATE_ARRIVING_SCD]\t[varchar](1)\tNOT NULL\n" +
                "\t,[ETL_ACTIVE_FL]\t\t\t[varchar](1)\tNOT NULL\n" +
                "\t,[ETL_ADD_DTS]\t\t\t[datetime2](7)\tNULL\n" +
                "\t,[ETL_LAST_UPDATE_DTS]\t\t[datetime2](7)\tNOT NULL\n" +
                "CONSTRAINT [Z_C" + generateShortcut(targetExtract, "BASE") + "_PK] PRIMARY KEY NONCLUSTERED \n" +
                "(\n" +
                "\t[" + primaryKeyColumnName + "] ASC\n" +
                "),\n" +
                "CONSTRAINT [Z_C" + generateShortcut(targetExtract, "BASE") + "_AK1] UNIQUE NONCLUSTERED \n" +
                "(\n" +
                "\t[" + primaryKeyColumnName.replace("KEY", "BID") + "] ASC\n" +
                ")\n" +
                ")\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][ODS] P17152-" +
                Field.getUserStoryNumber()  + ": Z_CS_" + targetExtract + "_BASE'\n" +
                "\n";
    }

    public String generateODSDeltaPart(List<Field> allFields) {
        String targetExtract = allFields.get(0).getTargetExtract();
        String primaryKeyColumnName = allFields.get(0).getColumnName().replace("(PK)", "").trim();
        return "IF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.TABLES \n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'Z_CS_" + targetExtract + "_DELTA'))\n" +
                "BEGIN\n" +
                "\tCREATE TABLE [dbo].[Z_CS_" + targetExtract + "_DELTA](\n" +
                "\t [" + primaryKeyColumnName.replace("KEY", "DID") + "]\t\t[int] IDENTITY(1,1) NOT NULL\n" +
                "\t,[" + primaryKeyColumnName + "]\t\t[varchar](100)\t\tNOT NULL\n" +
                "\t,[ETL_ROW_EFF_DTS]\t\t[datetime2](7)\t\tNOT NULL\n" +
                "\t,[ETL_ROW_EXP_DTS]\t\t[datetime2](7)\t\tNOT NULL\n" +
                "\t," + generateRowsForScript(allFields
                .stream()
                .filter(x -> !x.getScdType().equals("1"))
                .collect(Collectors.toList()), 1) +
                "[ETL_CURR_ROW_FL]\t\t[varchar](1)\t\tNOT NULL\n" +
                "\t,[ETL_LATE_ARRIVING_FL] [varchar](1)\t\tNOT NULL\n" +
                "\t,[ETL_ACTIVE_FL]\t\t[varchar](1)\t\tNOT NULL\n" +
                "\t,[ETL_ADD_DTS]\t\t\t[datetime2](7)\t\tNULL\n" +
                "\t,[ETL_LAST_UPDATE_DTS]\t[datetime2](7)\t\tNOT NULL\n" +
                " CONSTRAINT [Z_C" + generateShortcut(targetExtract, "DELTA") + "_PK] PRIMARY KEY NONCLUSTERED \n" +
                "(\n" +
                "\t[" + primaryKeyColumnName + "] ASC,\n" +
                "\t[ETL_ROW_EFF_DTS] ASC\n" +
                "),\n" +
                " CONSTRAINT [Z_C" + generateShortcut(targetExtract, "DELTA") + "_AK1] UNIQUE NONCLUSTERED \n" +
                "(\n" +
                "\t[" + primaryKeyColumnName.replace("KEY", "DID") + "] ASC\n" +
                ")\n" +
                ")\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][ODS] P17152-" +
                primaryKeyColumnName.replace("KEY", "DID") + ": Z_CS_" + targetExtract + "_DELTA'\n";
    }

    public String generateConstraintsPart(List<Field> allFields) {
        for (Field field : allFields) {
            field.setColumnName(field.getColumnName().replace("(PK)", "")
                    .replace("(FK)", "").trim());
        }
        List<Field> allKeyFields = allFields.stream()
                .filter(f -> f.getColumnName().endsWith("KEY"))
                .collect(Collectors.toList());
        String constraintPartScript ="";
        for (Field field : allKeyFields) {
            constraintPartScript = constraintPartScript + generateSingleConstraint(field);
        }
        return "\n/* Constraints */\n" + constraintPartScript;
    }

    public String generateIndexesPart(List<Field> allFields) {
        for (Field field : allFields) {
            field.setColumnName(field.getColumnName().replace("(PK)", "")
                    .replace("(FK)", "").trim());
        }
        List<Field> allKeyFields = allFields.stream()
                .filter(f -> f.getColumnName().endsWith("KEY"))
                .collect(Collectors.toList());
        String indexPartScript ="";
        for (Field field : allKeyFields) {
            indexPartScript = indexPartScript + generateSingleIndex(field);
        }
        return "\n/* Indexes */\n" + indexPartScript;
    }

    private String generateRowsForScript(List<Field> allFields, int id) {
        String rowsForScript = "";
        for (int i = id; i < allFields.size(); i++) {
            rowsForScript = rowsForScript + "[" + allFields.get(i).getColumnName().replace("(PK)","")
                    .replace("(FK)", "").trim() + "]\t[" + handleDatatype(allFields.get(i).getDatatype()) + "\t" +
                    handleGeneralRuleApplied(allFields.get(i).getGeneralRuleApplied()) + "\n\t,";
        }
        return rowsForScript;
    }

    private String handleDatatype(String datatype) {
        if (datatype.contains("varchar") || datatype.contains("decimal")){
            return datatype.replace("(", "](");
        }
        return datatype +"]";
    }

    private String handleGeneralRuleApplied(String generalRuleApplied) {
        List<String> rulesThatAllowNull = Arrays.asList("General Rule 5", "General Date Rule 5"
                ,"General Number Rule 4");
        for (String ruleThatAllowNull : rulesThatAllowNull) {
            if (ruleThatAllowNull.equalsIgnoreCase(generalRuleApplied.trim())){
                return "NULL";
            }
        }
        return "NOT NULL";
    }

    private String generateShortcut(String targetExtract, String scdType) {
        if (targetExtract.toUpperCase().startsWith("BDE")){
            targetExtract = targetExtract.substring(3);
        } else if (targetExtract.toUpperCase().endsWith("BDE")){
            targetExtract = targetExtract.substring(0, targetExtract.length() - 3);
        }
        if (targetExtract.toUpperCase().startsWith("_")){
            targetExtract = targetExtract.substring(1);
        } else if (targetExtract.toUpperCase().endsWith("_")){
            targetExtract = targetExtract.substring(0, targetExtract.length() - 1);
        }
        if (targetExtract.contains("_")){
            return Arrays.asList(targetExtract.split("_")).stream()
                    .map(x -> x.charAt(0) + "")
                    .collect(Collectors.joining()) + scdType.charAt(0);
        } else if (targetExtract.matches("\\w*[a-z]\\w*") && targetExtract.matches("\\w*[A-Z]\\w*")){
            return targetExtract.chars()
                    .mapToObj(x -> (char) x)
                    .filter(ch -> Character.isUpperCase(ch))
                    .map(x -> x + "")
                    .collect(Collectors.joining()) + scdType.charAt(0);
        } else {
            return targetExtract.toUpperCase().substring(0, 2) + scdType.charAt(0);
        }
    }

    private String generateSingleConstraint(Field field) {
        String targetExtract = field.getTargetExtract();
        String scdType;
        if (field.getScdType().equals("1")){
            scdType = "BASE";
        } else {
            scdType = "DELTA";
        }
        String foreginField = field.getColumnName().replace("_KEY", "");
        return "\nIF (EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.TABLES \n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'Z_CS_" + targetExtract + "_" + scdType + "')\n" +
                "\tAND EXISTS (SELECT * \n" +
                "\t\t\t\tFROM INFORMATION_SCHEMA.TABLES \n" +
                "\t\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\t\tAND  TABLE_NAME = 'Z_CS_" + foreginField + "_BASE')\n" +
                "\tAND NOT EXISTS (SELECT * \n" +
                "\t\t\t\t\tFROM sys.foreign_keys\n" +
                "\t\t\t\t\tWHERE name = 'Z_FK_C" + generateShortcut(targetExtract, scdType) +
                "_C" + generateShortcut(foreginField, "BASE") + "')\n" +
                "\t\t\t\t)\n" +
                "BEGIN\n" +
                "\n" +
                "\tALTER TABLE [dbo].[Z_CS_" + targetExtract + "_" + scdType + "] ADD CONSTRAINT " +
                "[Z_FK_C" + generateShortcut(targetExtract, scdType) + "_C" +generateShortcut(foreginField, "BASE") +
                "] FOREIGN KEY([" + field.getColumnName() + "])\n" +
                "\tREFERENCES [dbo].[Z_CS_" + foreginField + "_BASE] ([" + field.getColumnName() + ")\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][FK] P17152-" +
                Field.getUserStoryNumber() + ": Z_FK_C" +
                generateShortcut(targetExtract, scdType) + "_C" + generateShortcut(foreginField, "BASE") + "'\n";
    }


    private String generateSingleIndex(Field field) {
        String targetExtract = field.getTargetExtract();
        String scdType;
        if (field.getScdType().equals("1")){
            scdType = "BASE";
        } else {
            scdType = "DELTA";
        }
        String foreginField = field.getColumnName().replace("_KEY", "");
        return "\nIF (EXISTS (SELECT *\n" +
                "\t\t\tFROM INFORMATION_SCHEMA.COLUMNS\n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo'\n" +
                "\t\t\tAND TABLE_NAME = 'Z_CS_" + targetExtract + "_" + scdType + "'\n" +
                "\t\t\tAND COLUMN_NAME = '" + field.getColumnName() + "')\n" +
                "\tAND NOT EXISTS (SELECT *\n" +
                "\t\t\t\t\tFROM sys.indexes\n" +
                "\t\t\t\t\tWHERE name = 'Z_FK_C"  +
                generateShortcut(targetExtract, scdType) + "_C" + generateShortcut(foreginField, "BASE") + "_XFK')\n" +
                "\t)\n" +
                "BEGIN\n" +
                "\tcreate nonclustered index Z_FK_C" + generateShortcut(targetExtract, scdType) + "_C" +
                generateShortcut(foreginField, "BASE") + "_XFK on Z_CS_" + targetExtract + "_" + scdType +
                " (" + field.getColumnName() + " ASC)\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][index] P17152-" +
                Field.getUserStoryNumber() +": Z_FK_C" + generateShortcut(targetExtract, scdType) + "_C" +
                generateShortcut(foreginField, "BASE") + "_XFK'\n";
    }
}
