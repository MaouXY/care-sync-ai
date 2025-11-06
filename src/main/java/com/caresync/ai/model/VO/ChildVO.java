package com.caresync.ai.model.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChildVO {
    private Long id;
    private String childNo;
    private String name;
    private Integer age;
    private String gender;
    private LocalDate birthDate;
    private String idCard;
    private String address;
    private String phone;
    private String guardianName;
    private String guardianPhone;
    private String socialWorkerName;
}
