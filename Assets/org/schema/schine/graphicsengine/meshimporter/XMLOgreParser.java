/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>XMLOgreParser</H2>
 * <H3>org.schema.schine.graphicsengine.meshimporter</H3>
 * XMLOgreParser.java
 * <HR>
 * Description goes here. If you see this message, please contact me and the
 * description will be filled.<BR>
 * <BR>
 *
 * @author Robin Promesberger (schema)
 * @mail <A HREF="mailto:schemaxx@gmail.com">schemaxx@gmail.com</A>
 * @site <A
 * HREF="http://www.the-schema.com/">http://www.the-schema.com/</A>
 * @project JnJ / VIR / Project R
 * @homepage <A
 * HREF="http://www.the-schema.com/JnJ">
 * http://www.the-schema.com/JnJ</A>
 * @copyright Copyright � 2004-2010 Robin Promesberger (schema)
 * @licence Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.schema.schine.graphicsengine.meshimporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Quat4Util;
import org.schema.schine.graphicsengine.animation.Animation;
import org.schema.schine.graphicsengine.animation.AnimationTrack;
import org.schema.schine.graphicsengine.animation.BoneAnimationTrack;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.ResourceException;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.AnimationNotFoundException;
import org.schema.schine.graphicsengine.forms.Bone;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.graphicsengine.forms.Geometry;
import org.schema.schine.graphicsengine.forms.Line;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.forms.Mesh.Face;
import org.schema.schine.graphicsengine.forms.MeshGroup;
import org.schema.schine.graphicsengine.forms.OldTerrain;
import org.schema.schine.graphicsengine.forms.SceneNode;
import org.schema.schine.graphicsengine.forms.Skeleton;
import org.schema.schine.graphicsengine.forms.Skin;
import org.schema.schine.graphicsengine.forms.VertexBoneWeight;
import org.schema.schine.graphicsengine.forms.WeightsBuffer;
import org.schema.schine.graphicsengine.texture.Material;
import org.schema.schine.resource.ResourceLoader;
import org.schema.schine.xmlparser.XMLAttribute;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;


/**
 * This class parses and converts XML Files exported with the ogremax Exporter.
 * This file-format is a lot better than 3ds or wavefront. all .mesh.xml files
 * have to be in the same directory a the .scene file you want to read. the
 * files are normally in triangle format.
 *
 * @author schema
 */
public class XMLOgreParser{

	public static final int TYPE_MESH = 0;
	public static final int TYPE_TERRAIN = 1;
	/**
	 * The Constant sNEWLINE.
	 */
	static final String sNEWLINE = System.getProperty("line.separator");

	// ---- main ----
	/**
	 * The matTmp map.
	 */
	Map<String, Material> matMap = new Object2ObjectOpenHashMap<String, Material>();
	/**
	 * The scene path.
	 */
	private String scenePath;
	
	public boolean recordArrays;
	/**
	 * The scene file.
	 */
	private String sceneFile;
	/**
	 * The materials.
	 */
	private Material[] materials;
	private int bufferIndex;

	/**
	 * Instantiates a new xML ogre parser.
	 */
	public XMLOgreParser() {
	}

	/**
	 * The main method.
	 *
	 * @param argv the arguments
	 * @throws ResourceException the resource exception
	 * @
	 */
	public static void main(String[] argv) throws ResourceException {
		// System.err.println(argv[0]+", "+ argv[1]);
		XMLOgreParser handler = new XMLOgreParser();
		String path = "ogretest/temple-machine.scene";

		AbstractSceneNode w = handler.parseScene(path, "");

	}



	/**
	 * Parses the animation.
	 *
	 * @param c the c
	 * @return the animation
	 */
	private Animation parseAnimation(XMLOgreContainer c) {
		Animation a = new Animation();
		int framecount = c.childs.size();

		for (XMLAttribute att : c.attribs) {
			if (att.name.equals("name")) {
				a.setName(att.value);
				System.err.println("... Animation name " + a.getName());
			}
			if (att.name.equals("loop")) {
				a.setLoop(Boolean.parseBoolean(att.value));
			}
			// interpolationMode="spline" rotationInterpolationMode="spherical"
			// length="6.66687"
			if (att.name.equals("interpolationMode")) {
				// TODO
			}
			if (att.name.equals("rotationInterpolationMode")) {
				// TODO
			}
			if (att.name.equals("length")) {
				// TODO
			}
		}
		parseKeyFrames(c, framecount);
		//System.err.println("parsing animation "+a.getName()+" in container "+c
		// .name);

		return a;
	}

	/**
	 * Parses the animations.
	 *
	 * @param c the c
	 * @return the vector
	 */
	private Vector<Animation> parseAnimations(XMLOgreContainer c) {
		Vector<Animation> anims = new Vector<Animation>();
		for (XMLOgreContainer animation : c.childs) {
			if (animation.name.equals("animation")) {
				anims.add(parseAnimation(animation));
			}
		}
		return anims;
	}

	/**
	 * Parses the bone animation.
	 *
	 * @param c the c
	 * @return the vector
	 */
	private Animation parseBoneAnimation(XMLOgreContainer c) {
		Animation a = null;
		String name = null;
		boolean loop = true;
		float animLength = 0;

		for (XMLAttribute att : c.attribs) {
			if (att.name.equals("name")) {
				name = (att.value);
				// System.err.println("... Animation description "+a.getName());
			}
			if (att.name.equals("loop")) {
				loop = (Boolean.parseBoolean(att.value));
			}
			// interpolationMode="spline" rotationInterpolationMode="spherical"
			// length="6.66687"
			if (att.name.equals("interpolationMode")) {
				// TODO
			}
			if (att.name.equals("rotationInterpolationMode")) {
				// TODO
			}
			if (att.name.equals("length")) {
				animLength = Float.parseFloat(att.value);
			}
		}

		//System.err.println("parsing animation "+a.getName()+" in container "+c
		// .name);
		a = new Animation();
		a.setName(name);
		a.setLoop(loop);
		a.animationLength = animLength;
		for (XMLOgreContainer animationContainer : c.childs) {
			// System.err.println(animation);
			if (animationContainer.name.equals("tracks")) {
				// System.err.println("parsing tracks ");
				for (XMLOgreContainer track : animationContainer.childs) {
					if (track.name.equals("track")) {
						parseBoneAnimationTrack(a, track);
						//						System.err.println("... Parsed animation track for "+a.getName()+"; ");
					}

				}
			}
		}
		return a;

	}

	/**
	 * Parses the bone animations.
	 *
	 * @param c the c
	 * @return the vector
	 */
	private List<Animation> parseBoneAnimations(XMLOgreContainer c) {
		ArrayList<Animation> anims = new ArrayList<Animation>();
		for (XMLOgreContainer animation : c.childs) {
			if (animation.name.equals("animation")) {
				anims.add(parseBoneAnimation(animation));
			}
		}
		return anims;
	}

	private Animation parseBoneAnimationTrack(Animation a, XMLOgreContainer trackContainer) {
		String boneName = null;
		for (XMLAttribute att : trackContainer.attribs) {
			if (att.name.equals("bone")) {
				boneName = (att.value);
			}
		}
		for (XMLOgreContainer keyframes : trackContainer.childs) {
			assert (boneName != null);
			BoneAnimationTrack track = new BoneAnimationTrack(boneName);
			parseTrackKeyFrames(keyframes, track);
			a.getTracks().put(track.getBoneName(), track);
		}
		return a;
	}

	private void parseBoneAssignments(Mesh mesh, XMLOgreContainer geoAndFacesAndBones) {

		VertexBoneWeight[] vertBoneAss = new VertexBoneWeight[geoAndFacesAndBones.childs
				.size()];
		int i = 0;
		for (XMLOgreContainer bonesAssign : geoAndFacesAndBones.childs) {
			if (bonesAssign.name.equals("vertexboneassignment")) {
				// vertexindex="2067" boneindex="22" weight="1.00000"
				int vertexIndex = -1;
				int boneIndex = -1;
				float weight = -1;
				for (XMLAttribute att : bonesAssign.attribs) {
					String val = att.value;
					if (att.name.equals("vertexindex")) {
						vertexIndex = Integer.parseInt(val);
					}
					if (att.name.equals("boneindex")) {
						boneIndex = Integer.parseInt(val);
					}
					if (att.name.equals("weight")) {
						weight = Float.parseFloat(val);
					}
				}
				VertexBoneWeight vbw = new VertexBoneWeight(
						vertexIndex, boneIndex, weight);
				vertBoneAss[i] = vbw;
				mesh.setVertexBoneAssignments(vertBoneAss);
			}
			i++;
		}
	}

	/**
	 * Parses the bone hierarchy.
	 *
	 * @param skeleton the skeleton
	 * @param c        the c
	 * @param skelname
	 * @throws ResourceException
	 */
	private void parseBoneHierarchy(Skeleton skeleton, XMLOgreContainer c, String skelname) throws ResourceException {
		for (XMLOgreContainer bParent : c.childs) {
			if (bParent.name.equals("boneparent")) {
				String boneName = null;
				String boneParent = null;
				for (XMLAttribute att : bParent.attribs) {
					if (att.name.equals("bone")) {
						boneName = att.value;
					}
					if (att.name.equals("parent")) {
						boneParent = att.value;
					}
				}
				assert (boneName != null);
				// System.err.println("parent of "+boneName+" is "+boneParent);
				for (Bone bone : skeleton.getBones().values()) {
					if (bone.name.equals(boneName)) {
						for (Bone parent : skeleton.getBones().values()) {
							if (parent.name.equals(boneParent)) {
								bone.setParent(parent);
								parent.getChilds().add(bone);
								break;
							}
						}
						break;
					}
				}

			}
		}
		boolean foundRoot = false;
		for (Bone b : skeleton.getBones().values()) {
			if (b.getParent() == null) {
				if (!foundRoot) {
					foundRoot = true;
					skeleton.setRootBone(b);
				} else {
					System.err
							.println("WARNING: more than one skeleton root bone found "
									+ b.name);
					throw new ResourceException("WARNING: more than one skeleton root bone found "
							+ b.name + "; from scene: " + sceneFile + "; skeleton: " + skelname);
				}
			}
		}
	}

	/**
	 * Parses the bones.
	 *
	 * @param skeleton  the skeleton
	 * @param bonesCont the bones cont
	 */
	private void parseBones(Skeleton skeleton, XMLOgreContainer bonesCont) {
		for (XMLOgreContainer c : bonesCont.childs) {
			if (c.name.equals("bone")) {
				String name = null;
				int id = 0;

				for (XMLAttribute att : c.attribs) {
					if (att.name.equals("name")) {
						name = att.value;
					}
					if (att.name.equals("id")) {
						id = Integer.parseInt(att.value);
					}
				}
				Bone b = new Bone(id, name);
				b.setSkeleton(skeleton);
				Vector3f pos = null;
				Vector3f scale = new Vector3f(1, 1, 1);
				AxisAngle4f rot = null;
				for (XMLOgreContainer posrotscale : c.childs) {

					if (posrotscale.name.equals("position")) {

						pos = (parseVector3(posrotscale));
						//System.err.println("parsing "+posrotscale.name+" - "+b
						// .pos);
					}
					if (posrotscale.name.equals("rotation")) {
						rot = new AxisAngle4f();
						for (XMLAttribute att : posrotscale.attribs) {
							String val = att.value;
							if (att.name.equals("angle")) {
								rot.angle = Float.parseFloat(val);
							}
						}
						for (XMLOgreContainer axis : posrotscale.childs) {
							if (axis.name.equals("axis")) {
								Vector3f ax = parseVector3(axis);
								ax.normalize();
								rot.x = ax.x;
								rot.y = ax.y;
								rot.z = ax.z;
							}
						}
					}
					if (posrotscale.name.equals("scale")) {
						scale = parseVector3(posrotscale);
					}

				}
//				System.err.println("BINDING "+sceneFile+" ::: "+Quat4Util.fromAngleNormalAxis(rot.angle, new Vector3f(rot.x, rot.y, rot.z), new Quat4f())+" ::: "+rot.angle+"; "+rot);
				b.setBindTransforms(pos, Quat4Util.fromAngleNormalAxis(rot.angle, new Vector3f(rot.x, rot.y, rot.z), new Quat4f()), scale);
				assert (b.name != null);
				assert (b.boneID >= 0);
				skeleton.getBones().put(b.boneID, b);
				//				 System.err.println("PARSED BONE: "+b);
			}
		}
	}

	/**
	 * Parses the entity.
	 *
	 * @param c the c
	 * @return the geometry
	 * @throws ResourceException the resource exception
	 * @
	 */
	private Geometry parseEntity(XMLOgreContainer c) throws ResourceException {
		Geometry geometry = null;
		String name = "default";
		for (XMLAttribute att : c.attribs) {
			String val = att.value;
			if (att.name.equals("description")) {
				// description
				name = val;
				// System.err.println("... Mesh " + description + " created");
			}
			if (att.name.equals("id")) {
				// id
				// mesh.setID(val);
			}
			if (att.name.equals("castShadows")) {
				// castShadows
				// mesh.setCastShadows(Boolean.parseBoolean(val));
			}
			if (att.name.equals("receiveShadows")) {
				// receive shadows
				// mesh.setReceiveShadows(Boolean.parseBoolean(val));
			}
			if (att.name.equals("meshFile")) {
				assert(this.sceneFile != null);
				assert(this.scenePath != null);
				// meshfile
				geometry = parseMesh(scenePath, val, TYPE_MESH);

				// System.err.println("...... parsing mesh finished");
			}
		}
		geometry.setName(name);
		Material mat = null;
		for (XMLOgreContainer subentities : c.childs) {
			if (subentities.name.equals("subentities")) {
				//parse the subentities
				for (XMLOgreContainer subentity : subentities.childs) {
					for (XMLAttribute att : subentity.attribs) {
						if (att.name.equals("index")) {

						}
						if (att.name.equals("materialName")) {
							if (matMap.containsKey(att.value)) {
								mat = matMap.get(att.value);
							} else {
								// System.err.println("... parsing Material "
								// + scenePath + sceneFile + ".material");
								if (materials == null) {
									materials = parseMaterial(scenePath + sceneFile
											+ ".material");
									for (Material m : materials) {
										// System.err.println("material "+m);
										// System.err.println("matTmp map "+matMap);
										matMap.put(m.getName(), m);
									}
								}
								mat = matMap.get(att.value);
							}
						}
					}
				}
			} else if (subentities.name.equals("userData") && subentities.text != null) {
				//parse the userData if provided
				//				System.err.println("[XMLParser] USERDATA of "+mesh.getName()+": "+subentities.text);
				try {
					DocumentBuilderFactory dbf =
							DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					InputSource is = new InputSource();
					is.setCharacterStream(new StringReader(subentities.text));
					Document doc = db.parse(is);
					geometry.setUserData(doc);
					Node item = doc.getElementsByTagName("Mass").item(0);
					if (item != null) {
						float mass = Float.parseFloat(item.getTextContent());
						geometry.setMass(mass);
					}
				} catch (SAXException e) {
					System.err.println("Exception while parsing userdata from " + geometry.getName());
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}

			}
		}
		geometry.setMaterial(mat);

		return geometry;

	}

	/**
	 * Parses the environment.
	 *
	 * @param c     the c
	 * @param scene the scene
	 */
	private void parseEnvironment(XMLOgreContainer c, AbstractSceneNode scene) {
		for (XMLOgreContainer node : c.childs) {

		}
	}

	/**
	 * Parses the key frames.
	 *
	 * @param c          the c
	 * @param framecount the framecount
	 * @return the pos rot scale
	 * Deprecation: see parseBoneAnimation
	 */
	@Deprecated
	private PosRotScale parseKeyFrames(XMLOgreContainer c, int framecount) {
		PosRotScale prs = new PosRotScale(framecount);
		int i = 0;
		for (XMLOgreContainer keyframes : c.childs) {
			// System.err.println("--"+keyframes.name);
			if (keyframes.name.equals("keyframe")) {

				for (XMLOgreContainer keyframe : keyframes.childs) {

					// System.err.println("-->"+keyframe.name);
					if (keyframe.name.equals("translation")) {
						// position
						prs.pos[i] = parseVector3(keyframe);
					}
					if (keyframe.name.equals("rotation")) {
						// rotation
						prs.rot[i] = parseRot(keyframe);
						// System.err.println("parsed rot: " + rot[i]);
					}
					if (keyframe.name.equals("scale")) {
						// scale
						prs.scale[i] = parseVector3(keyframe);
					}
				}
				i++;
			}
		}
		return prs;
	}

	
	/**
	 * Parses the material.
	 *
	 * @param materialName the material description
	 * @return the material[]
	 * @throws ResourceException the resource exception
	 */
	private Material[] parseMaterial(String materialName)
			throws ResourceException {
		Material[] mat = null;
		// File f = new FileExt(materialName);

		BufferedReader br = new BufferedReader(new InputStreamReader(
				ResourceLoader.resourceUtil
						.getResourceAsInputStream(materialName)));
		StringBuffer cont = new StringBuffer();
		try {
			while (br.ready()) {
				cont.append(br.readLine() + "\n");
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] matList = cont.toString().trim().split("material");
		mat = new Material[matList.length];
		for (int x = 0; x < matList.length; x++) {
			String materialString = matList[x];

			String[] lines = materialString.split("\n");
			if (lines.length == 0) {
				System.err.println("Material String: line length 0: " + materialString);
			}
			mat[x] = new Material();
			mat[x].setName(lines[0].trim());
			// System.err.println("[PARSER] parsed Material "+x+": "+matTmp[x].
			// getName());
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];

				if (line.contains("ambient")) {
					float rbga[] = parseMaterialRBGA(line);
					mat[x].setAmbient(rbga);
					//					System.err.println("parsed ambient "+Arrays.toString(matTmp[x].getAmbient()));
				}
				if (line.contains("diffuse")) {
					float rbga[] = parseMaterialRBGA(line);
					mat[x].setDiffuse(rbga);
				}
				if (line.contains("specular")) {
					float rbga[] = parseMaterialRBGA(line);
					mat[x].setSpecular(rbga);
				}
				if (line.contains("emissive")) {
					float rbga[] = parseMaterialRBGA(line);
					// no value implemented in Material
				}
//				System.err.println("LINE: "+i+": "+line);
				if (line.contains("texture_unit")) {
					String textureName = null;
					boolean emissive = false;
					for (; i < lines.length; i++) {
						line = lines[i].trim();

//						System.err.println("LINE: "+i+": "+line);
						if (line.startsWith("texture ")) {
							String p[] = line.split(" ", 2);

							textureName = p[1].trim().replaceAll("\"", "");
							assert (!textureName.equals("add")) : line;
						}
						if (line.startsWith("colour_op_ex add src_texture src_current")) {
							emissive = true;
						}
						if (line.startsWith("}")) {
							//end of unit
							break;
						}
					}
					if (textureName != null) {
						if (emissive) {
							assert (textureName.toLowerCase(Locale.ENGLISH).contains("_em")) : textureName + "; line" + line + " i " + i;
							mat[x].setEmissiveTextureFile(textureName);
						} else if(textureName.toLowerCase(Locale.ENGLISH).contains("_nrm")){
							mat[x].setNormalTextureFile(textureName);
						} else {
							assert (!textureName.toLowerCase(Locale.ENGLISH).contains("_em")) : textureName + "; line" + line + " i " + i;
							mat[x].setTextureFile(textureName);
							mat[x].setMaterialTextured(true);
						}

					}
				}
			}
		}

		return mat;
	}

	/**
	 * Parses the material rbga.
	 *
	 * @param line the line
	 * @return the float[]
	 */
	private float[] parseMaterialRBGA(String line) {
		String p[] = line.split(" ");
		float rbga[] = new float[4];
		rbga[0] = Float.parseFloat(p[1]);
		rbga[1] = Float.parseFloat(p[2]);
		rbga[2] = Float.parseFloat(p[3]);
		if (p.length > 4) {
			rbga[3] = Float.parseFloat(p[4]);
		} else {
			rbga[3] = 1.0f;
		}
		return rbga;
	}

	/**
	 * Parses the mesh.
	 *
	 * @param meshPath the mesh path
	 * @param meshName the mesh description
	 * @param type
	 * @return the geometry
	 * @throws ResourceException the resource exception
	 * @
	 */
	public Geometry parseMesh(String meshPath, String meshName, int type)
			throws ResourceException {
		List<Geometry> meshes = new ObjectArrayList<Geometry>();
//		System.out.println("... parse Mesh " + meshPath + meshName + ".xml " + bufferIndex);
		StAXDocument st = new StAXDocument();
		XMLOgreParser mParser = new XMLOgreParser();
		mParser.bufferIndex = bufferIndex;
		st.parseXML(meshPath + meshName + ".xml");
		
		mParser.sceneFile = this.sceneFile;
		mParser.scenePath = this.scenePath;
		mParser.recordArrays = this.recordArrays;
		String skeletornlink = null;
		for (XMLOgreContainer c : st.getRoot().childs) {
			if (c.name.equals("submeshes")) {
				// System.err.println("... entered submeshes");
				meshes.addAll(mParser.parseSubmeshes(c, type));
			}
			if (c.name.equals("submeshnames")) {
				mParser.parseSubmeshnames(c, meshes);
			}
			if (c.name.equals("poses")) {
				// mParser.parsePoses(c,mesh);
			}

			if (c.name.equals("skeletonlink")) {

				skeletornlink = parseSkeletonName(c);
				//				System.out.println("parsing skeleton: "+skeletornlink);
			}
		}
		// System.err.println("... root mesh set to " + meshes.get(0).getName()
		// + " of " + meshes.size() + " meshes");
		Geometry rootmesh = meshes.remove(0);
		// RootMesh First Vertex [8.7583, 20.1304, -6.96387]
		for (Geometry m : meshes) {
			rootmesh.attach(m); // its only one level hirarchy
			// more level should be implemented recursivly
		}
		if (skeletornlink != null) {
			parseSkeleton((Mesh) rootmesh, meshPath, skeletornlink);

		}
		//set from mesh
		bufferIndex = mParser.bufferIndex;
		return rootmesh;
	}

	/**
	 * Parses the node.
	 *
	 * @param c     the c
	 * @param scene the scene
	 * @throws ResourceException the resource exception
	 * @
	 */
	private void parseNode(XMLOgreContainer c, AbstractSceneNode scene)
			throws ResourceException {
		String name = "default";
		int visibility = AbstractSceneNode.VISIBILITY_VISIBLE; //DEFAULT
		for (XMLAttribute att : c.attribs) {
			if (att.name.equals("name")) {
				// description
				name = att.value;
//				 System.err.println("... node Name: " + name);
			}
			if (att.name.equals("visibility")) {
				if (att.value.equals("visible")) {
					visibility = AbstractSceneNode.VISIBILITY_VISIBLE;
				}
				if (att.value.equals("hidden")) {
					//					System.err.println("object "+description+" is hidden");
					visibility = AbstractSceneNode.VISIBILITY_HIDDEN;
				}
				if (att.value.equals("tree visible")) {
					visibility = AbstractSceneNode.VISIBILITY_TREE_VISIBLE;
				}
				if (att.value.equals("tree hidden")) {
					visibility = AbstractSceneNode.VISIBILITY_TREE_HIDDEN;
				}
			}
		}
		AbstractSceneNode form = null;
		Vector3f pos = new Vector3f();
		Vector4f rot = new Vector4f();
		Vector3f scale = new Vector3f();
		Vector<Animation> anims = null;
		boolean isMesh = false;
		for (XMLOgreContainer node : c.childs) {
			// System.err.println("... parsing node info: "+node.childs);
			if (node.name.equals("position")) {
				// position
				pos = parseVector3(node);
			}
			if (node.name.equals("rotation")) {
				// rotation
				rot = parseRot(node);
			}
			if (node.name.equals("scale")) {
				// scale
				scale = parseVector3(node);
			}
			if (node.name.equals("animations")) {
				// entity
				anims = parseAnimations(node);
			}
			if (node.name.equals("entity")) {
				// entity
				isMesh = true;
				assert(this.sceneFile != null);
				assert(this.scenePath != null);
				form = parseEntity(node); // child node is a Geometry
				form.setVisibility(visibility);
//				 System.err.println("node "+node.name+" bb "+bufferIndex);
				// scene.getChilds().set(scene.getChilds().size()-1, mesh);
				// //replace scene Node with mesh
				// System.err.println("... attached to scene "
				// + scene.getName() + ": " + sceneNode);

			}

		}
		if (!isMesh) {
			// its a scene node (not a geometry)
			form = new SceneNode();

			form.setVisibility(visibility);
		}
		// do hirachical parse last
		for (XMLOgreContainer node : c.childs) {
			if (node.name.equals("node")) {
				parseNode(node, form); // bone hierarchy independent from
				// scene
			}
		}
		assert (!scene.getChilds().contains(form));
		scene.attach(form);
		if (anims != null) {
			form.setAnimations(anims);
			try {
				form.selectAnimation(
						form.getName());
			} catch (AnimationNotFoundException e) {
				e.printStackTrace();
			}
			form.setAnimated(true);
		}
		form.setInitionPos(pos);
		form.setQuatRot(rot);
		form.setInitialQuadRot(rot);
		form.getScale().set(scale);
		form.setInitialScale(new Vector3f(scale));
		form.setName(name);
	}

	/**
	 * Parses the nodes.
	 *
	 * @param c     the c
	 * @param scene the scene
	 * @throws ResourceException the resource exception
	 * @
	 */
	private void parseNodes(XMLOgreContainer c, AbstractSceneNode scene)
			throws ResourceException {
		int nodes_count = 0;
		for (XMLOgreContainer node : c.childs) {
			if (node.name.equals("node")) {
//				System.err.println("PARSING NODE: "+nodes_count+"; "+bufferIndex);
				nodes_count++;
				parseNode(node, scene);
			}
		}
		// System.err.println("... parsed " + nodes_count + " node(s)");
	}

	/**
	 * Parses the scene.
	 *
	 * @param scenePath the scene path
	 * @param sceneFile the scene file
	 * @return the world
	 * @throws ResourceException the resource exception
	 * @
	 */
	public MeshGroup parseScene(String scenePath, String sceneFile)
			throws ResourceException {
		GlUtil.LOCKDYN = !recordArrays;
		this.scenePath = scenePath;
		
		if(sceneFile.endsWith(".scene")) {
			sceneFile = sceneFile.substring(0, sceneFile.length()-".scene".length());
		}
		
		this.sceneFile = sceneFile;
		
		this.bufferIndex = 0;
		assert(this.sceneFile != null);
		assert(this.scenePath != null);
		MeshGroup scene = new MeshGroup();
		String sceneFilePath = scenePath + sceneFile + ".scene";
		while (sceneFilePath.contains("//")) {
			sceneFilePath = sceneFilePath.replaceAll("//", "/");
		}
		StAXDocument t = new StAXDocument();
		t.parseXML(sceneFilePath);
		
		for (XMLOgreContainer c : t.getRoot().childs) {

			if (c.name.equals("environment")) {
				parseEnvironment(c, scene);
			}
			if (c.name.equals("nodes")) {
				assert(this.sceneFile != null);
				assert(this.scenePath != null);
				parseNodes(c, scene);

			}
		}

		//the mass of the whole scene is the sum of all its submeshes
		int mass = 0;
		for (AbstractSceneNode f : scene.getChilds()) {
			if (f instanceof Geometry) {
				mass += ((Geometry) f).getMass();
			}
		}
		scene.setMass(mass);

		//create a bounding box around the whole scene (performance)
		scene.setBoundingBox(scene.makeBiggestBoundingBox(new Vector3f(),
				new Vector3f()));

		//create a bounding sphere around the whole scene (performance)
		scene.makeBoundingSphere();

		scene.setLoaded(true);

		scene.scenePath = this.scenePath;
		scene.sceneFile = this.sceneFile;
		return scene;
	}

	private void parseSkeleton(Mesh skeletor, String meshPath, String skeletornlink) throws ResourceException {
		//System.err.println("now parsing skeleton: "+meshPath+skeletornlink
		// +".xml");
		Skeleton skeleton = parseSkeletonFile(meshPath, skeletornlink);
		HashMap<Integer, List<VertexBoneWeight>> vertexWeightMap = new HashMap<Integer, List<VertexBoneWeight>>();
		VertexBoneWeight[] w = skeletor.getVertexBoneAssignments();
		for (VertexBoneWeight vbw : w) {
			//
			List<VertexBoneWeight> list = vertexWeightMap.get(vbw.vertexIndex);
			if (list == null) {
				list = new com.bulletphysics.util.ObjectArrayList();
				vertexWeightMap.put(vbw.vertexIndex, list);
			}
			//			 System.err.println("added bone "+vbw.boneIndex+" to vertex "+
			//			 vbw.vertexIndex);
			list.add(vbw);
			assert (list.size() < 5);

		}

		WeightsBuffer weightsBuffer = new WeightsBuffer(skeletor.getVertCount());
		weightsBuffer.initialize(vertexWeightMap, skeleton);
		skeletor.setVertexBoneAssignments(null);

		skeletor.setSkin(new Skin(skeleton, weightsBuffer));
	}

	/**
	 * Parses the skeleton.
	 *
	 * @param skelpath the skelpath
	 * @param skelname the skelname
	 * @return the skeleton
	 * @throws ResourceException the resource exception
	 */
	private Skeleton parseSkeletonFile(String skelpath, String skelname)
			throws ResourceException {
//		System.err.println("[PARSER] parsing skeleton " + skelpath + skelname);
		XMLOgreParser mParser = new XMLOgreParser();
		mParser.bufferIndex = bufferIndex;
		StAXDocument t = new StAXDocument();
		t.parseXML(skelpath + skelname + ".xml");
		Skeleton skeleton = new Skeleton();
		XMLOgreContainer bones = null;
		XMLOgreContainer hierarchy = null;
		XMLOgreContainer animations = null;
		for (XMLOgreContainer c : t.getRoot().childs) {
			if (c.name.equals("bones")) {
				bones = c;
			}
			if (c.name.equals("bonehierarchy")) {
				hierarchy = c;
			}
			if (c.name.equals("animations")) {
				animations = c;
			}
		}

		parseBones(skeleton, bones);
		parseBoneHierarchy(skeleton, hierarchy, skelname);
		if (animations != null) {

			skeleton.setLoadedAnimations(parseBoneAnimations(animations));
		}
		bufferIndex = mParser.bufferIndex;
		return skeleton;
	}

	/**
	 * Parses the skeleton description.
	 *
	 * @param c the c
	 * @return the string
	 */
	private String parseSkeletonName(XMLOgreContainer c) {
		for (XMLAttribute att : c.attribs) {
			if (att.name.equals("name")) {
				return att.value;
			}
		}
		return null;

	}

	/**
	 * Parses the submesh.
	 *
	 * @param c    the c
	 * @param type
	 * @return the geometry
	 */
	public Geometry parseSubmesh(XMLOgreContainer c, int type) {
		Geometry geometry = null;
		String material = null;
		boolean usesharedvertices = false;
		boolean use32bitindexes = false;
		String operationtype = null;

		for (XMLAttribute att : c.attribs) {
			String val = att.value;
			if (att.name.equals("material")) {
				material = val;
			}
			if (att.name.equals("usesharedvertices")) {
				usesharedvertices = Boolean.parseBoolean(val);
			}
			if (att.name.equals("use32bitindexes")) {
				use32bitindexes = Boolean.parseBoolean(val);
			}
			if (att.name.equals("operationtype")) {
				operationtype = val;
			}

		}
		if (operationtype.equals("line_list")) {
			geometry = new Line();
		} else {
			if (type == TYPE_MESH) {
				geometry = new Mesh();
				geometry.scenePath = this.scenePath;
				geometry.sceneFile = this.sceneFile;
			}
			if (type == TYPE_TERRAIN) {
				geometry = new OldTerrain();
			}
		}
		int n = 0;
		for (XMLOgreContainer geoAndFacesAndBones : c.childs) {
			if (geoAndFacesAndBones.name.equals("faces")) {
				Mesh mesh = null;
				int i = 0;
				if (geometry instanceof Mesh) {
					mesh = (Mesh) geometry;
					int faceCount = Integer
							.parseInt(geoAndFacesAndBones.attribs.get(0).value);

					mesh.setFaceCount(faceCount);

					if(!recordArrays){
						mesh.setIndexBuffer(GlUtil.getDynamicByteBuffer(faceCount * 3 * ByteUtil.SIZEOF_INT, bufferIndex++).asIntBuffer());
						mesh.normalIndexBuffer = GlUtil.getDynamicByteBuffer(faceCount * 3 * ByteUtil.SIZEOF_INT, bufferIndex++).asIntBuffer();
						mesh.texCoordIndexBuffer = GlUtil.getDynamicByteBuffer(faceCount * 3 * ByteUtil.SIZEOF_INT, bufferIndex++).asIntBuffer();
						mesh.tangentIndexBuffer = GlUtil.getDynamicByteBuffer(faceCount * 3 * ByteUtil.SIZEOF_INT, bufferIndex++).asIntBuffer();
						mesh.binormalIndexBuffer = GlUtil.getDynamicByteBuffer(faceCount * 3 * ByteUtil.SIZEOF_INT, bufferIndex++).asIntBuffer();
					}
					for (XMLOgreContainer face : geoAndFacesAndBones.childs) {

						//						mesh.faces[i] = new Face();
						mesh.setIndicedNormals(true);// indiced Normal

						for (int j = 0; j < 3; j++) {
							int v = Integer
							.parseInt(face.attribs.get(j).value);
							if(recordArrays){
								mesh.recordedIndices.add(v);
							}else{
								mesh.getIndexBuffer().put(v);
	
								mesh.texCoordIndexBuffer.put(v);
	
								mesh.normalIndexBuffer.put(v);
								
								mesh.tangentIndexBuffer.put(v);
								
								mesh.binormalIndexBuffer.put(v);
							}
						}
						i++;
					}
				}
				//				 System.err.println("... Faces of " + mesh.getName() +
				//				 " read "
				//				 + i + "/" + geoAndFacesAndBones.attribs.get(0).value);
			}
			if (geoAndFacesAndBones.name.equals("geometry")) {
				int vert_count = 0;

				for (XMLAttribute att : geoAndFacesAndBones.attribs) {
					String val = att.value;
					if (att.name.equals("vertexcount")) {
						vert_count = Integer.parseInt(val);
						geometry.setVertCount(vert_count);
						if (geometry instanceof Mesh) {
							((Mesh) geometry).setTexCoordCount(vert_count);
						}
					}
				}
				for (XMLOgreContainer geo : geoAndFacesAndBones.childs) {
					if (geo.name.equals("vertexbuffer")) {
						parseVertexBuffer(geometry, geo, vert_count, n);
					}
				}
			}
			if (geoAndFacesAndBones.name.equals("boneassignments")) {
				parseBoneAssignments((Mesh) geometry, geoAndFacesAndBones);
			}
			n++;
		}
		if(!recordArrays){
		geometry.verticesBuffer.rewind();
		geometry.getIndexBuffer().rewind();
		}
		geometry.scenePath = this.scenePath;
		geometry.sceneFile = this.sceneFile;
		assert(this.sceneFile != null);
		assert(this.scenePath != null);
		if (geometry instanceof Mesh && !recordArrays) {
			((Mesh) geometry).texCoordsBuffer.flip();
			((Mesh) geometry).normalsBuffer.flip();
			if(((Mesh) geometry).hasTangents){
				((Mesh) geometry).tangentsBuffer.flip();
			}
			if(((Mesh) geometry).hasBinormals){
				((Mesh) geometry).binormalsBuffer.flip();
			}
			((Mesh) geometry).normalIndexBuffer.flip();
			((Mesh) geometry).texCoordIndexBuffer.flip();
			((Mesh) geometry).tangentIndexBuffer.flip();
			((Mesh) geometry).binormalIndexBuffer.flip();
		}
		return geometry;
	}

	/**
	 * Parses the submeshes.
	 *
	 * @param c    the c
	 * @param type
	 * @return the vector
	 */
	public Vector<Geometry> parseSubmeshes(XMLOgreContainer c, int type) {
		Vector<Geometry> meshes = new Vector<Geometry>();
		for (XMLOgreContainer submesh : c.childs) {
			//			 System.err.println("... entered Submesh");
			Geometry mesh = parseSubmesh(submesh, type);
			meshes.add(mesh);
		}
		return meshes;
	}

	/**
	 * Parses the submeshnames.
	 *
	 * @param c      the c
	 * @param meshes the meshes
	 */
	public void parseSubmeshnames(XMLOgreContainer c, List<Geometry> meshes) {
		for (XMLOgreContainer submeshname : c.childs) {
			int index = 0;
			String name = "";
			for (XMLAttribute att : submeshname.attribs) {

				if (att.name.equals("name")) {
					name = att.value;
					// System.err.println(".. description found");
				}
				if (att.name.equals("index")) {
					index = Integer.parseInt(att.value);
					// System.err.println(".. index found");
				}
			}
			meshes.get(index).setName(name);
			// System.err.println("... description of "+index+" set to "+description);
		}
	}

	/**
	 * Parses the submesh.
	 *
	 * @param c    the c
	 * @param type
	 * @return the geometry
	 */
	@Deprecated
	public Geometry parseSubmeshToMEshArrays(XMLOgreContainer c, int type) {
		Geometry geometry = null;
		String material = null;
		boolean usesharedvertices = false;
		boolean use32bitindexes = false;
		String operationtype = null;

		for (XMLAttribute att : c.attribs) {
			String val = att.value;
			if (att.name.equals("material")) {
				material = val;
			}
			if (att.name.equals("usesharedvertices")) {
				usesharedvertices = Boolean.parseBoolean(val);
			}
			if (att.name.equals("use32bitindexes")) {
				use32bitindexes = Boolean.parseBoolean(val);
			}
			if (att.name.equals("operationtype")) {
				operationtype = val;
			}

		}
		if (operationtype.equals("line_list")) {
			geometry = new Line();
		} else {
			if (type == TYPE_MESH) {
				geometry = new Mesh();
			}
			if (type == TYPE_TERRAIN) {
				geometry = new OldTerrain();
			}
		}
		for (XMLOgreContainer geoAndFacesAndBones : c.childs) {
			if (geoAndFacesAndBones.name.equals("faces")) {
				Mesh mesh = null;
				if (geometry instanceof Mesh) {
					mesh = (Mesh) geometry;
					int count = Integer
							.parseInt(geoAndFacesAndBones.attribs.get(0).value);
					mesh.faces = new Face[count];
					mesh.setFaceCount(count);
				}

				int i = 0;
				for (XMLOgreContainer face : geoAndFacesAndBones.childs) {

					mesh.faces[i] = new Face();
					mesh.setIndicedNormals(true);// indiced Normal

					for (int j = 0; j < mesh.faces[i].m_vertsIndex.length; j++) {
						mesh.faces[i].m_vertsIndex[j] = Integer
								.parseInt(face.attribs.get(j).value);
						mesh.faces[i].m_texCoordsIndex[j] = Integer
								.parseInt(face.attribs.get(j).value);
						mesh.faces[i].m_normalIndex[j] = Integer
								.parseInt(face.attribs.get(j).value);
					}
					i++;
				}
				// System.err.println("... Faces of " + mesh.getName() +
				// " read "
				// + i + "/" + geoAndFaces.attribs.get(0).value);
			}
			if (geoAndFacesAndBones.name.equals("geometry")) {
				int vert_count = 0;

				for (XMLAttribute att : geoAndFacesAndBones.attribs) {
					String val = att.value;
					if (att.name.equals("vertexcount")) {
						vert_count = Integer.parseInt(val);
						geometry.setVertCount(vert_count);
						if (geometry instanceof Mesh) {
							((Mesh) geometry).setTexCoordCount(vert_count);
						}
					}
				}
				for (XMLOgreContainer geo : geoAndFacesAndBones.childs) {
					if (geo.name.equals("vertexbuffer")) {
						boolean positions = false;
						boolean normals = false;
						boolean tangents = false;
						boolean binormals = false;
						int texCoord_count = 0;
						int texCoord_dim = 2;
						for (XMLAttribute att : geo.attribs) {
							String val = att.value;
							System.err.println("PARSING VAL: " + val);
							if (att.name.equals("positions")) {

								positions = Boolean.parseBoolean(val);
								//System.err.println("... positions: "+positions
								// );
							}
							if (att.name.equals("normals")) {
								normals = Boolean.parseBoolean(val);
							}
							if (att.name.equals("tangents")) {
								tangents = Boolean.parseBoolean(val);
							}
							if (att.name.equals("binormals")) {
								binormals = Boolean.parseBoolean(val);
							}
							if (att.name.equals("texture_coords")) {
								texCoord_count = Integer.parseInt(val);
							}
							if (att.name.equals("texture_coord_dimension_0")) {
								texCoord_dim = Integer.parseInt(val);
							}
							if (att.name.equals("texture_coord_dimensions_0")) {
//								System.err.println("[MESH] TEXTURE COORD DIM: "+val);
								if (val.equals("float1")) {
									texCoord_dim = 1;
								} else if (val.equals("float2")) {
									texCoord_dim = 2;
								} else if (val.equals("float3")) {
									texCoord_dim = 3;
								} else {
									assert false;
								}
							}
						}
						geometry.vertices = new Vector3f[vert_count];
						if (geometry instanceof Mesh) {
							if(normals){
								((Mesh) geometry).normals = new Vector3f[vert_count];
							}
							((Mesh) geometry).texCoords = new Vector3f[vert_count];
							if(tangents){
								((Mesh) geometry).tangents = new Vector3f[vert_count];
							}
							if(binormals){
								((Mesh) geometry).binormals = new Vector3f[vert_count];
							}
						}
						int i = 0;
						float maxX = Integer.MIN_VALUE;
						float minX = Integer.MAX_VALUE;
						float maxY = Integer.MIN_VALUE;
						float minY = Integer.MAX_VALUE;
						float maxZ = Integer.MIN_VALUE;
						float minZ = Integer.MAX_VALUE;
						for (XMLOgreContainer vertices : geo.childs) {
							//System.err.println("verts: "+vertices.childs.size(
							// ));
							for (XMLOgreContainer vertex : vertices.childs) {
								int test = 0;
								// System.err.println(vertex.name);

								if (positions && vertex.name.equals("position")) {
									test++;
									geometry.vertices[i] = parseVector3(vertex);
									maxX = (geometry.vertices[i].x > maxX) ? geometry.vertices[i].x
											: maxX;
									minX = (geometry.vertices[i].x < minX) ? geometry.vertices[i].x
											: minX;
									maxY = (geometry.vertices[i].y > maxY) ? geometry.vertices[i].y
											: maxY;
									minY = (geometry.vertices[i].y < minY) ? geometry.vertices[i].y
											: minY;
									maxZ = (geometry.vertices[i].z > maxZ) ? geometry.vertices[i].z
											: maxZ;
									minZ = (geometry.vertices[i].z < minZ) ? geometry.vertices[i].z
											: minZ;
								}
								if (normals && vertex.name.equals("normal")) {
									test++;
									((Mesh) geometry).normals[i] = parseVector3(vertex);
								}
								if (tangents && vertex.name.equals("tangent")) {
									test++;
									((Mesh) geometry).tangents[i] = parseVector3(vertex);
								}
								if (binormals && vertex.name.equals("binormal")) {
									test++;
									((Mesh) geometry).binormals[i] = parseVector3(vertex);
								}
								if (texCoord_count > 0
										&& vertex.name.equals("texcoord")) {
									test++;
									((Mesh) geometry).texCoords[i] = parseVector3(vertex);

								}
								// if(test < 3){
								// System.err.println("!!! not all factors of
								// vertex loaded");
								// }

							}
							i++;
						}
						Vector3f min = new Vector3f(minX, minY, minZ);
						Vector3f max = new Vector3f(maxX, maxY, maxZ);
						//System.err.println("setting bounding box for "+mesh+" "
						// +min+" - "+max);
						geometry.setBoundingBox(new BoundingBox(min, max));
						geometry.makeBoundingSphere();
					}
				}
			}
			if (geoAndFacesAndBones.name.equals("boneassignments")) {
				VertexBoneWeight[] vertBoneAss = new VertexBoneWeight[geoAndFacesAndBones.childs
						.size()];
				int i = 0;
				for (XMLOgreContainer bonesAssign : geoAndFacesAndBones.childs) {
					if (bonesAssign.name.equals("vertexboneassignment")) {
						// vertexindex="2067" boneindex="22" weight="1.00000"
						int vertexIndex = -1;
						int boneIndex = -1;
						float weight = -1;
						for (XMLAttribute att : bonesAssign.attribs) {
							String val = att.value;
							if (att.name.equals("vertexindex")) {
								vertexIndex = Integer.parseInt(val);
							}
							if (att.name.equals("boneindex")) {
								boneIndex = Integer.parseInt(val);
							}
							if (att.name.equals("weight")) {
								weight = Float.parseFloat(val);
							}
						}
						VertexBoneWeight vbw = new VertexBoneWeight(
								vertexIndex, boneIndex, weight);
						vertBoneAss[i] = vbw;
						((Mesh) geometry).setVertexBoneAssignments(vertBoneAss);
					}
					i++;
				}
			}
		}

		// System.err.println("... Vertex count: " + mesh.getVertCount() + "/"
		// + mesh.getFaceCount() * 3);
		// for(int i = 0; i < mesh.getFaceCount(); i++){
		// mesh.faces[i].m_normals = new Vector3f[3];
		// mesh.faces[i].m_normals[0] = mesh.normals[i*3];
		// mesh.faces[i].m_normals[1] = mesh.normals[i*3+1];
		// mesh.faces[i].m_normals[2] = mesh.normals[i*3+2];
		// }
		return geometry;
	}

	/**
	 * Parses the track key frames.
	 *
	 * @param c          the c
	 * @param framecount the framecount
	 * @return the pos rot scale
	 */
	private void parseTrackKeyFrames(XMLOgreContainer c, AnimationTrack track) {
		int i = 0;
		for (XMLOgreContainer keyframes : c.childs) {

			if (keyframes.name.equals("keyframe")) {
				float time = -1;
				for (XMLAttribute att : keyframes.attribs) {
					if (att.name.equals("time")) {
						time = Float.parseFloat(att.value);
					}
				}

				Vector3f translate = null;
				AxisAngle4f rotation = null;
				Vector3f scale = null;
				for (XMLOgreContainer keyframe : keyframes.childs) {

					// System.err.println("-->"+keyframe.name);
					if (keyframe.name.equals("translate")) {
						// position
						translate = parseVector3(keyframe, new Vector3f());
					}

					if (keyframe.name.equals("rotate")) {
						// rotation
						rotation = new AxisAngle4f();
						for (XMLAttribute att : keyframe.attribs) {
							String val = att.value;
							if (att.name.equals("angle")) {
								rotation.angle = Float.parseFloat(val);
							}
						}
						for (XMLOgreContainer axis : keyframe.childs) {
							if (axis.name.equals("axis")) {
								Vector3f ax = parseVector3(axis, new Vector3f());
								ax.normalize();
								rotation.x = ax.x;
								rotation.y = ax.y;
								rotation.z = ax.z;
							}
						}
					}
					if (keyframe.name.equals("scale")) {
						// scale
						scale = parseVector3(keyframe);
					}
				}
				i++;
				assert (time >= 0);
				assert (translate != null);
				assert (rotation != null);

				Quat4f fromAngleAxis = Quat4Util.fromAngleAxis(rotation.angle, new Vector3f(rotation.x, rotation.y, rotation.z), new Quat4f());
				track.addFrame(time, translate, fromAngleAxis, scale);
			}
		}
	}

	/**
	 * Parses the vector3.
	 *
	 * @param c the c
	 * @return the vector3 d
	 */
	private Vector3f parseVector3(XMLOgreContainer c) {
		return parseVector3(c, new Vector3f());
	}
	private Vector3f parseVector3(XMLOgreContainer c, Vector3f out) {
		XMLAttribute att = c.attribs.get(0);
		out.x = (Float.parseFloat(att.value));
		att = c.attribs.get(1);
		out.y = (Float.parseFloat(att.value));
		if (c.attribs.size() > 2) {
			att = c.attribs.get(2);
			out.z = (Float.parseFloat(att.value));
		} else {
			out.z = (1);
		}
		return out;
	}

//	/**
//	 * Parses the vector4.
//	 *
//	 * @param c the c
//	 * @return the vector4 d
//	 */
//	private Vector4f parseVector4(XMLOgreContainer c) {
//		Vector4f v = new Vector4f();
//		XMLAttribute att = c.attribs.get(0);
//		v.x = Float.parseFloat(att.value);
//		att = c.attribs.get(1);
//		v.y = Float.parseFloat(att.value);
//		att = c.attribs.get(2);
//		v.z = Float.parseFloat(att.value);
//		att = c.attribs.get(3);
//		v.w = Float.parseFloat(att.value);
//		return v;
//	}
	private Vector4f parseRot(XMLOgreContainer m) {
		Vector4f rot = new Vector4f(); 
		for(int i = 0; i < m.attribs.size(); i++) {
			XMLAttribute att = m.attribs.get(i);
			if(att.name.toLowerCase(Locale.ENGLISH).equals("qx")) {
				rot.x = Float.parseFloat(att.value);
			}else if(att.name.toLowerCase(Locale.ENGLISH).equals("qy")) {
				rot.y = Float.parseFloat(att.value);
			}else if(att.name.toLowerCase(Locale.ENGLISH).equals("qz")) {
				rot.z = Float.parseFloat(att.value);
			}else if(att.name.toLowerCase(Locale.ENGLISH).equals("qw")) {
				rot.w = Float.parseFloat(att.value);
			}else {
				throw new RuntimeException("Malformed rotation "+m);
			}
		}
		return rot;
	}

	private void parseVertexBuffer(Geometry geometry, XMLOgreContainer geo, int vert_count, int n) {
		boolean positions = false;
		boolean normals = false;
		boolean tangents = false;
		boolean binormals = false;
		boolean newStyleTexCoords = false;
		int texCoord_count = 0;
		int texCoord_dim = 0;
		for (XMLAttribute att : geo.attribs) {
			String val = att.value;
			if (att.name.equals("positions")) {

				positions = Boolean.parseBoolean(val);
				//System.err.println("... positions: "+positions
				// );
			}
			if (att.name.equals("normals")) {
				// System.err.println("... normals: "+val);
				normals = Boolean.parseBoolean(val);
				
			}
			if (att.name.equals("tangents")) {
				tangents = Boolean.parseBoolean(val);
			}
			if (att.name.equals("binormals")) {
				binormals = Boolean.parseBoolean(val);
			}
			if (att.name.equals("texture_coords")) {
				texCoord_count = Integer.parseInt(val);
			}
			if (att.name.equals("texture_coord_dimension_0")) {
				texCoord_dim = Integer.parseInt(val);
			}
			if (att.name.equals("texture_coord_dimensions_0")) {
//				System.err.println("[MESH] TEXTURE COORD DIM: "+val);
				try {
					texCoord_dim = Integer.parseInt(val);

				} catch (NumberFormatException e) {
					newStyleTexCoords = true;
					if (val.equals("float1")) {
						texCoord_dim = 1;
					} else if (val.equals("float2")) {
						texCoord_dim = 2;
					} else if (val.equals("float3")) {
						texCoord_dim = 3;
					} else {
						assert false : geometry.getName() + ": " + att.name + ": " + val;
					}
				}
			}
		}
		if (positions) {
			if(!recordArrays){
				geometry.verticesBuffer = GlUtil.getDynamicByteBuffer(vert_count * 3 * ByteUtil.SIZEOF_FLOAT, bufferIndex++).asFloatBuffer();
			}
		}
		if (normals) {
			if(!recordArrays){
				((Mesh) geometry).normalsBuffer = GlUtil.getDynamicByteBuffer(vert_count * 3 * ByteUtil.SIZEOF_FLOAT, bufferIndex++).asFloatBuffer();
			}
		}
		if (tangents) {
			
			((Mesh) geometry).hasTangents = true;
			if(!recordArrays){
			((Mesh) geometry).tangentsBuffer = GlUtil.getDynamicByteBuffer(vert_count * 3 * ByteUtil.SIZEOF_FLOAT, bufferIndex++).asFloatBuffer();
			}
		}
		if (binormals) {
			((Mesh) geometry).hasBinormals = true;
			if(!recordArrays){
			((Mesh) geometry).binormalsBuffer = GlUtil.getDynamicByteBuffer(vert_count * 3 * ByteUtil.SIZEOF_FLOAT, bufferIndex++).asFloatBuffer();
			}
		}
		if (texCoord_count > 0) {
			if(!recordArrays){
				((Mesh) geometry).texCoordsBuffer = GlUtil.getDynamicByteBuffer(vert_count * texCoord_dim * texCoord_count * ByteUtil.SIZEOF_FLOAT, bufferIndex++).asFloatBuffer();
			}
			((Mesh) geometry).texCoordSetCount = texCoord_count;
		}
		Vector3f tmp = new Vector3f();
		int i = 0;
		int parsedVertexCount = 0;
		float maxX = Integer.MIN_VALUE;
		float minX = Integer.MAX_VALUE;
		float maxY = Integer.MIN_VALUE;
		float minY = Integer.MAX_VALUE;
		float maxZ = Integer.MIN_VALUE;
		float minZ = Integer.MAX_VALUE;
		for (XMLOgreContainer vertices : geo.childs) {
			for (XMLOgreContainer vertex : vertices.childs) {
				if (positions && vertex.name.equals("position")) {
					Vector3f v = parseVector3(vertex, tmp);

					if(!recordArrays){
						geometry.verticesBuffer.put(v.x);
						geometry.verticesBuffer.put(v.y);
						geometry.verticesBuffer.put(v.z);
					}
					maxX = (v.x > maxX) ? v.x
							: maxX;
					minX = (v.x < minX) ? v.x
							: minX;
					maxY = (v.y > maxY) ? v.y
							: maxY;
					minY = (v.y < minY) ? v.y
							: minY;
					maxZ = (v.z > maxZ) ? v.z
							: maxZ;
					minZ = (v.z < minZ) ? v.z
							: minZ;
					parsedVertexCount++;
					if(recordArrays){
						((Mesh)geometry).recordedVectices.add(new Vector3f(v));
					}
				}
				if (normals && vertex.name.equals("normal")) {
					Vector3f v = parseVector3(vertex, tmp);
					
					
					if(recordArrays){
						((Mesh)geometry).recordedNormals.add(new Vector3f(v));
					}else{
						((Mesh) geometry).normalsBuffer.put(v.x);
						((Mesh) geometry).normalsBuffer.put(v.y);
						((Mesh) geometry).normalsBuffer.put(v.z);
					}
				}
				if (normals && vertex.name.equals("tangent")) {
					if(recordArrays){
						
					}else{
					Vector3f v = parseVector3(vertex);
					((Mesh) geometry).tangentsBuffer.put(v.x);
					((Mesh) geometry).tangentsBuffer.put(v.y);
					((Mesh) geometry).tangentsBuffer.put(v.z);
					}
				}
				if (normals && vertex.name.equals("binormal")) {
					if(recordArrays){
						
					}else{
					Vector3f v = parseVector3(vertex);
					((Mesh) geometry).binormalsBuffer.put(v.x);
					((Mesh) geometry).binormalsBuffer.put(v.y);
					((Mesh) geometry).binormalsBuffer.put(v.z);
					}
				}
				if (texCoord_count > 0
						&& vertex.name.equals("texcoord")) {
					Vector3f v = parseVector3(vertex);
					if(recordArrays){
						((Mesh)geometry).recordedTextcoords.add(new Vector2f(v.x, v.y));
					}else{
						((Mesh) geometry).texCoordsBuffer.put(v.x);
						((Mesh) geometry).texCoordsBuffer.put(v.y);
					}
					//					if(newStyleTexCoords){
					//						System.err.println("PARSED: "+v);
					//					}
				}
			}
			i++;
		}
		if (positions) {
			Vector3f min = new Vector3f(minX, minY, minZ);
			Vector3f max = new Vector3f(maxX, maxY, maxZ);
			assert (parsedVertexCount == vert_count);
			//			System.err.println("setting bounding box for "+geometry.getName()+" "
			//					+min+" - "+max+"; vertex count: "+parsedVertexCount);
			geometry.setBoundingBox(new BoundingBox(min, max));
			geometry.makeBoundingSphere();
		}
	}

	
}
