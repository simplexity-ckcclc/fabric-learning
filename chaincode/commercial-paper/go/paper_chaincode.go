package main

import (
    "encoding/json"
    "fmt"
    "github.com/hyperledger/fabric/core/chaincode/shim"
    pb "github.com/hyperledger/fabric/protos/peer"
    "strconv"
)

type PaperChaincode struct {

}

func (cc *PaperChaincode) Init(stub shim.ChaincodeStubInterface) pb.Response {
    return shim.Success(nil)
}


func (cc *PaperChaincode) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
    fmt.Println("Paper Chaincode Invoke")
    function, args := stub.GetFunctionAndParameters()
    if function == "issue" {
        return cc.issue(stub, args)
    } else if function == "buy" {
        return cc.buy(stub, args)
    } else if function == "redeem" {
        return cc.redeem(stub, args)
    } else if function == "query" {
        return cc.query(stub, args)
    }

    return shim.Error("Invalid invoke function name : " + function)
}

func (cc *PaperChaincode) issue(stub shim.ChaincodeStubInterface, args []string) pb.Response {
    if len(args) != 5 {
        return shim.Error("Invalid arguments")
    }

    pIssuer := args[0]
    pOwner := args[0]

    pPaperNumber := args[1]
    pId, err := strconv.Atoi(pPaperNumber)
    if err != nil {
        return shim.Error("paperId cannot convert to integer")
    }
    pIssueDateTime := args[2]
    pMaturityDateTime := args[3]
    pFaceValue, err := strconv.Atoi(args[4])
    if err != nil {
        return shim.Error("faceValue cannot convert to integer")
    }

    paperKey := cc.paperKey(pIssuer, pId)
    if paper, _ := stub.GetState(paperKey); paper != nil {
        return shim.Error("Paper " + pIssuer + pPaperNumber + " already exists")
    }

    paper := &paper{
        PaperNumber : pId,
        Issuer: pIssuer,
        Owner:  pOwner,
        IssueDateTime:  pIssueDateTime,
        MaturityDateTime:   pMaturityDateTime,
        FaceValue:  pFaceValue,
    }
    paper.Status = ISSUED

    paperBytes, err := json.Marshal(paper)
    if err != nil {
        return shim.Error("Error marshal paper")
    }

    if err := stub.PutState(paperKey, paperBytes); err != nil {
        return shim.Error("Error issue paper")
    }
    fmt.Printf("[Issue]Put paper state key:%s%s, value: %s\n", pIssuer, pPaperNumber, paperBytes)
    return shim.Success(paperBytes)
}

func (cc *PaperChaincode) buy(stub shim.ChaincodeStubInterface, args []string) pb.Response {
    if len(args) != 4 {
        return shim.Error("Invalid arguments")
    }

    pIssuer := args[0]
    pPaperNumber := args[1]
    pId, err := strconv.Atoi(pPaperNumber)
    if err != nil {
        return shim.Error("paperId cannot convert to integer")
    }
    pCurrentOwner := args[2]
    pNewOwner := args[3]
    //pPrice := args[4]
    //pPurchaseDateTime := args[5]

    paperKey := cc.paperKey(pIssuer, pId)
    bytes, _ := stub.GetState(paperKey)
    if bytes == nil {
        return shim.Error("Paper " + pIssuer + pPaperNumber + " not exists")
    }

    paper := paper{}
    if err := json.Unmarshal(bytes, &paper); err != nil {
        return shim.Error("Error unmarshal paper")
    }

    if paper.Owner != pCurrentOwner {
        return shim.Error("Paper " + pIssuer + pPaperNumber + " is not owned by " + pCurrentOwner)
    }

    if paper.Status == ISSUED {
        paper.Status = TRADING
    }

    if paper.Status == TRADING {
        paper.Owner = pNewOwner
    } else {
        return shim.Error("Paper " + pIssuer + pPaperNumber + " is not trading. Current state = " + paper.Status.String())
    }

    paperBytes, err := json.Marshal(paper)
    if err != nil {
        return shim.Error("Error marshal paper")
    }

    if err := stub.PutState(paperKey, paperBytes); err != nil {
        return shim.Error("Error update paper")
    }
    fmt.Printf("[Buy]Put paper state key:%s%s, value: %s\n", pIssuer, pPaperNumber, paperBytes)
    return shim.Success(paperBytes)
}

func (cc *PaperChaincode) redeem(stub shim.ChaincodeStubInterface, args []string) pb.Response {
    if len(args) != 3 {
        return shim.Error("Invalid arguments")
    }

    pIssuer := args[0]
    pPaperNumber := args[1]
    pId, err := strconv.Atoi(pPaperNumber)
    if err != nil {
        return shim.Error("paperId cannot convert to integer")
    }
    pRedeemingOwner := args[2]
    //pRedeemDateTime := args[3]

    paperKey := cc.paperKey(pIssuer, pId)
    bytes, _ := stub.GetState(paperKey)
    if bytes == nil {
        return shim.Error("Paper " + pIssuer + pPaperNumber + " not exists")
    }

    paper := paper{}
    if err := json.Unmarshal(bytes, &paper); err != nil {
        return shim.Error("Error unmarshal paper")
    }

    if paper.Status == REDEEMED {
        return shim.Error("Paper " + pIssuer + pPaperNumber + " already redeemed")
    }

    if paper.Owner != pRedeemingOwner {
        return shim.Error("Paper " + pIssuer + pPaperNumber + " is not owned by " + pRedeemingOwner)
    }

    paper.Owner = paper.Issuer
    paper.Status = REDEEMED

    paperBytes, err := json.Marshal(paper)
    if err != nil {
        return shim.Error("Error marshal paper")
    }

    if err := stub.PutState(paperKey, paperBytes); err != nil {
        return shim.Error("Error update paper")
    }
    fmt.Printf("[Redeem]Put paper state key:%s%s, value: %s\n", pIssuer, pPaperNumber, paperBytes)
    return shim.Success(paperBytes)
}

func (cc *PaperChaincode) query(stub shim.ChaincodeStubInterface, args []string) pb.Response {
    if len(args) != 2 {
        return shim.Error("Invalid arguments")
    }

    pIssuer := args[0]
    pPaperNumber := args[1]
    pId, err := strconv.Atoi(pPaperNumber)
    if err != nil {
        return shim.Error("paperId cannot convert to integer")
    }

    paperKey := cc.paperKey(pIssuer, pId)
    bytes, _ := stub.GetState(paperKey)
    if bytes == nil {
        return shim.Error("Paper " + pIssuer + pPaperNumber + " not exists")
    }
    fmt.Printf("[Query]Get paper state key:%s%s, value: %s\n", pIssuer, pPaperNumber, bytes)
    return shim.Success(bytes)
}

func (cc *PaperChaincode) paperKey(issuer string, id int) string {

    return issuer + strconv.Itoa(id)
}

func main()  {
    err := shim.Start(new(PaperChaincode))
    if err != nil {
        fmt.Printf("Error starting Simple chaincode: %s", err)
    }
}