namespace Api.Element.Block
{
    public enum FactoryType
    {
        //stateDescs = {"none", "capsule refinery", "micro assembler", "basic factory", "standard factory", "advanced factory"},
        None,
        CapsuleRefinery,
        MicroAssembler,
        Basic,
        Standard,
        Advanced
    }

    public static class FactoryTypeExtensions
    {
        public static short GetId(this FactoryType factoryType)
        {
            return (short)factoryType;
        }
    }
}
