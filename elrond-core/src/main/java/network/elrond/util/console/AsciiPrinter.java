package network.elrond.util.console;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import network.elrond.account.AccountState;
import network.elrond.account.Accounts;
import network.elrond.application.AppState;
import network.elrond.core.Util;
import network.elrond.data.Block;
import network.elrond.data.Transaction;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;

public class AsciiPrinter {

	private static final AsciiPrinter INSTANCE = new AsciiPrinter();

	public static AsciiPrinter instance() {
		return INSTANCE;
	}

	public AsciiTable accountStateAsciiTable(AccountState accountState) {

		AsciiTable table = new AsciiTable();
		table.setMaxColumnWidth(90);

		table.getColumns().add(new AsciiTable.Column("Account "));
		table.getColumns().add(new AsciiTable.Column(Util.byteArrayToHexString(accountState.getAddress().getBytes())));

		AsciiTable.Row row0 = new AsciiTable.Row();
		row0.getValues().add("Nonce");
		row0.getValues().add(accountState.getNonce().toString());
		table.getData().add(row0);

		AsciiTable.Row row1 = new AsciiTable.Row();
		row1.getValues().add("Balance");
		row1.getValues().add(accountState.getBalance() + "");
		table.getData().add(row1);

		table.calculateColumnWidth();
		return table;
	}

	public AsciiTable appStateAsciiTable(AppState appState) {

		AsciiTable table = new AsciiTable();
		table.setMaxColumnWidth(90);

		table.getColumns().add(new AsciiTable.Column("AppState "));
		table.getColumns().add(new AsciiTable.Column(Util.byteArrayToHexString(appState.getPublicKey().getValue())));

		AsciiTable.Row row0 = new AsciiTable.Row();
		row0.getValues().add("Shard");
		row0.getValues().add(appState.getShard().getIndex().toString());
		table.getData().add(row0);

		AsciiTable.Row row1 = new AsciiTable.Row();
		row1.getValues().add("Pool");
		row1.getValues().add(String.valueOf(appState.getPool().getTransactions().size()));
		table.getData().add(row1);

		table.calculateColumnWidth();
		return table;
	}

	public AsciiTable blockAsciiTable(Block block) {

		AsciiTable table = new AsciiTable();
		table.setMaxColumnWidth(200);

		table.getColumns().add(new AsciiTable.Column("Block "));
		table.getColumns().add(new AsciiTable.Column(block.getNonce() + ""));

		AsciiTable.Row rowS = new AsciiTable.Row();
		rowS.getValues().add("Shard");
		rowS.getValues().add(block.getShard().getIndex() + "");
		table.getData().add(rowS);

		AsciiTable.Row row0 = new AsciiTable.Row();
		row0.getValues().add("Nonce");
		row0.getValues().add(block.getNonce().toString());
		table.getData().add(row0);

		AsciiTable.Row row1 = new AsciiTable.Row();
		row1.getValues().add("State Hash");
		row1.getValues().add(Util.getDataEncoded64(block.getAppStateHash()));
		table.getData().add(row1);

		AsciiTable.Row row2 = new AsciiTable.Row();
		row2.getValues().add("Signature");
		row2.getValues().add(Util.getDataEncoded64(block.getSignature()));
		table.getData().add(row2);

		AsciiTable.Row row3 = new AsciiTable.Row();
		row3.getValues().add("Commitment");
		row3.getValues().add(Util.getDataEncoded64(block.getCommitment()));
		table.getData().add(row3);

		AsciiTable.Row row4 = new AsciiTable.Row();
		row4.getValues().add("Prev block");
		row4.getValues().add(Util.getDataEncoded64(block.getPrevBlockHash()));
		table.getData().add(row4);

		AsciiTable.Row row5 = new AsciiTable.Row();
		row5.getValues().add("Transactions in block");
		row5.getValues().add(block.getListTXHashes().size() + "");
		table.getData().add(row5);

		AsciiTable.Row row6 = new AsciiTable.Row();
		row6.getValues().add("----------------------");
		row6.getValues().add("----------------------------------------------------------------");
		table.getData().add(row6);

		for (int index = 0; index < block.getListTXHashes().size(); index++) {
			byte[] tx = block.getListTXHashes().get(index);
			AsciiTable.Row row7 = new AsciiTable.Row();
			row7.getValues().add("#" + index);
			row7.getValues().add(Util.getDataEncoded64(tx));
			table.getData().add(row7);
		}

		AsciiTable.Row row8 = new AsciiTable.Row();
		row8.getValues().add("----------------------");
		row8.getValues().add("----------------------------------------------------------------");
		table.getData().add(row8);

		AsciiTable.Row row9 = new AsciiTable.Row();
		row9.getValues().add("Peers in block");
		row9.getValues().add(block.getPeers().size() + "");
		table.getData().add(row9);

		AsciiTable.Row row10 = new AsciiTable.Row();
		row10.getValues().add("----------------------");
		row10.getValues().add("----------------------------------------------------------------");
		table.getData().add(row10);

		int index = 0;

		for (String node : block.getPeers()) {
			index++;
			AsciiTable.Row row11 = new AsciiTable.Row();
			row11.getValues().add("#" + index);
			row11.getValues().add(node);
			table.getData().add(row11);
		}

		table.calculateColumnWidth();
		return table;
	}
	
    public AsciiTable shardAsciiTable(Shard shard) {

        AsciiTable table = new AsciiTable();
        table.setMaxColumnWidth(30);

        table.getColumns().add(new AsciiTable.Column("Shard"));

        AsciiTable.Row row1 = new AsciiTable.Row();
        row1.getValues().add("Index");
        row1.getValues().add(shard.getIndex() + "");
        table.getData().add(row1);

        table.calculateColumnWidth();
        return table;
    }
    
    public AsciiTable transactionAsciiTable(Transaction transaction) {
        AsciiTable table = new AsciiTable();
        table.setMaxColumnWidth(30);

        table.getColumns().add(new AsciiTable.Column("Transaction"));
        table.getColumns().add(new AsciiTable.Column(transaction.getNonce() + ""));

        AsciiTable.Row row0 = new AsciiTable.Row();
        row0.getValues().add("Value");
        row0.getValues().add(transaction.getValue().toString());

        AsciiTable.Row row1 = new AsciiTable.Row();
        row1.getValues().add("From");
        row1.getValues().add(transaction.getSenderAddress());
        table.getData().add(row1);

        AsciiTable.Row row2 = new AsciiTable.Row();
        row2.getValues().add("To");
        row2.getValues().add(transaction.getReceiverAddress());
        table.getData().add(row2);

        AsciiTable.Row row3 = new AsciiTable.Row();
        row3.getValues().add("From Shard");
        row3.getValues().add(transaction.getSenderShard().getIndex() + "");
        table.getData().add(row3);

        AsciiTable.Row row4 = new AsciiTable.Row();
        row4.getValues().add("To Shard");
        row4.getValues().add(transaction.getReceiverShard().getIndex() + "");
        table.getData().add(row4);

        AsciiTable.Row row5 = new AsciiTable.Row();
        row5.getValues().add("Nonce");
        row5.getValues().add(transaction.getNonce().toString());
        table.getData().add(row5);

        AsciiTable.Row row6 = new AsciiTable.Row();
        row6.getValues().add("Value");
        row6.getValues().add(transaction.getValue().toString());
        table.getData().add(row6);


        table.calculateColumnWidth();
        return table;
    }
    
    public String printAccounts(Accounts accounts) {
    	List<AccountState> accountStates = accounts.getAddresses()
                .stream()
                .map(accountAddress -> {
                    try {
                        return AppServiceProvider.getAccountStateService().getAccountState(accountAddress, accounts);
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    	
    	StringBuilder builder = new StringBuilder();
        for (AccountState accountState : accountStates) {
            builder.append(accountStateAsciiTable(accountState).render());
        }
        return builder.toString();
    }

}
