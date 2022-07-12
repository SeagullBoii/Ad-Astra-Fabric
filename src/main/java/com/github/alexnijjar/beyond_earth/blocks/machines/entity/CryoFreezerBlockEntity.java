package com.github.alexnijjar.beyond_earth.blocks.machines.entity;

import javax.annotation.Nullable;

import com.github.alexnijjar.beyond_earth.BeyondEarth;
import com.github.alexnijjar.beyond_earth.gui.screen_handlers.CryoFreezerScreenHandler;
import com.github.alexnijjar.beyond_earth.recipes.CryoFuelConversionRecipe;
import com.github.alexnijjar.beyond_earth.recipes.ModRecipeType;
import com.github.alexnijjar.beyond_earth.registry.ModBlockEntities;
import com.github.alexnijjar.beyond_earth.registry.ModRecipes;
import com.github.alexnijjar.beyond_earth.util.FluidUtils;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;

public class CryoFreezerBlockEntity extends FluidMachineBlockEntity {

    public static final long MAX_ENERGY = BeyondEarth.CONFIG.cryoFreezer.maxEnergy;
    public static final long ENERGY_PER_TICK = BeyondEarth.CONFIG.cryoFreezer.energyPerTick;
    public static final int TANK_SIZE = BeyondEarth.CONFIG.cryoFreezer.tankBuckets;

    protected short cookTime;
    protected short cookTimeTotal;

    @Nullable
    protected Item inputItem;
    @Nullable
    protected Fluid outputFluid;

    public CryoFreezerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.CRYO_FREEZER, blockPos, blockState);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.cookTime = nbt.getShort("CookTime");
        this.cookTimeTotal = nbt.getShort("CookTimeTotal");
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putShort("CookTime", this.cookTime);
        nbt.putShort("CookTimeTotal", this.cookTimeTotal);
    }

    public short getCookTime() {
        return this.cookTime;
    }

    public short getCookTimeTotal() {
        return this.cookTimeTotal;
    }

    public void finishCooking() {
        if (this.outputFluid != null) {
            CryoFuelConversionRecipe recipe = this.createRecipe(ModRecipes.CRYO_FUEL_CONVERSION_RECIPE, this.getStack(0), false);
            if (recipe != null) {
                try (Transaction transaction = Transaction.openOuter()) {
                    if (this.outputTank.insert(FluidVariant.of(recipe.getFluidOutput()), FluidUtils.millibucketsToDroplets((long) (1000 * recipe.getConversionRatio())), transaction) > 0) {
                        transaction.commit();
                    }
                }
            }
        }
        this.stopCooking();
    }

    public void stopCooking() {
        this.cookTime = 0;
        this.cookTimeTotal = 0;
        this.outputFluid = null;
        this.inputItem = null;
        this.markDirty();
    }

    @Override
    public long getInputSize() {
        return 0;
    }

    @Override
    public long getOutputSize() {
        return TANK_SIZE;
    }

    @Override
    public boolean usesEnergy() {
        return true;
    }

    @Override
    public long getMaxGeneration() {
        return MAX_ENERGY;
    }

    @Override
    public long getEnergyPerTick() {
        return ENERGY_PER_TICK;
    }

    @Override
    public long getMaxEnergyInsert() {
        return ENERGY_PER_TICK * 32;
    }

    @Override
    public int getInventorySize() {
        return 3;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new CryoFreezerScreenHandler(syncId, inv, this);
    }

    public CryoFuelConversionRecipe createRecipe(ModRecipeType<CryoFuelConversionRecipe> type, ItemStack testStack, boolean checkOutput) {
        stopCooking();

        CryoFuelConversionRecipe recipe = type.findFirst(this.world, f -> f.test(testStack));

        if (recipe != null) {

            // Stop if something is already in the output.
            if (checkOutput) {
                ItemStack outputSlot = this.getStack(1);
                ItemStack output = recipe.getOutput();
                if (!outputSlot.isEmpty() && !outputSlot.getItem().equals(recipe.getOutput().getItem()) || outputSlot.getCount() + output.getCount() > outputSlot.getMaxCount()) {
                    return null;
                }
            }

            this.outputFluid = recipe.getFluidOutput();
            this.inputItem = testStack.getItem();
        }

        return recipe;
    }

    @Override
    public void tick() {
        if (!this.world.isClient) {

            ItemStack input = this.getStack(0);
            ItemStack outputInsertSlot = this.getStack(1);
            ItemStack outputExtractSlot = this.getStack(2);

            if (!outputInsertSlot.isEmpty() && outputExtractSlot.getCount() < outputExtractSlot.getMaxCount()) {
                FluidUtils.extractFluidFromTank(this, this.outputTank, 1, 2);
            }

            if (this.hasEnergy()) {
                if ((!input.isEmpty() && (input.getItem().equals(this.inputItem) || this.inputItem == null)) && this.outputTank.getAmount() < this.outputTank.getCapacity()) {
                    this.setActive(true);
                    if (this.cookTime < this.cookTimeTotal) {
                        this.cookTime++;
                        this.drainEnergy();

                    } else if (this.outputFluid != null) {
                        input.decrement(1);
                        this.finishCooking();

                    } else {
                        CryoFuelConversionRecipe recipe = this.createRecipe(ModRecipes.CRYO_FUEL_CONVERSION_RECIPE, input, false);
                        if (recipe != null) {
                            this.cookTimeTotal = 25;
                            this.cookTime = 0;
                        }
                    }
                } else if (this.outputFluid != null) {
                    this.stopCooking();
                } else {
                    this.setActive(false);
                }
            }
        }
    }
}