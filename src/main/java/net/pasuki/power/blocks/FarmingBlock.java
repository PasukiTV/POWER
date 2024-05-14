package net.pasuki.power.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"deprecation", "NullableProblems"})
public class FarmingBlock extends Block implements EntityBlock {

    public static final String SCREEN_FARM = "screen.farm";

    public FarmingBlock() {
        super(Properties.of()
                .strength(3.5F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL));
    }

    /**
     * Erstellt eine neue BlockEntity für diesen Block.
     *
     * @param blockPos die Position des Blocks.
     * @param blockState der Zustand des Blocks.
     * @return eine neue Instanz von FarmingBlockEntity.
     */
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new FarmingBlockEntity(blockPos, blockState);
    }

    /**
     * Gibt den serverseitigen Ticker für die BlockEntity zurück.
     *
     * @param level das Level, in dem der Block platziert ist.
     * @param state der Zustand des Blocks.
     * @param type der Typ der BlockEntity.
     * @return einen Ticker für die BlockEntity.
     */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null; // Kein Ticker auf der Client-Seite
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof FarmingBlockEntity farming) {
                farming.tickServer(); // Serverseitige Tick-Methode aufrufen
            }
        };
    }

    /**
     * Behandelt die Interaktion mit dem Block.
     *
     * @param state der Zustand des Blocks.
     * @param level das Level, in dem der Block platziert ist.
     * @param pos die Position des Blocks.
     * @param player der Spieler, der mit dem Block interagiert.
     * @param hand die Hand, die der Spieler benutzt.
     * @param trace das Blocktreffer-Ergebnis.
     * @return das Ergebnis der Interaktion.
     */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult trace) {
        // Überprüfen, ob wir serverseitig sind und ob die Haupt-Hand benutzt wird und kein Eisenschwert gehalten wird
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND && !(player.getItemInHand(hand).getItem() == Items.IRON_SWORD)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FarmingBlockEntity) {
                // Menü-Anbieter erstellen, um das GUI zu öffnen
                MenuProvider containerProvider = new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return Component.translatable(SCREEN_FARM);
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
                        return new FarmContainer(windowId, playerEntity, pos);
                    }
                };
                // Öffnen des Bildschirms für den Server-Spieler
                NetworkHooks.openScreen((ServerPlayer) player, containerProvider, be.getBlockPos());
            } else {
                throw new IllegalStateException("Unser benannter Container-Anbieter fehlt!"); // Fehler, wenn der Menü-Anbieter fehlt
            }
        }
        return InteractionResult.SUCCESS; // Erfolg beim Interaktionsversuch
    }

    /**
     * Setzt den anfänglichen Blockzustand bei der Platzierung.
     *
     * @param context der Blockplatzierungs-Kontext.
     * @return der anfängliche Blockzustand.
     */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(BlockStateProperties.FACING, context.getHorizontalDirection().getOpposite()) // Setzt die Ausrichtung des Blocks
                .setValue(BlockStateProperties.POWERED, false); // Initialisierung mit nicht aktiviertem Zustand
    }

    /**
     * Definiert die Blockzustand-Eigenschaften.
     *
     * @param builder der Zustandsdefinitions-Builder.
     */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.POWERED, BlockStateProperties.FACING); // Hinzufügen der POWERED- und FACING-Eigenschaften
    }
}
