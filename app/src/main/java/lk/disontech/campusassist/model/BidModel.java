package lk.disontech.campusassist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BidModel {
    private String bidId;
    private String assignmentId;
    private String studentId;
    private String writerId;
    private String writerName;
    private String writerEmail;
    private String writerMobile;
    private long createdAt;
    private String status; // "Pending", "Accepted", "Rejected"
    private long completedProjectsCount;
    private Double rating;
}

