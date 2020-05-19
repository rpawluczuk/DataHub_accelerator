package springapp.datahubaccelerator.generators;

import springapp.datahubaccelerator.domain.Field;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ScriptGenerator {

    public ScriptGenerator() {
    }

    String generateEntityName(String targetExtract) {
        if (!targetExtract.contains("_")) return targetExtract;
        String entityName = "";
        List<String> splitedTargetExtract = Arrays.asList(targetExtract.toLowerCase().split("_"));
        for (String split : splitedTargetExtract) {
            entityName = entityName.concat(split.substring(0, 1).toUpperCase() + split.substring(1));
        }
        return entityName;
    }

    public static Integer getNewestUserStoryNumber(List<Field> allFields) {
        return allFields.stream()
                .filter(f -> !f.getReasonAdded().toUpperCase().equals("BASE"))
                .map(f -> f.getReasonAdded().replace("P17152-", ""))
                .map(s -> Integer.valueOf(s))
                .max(Integer::compare).get();
    }

    public static List<Field> getKeyFieldsList(List<Field> allFields) {
        for (Field field : allFields) {
            field.setColumnName(field.getColumnName().replace("(PK)", "")
                    .replace("(FK)", "").trim());
        }
        return allFields.stream()
                .filter(f -> f.getColumnName().endsWith("KEY"))
                .collect(Collectors.toList());
    }

    public static String generateJoinedTableName(String sourceTable) {
        if (sourceTable.toUpperCase().contains("CC_")) {
            return "CS_" + sourceTable.toUpperCase().replace("CC_", "CLAIM_") + "_BASE";
        } else if (sourceTable.toUpperCase().contains("PC_")) {
            return "CS_" + sourceTable.toUpperCase().replace("PC_", "") + "_BASE";
        } else {
            String foreignEntityName = sourceTable.toUpperCase()
                    .replace("CCX_", "")
                    .replace("PCX_", "")
                    .replace("_EXT", "")
                    .trim();
            return "Z_CS_" + foreignEntityName + "_BASE";
        }
    }

    public static String generatePrimaryKeyOfJoinedTableName(String sourceTable) {
        String foreignEntityName = sourceTable.toUpperCase()
                .replace("CC_", "CLAIM_")
                .replace("CCX_", "")
                .replace("PCX_", "")
                .replace("_EXT", "")
                .replace("PC_", "").trim();
        return foreignEntityName + "_KEY";
    }

    String generateRowsForScript(List<Field> allFields, int id) {
        String rowsForScript = "";
        for (int i = id; i < allFields.size(); i++) {
            if (i == 0) {
                rowsForScript = rowsForScript + "\n\t [" + allFields.get(i).getColumnName() + "]\t" +
                        handleDataType(allFields.get(i).getDatatype()) + "\t" +
                        handleGeneralRuleApplied(allFields.get(i).getGeneralRuleApplied());
            } else {
                rowsForScript = rowsForScript + "\n\t,[" + allFields.get(i).getColumnName() + "]\t" +
                        handleDataType(allFields.get(i).getDatatype()) + "\t" +
                        handleGeneralRuleApplied(allFields.get(i).getGeneralRuleApplied());
            }
        }
        return rowsForScript;
    }

    String listingOfColumnNames(List<Field> newFields, int id) {
        String listingForScript = "";
        for (int i = id; i < newFields.size(); i++) {
            if (i == 0) {
                listingForScript = listingForScript + "\n\t\t\t\t\t'" + newFields.get(i).getColumnName().replace("(PK)", "")
                        .replace("(FK)", "").trim() + "'";
            } else {
                listingForScript = listingForScript + "\n\t\t\t\t\t,'" + newFields.get(i).getColumnName().replace("(PK)", "")
                        .replace("(FK)", "").trim() + "'";
            }
        }
        return listingForScript;
    }

    public String generateShortcut(String targetExtract, String scdType) {
        if (targetExtract.equals("ETL_LAST_UPDATE_DTS")) {
            return "XUPDT";
        }
        if (targetExtract.toUpperCase().startsWith("BDE")) {
            targetExtract = targetExtract.substring(3);
        } else if (targetExtract.toUpperCase().endsWith("BDE")) {
            targetExtract = targetExtract.substring(0, targetExtract.length() - 3);
        }
        if (targetExtract.toUpperCase().startsWith("_")) {
            targetExtract = targetExtract.substring(1);
        } else if (targetExtract.toUpperCase().endsWith("_")) {
            targetExtract = targetExtract.substring(0, targetExtract.length() - 1);
        }
        if (targetExtract.contains("_")) {
            return "C" + Arrays.asList(targetExtract.split("_")).stream()
                    .map(x -> x.charAt(0) + "")
                    .collect(Collectors.joining()) + scdType.charAt(0);
        } else if (targetExtract.matches("\\w*[a-z]\\w*") && targetExtract.matches("\\w*[A-Z]\\w*")) {
            return "C" + targetExtract.chars()
                    .mapToObj(x -> (char) x)
                    .filter(ch -> Character.isUpperCase(ch))
                    .map(x -> x + "")
                    .collect(Collectors.joining()) + scdType.charAt(0);
        } else {
            return "C" + targetExtract.toUpperCase().substring(0, 2) + scdType.charAt(0);
        }
    }

    private String handleGeneralRuleApplied(String generalRuleApplied) {
        List<String> rulesThatAllowNull = Arrays.asList("General Rule 5", "General Date Rule 5"
                , "General Number Rule 4");
        for (String ruleThatAllowNull : rulesThatAllowNull) {
            if (ruleThatAllowNull.equalsIgnoreCase(generalRuleApplied.trim())) {
                return "NULL";
            }
        }
        return "NOT NULL";
    }

    public String handleDataType(String dataType) {
        if (dataType.equalsIgnoreCase("datetime")) {
            return "datetime2(7)";
        }
        if (dataType.equalsIgnoreCase("decimal(19,0)")) {
            return "bigint";
        }
        return dataType;
    }

    public static void extractSourceColumnName(Field field) {
        int positionOfFirstChar;
        int positionOfLastChar;
        if (field.getColumnName().toUpperCase().contains("KEY")) {
            String fromJoinWhere = Field.getFromJoinWhere();
            positionOfFirstChar = fromJoinWhere.indexOf(field.getSourceTable());
            fromJoinWhere = fromJoinWhere.substring(positionOfFirstChar);
            positionOfFirstChar = fromJoinWhere.indexOf("=");
            positionOfFirstChar = fromJoinWhere.indexOf(".", positionOfFirstChar) + 1;
            positionOfLastChar = fromJoinWhere.indexOf("\n", positionOfFirstChar);
            field.setSourceColumnName(fromJoinWhere.substring(positionOfFirstChar, positionOfLastChar));
        } else if (field.getColumnMapping().contains("TYPECODE")) {
            field.setSourceColumnName(field.getColumnName());
        } else if (field.getColumnMapping().contains(" AS ")){
            positionOfFirstChar = field.getColumnMapping().indexOf(".") + 1;
            positionOfLastChar = field.getColumnMapping().indexOf(" ", positionOfFirstChar);
            field.setSourceColumnName(field.getColumnMapping().substring(positionOfFirstChar, positionOfLastChar));
        } else {
            field.setSourceColumnName(field.getColumnName());
        }
    }

    public String generateSingleConstraint(Field field, String primaryTableName) {
        String scdType;
        if (field.getScdType().equals("1")) {
            scdType = "BASE";
        } else {
            scdType = "DELTA";
        }
        String pureTableName = primaryTableName
                .replace("Z_CS_", "")
                .replace("CS_", "")
                .replace("_BASE", "");
        String pureJoinedTableName = field.getJoinedTable()
                .replace("Z_CS_", "")
                .replace("CS_", "")
                .replace("_BASE", "")
                .replace("_DELTA", "");
        return "\nIF (EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.TABLES \n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = '" + primaryTableName + "')\n" +
                "\tAND EXISTS (SELECT * \n" +
                "\t\t\t\tFROM INFORMATION_SCHEMA.TABLES \n" +
                "\t\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\t\tAND  TABLE_NAME = '" + field.getJoinedTable() + "')\n" +
                "\tAND NOT EXISTS (SELECT * \n" +
                "\t\t\t\t\tFROM sys.foreign_keys\n" +
                "\t\t\t\t\tWHERE name = 'Z_FK_" + generateShortcut(pureTableName, scdType) +
                "_" + generateShortcut(pureJoinedTableName, "BASE") + "')\n" +
                "\t\t\t\t)\n" +
                "BEGIN\n" +
                "\n" +
                "\tALTER TABLE [dbo].[" + primaryTableName + "] ADD CONSTRAINT " +
                "[Z_FK_" + generateShortcut(pureTableName, scdType) + "_" + generateShortcut(pureJoinedTableName, "BASE") +
                "] FOREIGN KEY([" + field.getColumnName() + "])\n" +
                "\tREFERENCES [dbo].[" + field.getJoinedTable() + "] ([" + field.getPrimaryKeyOfJoinedTable() + "])\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][FK] " +
                field.getReasonAdded() + ": Z_FK_" +
                generateShortcut(pureTableName, scdType) + "_" + generateShortcut(pureJoinedTableName, "BASE") + "'\n";
    }

    String generateSingleIndex(String scdType, String columnName, Field field, String primaryTableName) {
        if (scdType.equals("1")) {
            scdType = "BASE";
        } else {
            scdType = "DELTA";
        }
        String pureTableName = primaryTableName
                .replace("Z_CS_", "")
                .replace("CS_", "")
                .replace("_BASE", "");
        String pureJoinedTableName = field.getJoinedTable()
                .replace("Z_CS_", "")
                .replace("CS_", "")
                .replace("_BASE", "")
                .replace("_DELTA", "");
        String index;
        if (columnName.equals("ETL_LAST_UPDATE_DTS")) {
            index = "Z_" + generateShortcut(pureTableName, scdType) + "_XUPDT";
        } else {
            index = "Z_FK_" + generateShortcut(pureTableName, scdType) + "_" +
                    generateShortcut(pureJoinedTableName, "BASE") + "_XFK";
        }
        return "\nIF (EXISTS (SELECT *\n" +
                "\t\t\tFROM INFORMATION_SCHEMA.COLUMNS\n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo'\n" +
                "\t\t\tAND TABLE_NAME = '" + primaryTableName + "'\n" +
                "\t\t\tAND COLUMN_NAME = '" + columnName + "')\n" +
                "\tAND NOT EXISTS (SELECT *\n" +
                "\t\t\t\t\tFROM sys.indexes\n" +
                "\t\t\t\t\tWHERE name = '" + index + "')\n" +
                "\t)\n" +
                "BEGIN\n" +
                "\tcreate nonclustered index " + index + " on " + primaryTableName +
                " (" + columnName + " ASC)\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][index] " +
                field.getReasonAdded() + ": " + index + "'\n";
    }

    String generateSingleInsertion(Field field, String primaryTableName) {
        String scdType;
        if (field.getScdType().equals("1")) {
            scdType = "BASE";
        } else {
            scdType = "DELTA";
        }
        String pureTableName = primaryTableName
                .replace("Z_CS_", "")
                .replace("CS_", "")
                .replace("_BASE", "");
        String pureJoinedTableName = field.getJoinedTable()
                .replace("Z_CS_", "")
                .replace("CS_", "")
                .replace("_BASE", "")
                .replace("_DELTA", "");
        return "\n/* Insert MD_FK_REF record: Z_FK_" + generateShortcut(pureTableName, scdType) + "_" +
                generateShortcut(pureJoinedTableName, "BASE") + " */\n" +
                "IF (EXISTS (SELECT * \n" +
                "\t\t\tFROM sys.foreign_keys\n" +
                "\t\t\tWHERE name = 'Z_FK_" + generateShortcut(pureTableName, scdType) + "_" +
                generateShortcut(pureJoinedTableName, "BASE") + "')\n" +
                "\tAND NOT EXISTS (SELECT *\n" +
                "\t\t\t\t\tFROM MD_FK_REF\n" +
                "\t\t\t\t\tWHERE FK_CONSTR_NAME = 'Z_FK_" + generateShortcut(pureTableName, scdType) + "_" +
                generateShortcut(pureJoinedTableName, "BASE") + "')\n" +
                ")\n" +
                "BEGIN\n" +
                "\tINSERT INTO MD_FK_REF(TRF_NAME,FK_TAB_NAME,FK_COL_NAME,PK_TAB_NAME,PK_COL_NAME,LOB_CD,FK_CONSTR_NAME," +
                "OOTB_FUTURE_USE_FL,MULTI_SRCE_FL,SELF_REF_FL) \nVALUES " +
                "('Z_TRF_" + pureTableName + "','" + primaryTableName + "','" + field.getColumnName() +
                "','" + field.getJoinedTable() + "','" + field.getColumnName() + "','All','" +
                "Z_FK_" + generateShortcut(pureTableName, scdType) + "_" +
                generateShortcut(pureJoinedTableName, "BASE") + "','N','N','N');\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][MD_FK_REF] " + field.getReasonAdded() +
                ": Z_FK_" + generateShortcut(pureTableName, scdType) + "_" +
                generateShortcut(pureJoinedTableName, "BASE") + "'\n\nGO\n";
    }
}
