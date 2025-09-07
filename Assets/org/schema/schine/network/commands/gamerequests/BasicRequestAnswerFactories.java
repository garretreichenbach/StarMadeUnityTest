package org.schema.schine.network.commands.gamerequests;

public enum BasicRequestAnswerFactories implements GameRequestAnswerFactory{
	

	
	
	
	
	SERVER_TIME((byte)100, ServerTimeRequest::new, ServerTimeAnswer::new, true),
	
	
	
	
	
	
	;
	
	
	static {
		initialize();
	}

	public static void initialize(){
		for(BasicRequestAnswerFactories c : BasicRequestAnswerFactories.values()) {
			assert(!GameRequestAnswerFactory.factories.containsKey(c.id)):"Already contains key "+c+"; existing: "+GameRequestAnswerFactory.factories.get(c.id);
			GameRequestAnswerFactory.factories.put(c.id, c);
		}
	}

	public String toString() {
		return name()+"("+id+")";
	}
	public static interface ReqFac{
		public GameRequestInterface getRequestInstance(); 
	}
	public static interface AnsFac{
		public GameAnswerInterface getAnswerInstance(); 
	}
	private final byte id;
	private final ReqFac rFac;
	private final AnsFac aFac;
	private final boolean blocking;
	
	private BasicRequestAnswerFactories(byte id, ReqFac rFac, AnsFac aFac, boolean blocking) {
		this.id = id;
		this.rFac = rFac;
		this.aFac = aFac;
		this.blocking = blocking;
	}
	
	@Override
	public byte getGameRequestid() {
		return id;
	}

	@Override
	public GameRequestInterface getRequestInstance() {
		GameRequestInterface c = rFac.getRequestInstance();
		assert(c.getFactory() == this):c.getFactory()+"; "+this;
		return c;
	}

	@Override
	public GameAnswerInterface getAnswerInstance() {
		GameAnswerInterface c = aFac.getAnswerInstance();
		assert(c.getFactory() == this):c.getFactory()+"; "+this;
		return c;
	}

	@Override
	public boolean isBlocking() {
		return blocking;
	}

}
