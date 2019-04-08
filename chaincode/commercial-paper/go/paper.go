package main

type PaperStatus int

const (
    ISSUED     PaperStatus = iota
    TRADING
    REDEEMED

)

type paper struct {
    PaperNumber         int
    Issuer              string
    Owner               string
    IssueDateTime       string
    MaturityDateTime    string
    FaceValue           int
    Status              PaperStatus
}

var statuses = [...]string{"Issued", "Trading", "Redeemed"}

func (status PaperStatus) String() string {
    return statuses[status]
}