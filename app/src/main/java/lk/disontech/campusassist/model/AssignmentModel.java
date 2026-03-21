package lk.disontech.campusassist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentModel {
    private String assignmentId;
    private String studentId;
    private String studentName;
    private String studentEmail;
    private String title;
    private String subject;
    private String description;
    private String deadline;
    private String deliveryAddress;
    private Double deliveryLatitude;
    private Double deliveryLongitude;
    private String fileUrl;
    private String fileName;
    private long createdAt;
    private long updatedAt;
    private String status; // "Open", "Assigned", "Completed"
    private String assignedWriterId;
    private String assignedWriterName;
    private String submissionFileUrl;
    private String submissionFileName;
    private String submissionNotes;
    private String submissionWriterId;
    private long submissionAt;
}