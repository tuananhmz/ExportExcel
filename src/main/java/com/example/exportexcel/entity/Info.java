package com.example.exportexcel.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "info")
public class Info {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SequenceGenerator(name = "info_seq", sequenceName = "info_sequence", initialValue = 1, allocationSize = 1)
    private Long id;
    private String name;
    private String email;
    private String employmentType;
    private String status;
    private String joinDate;
    private String registeredDepartment;
    private String newDepartment;
    private String position1;
    private String position2;
    private String job;
    private String employeeNumber;
    @Column(columnDefinition = "longtext")
    private String detailWork;
}
