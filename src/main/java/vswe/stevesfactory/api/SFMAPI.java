package vswe.stevesfactory.api;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.*;
import vswe.stevesfactory.StevesFactoryManager;
import vswe.stevesfactory.api.logic.IProcedureFactory;

@EventBusSubscriber(modid = StevesFactoryManager.MODID, bus = Bus.MOD)
public class SFMAPI {

    private static IForgeRegistry<IProcedureFactory<?>> procedures;

    public static IForgeRegistry<IProcedureFactory<?>> getProceduresRegistry() {
        if (procedures == null) {
            procedures = RegistryManager.ACTIVE.getRegistry(IProcedureFactory.class);
        }
        return procedures;
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void onRegistryCreation(RegistryEvent.NewRegistry event) {
        makeRegistry(new ResourceLocation(StevesFactoryManager.MODID, "procedures"), IProcedureFactory.class).create();
    }

    // Somehow inlining this method would create an compile error
    private static <T extends IForgeRegistryEntry<T>> RegistryBuilder<T> makeRegistry(ResourceLocation name, Class<T> type) {
        return new RegistryBuilder<T>().setName(name).setType(type);
    }
}
