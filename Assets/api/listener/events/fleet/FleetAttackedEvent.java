package api.listener.events.fleet;

import api.common.GameServer;
import api.listener.events.Event;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 07.10.2020
 * TIME: 18:45
 */

/**
 * Is fired when a fleetmember is attacked
 */
public class FleetAttackedEvent extends Event {
    public FleetMember getMember() {
        return member;
    }

    private Fleet fleet;

    public Fleet getFleet() {
        return fleet;
    }

    private FleetMember member;

    public SegmentController getMemberSC() {
        return memberSC;
    }

    private SegmentController memberSC;
    public FleetAttackedEvent(Fleet fleet, FleetMember member) {
        this.fleet = fleet;
        this.member = member;
        for (SegmentController sc: GameServer.getServerState().getSegmentControllersByName().values()) {
            if (sc.getUniqueIdentifier().equals(member.UID)) {
               this.memberSC = sc;
            }
        }
        //DebugFile.log("a fleet member was attacked and the event was fired");
    }
}
