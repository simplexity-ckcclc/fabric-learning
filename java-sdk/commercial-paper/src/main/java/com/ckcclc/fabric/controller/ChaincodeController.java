package com.ckcclc.fabric.controller;

import com.ckcclc.fabric.common.Result;
import com.ckcclc.fabric.entity.vo.IssueRequest;
import com.ckcclc.fabric.service.ChaincodeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author hyc 2018/12/03
 * @desc
 **/
@RestController
@RequestMapping("/chaincode")
@Api(value = "Chaincode服务", description = "Chaincode服务", tags = {"Chaincode Service"})
public class ChaincodeController {

    @Autowired
    private ChaincodeService chaincodeService;

    @GetMapping(value = "/query", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "查询", notes = "查询", response = Result.class)
    protected ResponseEntity<?> query(@ApiParam(value = "签发者") @RequestParam(value = "issuer") String issuer,
                                      @ApiParam(value = "票据编号") @RequestParam(value = "paperNumber") String paperNumber) {
        Result result = chaincodeService.query(issuer, paperNumber);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/issue", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "签发", notes = "签发", response = Result.class)
    public ResponseEntity<?> issue(@RequestBody IssueRequest request) {
        Result result = chaincodeService.issue(request);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
