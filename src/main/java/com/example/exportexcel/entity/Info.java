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
    @Column(name = "employment_type")
    private String employmentType;
    private String status;
    @Column(name = "join_date")
    private String joinDate;
    @Column(name = "registered_department")
    private String registeredDepartment;
    @Column(name = "new_department")
    private String newDepartment;
    private String position1;
    private String position2;
    private String job;
    @Column(name = "employee_number")
    private String employeeNumber;
    @Column(columnDefinition = "longtext", name = "detail_work")
    private String detailWork;
}
