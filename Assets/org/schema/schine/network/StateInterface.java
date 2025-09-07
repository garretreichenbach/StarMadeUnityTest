package org.schema.schine.network;

import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.schema.common.util.Version;
import org.schema.schine.common.DebugTimer;
import org.schema.schine.common.TextCallback;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.network.commands.SynchronizeAllCommandPackage;
import org.schema.schine.network.commands.SynchronizePrivateCommandPackage;
import org.schema.schine.network.commands.SynchronizePublicCommandPackage;
import org.schema.schine.network.common.NetworkManager;
import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.common.NetworkSettings;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.resource.ResourceMap;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadPoolExecutor;

public interface StateInterface {

	public Object2ObjectOpenHashMap<Class<? extends NetworkObject>, Int2LongOpenHashMap> getSentData();

	public Object2ObjectOpenHashMap<Class<? extends NetworkObject>, Int2LongOpenHashMap> getReceivedData();

	public void chat(String from, String message, String prefix, boolean diaplayName);

	public String[] getCommandPrefixes();

	public DataStatsManager getDataStatsManager();

	public ControllerInterface getController();

	public byte[] getDataBuffer();

	public ByteBuffer getDataByteBuffer();

	public boolean isPassive();

	int getId();

	public NetworkStateContainer getLocalAndRemoteObjectContainer();

	public NetworkStatus getNetworkStatus();

	public int getServerTimeDifference();

	public short getNumberOfUpdate();

	public Version getVersion();

	public void incUpdateNumber();

	public boolean isReadingBigChunk();

	boolean isReady();


	public void notifyOfAddedObject(Sendable sendable);

	public void notifyOfRemovedObject(Sendable sendable);

	public String onAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException;

	public void onStringCommand(String subSequence, TextCallback callback, String prefix);

	public void releaseDataByteBuffer(ByteBuffer buffer);

	public ResourceMap getResourceMap();

	public long getUpdateTime();

	public void setSynched();

	public void setUnsynched();

	public boolean isSynched();

	public long getUploadBlockSize();

	public void exit();

	public DebugTimer getDebugTimer();

	public NetworkSettings getSettings();

	public boolean isOnServer();

	public NetworkManager getNetworkManager();

	public ThreadPoolExecutor getConnectionThreadPool();

	public void notifyUpdateNeeded();

	public void receivedSynchronization(NetworkProcessor from, SynchronizePublicCommandPackage pack) throws IOException;
	public void receivedPrivateSynchronization(NetworkProcessor from, SynchronizePrivateCommandPackage pack) throws IOException;
	public void receivedAllSynchronization(NetworkProcessor recipient, SynchronizeAllCommandPackage pack) throws IOException;

}
