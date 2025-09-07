using System;

public class SegmentDataWriteException : Exception
{
    public SegmentDataWriteException(string message) : base(message)
    {
    }
}

public class SegmentInflaterException : Exception
{
    public SegmentInflaterException(string message) : base(message)
    {
    }
}

public class DataFormatException : Exception
{
    public DataFormatException(string message) : base(message)
    {
    }
}
