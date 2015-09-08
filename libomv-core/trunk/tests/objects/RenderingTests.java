package objects;

import junit.framework.TestCase;
import libomv.GridClient;
import libomv.rendering.LindenSkeleton;

public class RenderingTests extends TestCase
{
    public void testLindenSkeleton() throws Exception
    {
    	GridClient client = new GridClient();
    	LindenSkeleton skeleton = LindenSkeleton.load(client);
    	assertTrue("Loading of skeleton failed", skeleton != null);
    }
}
