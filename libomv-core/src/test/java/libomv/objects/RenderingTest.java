/**
 * Copyright (c) 2012-2017, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Neither the name of the openmetaverse.org nor the names
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
package libomv.objects;

import junit.framework.TestCase;
import libomv.rendering.LindenSkeleton;

public class RenderingTest extends TestCase {
	private static final ClassLoader classLoader = RenderingTest.class.getClassLoader();

	public void testLindenSkeleton() throws Exception {
		LindenSkeleton skeleton = LindenSkeleton.load();
		assertTrue("Loading of skeleton failed", skeleton != null);
	}

	/*
	 * public void testLindenMesh() throws Exception {
	 * 
	 * File meshFile = new File(
	 * classLoader.getResource("character/avatar_lad.xml").getFile() );
	 * 
	 * DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	 * DocumentBuilder builder = factory.newDocumentBuilder(); Document doc =
	 * builder.parse(meshFile);
	 * 
	 * NodeList meshes = doc.getElementsByTagName("mesh"); for (int i = 0; i <
	 * meshes.getLength(); i++) { Node meshNode = meshes.item(i); String type =
	 * meshNode.getAttributes().getNamedItem("type").getNodeValue(); int lod =
	 * Integer.parseInt(meshNode.getAttributes().getNamedItem("lod").getNodeValue())
	 * ; String fileName =
	 * meshNode.getAttributes().getNamedItem("file_name").getNodeValue();
	 * 
	 * // Mash up the filename with the current path fileName = new
	 * File(meshFile.getParentFile(), fileName).getPath();
	 * 
	 * LindenMesh mesh = new LindenMesh(type); if (lod == 0) { mesh.load(fileName);
	 * } else { mesh.loadLod(lod, fileName); } System.out.println(mesh); } }
	 */
}
