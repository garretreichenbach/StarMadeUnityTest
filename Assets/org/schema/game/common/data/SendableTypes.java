package org.schema.game.common.data;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.common.controller.*;
import org.schema.game.common.data.creature.AICharacter;
import org.schema.game.common.data.creature.AIRandomCompositeCreature;
import org.schema.game.common.data.player.*;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.space.BlackHole;
import org.schema.game.common.data.world.space.PlanetCore;
import org.schema.game.common.data.world.space.PlanetIcoCore;
import org.schema.game.common.data.world.space.Sun;
import org.schema.schine.network.NetUtil;
import org.schema.schine.network.SendableFactory;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;

import java.io.IOException;

public enum SendableTypes implements SendableType {

    AI_RANDOM_COMPOSITE_CREATURE((byte) 1, AIRandomCompositeCreature::new),
    BLACK_HOLE((byte) 2, BlackHole::new),
    CHARACTER_PROVIDER((byte) 3, CharacterProvider::new),
    CLIENT_CHANNEL((byte) 4, ClientChannel::new),
    FIXED_SPACE_ENTITY_PROVIDER((byte) 6, FixedSpaceEntityProvider::new),
    FLOATING_ROCK((byte) 7, FloatingRock::new),
    PLANET((byte) 8, Planet::new),
    PLANET_CORE((byte) 9, PlanetIcoCore::new),//PlanetCore::new),
    PLANET_ICO((byte) 10, PlanetIco::new),
    PLAYER_CHARACTER((byte) 11, PlayerCharacter::new),
    PLAYER_STATE((byte) 12, PlayerState::new),
    REMOTE_SECTOR((byte) 13, RemoteSector::new),
    SENDABLE_GAME_STATE((byte) 14, SendableGameState::new),
    SENDABLE_SEGMENT_PROVIDER((byte) 15, SendableSegmentProvider::new),
    SHIP((byte) 16, Ship::new),
    SHOP_SPACE_STATION((byte) 17, ShopSpaceStation::new),
    SPACE_CREATURE((byte) 18, SpaceCreature::new),
    SPACE_CREATURE_PROVIDER((byte) 19, SpaceCreatureProvider::new),
    SPACE_STATION((byte) 20, SpaceStation::new),
    SUN((byte) 21, Sun::new),
    TEAM_DEATH_STAR((byte) 22, TeamDeathStar::new),
    GAS_PLANET((byte) 23, GasPlanet::new),
    AI_CHARACTER((byte) 24, AICharacter::new),
    MANAGED_SHOP((byte) 25, ManagedShop::new),
    VEHICLE((byte) 26, Vehicle::new);

    public static void initTypesMap() {

        for(SendableTypes s : values()) {
            assert (!NetUtil.map.containsKey(s.code)) : s + "; " + NetUtil.map.get(s.code);
            NetUtil.map.put(s.code, s);
        }
    }

    private final byte code;
    private final SendableFactory fac;

    public String toString() {
        return name() + "(" + code + ")";
    }

    SendableTypes(byte code, SendableFactory fac) {
        this.code = code;
        this.fac = fac;
    }

    @Override
    public Sendable getInstance(StateInterface state) throws IOException {
        return fac.getInstance(state);
    }

    @Override
    public byte getTypeCode() {
        return code;
    }
}
