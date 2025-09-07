package api.inventory;

import java.util.Map;

public class InventoryFilter {

    private Map<ItemStack, int[]> blocks;

    public InventoryFilter() {
        this.blocks = null;
    }

    public void addBlock(ItemStack block, int amount) {
        blocks.put(block, new int[] { amount, 0 });
    }

    public void addBlock(ItemStack block, int amount, int upTo) {
        blocks.put(block, new int[] { amount, upTo });
    }

    public void removeBlock(ItemStack block) {
        blocks.remove(block);
    }

    public Map<ItemStack, int[]> getBlocks() {
        return blocks;
    }
}
