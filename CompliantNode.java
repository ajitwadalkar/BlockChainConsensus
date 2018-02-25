import java.util.HashMap;
import java.util.Set;
public class CompliantNode implements Node {

    boolean[] followees;
    Set<Transaction> proposedTransactions;
    HashMap<Transaction, Set<Integer>> newCandidates;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {

    }

    public void setFollowees(boolean[] followees) {
        this.followees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
         proposedTransactions = pendingTransactions;
    }


    public Set<Transaction> sendToFollowers() {
            return proposedTransactions;
     }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        //assuming all transactions are valid
        for (Candidate c: candidates) {
            if (followees[c.sender])
                proposedTransactions.add(c.tx);
        }


    }
}