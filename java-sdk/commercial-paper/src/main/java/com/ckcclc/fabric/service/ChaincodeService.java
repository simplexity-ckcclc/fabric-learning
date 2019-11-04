package com.ckcclc.fabric.service;

import com.ckcclc.fabric.common.ErrorCode;
import com.ckcclc.fabric.common.ParamChecker;
import com.ckcclc.fabric.common.Result;
import com.ckcclc.fabric.entity.vo.IssueRequest;

import org.hyperledger.fabric.protos.peer.FabricProposalResponse;
import org.hyperledger.fabric.sdk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import static com.ckcclc.fabric.common.ParamChecker.LongChecker.greaterThan;
import static com.ckcclc.fabric.common.ParamChecker.StringChecker.isNotBlank;
import static com.ckcclc.fabric.common.ParamChecker.nonNull;


@Service
public class ChaincodeService {

    private static final Logger logger = LoggerFactory.getLogger(ChaincodeService.class);

    private static final String QUERY_FUNC = "query";
    private static final String ISSUE_FUNC = "issue";

    @Value("${channel.id}")
    private String channelID;

    @Value("${chaincode.name}")
    private String chaincodeName;

    @Autowired
    @Qualifier(value = "adminClient")
    private HFClient admin;

    public Result issue(IssueRequest request) {
        ParamChecker.Result result = ParamChecker.newInstance()
                .add(request, nonNull(), "'request' cannot be null")
                .add(request.getIssuer(), isNotBlank(), "'issuer' cannot be blank")
                .add(request.getPaperNumber(), isNotBlank(), "'paperNumber' cannot be blank")
                .add(request.getFaceValue(), greaterThan(0L), "'faceValue' should be positive")
                .add(request.getIssueDateTime(), greaterThan(0L), "'issueDateTime' should be positive")
                .add(request.getMaturityDateTime(), greaterThan(request.getIssueDateTime()),
                        "'maturityDateTime' should be greater than 'issueDateTime'")
                .check();
        if (!result.isValid()) {
            return Result.fail(ErrorCode.REQUEST_PARAMETER_ERROR).withErrorMsg(result.getErrorMsg());
        }

        String issueDate, maturityDate;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            issueDate = sdf.format(new Date(request.getIssueDateTime()));
            maturityDate = sdf.format(new Date(request.getMaturityDateTime()));
        } catch (Exception e) {
            return Result.fail(ErrorCode.REQUEST_PARAMETER_ERROR).withErrorMsg("issueDateTime' or 'maturityDateTime' invalid");
        }

        //提交链码交易
        TransactionProposalRequest proposalRequest = admin.newTransactionProposalRequest();
        ChaincodeID cid = ChaincodeID.newBuilder().setName(chaincodeName).build();
        proposalRequest.setChaincodeID(cid);
        proposalRequest.setFcn(ISSUE_FUNC);
        proposalRequest.setArgs(request.getIssuer(), request.getPaperNumber(), issueDate, maturityDate,
                String.valueOf(request.getFaceValue()));


        try {
            Channel channel = admin.getChannel(channelID);
            Collection<ProposalResponse> propResps = channel.sendTransactionProposal(proposalRequest);
            Collection<Set<ProposalResponse>> proposalConsistencySets = SDKUtils.getProposalConsistencySets(propResps);
            if (proposalConsistencySets.size() != 1) {
                return Result.fail(ErrorCode.CHAINCOED_SERVICE_ERROR)
                        .withErrorMsg("Expected only one set of consistent proposal responses but got more");
            }

            BlockEvent.TransactionEvent event = channel.sendTransaction(propResps).get();
            logger.info("txid : {}, event is valid? : {}", event.getTransactionID(), event.isValid());
            if (event.isValid()) {
                return Result.success().withResponse("txid : " + event.getTransactionID());
            }
            return Result.fail(ErrorCode.CHAINCOED_SERVICE_ERROR);
        } catch (Exception e) {
            logger.warn("query chaincode : {} ex", chaincodeName, e);
            return Result.fail(ErrorCode.CHAINCOED_INVOKE_ERROR).withErrorMsg(e.getMessage());
        }
    }

    public Result query(String issuer, String paperNumber) {
        ParamChecker.Result result = ParamChecker.newInstance()
                .add(issuer, isNotBlank(), "'issuer' cannot be blank")
                .add(paperNumber, isNotBlank(), "'paperNumber' cannot be blank")
                .check();
        if (!result.isValid()) {
            return Result.fail(ErrorCode.REQUEST_PARAMETER_ERROR).withErrorMsg(result.getErrorMsg());
        }

        QueryByChaincodeRequest req = admin.newQueryProposalRequest();
        ChaincodeID cid = ChaincodeID.newBuilder().setName(chaincodeName).build();
        req.setChaincodeID(cid);
        req.setFcn(QUERY_FUNC);
        req.setArgs(issuer, paperNumber);

        try {
            Channel channel = admin.getChannel(channelID);
            Collection<ProposalResponse> propResps = channel.queryByChaincode(req);
            Collection<Set<ProposalResponse>> proposalConsistencySets = SDKUtils.getProposalConsistencySets(propResps);
            if (proposalConsistencySets.size() != 1) {
                return Result.fail(ErrorCode.CHAINCOED_SERVICE_ERROR)
                        .withErrorMsg("Expected only one set of consistent proposal responses but got more");
            }

            FabricProposalResponse.Response res = propResps.iterator().next().getProposalResponse().getResponse();
            if (res.getStatus() == 200) {
                return Result.success().withResponse(res.getPayload().toStringUtf8());
            }
            return Result.fail(ErrorCode.CHAINCOED_SERVICE_ERROR).withErrorMsg(res.getMessage());
        } catch (Exception e) {
            logger.warn("query chaincode : {} ex", chaincodeName, e);
            return Result.fail(ErrorCode.CHAINCOED_INVOKE_ERROR).withErrorMsg(e.getMessage());
        }
    }
}
