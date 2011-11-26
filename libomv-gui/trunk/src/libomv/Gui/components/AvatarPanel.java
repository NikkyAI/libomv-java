package libomv.Gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;
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

import libomv.rendering.LindenMesh;
import libomv.types.Vector3;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class AvatarPanel extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	
	private JPanel jPnSettings;
	private JPanel jPnTextures;
	
	private JSlider jSldRoll;
	private JSlider jSldPitch;
	private JSlider jSldYaw;
	private JSlider jSldZoom;
	private JTextField jTxtAnimPath;

    private GLCanvas glControl;

    private HashMap<String, LindenMesh> _meshes = new HashMap<String, LindenMesh>();
    private boolean _wireframe = true;
    private boolean _showSkirt = false;

    static GLU glu = new GLU();

	public AvatarPanel()
	{
		JTabbedPane tabs = new JTabbedPane();
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
		
		JMenuItem mntmTexture = new JMenuItem("Texture");
		mntmTexture.setActionCommand("texture");
		mntmTexture.addActionListener(this);
		mnOpen.add(mntmTexture);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.setActionCommand("exit");
		mntmExit.addActionListener(this);
		mnFile.add(mntmExit);
		
//        panel.add(getGLControl());	
//        getGLControl().requestFocus();	
	}
	
	private File FileDialog(String description, String...extensions)
	{
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new FileNameExtensionFilter(description, extensions));
		int returnVal = fc.showOpenDialog(this);
	    if (returnVal == JFileChooser.APPROVE_OPTION)
	    {
	        return fc.getSelectedFile();
	    }
		Logger.Log("Open command cancelled by user.", LogLevel.Debug);
		return null;
	}

	private void loadAvatarMesh(File file)
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			// Use the factory to create a builder
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file);
			// Get a list of all meshes in the document
			NodeList meshes = doc.getElementsByTagName("mesh");				
	        for (int i = 0; i < meshes.getLength(); i++)
	        {
	        	Node meshNode = meshes.item(i);
	            String type = meshNode.getAttributes().getNamedItem("type").getNodeValue();
	            int lod = Integer.parseInt(meshNode.getAttributes().getNamedItem("lod").getNodeValue());
	            String fileName = meshNode.getAttributes().getNamedItem("file_name").getNodeValue();
	            //string minPixelWidth = meshNode.getAttributes().getNamedItem("min_pixel_width").getNodeValue();

	            // Mash up the filename with the current path
	            fileName = new File(file.getParentFile(), fileName).getPath();

	            LindenMesh mesh = (_meshes.containsKey(type) ? _meshes.get(type) : new LindenMesh(type));
	            if (lod == 0)
	            {
	                mesh.LoadMesh(fileName);
	            }
	            else
	            {
	                mesh.LoadLODMesh(lod, fileName);
	            }

	            _meshes.put(type, mesh);
	            glControl.invalidate();
	        }
		}
		catch (Exception ex)
		{
			Logger.Log("", Logger.LogLevel.Error, ex);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand().equals("animation"))
		{
			File file = FileDialog("Animation File (*.animation, *.bvh)", "animation" ,"bvh");
			if (file != null)
			{
				
			}
		}
		else if (e.getActionCommand().equals("avatar"))
		{
			File file = FileDialog("Avatar File (avatar_lad.xml)", "avatar_lad.xml");
			if (file != null)
			{
				loadAvatarMesh(file);
			}
		}
		else if (e.getActionCommand().equals("texture"))
		{
			File file = FileDialog("Texture File (*.tga, *.png, *.j2k)", "tga", "png", "j2k");
			if (file != null)
			{
				
			}
		}
	}

	private ActionListener getActionListener()
	{
		return this;
	}

	private class AvatarViewer implements GLEventListener
	{
	    @Override
	    public void init(GLAutoDrawable drawable)
	    {
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
		public void display(GLAutoDrawable drawable)
	    {
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

	        if (_meshes.size() > 0)
	        {
	            for (LindenMesh mesh : _meshes.values())
	            {
	                if (!_showSkirt && mesh.getName() == "skirtMesh")
	                    continue;

	                gl.glColor3f(1f, 1f, 1f);

	                // Individual prim matrix
	                gl.glPushMatrix();

	                //GL.glTranslatef(mesh.Position.X, mesh.Position.Y, mesh.Position.Z);

	                gl.glRotatef(mesh.getRotationAngles().X, 1f, 0f, 0f);
	                gl.glRotatef(mesh.getRotationAngles().Y, 0f, 1f, 0f);
	                gl.glRotatef(mesh.getRotationAngles().Z, 0f, 0f, 1f);

	                gl.glScalef(mesh.getScale().X, mesh.getScale().Y, mesh.getScale().Z);

	                // TODO: Texturing

	                gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, mesh.TexCoords);
	                gl.glVertexPointer(3, GL.GL_FLOAT, 0, mesh.Vertices);
	                gl.glDrawElements(GL.GL_TRIANGLES, mesh.Indices.capacity(), GL.GL_UNSIGNED_SHORT, mesh.Indices);
	            }
	        }

	        // Pop the world matrix
	        gl.glPopMatrix();

	        gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
	        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);

	        gl.glFlush();
	    }

		@Override
		public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	    {
	        final GL2 gl = drawable.getGL().getGL2();

	        if (height <= 0) {
	            height = 1;
	        }
	        float aspect = (float) width / (float) height;

	        //GL.glClearColor(0.39f, 0.58f, 0.93f, 1.0f); // Cornflower blue anyone?
	        gl.glClearColor(0f, 0f, 0f, 1f);

	        gl.glPushMatrix();
	        gl.glMatrixMode(GL2.GL_PROJECTION);
	        gl.glLoadIdentity();

	        glu.gluPerspective(50.0, aspect, 0.001, 50);

	        Vector3 center = Vector3.Zero;
	        if (_meshes.containsKey("headMesh") && _meshes.containsKey("lowerBodyMesh"))
	        {
	            LindenMesh head = _meshes.get("headMesh"), lowerBody = _meshes.get("lowerBodyMesh");
	            center = Vector3.divide(Vector3.add(head.Center, lowerBody.Center), 2f);
	        }
	        
	        double value = getJSldZoom().getValue() * 0.1d + center.Y;
	        glu.gluLookAt(center.X, value, center.Z, center.X, value + 1d, center.Z, 0d, 0d, 1d);

	        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
	    }

		@Override
		public void dispose(GLAutoDrawable drawable)
		{
			// TODO Auto-generated method stub
			
		}
	}
	
	protected GLCanvas getGLControl()
	{
		if (glControl == null)
		{
			glControl = new GLCanvas();
	        glControl.addGLEventListener(new AvatarViewer());
		}
		return glControl;
	}
	
	protected JPanel getJPnSettings()
	{
		if (jPnSettings == null)
		{
			jPnSettings = new JPanel();
			jPnSettings.setLayout(new BorderLayout(1, 1));

			JPanel jPnSliders = new JPanel();
			jPnSliders.setBorder(new EmptyBorder(10, 10, 10, 10));
			jPnSettings.add(jPnSliders, BorderLayout.WEST);

			GridBagLayout gbl_jPnSettings = new GridBagLayout();
			gbl_jPnSettings.columnWidths = new int[]{0, 0, 0, 0, 0};
			gbl_jPnSettings.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			gbl_jPnSettings.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
			gbl_jPnSettings.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			jPnSliders.setLayout(gbl_jPnSettings);
			
			JLabel jLblRoll = new JLabel("Roll");
			GridBagConstraints gridBagConstraint = new GridBagConstraints();
			gridBagConstraint.anchor = GridBagConstraints.WEST;
			gridBagConstraint.fill = GridBagConstraints.VERTICAL;
			gridBagConstraint.insets = new Insets(0, 0, 5, 0);
			gridBagConstraint.gridx = 1;
			gridBagConstraint.gridy = 1;
			jPnSliders.add(jLblRoll, gridBagConstraint);
			
			gridBagConstraint = new GridBagConstraints();
			gridBagConstraint.anchor = GridBagConstraints.CENTER;
			gridBagConstraint.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraint.insets = new Insets(0, 0, 5, 0);
			gridBagConstraint.gridx = 3;
			gridBagConstraint.gridy = 1;
			jPnSliders.add(getJSldRoll(), gridBagConstraint);
			
			JLabel jLblPitch = new JLabel("Pitch");
			gridBagConstraint = new GridBagConstraints();
			gridBagConstraint.anchor = GridBagConstraints.WEST;
			gridBagConstraint.fill = GridBagConstraints.VERTICAL;
			gridBagConstraint.insets = new Insets(0, 0, 5, 0);
			gridBagConstraint.gridx = 1;
			gridBagConstraint.gridy = 2;
			jPnSliders.add(jLblPitch, gridBagConstraint);
			
			gridBagConstraint = new GridBagConstraints();
			gridBagConstraint.anchor = GridBagConstraints.CENTER;
			gridBagConstraint.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraint.insets = new Insets(0, 0, 5, 0);
			gridBagConstraint.gridx = 3;
			gridBagConstraint.gridy = 2;
			jPnSliders.add(getJSldPitch(), gridBagConstraint);
			
			JLabel jLblYaw = new JLabel("Yaw");
			gridBagConstraint = new GridBagConstraints();
			gridBagConstraint.anchor = GridBagConstraints.WEST;
			gridBagConstraint.fill = GridBagConstraints.VERTICAL;
			gridBagConstraint.insets = new Insets(0, 0, 5, 0);
			gridBagConstraint.gridx = 1;
			gridBagConstraint.gridy = 3;
			jPnSliders.add(jLblYaw, gridBagConstraint);

			gridBagConstraint = new GridBagConstraints();
			gridBagConstraint.anchor = GridBagConstraints.CENTER;
			gridBagConstraint.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraint.insets = new Insets(0, 0, 5, 0);
			gridBagConstraint.gridx = 3;
			gridBagConstraint.gridy = 3;
			jPnSliders.add(getJSldYaw(), gridBagConstraint);
			
			JLabel jLblZoom = new JLabel("Zoom");
			gridBagConstraint = new GridBagConstraints();
			gridBagConstraint.anchor = GridBagConstraints.WEST;
			gridBagConstraint.fill = GridBagConstraints.VERTICAL;
			gridBagConstraint.insets = new Insets(0, 0, 5, 0);
			gridBagConstraint.gridx = 1;
			gridBagConstraint.gridy = 4;
			jPnSliders.add(jLblZoom, gridBagConstraint);

			gridBagConstraint = new GridBagConstraints();
			gridBagConstraint.anchor = GridBagConstraints.CENTER;
			gridBagConstraint.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraint.insets = new Insets(0, 0, 5, 0);
			gridBagConstraint.gridx = 3;
			gridBagConstraint.gridy = 4;
			jPnSliders.add(getJSldZoom(), gridBagConstraint);
			
			JPanel jPnAnimation = new JPanel();
			jPnAnimation.setBorder(new EmptyBorder(10, 10, 10, 10));
			jPnSettings.add(jPnAnimation, BorderLayout.SOUTH);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0};
			gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0};
			gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
			gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			jPnAnimation.setLayout(gbl_panel);
			
			JLabel lblAnimation = new JLabel("Animation");
			GridBagConstraints gbc_lblAnimation = new GridBagConstraints();
			gbc_lblAnimation.anchor = GridBagConstraints.WEST;
			gbc_lblAnimation.insets = new Insets(0, 0, 5, 5);
			gbc_lblAnimation.gridx = 1;
			gbc_lblAnimation.gridy = 1;
			jPnAnimation.add(lblAnimation, gbc_lblAnimation);
			
			gridBagConstraint = new GridBagConstraints();
			gridBagConstraint.anchor = GridBagConstraints.CENTER;
			gridBagConstraint.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraint.insets = new Insets(0, 0, 5, 5);
			gridBagConstraint.gridx = 1;
			gridBagConstraint.gridy = 2;
			jPnAnimation.add(getJTxtAnimPath(), gridBagConstraint);
			
			JButton jBtnAnimPath = new JButton("Browse");
			jBtnAnimPath.setMnemonic(KeyEvent.VK_B);
			jBtnAnimPath.setActionCommand("animation");
			jBtnAnimPath.addActionListener(getActionListener());
			
			gridBagConstraint = new GridBagConstraints();
			gridBagConstraint.anchor = GridBagConstraints.CENTER;
			gridBagConstraint.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraint.insets = new Insets(0, 0, 5, 5);
			gridBagConstraint.gridx = 2;
			gridBagConstraint.gridy = 2;
			jPnAnimation.add(jBtnAnimPath, gridBagConstraint);
			
			JPanel panel_1 = new JPanel();
			panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
			jPnSettings.add(panel_1, BorderLayout.CENTER);
		}
		return jPnSettings;
	}
	
	private class SliderChangeListener implements ChangeListener
	{
		@Override
		public void stateChanged(ChangeEvent e)
		{
	        getGLControl().invalidate();
		}
	}
	
	protected JSlider getJSldRoll()
	{
		if  (jSldRoll == null)
		{
			jSldRoll = new JSlider();
			jSldRoll.addChangeListener(new SliderChangeListener());
		}
		return jSldRoll;
	}

	public JSlider getJSldPitch()
	{
		if  (jSldPitch == null)
		{
			jSldPitch = new JSlider();
			jSldPitch.addChangeListener(new SliderChangeListener());
		}
		return jSldPitch;
	}

	public JSlider getJSldYaw()
	{
		if  (jSldYaw == null)
		{
			jSldYaw = new JSlider();
			jSldYaw.addChangeListener(new SliderChangeListener());
		}
		return jSldYaw;
	}

	public JSlider getJSldZoom()
	{
		if  (jSldZoom == null)
		{
			jSldZoom = new JSlider();
			jSldZoom.addChangeListener(new SliderChangeListener());
		}
		return jSldZoom;
	}

	protected JTextField getJTxtAnimPath()
	{
		if (jTxtAnimPath == null)
		{
			jTxtAnimPath = new JTextField();
			jTxtAnimPath.setColumns(10);
		}
		return jTxtAnimPath;
	}
	
	protected JPanel getJPnTextures()
	{
		if (jPnTextures == null)
		{
			jPnTextures = new JPanel();
			jPnTextures.setLayout(new FormLayout(new ColumnSpec[] {
					ColumnSpec.decode("10px"),
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					ColumnSpec.decode("90px"),
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					ColumnSpec.decode("10px"),
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					ColumnSpec.decode("90px"),
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					ColumnSpec.decode("10px"),
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					ColumnSpec.decode("90px"),
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					ColumnSpec.decode("10px"),
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					ColumnSpec.decode("90px"),
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					ColumnSpec.decode("10px"),
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					ColumnSpec.decode("90px"),
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					ColumnSpec.decode("10px"),
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					ColumnSpec.decode("90px"),
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					ColumnSpec.decode("10px"),
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					ColumnSpec.decode("90px"),
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					ColumnSpec.decode("10px"),},
				new RowSpec[] {
					FormFactory.LINE_GAP_ROWSPEC,
					RowSpec.decode("10px"),
					FormFactory.LINE_GAP_ROWSPEC,
					RowSpec.decode("90px"),
					FormFactory.LINE_GAP_ROWSPEC,
					RowSpec.decode("10px"),
					FormFactory.LINE_GAP_ROWSPEC,
					RowSpec.decode("90px"),
					FormFactory.LINE_GAP_ROWSPEC,
					RowSpec.decode("10px"),
					FormFactory.LINE_GAP_ROWSPEC,
					RowSpec.decode("90px"),
					FormFactory.LINE_GAP_ROWSPEC,
					RowSpec.decode("10px"),
					FormFactory.LINE_GAP_ROWSPEC,
					RowSpec.decode("90px"),
					FormFactory.LINE_GAP_ROWSPEC,
					RowSpec.decode("10px"),
					FormFactory.LINE_GAP_ROWSPEC,
					RowSpec.decode("90px"),
					FormFactory.LINE_GAP_ROWSPEC,
					RowSpec.decode("10px"),}));
			
			
			JLabel lblHead = new JLabel("Head");
			jPnTextures.add(lblHead, "2, 2, 2, 1, center, center");
			
//			ImagePanel icon1 = new ImagePanel();

			JPanel panel11 = new JPanel();
			panel11.setBorder(new LineBorder(new Color(0, 0, 0)));
			jPnTextures.add(panel11, "2, 4, 2, 1, fill, fill");
			
			JLabel plus11 = new JLabel("+");
			jPnTextures.add(plus11, "4, 4, 2, 1, center, center");
			
			JPanel panel12 = new JPanel();
			panel12.setBorder(new LineBorder(new Color(0, 0, 0)));
			jPnTextures.add(panel12, "6, 4, 2, 1, fill, fill");
			
			JLabel equal1 = new JLabel("=");
			jPnTextures.add(equal1, "8, 4, 2, 1, center, center");
			
			JPanel panel13 = new JPanel();
			panel13.setBorder(new LineBorder(new Color(0, 0, 0)));
			jPnTextures.add(panel13, "10, 4, 2, 1, fill, fill");
			
			JLabel lblEyes = new JLabel("Eyes");
			jPnTextures.add(lblEyes, "2, 6, 2, 1, center, center");
			
			JPanel panel21 = new JPanel();
			panel21.setBorder(new LineBorder(new Color(0, 0, 0)));
			jPnTextures.add(panel21, "2, 8, 2, 1, fill, fill");
			
			JLabel lblUpper = new JLabel("Upper");
			jPnTextures.add(lblUpper, "2, 10, 2, 1, center, center");
			
			JPanel panel31 = new JPanel();
			panel31.setBorder(new LineBorder(new Color(0, 0, 0)));
			jPnTextures.add(panel31, "2, 12, 2, 1, fill, fill");
			
			JLabel plus31 = new JLabel("+");
			jPnTextures.add(plus31, "4, 12, 2, 1, center, center");
			
			JPanel panel32 = new JPanel();
			panel32.setBorder(new LineBorder(new Color(0, 0, 0)));
			jPnTextures.add(panel32, "6, 12, 2, 1, fill, fill");

			JLabel plus32 = new JLabel("+");
			jPnTextures.add(plus32, "8, 12, 2, 1, center, center");
			
			JPanel panel33 = new JPanel();
			panel33.setBorder(new LineBorder(new Color(0, 0, 0)));
			jPnTextures.add(panel33, "10, 12, 2, 1, fill, fill");
			
			JLabel plus33 = new JLabel("+");
			jPnTextures.add(plus33, "12, 12, 2, 1, center, center");
			
			JPanel panel34 = new JPanel();
			panel34.setBorder(new LineBorder(new Color(0, 0, 0)));
			jPnTextures.add(panel34, "14, 12, 2, 1, fill, fill");

			JLabel plus34 = new JLabel("+");
			jPnTextures.add(plus34, "16, 12, 2, 1, center, center");
			
			JPanel panel35 = new JPanel();
			panel35.setBorder(new LineBorder(new Color(0, 0, 0)));
			jPnTextures.add(panel35, "18, 12, 2, 1, fill, fill");

			JLabel equal3 = new JLabel("=");
			jPnTextures.add(equal3, "20, 12, 2, 1, center, center");
			
			JPanel panel36 = new JPanel();
			panel36.setBorder(new LineBorder(new Color(0, 0, 0)));
			jPnTextures.add(panel36, "18, 12, 2, 1, fill, fill");

			JLabel lblLower = new JLabel("Lower");
			jPnTextures.add(lblLower, "2, 14, 2, 1, center, center");
			
			JPanel panel41 = new JPanel();
			panel41.setBorder(new LineBorder(new Color(0, 0, 0)));
			jPnTextures.add(panel41, "2, 16, 2, 1, fill, fill");
			
			JLabel plus41 = new JLabel("+");
			jPnTextures.add(plus41, "4, 16, 2, 1, center, center");
			
			JPanel panel42 = new JPanel();
			panel42.setBorder(new LineBorder(new Color(0, 0, 0)));
			jPnTextures.add(panel42, "6, 16, 2, 1, fill, fill");
			
			JLabel plus42 = new JLabel("+");
			jPnTextures.add(plus42, "8, 16, 2, 1, center, center");
			
			JPanel panel43 = new JPanel();
			panel43.setBorder(new LineBorder(new Color(0, 0, 0)));
			jPnTextures.add(panel43, "10, 16, 2, 1, fill, fill");
			
			JLabel plus43 = new JLabel("+");
			jPnTextures.add(plus43, "12, 16, 2, 1, center, center");
			
			JPanel panel44 = new JPanel();
			panel44.setBorder(new LineBorder(new Color(0, 0, 0)));
			jPnTextures.add(panel44, "14, 16, 2, 1, fill, fill");
			
			JLabel plus44 = new JLabel("+");
			jPnTextures.add(plus44, "16, 16, 2, 1, center, center");
			
			JPanel panel45 = new JPanel();
			panel45.setBorder(new LineBorder(new Color(0, 0, 0)));
			jPnTextures.add(panel45, "18, 16, 2, 1, fill, fill");
			
			JLabel plus45 = new JLabel("+");
			jPnTextures.add(plus45, "20, 16, 2, 1, center, center");
			
			JPanel panel46 = new JPanel();
			panel46.setBorder(new LineBorder(new Color(0, 0, 0)));
			jPnTextures.add(panel46, "22, 16, 2, 1, fill, fill");
			
			JLabel equal4 = new JLabel("=");
			jPnTextures.add(equal4, "24, 16, 2, 1, center, center");
			
			JPanel panel47 = new JPanel();
			panel47.setBorder(new LineBorder(new Color(0, 0, 0)));
			jPnTextures.add(panel47, "26, 16, 2, 1, fill, fill");
			
			JLabel lblSkirt = new JLabel("Skirt");
			jPnTextures.add(lblSkirt, "2, 18, 2, 1, center, center");
			
			JPanel panel51 = new JPanel();
			panel51.setBorder(new LineBorder(new Color(0, 0, 0)));
			jPnTextures.add(panel51, "2, 20, 2, 1, fill, fill");
		}
		return jPnTextures;
	}
}