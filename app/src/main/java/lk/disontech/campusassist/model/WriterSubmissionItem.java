package lk.disontech.campusassist.model;

public class WriterSubmissionItem {

    private final String assignmentId;
    private final String title;
    private final String subject;
    private final String studentId;
    private final String studentName;
    private final String deadline;
    private final String description;
    private final String fileUrl;
    private final String fileName;
    private final String assignmentStatus;
    private final long submittedAt;
    private final String priceDisplay;

    public WriterSubmissionItem(String assignmentId,
                                String title,
                                String subject,
                                String studentId,
                                String studentName,
                                String deadline,
                                String description,
                                String fileUrl,
                                String fileName,
                                String assignmentStatus,
                                long submittedAt,
                                String priceDisplay) {
        this.assignmentId = assignmentId;
        this.title = title;
        this.subject = subject;
        this.studentId = studentId;
        this.studentName = studentName;
        this.deadline = deadline;
        this.description = description;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.assignmentStatus = assignmentStatus;
        this.submittedAt = submittedAt;
        this.priceDisplay = priceDisplay;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public String getTitle() {
        return title;
    }

    public String getSubject() {
        return subject;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getDeadline() {
        return deadline;
    }

    public String getDescription() {
        return description;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public String getAssignmentStatus() {
        return assignmentStatus;
    }

    public long getSubmittedAt() {
        return submittedAt;
    }

    public String getPriceDisplay() {
        return priceDisplay;
    }
}

