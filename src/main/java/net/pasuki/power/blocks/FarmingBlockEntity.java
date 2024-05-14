package net.pasuki.power.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.pasuki.power.Registration;
import net.pasuki.power.tools.AdaptedEnergyStorage;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;

@SuppressWarnings({"NullableProblems", "DataFlowIssue"})
public class FarmingBlockEntity extends BlockEntity {

    public static final String ENERGY_TAG = "Energy";
    public static final String ITEMS_TAG = "Inventory";
    public static final int MAXRECEIVE = 1000;
    public static final int CAPACITY = 10000;
    public static final int ENERGY_CONSUMPTION_PLANT = 50;
    public static final int ENERGY_CONSUMPTION_HARVEST = 100;

    public static final int SLOT_COUNT = 1;
    public static final int SLOT = 0;

    private final ItemStackHandler items = createItemHandler();
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);

    private final EnergyStorage energy = createEnergyStorage();
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> new AdaptedEnergyStorage(energy) {
        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            setChanged();
            return super.receiveEnergy(maxReceive, simulate);
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

    public FarmingBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.FARMING_BLOCK_ENTITY.get(), pos, state);
    }

    public void tickServer() {
        boolean powered = energy.getEnergyStored() > 0;
        if (powered != getBlockState().getValue(BlockStateProperties.POWERED)) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(BlockStateProperties.POWERED, powered));
        }

        if (powered) {
            performFarmingOperations();
        }
    }

    private void performFarmingOperations() {
        logEnergy("Start of Farming Operations", energy.getEnergyStored());

        BlockPos pos = getBlockPos();
        List<BlockPos> positions = BlockPos.betweenClosedStream(pos.offset(-3, -1, -3), pos.offset(3, 1, 3)).map(BlockPos::immutable).toList();

        for (BlockPos targetPos : positions) {
            BlockState state = level.getBlockState(targetPos);
            if (state.getBlock() instanceof FarmBlock) {
                BlockPos abovePos = targetPos.above();
                BlockState aboveState = level.getBlockState(abovePos);

                // Planting Logic
                if (aboveState.isAir() && hasSeeds()) {
                    int energyToPlant = ENERGY_CONSUMPTION_PLANT;
                    if (energy.extractEnergy(energyToPlant, true) >= energyToPlant) {
                        logEnergy("Before Planting", energy.getEnergyStored());
                        level.setBlockAndUpdate(abovePos, Blocks.WHEAT.defaultBlockState());
                        energy.extractEnergy(energyToPlant, false); // Deduct energy
                        consumeSeed();
                        logEnergy("After Planting", energy.getEnergyStored());
                    }
                }

                // Harvesting Logic
                else if (aboveState.getBlock() instanceof CropBlock cropBlock && cropBlock.isMaxAge(aboveState)) {
                    int energyToHarvest = ENERGY_CONSUMPTION_HARVEST;
                    if (energy.extractEnergy(energyToHarvest, true) >= energyToHarvest) {
                        logEnergy("Before Harvesting", energy.getEnergyStored());
                        ItemStack wheat = new ItemStack(Items.WHEAT);
                        ItemStack seeds = new ItemStack(Items.WHEAT_SEEDS);
                        boolean wheatAdded = addItemToInventory(wheat);
                        boolean seedsAdded = addItemToInventory(seeds);

                        if (!wheatAdded) {
                            cropBlock.popResource(level, abovePos, wheat);
                        }
                        if (!seedsAdded) {
                            cropBlock.popResource(level, abovePos, seeds);
                        }

                        if (wheatAdded || !wheat.isEmpty() || seedsAdded || !seeds.isEmpty()) {
                            level.setBlockAndUpdate(abovePos, Blocks.AIR.defaultBlockState());
                        }

                        energy.extractEnergy(energyToHarvest, false); // Deduct energy
                        logEnergy("After Harvesting", energy.getEnergyStored());
                    }
                }
            }
        }

        logEnergy("End of Farming Operations", energy.getEnergyStored());
    }

    private void logEnergy(String message, int currentEnergy) {
        System.out.println(message + ": " + currentEnergy);
    }

    private boolean addItemToInventory(ItemStack itemStack) {
        ItemStack remaining = ItemHandlerHelper.insertItem(items, itemStack, false);
        return remaining.isEmpty();
    }


    private boolean hasSeeds() {
        ItemStack seedStack = items.getStackInSlot(SLOT);
        return !seedStack.isEmpty() && seedStack.getItem() == Items.WHEAT_SEEDS;
    }

    private void consumeSeed() {
        ItemStack stack = items.getStackInSlot(SLOT);
        if (!stack.isEmpty()) {
            stack.shrink(1);
        }
        if (stack.isEmpty()) {
            items.setStackInSlot(SLOT, ItemStack.EMPTY);
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(ITEMS_TAG, items.serializeNBT());
        tag.put(ENERGY_TAG, energy.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(ITEMS_TAG)) items.deserializeNBT(tag.getCompound(ITEMS_TAG));
        if (tag.contains(ENERGY_TAG)) energy.deserializeNBT(tag.get(ENERGY_TAG));
    }

    @Nonnull
    private ItemStackHandler createItemHandler() {
        return new ItemStackHandler(SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    @Nonnull
    private EnergyStorage createEnergyStorage() {
        return new EnergyStorage(CAPACITY, MAXRECEIVE);
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

    public int getStoredPower() {
        return energy.getEnergyStored();
    }
}
