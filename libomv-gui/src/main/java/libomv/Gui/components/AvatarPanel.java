/**
 * Copyright (c) 2009-2017, Frederick Martian
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the name of the libomv-java project nor the names of its
 *   contributors may be used to endorse or promote products derived from
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
package libomv.Gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;

import libomv.io.GridClient;
import libomv.rendering.LindenMesh;
import libomv.types.Vector3;

public class AvatarPanel extends JFrame implements ActionListener, MouseListener {
	private static final Logger logger = Logger.getLogger(AvatarPanel.class);
	private static final long serialVersionUID = 1L;

	private JPanel jPnSettings;
	private JPanel jPnTextures;

	private JSlider jSldRoll;
	private JSlider jSldPitch;
	private JSlider jSldYaw;
	private JSlider jSldZoom;
	private JTextField jTxtAnimPath;

	private GLCanvas glControl;

	private GridClient _client;

	private HashMap<String, LindenMesh> _meshes = new HashMap<String, LindenMesh>();
	private boolean _wireframe = true;
	private boolean _showSkirt = false;

	static GLU glu = new GLU();

	public AvatarPanel(GridClient client) {
		JTabbedPane tabs = new JTabbedPane();
		tabs.setMinimumSize(new Dimension(300, 300));
		tabs.setTabPlacement(JTabbedPane.TOP);
		tabs.addTab("Avatar Settings", null, getJPnSettings(), null);
		tabs.addTab("Textures", null, getJPnTextures(), null);

		getContentPane().setLayout(new BorderLayout(1, 1));
		getContentPane().add(tabs, BorderLayout.EAST);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel.setBackground(new Color(240, 240, 240));
		getContentPane().add(panel, BorderLayout.WEST);
		// panel.add(getGLControl());
		// getGLControl().requestFocus();

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenu mnOpen = new JMenu("Open");
		mnFile.add(mnOpen);

		JMenuItem mntmOpen = new JMenuItem("Avatar");
		mnOpen.add(mntmOpen);
		mntmOpen.setActionCommand("avatar");
		mntmOpen.addActionListener(this);

		// JMenuItem mntmTexture = new JMenuItem("Texture");
		// mntmTexture.setActionCommand("texture");
		// mntmTexture.addActionListener(this);
		// mnOpen.add(mntmTexture);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.setActionCommand("exit");
		mntmExit.addActionListener(this);
		mnFile.add(mntmExit);

		_client = client;
	}

	private File FileDialog(String description, String... extensions) {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new FileNameExtensionFilter(description, extensions));
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile();
		}
		logger.debug("Open command cancelled by user.");
		return null;
	}

	private void loadAvatarMesh(File file) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			// Use the factory to create a builder
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file);
			// Get a list of all meshes in the document
			NodeList meshes = doc.getElementsByTagName("mesh");
			for (int i = 0; i < meshes.getLength(); i++) {
				Node meshNode = meshes.item(i);
				String type = meshNode.getAttributes().getNamedItem("type").getNodeValue();
				int lod = Integer.parseInt(meshNode.getAttributes().getNamedItem("lod").getNodeValue());
				String fileName = meshNode.getAttributes().getNamedItem("file_name").getNodeValue();

				// Mash up the filename with the current path
				fileName = new File(file.getParentFile(), fileName).getPath();

				// LindenMesh mesh = (_meshes.containsKey(type) ? _meshes.get(type) : new
				// LindenMesh(_client, type));
				LindenMesh mesh = (_meshes.containsKey(type) ? _meshes.get(type) : new LindenMesh(type));
				if (lod == 0) {
					mesh.load(fileName);
				} else {
					mesh.loadLod(lod, fileName);
				}

				_meshes.put(type, mesh);
				getGLControl().invalidate();
			}
		} catch (Exception ex) {
			logger.error("", ex);
		}
	}

	private void loadTexture(File file, ImagePanel panel) {
		// file.
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("animation")) {
			File file = FileDialog("Animation File (*.animation, *.bvh)", "animation", "bvh");
			if (file != null) {

			}
		} else if (e.getActionCommand().equals("avatar")) {
			File file = FileDialog("Avatar File (avatar_lad.xml)", "xml");
			if (file != null) {
				loadAvatarMesh(file);
			}
		}
		if (e.getActionCommand().equals("exit")) {
			System.exit(0);
		}
	}

	private class AvatarViewer implements GLEventListener {
		@Override
		public void init(GLAutoDrawable drawable) {
			final GL2 gl = drawable.getGL().getGL2();

			gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
			gl.glClearColor(0f, 0f, 0f, 0f);
			gl.glClearDepth(1.0f);
			gl.glEnable(GL.GL_DEPTH_TEST);
			gl.glDepthMask(true);
			gl.glDepthFunc(GL.GL_LEQUAL);
			gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
		}

		@Override
		public void display(GLAutoDrawable drawable) {
			final GL2 gl = drawable.getGL().getGL2();

			gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
			gl.glLoadIdentity();

			// Setup wireframe or solid fill drawing mode
			if (_wireframe)
				gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
			else
				gl.glPolygonMode(GL.GL_FRONT, GL2.GL_FILL);

			// Push the world matrix
			gl.glPushMatrix();

			gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);

			// World rotations
			gl.glRotatef(getJSldRoll().getValue(), 1f, 0f, 0f);
			gl.glRotatef(getJSldPitch().getValue(), 0f, 1f, 0f);
			gl.glRotatef(getJSldYaw().getValue(), 0f, 0f, 1f);

			if (_meshes.size() > 0) {
				for (LindenMesh mesh : _meshes.values()) {
					if (!_showSkirt && mesh.getName() == "skirtMesh")
						continue;

					gl.glColor3f(1f, 1f, 1f);

					// Individual prim matrix
					gl.glPushMatrix();

					// gl.glTranslatef(mesh.Position.X, mesh.Position.Y, mesh.Position.Z);

					gl.glRotatef(mesh.getRotationAngles().x, 1f, 0f, 0f);
					gl.glRotatef(mesh.getRotationAngles().y, 0f, 1f, 0f);
					gl.glRotatef(mesh.getRotationAngles().z, 0f, 0f, 1f);

					gl.glScalef(mesh.getScale().x, mesh.getScale().y, mesh.getScale().z);

					// TODO: Texturing

					gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, mesh.texCoords);
					gl.glVertexPointer(3, GL.GL_FLOAT, 0, mesh.vertices);
					gl.glDrawElements(GL.GL_TRIANGLES, mesh.indices.capacity(), GL.GL_UNSIGNED_SHORT, mesh.indices);
				}
			}

			// Pop the world matrix
			gl.glPopMatrix();

			gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
			gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);

			gl.glFlush();
		}

		@Override
		public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
			final GL2 gl = drawable.getGL().getGL2();

			if (height <= 0) {
				height = 1;
			}
			float aspect = (float) width / (float) height;

			// GL.glClearColor(0.39f, 0.58f, 0.93f, 1.0f); // Cornflower blue anyone?
			gl.glClearColor(0f, 0f, 0f, 1f);

			gl.glPushMatrix();
			gl.glMatrixMode(GL2.GL_PROJECTION);
			gl.glLoadIdentity();

			glu.gluPerspective(50.0, aspect, 0.001, 50);

			Vector3 center = Vector3.ZERO;
			if (_meshes.containsKey("headMesh") && _meshes.containsKey("lowerBodyMesh")) {
				LindenMesh head = _meshes.get("headMesh"), lowerBody = _meshes.get("lowerBodyMesh");
				center = Vector3.divide(Vector3.add(head.getCenter(), lowerBody.getCenter()), 2f);
			}

			double value = getJSldZoom().getValue() * 0.1d + center.y;
			glu.gluLookAt(center.x, value, center.z, center.x, value + 1d, center.z, 0d, 0d, 1d);

			gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		}

		@Override
		public void dispose(GLAutoDrawable drawable) {
			final GL2 gl = drawable.getGL().getGL2();
			// TODO Auto-generated method stub
			gl.glFinish();
		}
	}

	protected GLCanvas getGLControl() {
		if (glControl == null) {
			glControl = new GLCanvas();
			glControl.addGLEventListener(new AvatarViewer());
		}
		return glControl;
	}

	protected JPanel getJPnSettings() {
		if (jPnSettings == null) {
			jPnSettings = new JPanel();
			jPnSettings.setLayout(new BorderLayout(1, 1));

			JPanel jPnSliders = new JPanel();
			jPnSliders.setBorder(new EmptyBorder(10, 10, 10, 10));
			jPnSettings.add(jPnSliders, BorderLayout.NORTH);

			GridBagLayout gbl_jPnSettings = new GridBagLayout();
			gbl_jPnSettings.columnWidths = new int[] { 0, 0, 0 };
			gbl_jPnSettings.rowHeights = new int[] { 0, 0, 0, 0, 0 };
			gbl_jPnSettings.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
			gbl_jPnSettings.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
			jPnSliders.setLayout(gbl_jPnSettings);

			JLabel jLblRoll = new JLabel("Roll");
			GridBagConstraints gridBagConstraint = new GridBagConstraints();
			gridBagConstraint.anchor = GridBagConstraints.WEST;
			gridBagConstraint.fill = GridBagConstraints.VERTICAL;
			gridBagConstraint.insets = new Insets(0, 0, 5, 5);
			gridBagConstraint.gridx = 0;
			gridBagConstraint.gridy = 0;
			jPnSliders.add(jLblRoll, gridBagConstraint);

			gridBagConstraint = new GridBagConstraints();
			gridBagConstraint.anchor = GridBagConstraints.CENTER;
			gridBagConstraint.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraint.insets = new Insets(0, 0, 5, 0);
			gridBagConstraint.gridx = 1;
			gridBagConstraint.gridy = 0;
			jPnSliders.add(getJSldRoll(), gridBagConstraint);

			JLabel jLblPitch = new JLabel("Pitch");
			gridBagConstraint = new GridBagConstraints();
			gridBagConstraint.anchor = GridBagConstraints.WEST;
			gridBagConstraint.fill = GridBagConstraints.VERTICAL;
			gridBagConstraint.insets = new Insets(0, 0, 5, 5);
			gridBagConstraint.gridx = 0;
			gridBagConstraint.gridy = 1;
			jPnSliders.add(jLblPitch, gridBagConstraint);

			gridBagConstraint = new GridBagConstraints();
			gridBagConstraint.anchor = GridBagConstraints.CENTER;
			gridBagConstraint.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraint.insets = new Insets(0, 0, 5, 0);
			gridBagConstraint.gridx = 1;
			gridBagConstraint.gridy = 1;
			jPnSliders.add(getJSldPitch(), gridBagConstraint);

			JLabel jLblYaw = new JLabel("Yaw");
			gridBagConstraint = new GridBagConstraints();
			gridBagConstraint.anchor = GridBagConstraints.WEST;
			gridBagConstraint.fill = GridBagConstraints.VERTICAL;
			gridBagConstraint.insets = new Insets(0, 0, 5, 5);
			gridBagConstraint.gridx = 0;
			gridBagConstraint.gridy = 2;
			jPnSliders.add(jLblYaw, gridBagConstraint);

			gridBagConstraint = new GridBagConstraints();
			gridBagConstraint.anchor = GridBagConstraints.CENTER;
			gridBagConstraint.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraint.insets = new Insets(0, 0, 5, 0);
			gridBagConstraint.gridx = 1;
			gridBagConstraint.gridy = 2;
			jPnSliders.add(getJSldYaw(), gridBagConstraint);

			JLabel jLblZoom = new JLabel("Zoom");
			gridBagConstraint = new GridBagConstraints();
			gridBagConstraint.anchor = GridBagConstraints.WEST;
			gridBagConstraint.fill = GridBagConstraints.VERTICAL;
			gridBagConstraint.insets = new Insets(0, 0, 0, 5);
			gridBagConstraint.gridx = 0;
			gridBagConstraint.gridy = 3;
			jPnSliders.add(jLblZoom, gridBagConstraint);

			gridBagConstraint = new GridBagConstraints();
			gridBagConstraint.anchor = GridBagConstraints.CENTER;
			gridBagConstraint.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraint.gridx = 1;
			gridBagConstraint.gridy = 3;
			jPnSliders.add(getJSldZoom(), gridBagConstraint);

			JPanel jPnAnimation = new JPanel();
			jPnAnimation.setBorder(new EmptyBorder(10, 10, 10, 10));
			jPnSettings.add(jPnAnimation, BorderLayout.SOUTH);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[] { 0, 0, 0 };
			gbl_panel.rowHeights = new int[] { 0, 0, 0 };
			gbl_panel.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
			gbl_panel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
			jPnAnimation.setLayout(gbl_panel);

			JLabel lblAnimation = new JLabel("Animation");
			GridBagConstraints gbc_lblAnimation = new GridBagConstraints();
			gbc_lblAnimation.anchor = GridBagConstraints.WEST;
			gbc_lblAnimation.insets = new Insets(0, 0, 5, 5);
			gbc_lblAnimation.gridx = 0;
			gbc_lblAnimation.gridy = 0;
			jPnAnimation.add(lblAnimation, gbc_lblAnimation);

			gridBagConstraint = new GridBagConstraints();
			gridBagConstraint.anchor = GridBagConstraints.CENTER;
			gridBagConstraint.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraint.insets = new Insets(0, 0, 0, 5);
			gridBagConstraint.gridx = 0;
			gridBagConstraint.gridy = 1;
			jPnAnimation.add(getJTxtAnimPath(), gridBagConstraint);

			JButton jBtnAnimPath = new JButton("Browse");
			jBtnAnimPath.setMnemonic(KeyEvent.VK_B);
			jBtnAnimPath.setActionCommand("animation");
			jBtnAnimPath.addActionListener(this);

			gridBagConstraint = new GridBagConstraints();
			gridBagConstraint.anchor = GridBagConstraints.CENTER;
			gridBagConstraint.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraint.gridx = 1;
			gridBagConstraint.gridy = 1;
			jPnAnimation.add(jBtnAnimPath, gridBagConstraint);

			JPanel panel_1 = new JPanel();
			panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
			jPnSettings.add(panel_1, BorderLayout.CENTER);
		}
		return jPnSettings;
	}

	private class SliderChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			getGLControl().invalidate();
		}
	}

	protected JSlider getJSldRoll() {
		if (jSldRoll == null) {
			jSldRoll = new JSlider();
			jSldRoll.addChangeListener(new SliderChangeListener());
		}
		return jSldRoll;
	}

	public JSlider getJSldPitch() {
		if (jSldPitch == null) {
			jSldPitch = new JSlider();
			jSldPitch.addChangeListener(new SliderChangeListener());
		}
		return jSldPitch;
	}

	public JSlider getJSldYaw() {
		if (jSldYaw == null) {
			jSldYaw = new JSlider();
			jSldYaw.addChangeListener(new SliderChangeListener());
		}
		return jSldYaw;
	}

	public JSlider getJSldZoom() {
		if (jSldZoom == null) {
			jSldZoom = new JSlider();
			jSldZoom.addChangeListener(new SliderChangeListener());
		}
		return jSldZoom;
	}

	protected JTextField getJTxtAnimPath() {
		if (jTxtAnimPath == null) {
			jTxtAnimPath = new JTextField();
			jTxtAnimPath.setColumns(10);
		}
		return jTxtAnimPath;
	}

	protected JPanel getJPnTextures() {
		if (jPnTextures == null) {
			jPnTextures = new JPanel();
			jPnTextures.setBorder(new EmptyBorder(5, 5, 5, 5));

			GridBagLayout gbl_jPnTextures = new GridBagLayout();
			gbl_jPnTextures.columnWidths = new int[] { 60, 8, 60, 8, 60, 8, 60, 8, 60, 8, 60, 8, 60, 1 };
			gbl_jPnTextures.rowHeights = new int[] { 14, 60, 14, 60, 14, 60, 14, 60, 14, 60, 1 };
			gbl_jPnTextures.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
					0.0, Double.MIN_VALUE };
			gbl_jPnTextures.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
					Double.MIN_VALUE };
			jPnTextures.setLayout(gbl_jPnTextures);

			JLabel lblHead = new JLabel("Head");
			GridBagConstraints gbc_lblHead = new GridBagConstraints();
			gbc_lblHead.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblHead.insets = new Insets(0, 0, 5, 5);
			gbc_lblHead.gridx = 0;
			gbc_lblHead.gridy = 0;
			jPnTextures.add(lblHead, gbc_lblHead);

			ImagePanel panel11 = new ImagePanel();
			panel11.setMinimumSize(new Dimension(60, 60));
			panel11.setBorder(new LineBorder(new Color(0, 0, 0)));
			panel11.addMouseListener(this);
			GridBagConstraints gbc_panel11 = new GridBagConstraints();
			gbc_panel11.fill = GridBagConstraints.BOTH;
			gbc_panel11.insets = new Insets(0, 0, 5, 5);
			gbc_panel11.gridx = 0;
			gbc_panel11.gridy = 1;
			jPnTextures.add(panel11, gbc_panel11);

			JLabel plus11 = new JLabel("+");
			GridBagConstraints gbc_plus11 = new GridBagConstraints();
			gbc_plus11.fill = GridBagConstraints.VERTICAL;
			gbc_plus11.insets = new Insets(0, 0, 5, 5);
			gbc_plus11.gridx = 1;
			gbc_plus11.gridy = 1;
			jPnTextures.add(plus11, gbc_plus11);

			ImagePanel panel12 = new ImagePanel();
			panel12.setMinimumSize(new Dimension(60, 60));
			panel12.setBorder(new LineBorder(new Color(0, 0, 0)));
			panel12.addMouseListener(this);
			GridBagConstraints gbc_panel12 = new GridBagConstraints();
			gbc_panel12.fill = GridBagConstraints.BOTH;
			gbc_panel12.insets = new Insets(0, 0, 5, 5);
			gbc_panel12.gridx = 2;
			gbc_panel12.gridy = 1;
			jPnTextures.add(panel12, gbc_panel12);

			JLabel equal1 = new JLabel("=");
			GridBagConstraints gbc_equal1 = new GridBagConstraints();
			gbc_equal1.fill = GridBagConstraints.VERTICAL;
			gbc_equal1.insets = new Insets(0, 0, 5, 5);
			gbc_equal1.gridx = 3;
			gbc_equal1.gridy = 1;
			jPnTextures.add(equal1, gbc_equal1);

			ImagePanel panel13 = new ImagePanel();
			panel13.setMinimumSize(new Dimension(60, 60));
			panel13.setBorder(new LineBorder(new Color(0, 0, 0)));
			panel13.addMouseListener(this);
			GridBagConstraints gbc_panel13 = new GridBagConstraints();
			gbc_panel13.fill = GridBagConstraints.BOTH;
			gbc_panel13.insets = new Insets(0, 0, 5, 5);
			gbc_panel13.gridx = 4;
			gbc_panel13.gridy = 1;
			jPnTextures.add(panel13, gbc_panel13);

			JLabel lblEyes = new JLabel("Eyes");
			GridBagConstraints gbc_lblEyes = new GridBagConstraints();
			gbc_lblEyes.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblEyes.insets = new Insets(0, 0, 5, 5);
			gbc_lblEyes.gridx = 0;
			gbc_lblEyes.gridy = 2;
			jPnTextures.add(lblEyes, gbc_lblEyes);

			ImagePanel panel21 = new ImagePanel();
			panel21.setMinimumSize(new Dimension(60, 60));
			panel21.setBorder(new LineBorder(new Color(0, 0, 0)));
			panel21.addMouseListener(this);
			GridBagConstraints gbc_panel21 = new GridBagConstraints();
			gbc_panel21.fill = GridBagConstraints.BOTH;
			gbc_panel21.insets = new Insets(0, 0, 5, 5);
			gbc_panel21.gridx = 0;
			gbc_panel21.gridy = 3;
			jPnTextures.add(panel21, gbc_panel21);

			JLabel lblUpper = new JLabel("Upper");
			GridBagConstraints gbc_lblUpper = new GridBagConstraints();
			gbc_lblUpper.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblUpper.insets = new Insets(0, 0, 5, 5);
			gbc_lblUpper.gridx = 0;
			gbc_lblUpper.gridy = 4;
			jPnTextures.add(lblUpper, gbc_lblUpper);

			ImagePanel panel31 = new ImagePanel();
			panel31.setMinimumSize(new Dimension(60, 60));
			panel31.setBorder(new LineBorder(new Color(0, 0, 0)));
			panel31.addMouseListener(this);
			GridBagConstraints gbc_panel31 = new GridBagConstraints();
			gbc_panel31.fill = GridBagConstraints.BOTH;
			gbc_panel31.insets = new Insets(0, 0, 5, 5);
			gbc_panel31.gridx = 0;
			gbc_panel31.gridy = 5;
			jPnTextures.add(panel31, gbc_panel31);

			JLabel plus31 = new JLabel("+");
			GridBagConstraints gbc_plus31 = new GridBagConstraints();
			gbc_plus31.fill = GridBagConstraints.VERTICAL;
			gbc_plus31.insets = new Insets(0, 0, 5, 5);
			gbc_plus31.gridx = 1;
			gbc_plus31.gridy = 5;
			jPnTextures.add(plus31, gbc_plus31);

			ImagePanel panel32 = new ImagePanel();
			panel32.setMinimumSize(new Dimension(60, 60));
			panel32.setBorder(new LineBorder(new Color(0, 0, 0)));
			panel32.addMouseListener(this);
			GridBagConstraints gbc_panel32 = new GridBagConstraints();
			gbc_panel32.fill = GridBagConstraints.BOTH;
			gbc_panel32.insets = new Insets(0, 0, 5, 5);
			gbc_panel32.gridx = 2;
			gbc_panel32.gridy = 5;
			jPnTextures.add(panel32, gbc_panel32);

			JLabel plus32 = new JLabel("+");
			GridBagConstraints gbc_plus32 = new GridBagConstraints();
			gbc_plus32.fill = GridBagConstraints.VERTICAL;
			gbc_plus32.insets = new Insets(0, 0, 5, 5);
			gbc_plus32.gridx = 3;
			gbc_plus32.gridy = 5;
			jPnTextures.add(plus32, gbc_plus32);

			ImagePanel panel33 = new ImagePanel();
			panel33.setMinimumSize(new Dimension(60, 60));
			panel33.setBorder(new LineBorder(new Color(0, 0, 0)));
			panel33.addMouseListener(this);
			GridBagConstraints gbc_panel33 = new GridBagConstraints();
			gbc_panel33.fill = GridBagConstraints.BOTH;
			gbc_panel33.insets = new Insets(0, 0, 5, 5);
			gbc_panel33.gridx = 4;
			gbc_panel33.gridy = 5;
			jPnTextures.add(panel33, gbc_panel33);

			JLabel plus33 = new JLabel("+");
			GridBagConstraints gbc_plus33 = new GridBagConstraints();
			gbc_plus33.fill = GridBagConstraints.VERTICAL;
			gbc_plus33.insets = new Insets(0, 0, 5, 5);
			gbc_plus33.gridx = 5;
			gbc_plus33.gridy = 5;
			jPnTextures.add(plus33, gbc_plus33);

			ImagePanel panel34 = new ImagePanel();
			panel34.setMinimumSize(new Dimension(60, 60));
			panel34.setBorder(new LineBorder(new Color(0, 0, 0)));
			panel34.addMouseListener(this);
			GridBagConstraints gbc_panel34 = new GridBagConstraints();
			gbc_panel34.fill = GridBagConstraints.BOTH;
			gbc_panel34.insets = new Insets(0, 0, 5, 5);
			gbc_panel34.gridx = 6;
			gbc_panel34.gridy = 5;
			jPnTextures.add(panel34, gbc_panel34);

			JLabel plus34 = new JLabel("+");
			GridBagConstraints gbc_plus34 = new GridBagConstraints();
			gbc_plus34.fill = GridBagConstraints.VERTICAL;
			gbc_plus34.insets = new Insets(0, 0, 5, 5);
			gbc_plus34.gridx = 7;
			gbc_plus34.gridy = 5;
			jPnTextures.add(plus34, gbc_plus34);

			ImagePanel panel35 = new ImagePanel();
			panel35.setMinimumSize(new Dimension(60, 60));
			panel35.setBorder(new LineBorder(new Color(0, 0, 0)));
			panel35.addMouseListener(this);
			GridBagConstraints gbc_panel35 = new GridBagConstraints();
			gbc_panel35.fill = GridBagConstraints.BOTH;
			gbc_panel35.insets = new Insets(0, 0, 5, 5);
			gbc_panel35.gridx = 8;
			gbc_panel35.gridy = 5;
			jPnTextures.add(panel35, gbc_panel35);

			JLabel equal3 = new JLabel("=");
			GridBagConstraints gbc_equal3 = new GridBagConstraints();
			gbc_equal3.fill = GridBagConstraints.VERTICAL;
			gbc_equal3.insets = new Insets(0, 0, 5, 5);
			gbc_equal3.gridx = 9;
			gbc_equal3.gridy = 5;
			jPnTextures.add(equal3, gbc_equal3);

			ImagePanel panel36 = new ImagePanel();
			panel36.setMinimumSize(new Dimension(60, 60));
			panel36.setBorder(new LineBorder(new Color(0, 0, 0)));
			panel36.addMouseListener(this);
			GridBagConstraints gbc_panel36 = new GridBagConstraints();
			gbc_panel36.fill = GridBagConstraints.BOTH;
			gbc_panel36.insets = new Insets(0, 0, 5, 0);
			gbc_panel36.gridx = 10;
			gbc_panel36.gridy = 5;
			jPnTextures.add(panel36, gbc_panel36);

			JLabel lblLower = new JLabel("Lower");
			GridBagConstraints gbc_lblLower = new GridBagConstraints();
			gbc_lblLower.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblLower.insets = new Insets(0, 0, 5, 5);
			gbc_lblLower.gridx = 0;
			gbc_lblLower.gridy = 6;
			jPnTextures.add(lblLower, gbc_lblLower);

			ImagePanel panel41 = new ImagePanel();
			panel41.setMinimumSize(new Dimension(60, 60));
			panel41.setBorder(new LineBorder(new Color(0, 0, 0)));
			panel41.addMouseListener(this);
			GridBagConstraints gbc_panel41 = new GridBagConstraints();
			gbc_panel41.fill = GridBagConstraints.BOTH;
			gbc_panel41.insets = new Insets(0, 0, 5, 5);
			gbc_panel41.gridx = 0;
			gbc_panel41.gridy = 7;
			jPnTextures.add(panel41, gbc_panel41);

			JLabel plus41 = new JLabel("+");
			GridBagConstraints gbc_plus41 = new GridBagConstraints();
			gbc_plus41.fill = GridBagConstraints.VERTICAL;
			gbc_plus41.insets = new Insets(0, 0, 5, 5);
			gbc_plus41.gridx = 1;
			gbc_plus41.gridy = 7;
			jPnTextures.add(plus41, gbc_plus41);

			ImagePanel panel42 = new ImagePanel();
			panel42.setMinimumSize(new Dimension(60, 60));
			panel42.setBorder(new LineBorder(new Color(0, 0, 0)));
			panel42.addMouseListener(this);
			GridBagConstraints gbc_panel42 = new GridBagConstraints();
			gbc_panel42.fill = GridBagConstraints.BOTH;
			gbc_panel42.insets = new Insets(0, 0, 5, 5);
			gbc_panel42.gridx = 2;
			gbc_panel42.gridy = 7;
			jPnTextures.add(panel42, gbc_panel42);

			JLabel plus42 = new JLabel("+");
			GridBagConstraints gbc_plus42 = new GridBagConstraints();
			gbc_plus42.fill = GridBagConstraints.VERTICAL;
			gbc_plus42.insets = new Insets(0, 0, 5, 5);
			gbc_plus42.gridx = 3;
			gbc_plus42.gridy = 7;
			jPnTextures.add(plus42, gbc_plus42);

			ImagePanel panel43 = new ImagePanel();
			panel43.setMinimumSize(new Dimension(60, 60));
			panel43.setBorder(new LineBorder(new Color(0, 0, 0)));
			panel43.addMouseListener(this);
			GridBagConstraints gbc_panel43 = new GridBagConstraints();
			gbc_panel43.fill = GridBagConstraints.BOTH;
			gbc_panel43.insets = new Insets(0, 0, 5, 5);
			gbc_panel43.gridx = 4;
			gbc_panel43.gridy = 7;
			jPnTextures.add(panel43, gbc_panel43);

			JLabel plus43 = new JLabel("+");
			GridBagConstraints gbc_plus43 = new GridBagConstraints();
			gbc_plus43.fill = GridBagConstraints.VERTICAL;
			gbc_plus43.insets = new Insets(0, 0, 5, 5);
			gbc_plus43.gridx = 5;
			gbc_plus43.gridy = 7;
			jPnTextures.add(plus43, gbc_plus43);

			ImagePanel panel44 = new ImagePanel();
			panel44.setMinimumSize(new Dimension(60, 60));
			panel44.setBorder(new LineBorder(new Color(0, 0, 0)));
			panel44.addMouseListener(this);
			GridBagConstraints gbc_panel44 = new GridBagConstraints();
			gbc_panel44.fill = GridBagConstraints.BOTH;
			gbc_panel44.insets = new Insets(0, 0, 5, 5);
			gbc_panel44.gridx = 6;
			gbc_panel44.gridy = 7;
			jPnTextures.add(panel44, gbc_panel44);

			JLabel plus44 = new JLabel("+");
			GridBagConstraints gbc_plus44 = new GridBagConstraints();
			gbc_plus44.fill = GridBagConstraints.VERTICAL;
			gbc_plus44.insets = new Insets(0, 0, 5, 5);
			gbc_plus44.gridx = 7;
			gbc_plus44.gridy = 7;
			jPnTextures.add(plus44, gbc_plus44);

			ImagePanel panel45 = new ImagePanel();
			panel45.setMinimumSize(new Dimension(60, 60));
			panel45.setBorder(new LineBorder(new Color(0, 0, 0)));
			panel45.addMouseListener(this);
			GridBagConstraints gbc_panel45 = new GridBagConstraints();
			gbc_panel45.fill = GridBagConstraints.BOTH;
			gbc_panel45.insets = new Insets(0, 0, 5, 5);
			gbc_panel45.gridx = 8;
			gbc_panel45.gridy = 7;
			jPnTextures.add(panel45, gbc_panel45);

			JLabel plus45 = new JLabel("+");
			GridBagConstraints gbc_plus45 = new GridBagConstraints();
			gbc_plus45.fill = GridBagConstraints.VERTICAL;
			gbc_plus45.insets = new Insets(0, 0, 5, 5);
			gbc_plus45.gridx = 9;
			gbc_plus45.gridy = 7;
			jPnTextures.add(plus45, gbc_plus45);

			ImagePanel panel46 = new ImagePanel();
			panel46.setMinimumSize(new Dimension(60, 60));
			panel46.setBorder(new LineBorder(new Color(0, 0, 0)));
			panel46.addMouseListener(this);
			GridBagConstraints gbc_panel46 = new GridBagConstraints();
			gbc_panel46.fill = GridBagConstraints.BOTH;
			gbc_panel46.insets = new Insets(0, 0, 5, 5);
			gbc_panel46.gridx = 10;
			gbc_panel46.gridy = 7;
			jPnTextures.add(panel46, gbc_panel46);

			JLabel equal4 = new JLabel("=");
			GridBagConstraints gbc_equal4 = new GridBagConstraints();
			gbc_equal4.fill = GridBagConstraints.VERTICAL;
			gbc_equal4.insets = new Insets(0, 0, 5, 5);
			gbc_equal4.gridx = 11;
			gbc_equal4.gridy = 7;
			jPnTextures.add(equal4, gbc_equal4);

			ImagePanel panel47 = new ImagePanel();
			panel47.setMinimumSize(new Dimension(60, 60));
			panel47.setBorder(new LineBorder(new Color(0, 0, 0)));
			panel47.addMouseListener(this);
			GridBagConstraints gbc_panel47 = new GridBagConstraints();
			gbc_panel47.fill = GridBagConstraints.BOTH;
			gbc_panel47.insets = new Insets(0, 0, 5, 5);
			gbc_panel47.gridx = 12;
			gbc_panel47.gridy = 7;
			jPnTextures.add(panel47, gbc_panel47);

			JLabel lblSkirt = new JLabel("Skirt");
			GridBagConstraints gbc_lblSkirt = new GridBagConstraints();
			gbc_lblSkirt.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblSkirt.insets = new Insets(0, 0, 5, 5);
			gbc_lblSkirt.gridx = 0;
			gbc_lblSkirt.gridy = 8;
			jPnTextures.add(lblSkirt, gbc_lblSkirt);

			ImagePanel panel51 = new ImagePanel();
			panel51.setMinimumSize(new Dimension(60, 60));
			panel51.setBorder(new LineBorder(new Color(0, 0, 0)));
			panel51.addMouseListener(this);
			GridBagConstraints gbc_panel51 = new GridBagConstraints();
			gbc_panel51.fill = GridBagConstraints.BOTH;
			gbc_panel51.insets = new Insets(0, 0, 0, 5);
			gbc_panel51.gridx = 0;
			gbc_panel51.gridy = 9;
			jPnTextures.add(panel51, gbc_panel51);
		}
		return jPnTextures;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		File file = FileDialog("Texture File (*.tga, *.png, *.j2k)", "tga", "png", "j2k");
		if (file != null) {
			loadTexture(file, (ImagePanel) e.getComponent());
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}
}