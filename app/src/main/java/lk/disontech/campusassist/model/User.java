package lk.disontech.campusassist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String uId;
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private String profilePicURL;
    private String userType;
    private String bio;
    private String interests;

}
