package fm.douban.model;

import java.io.Serializable;
import java.util.List;

public class MhzViewModel implements Serializable {
    private String title;
    private List<Subject> subjects;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }
}
