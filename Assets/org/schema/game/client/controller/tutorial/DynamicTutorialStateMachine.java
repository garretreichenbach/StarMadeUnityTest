package org.schema.game.client.controller.tutorial;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.tutorial.states.ActivateBlockTestState;
import org.schema.game.client.controller.tutorial.states.ActivateGravityTestState;
import org.schema.game.client.controller.tutorial.states.AssignWeaponTestState;
import org.schema.game.client.controller.tutorial.states.ConnectedFromToTestState;
import org.schema.game.client.controller.tutorial.states.CreateShipTestState;
import org.schema.game.client.controller.tutorial.states.DestroyEntityTestState;
import org.schema.game.client.controller.tutorial.states.EnterLastSpawnedShipTestState;
import org.schema.game.client.controller.tutorial.states.EnterUIDSegmentControllerTestState;
import org.schema.game.client.controller.tutorial.states.InstantiateInventoyTestState;
import org.schema.game.client.controller.tutorial.states.OpenChestTestState;
import org.schema.game.client.controller.tutorial.states.PlaceElementOnLastSpawnedTestState;
import org.schema.game.client.controller.tutorial.states.PlaceElementTestState;
import org.schema.game.client.controller.tutorial.states.PutInChestTestState;
import org.schema.game.client.controller.tutorial.states.RestoreInventoyTestState;
import org.schema.game.client.controller.tutorial.states.SatisfyingCondition;
import org.schema.game.client.controller.tutorial.states.SelectProductionTestState;
import org.schema.game.client.controller.tutorial.states.ShipBuildControllerTestState;
import org.schema.game.client.controller.tutorial.states.ShipFlightControllerTestState;
import org.schema.game.client.controller.tutorial.states.SkipTestState;
import org.schema.game.client.controller.tutorial.states.TalkToNPCTestState;
import org.schema.game.client.controller.tutorial.states.TeleportToTestState;
import org.schema.game.client.controller.tutorial.states.TeleportToTutorialSector;
import org.schema.game.client.controller.tutorial.states.TextState;
import org.schema.game.client.controller.tutorial.states.TutorialEnded;
import org.schema.game.client.controller.tutorial.states.TutorialEndedTextState;
import org.schema.game.client.controller.tutorial.states.TutorialFailedState;
import org.schema.game.client.controller.tutorial.states.TypeInInventoryTestState;
import org.schema.game.client.controller.tutorial.states.TypeInPersonalCapsuleRefineryTestState;
import org.schema.game.client.controller.tutorial.states.TypeInPersonalFactoryAssemberTestState;
import org.schema.game.client.controller.tutorial.states.WaitingTextState;
import org.schema.game.client.controller.tutorial.states.WeaponPanelClosedTestState;
import org.schema.game.client.controller.tutorial.states.WeaponPanelOpenTestState;
import org.schema.game.client.controller.tutorial.states.conditions.TutorialCondition;
import org.schema.game.client.controller.tutorial.states.conditions.TutorialConditionBlockExists;
import org.schema.game.client.controller.tutorial.states.conditions.TutorialConditionChestOpen;
import org.schema.game.client.controller.tutorial.states.conditions.TutorialConditionInBuildMode;
import org.schema.game.client.controller.tutorial.states.conditions.TutorialConditionInFlightMode;
import org.schema.game.client.controller.tutorial.states.conditions.TutorialConditionInLastShip;
import org.schema.game.client.controller.tutorial.states.conditions.TutorialConditionInShipUID;
import org.schema.game.client.controller.tutorial.states.conditions.TutorialConditionProductionSet;
import org.schema.game.client.controller.tutorial.states.conditions.TutorialConditionWeaponPanelOpen;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.ai.MachineProgram;
import org.schema.schine.ai.stateMachines.AiEntityState;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.Message;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.resource.FileExt;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class DynamicTutorialStateMachine extends FiniteStateMachine<String> {

	/**
	 * The Constant serialVersionUID.
	 */
	
	private State tutorialStartState;
	private State tutorialEndState;
	private TutorialEnded tutorialEnded;
	private TutorialFailedState tutorialFailed;
	private String tutorialName;

	/**
	 * Instantiates a new standart unit machine.
	 *
	 * @param obj    the obj
	 * @param subDir
	 */
	public DynamicTutorialStateMachine(AiEntityState obj, MachineProgram<?> p, String path, File subDir) {
		super(obj, p, path);

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.stateMachines.FiniteStateMachine#createFSM()
	 */
	@Override
	public void createFSM(String path) {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;

		this.tutorialName = (new FileExt(path)).getParentFile().getName();


		GameClientState state = getMachineProgram().getState();
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new FileExt(path));

			

			Element root = doc.getDocumentElement();
			String description = root.getAttribute("description");

			NodeList startTag = root.getElementsByTagName("Start");

			NodeList endTag = root.getElementsByTagName("End");

			if (startTag.getLength() == 0 || endTag.getLength() == 0) {
				throw new TutorialException("Start or End state not found");
			}
			Object2ObjectOpenHashMap<String, String> translations = TutorialMode.translation.get(description);
//			assert(translations != null):description+"\n"+TutorialMode.translation;
			tutorialFailed = new TutorialFailedState(getObj(), state);
			tutorialEnded = new TutorialEnded(getObj());

			tutorialStartState = parseState(startTag.item(0), state, translations);
			tutorialEndState = parseState(endTag.item(0), state, translations);

			Object2ObjectOpenHashMap<String, State> stateMap = new Object2ObjectOpenHashMap<String, State>();
			stateMap.put("start", tutorialStartState);
			stateMap.put("end", tutorialEndState);

			NodeList childNodes = root.getChildNodes();

			for (int i = 0; i < childNodes.getLength(); i++) {
				Node child = childNodes.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					if (!child.getNodeName().toLowerCase(Locale.ENGLISH).equals("start") && !child.getNodeName().toLowerCase(Locale.ENGLISH).equals("end")) {

						State parseState = parseState(child, state, translations);
						stateMap.put(child.getNodeName().toLowerCase(Locale.ENGLISH), parseState);
					}
				}
			}
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node child = childNodes.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					parseTransitions(child.getNodeName().toLowerCase(Locale.ENGLISH), child, stateMap, state);
				}
			}
			tutorialStartState.addTransition(Transition.TUTORIAL_END, tutorialEndState);
			tutorialStartState.addTransition(Transition.TUTORIAL_FAILED, tutorialFailed);
			tutorialStartState.addTransition(Transition.TUTORIAL_STOP, tutorialEnded);
			tutorialEndState.addTransition(Transition.TUTORIAL_RESTART, tutorialStartState);
			tutorialEndState.addTransition(Transition.TUTORIAL_FAILED, tutorialFailed);
			tutorialEndState.addTransition(Transition.CONDITION_SATISFIED, tutorialEnded);
			tutorialEnded.addTransition(Transition.TUTORIAL_RESTART, tutorialStartState);
			tutorialFailed.addTransition(Transition.TUTORIAL_FAILED, tutorialEnded);

		} catch (SAXException e) {
			e.printStackTrace();
			throw new TutorialException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new TutorialException(e);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new TutorialException(e);
		}

		tutorialEndState.addTransition(Transition.CONDITION_SATISFIED, tutorialEnded);
		setStartingState(tutorialStartState);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.stateMachines.FiniteStateMachine#getMachineProgram()
	 */
	@Override
	public TutorialMode getMachineProgram() {
		return (TutorialMode) super.getMachineProgram();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.stateMachines.FiniteStateMachine#onMsg(org.schema.schine.ai.stateMachines.Message)
	 */
	@Override
	public void onMsg(Message message) {
		
	}

	private void parseTransitions(String nodeName, Node node,
	                              Object2ObjectOpenHashMap<String, State> stateMap,
	                              GameClientState state) {
		NodeList childNodes = node.getChildNodes();
		String text = null;
		int duration = -1;

		State stateFrom = stateMap.get(nodeName.toLowerCase(Locale.ENGLISH));
		stateFrom.addTransition(Transition.TUTORIAL_STOP, tutorialEnded);
		stateFrom.addTransition(Transition.TUTORIAL_END, tutorialEndState);
		stateFrom.addTransition(Transition.TUTORIAL_RESTART, tutorialStartState);
		stateFrom.addTransition(Transition.TUTORIAL_FAILED, tutorialFailed);

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				if (child.getNodeName().toLowerCase(Locale.ENGLISH).equals("satisfied")) {
					String c = child.getTextContent().toLowerCase(Locale.ENGLISH);
					State stateTo = stateMap.get(c);
					if (child.getParentNode().getNodeName().toLowerCase(Locale.ENGLISH).equals(c)) {
						throw new TutorialException("Transition state not found for " + child.getNodeName() + "->" + child.getParentNode().getNodeName() + " can't point to itself");
					}
					if (stateTo == null) {
						throw new TutorialException("Transition state not found for " + child.getNodeName() + "->" + child.getParentNode().getNodeName() + " searched: " + c + " (Available: " + stateMap.keySet() + ")");
					}
					stateFrom.addTransition(Transition.CONDITION_SATISFIED, stateTo);
				}
				if (child.getNodeName().toLowerCase(Locale.ENGLISH).equals("back")) {
					String c = child.getTextContent().toLowerCase(Locale.ENGLISH);
					State stateTo = stateMap.get(c);
					if (child.getParentNode().getNodeName().toLowerCase(Locale.ENGLISH).equals(c)) {
						throw new TutorialException("Transition state not found for " + child.getNodeName() + "->" + child.getParentNode().getNodeName() + " can't point to itself");
					}
					if (stateTo == null) {
						throw new TutorialException("Transition state not found for " + child.getNodeName() + "->" + child.getParentNode().getNodeName());
					}
					stateFrom.addTransition(Transition.BACK, stateTo);
				}
				if (child.getNodeName().toLowerCase(Locale.ENGLISH).equals("backback")) {
					String c = child.getTextContent().toLowerCase(Locale.ENGLISH);
					State stateTo = stateMap.get(c);
					if (child.getParentNode().getNodeName().toLowerCase(Locale.ENGLISH).equals(c)) {
						throw new TutorialException("Transition state not found for " + child.getNodeName() + "->" + child.getParentNode().getNodeName() + " can't point to itself");
					}
					if (stateTo == null) {
						throw new TutorialException("Transition state not found for " + child.getNodeName() + "->" + child.getParentNode().getNodeName());
					}
					stateFrom.addTransition(Transition.BACK, stateTo);
				}

				if (child.getNodeName().toLowerCase(Locale.ENGLISH).equals("condition")) {
					if (child.getAttributes().getNamedItem("failTo") == null) {
						throw new TutorialException("Condition needs attribute: 'failTo': '" + child.getAttributes() + "' " + child.getNodeName() + "->" + child.getParentNode().getNodeName());
					}

					String failTo = child.getAttributes().getNamedItem("failTo").getNodeValue();
					State stateTo = stateMap.get(failTo.toLowerCase(Locale.ENGLISH));
					if (child.getParentNode().getNodeName().toLowerCase(Locale.ENGLISH).equals(failTo.toLowerCase(Locale.ENGLISH))) {
						throw new TutorialException("Transition state not found for " + child.getNodeName() + "->" + child.getParentNode().getNodeName() + " can't point to itself");
					}
					if (stateTo == null) {
						throw new TutorialException("Condition fail to state doesnt exist: '" + failTo + "' " + child.getAttributes() + "' " + child.getNodeName() + "->" + child.getParentNode().getNodeName());
					}
					TutorialCondition c;
					if (child.getTextContent().toLowerCase(Locale.ENGLISH).equals("inshipuid")) {
						if (child.getAttributes().getNamedItem("uid") == null) {
							throw new TutorialException("Condition needs attribute: 'uid': '" + child.getAttributes() + "' " + child.getNodeName() + "->" + child.getParentNode().getNodeName());
						}
						String shipUid = child.getAttributes().getNamedItem("uid").getNodeValue();

						c = new TutorialConditionInShipUID(stateFrom, stateTo, shipUid);

					} else if (child.getTextContent().toLowerCase(Locale.ENGLISH).equals("inflightmode")) {
						c = new TutorialConditionInFlightMode(stateFrom, stateTo);
					} else if (child.getTextContent().toLowerCase(Locale.ENGLISH).equals("inbuildmode")) {
						c = new TutorialConditionInBuildMode(stateFrom, stateTo);
					} else if (child.getTextContent().toLowerCase(Locale.ENGLISH).equals("inlastspawnedship")) {
						c = new TutorialConditionInLastShip(stateFrom, stateTo);
					} else if (child.getTextContent().toLowerCase(Locale.ENGLISH).equals("weaponpanelopen")) {
						c = new TutorialConditionWeaponPanelOpen(stateFrom, stateTo);
					} else if (child.getTextContent().toLowerCase(Locale.ENGLISH).equals("chestopen")) {
						if (child.getAttributes().getNamedItem("pos") == null) {
							throw new TutorialException("Condition needs attribute: 'pos': '" + child.getAttributes() + "' " + child.getNodeName() + "->" + child.getParentNode().getNodeName());
						}
						try {
							String posStr = child.getAttributes().getNamedItem("pos").getNodeValue();
							String[] cs = posStr.split(",", 3);
							Vector3i pos = new Vector3i(Integer.parseInt(cs[0].trim()), Integer.parseInt(cs[1].trim()), Integer.parseInt(cs[2].trim()));

							c = new TutorialConditionChestOpen(stateFrom, stateTo, pos);
						} catch (Exception e) {
							e.printStackTrace();
							throw new TutorialException("Failed: " + child.getAttributes() + "' " + child.getNodeName() + "->" + child.getParentNode().getNodeName(), e);
						}
					} else if (child.getTextContent().toLowerCase(Locale.ENGLISH).equals("blockexists")) {
						if (child.getAttributes().getNamedItem("pos") == null) {
							throw new TutorialException("Condition needs attribute: 'pos': '" + child.getAttributes() + "' " + child.getNodeName() + "->" + child.getParentNode().getNodeName());
						}
						try {
							String posStr = child.getAttributes().getNamedItem("pos").getNodeValue();
							String[] cs = posStr.split(",", 3);
							Vector3i pos = new Vector3i(Integer.parseInt(cs[0].trim()), Integer.parseInt(cs[1].trim()), Integer.parseInt(cs[2].trim()));

							c = new TutorialConditionBlockExists(stateFrom, stateTo, pos);
						} catch (Exception e) {
							e.printStackTrace();
							throw new TutorialException("Failed: " + child.getAttributes() + "' " + child.getNodeName() + "->" + child.getParentNode().getNodeName(), e);
						}
					} else if (child.getTextContent().toLowerCase(Locale.ENGLISH).equals("setproduction")) {
						if (child.getAttributes().getNamedItem("pos") == null) {
							throw new TutorialException("Condition needs attribute: 'pos': '" + child.getAttributes() + "' " + child.getNodeName() + "->" + child.getParentNode().getNodeName());
						}
						if (child.getAttributes().getNamedItem("type") == null) {
							throw new TutorialException("Condition needs attribute: 'type': '" + child.getAttributes() + "' " + child.getNodeName() + "->" + child.getParentNode().getNodeName());
						}

						try {
							short type = Short.parseShort(child.getAttributes().getNamedItem("type").getNodeValue());
							String posStr = child.getAttributes().getNamedItem("pos").getNodeValue();
							String[] cs = posStr.split(",", 3);
							Vector3i pos = new Vector3i(Integer.parseInt(cs[0].trim()), Integer.parseInt(cs[1].trim()), Integer.parseInt(cs[2].trim()));

							c = new TutorialConditionProductionSet(stateFrom, stateTo, pos, type);
						} catch (Exception e) {
							e.printStackTrace();
							throw new TutorialException("Failed: " + child.getAttributes() + "' " + child.getNodeName() + "->" + child.getParentNode().getNodeName(), e);
						}
					} else {
						throw new TutorialException("Condition type not found: '" + child.getTextContent() + "' " + child.getNodeName() + "->" + child.getParentNode().getNodeName());
					}

					((SatisfyingCondition) stateFrom).getTutorialConditions().add(c);
				}
			}
		}
	}

	public String getAttrib(Node node, String name) {
		NamedNodeMap attributes = node.getAttributes();
		Node typeNode = attributes.getNamedItem(name);
		if (typeNode == null) {
			throw new TutorialException("attribute \"" + name + "\" not found in " + node.getNodeName());
		}

		return typeNode.getNodeValue();
	}

	private State parseState(Node node, GameClientState state, Object2ObjectOpenHashMap<String, String> translations) {

		String type = getAttrib(node, "type");
		SatisfyingCondition s = null;
		NodeList childNodes = node.getChildNodes();
		String stateName = node.getNodeName();
		String text = null;
		int duration = -1;
		String image = null;
		String condition = null;
		Vector3i blockPosA = null;
		Vector3i blockPosB = null;
		String structureUID = null;
		String npcName = null;
		Boolean clear = null;
		boolean recordPosition = false;
		short typeA = -1;
		short typeB = -1;
		int slot = 1;

		boolean limitedBlockSupply = false;
		boolean takeAsCurrentContext = false;
		boolean active = false;
		boolean skipWindowMessage = false;
		int count = 1;

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				try {
					if (child.getNodeName().toLowerCase(Locale.ENGLISH).equals("main/content")) {
						
						String translation = null;
						if(translations != null){
							translation = translations.get(stateName);
						}
						if(translation != null){
							text = formatText(translation, state);
						}else{
							text = formatText(child.getTextContent(), state);
						}
					} else if (child.getNodeName().toLowerCase(Locale.ENGLISH).equals("duration")) {
						duration = Integer.parseInt(child.getTextContent());
					} else if (child.getNodeName().toLowerCase(Locale.ENGLISH).equals("image")) {
						image = tutorialName + "_" + child.getTextContent();
					} else if (child.getNodeName().toLowerCase(Locale.ENGLISH).equals("npcname")) {
						npcName = child.getTextContent();
					} else if (child.getNodeName().toLowerCase(Locale.ENGLISH).equals("structureuid")) {
						structureUID = child.getTextContent();
					} else if (child.getNodeName().toLowerCase(Locale.ENGLISH).equals("typea")) {
						typeA = Short.parseShort(child.getTextContent());
					} else if (child.getNodeName().toLowerCase(Locale.ENGLISH).equals("count")) {
						count = Integer.parseInt(child.getTextContent());
					} else if (child.getNodeName().toLowerCase(Locale.ENGLISH).equals("clear")) {
						clear = Boolean.parseBoolean(child.getTextContent());
					} else if (child.getNodeName().toLowerCase(Locale.ENGLISH).equals("recordposition")) {
						recordPosition = Boolean.parseBoolean(child.getTextContent());
					} else if (child.getNodeName().toLowerCase(Locale.ENGLISH).equals("skipwindowmessage")) {
						skipWindowMessage = Boolean.parseBoolean(child.getTextContent());
					} else if (child.getNodeName().toLowerCase(Locale.ENGLISH).equals("slot")) {
						slot = Integer.parseInt(child.getTextContent());
						if (slot < 0 || slot > 10) {
							throw new TutorialException("At " + node.getNodeName() + "; " + node.getParentNode().getNodeName() + ": slot invalid: must be [0-10]: but was" + slot);
						}
					} else if (child.getNodeName().toLowerCase(Locale.ENGLISH).equals("typeb")) {
						typeB = Short.parseShort(child.getTextContent());
					} else if (child.getNodeName().toLowerCase(Locale.ENGLISH).equals("limitedblocksupply")) {
						limitedBlockSupply = Boolean.parseBoolean(child.getTextContent());
					} else if (child.getNodeName().toLowerCase(Locale.ENGLISH).equals("active")) {
						active = Boolean.parseBoolean(child.getTextContent());
					} else if (child.getNodeName().toLowerCase(Locale.ENGLISH).equals("takeascurrentcontext")) {
						takeAsCurrentContext = Boolean.parseBoolean(child.getTextContent());
					} else if (child.getNodeName().toLowerCase(Locale.ENGLISH).equals("blockposa")) {
						String[] split = child.getTextContent().split(",", 3);
						blockPosA = new Vector3i(Integer.parseInt(split[0].trim()), Integer.parseInt(split[1].trim()), Integer.parseInt(split[2].trim()));
					} else if (child.getNodeName().toLowerCase(Locale.ENGLISH).equals("blockposb")) {
						String[] split = child.getTextContent().split(",", 3);
						blockPosB = new Vector3i(Integer.parseInt(split[0].trim()), Integer.parseInt(split[1].trim()), Integer.parseInt(split[2].trim()));
					}
				} catch (Exception e) {
					throw new TutorialException("At " + node.getNodeName(), e);
				}
			}
		}

		if (text == null) {
			throw new TutorialException("At " + node.getNodeName() + "; no <Content> tag found");
		}
		if (duration < 0) {
			throw new TutorialException("At " + node.getNodeName() + "; no <Duration> tag found or duration negative");
		}

		if (type.toLowerCase(Locale.ENGLISH).equals("placeblockonlastspawned")) {
			if (!ElementKeyMap.exists(typeA)) {
				throw new TutorialException("At " + node.getNodeName() + "; <typeA> must be specified and valid");
			}
			s = new PlaceElementOnLastSpawnedTestState(getObj(), state, text, typeA, blockPosA, limitedBlockSupply, count);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("openchest")) {
			if (blockPosA == null) {
				throw new TutorialException("At " + node.getNodeName() + "; <blockPosA> must be specified and valid");
			}
			s = new OpenChestTestState(getObj(), text, state, blockPosA);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("placeblock")) {
			if (!ElementKeyMap.exists(typeA)) {
				throw new TutorialException("At " + node.getNodeName() + "; <typeA> must be specified and valid");
			}
			s = new PlaceElementTestState(getObj(), state, text, typeA, blockPosA, limitedBlockSupply, count);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("putinchest")) {
			if (!ElementKeyMap.exists(typeA)) {
				throw new TutorialException("At " + node.getNodeName() + "; <typeA> must be specified and valid");
			}
			if (blockPosA == null) {
				throw new TutorialException("At " + node.getNodeName() + "; <blockPosA> must be specified and valid");
			}
			s = new PutInChestTestState(getObj(), state, text, typeA, blockPosA, limitedBlockSupply, count);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("selectproductionon")) {
			if (!ElementKeyMap.exists(typeA)) {
				throw new TutorialException("At " + node.getNodeName() + "; <typeA> must be specified and valid");
			}
			if (blockPosA == null) {
				throw new TutorialException("At " + node.getNodeName() + "; <blockPosA> must be specified and valid");
			}
			s = new SelectProductionTestState(getObj(), text, state, blockPosA, typeA);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("assignweaponslot")) {
			if (!ElementKeyMap.exists(typeA)) {
				throw new TutorialException("At " + node.getNodeName() + "; <typeA> must be specified and valid");
			}
			s = new AssignWeaponTestState(getObj(), text, state, typeA, slot);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("weaponpanelopen")) {
			s = new WeaponPanelOpenTestState(getObj(), text, state);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("teleporttotutorialsector")) {
			if (blockPosA == null) {
				throw new TutorialException("At " + node.getNodeName() + "; <blockPosA> must be specified and valid");
			}
			if (structureUID == null) {
				throw new TutorialException("At " + node.getNodeName() + "; <ShipUID> must be specified and valid");
			}
			s = new TeleportToTutorialSector(getObj(), text, state, structureUID, blockPosA);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("teleportto")) {
			if (blockPosA == null) {
				throw new TutorialException("At " + node.getNodeName() + "; <blockPosA> must be specified and valid");
			}
			if (structureUID == null) {
				throw new TutorialException("At " + node.getNodeName() + "; <ShipUID> must be specified and valid");
			}
			s = new TeleportToTestState(getObj(), text, state, structureUID, blockPosA);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("destroyentity")) {
			if (structureUID == null) {
				throw new TutorialException("At " + node.getNodeName() + "; <ShipUID> must be specified and valid");
			}
			s = new DestroyEntityTestState(getObj(), text, state, structureUID);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("tutorialcompleted")) {

			s = new TutorialEndedTextState(getObj(), state, text, duration);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("typeininventory")) {
			if (!ElementKeyMap.exists(typeA)) {
				throw new TutorialException("At " + node.getNodeName() + "; <typeA> must be specified and valid");
			}
			s = new TypeInInventoryTestState(getObj(), text, state, typeA, count);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("typeinpersonalcapsulerefinery")) {
			if (!ElementKeyMap.exists(typeA)) {
				throw new TutorialException("At " + node.getNodeName() + "; <typeA> must be specified and valid");
			}
			s = new TypeInPersonalCapsuleRefineryTestState(getObj(), text, state, typeA, count);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("typeinpersonalfactoryassember")) {
			if (!ElementKeyMap.exists(typeA)) {
				throw new TutorialException("At " + node.getNodeName() + "; <typeA> must be specified and valid");
			}
			s = new TypeInPersonalFactoryAssemberTestState(getObj(), text, state, typeA, count);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("activategravity")) {
			if (structureUID == null) {
				throw new TutorialException("At " + node.getNodeName() + "; <ShipUID> must be specified and valid");
			}
			s = new ActivateGravityTestState(getObj(), text, state, structureUID);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("instantiateinventory")) {
			if (clear == null) {
				throw new TutorialException("At " + node.getNodeName() + "; <Clear> must be specified and valid");
			}
			s = new InstantiateInventoyTestState(getObj(), text, state, clear);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("restoryinventory")) {
			s = new RestoreInventoyTestState(getObj(), text, state);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("weaponpanelclosed")) {
			s = new WeaponPanelClosedTestState(getObj(), text, state);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("talktonpc")) {
			if (npcName == null) {
				throw new TutorialException("At " + node.getNodeName() + "; <NpcName> must be specified and valid");
			}
			s = new TalkToNPCTestState(getObj(), text, state, npcName);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("spawnship")) {
			s = new CreateShipTestState(getObj(), text, state, limitedBlockSupply, false);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("getintolastspawnedship")) {
			s = new EnterLastSpawnedShipTestState(getObj(), text, state);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("connectblocks")) {
			if (blockPosA == null) {
				throw new TutorialException("At " + node.getNodeName() + "; <blockPosA> must be specified and valid");
			}
			if (blockPosB == null) {
				throw new TutorialException("At " + node.getNodeName() + "; <blockPosB> must be specified and valid");
			}
			s = new ConnectedFromToTestState(getObj(), text, state, blockPosA, blockPosB);

		} else if (type.toLowerCase(Locale.ENGLISH).equals("activateflightmode")) {
			s = new ShipFlightControllerTestState(getObj(), text, state);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("activatebuildmode")) {
			s = new ShipBuildControllerTestState(getObj(), text, state);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("activateblock")) {
			if (blockPosA == null) {
				throw new TutorialException("At " + node.getNodeName() + "; <blockPosA> must be specified and valid");
			}
			s = new ActivateBlockTestState(getObj(), text, state, blockPosA, active);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("getinstructureuid")) {
			if (structureUID == null) {
				throw new TutorialException("At " + node.getNodeName() + "; <ShipUID> must be specified and valid");
			}
			if (blockPosA == null) {
				throw new TutorialException("At " + node.getNodeName() + "; <blockPosA> must be specified and valid");
			}
			s = new EnterUIDSegmentControllerTestState(getObj(), text, state, structureUID, blockPosA);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("skip")) {
			s = new SkipTestState(getObj(), state);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("waitingtext")) {
			s = new WaitingTextState(getObj(), state, text, duration);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("text")) {
			s = new TextState(getObj(), state, text, duration);
		}

		if (s == null) {
			throw new TutorialException("Unknown type: " + type);
		}
		if (image != null) {
			if (Controller.getResLoader().getSprite(image) != null) {
				((TextState) s).setImage(Controller.getResLoader().getSprite(image));
			}
		}

		s.setSkipWindowMessage(skipWindowMessage);

		return s;
	}

	private String formatText(String textContent, GameClientState state) {
		return KeyboardMappings.formatText(textContent).replaceAll("\\$PLAYER", state.getPlayerName());
	}

	/**
	 * @return the tutorialEndState
	 */
	public State getTutorialEndedState() {
		return tutorialEnded;
	}

	/**
	 * @return the tutorialEndState
	 */
	public State getTutorialEndState() {
		return tutorialEndState;
	}

	/**
	 * @return the tutorialStartState
	 */
	public State getTutorialStartState() {
		return tutorialStartState;
	}

}
