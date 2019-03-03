package app.dhruv.javaannotate.models;

import app.dhruv.javaannotate.annotations.Model;

import javax.lang.model.element.TypeElement;
import java.util.List;

public class ModelsMap {
    private TypeElement mainClass;

    private String packageName;

    private List<Model> replicatedClasses;

    public TypeElement getMainClass() {
        return mainClass;
    }

    public void setMainClass(TypeElement mainClass) {
        this.mainClass = mainClass;
    }

    public List<Model> getReplicatedClasses() {
        return replicatedClasses;
    }

    public void setReplicatedClasses(List<Model> replicatedClasses) {
        this.replicatedClasses = replicatedClasses;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
