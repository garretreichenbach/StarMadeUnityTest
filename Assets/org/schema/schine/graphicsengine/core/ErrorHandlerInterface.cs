using System;

namespace Org.Schema.Schine.GraphicsEngine.Core
{
    public interface IErrorHandler
    {
        void HandleError(string msg);
        void HandleError(Exception e);
    }
}
