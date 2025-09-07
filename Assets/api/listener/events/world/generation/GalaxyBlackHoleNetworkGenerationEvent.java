package api.listener.events.world.generation;

import api.listener.events.Event;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.physics.Pair;

import java.util.List;

/**
 * STARMADE MOD
 * CREATOR: Ithirahad Ivrar'kiim
 * DATE: 27.8.2020
 * TIME: Too damn late.
`* This event will be fired whenever the game attempts to create a black hole network.
 * However, that will require reorganizing the loop in Galaxy that creates the network in the first place. (TO-DO)
 * At the moment, it doesn't look like there is any good place to fire the event, as that would have to take place
 * between the finalization of the black hole network map and the actual creation of the network...
 * And at the moment, the game appears to create actual links as it goes.
 * Status: Never fired; doesn't do anything.
 */
public class GalaxyBlackHoleNetworkGenerationEvent extends Event {
    private double blackHoleCount;
    private List<Vector3i> blackHoles;
    private ObjectArrayList<Pair<Vector3i>> blackHoleLinks = new ObjectArrayList<>(); //Gross misuse of a class probably, but if it works...

    public GalaxyBlackHoleNetworkGenerationEvent(double blackHoleCount, List<Vector3i> blackHoles){
        this.blackHoleCount = blackHoleCount;
        this.blackHoles = blackHoles;
    }

    public void setLinks(List<Pair<Vector3i>> links){
        blackHoleLinks.clear(); //"set" means we presumably don't want the old ones.
        for(Pair<Vector3i> link : links){
            if(blackHoles.contains(link.a) && blackHoles.contains(link.b))
                blackHoleLinks.add(new Pair<>(link.a,link.b));
            else
                /*TODO: Throw an exception? You should not be trying to make a link out of nonexistent black holes.*/;
        }
    }

    public void addLink(Pair<Vector3i> link){
        blackHoleLinks.add(link);
    }

    public void addLink(Vector3i origin, Vector3i destination){
        blackHoleLinks.add(new Pair<>(origin,destination));
    }

    public void clearLinks(){
        blackHoleLinks.clear();
    }

    public List<Pair<Vector3i>> getBlackHoleLinks(){
        return blackHoleLinks;
    }
}
