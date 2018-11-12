package network.elrond.data.model;

import java.util.List;

public class BlockReceipts {

	private final Block block;
	private final List<Receipt> receipts;

	public BlockReceipts(Block block, List<Receipt> receipts) {
		this.block = block;
		this.receipts = receipts;
	}

	public Block getBlock() {
		return block;
	}

	public List<Receipt> getReceipts() {
		return receipts;
	}

}
