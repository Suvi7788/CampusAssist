package lk.disontech.campusassist.model;

public class AcceptedWorkModel {

    private final String title;
    private final String subject;
    private final String studentName;
    private final String dueDate;
    private final String status;

    public AcceptedWorkModel(String title, String subject, String studentName, String dueDate, String status) {
        this.title = title;
        this.subject = subject;
        this.studentName = studentName;
        this.dueDate = dueDate;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public String getSubject() {
        return subject;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getStatus() {
        return status;
    }
}