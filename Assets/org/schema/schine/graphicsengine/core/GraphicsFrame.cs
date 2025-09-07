using System;
using System.IO;
using System.Xml.Linq; // For SAXException, ParserConfigurationException (simplified)

// Placeholders for custom exceptions
using Org.Schema.Common; // For ParseException
using Org.Schema.Schine.Resource; // For ResourceException

namespace Org.Schema.Schine.GraphicsEngine.Core
{
    public interface GraphicsFrame
    {
        void doFrameAndUpdate(GraphicsContext context);

        void handleException(Exception e);

        void onEndLoop(GraphicsContext context);

        void afterFrame();

        void enqueueFrameResources();

        void setFinishedFrame(bool b);

        bool synchByFrame();

        void queueException(Exception e); // Using System.Exception for RuntimeException
    }
}
