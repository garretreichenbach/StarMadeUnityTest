using System;

namespace Api.Common
{
    public class GameServer
    {
        public static GameServerState GetServerState()
        {
            return GameServerState.instance;
        }

        public static Universe GetUniverse()
        {
            return GetServerState().GetUniverse();
        }

        public static RegisteredClientOnServer GetServerClient(PlayerState player)
        {
            return GetServerState().GetClients()[player.GetClientId()];
        }

        public static void ExecuteAdminCommand(string command)
        {
            RegisteredClientInterface registeredClientInterface = GetServerState().GetAdminLocalClient();
            try
            {
                string[] split2;
                AdminCommands value = (AdminCommands)Enum.Parse(typeof(AdminCommands), (split2 = command.Split(' '))[0].Trim().ToUpper());
                GetServerState().GetController().EnqueueAdminCommand(registeredClientInterface, value, AdminCommands.PackParameters(value, StringTools.SplitParameters(command.Substring(split2[0].Length))));
            }
            catch (Exception ex3)
            {
                Debug.LogException(ex3);
                Debug.LogError("Failed to execute admin command: " + command);
                registeredClientInterface.ExecutedAdminCommand();
            }
        }
    }
}
