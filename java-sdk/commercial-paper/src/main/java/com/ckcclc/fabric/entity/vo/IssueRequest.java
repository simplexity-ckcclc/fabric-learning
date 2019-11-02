package com.ckcclc.fabric.entity.vo;


import lombok.Data;

@Data
public class IssueRequest {

    private String issuer;
    private String paperNumber;
    private Long issueDateTime;
    private Long maturityDateTime;
    private Long faceValue;
}
