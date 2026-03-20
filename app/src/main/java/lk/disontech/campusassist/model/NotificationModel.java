package lk.disontech.campusassist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationModel {
    private String notificationId;
    private String recipientUserId;
    private String recipientRole;
    private String title;
    private String message;
    private String assignmentId;
    private String assignmentTitle;
    private String assignmentStatus;
    private String writerWorkStatus;
    private long createdAt;
    private boolean read;
}

