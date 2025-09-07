package org.schema.schine.graphicsengine.psys;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.psys.modules.CollisionModule;
import org.schema.schine.graphicsengine.psys.modules.ColorBySpeedModule;
import org.schema.schine.graphicsengine.psys.modules.ColorOverLifetimeModule;
import org.schema.schine.graphicsengine.psys.modules.EmissionModule;
import org.schema.schine.graphicsengine.psys.modules.ForceOverLifetimeModule;
import org.schema.schine.graphicsengine.psys.modules.InitialModule;
import org.schema.schine.graphicsengine.psys.modules.LimitVelocityOverTimeModule;
import org.schema.schine.graphicsengine.psys.modules.ParticleSystemModule;
import org.schema.schine.graphicsengine.psys.modules.RendererModule;
import org.schema.schine.graphicsengine.psys.modules.RotationBySpeedModule;
import org.schema.schine.graphicsengine.psys.modules.RotationOverLifetimeModule;
import org.schema.schine.graphicsengine.psys.modules.ShapeModule;
import org.schema.schine.graphicsengine.psys.modules.SizeBySpeedModule;
import org.schema.schine.graphicsengine.psys.modules.SizeOverLifetimeModule;
import org.schema.schine.graphicsengine.psys.modules.TextureSheetAnimationModule;
import org.schema.schine.graphicsengine.psys.modules.VelocityOverLifetimeModule;
import org.schema.schine.graphicsengine.psys.modules.iface.ParticleColorInterface;
import org.schema.schine.graphicsengine.psys.modules.iface.ParticleDeathInterface;
import org.schema.schine.graphicsengine.psys.modules.iface.ParticleMoveInterface;
import org.schema.schine.graphicsengine.psys.modules.iface.ParticleStartInterface;
import org.schema.schine.graphicsengine.psys.modules.iface.ParticleUpdateInterface;
import org.schema.schine.physics.Physics;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ParticleSystemConfiguration {

	private InitialModule initialModule;
	private EmissionModule emissionModule;
	private RendererModule rendererModule;
	private CollisionModule collisionModule;
	private final List<ParticleSystemModule> modules = new ArrayList<ParticleSystemModule>();
	private String name;
	private String parent;
	protected String saveName;
	Vector3f velocityTmp = new Vector3f();
	private boolean moduleEnabledChanged;
	private ObjectArrayList<ParticleUpdateInterface> particleUpdateModules = new ObjectArrayList<ParticleUpdateInterface>();
	private ObjectArrayList<ParticleStartInterface> particleStartModules = new ObjectArrayList<ParticleStartInterface>();
	private ObjectArrayList<ParticleColorInterface> particleColorModules = new ObjectArrayList<ParticleColorInterface>();
	private ObjectArrayList<ParticleDeathInterface> particleDeathInterfaces = new ObjectArrayList<ParticleDeathInterface>();
	private ObjectArrayList<ParticleMoveInterface> particleMoveInterfaces = new ObjectArrayList<ParticleMoveInterface>();
	private ParticleContainer tmp = new ParticleContainer();
	private Random random = new Random();
	private static List<Class<? extends ParticleSystemModule>> allModules = new ArrayList<Class<? extends ParticleSystemModule>>();

	static
	{
		// Register modules
		allModules.add(InitialModule.class);
		allModules.add(EmissionModule.class);
		allModules.add(RendererModule.class);
		allModules.add(CollisionModule.class);
		allModules.add(ColorOverLifetimeModule.class);
		allModules.add(ColorBySpeedModule.class);
		allModules.add(LimitVelocityOverTimeModule.class);
		allModules.add(RotationBySpeedModule.class);
		allModules.add(RotationOverLifetimeModule.class);
		allModules.add(ShapeModule.class);
		allModules.add(SizeBySpeedModule.class);
		allModules.add(SizeOverLifetimeModule.class);
		allModules.add(TextureSheetAnimationModule.class);
		allModules.add(ForceOverLifetimeModule.class);
		allModules.add(VelocityOverLifetimeModule.class);
	}

	public ParticleSystemConfiguration() {

	}

	public static ParticleSystemConfiguration fromScratch() {
		return fromScratch("undefinedName", "noParent");
	}

	/**
	 * Construct a new particle system config, with all modules loaded
     */
	public static ParticleSystemConfiguration fromScratch(String name, String parent) {
		ParticleSystemConfiguration configuration = new ParticleSystemConfiguration();
		configuration.name = name;
		configuration.parent = parent;

		for (Class<? extends ParticleSystemModule> c : allModules)
		{
			try
			{
				ParticleSystemModule module = (ParticleSystemModule) c.getConstructors()[0].newInstance(configuration);
				if (module.getClass() == InitialModule.class)
				{
					configuration.initialModule = (InitialModule) module;
				}
				else if (module.getClass() == EmissionModule.class)
				{
					configuration.emissionModule = (EmissionModule) module;
				}
				else if (module.getClass() == RendererModule.class)
				{
					configuration.rendererModule = (RendererModule) module;
				}
				else if (module.getClass() == CollisionModule.class)
				{
					configuration.collisionModule = (CollisionModule) module;
				}
				configuration.modules.add(module);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		configuration.updateActiveModules();

/*
		configuration.initialModule = new InitialModule(configuration);
		configuration.emissionModule = new EmissionModule(configuration);
		configuration.rendererModule = new RendererModule(configuration);
		configuration.collisionModule = new CollisionModule(configuration);
		// Convert to global variables if necessary
		ColorOverLifetimeModule colorOverLifetimeModule = new ColorOverLifetimeModule(configuration);
		ColorBySpeedModule colorBySpeedModule = new ColorBySpeedModule(configuration);
		LimitVelocityOverTimeModule limitVelocityOverTimeModule = new LimitVelocityOverTimeModule(configuration);
		RotationBySpeedModule rotationBySpeedModule = new RotationBySpeedModule(configuration);
		RotationOverLifetimeModule rotationOverLifetimeModule = new RotationOverLifetimeModule(configuration);
		ShapeModule shapeModule = new ShapeModule(configuration);
		SizeBySpeedModule sizeBySpeedModule = new SizeBySpeedModule(configuration);
		SizeOverLifetimeModule sizeOverLifetimeModule = new SizeOverLifetimeModule(configuration);
		//SubEmitterModule subEmitterModule = new SubEmitterModule(this);
		TextureSheetAnimationModule textureSheetAnimationModule = new TextureSheetAnimationModule(configuration);
		ForceOverLifetimeModule forceOverLifetimeModule = new ForceOverLifetimeModule(configuration);
		VelocityOverLifetimeModule velocityOverLifetimeModule = new VelocityOverLifetimeModule(configuration);

		configuration.modules[i++] = configuration.initialModule;
		configuration.modules[i++] = configuration.emissionModule;
		configuration.modules[i++] = configuration.rendererModule;
		configuration.modules[i++] = configuration.collisionModule;
		configuration.modules[i++] = colorBySpeedModule;
		configuration.modules[i++] = colorOverLifetimeModule;
		configuration.modules[i++] = limitVelocityOverTimeModule;
		configuration.modules[i++] = rotationBySpeedModule;
		configuration.modules[i++] = rotationOverLifetimeModule;
		configuration.modules[i++] = shapeModule;
		configuration.modules[i++] = sizeBySpeedModule;
		configuration.modules[i++] = sizeOverLifetimeModule;
		//configuration.modules[i++] = subEmitterModule;
		configuration.modules[i++] = textureSheetAnimationModule;
		configuration.modules[i++] = forceOverLifetimeModule;
		configuration.modules[i] = velocityOverLifetimeModule;*/

		return configuration;
	}

	/**
	 * Loads the particle from a file. NOTE: This does not load all modules, only the ones that were active upon file read
     */
	public static ParticleSystemConfiguration fromFile(File f, boolean loadDisabledModules) throws SAXException, IOException, ParserConfigurationException, IllegalArgumentException, IllegalAccessException, DOMException {
		ParticleSystemConfiguration configuration = new ParticleSystemConfiguration();

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(f);

		Element root = doc.getDocumentElement();

//		System.err.println("PARSING: "+root.getNodeName());

		String name = root.getAttributes().getNamedItem("name").getNodeValue();
		String parent = root.getAttributes().getNamedItem("parent").getNodeValue();
		configuration.name = name;
		configuration.parent = parent;

		System.err.println("[PSYSCONFIG] Loading " + name + "; Parent: " + parent);

		NodeList childNodes = root.getChildNodes();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				for (Class<? extends ParticleSystemModule> c : allModules) {
					try {
						ParticleSystemModule module = (ParticleSystemModule) c.getConstructors()[0].newInstance(configuration);
						String moduleName = (String)c.getMethod("getName").invoke(module);
						if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals(moduleName.replace(" ", "").toLowerCase(Locale.ENGLISH))) {
							if (loadDisabledModules || Boolean.parseBoolean(item.getAttributes().getNamedItem("enabled").getNodeValue()))
							{
								if (module.getClass() == InitialModule.class) {
									configuration.initialModule = (InitialModule) module;
								}
								else if (module.getClass() == EmissionModule.class) {
									configuration.emissionModule = (EmissionModule) module;
								}
								else if (module.getClass() == RendererModule.class) {
									configuration.rendererModule = (RendererModule) module;
								}
								else if (module.getClass() == CollisionModule.class) {
									configuration.collisionModule = (CollisionModule) module;
								}
								module.deserialize(item);
								configuration.modules.add(module);
								break;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		configuration.updateActiveModules();
		return configuration;
	}

	public static void main(String[] sdf) {
		final ParticleSystemConfiguration system = fromScratch();
		SwingUtilities.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				e.printStackTrace();
			}
			JFrame f = new JFrame("ParticleSystemGUI");
			f.setSize(500, 800);
			f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			f.setContentPane(system.getPanel());
			f.setLocationRelativeTo(null);
			f.setVisible(true);
		});

	}

	private void updateActiveModules() {

		particleUpdateModules.clear();
		for (ParticleSystemModule module : modules) {
			if (module instanceof ParticleUpdateInterface && module.isEnabled()) {
				particleUpdateModules.add((ParticleUpdateInterface) module);
			}
		}

		particleStartModules.clear();
		for (ParticleSystemModule module : modules) {
			if (module instanceof ParticleStartInterface && module.isEnabled()) {
				particleStartModules.add((ParticleStartInterface) module);
			}
		}

		particleColorModules.clear();
		for (ParticleSystemModule module : modules) {
			if (module instanceof ParticleColorInterface && module.isEnabled()) {
				particleColorModules.add((ParticleColorInterface) module);
			}
		}

		particleDeathInterfaces.clear();
		for (ParticleSystemModule module : modules) {
			if (module instanceof ParticleDeathInterface && module.isEnabled()) {
				particleDeathInterfaces.add((ParticleDeathInterface) module);
			}
		}

		particleMoveInterfaces.clear();
		for (ParticleSystemModule module : modules) {
			if (module instanceof ParticleMoveInterface && module.isEnabled()) {
				particleMoveInterfaces.add((ParticleMoveInterface) module);
			}
		}
	}

	protected JPanel getPanel() {
		JPanel sup = new JPanel();
		sup.setLayout(new GridBagLayout());
		JScrollPane pane = new JScrollPane();
		pane.setLayout(new ScrollPaneLayout());
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		sup.add(pane);
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		for (int i = 0; i < modules.size(); i++) {
			{
				GridBagConstraints gbc_equationDisplay = new GridBagConstraints();
				gbc_equationDisplay.weighty = 1.0;
				gbc_equationDisplay.weightx = 1.0;
				gbc_equationDisplay.fill = GridBagConstraints.BOTH;
				gbc_equationDisplay.gridx = 0;
				gbc_equationDisplay.gridy = i;
				p.add(modules.get(i).getPanel(), gbc_equationDisplay);
			}
		}
		pane.setViewportView(p);
		{
			GridBagConstraints gbc_equationDisplay = new GridBagConstraints();
			gbc_equationDisplay.weighty = 1.0;
			gbc_equationDisplay.weightx = 1.0;
			gbc_equationDisplay.fill = GridBagConstraints.BOTH;
			gbc_equationDisplay.gridx = 0;
			gbc_equationDisplay.gridy = 0;
			sup.add(pane, gbc_equationDisplay);
		}

		return sup;
	}

	public void save(File file) {
		try {
			// ///////////////////////////
			// Creating an empty XML Document

			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			org.w3c.dom.Element root = doc.createElement("ParticleSystem");
			root.setAttribute("name", name);
			root.setAttribute("parent", parent);

			// //////////////////////
			// Creating the XML tree

			Comment comment = doc
					.createComment("autocreated by the starmade particle system editor");
			root.appendChild(comment);

			for (ParticleSystemModule m : modules) {
				root.appendChild(m.serialize(doc));
			}
			// create the root element and add it to the document

			doc.appendChild(root);
			doc.setXmlVersion("1.0");

			// create a comment and put it in the root element

			// ///////////////
			// Output the XML

			// set up a transformer
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			// create string from xml tree
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(file);
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);
			String xmlString = sw.toString();

			// print xml
			System.out.println("Here's the xml:\n\n" + xmlString);
		} catch (Exception e) {
			e.printStackTrace();
			GLFrame.processErrorDialogException(e, null);
		}
	}

	public int getMaxParticles() {
		return initialModule.getMaxParticles();
	}

	public int getParticleCount() {
		return initialModule.getParticleCount();
	}

	public void setParticleCount(int i) {
		initialModule.setParticleCount(i);
	}

	public float getSystemTime() {
		return initialModule.getTime();
	}

	public void handleParticleUpdate(Physics physics, Timer timer, ParticleContainer p) {

		assert (initialModule.getParticleLifetimeDiv() > 0);
		p.lifetime -= (timer.getDelta() * initialModule.getDuration()) * initialModule.getParticleLifetimeDiv();
		if (p.lifetime > 0) {
			for (ParticleUpdateInterface updateInterface : particleUpdateModules) {
				updateInterface.onParticleUpdate(timer, p);
			}
			for (ParticleMoveInterface updateInterface : particleMoveInterfaces) {
				updateInterface.onParticleMove(physics, timer, p);
			}
			if (collisionModule == null || !collisionModule.isEnabled()) {
				velocityTmp.set(p.velocity);
				velocityTmp.scale(timer.getDelta());
				p.position.add(velocityTmp);
			}
		} else {
			for (ParticleDeathInterface deathInterface : particleDeathInterfaces) {
				deathInterface.onParticleDeath(p);
			}
		}
	}

	public void start() {
		initialModule.start();
	}

	public boolean spawn(Timer timer, float[] rawParticles, Transform systemTransform) {

		if (initialModule.getMaxParticles() * ParticleProperty.getPropertyCount() != rawParticles.length) {
			return false;
		}
		if (moduleEnabledChanged) {
			updateActiveModules();
			moduleEnabledChanged = false;
		}

		int particlesToSpawn = 0;
		if (initialModule.isEnabled()) {
			particlesToSpawn += initialModule.getParticlesToSpawn();
		}
		initialModule.updateTimes(timer);
		if (emissionModule.isEnabled()) {
			particlesToSpawn += emissionModule.getParticlesToSpawn(timer);
		}

		particlesToSpawn = Math.min(particlesToSpawn, Math.max(0, initialModule.getMaxParticles() - getParticleCount()));

		for (int i = 0; i < particlesToSpawn; i++) {
			tmp.reset();
			for (ParticleStartInterface startInterface : particleStartModules) {
				startInterface.onParticleSpawn(tmp, systemTransform);
			}
			assert (tmp.lifetime > 0);
			Particle.add(tmp, this, rawParticles);
		}
		return true;
	}

	public void draw(ParticleSystem rawParticles, ParticleVertexBuffer buffer) {
		rendererModule.draw(rawParticles, buffer);
	}

	public boolean isModuleEnabledChanged() {
		return moduleEnabledChanged;
	}

	public void setModuleEnabledChanged(boolean moduleEnabledChanged) {
		this.moduleEnabledChanged = moduleEnabledChanged;
	}

	public float getParticleSystemDuration() {
		return initialModule.getParticleSystemDuration();
	}

	public void getColor(Vector4f color, ParticleContainer p) {

		for (ParticleColorInterface colorInterface : particleColorModules) {
			colorInterface.onParticleColor(color, p);
		}
	}

	public Random getRandom() {
		return random;
	}

	public long getSeed() {
		return initialModule.getRandomSeed();
	}
}
