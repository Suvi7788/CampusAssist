package lk.disontech.campusassist.model;

public class WriterEarningItem {
    private final String assignmentId;
    private final String assignmentTitle;
    private final String studentName;
    private final double amount;
    private final String paymentStatus;
    private final long paidAtMillis;

    public WriterEarningItem(String assignmentId,
                             String assignmentTitle,
                             String studentName,
                             double amount,
                             String paymentStatus,
                             long paidAtMillis) {
        this.assignmentId = assignmentId;
        this.assignmentTitle = assignmentTitle;
        this.studentName = studentName;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.paidAtMillis = paidAtMillis;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public String getAssignmentTitle() {
        return assignmentTitle;
    }

    public String getStudentName() {
        return studentName;
    }

    public double getAmount() {
        return amount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public long getPaidAtMillis() {
        return paidAtMillis;
    }
}

