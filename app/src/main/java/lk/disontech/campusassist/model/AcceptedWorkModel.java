package lk.disontech.campusassist.model;

public class AcceptedWorkModel {

    private final String bidId;
    private final String assignmentId;
    private final String title;
    private final String subject;
    private final String studentName;
    private final String dueDate;
    private final String status;
    private final String bidStatus;
    private final String assignmentStatus;
    private final String description;
    private final String fileUrl;
    private final String fileName;
    private final String studentId;
    private final Double paymentAmount;
    private final boolean canOpenDetails;
    private final boolean canCancelBid;

    public AcceptedWorkModel(String bidId,
                             String assignmentId,
                             String title,
                             String subject,
                             String studentName,
                             String dueDate,
                             String status,
                             String bidStatus,
                             String assignmentStatus,
                             String description,
                             String fileUrl,
                             String fileName,
                             String studentId,
                             Double paymentAmount,
                             boolean canOpenDetails,
                             boolean canCancelBid) {
        this.bidId = bidId;
        this.assignmentId = assignmentId;
        this.title = title;
        this.subject = subject;
        this.studentName = studentName;
        this.dueDate = dueDate;
        this.status = status;
        this.bidStatus = bidStatus;
        this.assignmentStatus = assignmentStatus;
        this.description = description;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.studentId = studentId;
        this.paymentAmount = paymentAmount;
        this.canOpenDetails = canOpenDetails;
        this.canCancelBid = canCancelBid;
    }

    public String getBidId() {
        return bidId;
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

    public String getStudentName() {
        return studentName;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getStatus() {
        return status;
    }

    public String getBidStatus() {
        return bidStatus;
    }

    public String getAssignmentStatus() {
        return assignmentStatus;
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

    public String getStudentId() {
        return studentId;
    }

    public Double getPaymentAmount() {
        return paymentAmount;
    }

    public boolean canOpenDetails() {
        return canOpenDetails;
    }

    public boolean canCancelBid() {
        return canCancelBid;
    }
}