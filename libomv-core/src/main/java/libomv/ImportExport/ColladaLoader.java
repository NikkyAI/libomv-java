/**
 * Copyright (c) 2006-2016, openmetaverse.org
 * Copyright (c) 2016-2017, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Neither the name of the openmetaverse.co nor the names
 *   of its contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package libomv.ImportExport;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import libomv.ImportExport.ModelMaterial;
import libomv.ImportExport.ModelPrim;
import libomv.ImportExport.collada.Asset;
import libomv.ImportExport.collada.COLLADA;
import libomv.ImportExport.collada.CommonColorOrTextureType;
import libomv.ImportExport.collada.Effect;
import libomv.ImportExport.collada.Geometry;
import libomv.ImportExport.collada.Image;
import libomv.ImportExport.collada.InputLocalOffset;
import libomv.ImportExport.collada.InstanceGeometry;
import libomv.ImportExport.collada.InstanceMaterial;
import libomv.ImportExport.collada.LibraryEffects;
import libomv.ImportExport.collada.LibraryGeometries;
import libomv.ImportExport.collada.LibraryImages;
import libomv.ImportExport.collada.LibraryMaterials;
import libomv.ImportExport.collada.LibraryVisualScenes;
import libomv.ImportExport.collada.Material;
import libomv.ImportExport.collada.Matrix;
import libomv.ImportExport.collada.Mesh;
import libomv.ImportExport.collada.Node;
import libomv.ImportExport.collada.Polylist;
import libomv.ImportExport.collada.ProfileCOMMON;
import libomv.ImportExport.collada.Source;
import libomv.ImportExport.collada.Triangles;
import libomv.ImportExport.collada.UpAxisType;
import libomv.ImportExport.collada.VisualScene;
import libomv.imaging.J2KImage;
import libomv.imaging.ManagedImage;
import libomv.imaging.TGAImage;
import libomv.rendering.Mesh.Vertex;
import libomv.types.Color4;
import libomv.types.Matrix4;
import libomv.types.UUID;
import libomv.types.Vector2;
import libomv.types.Vector3;
import libomv.utils.Helpers;
import libomv.utils.RefObject;

// Parsing Collada model files into data structures
public class ColladaLoader {
	private static final Logger logger = Logger.getLogger(ColladaLoader.class);

	COLLADA Model;
	List<LoaderNode> Nodes;
	List<ModelMaterial> Materials;
	HashMap<String, String> MatSymTarget;
	File FileName;

	class LoaderNode {
		public Matrix4 Transform = new Matrix4();
		public String Name;
		public String ID;
		public String MeshID;
	}

	/// Parses Collada document
	/// </summary>
	/// <param name="filename">Load .dae model from this file</param>
	/// <param name="loadImages">Load and decode images for uploading with
	/// model</param>
	/// <returns>A list of mesh prims that were parsed from the collada
	/// file</returns>
	public List<ModelPrim> Load(File filename, boolean loadImages) {
		try {
			this.FileName = filename;

			// A FileStream is needed to read the XML document.
			// create JAXB context and instantiate marshaller
			JAXBContext context = JAXBContext.newInstance(COLLADA.class);
			Unmarshaller um = context.createUnmarshaller();
			Model = (COLLADA) um.unmarshal(new FileReader(filename));
			List<ModelPrim> prims = Parse();
			if (loadImages) {
				LoadImages(prims);
			}
			return prims;
		} catch (Exception ex) {
			logger.error("Failed parsing collada file: " + ex.getMessage(), ex);
			return new ArrayList<ModelPrim>();
		}
	}

	void LoadImages(List<ModelPrim> prims) {
		for (ModelPrim prim : prims) {
			for (ModelFace face : prim.Faces) {
				if (!Helpers.isEmpty(face.Material.Texture)) {
					LoadImage(face.Material);
				}
			}
		}
	}

	void LoadImage(ModelMaterial material) {
		File fname = new File(FileName.getParentFile(), material.Texture);
		try {
			String ext = Helpers.getFileExtension(material.Texture).toLowerCase();

			ManagedImage image = null;

			switch (ext) {
			case "jp2":
			case "j2c":
				material.TextureData = FileUtils.readFileToByteArray(fname);
				return;
			case "tga":
				image = TGAImage.decode(fname);
				break;
			default:
				image = ManagedImage.decode(fname);
				break;
			}

			int width = image.getWidth();
			int height = image.getHeight();

			// Handle resizing to prevent excessively large images and irregular dimensions
			if (!IsPowerOfTwo(width) || !IsPowerOfTwo(height) || width > 1024 || height > 1024) {
				int origWidth = width;
				int origHieght = height;

				width = ClosestPowerOfTwo(width);
				height = ClosestPowerOfTwo(height);

				width = width > 1024 ? 1024 : width;
				height = height > 1024 ? 1024 : height;

				logger.info("Image has irregular dimensions " + origWidth + "x" + origHieght + ". Resizing to " + width
						+ "x" + height);

				ManagedImage resized = new ManagedImage(image);
				// FIXME: SmoothingMode.HighQuality && InterpolationMode.HighQualityBicubic
				resized.resizeNearestNeighbor(width, height);

				image = resized;
			}

			material.TextureData = J2KImage.encode(image, false);

			logger.info("Successfully encoded " + fname);
		} catch (Exception ex) {
			logger.warn("Failed loading " + fname + ": " + ex.getMessage());
		}

	}

	private boolean IsPowerOfTwo(int n) {
		return (n & (n - 1)) == 0 && n != 0;
	}

	private int ClosestPowerOfTwo(int n) {
		int res = 1;

		while (res < n) {
			res <<= 1;
		}

		return res > 1 ? res / 2 : 1;
	}

	ModelMaterial ExtractMaterial(Object diffuse) {
		ModelMaterial ret = new ModelMaterial();
		if (diffuse instanceof CommonColorOrTextureType.Color) {
			CommonColorOrTextureType.Color col = (CommonColorOrTextureType.Color) diffuse;
			List<Double> values = col.getValue();
			ret.DiffuseColor = new Color4(values.get(0).floatValue(), values.get(1).floatValue(),
					values.get(2).floatValue(), values.get(3).floatValue());
		} else if (diffuse instanceof CommonColorOrTextureType.Texture) {
			CommonColorOrTextureType.Texture tex = (CommonColorOrTextureType.Texture) diffuse;
			ret.Texture = tex.getTexture();
		}
		return ret;

	}

	void ParseMaterials() {

		if (Model == null)
			return;

		Materials = new ArrayList<ModelMaterial>();

		// Material -> effect mapping
		HashMap<String, String> matEffect = new HashMap<String, String>();
		List<ModelMaterial> tmpEffects = new ArrayList<ModelMaterial>();

		// Image ID -> filename mapping
		HashMap<String, String> imgMap = new HashMap<String, String>();

		for (Object item : Model.getItems()) {
			if (item instanceof LibraryImages) {
				LibraryImages images = (LibraryImages) item;
				if (images.getImage() != null) {
					for (Image image : images.getImage()) {
						String ID = image.getId();
						Object imageItem = image.getItem();
						if (imageItem instanceof String) {
							imgMap.put(ID, (String) imageItem);
						}
					}
				}
			}
		}

		for (Object item : Model.getItems()) {
			if (item instanceof LibraryMaterials) {
				LibraryMaterials materials = (LibraryMaterials) item;
				if (materials.getMaterial() != null) {
					for (Material material : materials.getMaterial()) {
						String ID = material.getId();
						if (!Helpers.isEmpty(material.getInstanceEffect().getUrl())) {
							matEffect.put(material.getInstanceEffect().getUrl().substring(1), ID);
						}
					}
				}
			}
		}

		for (Object item : Model.getItems()) {
			if (item instanceof LibraryEffects) {
				LibraryEffects effects = (LibraryEffects) item;
				if (effects.getEffect() != null) {
					for (Effect effect : effects.getEffect()) {
						String ID = effect.getId();
						for (Object effItem : effect.getFxProfileAbstract()) {
							if (effItem instanceof ProfileCOMMON) {
								ProfileCOMMON.Technique teq = ((ProfileCOMMON) effItem).getTechnique();
								if (teq != null) {
									if (teq.getItem() instanceof ProfileCOMMON.Technique.Phong) {
										ProfileCOMMON.Technique.Phong shader = (ProfileCOMMON.Technique.Phong) teq
												.getItem();
										if (shader.getDiffuse() != null) {
											ModelMaterial material = ExtractMaterial(shader.getDiffuse().getItem());
											material.ID = ID;
											tmpEffects.add(material);
										}
									} else if (teq.getItem() instanceof ProfileCOMMON.Technique.Lambert) {
										ProfileCOMMON.Technique.Lambert shader = (ProfileCOMMON.Technique.Lambert) teq
												.getItem();
										if (shader.getDiffuse() != null) {
											ModelMaterial material = ExtractMaterial(shader.getDiffuse().getItem());
											material.ID = ID;
											tmpEffects.add(material);
										}
									}
								}
							}
						}
					}
				}
			}
		}

		for (ModelMaterial effect : tmpEffects) {
			if (matEffect.containsKey(effect.ID)) {
				effect.ID = matEffect.get(effect.ID);
				if (!Helpers.isEmpty(effect.Texture)) {
					if (imgMap.containsKey(effect.Texture)) {
						effect.Texture = imgMap.get(effect.Texture);
					}
				}
				Materials.add(effect);
			}
		}
	}

	void ProcessNode(Node node) {
		LoaderNode n = new LoaderNode();
		n.ID = node.getId();

		if (node.getLookatOrMatrixOrRotate() != null)
			// Try finding matrix
			for (Object i : node.getLookatOrMatrixOrRotate()) {
				if (i instanceof Matrix) {
					Matrix m = (Matrix) i;
					for (int a = 0; a < 4; a++)
						for (int b = 0; b < 4; b++) {
							n.Transform.setItem(b, a, m.getValue().get(a * 4 + b).floatValue());
						}
				}
			}

		// Find geometry and material
		if (node.getInstanceGeometry() != null && node.getInstanceGeometry().size() > 0) {
			InstanceGeometry instGeom = node.getInstanceGeometry().get(0);
			if (!Helpers.isEmpty(instGeom.getUrl())) {
				n.MeshID = instGeom.getUrl().substring(1);
			}
			if (instGeom.getBindMaterial().getTechniqueCommon() != null) {
				for (InstanceMaterial teq : instGeom.getBindMaterial().getTechniqueCommon().getInstanceMaterial()) {
					String target = teq.getName();
					if (Helpers.isEmpty(target))
						continue;

					target = target.substring(1);
					MatSymTarget.put(teq.getSymbol(), target);
				}
			}
		}

		if (node.getNode() != null && node.getInstanceGeometry() != null && node.getInstanceGeometry().size() > 0)
			Nodes.add(n);

		// Recurse if the scene is hierarchical
		if (node.getNode() != null)
			for (Node nd : node.getNode())
				ProcessNode(nd);
	}

	void ParseVisualScene() {
		Nodes = new ArrayList<LoaderNode>();
		if (Model == null)
			return;

		MatSymTarget = new HashMap<String, String>();

		for (Object item : Model.getItems()) {
			if (item instanceof LibraryVisualScenes) {
				VisualScene scene = ((LibraryVisualScenes) item).getVisualScene().get(0);
				for (Node node : scene.getNode()) {
					ProcessNode(node);
				}
			}
		}
	}

	List<ModelPrim> Parse() throws IOException {
		List<ModelPrim> Prims = new ArrayList<ModelPrim>();

		float DEG_TO_RAD = 0.017453292519943295769236907684886f;

		if (Model == null)
			return Prims;

		Matrix4 transform = Matrix4.Identity;

		UpAxisType upAxis = UpAxisType.Y_UP;

		Asset asset = Model.getAsset();
		if (asset != null) {
			upAxis = asset.getUpAxis();
			if (asset.getUnit() != null) {
				float meter = (float) asset.getUnit().getMeter();
				transform.setItem(0, 0, meter);
				transform.setItem(1, 1, meter);
				transform.setItem(2, 2, meter);
			}
		}

		Matrix4 rotation = Matrix4.Identity;

		if (upAxis == UpAxisType.X_UP) {
			rotation = Matrix4.createFromEulers(0.0f, 90.0f * DEG_TO_RAD, 0.0f);
		} else if (upAxis == UpAxisType.Y_UP) {
			rotation = Matrix4.createFromEulers(90.0f * DEG_TO_RAD, 0.0f, 0.0f);
		}

		rotation = Matrix4.multiply(rotation, transform);
		transform = rotation;

		ParseVisualScene();
		ParseMaterials();

		for (Object item : Model.getItems()) {
			if (item instanceof LibraryGeometries) {
				LibraryGeometries geometries = (LibraryGeometries) item;
				for (Geometry geo : geometries.getGeometry()) {
					Mesh mesh = geo.getMesh();
					if (mesh == null)
						continue;

					// Find all instances of this geometry
					List<LoaderNode> nodes = new ArrayList<LoaderNode>();
					for (LoaderNode node : Nodes) {
						if (node.MeshID.equals(geo.getId()))
							nodes.add(node);
					}
					if (!nodes.isEmpty()) {
						ModelPrim firstPrim = null; // The first prim is actually calculated, the others are just copied
													// from it.

						Vector3 asset_scale = new Vector3(1, 1, 1);
						Vector3 asset_offset = new Vector3(0, 0, 0); // Scale and offset between Collada and OS asset
																		// (Which is always in a unit cube)

						for (LoaderNode node : nodes) {
							ModelPrim prim = new ModelPrim();
							prim.ID = node.ID;
							Prims.add(prim);

							// First node is used to create the asset. This is as the code to crate the byte
							// array is somewhat
							// erroneously placed in the ModelPrim class.
							if (firstPrim == null) {
								firstPrim = prim;
								AddPositions(asset_scale, asset_offset, mesh, prim, transform); // transform is used
																								// only for inch ->
																								// meter and up axis
																								// transform.

								for (Object mitem : mesh.getLinesOrLinestripsOrPolygons()) {
									if (mitem instanceof Triangles)
										AddFacesFromPolyList(Triangles2Polylist((Triangles) mitem), mesh, prim,
												transform); // Transform is used to turn normals according to up axis
									if (mitem instanceof Polylist)
										AddFacesFromPolyList((Polylist) mitem, mesh, prim, transform);
								}
								prim.CreateAsset(UUID.Zero);
							} else {
								// Copy the values set by AddPositions and AddFacesFromPolyList as these are the
								// same as long as the mesh is the same
								prim.Asset = firstPrim.Asset;
								prim.BoundMin = firstPrim.BoundMin;
								prim.BoundMax = firstPrim.BoundMax;
								prim.Positions = firstPrim.Positions;
								prim.Faces = firstPrim.Faces;
							}

							// Note: This ignores any shear or similar non-linear effects. This can cause
							// some problems but it
							// is unlikely that authoring software can generate such matrices.
							node.Transform.decompose(prim.Scale, prim.Rotation, prim.Position);
							RefObject<Float> roll = new RefObject<Float>(0.0f), pitch = new RefObject<Float>(0.0f),
									yaw = new RefObject<Float>(0.0f);
							node.Transform.getEulerAngles(roll, pitch, yaw);

							// The offset created when normalizing the mesh vertices into the OS unit cube
							// must be rotated
							// before being added to the position part of the Collada transform.
							Matrix4 rot = Matrix4.createFromQuaternion(prim.Rotation); // Convert rotation to matrix for
																						// for Transform
							Vector3 offset = Vector3.transform(asset_offset.multiply(prim.Scale), rot); // The offset
																										// must be
																										// rotated and
																										// mutiplied by
																										// the Collada
																										// file's scale
																										// as the offset
																										// is added
																										// during
																										// rendering
																										// with the unit
																										// cube mesh
																										// already
																										// multiplied by
																										// the compound
																										// scale.
							prim.Position.add(offset);
							prim.Scale.multiply(asset_scale); // Modify scale from Collada instance by the rescaling
																// done in AddPositions()
						}
					}
				}
			}
		}
		return Prims;
	}

	Source FindSource(List<Source> sources, String id) {
		id = id.substring(1);

		for (Source src : sources) {
			if (src.getId().equals(id))
				return src;
		}
		return new Source();
	}

	void AddPositions(Vector3 scale, Vector3 offset, Mesh mesh, ModelPrim prim, Matrix4 transform) {
		prim.Positions = new ArrayList<Vector3>();
		Source posSrc = FindSource(mesh.getSource(), mesh.getVertices().getInput().get(0).getSource());
		Double[] posVals = null;
		posVals = posSrc.getFloatArray().getValue().toArray(posVals);

		for (int i = 0; i < posVals.length / 3; i++) {
			Vector3 pos = new Vector3(posVals[i * 3].floatValue(), posVals[i * 3 + 1].floatValue(),
					posVals[i * 3 + 2].floatValue());
			pos = Vector3.transform(pos, transform);
			prim.Positions.add(pos);
		}

		prim.BoundMin = new Vector3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		prim.BoundMax = new Vector3(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

		for (Vector3 pos : prim.Positions) {
			if (pos.X > prim.BoundMax.X)
				prim.BoundMax.X = pos.X;
			if (pos.Y > prim.BoundMax.Y)
				prim.BoundMax.Y = pos.Y;
			if (pos.Z > prim.BoundMax.Z)
				prim.BoundMax.Z = pos.Z;

			if (pos.X < prim.BoundMin.X)
				prim.BoundMin.X = pos.X;
			if (pos.Y < prim.BoundMin.Y)
				prim.BoundMin.Y = pos.Y;
			if (pos.Z < prim.BoundMin.Z)
				prim.BoundMin.Z = pos.Z;
		}

		scale = Vector3.subtract(prim.BoundMax, prim.BoundMin);
		offset = Vector3.add(prim.BoundMin, Vector3.divide(scale, 2));

		// Fit vertex positions into identity cube -0.5 .. 0.5
		for (int i = 0; i < prim.Positions.size(); i++) {
			Vector3 pos = prim.Positions.get(i);
			pos = new Vector3(scale.X == 0 ? 0 : ((pos.X - prim.BoundMin.X) / scale.X) - 0.5f,
					scale.Y == 0 ? 0 : ((pos.Y - prim.BoundMin.Y) / scale.Y) - 0.5f,
					scale.Z == 0 ? 0 : ((pos.Z - prim.BoundMin.Z) / scale.Z) - 0.5f);
			prim.Positions.set(i, pos);
		}
	}

	int[] StrToArray(String s) {
		String[] vals = s.trim().split("\\s+");
		int[] ret = new int[vals.length];

		for (int i = 0; i < ret.length; i++) {
			ret[i] = Integer.parseInt(vals[i]);
		}

		return ret;
	}

	void AddFacesFromPolyList(Polylist list, Mesh mesh, ModelPrim prim, Matrix4 transform)
			throws InvalidObjectException {
		// String material = list.getMaterial();
		Source posSrc = null;
		Source normalSrc = null;
		Source uvSrc = null;

		long stride = 0;
		int posOffset = -1;
		int norOffset = -1;
		int uvOffset = -1;

		for (InputLocalOffset inp : list.getInput()) {
			stride = Math.max(stride, inp.getOffset().longValue());

			String semantic = inp.getSemantic();
			if (semantic.equals("VERTEX")) {
				posSrc = FindSource(mesh.getSource(), mesh.getVertices().getInput().get(0).getSource());
				posOffset = inp.getOffset().intValue();
			} else if (semantic.equals("NORMAL")) {
				normalSrc = FindSource(mesh.getSource(), inp.getSource());
				norOffset = inp.getOffset().intValue();
			} else if (semantic.equals("TEXCOORD")) {
				uvSrc = FindSource(mesh.getSource(), inp.getSource());
				uvOffset = inp.getOffset().intValue();
			}
		}

		stride += 1;

		if (posSrc == null)
			return;

		Integer[] vcount = list.getVcount().toArray(new Integer[0]);
		Integer[] idx = list.getP().toArray(new Integer[0]);

		Vector3[] normals = null;
		if (normalSrc != null) {
			Double[] norVal = normalSrc.getFloatArray().getValue().toArray(new Double[0]);
			normals = new Vector3[norVal.length / 3];

			for (int i = 0; i < normals.length; i++) {
				normals[i] = new Vector3(norVal[i * 3 + 0].floatValue(), norVal[i * 3 + 1].floatValue(),
						norVal[i * 3 + 2].floatValue());
				normals[i] = Vector3.transformNormal(normals[i], transform);
				normals[i].normalize();
			}
		}

		Vector2[] uvs = null;
		if (uvSrc != null) {
			Double[] uvVal = uvSrc.getFloatArray().getValue().toArray(new Double[0]);
			uvs = new Vector2[uvVal.length / 2];

			for (int i = 0; i < uvs.length; i++) {
				uvs[i] = new Vector2(uvVal[i * 2 + 0].floatValue(), uvVal[i * 2 + 1].floatValue());
			}

		}

		ModelFace face = new ModelFace();
		face.MaterialID = list.getMaterial();
		if (face.MaterialID != null) {
			if (MatSymTarget.containsKey(list.getMaterial())) {
				for (ModelMaterial mat : Materials) {
					if (mat.ID.equals(MatSymTarget.get(list.getMaterial()))) {
						face.Material = mat;
					}
				}
			}
		}

		int curIdx = 0;

		for (int nvert : vcount) {
			if (nvert < 3 || nvert > 4) {
				throw new InvalidObjectException("Only triangles and quads supported");
			}

			Vertex[] verts = new Vertex[nvert];
			for (int j = 0; j < nvert; j++) {
				verts[j].Position = prim.Positions.get(idx[curIdx + posOffset + (int) stride * j]);

				if (normals != null) {
					verts[j].Normal = normals[idx[curIdx + norOffset + (int) stride * j]];
				}

				if (uvs != null) {
					verts[j].TexCoord = uvs[idx[curIdx + uvOffset + (int) stride * j]];
				}
			}

			switch (nvert) {
			case 3:
				face.AddVertex(verts[0]);
				face.AddVertex(verts[1]);
				face.AddVertex(verts[2]);
				break;
			case 4:
				face.AddVertex(verts[0]);
				face.AddVertex(verts[1]);
				face.AddVertex(verts[2]);

				face.AddVertex(verts[0]);
				face.AddVertex(verts[2]);
				face.AddVertex(verts[3]);
				break;
			}

			curIdx += (int) stride * nvert;
		}

		prim.Faces.add(face);

	}

	Polylist Triangles2Polylist(Triangles triangles) {
		Polylist poly = new Polylist();
		poly.setCount(triangles.getCount());
		poly.setInput(triangles.getInput());
		poly.setMaterial(triangles.getMaterial());
		poly.setName(triangles.getName());
		poly.setP(triangles.getP());

		List<BigInteger> count = poly.getVcount();
		BigInteger value = new BigInteger("3");
		count.clear();

		for (int i = 0; i < poly.getCount().intValue(); i++) {
			count.add(value);
		}
		return poly;
	}

}
