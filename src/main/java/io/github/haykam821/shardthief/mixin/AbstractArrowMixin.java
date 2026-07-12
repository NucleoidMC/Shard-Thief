package io.github.haykam821.shardthief.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.haykam821.shardthief.game.event.AllowProjectileHitEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import xyz.nucleoid.stimuli.EventInvokers;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.EventResult;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Projectile {
	public AbstractArrowMixin(EntityType<? extends Projectile> entityType, Level world) {
		super(entityType, world);
	}

	@Inject(method = "canHitEntity", at = @At("HEAD"), cancellable = true)
	private void preventArrowHit(Entity entity, CallbackInfoReturnable<Boolean> ci) {
		if (entity.level().isClientSide()) {
			return;
		}

		try (EventInvokers invokers = Stimuli.select().forEntity(entity)) {
			AbstractArrow projectile = (AbstractArrow) (Object) this;
			EventResult result = invokers.get(AllowProjectileHitEvent.EVENT).allowProjectileHit(entity, projectile);

			if (result == EventResult.DENY) {
				ci.setReturnValue(false);
			}
		}
	}
}
