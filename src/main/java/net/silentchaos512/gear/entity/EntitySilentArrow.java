package net.silentchaos512.gear.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.silentchaos512.gear.item.gear.CoreArrow;

/**
 * Minimal arrow entity for modular arrows: vanilla {@code EntityArrow} keeps its damage field
 * private, so a subclass is needed to add material damage and return the modular arrow on pickup.
 * <p>Material damage/speed/accuracy are read once from the arrow item's NBT; no per-tick NBT lookups.</p>
 */
public class EntitySilentArrow extends EntityArrow {
    private static final String NBT_BASE_DAMAGE = "SilentBaseDamage";
    private static final String NBT_MATERIAL_DAMAGE = "SilentMaterialDamage";
    private static final String NBT_SPEED = "SilentSpeed";
    private static final String NBT_ACCURACY = "SilentAccuracy";
    private static final String NBT_ARROW_ITEM = "SilentArrowItem";

    private ItemStack arrowStack = ItemStack.EMPTY;
    private double materialDamage;
    private float speedMultiplier = 1.0f;
    private float accuracy = 1.0f;
    private double baseDamage = 2.0;

    public EntitySilentArrow(World worldIn) {
        super(worldIn);
    }

    public EntitySilentArrow(World worldIn, EntityLivingBase shooter, ItemStack stack) {
        super(worldIn, shooter);
        setArrowStack(stack);
    }

    private void setArrowStack(ItemStack stack) {
        if (stack.isEmpty()) return;
        this.arrowStack = stack.copy();
        this.arrowStack.setCount(1);

        NBTTagCompound tag = arrowStack.getSubCompound(CoreArrow.NBT_ROOT);
        if (tag != null) {
            this.materialDamage = tag.getFloat(CoreArrow.NBT_DAMAGE);
            this.speedMultiplier = tag.getFloat(CoreArrow.NBT_SPEED);
            this.accuracy = tag.getFloat(CoreArrow.NBT_ACCURACY);
        }
        if (this.speedMultiplier <= 0f) this.speedMultiplier = 1.0f;
        if (this.accuracy <= 0f) this.accuracy = 1.0f;
        setDamage(this.baseDamage);
    }

    @Override
    public void setDamage(double damage) {
        this.baseDamage = damage;
        super.setDamage(damage + this.materialDamage);
    }

    @Override
    public double getDamage() {
        return this.baseDamage;
    }

    @Override
    public void shoot(Entity shooter, float pitch, float yaw, float velocityOffset, float velocity, float inaccuracy) {
        // Apply the material damage once even if the shooter never calls setDamage after firing.
        setDamage(this.baseDamage);
        super.shoot(shooter, pitch, yaw, velocityOffset, velocity * this.speedMultiplier,
                inaccuracy / this.accuracy);
    }

    @Override
    protected ItemStack getArrowStack() {
        return this.arrowStack.isEmpty() ? ItemStack.EMPTY : this.arrowStack.copy();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setDouble(NBT_BASE_DAMAGE, this.baseDamage);
        compound.setDouble(NBT_MATERIAL_DAMAGE, this.materialDamage);
        compound.setFloat(NBT_SPEED, this.speedMultiplier);
        compound.setFloat(NBT_ACCURACY, this.accuracy);
        if (!this.arrowStack.isEmpty()) {
            NBTTagCompound itemTag = new NBTTagCompound();
            this.arrowStack.writeToNBT(itemTag);
            compound.setTag(NBT_ARROW_ITEM, itemTag);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.baseDamage = compound.hasKey(NBT_BASE_DAMAGE) ? compound.getDouble(NBT_BASE_DAMAGE) : 2.0;
        this.materialDamage = compound.getDouble(NBT_MATERIAL_DAMAGE);
        this.speedMultiplier = compound.hasKey(NBT_SPEED, 5) ? compound.getFloat(NBT_SPEED) : 1.0f;
        this.accuracy = compound.hasKey(NBT_ACCURACY, 5) ? compound.getFloat(NBT_ACCURACY) : 1.0f;
        if (compound.hasKey(NBT_ARROW_ITEM, 10)) {
            this.arrowStack = new ItemStack(compound.getCompoundTag(NBT_ARROW_ITEM));
            this.arrowStack.setCount(1);
        }
        super.setDamage(this.baseDamage + this.materialDamage);
    }
}
