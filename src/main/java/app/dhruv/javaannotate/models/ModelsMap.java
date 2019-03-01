package app.dhruv.javaannotate.models;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.List;

public class ModelsMap {
    private TypeElement mainClass;

    private String packageName;

    private List<String> replicatedClasses;

    public TypeElement getMainClass() {
        return mainClass;
    }

    public void setMainClass(TypeElement mainClass) {
        this.mainClass = mainClass;
    }

    public List<String> getReplicatedClasses() {
        return replicatedClasses;
    }

    public void setReplicatedClasses(List<String> replicatedClasses) {
        this.replicatedClasses = replicatedClasses;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
