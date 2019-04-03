### Orderer

1. How many orderer in one channel? If not only one, how they communicate and keep sync, how to guatantee globle order? If only one, how to avoid single-point failure?

### Endorsor

1. When endorsor endorse the previous transaction proposal and execute over world state db, how to response to following transaction proposal, over the origin state or the state modified by previous proposal? If the first proposal fail, how to deal with the following proposals?

* IF *a ledger consistency check to verify that the current state of the ledger is compatible with the state of the ledger when the proposed update was generated*, the throughput is damaged.

> At the end of phase 1, the application is free to discard inconsistent transaction responses if it wishes to do so, effectively terminating the transaction workflow early. We’ll see later that if an application tries to use an inconsistent set of transaction responses to update the ledger, it will be rejected.

---

> It’s worth noting that the sequencing of transactions in a block is not necessarily the same as the order of arrival of transactions at the orderer! Transactions can be packaged in any order into a block, and it’s this sequence that becomes the order of execution. ***What’s important is that there is a strict order, rather than what that order is.***

---

> Every transaction that arrives at an orderer is mechanically packaged in a block — the orderer makes no judgement as to the value of a transaction, it simply packages it. 
* So, if one transaction proposal is fake, how to deal with others in same block?
> Failed transactions are retained for audit, but are not applied to the ledger.

---

> To do this, ***a peer must perform a ledger consistency check to verify that the current state of the ledger is compatible with the state of the ledger when the proposed update was generated.*** For example, another transaction may have updated the same asset in the ledger such that the transaction update is no longer valid and therefore can no longer be applied. After a peer has successfully validated each individual transaction, it updates the ledger. Failed transactions are not applied to the ledger, but they are retained for audit purposes, as are successful transactions. This means that peer blocks are almost exactly the same as the blocks received from the orderer, except for a valid or invalid indicator on each transaction in the block.

> Finally, every time a block is committed to a peer’s ledger, that peer generates an appropriate event. Block events include the full block content, while block transaction events include summary information only, such as whether each transaction in the block has been validated or invalidated. Applications can register for these event types so that they can be notified when they occur. 

> It means that chaincodes only have to be available on endorsing nodes, rather than throughout the blockchain network. This is often helpful as it keeps the logic of the chaincode confidential to endorsing organizations.