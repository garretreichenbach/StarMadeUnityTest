package api.utils;

import api.element.block.Blocks;
import api.mod.StarMod;
import api.utils.registry.UniversalRegistry;
import org.schema.game.common.controller.PlayerUsableInterface;

public class PlayerUsableHelper {

    /**
     * Gets a PlayerUsableInterface id from mod+uid
     *
     * The blockId will be registered in ICONS if it doesnt exist already.
     * These values persist between restart and are synchronized between client and server on join.
     */
    public static long getPlayerUsableId(short blockId, StarMod container, String uniqueId){
        long urv = UniversalRegistry.getExistingURV(UniversalRegistry.RegistryType.PLAYER_USABLE_ID, container, uniqueId);
        //Might already be registered, but it doesnt matter
        PlayerUsableInterface.ICONS.put(urv, blockId);
        return urv;
    }

    public static long getPlayerUsableId(short blockId) {
        switch(Blocks.fromId(blockId)) {
            case STEALTH_MODULE, STEALTH_COMPUTER -> {
                return PlayerUsableInterface.USABLE_ID_STEALTH;
            }
            case STEALTH_JAMMER, STEALTH_CLOAKER, STEALTH_ANTI_LOCKON, STEALTH_POWER_EFFICIENCY_1, STEALTH_POWER_EFFICIENCY_2, STEALTH_POWER_EFFICIENCY_3, STEALTH_RECHARGE_RATE_1, STEALTH_RECHARGE_RATE_2, STEALTH_RECHARGE_RATE_3, STEALTH_STRENGTH_1, STEALTH_STRENGTH_2, STEALTH_STRENGTH_3, STEALTH_STRENGTH_4, STEALTH_STRENGTH_5, STEALTH_STRENGTH_6 -> {
                return PlayerUsableInterface.USABLE_ID_STEALTH_REACTOR;
            }
            case JUMP_DRIVE_ROOT, JUMP_DRIVE_COMPUTER, JUMP_DRIVE_MODULE, JUMP_AUTOCHARGE, JUMP_CHARGE_TIME_1, JUMP_CHARGE_TIME_2, JUMP_CHARGE_TIME_3, JUMP_DISTANCE_1, JUMP_DISTANCE_2, JUMP_DISTANCE_3, JUMP_MULTICHARGE_1, JUMP_MULTICHARGE_2, JUMP_MULTICHARGE_3, JUMP_POWEREFFICIENCY_1, JUMP_POWEREFFICIENCY_2, JUMP_POWEREFFICIENCY_3 -> {
                return PlayerUsableInterface.USABLE_ID_JUMP;
            }
            case REACTOR_BASE_ENHANCEMENT, REACTOR_BOOSTER_1, REACTOR_BOOSTER_2, REACTOR_BOOSTER_3 -> {
                return PlayerUsableInterface.USABLE_ID_REACTOR_BOOST;
            }
            case THRUST_CONFIG_APPLY_SPEED_1, THRUST_CONFIG_APPLY_SPEED_2 -> {
                return PlayerUsableInterface.USABLE_ID_THRUSTER;
            }
            case THRUST_BLAST_1, THRUST_BLAST_CHARGE_SPEED_1, THRUST_BLAST_CHARGE_SPEED_2, THRUST_BLAST_STRENGTH_1, THRUST_BLAST_STRENGTH_2, THRUST_BLAST_MULTI_CHARGE_1, THRUST_BLAST_MULTI_CHARGE_2 -> {
                return PlayerUsableInterface.USABLE_ID_TAKE_OFF;
            }
            case ACTIVATION_MODULE -> {
                return PlayerUsableInterface.USABLE_ID_ACTIVATION_BEAM;
            }
            case SCANNER_COMPUTER, SCANNER_ANTENNA, ORE_SCANNER, DEEP_CORE_SCANNER, PROSPECTOR_SCANNER -> {
                return PlayerUsableInterface.USABLE_ID_SPACE_SCAN;
            }
            case WARP_INTERDICTION_1, WARP_INTERDICTION_2, WARP_INTERDICTION_3, WARP_INTERDICTION_4, WARP_INTERDICTION_5, WARP_INTERDICTION_6, WARP_INTERDICTION_7, WARP_INTERDICTION_8, WARP_INTERDICTION_9, WARP_INTERDICTION_10, WARP_INTERDICTION_DISTANCE_1, WARP_INTERDICTION_DISTANCE_2 -> {
                return PlayerUsableInterface.USABLE_ID_INTERDICTION;
            }
            case MINE_LAYER -> {
                return PlayerUsableInterface.USABLE_ID_MINE_SHOOTER;
            }
            default -> {
                return -1;
            }
        }
    }
}
