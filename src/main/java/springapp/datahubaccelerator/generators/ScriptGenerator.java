package springapp.datahubaccelerator.generators;

import springapp.datahubaccelerator.domain.Field;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ScriptGenerator {

    public ScriptGenerator() {
    }

    String generateEntityName(String targetExtract) {
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
        if (sourceTable.contains("CC_")){
            return "CS_" + sourceTable.toUpperCase().replace("CC_", "CLAIM_") + "_BASE";
        } else if (sourceTable.contains("PC_")){
            return "CS_" + sourceTable.toUpperCase().replace("PC_", "") + "_BASE";
        } else {
            String foreignEntityName = sourceTable.toUpperCase()
                    .replace("CCX_", "")
                    .replace("_EXT", "")
                    .trim();
            return "Z_CS_" + foreignEntityName + "_BASE";
        }
    }

    public static String generatePrimaryKeyOfJoinedTableName(String sourceTable) {
        String foreignEntityName = sourceTable.toUpperCase()
                .replace("CC_", "CLAIM_")
                .replace("CCX_", "")
                .replace("_EXT", "")
                .replace("PC_", "").trim();
        return foreignEntityName + "_KEY";
    }

    String generateRowsForScript(List<Field> allFields, int id) {
        String rowsForScript = "";
        for (int i = id; i < allFields.size(); i++) {
            if (i == 0) {
                rowsForScript = rowsForScript + "[" + allFields.get(i).getColumnName() + "]\t" +
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

    String generateShortcut(String targetExtract, String scdType) {
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

    private String handleDataType(String dataType) {
        if (dataType.equalsIgnoreCase("datetime")) {
            return "datetime2(7)";
        }
        if (dataType.equalsIgnoreCase("decimal(19,0)")) {
            return "bigint";
        }
        return dataType;
    }
}
