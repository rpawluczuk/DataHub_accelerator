package springapp.datahubaccelerator.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class Field {

    static final private int COLUMNS_LENGTH = 5000;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    static String userStoryNumber;
    static String entityName;

    @Column(length = COLUMNS_LENGTH)
    String targetExtract;

    @Column(length = COLUMNS_LENGTH)
    String columnName;

    @Column(length = COLUMNS_LENGTH)
    String datatype;

    @Column(length = COLUMNS_LENGTH)
    String scdType;

    @NotNull
    @Column(length = COLUMNS_LENGTH)
    String generalRuleApplied;

    public Field() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public static String getUserStoryNumber() {
        return userStoryNumber;
    }

    public static void setUserStoryNumber(String userStoryNumber) {
        Field.userStoryNumber = userStoryNumber;
    }

    public static String getEntityName() {
        return entityName;
    }

    public static void setEntityName(String entityName) {
        Field.entityName = entityName;
    }

    public String getTargetExtract() {
        return targetExtract;
    }

    public void setTargetExtract(String targetExtract) {
        this.targetExtract = targetExtract;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getScdType() {
        return scdType;
    }

    public void setScdType(String scdType) {
        this.scdType = scdType;
    }

    public String getGeneralRuleApplied() {
        return generalRuleApplied;
    }

    public void setGeneralRuleApplied(String generalRuleApplied) {
        this.generalRuleApplied = generalRuleApplied;
    }
}
