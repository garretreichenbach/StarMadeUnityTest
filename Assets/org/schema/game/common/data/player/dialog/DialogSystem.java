package org.schema.game.common.data.player.dialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.schema.common.util.data.DataUtil;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.resource.FileExt;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class DialogSystem {

	private final AiEntityStateInterface gObj;
	private State mainState;
	private State fromState;
	private Object2ObjectOpenHashMap<String, State> directStates;

	public DialogSystem(AiEntityStateInterface gObj) {
		System.err.println("DialogSystem instantiated " + gObj);
		this.gObj = gObj;

	}

	public static DialogSystem load(AICreatureDialogAI gObj) throws ScriptException, IOException {

		System.err.println("[DIALOG] LOADING: " + gObj);
		Globals globals = JsePlatform.standardGlobals();
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine scriptEngine = mgr.getEngineByName("luaj");
		File f = new FileExt("." + File.separator + DataUtil.dataPath + "script" + File.separator + gObj.getScriptName());

		BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
		LuaValue chunk;
		CompiledScript script = ((Compilable) scriptEngine).compile(bufferedReader);
		Bindings sb = new SimpleBindings();

		script.eval(sb); // Put the Lua functions into the sb environment
		LuaValue luaGObj = CoerceJavaToLua.coerce(gObj); // Java to Lua
		LuaValue binding = CoerceJavaToLua.coerce(sb); // Java to Lua
		LuaFunction onTalk = (LuaFunction) sb.get("create"); // Get Lua function

		LuaValue b = onTalk.call(luaGObj, binding); // Call the function
		System.out.println("onTalk answered: " + b);

		DialogSystem ret = (DialogSystem) CoerceLuaToJava.coerce(b, DialogSystem.class);
		bufferedReader.close();
		return ret;
		//		try {
		//			chunk = globals.load(,"tests.lua");
		//
		//
		//			chunk.call(luaGObj);
		//
		//
		//		} catch (FileNotFoundException e) {
		//			e.printStackTrace();
		//		}
	}

	public static void main(String[] atgs) {
		//		Globals globals = JsePlatform.standardGlobals();
		//		LuaValue load = globals.load(new DialogSystem());
		//		File f = new FileExt("./"+DataUtil.dataPath+"script/tests.lua");
		//		LuaValue chunk;
		//		globals.get("load").call(LuaValue.valueOf(
		//	            "sys = luajava.bindClass('java.lang.System')\n" +
		//	            "print ( sys:currentTimeMillis() )\n")).call();
	}

	public DialogStateMachineFactory getFactory(Bindings bindings) {

		fromState = new VoidState(gObj);
		DialogTextState resetStepState = new DialogTextState(gObj, gObj.getState(), null, "Reset State", 0);
		DialogTextState endState = new DialogTextState(gObj, gObj.getState(), null, "End State", 0);
		DialogTextState totalEndState = new DialogTextState(gObj, gObj.getState(), null, "Total End State", 0);

		assert (bindings != null);
		return new DialogStateMachineFactory(gObj, fromState, resetStepState, endState, totalEndState, gObj.getState(), bindings);
	}

	/**
	 * -------------------------------- called by Lua --------------
	 *
	 * @param factory
	 */
	public void add(DialogStateMachineFactory factory) {
		//CALLED BY LUA AT THE END OF THE SCRIPT
		mainState = factory.create();
		this.directStates = (factory.getDirectStates());
		//		System.err.println("Created Stte from: "+fromState);
		//		System.err.println("Created Stte to: "+mainState);
	}

	/**
	 * @return the mainState
	 */
	public State getMainState() {
		return mainState;
	}

	/**
	 * @return the fromState
	 */
	public State getFromState() {
		return fromState;
	}

	/**
	 * @return the directStates
	 */
	public Object2ObjectOpenHashMap<String, State> getDirectStates() {
		return directStates;
	}

}
