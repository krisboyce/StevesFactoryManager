package vswe.stevesfactory.logic.item;

import com.google.common.base.Preconditions;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.Ingredient;
import vswe.stevesfactory.api.item.IItemBufferElement;
import vswe.stevesfactory.api.item.ItemBuffers;
import vswe.stevesfactory.api.logic.IExecutionContext;
import vswe.stevesfactory.utils.NetworkHelper;

import java.util.IdentityHashMap;
import java.util.Map;

public class CraftingBufferElement implements IItemBufferElement {

    private static final Map<ICraftingRecipe, Map<Item, ItemStack>> ingredientsCache = new IdentityHashMap<>();

    private static Map<Item, ItemStack> getMatchingStacks(ICraftingRecipe recipe) {
        Map<Item, ItemStack> result = ingredientsCache.get(recipe);
        if (result != null) {
            return result;
        }

        Map<Item, ItemStack> cache = new IdentityHashMap<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            for (ItemStack stack : ingredient.getMatchingStacks()) {
                ItemStack existing = cache.get(stack.getItem());
                if (existing == null) {
                    cache.put(stack.getItem(), stack.copy());
                } else {
                    existing.grow(stack.getCount());
                }
            }
        }
        ingredientsCache.put(recipe, cache);
        return cache;
    }

    private final IExecutionContext context;

    private ICraftingRecipe recipe;
    private int outputBase = -1;
    private ItemStack result = ItemStack.EMPTY;

    public CraftingBufferElement(IExecutionContext context) {
        this.context = context;
    }

    public ICraftingRecipe getRecipe() {
        return recipe;
    }

    public void setRecipe(ICraftingRecipe recipe) {
        this.recipe = recipe;
        // TODO crafting inventory
        result = recipe.getCraftingResult(null);
        outputBase = result.getCount();
    }

    // TODO optimize (reduce the number of refresh() calls)
    @Override
    public ItemStack getStack() {
        refresh();
        return result;
    }

    @Override
    public void setStack(ItemStack stack) {
        Preconditions.checkArgument(result.isItemEqual(stack));
        this.result = stack;
        refresh();
    }

    @Override
    public int getUsed() {
        return 0;
    }

    @Override
    public void setUsed(int used) {
        throw new UnsupportedOperationException();
    }

    public void refresh() {
        Map<Item, ItemBuffers> buffers = context.getItemBuffers();
        int maxAvailable = 0;
        for (Map.Entry<Item, ItemStack> entry : getMatchingStacks(recipe).entrySet()) {
            ItemStack matchable = entry.getValue();
            DirectBufferElement buffer = NetworkHelper.getDirectBuffer(buffers, matchable.getItem());
            if (buffer == null) {
                continue;
            }
            int found = buffer.stack.getCount() / matchable.getCount();
            maxAvailable = Math.max(maxAvailable, found);
        }
        result.setCount(outputBase * maxAvailable);
    }

    @Override
    public void use(int amount) {
        refresh();
        Map<Item, ItemBuffers> buffers = context.getItemBuffers();
        for (Map.Entry<Item, ItemStack> entry : getMatchingStacks(recipe).entrySet()) {
            ItemStack matchable = entry.getValue();
            DirectBufferElement buffer = NetworkHelper.getDirectBuffer(buffers, matchable.getItem());
            if (buffer == null) {
                continue;
            }
            buffer.use(matchable.getCount());
        }
    }

    @Override
    public void put(int amount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanup() {
    }
}
