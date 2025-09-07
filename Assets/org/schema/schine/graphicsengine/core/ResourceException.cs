using System;
using System.IO; // For IOException
using System.Runtime.Serialization; // For SerializationInfo and StreamingContext

namespace Org.Schema.Schine.GraphicsEngine.Core
{
    [Serializable] // Mark as serializable if it needs to be
    public class ResourceException : IOException
    {
        public string DefaultPath { get; private set; } // Converted to public property

        public ResourceException() : base() { }

        public ResourceException(string message) : base(message)
        {
            DefaultPath = message;
        }

        public ResourceException(string message, Exception innerException) : base(message, innerException)
        {
            DefaultPath = message;
        }

        public ResourceException(Exception innerException) : base("A resource error occurred.", innerException)
        {
            // Default message if only cause is provided
        }

        // Constructor for serialization
        protected ResourceException(SerializationInfo info, StreamingContext context) : base(info, context)
        {
            DefaultPath = info.GetString("DefaultPath");
        }

        // Override GetObjectData for serialization
        public override void GetObjectData(SerializationInfo info, StreamingContext context)
        {
            base.GetObjectData(info, context);
            info.AddValue("DefaultPath", DefaultPath);
        }
    }
}