package dev.gigaherz.toolbelt;

import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import dev.gigaherz.toolbelt.integration.CosmeticArmorIntegration;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Optional;

public class BeltFinderCurios extends BeltFinder
{
    @CapabilityInject(ICuriosItemHandler.class)
    public static void initBaubles(Capability<?> cap)
    {
        BeltFinder.addFinder(new BeltFinderCurios());
    }

    @Override
    public String getName()
    {
        return "curios";
    }

    @Override
    public void setToSlot(LivingEntity player, int slot, ItemStack stack)
    {
        player.getCapability(CuriosCapability.INVENTORY).ifPresent((curios) ->
                curios.getStacksHandler("belt").ifPresent(handler -> handler.getStacks().setStackInSlot(slot, stack)));
    }

    @Override
    public Optional<? extends BeltGetter> findStack(PlayerEntity player)
    {
        return player.getCapability(CuriosCapability.INVENTORY)
                .resolve()
                .flatMap(curios -> curios.getStacksHandler("belt"))
                .flatMap(handler -> {
                    IDynamicStackHandler stacks = handler.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++)
                    {
                        ItemStack inSlot = stacks.getStackInSlot(i);
                        if (inSlot.getCount() > 0)
                        {
                            if (inSlot.getItem() instanceof ToolBeltItem)
                            {
                                return Optional.of(new CuriosBeltGetter(player, i));
                            }
                        }
                    }
                    return Optional.<BeltGetter>empty();
                });
    }

    private static class CuriosBeltGetter implements BeltGetter
    {
        private final PlayerEntity thePlayer;
        private final int slotNumber;

        private CuriosBeltGetter(PlayerEntity thePlayer, int slotNumber)
        {
            this.thePlayer = thePlayer;
            this.slotNumber = slotNumber;
        }

        @Override
        public ItemStack getBelt()
        {
            return thePlayer.getCapability(CuriosCapability.INVENTORY).map((curios) ->
                    curios.getStacksHandler("belt").map(handler ->
                            handler.getStacks().getStackInSlot(slotNumber)).orElse(ItemStack.EMPTY)
            ).orElse(ItemStack.EMPTY);
        }

        @Override
        public boolean isHidden()
        {
            return !thePlayer.getCapability(CuriosCapability.INVENTORY).resolve()
                    .flatMap((curios) -> curios.getStacksHandler("belt"))
                    .map(handler -> handler.isVisible() && handler.getRenders().get(slotNumber))
                    .orElse(true);
        }

        @Override
        public void syncToClients()
        {
            // No need! Curios does its own sync. I think.
        }
    }
}