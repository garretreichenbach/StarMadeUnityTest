namespace Api.Common
{
    public class GameCommon
    {
        public static SendableGameState GetGameState()
        {
            if (GameServerState.instance != null)
            {
                return GameServerState.instance.GetGameState();
            }
            else if (GameClientState.instance != null)
            {
                return GameClientState.instance.GetGameState();
            }
            //Probably in the main menu or something
            return null;
        }

        public static Sendable GetGameObject(int id)
        {
            return GetGameState().GetState().GetLocalAndRemoteObjectContainer().GetLocalObjects()[id];
        }

        public static bool IsOnSinglePlayer()
        {
            return ModStarter.JustStartedSinglePlayer;
        }

        public static bool IsDedicatedServer()
        {
            return ModStarter.JustStartedServer && !ModStarter.JustStartedSinglePlayer;
        }

        public static bool IsClientConnectedToServer()
        {
            return !ModStarter.JustStartedServer && ModStarter.JustStartedClient;
        }

        /// <summary>
        /// A unique id depending on the context the mod is loaded in.
        /// Single player/Dedicated Server = Universe Name
        /// Client connected to server = Server IP:Port
        /// </summary>
        public static string GetUniqueContextId()
        {
            return ModStarter.LastConnected;
        }

        public static PlayerState GetPlayerFromName(string pName)
        {
            if (GameServer.GetServerState() != null)
            {
                try
                {
                    return GameServer.GetServerState().GetPlayerFromName(pName);
                }
                catch (PlayerNotFoundException)
                {
                    return null;
                }
            }
            return GameClient.GetClientState().GetOnlinePlayersLowerCaseMap()[pName.ToLower()];
        }
    }
}
