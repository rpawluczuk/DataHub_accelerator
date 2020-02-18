package springapp.datahubaccelerator.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    static String userStoryNumber;
    static String entityName;
    String targetExtract;
    String columnName;
    String datatype;

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


}
