// Placeholder for HostPortLoginName
namespace Org.Schema.Schine.Network.Client
{
    public class HostPortLoginName
    {
        public string Host;
        public int Port;
        public string Name;
        public string ClientType; // Assuming STARMADE_CLIENT is a string constant

        public HostPortLoginName(string host, int port, string clientType, string name)
        {
            Host = host;
            Port = port;
            ClientType = clientType;
            Name = name;
        }

        // Assuming STARMADE_CLIENT is a constant
        public const string STARMADE_CLIENT = "STARMADE_CLIENT";
    }
}

namespace Org.Schema.Schine.GraphicsEngine.Core
{
    public class StateChangeRequest
    {
        public Org.Schema.Schine.Network.Client.HostPortLoginName HostPortLogin = new Org.Schema.Schine.Network.Client.HostPortLoginName("localhost", 4242, Org.Schema.Schine.Network.Client.HostPortLoginName.STARMADE_CLIENT, "schemanew");
    }
}