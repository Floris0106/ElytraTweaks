package floris0106.elytratweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import floris0106.elytratweaks.ElytraTweaks;
import floris0106.elytratweaks.util.InventoryUtil;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer
{
	@Shadow
	private boolean wasFallFlying;

	public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile)
	{
		super(clientLevel, gameProfile);
	}

	@Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setSprinting(Z)V", ordinal = 2))
	private void onStartSprinting(CallbackInfo ci)
	{
		if (isUnderWater())
			stopFallFlying();
	}

	@WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;tryToStartFallFlying()Z"))
	private boolean startFlying(LocalPlayer instance, Operation<Boolean> original)
	{
		if (isFallFlying() ||
			getAbilities().flying ||
			isInWater() ||
			onGround() ||
			isPassenger() ||
			hasEffect(MobEffects.LEVITATION))
			return false;

		for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES)
			if (canGlideUsing(getItemBySlot(equipmentSlot), equipmentSlot))
			{
				startFallFlying();
				return true;
			}

		if (!InventoryUtil.equipGlider(this))
			return false;

		startFallFlying();
		return true;
	}

	@WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isFallFlying()Z"))
	private boolean stopFlying(LocalPlayer instance, Operation<Boolean> original)
	{
		if (ElytraTweaks.STOP_FLYING_KEY.isDown())
			stopFallFlying();

		boolean isFallFlying = original.call(instance);

		if (wasFallFlying && !isFallFlying)
			InventoryUtil.equipArmor(this);

		return isFallFlying;
	}

	@ModifyExpressionValue(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isFallFlying()Z"))
	private boolean allowFlying(boolean original)
	{
		return false;
	}
}