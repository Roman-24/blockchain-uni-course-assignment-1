import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

// Meno študenta: Roman Bitarovský

/*
UTXO -> Unspent transaction output
tx -> transaction
 */

public class HandleTxs {


    /**
     * Vytvorí verejný ledger, ktorého aktuálny UTXOPool (zbierka nevyčerpaných
     * transakčných výstupov) je {@code utxoPool}. Malo by to vytvoriť obchrannú kópiu
     * utxoPool pomocou konštruktora UTXOPool (UTXOPool uPool).
     */
    public UTXOPool ledger;
    public HandleTxs(UTXOPool utxoPool) {
        // IMPLEMENTOVAŤ
        ledger = new UTXOPool(utxoPool);
    }


    /**
     * @return aktuálny UTXO pool. 
     * Ak nenájde žiadny aktuálny UTXO pool, tak vráti prázdny (nie nulový) objekt {@code UTXOPool}.
     */
    public UTXOPool UTXOPoolGet() {
        // IMPLEMENTOVAŤ
        // return false;
        return this.ledger;
    }


    /**
     * @return true, ak 
     * (1) sú všetky výstupy nárokované {@code tx} v aktuálnom UTXO pool, 
     * (2) podpisy na každom vstupe {@code tx} sú platné, 
     * (3) žiadne UTXO nie je nárokované viackrát, 
     * (4) všetky výstupné hodnoty {@code tx}s sú nezáporné a 
     * (5) súčet vstupných hodnôt {@code tx}s je väčší alebo rovný súčtu jej
     *     výstupných hodnôt; a false inak.
     */
    public boolean txIsValid(Transaction tx) {
        // IMPLEMENTOVAŤ

        // (3) seenUTXO ulozi unikatne UTXO aby sme vedeli skontrolovat doublespend
        HashSet<UTXO> seenUTXO_set = new HashSet<UTXO>();

        // (5) kontrola "zostatku" penazenky
        double inputSum = 0;
        double outputSum = 0;

        for(int i = 0; i < tx.numInputs(); i++){

            // Pre kazdy vstup vytvor tx a ziskaj hodnotu daneho vstupu pre pridanie jeho hash predchádzajúcej hodnoty do UTXO poolu
            Transaction.Input txInput = tx.getInput(i);

            // Pridaj predchadzajucu hodnotu do UTXO pool
            UTXO unspentTransaction = new UTXO(txInput.prevTxHash, txInput.outputIndex);

            // (1) Ak je output bez tx tak vraciame false
            if(this.ledger.contains(unspentTransaction) == false) {
                return false;
            }

            // Predchadzajuci output je pre nas vstupom
            inputSum += ledger.getTxOutput(unspentTransaction).value;

            // (2) Kontrola podpisov na vstupe
            // zober danú unspentTransaction pober jej adresu a skontroluj podpisy v rsa.jar
            if(!ledger.getTxOutput(unspentTransaction).address.verifySignature(tx.getDataToSign(i), txInput.signature)) {
                return false;
            }

            // (3) ak by v tx UTXO tato transakcia uz bola tak sa jedna o doublespend a teda vraciame fasle
            if(seenUTXO_set.contains(unspentTransaction)) {
                return false;
            }

            // pridaj transakciu do mojho poolu lebo zatial je validna
            seenUTXO_set.add(unspentTransaction);

        }

        // (4) pozri vsetky tx outputs a skontroluj ich
        for(Transaction.Output output : tx.getOutputs()) {

            // (4) kontrola ci nieje zaporny output
            if(output.value < 0.0) {
                return false;
            }
            outputSum += output.value;
        }

        // (5) ak by odislo viac ako prislo tak return false
        if(outputSum > inputSum) {
            return false;
        }

        // ak boli vsetky kontorly uspesne transakcia je validna a return true
        return true;
    }

    /**
     * Spracováva každú epochu prijímaním neusporiadaného radu navrhovaných
     * transakcií, kontroluje správnosť každej transakcie, vracia pole vzájomne 
     * platných prijatých transakcií a aktualizuje aktuálny UTXO pool podľa potreby.
     */
    public Transaction[] handler(Transaction[] possibleTxs) {
        // IMPLEMENTOVAŤ
        //return false;

        List<Transaction> acceptedTx = new ArrayList<>();

        for (Transaction tx : possibleTxs) {

            // pre kazdu transakciu zavola hore implementovanú funckiu txIsValid
            if (txIsValid(tx)) {

                // ak bola validna tak sa prida do zoznamu
                acceptedTx.add(tx);

                // a aktualizuje aktuálny UTXO pool podľa potreby.

                // pre danu tx zmaze UTXOs z aktualneho poolu
                for (Transaction.Input input : tx.getInputs()) {

                    UTXO UTXOForRemove = new UTXO(input.prevTxHash, input.outputIndex);
                    this.ledger.removeUTXO(UTXOForRemove);
                }

                // z outputov tx urobi nove UTXOs a pridaju sa do UTXO poolu
                int output_index = 0;
                for (Transaction.Output output : tx.getOutputs()) {

                    UTXO UTXOForAdd = new UTXO(tx.getHash(), output_index);
                    output_index++;

                    this.ledger.addUTXO(UTXOForAdd, output);
                }
            }
        }

        // vytvorenie pola validnych tx
        int size = acceptedTx.size();
        Transaction[] validTxArr = new Transaction[size];
        validTxArr = acceptedTx.toArray(validTxArr);

        // vracia pole vzájomne platných prijatých transakcií
        return validTxArr;
    }
}
