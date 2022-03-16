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

    // premenne pre moj vlatny blockchain
    private TransactionPool txPool;
    private HashMap<ByteArrayWrapper, BlockNode> blockChain;
    private BlockNode highestNode;

    /**
     * vytvor prázdny blockchain iba s Genesis blokom. Predpokladajme, že
     * {@code genesisBlock} je platný blok
     */
    public Blockchain(Block genesisBlock) {
        // IMPLEMENTOVAŤ
        blockChain = new HashMap<>();
        this.txPool = new TransactionPool();

        UTXOPool UTXOPool = new UTXOPool();
        addCoinbaseTx(genesisBlock, UTXOPool);

        BlockNode genesisNode = new BlockNode(genesisBlock, null, UTXOPool);
        highestNode = genesisNode;

        blockChain.put(new ByteArrayWrapper((genesisBlock.getHash())), genesisNode);


    }

    /** Získaj maximum height blok */
    public Block getBlockAtMaxHeight() {
        // IMPLEMENTOVAŤ
        return highestNode.b;
    }

    /** Získaj UTXOPool na ťaženie nového bloku na vrchu max height blok */
    public UTXOPool getUTXOPoolAtMaxHeight() {
        // IMPLEMENTOVAŤ
        return highestNode.getUTXOPoolCopy();
    }

    /** Získaj pool transakcií na vyťaženie nového bloku */
    public TransactionPool getTransactionPool() {
        // IMPLEMENTOVAŤ
        return txPool;
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
        byte[] previousBlock = block.getPrevBlockHash();
        if (previousBlock == null){
            return false;
        }

        // zober parenta ak nieje null
        BlockNode parentBlockNode = blockChain.get(new ByteArrayWrapper(previousBlock));
        if (parentBlockNode == null) {
            return false;
        }

        // zober UTXO pool predchadzajuceho bloku
        HandleTxs handlerTxs = new HandleTxs(parentBlockNode.getUTXOPoolCopy());
        Transaction[] txs = block.getTransactions().toArray(new Transaction[0]);

        // skontroluj txs predchadzajuceho bloku
        Transaction[] validTxs = handlerTxs.handler(txs);
        if (validTxs.length != txs.length) {
            return false;
        }

        if (parentBlockNode.height + 1 <= highestNode.height - CUT_OFF_AGE){
            return false;
        }

        UTXOPool utxoPool = handlerTxs.UTXOPoolGet();

        // coinbase tx teda vymajnovany BTC treba do bloku pridat ako prvu transakciu
        addCoinbaseTx(block, utxoPool);

        BlockNode node = new BlockNode(block, parentBlockNode, utxoPool);

        blockChain.put(new ByteArrayWrapper(block.getHash()), node);

        if (parentBlockNode.height + 1 > highestNode.height){
            highestNode = node;
        }

        return true;
    }

    /** Pridaj transakciu do transakčného poolu */
    public void transactionAdd(Transaction tx) {
        // IMPLEMENTOVAŤ
        txPool.addTransaction(tx);
    }

    // funkcia na pridanie coinbase tx
    private void addCoinbaseTx(Block block, UTXOPool utxoPool){
        Transaction coinbase = block.getCoinbase();
        UTXO utxoCoinbase = new UTXO(coinbase.getHash(), 0);
        utxoPool.addUTXO(utxoCoinbase, coinbase.getOutput(0));
    }
}