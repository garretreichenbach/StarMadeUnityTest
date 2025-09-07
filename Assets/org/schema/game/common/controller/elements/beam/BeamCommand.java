package org.schema.game.common.controller.elements.beam;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.data.element.beam.BeamReloadCallback;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.player.PlayerState;

import javax.vecmath.Vector3f;

public class BeamCommand {
	public long identifier;
	public final Vector3f relativePos = new Vector3f();
	public BeamReloadCallback reloadCallback;
	public final Vector3f from = new Vector3f();
	public final Vector3f to = new Vector3f();
	public PlayerState playerState;
	public float beamTimeout;
	public float tickRate;
	public float beamPower;
	public float cooldownSec;
	public float bursttime;
	public float initialTicks;
	public float powerConsumedByTick;
	public float powerConsumedExtraByTick;
	public long weaponId;
	public boolean dontFade = false; //for testing so that a beam does a full burst
	public boolean lastShot = true; //only to be used with split combination
	public int beamType;
	public MetaObject originMetaObject;
	public Vector3i controllerPos;
	public double railParent;
	public double railChild;
	public boolean handheld;
	public long currentTime;
	public boolean latchOn;
	public HitType hitType;
	public boolean firendlyFire;
	public boolean penetrating;
	public float acidDamagePercent;
	public boolean checkLatchConnection;
	public float minEffectiveRange = 1.0f;
	public float minEffectiveValue = 1.0f;
	public float maxEffectiveRange = 1.0f;
	public float maxEffectiveValue = 1.0f;
	public boolean ignoreShields;
	public boolean ignoreArmor;
	public float capacityPerTick = 0;
}
