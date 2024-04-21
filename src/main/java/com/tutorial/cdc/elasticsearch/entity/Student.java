package com.tutorial.cdc.elasticsearch.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

/**
 * ElasticSearch Student entity
 */
@Data
@Document(indexName = "student")
@Setting(replicas = 0, refreshInterval = "-1")
public class Student {
    @Id
    private Integer id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String address;

    @Field(type = FieldType.Text)
    private String email;
}