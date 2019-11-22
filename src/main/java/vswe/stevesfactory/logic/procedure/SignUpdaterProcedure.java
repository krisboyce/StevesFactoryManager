package vswe.stevesfactory.logic.procedure;

import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import vswe.stevesfactory.api.capability.CapabilityTextDisplay;
import vswe.stevesfactory.api.capability.ITextDisplay;
import vswe.stevesfactory.api.logic.IExecutionContext;
import vswe.stevesfactory.api.network.IConnectable;
import vswe.stevesfactory.logic.AbstractProcedure;
import vswe.stevesfactory.logic.Procedures;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.menu.InventorySelectionMenu;
import vswe.stevesfactory.ui.manager.menu.SignUpdaterLinesMenu;
import vswe.stevesfactory.utils.IOHelper;
import vswe.stevesfactory.utils.NetworkHelper;

import java.util.ArrayList;
import java.util.List;

public class SignUpdaterProcedure extends AbstractProcedure implements IInventoryTarget {

    public static final int SIGNS = 0;

    private List<BlockPos> signs = new ArrayList<>();
    private String[] texts = new String[4];

    private List<LazyOptional<ITextDisplay>> cachedCaps = new ArrayList<>();
    private boolean dirty = false;

    public SignUpdaterProcedure() {
        super(Procedures.SIGN_UPDATER.getFactory());
    }

    @Override
    public void execute(IExecutionContext context) {
        pushFrame(context, 0);
        updateCaches(context);
        for (LazyOptional<ITextDisplay> cap : cachedCaps) {
            cap.ifPresent(display -> {
                for (int i = 0; i < texts.length; i++) {
                    display.setLine(i, new StringTextComponent(texts[i]));
                }
            });
        }
    }

    private void updateCaches(IExecutionContext context) {
        if (!dirty) {
            return;
        }

        cachedCaps.clear();
        NetworkHelper.cacheCaps(context, cachedCaps, signs, CapabilityTextDisplay.TEXT_DISPLAY_CAPABILITY);
        dirty = false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public FlowComponent<SignUpdaterProcedure> createFlowComponent() {
        FlowComponent<SignUpdaterProcedure> f = FlowComponent.of(this);
        f.addMenu(new InventorySelectionMenu<>(SIGNS, I18n.format("gui.sfm.Menu.SignUpdater.Signs"), I18n.format("error.sfm.SignUpdater.NoTargets"), IConnectable.UNKNOWN_CONNECTION_CAPABILITY));
        f.addMenu(new SignUpdaterLinesMenu());
        return f;
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = super.serialize();
        tag.put("Texts", IOHelper.writeStrings(texts));
        return tag;
    }

    @Override
    public void deserialize(CompoundNBT tag) {
        super.deserialize(tag);
        IOHelper.readStrings(tag.getList("Texts", Constants.NBT.TAG_STRING), texts);
        markDirty();
    }

    @Override
    public List<BlockPos> getInventories(int id) {
        return signs;
    }

    @Override
    public void markDirty() {
        dirty = true;
    }
}
