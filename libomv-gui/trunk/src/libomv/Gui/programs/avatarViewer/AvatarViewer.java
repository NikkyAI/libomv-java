package libomv.Gui.programs.avatarViewer;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;

import libomv.rendering.LindenMesh;
import libomv.types.Vector3;

public class AvatarViewer implements GLEventListener, KeyListener
{
    HashMap<String, LindenMesh> _meshes = new HashMap<String, LindenMesh>();
    boolean _wireframe = true;
    boolean _showSkirt = false;

    static GLU glu = new GLU();
    static GLCanvas glControl = new GLCanvas();
    static Frame frame = new Frame("libomv Avatar Viewer");

    static public void main(String[] args)
	{
        glControl.addGLEventListener(new AvatarViewer());
        frame.add(glControl);
        frame.setSize(640, 480);
        frame.setUndecorated(true);
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.addWindowListener(new WindowAdapter()
        {
            @Override
			public void windowClosing(WindowEvent e)
            {
                exit();
            }
        });
        frame.setVisible(true);
        glControl.requestFocus();
		
	}

    public static void exit() {
        frame.dispose();
        System.exit(0);
    }
 
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
        ((Component)drawable).addKeyListener(this);
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
        gl.glRotatef((float)scrollRoll.Value, 1f, 0f, 0f);
        gl.glRotatef((float)scrollPitch.Value, 0f, 1f, 0f);
        gl.glRotatef((float)scrollYaw.Value, 0f, 0f, 1f);

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
        
        glu.gluLookAt(
                center.X, (double)scrollZoom * 0.1d + center.Y, center.Z,
                center.X, (double)scrollZoom * 0.1d + center.Y + 1d, center.Z,
                0d, 0d, 1d);

        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
    }

    private void scroll_ValueChanged()
    {
        glControl.invalidate();
    }

	@Override
	public void keyTyped(KeyEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose(GLAutoDrawable drawable)
	{
		// TODO Auto-generated method stub
		
	}
}
