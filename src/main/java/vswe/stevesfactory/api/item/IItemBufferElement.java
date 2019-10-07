package vswe.stevesfactory.api.item;

import net.minecraft.item.ItemStack;

public interface IItemBufferElement {

    ItemStack getStack();

    void setStack(ItemStack stack);

    int getUsed();

    void setUsed(int used);

    /**
     * Implementation may assume the parameter is always less than or equal to the stack size of {@link #getStack()}.
     */
    void use(int amount);

    void put(int amount);

    void cleanup();
}
