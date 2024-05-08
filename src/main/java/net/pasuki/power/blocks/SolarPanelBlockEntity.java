package net.pasuki.power.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.pasuki.power.Registration;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class SolarPanelBlockEntity extends BlockEntity {

    public static final String ENERGY_TAG = "Energy";
    public static final int GENERATE = 10; // Energie pro Sekunde

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.SOLAR_PANEL_BLOCK_ENTITY.get(), pos, state);
    }

    public void tickServer() {
        if (level != null && !level.isClientSide && level.isDay() && !level.isRaining() && level.getBrightness(LightLayer.SKY, getBlockPos().above()) == 15) {
            distributeEnergy();
        }
    }

    private void distributeEnergy() {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = getBlockPos().relative(direction);
            BlockEntity be = level.getBlockEntity(neighborPos);
            if (be != null) {
                be.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(e -> {
                    int energySent = e.receiveEnergy(GENERATE, false);
                    if (energySent > 0) {
                        setChanged();
                    }
                });
            }
        }
    }

    @Override
    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return LazyOptional.of(this::getEnergyStorage).cast();
        } else {
            return super.getCapability(cap, side);
        }
    }

    private IEnergyStorage getEnergyStorage() {
        return new IEnergyStorage() {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                // Solarzellen speichern keine Energie, daher wird immer 0 zurückgegeben
                return 0;
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                // Die Solarzellen generieren Energie, daher wird immer GENERATE_PER_SECOND zurückgegeben
                return GENERATE;
            }

            @Override
            public int getEnergyStored() {
                // Die Solarzellen speichern keine Energie, daher ist immer 0 gespeichert
                return 0;
            }

            @Override
            public int getMaxEnergyStored() {
                // Die Solarzellen speichern keine Energie, daher ist die maximale Speicherkapazität immer 0
                return 0;
            }

            @Override
            public boolean canExtract() {
                // Die Solarzellen speichern keine Energie, daher kann keine Energie extrahiert werden
                return false;
            }

            @Override
            public boolean canReceive() {
                // Die Solarzellen speichern keine Energie, daher kann keine Energie empfangen werden
                return false;
            }
        };
    }
}


