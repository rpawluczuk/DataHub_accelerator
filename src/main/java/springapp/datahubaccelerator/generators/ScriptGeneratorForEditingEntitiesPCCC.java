package springapp.datahubaccelerator.generators;

import springapp.datahubaccelerator.domain.Field;

import java.util.List;
import java.util.stream.Collectors;

public class ScriptGeneratorForEditingEntitiesPCCC extends ScriptGenerator {

    private List<Field> newFields;

    public ScriptGeneratorForEditingEntitiesPCCC(List<Field> allFields) {
        String newestUserStoryNumber = getNewestUserStoryNumber(allFields);
        this.newFields = allFields.stream()
                .filter(f -> f.getReasonAdded().contains(newestUserStoryNumber))
                .collect(Collectors.toList());
    }

    private String getNewestUserStoryNumber(List<Field> allFields) {
        return allFields.stream()
                    .filter(f -> !f.getReasonAdded().toUpperCase().equals("BASE"))
                    .map(f -> f.getReasonAdded().replace("P17152-", ""))
                    .map(s -> Integer.valueOf(s))
                    .max(Integer::compare).toString();
    }

    public String generateDDLScript() {
        return generateTRFPart();
    }

    private String generateTRFPart() {
        return "";
    }
}
