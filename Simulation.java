// Example of a Simulation. This test runs the nodes on a random graph.
// At the end, it will print out the Transaction ids which each node
// believes consensus has been reached upon. You can use this simulation to
// test your nodes. You will want to try creating some deviant nodes and
// mixing them in the network to fully test.

import java.util.*;

public class Simulation {

    public static void main(String[] args) {

        // There are four required command line arguments: p_graph (.1, .2, .3),
        // p_malicious (.15, .30, .45), p_txDistribution (.01, .05, .10),
        // and numRounds (10, 20). You should try to test your CompliantNode
        // code for all 3x3x3x2 = 54 combinations.

        int numNodes = 100;
        double p_graph = Double.parseDouble(args[0]); // parameter for random graph: prob. that an edge will exist
        double p_malicious = Double.parseDouble(args[1]); // prob. that a node will be set to be malicious
        double p_txDistribution = Double.parseDouble(args[2]); // probability of assigning an initial transaction to each node
        int numRounds = Integer.parseInt(args[3]); // number of simulation rounds your nodes will run for

        // pick which nodes are malicious and which are compliant
        Node[] nodes = new Node[numNodes];
        int count = 0;
        for (int i = 0; i < numNodes; i++) {
            if(Math.random() < p_malicious)
                // When you are ready to try testing with malicious nodes, replace the
                // instantiation below with an instantiation of a MaliciousNode
               nodes[i] = new MaliciousNode(p_graph, p_malicious, p_txDistribution, numRounds);
            else
                nodes[i] = new CompliantNode(p_graph, p_malicious, p_txDistribution, numRounds);
        }


        // initialize random follow graph
        boolean[][] followees = new boolean[numNodes][numNodes]; // followees[i][j] is true iff i follows j
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                if (i == j) continue;
                if(Math.random() < p_graph) { // p_graph is .1, .2, or .3
                    followees[i][j] = true;
                }
            }
        }

        // notify all nodes of their followees
        for (int i = 0; i < numNodes; i++)
            nodes[i].setFollowees(followees[i]);

        // initialize a set of 50 valid Transactions with random ids
        int numTx = 50;
        //HashSet<Integer,String> validTxIds = new HashSet<Integer,String>();
        HashMap<Integer, String> validTxIds =  new HashMap<>();
        Random random = new Random();
        for (int i = 0; i < numTx; i++) {
            int r = random.nextInt();
            String txAddress = PublicKeyEncoder.PubkeyEnc(Integer.toString(r));
            validTxIds.put(i, txAddress);
        }


        // distribute the 50 Transactions throughout the nodes, to initialize
        // the starting state of Transactions each node has heard. The distribution
        // is random with probability p_txDistribution for each Transaction-Node pair.
        for (int i = 0; i < numNodes; i++) {
            HashSet<Transaction> pendingTransactions = new HashSet<Transaction>();
            for(Map.Entry<Integer, String> entry : validTxIds.entrySet()) {
                Integer txID = entry.getKey();
                String address = entry.getValue();
                if (Math.random() < p_txDistribution) // p_txDistribution is .01, .05, or .10.
                    pendingTransactions.add(new Transaction(txID, address));
            }
            nodes[i].setPendingTransaction(pendingTransactions);
        }


        // Simulate for numRounds times
        for (int round = 0; round < numRounds; round++) { // numRounds is either 10 or 20

            // gather all the proposals into a map. The key is the index of the node receiving
            // proposals. The value is an ArrayList containing 1x2 Integer arrays. The first
            // element of each array is the id of the transaction being proposed and the second
            // element is the index # of the node proposing the transaction.
            HashMap<Integer, Set<Candidate>> allProposals = new HashMap<>();

            for (int i = 0; i < numNodes; i++) {
                Set<Transaction> proposals = nodes[i].sendToFollowers();
                for (Transaction tx : proposals) {
                    if (!validTxIds.containsKey(tx.id))
                        continue; // ensure that each tx is actually valid

                    for (int j = 0; j < numNodes; j++) {
                        if(!followees[j][i]) continue; // tx only matters if j follows i

                        if (!allProposals.containsKey(j)) {
                            Set<Candidate> candidates = new HashSet<>();
                            allProposals.put(j, candidates);
                        }

                        Candidate candidate = new Candidate(tx, i);
                        allProposals.get(j).add(candidate);
                    }

                }
            }

            // Distribute the Proposals to their intended recipients as Candidates
            for (int i = 0; i < numNodes; i++) {
                if (allProposals.containsKey(i))
                    nodes[i].receiveFromFollowees(allProposals.get(i));
            }
        }

        // print results
        Map<String,Integer> countMap = new HashMap<String, Integer>();
        for (int i = 0; i < numNodes; i++) {
            Set<Transaction> transactions = nodes[i].sendToFollowers();
            for (Transaction tx : transactions) {
                if(countMap.containsKey(tx.address)){
                    countMap.put(tx.address,countMap.get(tx.address)+1);
                }
                else{
                    countMap.put(tx.address,1);
                }
            }
        }
        int consensusCount = 0;
        System.out.println("The transactions that reached consensus are: ");
        System.out.println("**********************************************************************************************");
        for(Map.Entry<String,Integer> entry : countMap.entrySet()){
            if(entry.getValue() > (0.51*numNodes)) {
                System.out.println(entry.getKey());
                consensusCount++;
            }
        }
        System.out.println("**********************************************************************************************");
        System.out.println("The no of transactions that reached consensus: " + consensusCount);

    }


}