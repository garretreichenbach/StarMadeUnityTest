package org.schema.game.common.data.blockeffects.config;

import org.schema.schine.common.language.Lng;

public enum StatusEffectCategory {
	AI(new Object(){@Override
	public String toString(){return Lng.str("AI");}}),
	ARMOR(new Object(){@Override
	public String toString(){return Lng.str("Armor");}}),
	CARGO(new Object(){@Override
	public String toString(){return Lng.str("Cargo");}}),
	OLD_CLOAKERS(new Object(){@Override
	public String toString(){return Lng.str("Cloakers");}}),
	DAMAGE(new Object(){@Override
	public String toString(){return Lng.str("Damage");}}),
	FACTORIES(new Object(){@Override
	public String toString(){return Lng.str("Factories");}}),
	GRAVITY(new Object(){@Override
	public String toString(){return Lng.str("Gravity");}}),
	JUMP(new Object(){@Override
	public String toString(){return Lng.str("Jump Drive");}}),
	MASS(new Object(){@Override
	public String toString(){return Lng.str("Mass");}}),
	MINING(new Object(){@Override
	public String toString(){return Lng.str("Mining");}}),
	POWER(new Object(){@Override
	public String toString(){return Lng.str("Power");}}),
	OLD_RADARJAM(new Object(){@Override
	public String toString(){return Lng.str("Radar Jamming");}}),
	STEALTH(new Object(){@Override
	public String toString(){return Lng.str("Stealth");}}),
	RAILS(new Object(){@Override
	public String toString(){return Lng.str("Rails");}}),
	REACTOR(new Object(){@Override
	public String toString(){return Lng.str("Reactor");}}),
	SCANNERS(new Object(){@Override
	public String toString(){return Lng.str("Scanners");}}),
	SHIELDS(new Object(){@Override
	public String toString(){return Lng.str("Shields");}}),
	SHIPYARDS(new Object(){@Override
	public String toString(){return Lng.str("Shipyards");}}),
	THRUSTERS(new Object(){@Override
	public String toString(){return Lng.str("Thrusters");}}),
	TRANSPORTERS(new Object(){@Override
	public String toString(){return Lng.str("Transporters");}}),
	VISIBILITY(new Object(){@Override
	public String toString(){return Lng.str("Visibility");}}),
	WARHEADS(new Object(){@Override
	public String toString(){return Lng.str("Warheads");}}),
	WARP(new Object(){@Override
	public String toString(){return Lng.str("Warp Gates");}}),
	WEAPON(new Object(){@Override
	public String toString(){return Lng.str("Weapon");}}),
	BUILDING(new Object(){@Override
	public String toString(){return Lng.str("Building");}}),
	;
	
	
	private final Object nameObj;
	private StatusEffectCategory(Object nm){
		this.nameObj = nm;
	}
	
	public String getName(){
		return nameObj.toString();
	}
}
