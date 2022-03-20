// Meno študenta: Roman Bitarovský
// Blockchain by mal na uspokojenie funkcií udržiavať iba obmedzené množstvo uzlov
// Nemali by ste mať všetky bloky pridané do blockchainu v pamäti  
// pretože by to spôsobilo pretečenie pamäte.
import java.util.ArrayList;
import java.util.HashMap;

public class Blockchain {

    public static final int CUT_OFF_AGE = 12;

    // všetky potrebné informácie na spracovanie bloku v reťazi blokov
    private class BlockNode {
        public Block b;
        public BlockNode parent;
        public ArrayList<BlockNode> children;
        public int height;
        // utxo pool na vytvorenie nového bloku na vrchu tohto bloku
        private UTXOPool uPool;

        public BlockNode(Block b, BlockNode parent, UTXOPool uPool) {
            this.b = b;
            this.parent = parent;
            children = new ArrayList<BlockNode>();
            this.uPool = uPool;
            if (parent != null) {
                height = parent.height + 1;
                parent.children.add(this);
            } else {
                height = 1;
            }
        }
        public UTXOPool getUTXOPoolCopy() {
            return new UTXOPool(uPool);
         }
    }

    /**
     * vytvor prázdny blockchain iba s Genesis blokom. Predpokladajme, že
     * {@code genesisBlock} je platný blok
     */
    // premenne pre moj vlatny blockchain
    private TransactionPool txPool; // pool tx odkial sa budu brat transakcie na zostavenie blocku
    private HashMap<ByteArrayWrapper, BlockNode> blockChain;
    private BlockNode highestNode; // posledny node
    public Blockchain(Block genesisBlock) {
        // IMPLEMENTOVAŤ
        this.blockChain = new HashMap<>();
        this.txPool = new TransactionPool();

        UTXOPool UTXOPool = new UTXOPool();
        addCoinbaseTx(genesisBlock, UTXOPool);

        BlockNode genesisNode = new BlockNode(genesisBlock, null, UTXOPool);

        this.blockChain.put(new ByteArrayWrapper((genesisBlock.getHash())), genesisNode);

        this.highestNode = genesisNode;
    }

    /** Získaj maximum height blok */
    public Block getBlockAtMaxHeight() {
        // IMPLEMENTOVAŤ
        return this.highestNode.b;
    }

    /** Získaj UTXOPool na ťaženie nového bloku na vrchu max height blok */
    public UTXOPool getUTXOPoolAtMaxHeight() {
        // IMPLEMENTOVAŤ
        return this.highestNode.getUTXOPoolCopy();
    }

    /** Získaj pool transakcií na vyťaženie nového bloku */
    public TransactionPool getTransactionPool() {
        // IMPLEMENTOVAŤ
        return this.txPool;
    }

    /**
     * Pridaj {@code block} do blockchainu, ak je platný. Kvôli platnosti by mali
     * byť všetky transakcie platné a blok by mal byť na
     * {@code height > (maxHeight - CUT_OFF_AGE)}.
     *
     * Môžete napríklad vyskúšať vytvoriť nový blok nad blokom Genesis (výška bloku
     * 2), ak height blockchainu je {@code <=
     * CUT_OFF_AGE + 1}. Len čo {@code height > CUT_OFF_AGE + 1}, nemôžete vytvoriť
     * nový blok vo výške 2.
     *
     * @return true, ak je blok úspešne pridaný
     */
    public boolean blockAdd(Block block) {
        // IMPLEMENTOVAŤ

        // zober predosli blok ak nieje null
        if (block.getPrevBlockHash() == null){
            System.out.println("False: previousBlock == null");
            return false;
        }

        // zober node tohto predosleho bloku ak nieje null
        BlockNode parentBlockNode = this.blockChain.get(new ByteArrayWrapper(block.getPrevBlockHash()));
        if (parentBlockNode == null) {
            System.out.println("False: parentBlockNode == null");
            return false;
        }

        // zober UTXO pool predchadzajuceho bloku
        HandleTxs handlerTxs = new HandleTxs(parentBlockNode.getUTXOPoolCopy());
        Transaction[] txs = block.getTransactions().toArray(new Transaction[0]);

        // skontroluj txs predchadzajuceho bloku
        if (handlerTxs.handler(txs).length != txs.length){
            System.out.println("False: handlerTxs.handler(txs).length != txs.length");
            return false;
        }

        UTXOPool utxoPool = handlerTxs.UTXOPoolGet();
        // coinbase tx teda vymajnovany BTC treba do bloku pridat ako prvu transakciu
        addCoinbaseTx(block, utxoPool);

        // skontroluj vysku
        if (parentBlockNode.height + 1 <= highestNode.height - CUT_OFF_AGE){
            System.out.println("False: parentBlockNode.height + 1 <= highestNode.height - CUT_OFF_AGE");
            return false;
        }

        // ak je teda vsetko v pohode tak pridaj block do blockchainu
        BlockNode node = new BlockNode(block, parentBlockNode, utxoPool);
        this.blockChain.put(new ByteArrayWrapper(block.getHash()), node);
        // nastav novy najvyssi node
        if (parentBlockNode.height + 1 > this.highestNode.height){
            this.highestNode = node;
        }

        // tx kt. su v bloku locknute odober z txPoolu lebo uz su vykonane
        block.getTransactions().forEach( (tx) -> {
            this.txPool.removeTransaction(tx.getHash());
        });

        return true;
    }

    /** Pridaj transakciu do transakčného poolu */
    public void transactionAdd(Transaction tx) {
        // IMPLEMENTOVAŤ
        this.txPool.addTransaction(tx);
    }

    // funkcia na pridanie coinbase tx
    private void addCoinbaseTx(Block block, UTXOPool utxoPool){
        Transaction coinbase = block.getCoinbase();
        UTXO utxoCoinbase = new UTXO(coinbase.getHash(),0);
        utxoPool.addUTXO(utxoCoinbase, coinbase.getOutput(0));
    }
}