package lk.disontech.campusassist.model;

public class AssignmentModel {

    private String title;
    private String subject;
    private String deadline;
    private String budget;

    public AssignmentModel(String title, String subject, String deadline, String budget) {
        this.title = title;
        this.subject = subject;
        this.deadline = deadline;
        this.budget = budget;
    }

    public String getTitle() {
        return title;
    }

    public String getSubject() {
        return subject;
    }

    public String getDeadline() {
        return deadline;
    }

    public String getBudget() {
        return budget;
    }
}