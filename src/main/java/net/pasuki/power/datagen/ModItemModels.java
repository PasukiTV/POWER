package net.pasuki.power.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.pasuki.power.Power;
import net.pasuki.power.Registration;

public class ModItemModels extends ItemModelProvider {

    public ModItemModels(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Power.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        withExistingParent(Registration.GENERATOR_BLOCK.getId().getPath(), modLoc("block/generator_block_off"));
        withExistingParent(Registration.CHARGER_BLOCK.getId().getPath(), modLoc("block/charger_block_on"));
        withExistingParent(Registration.FARMING_BLOCK.getId().getPath(), modLoc("block/farming_block_off"));


        withExistingParent(Registration.CABLE_BLOCK.getId().getPath(), modLoc("block/cable"));
        withExistingParent(Registration.FACADE_BLOCK.getId().getPath(), modLoc("block/facade"));

    }

//    private ItemModelBuilder complexBlock(Block block){
//        return withExistingParent(ForgeRegistries.BLOCKS.getKey(block).getPath(), new ResourceLocation(Power.MODID,
//                "block/" + ForgeRegistries.BLOCKS.getKey(block).getPath()));
//    }
}
