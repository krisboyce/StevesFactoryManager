package vswe.stevesfactory.logic.procedure;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vswe.stevesfactory.api.logic.IExecutionContext;
import vswe.stevesfactory.logic.AbstractProcedure;
import vswe.stevesfactory.logic.Procedures;
import vswe.stevesfactory.logic.execution.ITickable;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.menu.IntervalMenu;

public class IntervalTriggerProcedure extends AbstractProcedure implements ITickable {

    private int tickCounter = 0;
    public int interval = 20;

    public IntervalTriggerProcedure() {
        super(Procedures.INTERVAL_TRIGGER.getFactory(), 0, 1);
    }

    @Override
    public void execute(IExecutionContext context) {
        pushFrame(context, 0);
    }

    @Override
    public void tick() {
        if (tickCounter >= interval) {
            getGraph().execute();
            tickCounter = 0;
        } else {
            tickCounter++;
        }
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = super.serialize();
        tag.putInt("Interval", interval);
        return tag;
    }

    @Override
    public void deserialize(CompoundNBT tag) {
        super.deserialize(tag);
        interval = tag.getInt("Interval");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public FlowComponent<IntervalTriggerProcedure> createFlowComponent() {
        FlowComponent<IntervalTriggerProcedure> f = FlowComponent.of(this, 0, 1);
        f.addMenu(new IntervalMenu());
        return f;
    }
}