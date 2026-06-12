package com.transport.api.company.entity;

import com.transport.api.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "company")
@Getter
@Setter
public class Company extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;
}