package net.pasuki.power.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.pasuki.power.Registration;
import net.pasuki.power.tools.AdaptedEnergyStorage;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings({"NullableProblems", "DataFlowIssue"})
public class BatteryBlockEntity extends BlockEntity {

    public static final String ENERGY_TAG = "Energy";

    public static final int MAXTRANSFER = 1000;
    public static final int CAPACITY = 100000;

    private int previousEnergy = 0; // Variable, um den vorherigen Energiewert zu speichern

    private final EnergyStorage energy = createEnergyStorage();
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> new AdaptedEnergyStorage(energy) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return super.receiveEnergy(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    });

    public BatteryBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.BATTERY_BLOCK_ENTITY.get(), pos, state);
    }



    public void tickServer() {
        int currentEnergy = energy.getEnergyStored();

        boolean dirty = false; // Flag, um zu überprüfen, ob der Block aktualisiert werden muss

        // Prüfen, ob die aktuelle Energiemenge größer als die vorherige ist
        if (currentEnergy > previousEnergy) {
            // Block ist nur dann powered, wenn die Energiemenge im Block ansteigt
            boolean powered = true;
            if (powered != getBlockState().getValue(BlockStateProperties.POWERED)) {
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(BlockStateProperties.POWERED, powered));
                dirty = true; // Blockzustand wurde geändert, daher setzen wir das dirty-Flag auf true
            }
        } else {
            // Wenn die Energiemenge nicht ansteigt, ist der Block nicht mehr powered
            boolean powered = false;
            if (powered != getBlockState().getValue(BlockStateProperties.POWERED)) {
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(BlockStateProperties.POWERED, powered));
                dirty = true; // Blockzustand wurde geändert, daher setzen wir das dirty-Flag auf true
            }
        }

        if (dirty) {
            setChanged(); // Markiere die Blockentität als geändert, damit die Änderungen gespeichert werden
        }

        previousEnergy = currentEnergy; // Aktualisieren des vorherigen Energiestatus für den nächsten Tick
    }

    private void distributeEnergy() {
        if (energy.getEnergyStored() <= 0) {
            return; // Keine Energie zum Verteilen vorhanden, daher abbrechen
        }
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = getBlockPos().relative(direction);
            BlockEntity neighborEntity = level.getBlockEntity(neighborPos);
            if (neighborEntity != null) {
                neighborEntity.getCapability(ForgeCapabilities.ENERGY).ifPresent(neighborEnergy -> {
                    if (neighborEnergy.canReceive()) {
                        int received = neighborEnergy.receiveEnergy(Math.min(energy.getEnergyStored(), MAXTRANSFER), false);
                        energy.extractEnergy(received, false);
                        setChanged();
                    }
                });
            }
        }
    }


    public int getStoredPower() {
        return energy.getEnergyStored();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(ENERGY_TAG, energy.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(ENERGY_TAG)) {
            energy.deserializeNBT(tag.get(ENERGY_TAG));
        }
    }

    @Nonnull
    private EnergyStorage createEnergyStorage() {
        return new EnergyStorage(CAPACITY, MAXTRANSFER, MAXTRANSFER);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
      if (cap == ForgeCapabilities.ENERGY) {
            return energyHandler.cast();
        } else {
            return super.getCapability(cap, side);
        }
    }
}
