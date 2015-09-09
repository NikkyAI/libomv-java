package objects;

import junit.framework.TestCase;
import libomv.rendering.LindenSkeleton;

public class RenderingTests extends TestCase
{
    public void testLindenSkeleton() throws Exception
    {
    	LindenSkeleton skeleton = LindenSkeleton.load();
    	assertTrue("Loading of skeleton failed", skeleton != null);
    }
}
