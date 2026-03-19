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
    private String fileUrl;
    private String fileName;
    private long createdAt;
    private long updatedAt;
    private String status; // "Open", "Assigned", "Completed"
}