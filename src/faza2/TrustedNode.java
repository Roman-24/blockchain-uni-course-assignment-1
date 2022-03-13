package faza2;

// Meno študenta: Roman Bitarovský
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/* TrustedNode označuje uzol, ktorý dodržuje pravidlá (nie je byzantský) */
public class TrustedNode implements Node {

    // TrustedNode func
    public double p_graph;
    public double p_byzantine;
    public double p_tXDistribution;
    public int numRounds;
    // followeesSet func func
    public boolean[] followees;
    // pendingTransactionSet func
    public Set<Transaction> pendingTransactions;

    // add by me
    private boolean[] notTrustedNodes;

    public TrustedNode(double p_graph, double p_byzantine, double p_txDistribution, int numRounds) {
        // IMPLEMENTOVAŤ
        this.p_graph = p_graph;
        this.p_byzantine = p_byzantine;
        this.p_tXDistribution = p_txDistribution;
        this.numRounds = numRounds;
    }

    public void followeesSet(boolean[] followees) {
        // IMPLEMENTOVAŤ
        this.followees = followees;
        this.notTrustedNodes = new boolean[followees.length];
    }

    public void pendingTransactionSet(Set<Transaction> pendingTransactions) {
        // IMPLEMENTOVAŤ
        this.pendingTransactions = pendingTransactions;
    }

    public Set<Transaction> followersSend() {
        // IMPLEMENTOVAŤ
        Set<Transaction> toSend = new HashSet<>(pendingTransactions);
        pendingTransactions.clear();
        return toSend;
    }

    public void followeesReceive(ArrayList<Integer[]> candidates) {
        // IMPLEMENTOVAŤ

        //convert arraylist to set
        Set<Candidate> setOfCandidates = new HashSet<Candidate>();
        candidates.forEach((c) -> {
            Candidate tempCandidate = new Candidate(new Transaction(c[0]), c[1]);
            setOfCandidates.add(tempCandidate);
        });

        Set<Integer> senders = setOfCandidates.stream().map(candidate -> candidate.sender).collect(toSet());
        for (int i = 0; i < followees.length; i++) {
            if (followees[i] && !senders.contains(i))
                notTrustedNodes[i] = true;
        }
        for (Candidate candidate : setOfCandidates) {
            if (!notTrustedNodes[candidate.sender]) {
                pendingTransactions.add(candidate.tx);
            }
        }
    }
}
