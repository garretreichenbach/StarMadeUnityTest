using System.Collections.Generic;
using System.Linq;

namespace Api.Common
{
    public class GameClient
    {
        public static GameClientState GetClientState()
        {
            return GameClientState.instance;
        }

        public static GameClientController GetClientController()
        {
            return GameClientState.instance.GetController();
        }

        public static PlayerState GetClientPlayerState()
        {
            return GetClientState().GetPlayer();
        }

        //Lots of internal stuff to be cleaned up later
        public static void MakeChatMessage(string msg)
        {
            GameClientState inst = GetClientState();
            inst.Chat("[SYSTEM]", "[ALL]", msg, true);
        }

        public static void SendMessage(string s)
        {
            GetClientState().Chat("[SYSTEM]", s, "[ALL]", true);
        }

        public static void ShowPopupMessage(string str, int time)
        {
            GetClientState().GetController().PopupInfoTextMessage(str, time);
        }

        public static void ShowBigText(string header, string str, int time)
        {
            GetClientState().GetController().ShowBigTitleMessage(header, str, time);
        }

        public static ICollection<PlayerState> GetConnectedPlayers()
        {
            return GameClientState.instance.GetOnlinePlayersLowerCaseMap().Values;
        }

        public static SegmentController GetCurrentControl()
        {
            ISet<ControllerStateUnit> units = GetClientPlayerState().GetControllerState().GetUnits();
            if (!units.Any()) return null;
            ControllerStateUnit unit = units.First();
            if (unit.playerControllable is SegmentController) return (SegmentController)unit.playerControllable;
            else return null;
        }

        public static ICollection<Fleet> GetAvailableFleets()
        {
            return GetClientState().GetFleetManager().GetAvailableFleetsClient();
        }

        public static void SetLoadString(string s)
        {
            Controller.GetResLoader().SetLoadString(s);
        }

        public static void SpawnBlockParticle(short id, Vector3f pos)
        {
            GameClientState state = GetClientState();
            if (state == null) return;
            Vector3fb vector3fb = new Vector3fb(pos);
            Transform transform = new Transform();
            transform.setIdentity();
            transform.origin.set(vector3fb);
            state.GetWorldDrawer().GetShards().VoronoiBBShatter((PhysicsExt)state.GetPhysics(), transform, id, state.GetCurrentSectorId(), transform.origin, null);
        }

        public static PlayerInteractionControlManager GetControlManager()
        {
            return GetClientState().GetGlobalGameControlManager().GetIngameControlManager().GetPlayerGameControlManager().GetPlayerIntercationManager();
        }
    }
}
