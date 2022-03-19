

// Meno študenta: Roman Bitarovský
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/* TrustedNode označuje uzol, ktorý dodržuje pravidlá (nie je byzantský) */
public class TrustedNode implements Node {

    // TrustedNode func vars
    public double p_graph;
    public double p_byzantine;
    public double p_tXDistribution;
    public int numRounds;
    // followeesSet func func vars
    public boolean[] followees;
    // pendingTransactionSet func vars
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

        // konvert arraylist na set
        // set bude storovat objekty typu triedy Candidate -> Candidate(Transaction tx, int sender)
        Set<Candidate> setOfCandidates = new HashSet<Candidate>();
        candidates.forEach((c) -> {
            Candidate tempCandidate = new Candidate(new Transaction(c[0]), c[1]);
            setOfCandidates.add(tempCandidate);
        });

        // stream aby sme mohli uplatnit funkciu map
        Stream<Candidate> sendersSteam = setOfCandidates.stream();
        // namapuj unikatkych senderov
        Stream<Object> sendersMap = sendersSteam.map(candidate -> candidate.sender);
        Set<Object> senders = sendersMap.collect(toSet());

        for (int i = 0; i < followees.length; i++) {

            // ak node vysiela ale nieje v senders
            if (followees[i] && senders.contains(i) !=  true)
                notTrustedNodes[i] = true;
        }
        for (Candidate candidate : setOfCandidates) {

            // ak sender nieje v nedôverihodnych nodes tak jeho tx bude uznana
            // teda pridasa do cakajucich tx
            if (notTrustedNodes[candidate.sender] != true) {
                pendingTransactions.add(candidate.tx);
            }
        }
    }
}
